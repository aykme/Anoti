#!/bin/bash
# Exports an archive built unsigned for the App Store. The app is first signed ad hoc, then the
# export signs it through Apple's cloud certificate and the App Store Connect key. Destination
# "upload" also sends it to TestFlight; "export" only writes the .ipa, which is then dropped.
# Usage: testflight-export.sh <archive> <upload|export>
# Env: TEAM_ID, BUNDLE_ID; KEY_ID, ISSUER_ID and KEY_P8, the key's secrets.
set -euo pipefail

archive=$1 destination=$2
app=$(find "$archive/Products/Applications" -maxdepth 1 -name '*.app' | head -1)
if [ -z "$app" ]; then
  echo "The archive holds no app: $archive"
  exit 1
fi

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

# The export reads the app's entitlements from its signature. So the app is signed ad hoc with the
# identifier and team Xcode would give it. PlistBuddy takes the dotted key whole.
/usr/libexec/PlistBuddy \
  -c "Add :application-identifier string $TEAM_ID.$BUNDLE_ID" \
  -c "Add :com.apple.developer.team-identifier string $TEAM_ID" "$work/entitlements.plist"
codesign --force --sign - --timestamp=none --generate-entitlement-der \
  --entitlements "$work/entitlements.plist" "$app"
codesign --verify --deep --strict "$app"
codesign --display --entitlements - "$app"

/usr/libexec/PlistBuddy \
  -c "Add :method string app-store-connect" \
  -c "Add :destination string $destination" \
  -c "Add :teamID string $TEAM_ID" \
  -c "Add :signingStyle string automatic" \
  -c "Add :uploadSymbols bool true" \
  -c "Add :manageAppVersionAndBuildNumber bool false" "$work/ExportOptions.plist"

# The key lives on disk only for this export.
(umask 077 && printf '%s\n' "$KEY_P8" | tr -d '\r' > "$work/AuthKey.p8")

# The log names the key's IDs, and GitHub masks them only in the console. So it stays in this
# temporary folder, and only its tail is printed.
log="$work/export.log"
if xcodebuild -exportArchive -archivePath "$archive" \
  -exportOptionsPlist "$work/ExportOptions.plist" -exportPath "$work/export" \
  -allowProvisioningUpdates -authenticationKeyPath "$work/AuthKey.p8" \
  -authenticationKeyID "$KEY_ID" -authenticationKeyIssuerID "$ISSUER_ID" > "$log" 2>&1; then
  tail -20 "$log"
  exit 0
fi
tail -80 "$log"
# The summary of Xcode's distribution logs only: the verbose one holds the requests.
find "${TMPDIR:-/tmp}" -name 'IDEDistribution.standard.log' -path '*.xcdistributionlogs/*' \
  -exec cat {} \; 2> /dev/null | tail -80
exit 1
