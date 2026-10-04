#!/bin/bash
# Tests ios-release-check.sh on any machine with bash, macOS or not. The script calls macOS
# tools: xcodebuild, nm, dwarfdump and atos. This test puts stubs of them first on PATH. Each
# case makes the stubs answer as a stripped, an unstripped or a broken build would. It then
# checks the script's exit code and the lines of its report.
#
# Run it from the repository root after any change to ios-release-check.sh:
#   bash .github/scripts/test/ios-release-check-test.sh
# It prints one line per case and ends with "passed N, failed 0". Any other end is a failure.
# An argument names another script to test instead, which shows a planted bug is caught.
#
# CI does not run it. Its stubs and cases live in a temporary folder, removed when it ends.
set -u
# A variable left in the calling shell would change what the cases see.
unset RELEASE_CHECK_ENFORCE GITHUB_WORKSPACE APP DSYM $(compgen -v STUB_)
here=$(cd "$(dirname "$0")" && pwd)
script=${1:-$here/../ios-release-check.sh}
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
stubs="$work/stubs"
mkdir -p "$stubs"

# The stubs fail as the real tools do: on a missing file, and nm on any flag but -U or
# --defined-only.
cat > "$stubs/xcrun" <<'EOF'
#!/bin/bash
exec "$@"
EOF
cat > "$stubs/xcodebuild" <<'EOF'
#!/bin/bash
if [ "${STUB_XCODEBUILD_FAIL:-0}" = 1 ]; then echo "xcodebuild: error: no project" >&2; exit 65; fi
case " $* " in *" -configuration Release "*) ;; *) echo "error: not Release" >&2; exit 65 ;; esac
echo "    DEAD_CODE_STRIPPING = YES"
echo "    DEPLOYMENT_POSTPROCESSING = ${STUB_POSTPROCESSING:-NO}"
echo "    STRIP_INSTALLED_PRODUCT = YES"
echo "    STRIP_STYLE = all"
EOF
cat > "$stubs/nm" <<'EOF'
#!/bin/bash
defined=0
case "$1" in
  -U|--defined-only) defined=1; shift ;;
  -*) echo "error: unknown argument '$1'" >&2; exit 1 ;;
esac
[ -f "$1" ] || { echo "error: $1: No such file or directory" >&2; exit 1; }
if [ "$defined" = 1 ]; then
  for i in $(seq 1 "${STUB_DSYM_KFUN:-60}"); do
    printf '%016x t _kfun:com.alekseivinogradov.anoti.F%d#f(){}\n' $((4096 + i * 16)) "$i"
  done
  echo "0000000000002000 t _objc_something"
  exit 0
fi
if [ "${STUB_NM_FAIL_APP:-0}" = 1 ]; then
  case "$1" in *dSYM*) ;; *) echo "error: truncated" >&2; exit 1 ;; esac
fi
echo "                 U _objc_msgSend"
for i in $(seq 1 "${STUB_APP_KFUN:-0}"); do
  printf '%016x t _kfun:com.alekseivinogradov.anoti.F%d#f(){}\n' $((4096 + i * 16)) "$i"
done
EOF
cat > "$stubs/dwarfdump" <<'EOF'
#!/bin/bash
[ -f "$2" ] || { echo "error: $2: No such file or directory" >&2; exit 1; }
case "$1" in
  --uuid)
    case "$2" in
      *dSYM*) echo "UUID: ${STUB_DSYM_UUID} (arm64) $2" ;;
      *) echo "UUID: ${STUB_APP_UUID} (arm64) $2" ;;
    esac ;;
  --show-sources)
    [ "${STUB_SOURCES_FAIL:-0}" = 1 ] && { echo "error: unknown option" >&2; exit 1; }
    for i in $(seq 1 "${STUB_KT_FILES:-5}"); do echo "/src/F$i.kt"; done
    echo "/src/runtime.cpp" ;;
  *) echo "error: unknown option $1" >&2; exit 1 ;;
esac
EOF
cat > "$stubs/atos" <<'EOF'
#!/bin/bash
object="" list=""
while [ $# -gt 0 ]; do
  case "$1" in
    -o) object=$2; shift 2 ;;
    -arch) shift 2 ;;
    -f) list=$2; shift 2 ;;
    *) echo "atos: unexpected argument $1" >&2; exit 1 ;;
  esac
done
[ -f "$object" ] || { echo "atos: cannot load $object" >&2; exit 1; }
[ "${STUB_ATOS_FAIL:-0}" = 1 ] && { echo "atos: cannot parse" >&2; exit 1; }
count=0
for a in $(cat "$list"); do
  count=$((count + 1))
  if [ "${STUB_ATOS_OK:-1}" = 1 ]; then echo "kfun:F#f(){} (in Anoti) (F.kt:12)"
  else echo "$a (in Anoti)"; fi
