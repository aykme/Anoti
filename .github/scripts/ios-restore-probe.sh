#!/bin/bash
# TEMPORARY: repeats "UI tests, then restore case 1" PROBE_ITERATIONS times with no retry. With
# PROBE_SETTLE=1 the scene is settled between the two, as the restore checks do.
set -uo pipefail
source .github/scripts/ios-restore-checks.sh

iterations=${PROBE_ITERATIONS:-6}
passed=0
for i in $(seq 1 "$iterations"); do
  echo "== probe iteration $i"
  MEDIA_DIR="$media_root/iter-$i"
  RESULTS_DIR="$results_root/iter-$i"
  mkdir -p "$MEDIA_DIR" "$RESULTS_DIR"
  xcodebuild test-without-building -xctestrun "$XCTESTRUN" -destination "id=$SIM_UDID" \
    -only-testing:iosAppUITests/AnimeFavoritesUserFlowUITests \
    -only-testing:iosAppUITests/OrientationUITests \
    -resultBundlePath "$RESULTS_DIR/ui-tests.xcresult" > "$MEDIA_DIR/ui-tests.log" 2>&1 \
    || echo "the UI tests failed"
  if [ "${PROBE_SETTLE:-0}" = 1 ]; then
    settle_the_scene 4
    for log in "$media_root"/settle/settle-*.log; do
      echo "$(basename "$log"): $(grep -h "the scene kept" "$log")"
    done
    mv "$media_root/settle" "$MEDIA_DIR/settle"
    mv "$results_root/settle" "$RESULTS_DIR/settle"
  fi
  MEDIA_DIR="$media_root/iter-$i"
  RESULTS_DIR="$results_root/iter-$i"
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
