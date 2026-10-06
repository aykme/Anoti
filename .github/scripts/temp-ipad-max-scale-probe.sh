#!/bin/bash
# Temporary: sets the largest text size and Display Zoom on the booted iPad and on an iPad mini,
# then measures the app's window on each. Every result is evidence to read, so nothing here fails
# the job.
set -uo pipefail

probe() {
    local name=$1 udid=$2
    echo "== $name ($udid)"
    xcrun simctl ui "$udid" content_size accessibility-extra-extra-extra-large \
        || echo "content size was not set"
    xcrun simctl io "$udid" recordVideo --codec=h264 --force "$MEDIA_DIR/probe-$name.mp4" &
    local recorder=$!
    sleep 2
    TEST_RUNNER_ANOTI_MAX_SCALE=1 xcodebuild test-without-building -xctestrun "$XCTESTRUN" \
        -destination "id=$udid" \
        -only-testing:iosAppUITests/MaxScaleProbeUITests/testSetLargerTextDisplayZoom \
        -resultBundlePath "$RESULTS_DIR/probe-zoom-$name.xcresult" \
        2>&1 | tee "$MEDIA_DIR/probe-zoom-$name.log"
    # Display Zoom restarts the home screen, which can end the run above. A booted device
    # reports booted at once, so the restart is waited out.
    xcrun simctl bootstatus "$udid" -b
    sleep 30
    TEST_RUNNER_ANOTI_MAX_SCALE=1 xcodebuild test-without-building -xctestrun "$XCTESTRUN" \
        -destination "id=$udid" \
        -only-testing:iosAppUITests/MaxScaleProbeUITests/testMeasureTheAppAtMaxScale \
        -resultBundlePath "$RESULTS_DIR/probe-measure-$name.xcresult" \
        2>&1 | tee "$MEDIA_DIR/probe-measure-$name.log"
    kill -INT "$recorder" 2> /dev/null
    wait "$recorder"
}

probe ipad-air "$SIM_UDID"
xcrun simctl shutdown "$SIM_UDID"

mini_env=$(mktemp)
if SIM_DEVICE="iPad mini" GITHUB_ENV="$mini_env" bash .github/scripts/ios-boot-simulator.sh; then
    probe ipad-mini "$(sed -n 's/^SIM_UDID=//p' "$mini_env")"
else
    echo "no iPad mini on this runner"
fi
exit 0
