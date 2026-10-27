#!/bin/bash
# Signs in to the App Store Connect API with the CI's key and looks the app up by its bundle ID.
# A refused key then fails with Apple's answer, not later as xcodebuild's missing profiles.
# Env: KEY_ID, ISSUER_ID and KEY_P8, the key's secrets; BUNDLE_ID, the app's.
set -euo pipefail

if [ -z "${KEY_ID:-}" ] || [ -z "${ISSUER_ID:-}" ] || [ -z "${KEY_P8:-}" ]; then
  echo "An APP_STORE_CONNECT_* secret is missing"
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

status=$(curl -sS -g --max-time 60 -o "$work/body.json" -w '%{http_code}' \
  -H "Authorization: Bearer $input.$signature" \
  "https://api.appstoreconnect.apple.com/v1/apps?filter[bundleId]=$BUNDLE_ID")

if [ "$status" != 200 ]; then
  # 401: the key, its ID or the issuer ID is wrong, or the key was revoked.
  echo "App Store Connect refused the key with HTTP $status:"
  cat "$work/body.json"
  echo
  exit 1
fi
# The filter may match more than the exact ID, so the answer must name it.
bundle_pattern=$(printf '%s' "$BUNDLE_ID" | sed 's/\./\\./g')
if ! grep -Eq "\"bundleId\"[[:space:]]*:[[:space:]]*\"$bundle_pattern\"" "$work/body.json"; then
  echo "The key signs in, but App Store Connect has no app with the bundle ID $BUNDLE_ID"
  exit 1
fi
echo "The key signs in, and App Store Connect has the app $BUNDLE_ID"
