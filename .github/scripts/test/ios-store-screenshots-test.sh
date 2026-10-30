#!/bin/bash
# Tests ios-store-screenshots.sh on any machine with bash and Python 3, macOS or not. The script
# calls sips, a macOS tool; this test puts a stub of it first on PATH. A fake screenshot is a text
# file holding "<width> <height> <alpha>", which the stub reads and, when turning, swaps. A real
# PNG, for the rotation tag, gives the stub its header's size. Each case lays out the attachments
# of one or more tries the way xcresulttool exports them. Then it checks the script's exit code,
# the files it wrote and the lines of its table.
#
# Run it from the repository root after any change to ios-store-screenshots.sh:
#   bash .github/scripts/test/ios-store-screenshots-test.sh
# It prints one line per case and ends with "passed N, failed 0". Any other end is a failure.
#
# CI does not run it. Its stubs and cases live in a temporary folder, removed when it ends.
set -u
here=$(cd "$(dirname "$0")" && pwd)
script=${1:-$here/../ios-store-screenshots.sh}
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
stubs="$work/stubs"
mkdir -p "$stubs"

# sips -g pixelWidth -g pixelHeight -g hasAlpha <file> prints the three values as sips does;
# sips -r 270 <src> --out <dst> writes the source turned a quarter. A PNG keeps its other chunks
# when turned, as sips keeps the metadata.
cat > "$stubs/sips" <<'EOF'
#!/bin/bash
png() { [ "$(head -c 4 "$1" | tail -c 3)" = PNG ]; }
if [ "$1" = -r ]; then
  if png "$3"; then
    python3 - "$3" "$5" <<'PY'
import struct, sys, zlib
data = open(sys.argv[1], "rb").read()
width, height = struct.unpack(">II", data[16:24])
header = b"IHDR" + struct.pack(">II", height, width) + data[24:29]
turned = data[:12] + header + struct.pack(">I", zlib.crc32(header)) + data[33:]
open(sys.argv[2], "wb").write(turned)
PY
  else
    read -r width height alpha < "$3"
    echo "$height $width $alpha" > "$5"
  fi
  echo "$5"
  exit 0
fi
file=${*: -1}
[ -f "$file" ] || { echo "sips: no file $file" >&2; exit 1; }
if png "$file"; then
  read -r width height alpha <<< "$(python3 -c 'import struct, sys
print(*struct.unpack(">II", open(sys.argv[1], "rb").read()[16:24]), "no")' "$file")"
else
  read -r width height alpha < "$file"
fi
printf '%s\n  pixelWidth: %s\n  pixelHeight: %s\n  hasAlpha: %s\n' \
  "$file" "$width" "$height" "$alpha"
EOF
# Wherever python3 is missing or a stand-in, as on Windows, the script gets the real one.
if ! python3 -c 'import sys' > /dev/null 2>&1; then
  printf '#!/bin/bash\nexec python "$@"\n' > "$stubs/python3"
