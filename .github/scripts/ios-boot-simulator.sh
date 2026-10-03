#!/bin/bash
# Boots an iPhone 17 on the iOS runtime of the selected Xcode and exports its id as SIM_UDID.
set -euo pipefail

runtime_key="iOS-${XCODE_VERSION//./-}"
udid=$(xcrun simctl list devices available -j | /usr/bin/python3 -c '
import json, sys
key = sys.argv[1]
devices = json.load(sys.stdin)["devices"]
runtimes = sorted(name for name in devices if key in name)
if not runtimes:
    sys.exit("no runtime matching " + key)
print(next(d["udid"] for d in devices[runtimes[-1]] if d["name"] == "iPhone 17"))
' "$runtime_key")
echo "Simulator: iPhone 17 on $runtime_key, $udid"
xcrun simctl boot "$udid"
xcrun simctl bootstatus "$udid" -b
echo "SIM_UDID=$udid" >> "$GITHUB_ENV"
