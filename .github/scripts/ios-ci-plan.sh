#!/usr/bin/env bash
# Turns ios.yml's event and inputs into the flags its jobs read. A push has no inputs, so it
# gets the inputs' defaults from here. Tested by .github/scripts/test/ios-ci-plan-test.sh.
set -euo pipefail

if [ "${EVENT:-}" != workflow_dispatch ]; then
  # The defaults of ios.yml's inputs.
  UNIT_TESTS=true
  UI_FLOW=false
  IOS_TESTS=false
  SWIFTLINT=true
  DEVICE=phone
  ORIENTATION=portrait
fi

case $DEVICE in
  phone) phone=true tablet=false ;;
  tablet) phone=false tablet=true ;;
  all) phone=true tablet=true ;;
  *) echo "Unknown device: $DEVICE" >&2; exit 1 ;;
esac

case $ORIENTATION in
  portrait | landscape) orientations=$ORIENTATION ;;
  all) orientations="portrait landscape" ;;
  *) echo "Unknown orientation: $ORIENTATION" >&2; exit 1 ;;
esac

# Prints true when every argument is true.
all_true() {
  for flag in "$@"; do
    [ "$flag" = true ] || { echo false; return; }
  done
  echo true
}

# The iPhone never turns, so it walks the flow only when portrait is asked for.
upright=$([ "$ORIENTATION" = landscape ] && echo false || echo true)
phone_flow=$(all_true "$UI_FLOW" "$phone" "$upright")
tablet_flow=$(all_true "$UI_FLOW" "$tablet")

if [ "$UI_FLOW" = true ] && [ "$phone_flow" = false ] && [ "$tablet_flow" = false ]; then
  echo "AnimeFavoritesUserFlowTest was asked for, but the iPhone does not turn to $ORIENTATION" >&2
  exit 1
fi

{
  echo "unit_tests=$UNIT_TESTS"
  echo "swiftlint=$SWIFTLINT"
  echo "phone_flow=$phone_flow"
  echo "phone_ios_tests=$(all_true "$IOS_TESTS" "$phone")"
  echo "tablet_flow=$tablet_flow"
  echo "tablet_ios_tests=$(all_true "$IOS_TESTS" "$tablet")"
  echo "orientations=$orientations"
} | tee -a "${GITHUB_OUTPUT:-/dev/null}"
