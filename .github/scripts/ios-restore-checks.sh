#!/bin/bash
# The restore and theme checks on the booted simulator SIM_UDID. Each case leaves a video,
# screenshots and the app's log in MEDIA_DIR.
#
# The script starts the app itself, so the app's println reaches a log file. The UI steps only
# attach to the running app with activate(); a UI step that launched the app would lose its log.
set -uo pipefail
: "${SIM_UDID:?}" "${MEDIA_DIR:?}" "${APP_PATH:?}" "${XCTESTRUN:?}" "${RESULTS_DIR:?}"

bundle_id=com.alekseivinogradov.anoti
check_dark=".github/scripts/ios-check-dark.py"
failures=0
recorder=""

# A copy of the test run that leaves the app out. Installing the app before each step would end
# the instance the script started, and its log with it. The copy sits next to the original,
# since the paths inside are relative to it.
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

start_video() {
  xcrun simctl io "$SIM_UDID" recordVideo --codec=h264 --force "$MEDIA_DIR/$1.mp4" &
  recorder=$!
  sleep 2
}

stop_video() {
  kill -INT "$recorder" 2> /dev/null
  wait "$recorder" 2> /dev/null
}

screenshot() {
  xcrun simctl io "$SIM_UDID" screenshot "$MEDIA_DIR/$1.png"
}

launch_logged() {
  xcrun simctl launch --terminate-running-process \
    --stdout="$MEDIA_DIR/$1.log" --stderr="$MEDIA_DIR/$1.err.log" "$SIM_UDID" "$bundle_id"
}

# Extra environment for the step goes before the call: READY_FILE=... run_step name.
run_step() {
  TEST_RUNNER_ANOTI_RESTORE_STEPS=1 TEST_RUNNER_ANOTI_READY_FILE="${READY_FILE:-}" \
    xcodebuild test-without-building -xctestrun "$steps_xctestrun" -destination "id=$SIM_UDID" \
    -only-testing:"iosAppUITests/RestoreSteps/$1" \
    -resultBundlePath "$RESULTS_DIR/$1-$2.xcresult" > "$MEDIA_DIR/$1-$2.xcodebuild.log" 2>&1 \
    || return 1
  # A step that skipped itself exits 0 as well.
  grep -qF "Test Case '-[iosAppUITests.RestoreSteps $1]' passed" \
    "$MEDIA_DIR/$1-$2.xcodebuild.log"
}

expect() {
  if [ "$1" -eq 0 ]; then
    echo "PASS: $2"
  else
    echo "FAIL: $2"
    failures=$((failures + 1))
  fi
}

# Waits up to $3 seconds for the pattern $2 in the log $1, from its line $4 on.
wait_for_line() {
  local waited=0
  until tail -n "+$4" "$1" 2> /dev/null | grep -Eq "$2"; do
    [ "$waited" -ge "$3" ] && return 1
    sleep 1
    waited=$((waited + 1))
  done
}

# The line a log's next entry will take, so a later wait ignores what came before.
next_line() {
  echo $(($(wc -l < "$1") + 1))
}

wait_for_file() {
  local waited=0
  until [ -f "$1" ]; do
    [ "$waited" -ge "$2" ] && return 1
    sleep 1
    waited=$((waited + 1))
  done
}

push_notification() {
  cat > "$RUNNER_TEMP/notification.apns" <<'JSON'
{
  "aps": { "alert": { "title": "Anoti CI notification", "body": "A new episode" } },
  "deep_link_target": "{\"type\":\"AnimeFavorites\"}"
}
JSON
  xcrun simctl push "$SIM_UDID" "$bundle_id" "$RUNNER_TEMP/notification.apns"
}

