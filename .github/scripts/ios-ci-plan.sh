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
  TABLET_MODEL=air
  TEXT_SIZE=default
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

# The iPads by the prefix of their simulator's name, each with the suffix of its media artifact.
air='{"device":"iPad Air 11-inch","slug":"air"}'
mini='{"device":"iPad mini","slug":"mini"}'
pro='{"device":"iPad Pro 13-inch","slug":"pro"}'
case $TABLET_MODEL in
  air) tablet_models="[$air]" ;;
  mini) tablet_models="[$mini]" ;;
  pro) tablet_models="[$pro]" ;;
  all) tablet_models="[$air,$mini,$pro]" ;;
  *) echo "Unknown tablet model: $TABLET_MODEL" >&2; exit 1 ;;
esac

# The Dynamic Type sizes by the names simctl takes. Large is the system's default.
case $TEXT_SIZE in
  default) content_size=large ;;
  xxxl) content_size=extra-extra-extra-large ;;
  ax5) content_size=accessibility-extra-extra-extra-large ;;
  *) echo "Unknown text size: $TEXT_SIZE" >&2; exit 1 ;;
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

# lint_only and release run no app test, so a flow they leave nowhere to run is no mistake.
skips_app_tests=$([ "${LINT_ONLY:-}" = true ] || [ "${RELEASE:-}" = true ] && echo true || echo false)

if [ "$UI_FLOW" = true ] && [ "$phone_flow" = false ] && [ "$tablet_flow" = false ] \
  && [ "$skips_app_tests" = false ]; then
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
  echo "tablet_models=$tablet_models"
  echo "content_size=$content_size"
} | tee -a "${GITHUB_OUTPUT:-/dev/null}"
