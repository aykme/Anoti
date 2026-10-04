#!/bin/bash
# Runs SwiftLint at the version gradle/libs.versions.toml pins, on this machine or on CI.
#
#   swiftlint.sh [path...]        lints the given files, or every file the configuration names
#   swiftlint.sh fix [path...]    corrects what SwiftLint can correct on its own
#   swiftlint.sh analyze <log>    runs the analyzer rules over a clean xcodebuild log
#
# Exit codes: 0 clean, 1 findings, 2 setup error, 3 this machine cannot run SwiftLint. A fix
# run exits 0 even when findings remain, so a lint run follows it.
set -uo pipefail
# A CDPATH match makes cd print the folder, which would break the captures below.
unset CDPATH

setup_error() { echo "swiftlint.sh: $1" >&2; exit 2; }
cannot_run() { echo "swiftlint.sh: this machine cannot run SwiftLint: $1" >&2; exit 3; }

absolute() { (cd "$(dirname "$1")" && echo "$(pwd)/$(basename "$1")"); }

mode=lint
case "${1:-}" in
  fix) mode=fix; shift ;;
  analyze) mode=analyze; shift ;;
esac

paths=()
for path in "$@"; do
  [ -e "$path" ] || setup_error "no such file: $path"
  paths+=("$(absolute "$path")")
done
[ "$mode" != analyze ] || [ ${#paths[@]} -eq 1 ] || setup_error "analyze takes one build log"

root=$(cd "$(dirname "$0")/../.." && pwd)
cd "$root" || exit 2
config=config/swiftlint/swiftlint.yml

read_version() { sed -n "s/^$1 = \"\(.*\)\"/\1/p" gradle/libs.versions.toml; }

on_path() {
  local dir IFS=:
  for dir in $PATH; do [ -f "$dir/$1" ] && return 0; done
  return 1
}

install_toolchain="Install the Swift toolchain: winget install Swift.Toolchain"

case "$(uname -s)" in
  MINGW* | MSYS* | CYGWIN*)
    host=windows archive=SwiftLint.amd64.zip binary=swiftlint.exe
    hash_key=swiftLintSha256Windows
    ;;
  Linux)
    [ "$(uname -m)" = x86_64 ] || cannot_run "the project pins no SwiftLint for $(uname -m)"
    host=linux archive=swiftlint_linux_amd64.zip binary=swiftlint
    hash_key=swiftLintSha256Linux
    ;;
  Darwin)
    host=mac archive=portable_swiftlint.zip binary=swiftlint
    hash_key=swiftLintSha256Mac
    ;;
  *) cannot_run "the project pins no SwiftLint for $(uname -s)" ;;
esac