# iOS writes the scene's storage to the app's container a while after the app leaves the screen,
# and a termination before that loses it. The system ends a suspended app much later. Waits up to
# $1 seconds for a saved-state file newer than the marker $2, then lists it.
wait_for_saved_scene() {
  local container saved waited=0
  container=$(xcrun simctl get_app_container "$SIM_UDID" "$bundle_id" data)
  saved="$container/Library/Saved Application State"
  until [ -n "$(find "$saved" -type f -newer "$2" 2> /dev/null | head -1)" ]; do
    if [ "$waited" -ge "$1" ]; then
      echo "no saved scene state newer than the step after $1 s"
      return 1
    fi
    sleep 1
    waited=$((waited + 1))
  done
  echo "the scene's state was written after $waited s:"
  find "$saved" -type f -newer "$2" -exec ls -l {} +
}

# The scene sessions the app's container keeps state for, one folder each.
list_scene_sessions() {
  local container
  container=$(xcrun simctl get_app_container "$SIM_UDID" "$bundle_id" data)
  echo "scene sessions $1:"
  ls -l "$container/Library/Saved Application State/$bundle_id.savedState" 2> /dev/null
}

# The exported attachment of the step $1 that the test named $2.
attachment() {
  /usr/bin/python3 - "$1/manifest.json" "$2" <<'PY'
import json
import sys

for test in json.load(open(sys.argv[1])):
    for kept in test["attachments"]:
        if kept["suggestedHumanReadableName"].startswith(sys.argv[2] + "_"):
            print(kept["exportedFileName"])
PY
}

# Ends the app the way iOS ends a suspended one: a SIGKILL to its process, which the simulator runs
# on the host. A plain simctl terminate is a polite exit, after which iOS sometimes opens a new
# scene session instead of restoring the old one.
end_like_the_system() {
  local pid
  pid=$(pgrep -f "/Anoti.app/Anoti$" | head -1)
  if [ -n "$pid" ]; then
    kill -KILL "$pid"
  else
    echo "no app process to kill, terminating instead"
    xcrun simctl terminate "$SIM_UDID" "$bundle_id" 2> /dev/null
  fi
  sleep 2
}

echo "== 1. The state is kept across a termination"
start_video case1-kept
launch_logged case1-before
sleep 10
home="$RUNNER_TEMP/home-1"
rm -f "$home"
from=$(next_line "$MEDIA_DIR/case1-before.log")
# The step creates the file right before it goes home, so a later state file is that save's.
READY_FILE="$home" run_step testLeaveTheAppOnFavoritesAfterASearch case1
expect $? "the app was left on favorites after a search"
wait_for_line "$MEDIA_DIR/case1-before.log" "saved [0-9]+ characters on AnimeFavorites" 30 "$from"
expect $? "the app saved its state on favorites when it went home"
wait_for_saved_scene 60 "$home"
expect $? "the scene's state reached the disk before the termination"
# iOS records which scene session to bring back a while after the app's own archive.
sleep 30
list_scene_sessions "before the end"
end_like_the_system
launch_logged case1-relaunch
sleep 20
list_scene_sessions "after the relaunch"
screenshot case1-relaunch
stop_video
grep -q "the root opens on AnimeFavorites" "$MEDIA_DIR/case1-relaunch.log"
expect $? "the relaunch opens on favorites"
grep -Eq "the scene kept [1-9][0-9]* characters" "$MEDIA_DIR/case1-relaunch.log"
expect $? "the scene's storage reached the view controller"

echo "== 2a. A notification tap from the background, with the app on the list"
start_video case2-warm-tap
launch_logged case2-warm
sleep 10
ready="$RUNNER_TEMP/ready-2a"
rm -f "$ready"
READY_FILE="$ready" run_step testTapTheNotificationFromTheBackground case2a &
step=$!
# The step writes the file once the app sits on the list in the background. Without it, the
# push goes out after two minutes and the step finds it in the notification list instead.
wait_for_file "$ready" 120 || echo "no ready file from the step, pushing anyway"
push_notification
wait "$step"
expect $? "a tap from the background opens favorites"
screenshot case2-warm-tap
grep -q "a notification opens AnimeFavorites, the root exists: true" \
  "$MEDIA_DIR/case2-warm.log"
