#!/bin/bash
# Collects the App Store screenshots the store-screenshots job of ios.yml kept. Each try's result
# bundle is exported beforehand to $MEDIA_DIR/attachments/store-try-<n>/, its manifest.json beside
# the files. Every shot goes to $MEDIA_DIR/store/try-<n>/<name>.png, named after its attachment;
# one the test marked @landscape is turned upright and loses the marker. Then each shot's size and
# alpha are checked against what App Store Connect takes, and the passing try, named in
# $MEDIA_DIR/store-passed-try, must hold all six main shots. Prints a table; exits 1 on any wrong
# shot, any shot it cannot name, or a missing main shot.
#
# Tested by .github/scripts/test/ios-store-screenshots-test.sh.
set -euo pipefail

media=${MEDIA_DIR:?MEDIA_DIR names the folder of the tries\' attachments}
status=0
device=""

# size <file>: prints "<width> <height> <alpha>" as sips reads them.
size() {
  sips -g pixelWidth -g pixelHeight -g hasAlpha "$1" \
    | awk '/pixelWidth/ {w = $2} /pixelHeight/ {h = $2} /hasAlpha/ {a = $2} END {print w, h, a}'
}

# accepted <name> <width> <height>: whether App Store Connect's slot for that device takes it.
accepted() {
  case "$1:$2x$3" in
    iphone-*:1206x2622 | ipad-*:2064x2752 | ipad-*:2752x2064) return 0 ;;
    *) return 1 ;;
  esac
}

printf '%-24s %-11s %-5s %s\n' shot size alpha verdict
for export in "$media"/attachments/store-try-*/; do
  [ -f "$export/manifest.json" ] || continue
  try=$(basename "$export")
  try=${try#store-try-}
  out="$media/store/try-$try"
  mkdir -p "$out"
  # Prints "<exported file> <name> <landscape>" for each screenshot attachment, and exits 1
  # after naming one whose name it cannot read.
  if ! list=$(python3 - "$export/manifest.json" <<'PY'
import json, re, sys
shot = re.compile(r"^((?:iphone|ipad)-\d\d(?:~[a-z0-9]+)?)(@landscape)?_\d+_[0-9A-Fa-f-]+\.png$")
bad = False
for test in json.load(open(sys.argv[1])):
    for attachment in test.get("attachments", []):
        name = attachment.get("suggestedHumanReadableName", "")
        if not re.match(r"^(?:iphone|ipad)-", name):
            continue
        match = shot.match(name)
        if not match:
            print(f"cannot read the shot name {name}", file=sys.stderr)
            bad = True
            continue
        print(attachment["exportedFileName"], match.group(1), "yes" if match.group(2) else "no")
sys.exit(1 if bad else 0)
PY
  ); then
    status=1
  fi
  # A Python on Windows, where the test can run, ends its lines with a carriage return.
  list=${list//$'\r'/}
  while read -r file name landscape; do
    [ -n "$file" ] || continue
    device=${name%%-*}
    source="$export$file"
    target="$out/$name.png"
    verdict=ok
    if [ "$landscape" = yes ]; then
      # The test keeps a landscape screen as the portrait buffer, the UI turned a quarter.
      read -r width height _ <<< "$(size "$source")"
      [ "$width" -lt "$height" ] || verdict=WRONG
      sips -r 270 "$source" --out "$target" > /dev/null
    else
      cp "$source" "$target"
    fi
    read -r width height alpha <<< "$(size "$target")"
    accepted "$name" "$width" "$height" && [ "$alpha" = no ] || verdict=WRONG
    [ "$verdict" = ok ] || status=1
    printf '%-24s %-11s %-5s %s\n' "try-$try/$name" "${width}x$height" "$alpha" "$verdict"
  done <<< "$list"
done

if [ -f "$media/store-passed-try" ]; then
  passing=$(cat "$media/store-passed-try")
  for n in 1 2 3 4 5 6; do
    main="${device:-unknown}-0$n"
    if [ ! -f "$media/store/try-$passing/$main.png" ]; then
      echo "The passing try $passing lacks $main"
      status=1
    fi
  done
fi
exit $status
