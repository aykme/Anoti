#!/bin/bash
# Signs in to the App Store Connect API with the CI's key and looks the app up by its bundle ID.
# It then checks the build to upload is not there yet. Either failure shows in seconds, with
# Apple's answer, not after the archive.
# Env: KEY_ID, ISSUER_ID and KEY_P8, the key's secrets; BUNDLE_ID, VERSION_NAME and VERSION_CODE,
# the app's.
set -euo pipefail

if [ -z "${KEY_ID:-}" ] || [ -z "${ISSUER_ID:-}" ] || [ -z "${KEY_P8:-}" ]; then
  echo "An APP_STORE_CONNECT_* secret is missing"
  exit 1
fi
if [ -z "${BUNDLE_ID:-}" ] || [ -z "${VERSION_NAME:-}" ] || [ -z "${VERSION_CODE:-}" ]; then
  echo "BUNDLE_ID, VERSION_NAME or VERSION_CODE is missing"
  exit 1
fi

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
key="$work/AuthKey.p8"
# A key pasted on Windows may carry CR line ends.
(umask 077 && printf '%s\n' "$KEY_P8" | tr -d '\r' > "$key")

if ! openssl pkey -in "$key" -noout 2> /dev/null; then
  echo "The APP_STORE_CONNECT_KEY_P8 secret holds no private key"
  exit 1
fi

base64url() { openssl base64 -A | tr '+/' '-_' | tr -d '='; }

# Apple's token: ES256 over the key ID and the issuer. Apple takes at most 20 minutes; this one
# lives 10.
now=$(date +%s)
header=$(printf '{"alg":"ES256","kid":"%s","typ":"JWT"}' "$KEY_ID" | base64url)
claims=$(printf '{"iss":"%s","iat":%d,"exp":%d,"aud":"appstoreconnect-v1"}' \
  "$ISSUER_ID" "$now" $((now + 600)) | base64url)
input="$header.$claims"

# openssl signs in DER. A JWT takes the two integers R and S as 32 bytes each, side by side.
signature_hex=""
while read -r integer; do
  padded=$(printf '%64s' "$integer" | tr ' ' 0)
  signature_hex+=${padded: -64}
done < <(printf '%s' "$input" | openssl dgst -sha256 -sign "$key" \
  | openssl asn1parse -inform DER | awk -F: '/INTEGER/ { print $NF }')
if [ ${#signature_hex} -ne 128 ]; then
  echo "openssl could not sign the token with the key"
  exit 1
fi
signature=$(printf '%s' "$signature_hex" | xxd -r -p | base64url)

# api <path> <file>: gets an API path into the file and prints the HTTP status.
api() {
  curl -sS -g --max-time 60 -o "$2" -w '%{http_code}' \
    -H "Authorization: Bearer $input.$signature" "https://api.appstoreconnect.apple.com$1"
}

status=$(api "/v1/apps?filter[bundleId]=$BUNDLE_ID" "$work/apps.json")
if [ "$status" != 200 ]; then
  # 401: the key, its ID or the issuer ID is wrong, or the key was revoked.
  echo "App Store Connect refused the key with HTTP $status:"
  cat "$work/apps.json"
  echo
  exit 1
fi
# The filter may match more than the exact ID, so the app is picked by it.
app_id=$(/usr/bin/python3 - "$work/apps.json" "$BUNDLE_ID" <<'PY'
import json, sys
apps = json.load(open(sys.argv[1]))["data"]
print(next((a["id"] for a in apps if a["attributes"]["bundleId"] == sys.argv[2]), ""))
PY
)
if [ -z "$app_id" ]; then
  echo "The key signs in, but App Store Connect has no app with the bundle ID $BUNDLE_ID"
  exit 1
fi
echo "The key signs in, and App Store Connect has the app $BUNDLE_ID"

# Each version is uploaded once. A build of it already there needs a catalog bump first.
builds_path="/v1/builds?filter[app]=$app_id&filter[version]=$VERSION_CODE"
builds_path+="&filter[preReleaseVersion.version]=$VERSION_NAME&limit=1"
status=$(api "$builds_path" "$work/builds.json")
if [ "$status" != 200 ]; then
  echo "App Store Connect answered the build lookup with HTTP $status:"
  cat "$work/builds.json"
  echo
  exit 1
fi
builds=$(/usr/bin/python3 -c 'import json, sys; print(len(json.load(open(sys.argv[1]))["data"]))' \
  "$work/builds.json")
if [ "$builds" != 0 ]; then
  echo "App Store Connect already has $VERSION_NAME ($VERSION_CODE)."
  echo "Raise versionName and versionCode in gradle/libs.versions.toml."
  exit 1
fi
echo "App Store Connect has no build $VERSION_NAME ($VERSION_CODE) yet"
