# New project

How to start a new Kotlin Multiplatform app for Android and iOS in the image of this one: reading
this design from another checkout or copying it, the tools a machine needs, which files to carry
over and what to edit in each, what to rename, the order the first modules come up in, and the
first checks. How the agent setup itself works is in [agent-setup.md](agent-setup.md).

Read when: starting a new project in this image, by pointing Claude at this design or by copying
it; carrying build, CI or agent files over to another repository.

## Two modes

**By path.** The developer names this repository's design, and the new project reads it in place.

- Start Claude in the new project and add this checkout:
  `claude --add-dir <path to this checkout>`. Then read
  [TECHNICAL-DESIGN.md](TECHNICAL-DESIGN.md) and its task-table files by path there.
- Nothing from an added directory loads by default. The Claude Code docs say that
  `CLAUDE_CODE_ADDITIONAL_DIRECTORIES_CLAUDE_MD=1` loads its `CLAUDE.md`, `.claude/CLAUDE.md`,
  `.claude/rules/*.md` and `CLAUDE.local.md`. They do not say that a rule with `paths:` then
  fires on the new project's files, and this project has not tested it. The variable would also
  load this repository's git and machine facts into the new session.
- So read the rules by hand. Up front: [CLAUDE.md](../../CLAUDE.md),
  [CLAUDE-ANDROID.md](../rules/android/CLAUDE-ANDROID.md),
  [CLAUDE-IOS.md](../rules/ios/CLAUDE-IOS.md), and the guards nearly every edit needs:
  [source-sets.md](../rules/source-sets.md), [code-comments.md](../rules/code-comments.md),
  [logging.md](../rules/logging.md).
- Then read each topic file at the situation its index line names. No path will ever deliver one,
  so the area-pointer rule is replaced by the task table. Watch the index lines a path usually
  makes unnecessary: [android/r8-minified.md](../rules/android/r8-minified.md),
  [ios/build-check.md](../rules/ios/build-check.md), [ios/versions.md](../rules/ios/versions.md),
  [ios/xcode-project.md](../rules/ios/xcode-project.md) and
  [ios/release-build.md](../rules/ios/release-build.md).
- `Example:` links resolve inside this checkout; open them there.

**By copy.** The new repository gets its own copy and keeps it current itself.

