#!/bin/bash
# Signs in to the App Store Connect API with the CI's key and looks the app up by its bundle ID.
# A refused key then fails with Apple's answer, not later as xcodebuild's missing profiles.
# Usage: asc-key-check.sh <AuthKey.p8> <key ID> <issuer ID> <bundle ID>
set -euo pipefail

if [ $# -ne 4 ]; then
  echo "usage: asc-key-check.sh <AuthKey.p8> <key ID> <issuer ID> <bundle ID>" >&2
  exit 2
fi
key=$1 key_id=$2 issuer_id=$3 bundle_id=$4

if ! openssl pkey -in "$key" -noout 2> /dev/null; then
  echo "The APP_STORE_CONNECT_KEY_P8 secret holds no private key"
  exit 1
fi

base64url() { openssl base64 -A | tr '+/' '-_' | tr -d '='; }

# Apple's token: ES256 over the key ID and the issuer, valid for at most 20 minutes.
now=$(date +%s)
header=$(printf '{"alg":"ES256","kid":"%s","typ":"JWT"}' "$key_id" | base64url)
claims=$(printf '{"iss":"%s","iat":%d,"exp":%d,"aud":"appstoreconnect-v1"}' \
  "$issuer_id" "$now" $((now + 600)) | base64url)
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

body=$(mktemp)
trap 'rm -f "$body"' EXIT
status=$(curl -sS -g -o "$body" -w '%{http_code}' \
  -H "Authorization: Bearer $input.$signature" \
  "https://api.appstoreconnect.apple.com/v1/apps?filter[bundleId]=$bundle_id")

if [ "$status" != 200 ]; then
  # 401: the key, its ID or the issuer ID is wrong, or the key was revoked.
  echo "App Store Connect refused the key with HTTP $status:"
  cat "$body"
  echo
  exit 1
fi
if grep -Eq '"data" *: *\[ *\]' "$body"; then
  echo "The key signs in, but App Store Connect has no app with the bundle ID $bundle_id"
  exit 1
fi
echo "The key signs in, and App Store Connect has the app $bundle_id"