fi
chmod +x "$stubs"/*
export PATH="$stubs:$PATH"

passed=0
failed=0

# attachment <case> <try> <attachment name> <content>: one exported file and its manifest entry.
attachment() {
  local dir="$work/cases/$1/media/attachments/store-try-$2" file
  mkdir -p "$dir"
  file="$(printf '%08X' $RANDOM$RANDOM)-AAAA-BBBB-CCCC-DDDDEEEEFFFF.png"
  echo "$4" > "$dir/$file"
  echo "$3|$file" >> "$dir/entries"
}

# png_attachment <case> <try> <attachment name> <width> <height>: a real PNG that carries a
# rotation tag in EXIF and in XMP, as a landscape capture does.
png_attachment() {
  local dir="$work/cases/$1/media/attachments/store-try-$2" file
  mkdir -p "$dir"
  file="$(printf '%08X' $RANDOM$RANDOM)-AAAA-BBBB-CCCC-DDDDEEEEFFFF.png"
  python3 - "$dir/$file" "$4" "$5" <<'PY'
import struct, sys, zlib
def chunk(kind, body):
    return struct.pack(">I", len(body)) + kind + body + struct.pack(">I", zlib.crc32(kind + body))
path, width, height = sys.argv[1], int(sys.argv[2]), int(sys.argv[3])
exif = bytes.fromhex("4d4d002a00000008000101120003000000010008000000000000")
xmp = b"XML:com.adobe.xmp\x00\x00\x00\x00\x00<tiff:Orientation>8</tiff:Orientation>"
open(path, "wb").write(
    b"\x89PNG\r\n\x1a\n"
    + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0))
    + chunk(b"eXIf", exif) + chunk(b"iTXt", xmp)
    + chunk(b"IDAT", zlib.compress(b"")) + chunk(b"IEND", b""))
PY
  echo "$3|$file" >> "$dir/entries"
}

# manifests <case>: writes each try's manifest.json from its entries, one test per entry.
manifests() {
  local dir
  for dir in "$work/cases/$1/media/attachments"/store-try-*; do
    [ -f "$dir/entries" ] || continue
    python3 - "$dir/entries" > "$dir/manifest.json" <<'PY'
import json, sys
tests = []
for line in open(sys.argv[1]):
    name, file = line.rstrip("\n").split("|")
    tests.append({"testIdentifier": "StoreScreenshotsTests/test()", "attachments": [
        {"suggestedHumanReadableName": name, "exportedFileName": file}]})
print(json.dumps(tests))
PY
    rm "$dir/entries"
  done
}

# check <case> <expected exit>: runs the script on the case's media folder.
check() {
  local name=$1 want=$2 got
  manifests "$name"
  MEDIA_DIR="$work/cases/$name/media" bash "$script" > "$work/cases/$name/out.txt" 2>&1
  got=$?
  if [ "$got" = "$want" ]; then
    passed=$((passed + 1)); echo "ok   $name"
  else
    failed=$((failed + 1)); echo "FAIL $name (exit $got, want $want)"
    cat "$work/cases/$name/out.txt"
  fi
}

# has <case> <text>: the script's output holds the text.
has() {
  if grep -qF -- "$2" "$work/cases/$1/out.txt"; then passed=$((passed + 1))
  else failed=$((failed + 1)); echo "FAIL $1 lacks: $2"; fi
}

# holds <case> <path under media> <content>: the script wrote that file with that content.
holds() {
  local file="$work/cases/$1/media/$2"
  if [ "$(cat "$file" 2> /dev/null)" = "$3" ]; then passed=$((passed + 1))
  else failed=$((failed + 1)); echo "FAIL $1: $2 holds '$(cat "$file" 2> /dev/null)', not '$3'"; fi
}

uuid=0_11111111-2222-3333-4444-555555555555

# six_iphone_shots <case> <try>: the main shots of a passing iPhone try.
six_iphone_shots() {
  local n
  for n in 1 2 3 4 5 6; do
    attachment "$1" "$2" "iphone-0${n}_$uuid.png" "1206 2622 no"
  done
}

# An iPhone try with its candidates, an extra and attachments that are not shots.
six_iphone_shots good 1
attachment good 1 "iphone-01~2_$uuid.png" "1206 2622 no"
attachment good 1 "iphone-05~collapsed_$uuid.png" "1206 2622 no"
attachment good 1 "main list_$uuid.png" "1206 2622 no"
mkdir -p "$work/cases/good/media" && echo 1 > "$work/cases/good/media/store-passed-try"
check good 0
holds good store/try-1/iphone-01.png "1206 2622 no"
holds good store/try-1/iphone-01~2.png "1206 2622 no"
holds good store/try-1/iphone-05~collapsed.png "1206 2622 no"
has good "iphone-06"
if ls "$work/cases/good/media/store/try-1" | grep -q "main list"; then
  failed=$((failed + 1)); echo "FAIL good: an attachment that is no shot was collected"
else passed=$((passed + 1)); fi

# An iPad try: the landscape shots come out turned, without their marker.
for n in 1 2; do
  attachment ipad 1 "ipad-0${n}@landscape_$uuid.png" "2064 2752 no"
done
for n in 3 4 5 6; do
  attachment ipad 1 "ipad-0${n}_$uuid.png" "2064 2752 no"
done
attachment ipad 1 "ipad-05~pressed_$uuid.png" "2064 2752 no"
mkdir -p "$work/cases/ipad/media" && echo 1 > "$work/cases/ipad/media/store-passed-try"
check ipad 0
holds ipad store/try-1/ipad-01.png "2752 2064 no"
holds ipad store/try-1/ipad-03.png "2064 2752 no"

# A failed try and a passing one: both collected, only the passing one must be whole.
attachment two-tries 1 "iphone-01_$uuid.png" "1206 2622 no"
six_iphone_shots two-tries 2
mkdir -p "$work/cases/two-tries/media" && echo 2 > "$work/cases/two-tries/media/store-passed-try"
check two-tries 0
holds two-tries store/try-1/iphone-01.png "1206 2622 no"
holds two-tries store/try-2/iphone-06.png "1206 2622 no"

# A size App Store Connect takes, but not the CI iPhone's.
six_iphone_shots wrong-size 1
attachment wrong-size 1 "iphone-03~2_$uuid.png" "1179 2556 no"
check wrong-size 1
has wrong-size "WRONG"

six_iphone_shots alpha 1
attachment alpha 1 "iphone-02~3_$uuid.png" "1206 2622 yes"
check alpha 1
has alpha "WRONG"

# A turned shot loses the capture's rotation tag, which would have a viewer turn it once more.
png_attachment rotation 1 "ipad-01@landscape_$uuid.png" 2064 2752
check rotation 0
has rotation "2752x2064"
if python3 - "$work/cases/rotation/media/store/try-1/ipad-01.png" <<'PY'
import struct, sys
data = open(sys.argv[1], "rb").read()
upright = struct.unpack(">II", data[16:24]) == (2752, 2064)
sys.exit(0 if upright and b"eXIf" not in data and b"adobe.xmp" not in data else 1)
PY
then passed=$((passed + 1)); else failed=$((failed + 1)); echo "FAIL rotation: the tag stayed"; fi

# A manifest names a file the export did not write.
six_iphone_shots not-exported 1
echo "iphone-02~2_$uuid.png|0000DEAD-AAAA-BBBB-CCCC-DDDDEEEEFFFF.png" \
  >> "$work/cases/not-exported/media/attachments/store-try-1/entries"
check not-exported 1
has not-exported "was not exported"

# A landscape shot whose buffer is already landscape would come out on its side.
attachment sideways 1 "ipad-01@landscape_$uuid.png" "2752 2064 no"
check sideways 1
has sideways "WRONG"

# The passing try lacks a main shot.
for n in 1 2 3 4 5; do
  attachment missing 1 "iphone-0${n}_$uuid.png" "1206 2622 no"
done
mkdir -p "$work/cases/missing/media" && echo 1 > "$work/cases/missing/media/store-passed-try"
check missing 1
has missing "iphone-06"

# A shot name the script cannot read; the readable shots are still collected.
six_iphone_shots unnamed 1
attachment unnamed 1 "iphone-7_$uuid.png" "1206 2622 no"
check unnamed 1
has unnamed "iphone-7"
holds unnamed store/try-1/iphone-04.png "1206 2622 no"

# A passing try that kept nothing, as a skipped test would leave it.
mkdir -p "$work/cases/nothing/media/attachments/store-try-1"
echo "unrelated|ABCDEF01-AAAA-BBBB-CCCC-DDDDEEEEFFFF.png" \
  > "$work/cases/nothing/media/attachments/store-try-1/entries"
echo 1 > "$work/cases/nothing/media/store-passed-try"
check nothing 1
has nothing "kept no shots"

# A try whose export wrote no manifest is skipped; the other try is collected.
six_iphone_shots no-manifest 2
mkdir -p "$work/cases/no-manifest/media/attachments/store-try-1"
echo 2 > "$work/cases/no-manifest/media/store-passed-try"
check no-manifest 0
holds no-manifest store/try-2/iphone-01.png "1206 2622 no"

echo "passed $passed, failed $failed"
[ "$failed" -eq 0 ]
