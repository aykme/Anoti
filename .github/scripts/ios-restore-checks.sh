#!/bin/bash
# The restore and theme checks on the booted simulator SIM_UDID. Each try of a case leaves a
# video, screenshots and the app's log in a folder of its own, MEDIA_DIR/<case>/try-<n>.
#
# The script starts the app itself, so the app's println reaches a log file. The UI steps only
# attach to the running app with activate(); a UI step that launched the app would lose its log.
set -uo pipefail
: "${SIM_UDID:?}" "${MEDIA_DIR:?}" "${APP_PATH:?}" "${XCTESTRUN:?}" "${RESULTS_DIR:?}"

bundle_id=com.alekseivinogradov.anoti
media_root=$MEDIA_DIR
results_root=$RESULTS_DIR
check_dark=".github/scripts/ios-check-dark.py"
failures=0
case_failures=0
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
    case_failures=$((case_failures + 1))
  fi
}

# Waits up to $3 seconds for the pattern $2 in the log $1.
wait_for_line() {
  local waited=0
  until grep -Eq "$2" "$1" 2> /dev/null; do
    [ "$waited" -ge "$3" ] && return 1
    sleep 1
    waited=$((waited + 1))
  done
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

# Every file of the scenes iOS keeps for the app. A scene restored on launch keeps its folder; a
# new one adds a folder of its own.
list_saved_scenes() {
  local container
  container=$(xcrun simctl get_app_container "$SIM_UDID" "$bundle_id" data)
  echo "the saved scenes $1:"
  find "$container/Library/Saved Application State" -type f -exec ls -lT {} + 2> /dev/null
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

# Ends the app the way iOS ends a suspended one: it goes to the background first, then a SIGKILL
# reaches its process, which the simulator runs on the host. An app ended while on screen counts
# as a crash or a force quit, and iOS then drops the scene state it kept.
end_like_the_system() {
  local pid
  xcrun simctl launch "$SIM_UDID" com.apple.Preferences > /dev/null
  sleep 5
  pid=$(pgrep -f "/Anoti.app/Anoti$" | head -1)
  if [ -n "$pid" ]; then
    echo "ending the app's process $pid in the background"
    kill -KILL "$pid"
  else
    echo "no app process to kill, terminating instead"
    xcrun simctl terminate "$SIM_UDID" "$bundle_id" 2> /dev/null
  fi
  sleep 2
}

# Gives a case a start of its own, so the cases run in any order and alone. A fresh install leaves
# nothing kept and favorites empty. Its first run answers the notification question, then ends in
# the background: an app ended on screen would lose what its next run keeps.
prepare_case() {
  xcrun simctl uninstall "$SIM_UDID" "$bundle_id"
  xcrun simctl install "$SIM_UDID" "$APP_PATH"
  launch_logged "$1-prepare"
  sleep 10
  run_step testAnswerTheNotificationQuestion "$1-prepare"
  expect $? "the case starts from a fresh install, $1"
  end_like_the_system
}

# Runs the case $3, with the rest of the arguments, up to three times until a try passes. Each
# try keeps its media in the folder $2 under MEDIA_DIR. The
# backend and the simulator can fail on their own, and every case starts from its own fresh
# install, so a try never depends on the one before. A failed try stays in the log.
run_case() {
  local name=$1 folder=$2 try
  shift 2
  for try in 1 2 3; do
    echo "== $name, try $try"
    MEDIA_DIR="$media_root/$folder/try-$try"
    RESULTS_DIR="$results_root/$folder/try-$try"
    mkdir -p "$MEDIA_DIR" "$RESULTS_DIR"
    case_failures=0
    "$@"
    [ "$case_failures" -eq 0 ] && return
    echo "$name failed on try $try"
  done
  failures=$((failures + case_failures))
}

case_kept_across_a_termination() {
  start_video case1-kept
  prepare_case case1
  launch_logged case1-before
  sleep 10
  home="$RUNNER_TEMP/home-1"
  rm -f "$home"
  # The step creates the file right before it goes home, so a later state file is that save's.
  READY_FILE="$home" run_step testLeaveTheAppOnFavoritesAfterASearch case1
  expect $? "the app was left on favorites after a search"
  wait_for_line "$MEDIA_DIR/case1-before.log" "saved [0-9]+ characters on AnimeFavorites" 30
  expect $? "the app saved its state on favorites when it went home"
  wait_for_saved_scene 60 "$home"
  expect $? "the scene's state reached the disk before the termination"
  end_like_the_system
  launch_logged case1-relaunch
  sleep 20
  screenshot case1-relaunch
  list_saved_scenes "after the relaunch"
  stop_video
  grep -q "the root opens on AnimeFavorites" "$MEDIA_DIR/case1-relaunch.log"
  expect $? "the relaunch opens on favorites"
  grep -Eq "the scene kept [1-9][0-9]* characters" "$MEDIA_DIR/case1-relaunch.log"
  expect $? "the scene's storage reached the view controller"
}

case_tap_from_the_background() {
  start_video case2-warm-tap
  prepare_case case2a
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
}

case_tap_with_the_app_closed() {
  start_video case2-cold-tap
  prepare_case case2b
  launch_logged case2-cold
  sleep 10
  home="$RUNNER_TEMP/home-2b"
  rm -f "$home"
  READY_FILE="$home" run_step testLeaveTheAppOnTheList case2b
  expect $? "the app was left on the list"
  wait_for_line "$MEDIA_DIR/case2-cold.log" "saved [0-9]+ characters on AnimeList" 30
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
}

case_fresh_after_a_reinstall() {
  start_video case3-reinstall
  prepare_case case3
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
}

# The theme is $1.
case_dark_in_a_theme() {
  local theme=$1
  xcrun simctl ui "$SIM_UDID" appearance "$theme"
  start_video "case4-$theme"
  prepare_case "case4-$theme"
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
  rm -rf "$attachments"
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
}

# After a UI test has launched the app, iOS hands no kept scene state back to the app's later
# launches, however they end. A restart of the simulator clears that; nothing is erased. A
# simulator that has just booted is still busy and slow to answer, so it gets time to settle.
restart_the_simulator() {
  xcrun simctl shutdown "$SIM_UDID"
  xcrun simctl boot "$SIM_UDID"
  xcrun simctl bootstatus "$SIM_UDID" -b > /dev/null
  sleep 30
}

main() {
  restart_the_simulator
  run_case "1. The state is kept across a termination" case1 case_kept_across_a_termination
  run_case "2a. A notification tap from the background, with the app on the list" case2a \
    case_tap_from_the_background
  run_case "2b. A notification tap with the app closed and the list kept" case2b \
    case_tap_with_the_app_closed
  run_case "3. A fresh start after a reinstall" case3 case_fresh_after_a_reinstall
  for theme in light dark; do
    run_case "4. Dark in the $theme system theme: launch, bars, app switcher" "case4-$theme" \
      case_dark_in_a_theme "$theme"
  done
  xcrun simctl ui "$SIM_UDID" appearance light

  echo "Restore checks failed: $failures"
  exit "$failures"
}

# Sourced, the script only defines its cases.
if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  main
fi
