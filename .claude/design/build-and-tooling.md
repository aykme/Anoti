# Build and tooling

How the Gradle build is put together above the modules: the root build script, the Gradle and
daemon settings, static analysis, coverage aggregation and the commands a task runs. What one
module's build file holds is in [.claude/design/new-module.md](new-module.md). Libraries and the
version catalog are in [.claude/design/tech-stack.md](tech-stack.md).

Read when: changing the root `build.gradle.kts`, `gradle.properties`, the daemon JVM settings,
`config/` or a workflow; looking for the Gradle command that checks something.

## Root build

The root [build.gradle.kts](../../build.gradle.kts) holds everything shared by all modules.

- Plugins are declared with `apply false`, so the modules apply them by catalog alias. Kover is the
  one plugin applied at the root, since the root aggregates coverage.
- The Compose compiler plugin is declared only to put its extension on the classpath. The
  `subprojects` block configures that extension.
- Catalog values the `subprojects` block needs are read into root-level `val`s first. A project's
  catalog accessor exists only once that project is evaluated.
- The `subprojects` block reacts to plugins with `plugins.withId`, so a module gets each setting
  only when it applies the plugin.

Detekt, for every module that applies its plugin:

- Detekt builds on its default config, with
  [config/detekt/detekt.yml](../../config/detekt/detekt.yml) on top. It adds the
  `detekt-formatting` and Compose rule plugins.
- Every detekt task skips files under `build/`, matched on the absolute path, so generated KSP and
  Compose resources sources stay out.
- The plugin registers tasks per compilation only. A shared test source set belongs to none, so
  the root registers `detektCommonTest`, `detektIosTest` and `detektAndroidTest` by hand. Each is
  given the extension's config and its own report names.
- `detektAll` runs every detekt task of every module.
- Detekt resolves types only for the JVM and Android targets. A rule that needs type resolution
  reports on `androidMain` alone, as the config's header says.

Robolectric, for every module with host tests:

- A KMP module with a `src/androidHostTest/kotlin` directory gets two generated sources. One is a
  `robolectric.properties` resource with the catalog's `robolectricSdk`. The other is
  `TestSdkVersions.kt` in `<root-package>.testsdk`, holding `MIN_SDK` from the catalog's `minSdk`.
- A module that declares the source set but holds no tests gets neither, since Gradle would then
  fail for finding no tests.
- The Android host gets the properties per variant through the variant API, and no
  constant.