expect $? "the running root was asked to open favorites"
stop_video

echo "== 2b. A notification tap with the app closed and the list kept"
start_video case2-cold-tap
home="$RUNNER_TEMP/home-2b"
rm -f "$home"
from=$(next_line "$MEDIA_DIR/case2-warm.log")
READY_FILE="$home" run_step testLeaveTheAppOnTheList case2b
expect $? "the app was left on the list"
wait_for_line "$MEDIA_DIR/case2-warm.log" "saved [0-9]+ characters on AnimeList" 30 "$from"
expect $? "the app saved its state on the list"
wait_for_saved_scene 60 "$home"
expect $? "the list's state reached the disk before the termination"
end_like_the_system
ready="$RUNNER_TEMP/ready-2b"
rm -f "$ready"
READY_FILE="$ready" run_step testTapTheNotificationWithTheAppClosed case2b &
step=$!
# Pushed once the step runs, so its banner is still up when the step looks for it.
wait_for_file "$ready" 120 || echo "no ready file from the step, pushing anyway"
push_notification
wait "$step"
expect $? "a cold tap opens favorites"
screenshot case2-cold-tap
stop_video

echo "== 3. A fresh start after a reinstall"
start_video case3-reinstall
xcrun simctl uninstall "$SIM_UDID" "$bundle_id"
xcrun simctl install "$SIM_UDID" "$APP_PATH"
launch_logged case3-reinstall
sleep 20
screenshot case3-reinstall
grep -q "nothing was kept" "$MEDIA_DIR/case3-reinstall.log"
expect $? "a reinstall starts with nothing kept"
grep -q "the root opens on AnimeList" "$MEDIA_DIR/case3-reinstall.log"
expect $? "a reinstall opens on the list"
# The reinstall brought the notification question back over the list.
run_step testAnswerTheNotificationQuestion case3
expect $? "the notification question was answered"
stop_video

echo "== 4. Dark in every system theme: launch, top bar, bottom bar, app switcher"
for theme in light dark; do
  xcrun simctl ui "$SIM_UDID" appearance "$theme"
  start_video "case4-$theme"
  xcrun simctl terminate "$SIM_UDID" "$bundle_id" 2> /dev/null
  launch_logged "case4-$theme"
  # The launch command returns before the zoom from the icon is over.
  sleep 1
  screenshot "case4-$theme-launch-1"
  sleep 0.6
  screenshot "case4-$theme-launch-2"
  sleep 15
  screenshot "case4-$theme-list"
  run_step testShowTheAppSwitcher "case4-$theme"
  expect $? "the app switcher step ran, $theme theme"
  stop_video
  attachments="$MEDIA_DIR/attachments/testShowTheAppSwitcher-case4-$theme"
  xcrun xcresulttool export attachments \
    --path "$RESULTS_DIR/testShowTheAppSwitcher-case4-$theme.xcresult" --output-path "$attachments"
  # The list may already show its light posters a second after launch, so the edges are what a
  # white launch frame would light up.
  for shot in launch-1 launch-2; do
    python3 "$check_dark" edges "$MEDIA_DIR/case4-$theme-$shot.png"
    expect $? "the launch is dark, $shot, $theme theme"
  done
  python3 "$check_dark" edges "$MEDIA_DIR/case4-$theme-list.png"
  expect $? "the top bar and the bottom bar are dark, $theme theme"
  python3 "$check_dark" icons "$MEDIA_DIR/case4-$theme-list.png"
  expect $? "the status bar's icons are light, $theme theme"
  home_shot="$attachments/$(attachment "$attachments" "home screen")"
  python3 "$check_dark" card "$home_shot" --expect-light
  expect $? "the switcher check tells the home screen apart, $theme theme"
  python3 "$check_dark" card "$attachments/$(attachment "$attachments" "app switcher")"
  expect $? "the app's card in the switcher is dark, $theme theme"
done
xcrun simctl ui "$SIM_UDID" appearance light

echo "Restore checks failed: $failures"
exit "$failures"
