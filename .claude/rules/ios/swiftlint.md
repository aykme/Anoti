---
paths:
  - "**/.claude/rules/ios/swiftlint.md"
  - "**/*.swift"
  - "**/config/swiftlint/**"
  - "**/iosApp/scripts/swiftlint.sh"
---

# Before committing Swift

SwiftLint checks every Swift file being committed, on the machine itself. It runs every lint rule
of `config/swiftlint/swiftlint.yml`. Its analyzer rules need a Mac, so they run in `ios.yml` only.

A commit makes a lint run due when it adds or changes a `*.swift` file. A change to how SwiftLint
runs does too: `config/swiftlint/swiftlint.yml`, `iosApp/scripts/swiftlint.sh`, or a `swiftLint*`
entry of the catalog. Nothing else does. SwiftLint reads no Kotlin, `iosMain` included. It reads
none of `Info.plist`, `project.yml`, the `.xcconfig` files and the asset catalogs either.

1. Run `bash iosApp/scripts/swiftlint.sh` with the committed Swift files as arguments, from
   `git diff --cached --name-only --diff-filter=d -- '*.swift'`. After a change to how SwiftLint
   runs, pass no argument, so every file is linted.
2. On exit code `1`, fix the findings and run it again until it exits `0`.
   `swiftlint.sh fix` corrects what it can, and its edits are read before they are committed. A
   finding that would need a substantial change to the logic goes to the developer.
3. On `2`, fix the setup problem the message names.
4. On `3`, this machine cannot run SwiftLint. Tell the developer at once what is missing and how
   to install it. On Windows that is `winget install Swift.Toolchain`. On macOS, it is Xcode, and
   on Linux the swift.org toolchain. Offer three ways on:
   - install it now;
   - push the branch and start a lint-only run, as [ci.md](ci.md) says, then fix, commit and run
     again until it passes;
   - commit without the lint, leaving the check to the push to `develop`.

   The push rule of [CLAUDE.md](../../../CLAUDE.md) applies: the developer's word every time, and
   an explicit question before any push even when told earlier not to stop.

A change to the analyzer rules needs an `ios.yml` run with `swiftlint` on and without `lint_only`
or `release`, since both skip the `app` job.

The compiler covers two checks SwiftLint cannot make. The build treats Swift warnings as errors,
so a deprecated API stops it. `SWIFT_STRICT_CONCURRENCY` is `complete`, so a call into
`XCUIApplication` or `XCUIElement` off the main actor stops it too.

What is left to a person:

- The formatting of files around it, where no rule speaks.
- A UI test's body is split by `//Given`, `//When` and `//Then`, as in Kotlin.
- Imports nothing uses. The analyzer finds them on CI only.
- Read the CI build logs of the round for warnings. A Release run keeps its log in the
  `ios-release` artifact as `build-release.log`. Off macOS nothing compiles Swift.
