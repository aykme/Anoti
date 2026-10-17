#!/bin/bash
# Boots a simulator on the iOS runtime of the selected Xcode and exports its id as SIM_UDID. The
# first device whose name starts with SIM_DEVICE is taken, an iPhone 17 when it is unset. Every
# device the runtime offers is listed first. CONTENT_SIZE, when set, is the Dynamic Type size the
# simulator gets once booted, by the name simctl takes.
set -euo pipefail

runtime_key="iOS-${XCODE_VERSION//./-}"
udid=$(xcrun simctl list devices available -j | /usr/bin/python3 -c '
import json, sys
key, wanted = sys.argv[1], sys.argv[2]
devices = json.load(sys.stdin)["devices"]
runtimes = sorted(name for name in devices if key in name)
if not runtimes:
    sys.exit("no runtime matching " + key)
offered = devices[runtimes[-1]]
print("Devices on " + runtimes[-1] + ": " + ", ".join(d["name"] for d in offered), file=sys.stderr)
print(next(d["udid"] for d in offered if d["name"].startswith(wanted)))
' "$runtime_key" "${SIM_DEVICE:-iPhone 17}")
echo "Simulator: ${SIM_DEVICE:-iPhone 17} on $runtime_key, $udid"
xcrun simctl boot "$udid"
xcrun simctl bootstatus "$udid" -b
if [ -n "${CONTENT_SIZE:-}" ]; then
  xcrun simctl ui "$udid" content_size "$CONTENT_SIZE"
  echo "Text size: $(xcrun simctl ui "$udid" content_size)"
fi
echo "SIM_UDID=$udid" >> "$GITHUB_ENV"
