#!/bin/bash
# Proves in a minute or two that xcodebuild's own sign-in takes the App Store Connect key. It
# archives a one-screen app with the real bundle ID, unsigned, and exports it without uploading.
# A refused key then fails here, before the app's long archive.
# Env: as testflight-export.sh, and xcodegen on PATH.
set -euo pipefail

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
mkdir "$work/Sources"
cat > "$work/Sources/ProbeApp.swift" <<'SWIFT'
import SwiftUI

@main
struct ProbeApp: App {
    var body: some Scene {
        WindowGroup { Text("Probe") }
    }
}
SWIFT
cat > "$work/project.yml" <<YAML
name: Probe
options:
  deploymentTarget:
    iOS: "16.0"
targets:
  Probe:
    type: application
    platform: iOS
    sources: [Sources]
    settings:
      base:
        PRODUCT_BUNDLE_IDENTIFIER: $BUNDLE_ID
        DEVELOPMENT_TEAM: $TEAM_ID
        SWIFT_VERSION: "5.0"
        GENERATE_INFOPLIST_FILE: YES
        INFOPLIST_KEY_UILaunchScreen_Generation: YES
        MARKETING_VERSION: "1.0"
        CURRENT_PROJECT_VERSION: "1"
schemes:
  Probe:
    build:
      targets:
        Probe: all
    archive:
      config: Release
YAML

(cd "$work" && xcodegen generate --quiet)
xcodebuild -project "$work/Probe.xcodeproj" -scheme Probe -configuration Release \
  -destination 'generic/platform=iOS' -archivePath "$work/Probe.xcarchive" \
  -derivedDataPath "$work/DerivedData" CODE_SIGNING_ALLOWED=NO archive \
  > "$work/archive.log" 2>&1 || { tail -40 "$work/archive.log"; exit 1; }
bash "$(dirname "$0")/testflight-export.sh" "$work/Probe.xcarchive" export
echo "xcodebuild signed in with the key and exported the probe"
