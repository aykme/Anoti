---
paths:
  - "**/.claude/rules/ios/release-build.md"
  - "**/.github/scripts/ios-release-check.sh"
  - "**/.github/scripts/test/ios-release-check-test.sh"
  - "**/iosApp/project.yml"
---

# The iOS Release build

- Release is built the way an archive is. `project.yml` sets `DEPLOYMENT_POSTPROCESSING` for it,
  so every Release build is stripped. Each one also keeps a dSYM.
- No `Minified` configuration exists. Android needs `minified` only to give `release` the debug
  key. iOS sets no signing team, and a simulator needs none.
- Nothing is obfuscated. Kotlin/Native has no option for it, and class names stay readable.
- The dSYM turns a stripped address back into a name and a line, as `mapping.txt` does on Android.
  A dSYM fits only the binary of its own build, matched by UUID, so a shipped crash needs the
  archive's own dSYM.
- Stripping removes symbol-table entries only. ObjC class names and Kotlin type names survive it.
- No automated test runs on Release; only the manual regression walks it. So Release must not
  behave differently from Debug in the code, as [swift-entry.md](swift-entry.md) says.
- `.github/scripts/ios-release-check.sh` checks a Release build's stripping and dSYM on CI. After a
  change to it, run `bash .github/scripts/test/ios-release-check-test.sh`, then a Release run.