- Copy `.claude/design`, `.claude/rules`, `.claude/skills` and `CLAUDE.md`.
- Replace every `Example:` link with the new project's own file once it exists, or drop it. Until
  then the link check fails on it, as [agent-setup.md](agent-setup.md#link-check) describes.
- Rewrite the "In this project" column of the placeholder table in
  [TECHNICAL-DESIGN.md](TECHNICAL-DESIGN.md#placeholders).
- Rewrite the facts below that belong to this repository, this developer or this machine.

| File | Facts to rewrite |
|---|---|
| `CLAUDE.md` | remote name `master`, repository `aykme/Anoti`, the `gh` path on the developer's Windows machine, the `develop` branch, the root `README.md` rule, the foreign user-level plugins under "Skills" |
| `.claude/skills/creating-a-module/` | `ANOTI-FULL-REGRESS.md` in `SKILL.md` and the script; in the script, the groups `core-kmp` and `feature-kmp`, the navigation module the root configs are read from, and the entry module `main` |
| `ci-github.md` | `aykme/Anoti` in every `gh` command; `develop` as the branch that runs both workflows |
| `ios/xcode-project.md` | `aykme/Anoti` in `gh run download` |
| `logging.md` | `ANOTI_TAG`; the restore-check script that quotes log wording |
| `module-docs.md` | `ANOTI-FULL-REGRESS.md`; the example `:core-kmp:celebrity`; the module folders `androidApp`, `main`, `core-kmp/*`, `feature-kmp/*`, `iosApp/` |
| `tests-on-device.md` | `AnimeFavoritesUserFlowTest`, the iOS UI tests and restore checks as allowed live-backend tests; `RetryRule` in `core-kmp:test-utils`; the two script paths |
| `android/instrumented-tests.md` | the task `:feature-kmp:anime-notification:connectedAndroidDeviceTest`; the live backend |
| `ios/ci.md` | `AnimeFavoritesUserFlowTest`, `OrientationUITests`, the restore and theme checks; the media folder `~/Desktop/iOS test/`; `develop` as the branch whose push runs it |
| `ios/swiftlint.md` | `develop` as the push that runs the lint |
| `finishing-a-task.md` | the media folder `~/Desktop/iOS test/` |
| `test-coverage.md` | the 95% bar; `repeatingClickable`, `Colors.kt`, `Fonts.kt`; `core-kmp:test-utils` in text and `paths:` |
| `tests.md` | `core-kmp/test-utils` in `paths:` |
| `ios/build-check.md`, `ios/tests.md` | the developer's Windows machine; `DiAppComponent` in `core-kmp:di-app` |
| `ios/framework.md`, `CLAUDE-IOS.md` | `core-kmp:di-app` as the module that links the framework |
| `CLAUDE-ANDROID.md` | the developer's own phone as a device that is not a test bench |
| `code-documentation` skill | its examples from this project's modules, in `SKILL.md` and `references/` |

## Prerequisites

- A JDK to start Gradle. The daemon runs on JDK 21 and is downloaded when the machine has none;
  see [build-and-tooling.md](build-and-tooling.md#gradle-settings).
- The Android SDK at the catalog's `compileSdk`, and the NDK the catalog names next to `agp`.
  Without that NDK the minified build differs from CI's; see
  [android/r8-minified.md](../rules/android/r8-minified.md).
- For iOS: a Mac with the Xcode release and XcodeGen the catalog pins. Off macOS the iOS targets
  only compile; see [ios/build-check.md](../rules/ios/build-check.md).
- A Swift toolchain for SwiftLint on a machine without Xcode; see
  [ios/swiftlint.md](../rules/ios/swiftlint.md).
- The GitHub CLI for CI runs.
- Python 3 for the `creating-a-module` check (`python`, or `python3` where only that exists).

## Carry-over files

| File | What to edit |
|---|---|
| [build.gradle.kts](../../build.gradle.kts) | the `testsdk` package under the root package; the Kover filters naming the root package (`generated.resources`, `testutils`) and the Room constructor class |
| [settings.gradle.kts](../../settings.gradle.kts) | `rootProject.name`; the `include` list |
| [gradle.properties](../../gradle.properties) | nothing app-specific |
| [.gitattributes](../../.gitattributes), [.editorconfig](../../.editorconfig) | nothing; LF everywhere, CRLF only for `*.bat` |
| [.gitignore](../../.gitignore), [androidApp/.gitignore](../../androidApp/.gitignore), [main/.gitignore](../../main/.gitignore) | the root file's `androidApp/release` paths if the host is renamed; the module files hold only `/build` |
| `gradlew`, `gradlew.bat`, [gradle/wrapper](../../gradle/wrapper/gradle-wrapper.properties) | nothing; keep the comments |
| [gradle/gradle-daemon-jvm.properties](../../gradle/gradle-daemon-jvm.properties) | nothing; keep the comments |
| [gradle/libs.versions.toml](../../gradle/libs.versions.toml) | reset `versionName` to `1.0` and `versionCode` to `10`, by its rule comment; drop the libraries only dropped features used, keeping the core modules' ones; keep the sections and reason comments |
| [config/detekt](../../config/detekt/detekt.yml), [config/swiftlint](../../config/swiftlint/swiftlint.yml) | nothing |
| [androidApp/proguard-rules.pro](../../androidApp/proguard-rules.pro) | nothing |
| [.github/workflows](../../.github/workflows/android.yml), [actions](../../.github/actions/ios-toolchain/action.yml) | the `develop` branch in `on.push`; app name in `.app` and dSYM paths; the flow test's name in inputs and steps; `TEST_RUNNER_<APP>_*` variables |
| [.github/scripts](../../.github/scripts/ios-ci-plan.sh) and [their tests](../../.github/scripts/test/ios-release-check-test.sh) | executable name and `kfun:` package prefix in the release check and its fixtures; the flow test's name in the plan script; bundle id, payload and quoted log lines in the restore checks, which belong to the first feature |
| [iosApp/project.yml](../../iosApp/project.yml) | the framework search path if the composition root moves; `Shared` stays |
| [Config.xcconfig](../../iosApp/Configuration/Config.xcconfig) | `BUNDLE_ID`, `APP_NAME`; `Version.xcconfig` is regenerated, not edited |
| [Info.plist](../../iosApp/iosApp/Info.plist) | the background-task identifier, if any |
| [PrivacyInfo.xcprivacy](../../iosApp/iosApp/PrivacyInfo.xcprivacy) | the accessed API categories and reasons the new app really has |
| [iosApp/scripts](../../iosApp/scripts/compile-kotlin-framework.sh) | the composition root's Gradle path, if it moves |
| [ANOTI-FULL-REGRESS.md](../../ANOTI-FULL-REGRESS.md) | renamed `<APP>-FULL-REGRESS.md`; links emptied |
| `.claude`, `CLAUDE.md` | as in "Two modes" |

The Xcode project is generated from `project.yml` and committed; take the first one from an
`ios.yml` run, as [ios/xcode-project.md](../rules/ios/xcode-project.md) says.

## Renames

- The root package `com.alekseivinogradov.anoti`: directories, `namespace`, `applicationId`,
  `BUNDLE_ID`, and every package a script or filter names.
- The app name forms `Anoti`, `ANOTI`, `anoti`: `rootProject.name`, `APP_NAME`, `app_name` in the
  Android host's strings, `<APP>-FULL-REGRESS.md`.
- The themes: the composable `AnotiTheme` and the Android style `Theme.Anoti`.
- The log tag `ANOTI_TAG` in the UI kit's consts file, and the log lines CI scripts quote.
- The Android `Application` class `AnotiApp` and its manifest entry; the Swift app struct
  `AnotiApp`.
- Every `packageOfResClass`, which repeats the root package.
- The framework keeps the name `Shared`.

## Bring-up order

Each step creates a module through [new-module.md](new-module.md); the owning file says how its
contents are written.

1. DI-scope leaf: [dependency-injection.md](dependency-injection.md#scopes).
2. UI kit and common core: [ui-compose.md](ui-compose.md#theme),
   [concurrency-and-lifecycle.md](concurrency-and-lifecycle.md#coroutine-contexts),
   [error-handling.md](error-handling.md).
3. Network: [data-layer.md](data-layer.md#network).
4. Navigation: [navigation.md](navigation.md#root-stack).
5. Persistence, if the app keeps data: [data-layer.md](data-layer.md#persistence).
6. Test utilities: [testing.md](testing.md#test-doubles).
7. Entry module: [navigation.md](navigation.md), [state-restoration.md](state-restoration.md),
   [platform-mirroring.md](platform-mirroring.md#startup).
8. Composition root: [dependency-injection.md](dependency-injection.md#creating-graphs),
   [kmp.md](kmp.md#the-ios-framework).
9. Android host: [platform-mirroring.md](platform-mirroring.md#startup),
   [module-anatomy.md](module-anatomy.md#manifests-and-resources),
   [versioning-and-release.md](versioning-and-release.md).
10. iOS host: [ios-host.md](ios-host.md), [ios/xcode-project.md](../rules/ios/xcode-project.md).
11. CI: [build-and-tooling.md](build-and-tooling.md#ci), [ci-github.md](../rules/ci-github.md).
12. First feature: [recipes.md](recipes.md#new-screen-feature).

## First verification

- `./gradlew :androidApp:assembleDebug` and `./gradlew allTests :androidApp:testDebugUnitTest`.
- `./gradlew detektAll`.
- The local iOS check in [ios/build-check.md](../rules/ios/build-check.md).
- `./gradlew koverVerify`, against the bar in [test-coverage.md](../rules/test-coverage.md).
- `./gradlew :androidApp:assembleMinified`, installed and walked on an emulator as
  [android/r8-minified.md](../rules/android/r8-minified.md) says.
- One `android.yml` and one `ios.yml` run, started as [ci-github.md](../rules/ci-github.md) says.
- The link check over the copied agent files.

## What not to copy

- Feature modules, their READMEs and regression files, and the UI tests and restore cases built
  on them.
- Claude's memory; it stays with this repository on this machine.
- Worktrees, `docs/superpowers/`, `.superpowers/` and `.claude/settings.local.json`.
- Generated and local files: `build/`, `.gradle/`, `.kotlin/`, `.idea/`, `local.properties`,
  `Version.xcconfig` and the Xcode project, which are generated again.
- The root `README.md` and `LICENSE`; the new project writes its own.