done
echo "$count" > "$STUB_ATOS_COUNT_FILE"
EOF
chmod +x "$stubs"/*

passed=0
failed=0
U=11111111-2222-3333-4444-555555555555
V=99999999-2222-3333-4444-555555555555

# check <name> <expected exit> -- env...: runs the script on a fake build.
# DSYM=file|folder|none and APP=file|none set what the fake build holds.
check() {
  local name=$1 want=$2
  shift 3
  local root="$work/cases/$name"
  mkdir -p "$root/media" "$root/tmp" "$root/products/Anoti.app"
  [ "${APP:-file}" = file ] && head -c 2048 /dev/zero > "$root/products/Anoti.app/Anoti"
  local dwarf_dir="$root/products/Anoti.app.dSYM/Contents/Resources/DWARF"
  case "${DSYM:-file}" in
    file) mkdir -p "$dwarf_dir" && touch "$dwarf_dir/Anoti" ;;
    folder) mkdir -p "$dwarf_dir" ;;
  esac
  env PATH="$stubs:$PATH" APP_PATH="$root/products/Anoti.app" MEDIA_DIR="$root/media" \
    RUNNER_TEMP="$root/tmp" STUB_ATOS_COUNT_FILE="$root/atos-count" "$@" \
    bash "$script" > "$root/out.txt" 2>&1
  local got=$?
  if [ "$got" -eq "$want" ]; then
    passed=$((passed + 1)); echo "ok   $name"
  else
    failed=$((failed + 1)); echo "FAIL $name (exit $got, want $want)"; cat "$root/out.txt"
  fi
}

# has <name> <pattern>, lacks <name> <pattern>: the case's report holds, or lacks, a line.
has() {
  if grep -qE "$2" "$work/cases/$1/media/release-check.txt" 2> /dev/null; then
    passed=$((passed + 1))
  else failed=$((failed + 1)); echo "FAIL $1 lacks: $2"; fi
}
lacks() {
  local report="$work/cases/$1/media/release-check.txt"
  if [ ! -f "$report" ] || grep -qE "$2" "$report"; then
    failed=$((failed + 1)); echo "FAIL $1 holds: $2, or wrote no report"
  else passed=$((passed + 1)); fi
}

GOOD="STUB_APP_UUID=$U STUB_DSYM_UUID=$U"

check unstripped-report 0 -- STUB_APP_KFUN=3 $GOOD
has unstripped-report "DEPLOYMENT_POSTPROCESSING = NO"
has unstripped-report "size Anoti: 2048"
has unstripped-report "Kotlin symbols in the executable: 3"
has unstripped-report "Kotlin files in the dSYM: 5"

check unstripped-enforced 1 -- RELEASE_CHECK_ENFORCE=1 STUB_APP_KFUN=3 $GOOD
has unstripped-enforced "FAIL: the executable keeps 3 Kotlin symbols"

check stripped-enforced 0 -- RELEASE_CHECK_ENFORCE=1 STUB_POSTPROCESSING=YES $GOOD
has stripped-enforced "DEPLOYMENT_POSTPROCESSING = YES"
has stripped-enforced "Kotlin symbols in the executable: 0"
has stripped-enforced "Kotlin addresses decoded: 50 of 50"
lacks stripped-enforced "FAIL"

check uuid-mismatch 1 -- RELEASE_CHECK_ENFORCE=1 STUB_APP_UUID=$U STUB_DSYM_UUID=$V
has uuid-mismatch "FAIL: the dSYM's UUID does not match"

DSYM=none check no-dsym 1 -- RELEASE_CHECK_ENFORCE=1 $GOOD
has no-dsym "FAIL: the dSYM's UUID does not match"
has no-dsym "FAIL: the dSYM decoded no Kotlin address"
lacks no-dsym "Kotlin files in the dSYM: 0"
has no-dsym "dwarfdump --uuid failed on the dSYM: error:"

DSYM=folder check dsym-without-dwarf 1 -- RELEASE_CHECK_ENFORCE=1 $GOOD
has dsym-without-dwarf "FAIL: the dSYM's UUID does not match"

APP=none check no-executable 1 -- RELEASE_CHECK_ENFORCE=1 $GOOD
has no-executable "size Anoti: none"
has no-executable "FAIL: nm could not read the executable"

check atos-no-lines 1 -- RELEASE_CHECK_ENFORCE=1 STUB_ATOS_OK=0 $GOOD
has atos-no-lines "FAIL: the dSYM decoded no Kotlin address"

check atos-fails 1 -- RELEASE_CHECK_ENFORCE=1 STUB_ATOS_FAIL=1 $GOOD
has atos-fails "atos failed: atos: cannot parse"

check sources-fail 0 -- STUB_SOURCES_FAIL=1 $GOOD
has sources-fail "dwarfdump --show-sources failed: error: unknown option"
lacks sources-fail "Kotlin files in the dSYM: 0"

check fifty-cap 0 -- STUB_DSYM_KFUN=400 $GOOD
has fifty-cap "Kotlin addresses decoded: 50 of 50"
if [ "$(cat "$work/cases/fifty-cap/atos-count" 2> /dev/null)" = 50 ]; then passed=$((passed + 1))
else failed=$((failed + 1)); echo "FAIL fifty-cap: atos did not get exactly 50 addresses"; fi

check nm-fails 1 -- RELEASE_CHECK_ENFORCE=1 STUB_NM_FAIL_APP=1 $GOOD
has nm-fails "FAIL: nm could not read the executable"

check no-settings-enforced 1 -- RELEASE_CHECK_ENFORCE=1 STUB_XCODEBUILD_FAIL=1 $GOOD
has no-settings-enforced "FAIL: xcodebuild printed no build settings"

check no-settings-report 0 -- STUB_XCODEBUILD_FAIL=1 $GOOD
has no-settings-report "settings: none, xcodebuild: error: no project"

check no-kotlin-sources 0 -- STUB_KT_FILES=0 $GOOD
has no-kotlin-sources "Kotlin files in the dSYM: 0"

echo "passed $passed, failed $failed"
[ "$failed" -eq 0 ]
