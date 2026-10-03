#!/bin/bash
# TEMPORARY: repeats restore case 1 PROBE_ITERATIONS times with no retry and keeps the system log
# of each iteration, to find why the case sometimes relaunches without its state.
set -uo pipefail
source .github/scripts/ios-restore-checks.sh

iterations=${PROBE_ITERATIONS:-10}
passed=0
for i in $(seq 1 "$iterations"); do
  MEDIA_DIR="$media_root/iter-$i"
  RESULTS_DIR="$results_root/iter-$i"
  mkdir -p "$MEDIA_DIR" "$RESULTS_DIR"
  xcrun simctl spawn "$SIM_UDID" log stream --style compact --level info \
    --predicate 'process == "Anoti" OR eventMessage CONTAINS[c] "alekseivinogradov"' \
    > "$MEDIA_DIR/system.log" 2>&1 &
  streamer=$!
  echo "== probe iteration $i"
  case_failures=0
  case_kept_across_a_termination
  kill "$streamer" 2> /dev/null
  wait "$streamer" 2> /dev/null
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
