#!/bin/bash
# TEMPORARY: repeats "UI tests, then the whole restore checks" PROBE_ITERATIONS times, to prove
# that no restore case needs a second try right after the UI tests.
set -uo pipefail
: "${MEDIA_DIR:?}" "${RESULTS_DIR:?}"
root_media=$MEDIA_DIR
root_results=$RESULTS_DIR

for i in $(seq 1 "${PROBE_ITERATIONS:-2}"); do
  echo "== probe iteration $i"
  export MEDIA_DIR="$root_media/iter-$i"
  export RESULTS_DIR="$root_results/iter-$i"
  mkdir -p "$MEDIA_DIR" "$RESULTS_DIR"
  xcodebuild test-without-building -xctestrun "$XCTESTRUN" -destination "id=$SIM_UDID" \
    -only-testing:iosAppUITests/AnimeFavoritesUserFlowUITests \
    -only-testing:iosAppUITests/OrientationUITests \
    -resultBundlePath "$RESULTS_DIR/ui-tests.xcresult" > "$MEDIA_DIR/ui-tests.log" 2>&1
  echo "the UI tests exited with $?"
  grep -E "Test Case .* (passed|failed|skipped)" "$MEDIA_DIR/ui-tests.log" | sed 's/^/  /'
  bash .github/scripts/ios-restore-checks.sh > "$MEDIA_DIR/restore-checks.log" 2>&1
  echo "the restore checks exited with $?"
  grep -E "^(== |FAIL|.* failed on try|Restore checks failed)" "$MEDIA_DIR/restore-checks.log" \
    | sed 's/^/  /'
done
