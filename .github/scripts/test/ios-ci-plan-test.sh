#!/usr/bin/env bash
# Runs ios-ci-plan.sh over the cases ios.yml relies on and checks each output. Run it by hand
# after a change to the script: bash .github/scripts/test/ios-ci-plan-test.sh
set -euo pipefail

script="$(dirname "$0")/../ios-ci-plan.sh"
failures=0

# plan <var=value>... prints the script's outputs for that environment.
plan() {
  env -u GITHUB_OUTPUT "$@" bash "$script"
}

expect() {
  local name=$1 output=$2 line=$3
  if grep -qx -- "$line" <<< "$output"; then
    echo "ok   $name: $line"
  else
    echo "FAIL $name: expected $line in:"
    sed 's/^/       /' <<< "$output"
    failures=$((failures + 1))
  fi
}

refused() {
  local name=$1
  shift
  if plan "$@" > /dev/null 2>&1; then
    echo "FAIL $name was accepted"
    failures=$((failures + 1))
  else
    echo "ok   $name is refused"
  fi
}

# A push gets the inputs' defaults, whatever the inputs say.
out=$(plan EVENT=push UI_FLOW=true DEVICE=all ORIENTATION=all)
expect push "$out" "unit_tests=true"
expect push "$out" "swiftlint=true"
expect push "$out" "phone_flow=false"
expect push "$out" "phone_ios_tests=false"
expect push "$out" "tablet_flow=false"
expect push "$out" "tablet_ios_tests=false"

# A run by hand with every default of ios.yml's inputs does what a push does.
yml="$(dirname "$0")/../../workflows/ios.yml"
# default_of <input> prints that input's default, the first default: after its name.
default_of() {
  awk -v name="$1:" '$1 == name { found = 1; next } found && $1 == "default:" { print $2; exit }' \
    "$yml"
}
push_out=$(plan EVENT=push)
dispatch_out=$(plan EVENT=workflow_dispatch UNIT_TESTS="$(default_of unit_tests)" \
  UI_FLOW="$(default_of ui_flow)" IOS_TESTS="$(default_of ios_tests)" \
  SWIFTLINT="$(default_of swiftlint)" DEVICE="$(default_of device)" \
  ORIENTATION="$(default_of orientation)")
if [ "$push_out" = "$dispatch_out" ]; then
  echo "ok   defaults: a push plans what a run with ios.yml's defaults plans"
else
  echo "FAIL defaults: a push plans"
  sed 's/^/       /' <<< "$push_out"
  echo "     but a run with ios.yml's defaults plans"
  sed 's/^/       /' <<< "$dispatch_out"
  failures=$((failures + 1))
fi

# Every device and orientation, with nothing but the flow.
out=$(plan EVENT=workflow_dispatch UNIT_TESTS=false UI_FLOW=true IOS_TESTS=false SWIFTLINT=false \
  DEVICE=all ORIENTATION=all)
expect flow-everywhere "$out" "unit_tests=false"
expect flow-everywhere "$out" "swiftlint=false"
expect flow-everywhere "$out" "phone_flow=true"
expect flow-everywhere "$out" "tablet_flow=true"
expect flow-everywhere "$out" "tablet_ios_tests=false"
expect flow-everywhere "$out" "orientations=portrait landscape"

# The iPhone never turns, so a landscape-only run leaves its flow out.
out=$(plan EVENT=workflow_dispatch UNIT_TESTS=true UI_FLOW=true IOS_TESTS=true SWIFTLINT=true \
  DEVICE=all ORIENTATION=landscape)
expect all-landscape "$out" "phone_flow=false"
expect all-landscape "$out" "phone_ios_tests=true"
expect all-landscape "$out" "tablet_flow=true"
expect all-landscape "$out" "orientations=landscape"

# The iPad alone, with the other app tests.
out=$(plan EVENT=workflow_dispatch UNIT_TESTS=true UI_FLOW=false IOS_TESTS=true SWIFTLINT=true \
  DEVICE=tablet ORIENTATION=portrait)
expect tablet "$out" "phone_ios_tests=false"
expect tablet "$out" "tablet_flow=false"
expect tablet "$out" "tablet_ios_tests=true"
expect tablet "$out" "orientations=portrait"

# An unknown value, or a flow left with no device to run on, fails rather than passing empty.
refused "an unknown device" EVENT=workflow_dispatch UNIT_TESTS=true UI_FLOW=true IOS_TESTS=false \
  SWIFTLINT=true DEVICE=fold ORIENTATION=portrait
refused "an unknown orientation" EVENT=workflow_dispatch UNIT_TESTS=true UI_FLOW=true \
  IOS_TESTS=false SWIFTLINT=true DEVICE=phone ORIENTATION=sideways
refused "the flow on an iPhone in landscape" EVENT=workflow_dispatch UNIT_TESTS=true UI_FLOW=true \
  IOS_TESTS=false SWIFTLINT=true DEVICE=phone ORIENTATION=landscape

# lint_only and release run no app test, so the same choice passes alongside either.
for override in LINT_ONLY=true RELEASE=true; do
  if plan EVENT=workflow_dispatch UNIT_TESTS=true UI_FLOW=true IOS_TESTS=false SWIFTLINT=true \
    DEVICE=phone ORIENTATION=landscape "$override" > /dev/null 2>&1; then
    echo "ok   the flow on an iPhone in landscape passes with $override"
  else
    echo "FAIL the flow on an iPhone in landscape is refused with $override"
    failures=$((failures + 1))
  fi
done

if [ "$failures" -ne 0 ]; then
  echo "$failures check(s) failed"
  exit 1
fi
echo "every check passed"