# SwiftLint dies without SourceKit whatever the rules, so its absence is found up front.
case $host in
  windows)
    if ! on_path sourcekitdInProc.dll; then
      [ -n "${LOCALAPPDATA:-}" ] || cannot_run "no Swift toolchain. $install_toolchain"
      swift_home="$(cygpath -u "$LOCALAPPDATA")/Programs/Swift"
      toolchain=$(ls -d "$swift_home"/Toolchains/*/ 2> /dev/null | sort -rV | head -1)
      toolchain=${toolchain%/}
      [ -f "$toolchain/usr/bin/sourcekitdInProc.dll" ] \
        || cannot_run "no Swift toolchain. $install_toolchain"
      toolchain_version=$(basename "$toolchain")
      runtime="$swift_home/Runtimes/${toolchain_version%%+*}/usr/bin"
      [ -d "$runtime" ] || cannot_run "no Swift runtime next to $toolchain. $install_toolchain"
      export PATH="$toolchain/usr/bin:$runtime:$PATH"
    fi
    ;;
  linux)
    command -v swift > /dev/null \
      || cannot_run "no swift on PATH. Install the Swift toolchain from swift.org"
    ;;
  mac)
    case "$(xcode-select -p 2> /dev/null)" in
      *.app/Contents/Developer) ;;
      *) cannot_run "no Xcode selected. Install Xcode and select it with xcode-select" ;;
    esac
    ;;
esac

version=$(read_version swiftLint)
expected=$(read_version "$hash_key")
[ -n "$version" ] && [ -n "$expected" ] || setup_error "the catalog lacks swiftLint or $hash_key"

sha256() {
  if command -v sha256sum > /dev/null; then sha256sum "$1"; else shasum -a 256 "$1"; fi \
    | cut -d' ' -f1
}

dir="build/swiftlint/$version"
exe="$dir/$binary"
if [ ! -x "$exe" ]; then
  for tool in curl unzip; do command -v $tool > /dev/null || setup_error "$tool is missing"; done
  mkdir -p build/swiftlint
  download=$(mktemp -d build/swiftlint/download.XXXXXX)
  url="https://github.com/realm/SwiftLint/releases/download/$version/$archive"
  if ! curl -fsSL --retry 3 --retry-connrefused --connect-timeout 30 --max-time 300 \
    -o "$download/$archive" "$url"; then
    rm -rf "$download"
    setup_error "could not download $url"
  fi
  actual=$(sha256 "$download/$archive")
  if [ "$actual" != "$expected" ]; then
    rm -rf "$download"
    setup_error "$archive has SHA-256 $actual; the catalog expects $expected"
  fi
  if ! unzip -q -o "$download/$archive" -d "$download"; then
    rm -rf "$download"
    setup_error "could not unpack $archive"
  fi
  rm -f "$download/$archive"
  chmod +x "$download/$binary"
  # Another run may have unpacked the same version meanwhile; both copies are the same binary.
  if [ -x "$exe" ]; then
    rm -rf "$download"
  else
    rm -rf "$dir"
    mv "$download" "$dir" || setup_error "could not move SwiftLint into $dir"
  fi
fi

# The Windows build misreads a CRLF file, and fix corrupts it. MSYS strips a CR inside $(...)
# and in grep's text mode, hence the variable and -U.
if [ $host = windows ] && [ "$mode" != analyze ]; then
  if [ ${#paths[@]} -gt 0 ]; then targets=("${paths[@]}"); else targets=(iosApp); fi
  cr=$'\r'
  crlf=$(grep -rlIU --include='*.swift' "$cr" "${targets[@]}")
  [ $? -le 1 ] || setup_error "could not check the line endings of ${targets[*]}"
  [ -z "$crlf" ] || setup_error "convert these files to LF line endings: $crlf"
fi

reporter=()
[ -z "${GITHUB_ACTIONS:-}" ] || reporter=(--reporter github-actions-logging)

errors=$(mktemp)
trap 'rm -f "$errors"' EXIT
# The ${a[@]+...} form keeps an empty array legal under set -u in macOS's bash 3.2. The cache
# is off: it would hide a file SwiftLint could not read on every run after the first.
case $mode in
  lint)
    "$exe" lint --quiet --no-cache --config "$config" ${reporter[@]+"${reporter[@]}"} \
      ${paths[@]+"${paths[@]}"} 2> "$errors"
    ;;
  fix)
    "$exe" lint --fix --quiet --no-cache --config "$config" ${paths[@]+"${paths[@]}"} \
      2> "$errors"
    ;;
  analyze)
    "$exe" analyze --no-cache --config "$config" ${reporter[@]+"${reporter[@]}"} \
      --compiler-log-path "${paths[0]}" 2> "$errors"
    ;;
esac
code=$?
cat "$errors" >&2

[ $host != windows ] || [ $code -ne 132 ] \
  || cannot_run "SourceKit did not load. Put the Swift runtime next to the toolchain on PATH"
# A clean run warns about nothing, so a warning is a configuration typo or a file SourceKit
# could not index. A file that is not UTF-8 is linted as empty, with only this line to show it.
grep -q "^warning: " "$errors" && setup_error "SwiftLint warned; the warnings are above"
grep -q "^Could not read contents of" "$errors" \
  && setup_error "SwiftLint could not read a file; save it as UTF-8"
case $code in 0 | 2) ;; *) setup_error "SwiftLint failed with exit code $code" ;; esac
if [ "$mode" = analyze ]; then
  # realm/SwiftLint#6877: an unreadable log gives a partial analysis and a green run.
  grep -q "cursor info failed" "$errors" && setup_error "SourceKit refused the compiler arguments"
  analyzed=$(sed -n 's/.* serious in \([0-9]*\) files\{0,1\}\.$/\1/p' "$errors")
  sources=$(find iosApp -name '*.swift' | wc -l | tr -d ' ')
  [ "$analyzed" = "$sources" ] \
    || setup_error "SwiftLint analyzed ${analyzed:-no} of the $sources Swift files"
fi

case $code in
  0) exit 0 ;;
  2) exit 1 ;;
  *) setup_error "SwiftLint failed with exit code $code" ;;
esac
