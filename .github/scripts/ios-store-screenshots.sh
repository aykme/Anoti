#!/bin/bash
# Turns the raw attachments of the store-screenshots job of ios.yml into App Store-ready files, and
# fails the job when a shot would not fit its App Store Connect slot. So the passing try's six main
# shots can go to the store as they are, and a wrong shot shows in CI, not at upload.
#
# Each try's result bundle is exported beforehand to $MEDIA_DIR/attachments/store-try-<n>/, beside
# its manifest.json.
# Every shot goes to $MEDIA_DIR/store/try-<n>/<name>.png, named after its attachment. One the test
# marked @landscape is turned upright and loses the marker and its rotation tag. Each shot's size
# and alpha are checked against what App Store Connect takes. The passing try, named in
# $MEDIA_DIR/store-passed-try, must hold all six main shots. Prints a table. Exits 1 on a wrong
# shot, a shot it cannot name or file, or a missing main shot.
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

# drop_rotation <file>: removes the EXIF and XMP chunks of a turned shot. They keep the capture's
# rotation tag, and a viewer that honors it would turn the upright pixels once more. A file that is
# no PNG is left as it is.
drop_rotation() {
  python3 - "$1" <<'PY'
import struct, sys
path = sys.argv[1]
data = open(path, "rb").read()
if not data.startswith(b"\x89PNG\r\n\x1a\n"):
    sys.exit(0)
kept, at = [data[:8]], 8
while at < len(data):
    length = struct.unpack(">I", data[at:at + 4])[0]
    kind = data[at + 4:at + 8]
    xmp = kind == b"iTXt" and data[at + 8:at + 25] == b"XML:com.adobe.xmp"
    if kind != b"eXIf" and not xmp:
        kept.append(data[at:at + 12 + length])
    at += 12 + length
open(path, "wb").write(b"".join(kept))
PY
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
shot = re.compile(
    r"^((?:iphone|ipad)-\d\d(?:~[a-z0-9]+)?)(@landscape)?_\d+_[0-9A-Fa-f-]+(?:\.png)?$")
bad = False
for test in json.load(open(sys.argv[1])):
    for attachment in test.get("attachments", []):
        name = attachment.get("suggestedHumanReadableName", "")
        if not re.match(r"^(?:iphone|ipad)-", name):
            continue
        match = shot.match(name)
        file = attachment.get("exportedFileName")
        if not match or not file:
            print(f"cannot read the shot {name}", file=sys.stderr)
            bad = True
            continue
        print(file, match.group(1), "yes" if match.group(2) else "no")
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
    if [ ! -f "$source" ]; then
      echo "try-$try/$name was not exported: no $file"
      status=1
      continue
    fi
    verdict=ok
    if [ "$landscape" = yes ]; then
      # The test keeps a landscape screen as the portrait buffer, the UI turned a quarter.
      read -r width height _ <<< "$(size "$source")"
      [ "$width" -lt "$height" ] || verdict=WRONG
      sips -r 270 "$source" --out "$target" > /dev/null
      drop_rotation "$target"
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
  if [ -z "$device" ]; then
    echo "The passing try $passing kept no shots"
    status=1
  fi
  for n in 1 2 3 4 5 6; do
    main="${device:-unknown}-0$n"
    if [ ! -f "$media/store/try-$passing/$main.png" ]; then
      echo "The passing try $passing lacks $main"
      status=1
    fi
  done
fi
exit $status
