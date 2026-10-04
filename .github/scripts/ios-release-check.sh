#!/bin/bash
# Reports how the Release app was built, into MEDIA_DIR/release-check.txt. With
# RELEASE_CHECK_ENFORCE=1 it fails unless the executable is stripped, the dSYM has its UUID and
# the dSYM decodes Kotlin. Tested by test/ios-release-check-test.sh.
#
# Small tool outputs go next to the report, so a red check can be read from the artifact. Symbol
# tables stay in RUNNER_TEMP. A tool that fails is reported, never read as a count of zero.
set -u
: "${APP_PATH:?}" "${MEDIA_DIR:?}" "${RUNNER_TEMP:?}"

report="$MEDIA_DIR/release-check.txt"
keep="$MEDIA_DIR/release-check"
work="$RUNNER_TEMP/release-check"
enforce="${RELEASE_CHECK_ENFORCE:-0}"
project="${GITHUB_WORKSPACE:-.}/iosApp/iosApp.xcodeproj"
executable="$APP_PATH/Anoti"
dwarf="$(dirname "$APP_PATH")/Anoti.app.dSYM/Contents/Resources/DWARF/Anoti"
mkdir -p "$keep" "$work"
failures=0

fail() {
  echo "FAIL: $1" >> "$report"
  failures=$((failures + 1))
}

: > "$report"
xcodebuild -project "$project" -target iosApp -configuration Release -sdk iphonesimulator \
  -showBuildSettings > "$work/settings.txt" 2> "$keep/settings.err"
names='DEAD_CODE_STRIPPING|DEPLOYMENT_POSTPROCESSING|STRIP[A-Z_]*'
grep -E "^ *($names) = " "$work/settings.txt" | sed 's/^ *//' > "$keep/settings.txt"
if [ -s "$keep/settings.txt" ]; then
  cat "$keep/settings.txt" >> "$report"
else
  echo "settings: none, $(head -1 "$keep/settings.err")" >> "$report"
  [ "$enforce" = 1 ] && fail "xcodebuild printed no build settings"
fi
if [ -f "$executable" ]; then
  echo "size Anoti: $(wc -c < "$executable" | tr -d ' ')" >> "$report"
else
  echo "size Anoti: none" >> "$report"
fi

app_kfun=""
if nm "$executable" > "$work/app-symbols.txt" 2> "$keep/app-symbols.err"; then
  app_kfun=$(grep -c 'kfun:' "$work/app-symbols.txt")
  echo "Kotlin symbols in the executable: $app_kfun" >> "$report"
else
  echo "nm failed on the executable: $(head -1 "$keep/app-symbols.err")" >> "$report"
fi

# uuid_of <file> <name>: prints the file's UUID, or reports why there is none.
uuid_of() {
  if xcrun dwarfdump --uuid "$1" > "$keep/$2-uuid.txt" 2>&1; then
    awk '/^UUID:/ { print $2; exit }' "$keep/$2-uuid.txt"
  else
    echo "dwarfdump --uuid failed on the $2: $(head -1 "$keep/$2-uuid.txt")" >> "$report"
  fi
}
app_uuid=$(uuid_of "$executable" executable)
dsym_uuid=$(uuid_of "$dwarf" dSYM)
echo "UUID of the executable: ${app_uuid:-none}" >> "$report"
echo "UUID of the dSYM: ${dsym_uuid:-none}" >> "$report"

if xcrun dwarfdump --show-sources "$dwarf" > "$keep/sources.txt" 2> "$keep/sources.err"; then
  echo "Kotlin files in the dSYM: $(grep -c '\.kt$' "$keep/sources.txt")" >> "$report"
else
  echo "dwarfdump --show-sources failed: $(head -1 "$keep/sources.err")" >> "$report"
fi

# It samples many functions, since a small one may be inlined away. Each sample is the address
# halfway into a function: its first instruction often maps to line 0.
: > "$keep/addresses.txt"
if nm -U -n "$dwarf" > "$work/dsym-symbols.txt" 2> "$keep/dsym-symbols.err"; then
  awk 'n >= 50 { exit }
    start != "" && $1 != start { print start, $1; start = ""; n++ }
    start == "" && $3 ~ /^_?kfun:com\.alekseivinogradov\./ { start = $1 }' \
    "$work/dsym-symbols.txt" > "$work/functions.txt"
  while read -r start end; do
    printf '0x%x\n' $(((16#$start + 16#$end) / 2))
  done < "$work/functions.txt" > "$keep/addresses.txt"
else
  echo "nm failed on the dSYM: $(head -1 "$keep/dsym-symbols.err")" >> "$report"
fi
decoded=0
if [ -s "$keep/addresses.txt" ]; then
  if xcrun atos -o "$dwarf" -arch arm64 -f "$keep/addresses.txt" > "$keep/atos.txt" \
    2> "$keep/atos.err"; then
    grep -E '\.kt:[1-9][0-9]*' "$keep/atos.txt" > "$keep/decoded.txt"
    decoded=$(wc -l < "$keep/decoded.txt" | tr -d ' ')
    sed -n '1,3p' "$keep/decoded.txt" >> "$report"
  else
    echo "atos failed: $(head -1 "$keep/atos.err")" >> "$report"
  fi
fi
sampled=$(wc -l < "$keep/addresses.txt" | tr -d ' ')
echo "Kotlin addresses decoded: $decoded of $sampled" >> "$report"

if [ "$enforce" = 1 ]; then
  if [ -z "$app_kfun" ]; then
    fail "nm could not read the executable"
  elif [ "$app_kfun" -ne 0 ]; then
    fail "the executable keeps $app_kfun Kotlin symbols"
  fi
  if [ -z "$app_uuid" ] || [ "$app_uuid" != "$dsym_uuid" ]; then
    fail "the dSYM's UUID does not match the executable's"
  fi
  [ "$decoded" -gt 0 ] || fail "the dSYM decoded no Kotlin address"
fi

cat "$report"
[ "$failures" -eq 0 ]