- How tests use them is in [.claude/design/testing-platforms.md](testing-platforms.md#android),
  section "Android".

Compose compiler reports:

- The reports are off until `-PcomposeCompilerReports` is passed. They then land in
  `<module>/build/compose_compiler/`.
- On Kotlin/Native the report's module name is set from the Gradle path without colons. On
  Windows a colon diverts the content into an NTFS alternate data stream.
- When the reports are due and how to read them is in
  [compose-compiler-reports.md](../rules/compose-compiler-reports.md).

Kover:

- Every subproject with its own build file applies Kover and feeds the root's aggregated report.
  Path-only projects such as `:core-kmp` are left out.
- One filter function serves every module and the aggregate, so both count the same classes.
- The root's `verify` rule checks whole-project line coverage against
  `wholeProjectLineCoverageMinimum`. The bar, the filters and the commands are owned by
  [test-coverage.md](../rules/test-coverage.md).

iOS versions:

- The root registers `generateIosVersionXcconfig`, and the Android host's `preBuild` depends on it.
  What it writes and how a version changes is in
  [.claude/design/versioning-and-release.md](versioning-and-release.md).

## Gradle settings

[gradle.properties](../../gradle.properties) sets:

- a large Gradle and Kotlin daemon heap, parallel execution, the build cache, configure on demand,
  file-system watching and ten workers;
- the configuration cache off, with the reason in a comment;
- `android.nonTransitiveRClass` and AndroidX;
- `kotlin.code.style=official`;
- `kotlin.native.ignoreDisabledTargets`, which silences the notice about iOS targets a non-Mac
  host cannot run.

CI writes smaller heap and worker settings into the runner's own `~/.gradle/gradle.properties`.
Example: [the gradle-memory action](../../.github/actions/gradle-memory/action.yml).

[gradle/gradle-daemon-jvm.properties](../../gradle/gradle-daemon-jvm.properties) runs the daemon on
JDK 21. A machine without one downloads it from Adoptium's "latest JDK 21" links. Rerunning
`updateDaemonJvm` writes one pinned build's links back, so the file's comment says to restore
these afterward. Why this was chosen is in [.claude/design/decisions.md](decisions.md).

The Gradle wrapper version follows the AGP policy in the catalog. Its comment sits next to
`distributionUrl` in [gradle-wrapper.properties](../../gradle/wrapper/gradle-wrapper.properties).

Files and line endings:

- [.editorconfig](../../.editorconfig) sets UTF-8, LF, four-space indents and a 100-column limit
  for Kotlin, Kotlin script and Swift.
- [.gitattributes](../../.gitattributes) stores and checks out text with LF on every machine, CRLF
  only for `*.bat`. Why that matters for the minified build is in
  [android/r8-minified.md](../rules/android/r8-minified.md).

## Static analysis

- Kotlin is checked by detekt, as configured above. Android Lint is not part of any workflow.
- Swift is checked by SwiftLint with
  [config/swiftlint/swiftlint.yml](../../config/swiftlint/swiftlint.yml); when a run is due and
  how to run it is in [ios/swiftlint.md](../rules/ios/swiftlint.md).
- Both configs turn off the comment-spacing rule, since test bodies carry `//Given`, `//When` and
  `//Then`.

## Commands

| Command | What it does |
|---|---|
| `./gradlew :androidApp:assembleDebug` | Builds the debug app |
| `./gradlew allTests :androidApp:testDebugUnitTest` | Runs every host test; no device test |
| `./gradlew detektAll` | Runs detekt over every module and source set |
| `./gradlew koverVerify` | Checks the whole-project coverage bar |
| `./gradlew :<module>:koverLog` | Prints one module's coverage while writing tests, e.g. `:feature-kmp:anime-list:koverLog` |
| `./gradlew :androidApp:assembleMinified` | Builds the minified app |
| `./gradlew generateIosVersionXcconfig` | Regenerates the iOS version file |

- The device tests' commands are in
  [android/instrumented-tests.md](../rules/android/instrumented-tests.md).
- The local iOS check off macOS is in [ios/build-check.md](../rules/ios/build-check.md).
- When `koverVerify` runs is in [finishing-a-task.md](../rules/finishing-a-task.md).

## CI

- `android.yml` runs on Linux. `ios.yml` runs its plan, SwiftLint and drift jobs on Linux, and
  every job that builds or runs iOS code on macOS. When they run and how to start and follow a run
  is in [ci-github.md](../rules/ci-github.md).
- What each checks is in [android/ci.md](../rules/android/ci.md) and
  [ios/ci.md](../rules/ios/ci.md).
- Steps several iOS jobs repeat live in composite actions under `.github/actions/`. The
  `ios-toolchain` action reads the Xcode and XcodeGen versions from the catalog. Example:
  [ios-toolchain](../../.github/actions/ios-toolchain/action.yml).
- Logic too long for a workflow step lives in a script under `.github/scripts/`. The plan script
  and the release check have tests under `.github/scripts/test/`. When to run them is in
  [ios/ci.md](../rules/ios/ci.md) and [ios/release-build.md](../rules/ios/release-build.md).
  Example: [ios-ci-plan-test.sh](../../.github/scripts/test/ios-ci-plan-test.sh).
- R8 and the minified build are covered by
  [android/r8-minified.md](../rules/android/r8-minified.md).
