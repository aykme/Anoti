#!/bin/bash
# TEMPORARY: repeats "UI tests, reset, restore case 1" PROBE_ITERATIONS times with no retry.
# PROBE_RESET picks what runs between the UI tests and the case: erase (the simulator is erased
# and booted again), reboot (shut down and booted, nothing erased) or runner (the UI-test runner
# app is uninstalled).
set -uo pipefail
source .github/scripts/ios-restore-checks.sh

reboot_simulator() {
  xcrun simctl shutdown "$SIM_UDID"
  [ "$1" = erase ] && xcrun simctl erase "$SIM_UDID"
  xcrun simctl boot "$SIM_UDID"
  xcrun simctl bootstatus "$SIM_UDID" -b > /dev/null
}

iterations=${PROBE_ITERATIONS:-6}
passed=0
for i in $(seq 1 "$iterations"); do
  echo "== probe iteration $i, reset: $PROBE_RESET"
  MEDIA_DIR="$media_root/iter-$i"
  RESULTS_DIR="$results_root/iter-$i"
  mkdir -p "$MEDIA_DIR" "$RESULTS_DIR"
  xcodebuild test-without-building -xctestrun "$XCTESTRUN" -destination "id=$SIM_UDID" \
    -only-testing:iosAppUITests/AnimeFavoritesUserFlowUITests \
    -only-testing:iosAppUITests/OrientationUITests \
    -resultBundlePath "$RESULTS_DIR/ui-tests.xcresult" > "$MEDIA_DIR/ui-tests.log" 2>&1 \
    || echo "the UI tests failed"
  started=$(date +%s)
  case "$PROBE_RESET" in
    erase | reboot) reboot_simulator "$PROBE_RESET" ;;
    runner) xcrun simctl uninstall "$SIM_UDID" com.alekseivinogradov.anoti.uitests.xctrunner ;;
  esac
  echo "the reset took $(($(date +%s) - started)) s"
  case_failures=0
  case_kept_across_a_termination
  for run in prepare before relaunch; do
    echo "$run: $(grep -h "the scene kept" "$MEDIA_DIR/case1-$run.log" 2> /dev/null)"
  done
  if [ "$case_failures" -eq 0 ]; then
    passed=$((passed + 1))
    echo "PROBE iteration $i: PASS"
  else
    echo "PROBE iteration $i: FAIL"
  fi
done
echo "PROBE passed $passed of $iterations"
