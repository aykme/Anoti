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

# A run by hand with every default does what a push does.
out=$(plan EVENT=workflow_dispatch UNIT_TESTS=true UI_FLOW=false IOS_TESTS=false SWIFTLINT=true \
  DEVICE=phone ORIENTATION=portrait)
expect defaults "$out" "unit_tests=true"
expect defaults "$out" "swiftlint=true"
expect defaults "$out" "phone_flow=false"
expect defaults "$out" "phone_ios_tests=false"
expect defaults "$out" "tablet_flow=false"
expect defaults "$out" "tablet_ios_tests=false"

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

if [ "$failures" -ne 0 ]; then
  echo "$failures check(s) failed"
  exit 1
fi
echo "every check passed"
