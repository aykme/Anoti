#!/bin/bash
# Temporary: finds which way of ending the app, and how long after it leaves the screen, lets iOS
# bring its scene state back. Prints one result line per try.
set -uo pipefail
: "${SIM_UDID:?}" "${MEDIA_DIR:?}" "${APP_PATH:?}" "${XCTESTRUN:?}" "${RESULTS_DIR:?}"

bundle_id=com.alekseivinogradov.anoti

steps_xctestrun="$(dirname "$XCTESTRUN")/restore-steps.xctestrun"
/usr/bin/python3 - "$XCTESTRUN" "$steps_xctestrun" <<'PY'
import plistlib
import sys

with open(sys.argv[1], "rb") as file:
    run = plistlib.load(file)
for config in run.get("TestConfigurations", []):
    for target in config.get("TestTargets", []):
        app = target.pop("UITargetAppPath", None)
        paths = target.get("DependentProductPaths", [])
        target["DependentProductPaths"] = [path for path in paths if path != app]
with open(sys.argv[2], "wb") as file:
    plistlib.dump(run, file)
PY

launch_logged() {
  xcrun simctl launch --terminate-running-process \
    --stdout="$MEDIA_DIR/$1.log" --stderr="$MEDIA_DIR/$1.err.log" "$SIM_UDID" "$bundle_id"
}

run_step() {
  TEST_RUNNER_ANOTI_RESTORE_STEPS=1 TEST_RUNNER_ANOTI_READY_FILE="${READY_FILE:-}" \
    xcodebuild test-without-building -xctestrun "$steps_xctestrun" -destination "id=$SIM_UDID" \
    -only-testing:"iosAppUITests/RestoreSteps/$1" \
    -resultBundlePath "$RESULTS_DIR/$1-$2.xcresult" > "$MEDIA_DIR/$1-$2.xcodebuild.log" 2>&1
}

sessions() {
  local container
  container=$(xcrun simctl get_app_container "$SIM_UDID" "$bundle_id" data)
  ls "$container/Library/Saved Application State/$bundle_id.savedState" 2> /dev/null | tr '\n' ' '
}

end_app() {
  if [ "$1" = kill ]; then
    kill -KILL "$(pgrep -f "/Anoti.app/Anoti$" | head -1)"
  else
    xcrun simctl terminate "$SIM_UDID" "$bundle_id"
  fi
  sleep 2
}

xcrun simctl install "$SIM_UDID" "$APP_PATH"
launch_logged first
sleep 10
run_step testAnswerTheNotificationQuestion first
echo "the notification question: $?"

for method in kill; do
  for wait in 10; do
    for try in 1 2 3 4 5 6; do
      name="$method-$wait-$try"
      launch_logged "$name-before"
      sleep 10
      READY_FILE="$RUNNER_TEMP/home-$name" run_step testLeaveTheAppOnFavorites "$name"
      step=$?
      sleep "$wait"
      before=$(sessions)
      end_app "$method"
      launch_logged "$name-after"
      sleep 12
      after=$(sessions)
      kept=$(grep -o "the scene kept [0-9]* characters" "$MEDIA_DIR/$name-after.log" | head -1)
      opens=$(grep -o "the root opens on [A-Za-z]*" "$MEDIA_DIR/$name-after.log" | head -1)
      echo "RESULT $name: step $step | $kept | $opens | sessions before: $before | after: $after"
      grep -h "EXP" "$MEDIA_DIR/$name-before.err.log" "$MEDIA_DIR/$name-after.err.log" | sed "s/^/  /" | cut -c1-160
    done
  done
done
