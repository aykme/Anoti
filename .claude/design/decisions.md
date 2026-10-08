# Decisions

The settled technical choices, one entry each: what was chosen, why, what was turned down, and
what would reopen it, where a trigger is recorded. The linked design file holds the how. Choices a
rule enforces get one line and the rule's link. The `production` branch holds the earlier,
Android-only stack that several entries name.

Read when: about to propose a library, a tool or an approach that replaces or reverses one of
these; when a review finding questions a choice listed here; before a version-bump or cleanup pass.

## Kotlin-inject without Anvil

- Status: accepted (2026-08-12; Anvil dropped 2026-08-19).
- Why: kotlin-inject builds the graph at compile time through KSP, on every target. Dagger runs
  only on the JVM, so it cannot serve the iOS app.
- Rejected: Dagger, the DI of the `production` branch. kotlin-inject-anvil: on Kotlin/Native its
  KSP step missed contributions from other modules (upstream issue amzn/kotlin-inject-anvil#118),
  and the library was in maintenance mode.
- Revisit when: a contribution-merging library works on Kotlin/Native and is maintained.
- Applied in: [dependency-injection.md](dependency-injection.md).

## Decompose for navigation

- Status: accepted (2026-08-13).
- Why: the navigation state lives in `commonMain` and is saved through Essenty's `StateKeeper`.
  Decompose and MVIKotlin share Essenty, so one lifecycle model serves both.
- Rejected: Jetpack Navigation with Fragments and a navigation graph, the `production` setup.
- Applied in: [navigation.md](navigation.md).

## MVIKotlin for stores

- Status: accepted (in use since before the multiplatform migration).
- Why: stores, executors and controller bindings run in `commonMain` on both platforms. Binders
  follow Essenty lifecycles, the same ones Decompose hands out.
- Rejected: no alternative is recorded.
- Applied in: [mvi.md](mvi.md).

## Room KMP with bundled SQLite

- Status: accepted (2026-08-09).
- Why: Room has stable multiplatform support, and the Android app already used Room. Existing
  installations keep their database file and data; continuity tests on both platforms check it.
- The bundled driver is set once in common code. It ships its own SQLite build, so both
  platforms run the same one. Example:
  [AnimeDatabase.kt](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/impl/data/AnimeDatabase.kt)
- Rejected: Realm Kotlin, an earlier plan for multiplatform storage.
- Applied in: [data-layer.md "Persistence"](data-layer.md#persistence).

## Ktor with platform engines

- Status: accepted.
- Why: one `HttpClient` and its configuration live in `commonMain`. Only the engine is per
  platform: OkHttp on Android, Darwin on iOS. Ktor's `MockEngine` stands in for the network in
  tests. Example: [build.gradle.kts](../../core-kmp/network/build.gradle.kts)
- Rejected: Retrofit with Moshi, the JVM-only stack of the `production` branch.
- Applied in: [data-layer.md "Network"](data-layer.md#network).

## OkHttp follows Ktor

- Status: accepted (2026-09-16).
- Why: no code calls OkHttp directly; it only serves as Ktor's engine. Pinning it above Ktor's
  own pin ships Ktor a runtime it was not tested against, for nothing the app uses.
- Rejected: the OkHttp BOM and its catalog entries. A "bump to latest" pass leaves this alone.
- Revisit when: a CVE lands in OkHttp ahead of Ktor, the app starts using OkHttp directly
  (interceptor, certificate pinning, logging), or a second OkHttp artifact diverges in version.
- Applied in: [tech-stack.md](tech-stack.md).

## Compose Multiplatform for all UI

- Status: accepted (migration finished 2026-09).
- Why: one UI in `commonMain` serves both apps. Platform code keeps only what the OS alone does.
- Rejected: Fragments with Android Views and XML layouts, the `production` UI.
- Applied in: [ui-compose.md](ui-compose.md).

## Material 3 pinned to its last stable release

- Status: accepted.
- Why: Material 3 has no stable release on the Compose Multiplatform line above 1.9.0. Everything
  newer is an alpha. The reason sits next to the version in
  [libs.versions.toml](../../gradle/libs.versions.toml).
- Rejected: following the Compose Multiplatform version into an alpha.
- Revisit when: a stable Material 3 newer than 1.9.0 ships.
- Applied in: [tech-stack.md](tech-stack.md).

## Configuration changes kept by the activity

- Status: accepted.
- Why: the screens are Compose-only and draw no orientation-, size- or density-qualified
  resources. A rotation, a resize or a density change has nothing to swap. Keeping the activity
  alive avoids rebuilding the root graph and every store. Other changes still recreate it.
  Example: [AndroidManifest.xml](../../main/src/androidMain/AndroidManifest.xml)
- Rejected: Android's default recreation for those changes.
- Revisit when: a resource qualified by orientation, size or density is added.
- Applied in: [state-restoration.md "Scenarios"](state-restoration.md#scenarios).

## Platform-owned root state

- Status: accepted (2026-10-02).
- Why: each platform keeps the state its documented way. Android uses the saved instance state
  through `defaultComponentContext()` and Compose's own saveable registry. iOS keeps one string
  in the scene's `@SceneStorage`. Neither Compose Multiplatform nor Decompose saves anything on
  iOS, so an iOS-only holder and codec fill that one gap. Example:
  [IosRootHolder](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/IosRootHolder.kt)
- A notification tap navigates the live root on both platforms. Example:
  [MainActivity](../../main/src/androidMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/MainActivity.kt)
- Rejected: one common pipeline with self-made session and saved-state file classes; a file
  store on Android; the app delegate's coder hooks on iOS; clearing the task on a tap.
- Revisit when: Compose Multiplatform or Decompose starts saving state on iOS.
- Applied in: [state-restoration.md](state-restoration.md) and
  [platform-mirroring.md](platform-mirroring.md).

## Unscoped store bindings

- Status: accepted.
- Why: the database is the single source of truth. Every store instance collects the same flow,
  so what one writes reaches all. An extra collector costs nothing. Each reader reads the
  binding once and disposes what it got. Example:
  [RootHost](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHost.kt)
- Rejected: an app-scoped single store. Its executor coroutines belong to the store's scope, so it
  would keep its database subscription open for the whole process.
- Revisit when: a store holds state of its own that its readers must share.
- Applied in: [dependency-injection.md "Scopes"](dependency-injection.md#scopes), [mvi.md](mvi.md).

## Compose compiler on native targets only in the composition root

- Status: accepted (2026-09-29).
- Why: the composition root links the iOS framework and holds no composable code. On Android the
  compiler would add stability members to its DI classes. Measured: R8 then merged classes
  differently, and 4635 lines of `mapping.txt` changed across 327 classes.
- Rejected: the Compose compiler on every target of that module.
- Revisit when: that module gains composable code.
- Applied in: [kmp.md "The iOS framework"](kmp.md#the-ios-framework).

## Catalog sections by platform

- Status: accepted.
- Why: one catalog serves both apps. Every entry sits under the header of the platform that owns
  it: KMP, Android or iOS. Values both apps share, such as `versionCode`, are KMP.
- Rejected: renaming the iOS sections to "Kotlin/Native (iOS)" and deleting the empty ones.
- Applied in: [tech-stack.md](tech-stack.md).

## Gradle daemon on JDK 21

- Status: accepted.
- Why: every entry point builds on the same JDK: terminal, Android Studio and CI. With Studio on
  its bundled JBR 25 and the terminal on 21, one shared build cache broke only the IDE.
- Its download links are Adoptium's "latest 21"; the foojay links `updateDaemonJvm` writes go stale.
  Example: [gradle-daemon-jvm.properties](../../gradle/gradle-daemon-jvm.properties)
- Rejected: `jvmToolchain(...)` in the root build, and the foojay toolchain resolver plugin. Each
  broke AAPT2 on the developer's Windows machine.
- Applied in: [build-and-tooling.md](build-and-tooling.md).

## RedundantSuspendModifier off

- Status: accepted (2026-09-16).
- Why: detekt cannot resolve the generated Compose Resources accessors. It reads every
  `getString` call as non-suspending and reports the modifier as redundant. Android Studio
  resolves them and still reports genuinely redundant modifiers. Example:
  [detekt.yml](../../config/detekt/detekt.yml)
- Rejected: a per-function `@Suppress`, which Studio then reports as redundant; scoping the rule
  with `excludes:`, since the false positives are not confined to a few files.
- Revisit when: detekt resolves suspend calls in this project.
- Applied in: [build-and-tooling.md](build-and-tooling.md).

## No dependency analysis plugin

- Status: accepted (2026-09-23).
- Why: a trial of dependency-analysis-gradle-plugin showed no APK gain, since R8 already strips
  unused code. 33 of its 44 findings were KMP false positives: it advised replacing Compose
  Multiplatform artifacts with androidx ones, which breaks iOS. It reads JVM bytecode, so it
  cannot see Kotlin/Native at all.
- Rejected: that plugin. The Gradle files of a change are reviewed by hand, as
  [finishing-a-task.md](../rules/finishing-a-task.md) says.
- Revisit when: a dependency analyzer understands KMP and Kotlin/Native.

## Rule-backed decisions

- The app logs with `println` only: [logging.md](../rules/logging.md).
- Test doubles are handwritten and end in `Fake`:
  [testing.md "Test doubles"](testing.md#test-doubles).
- One coverage bar over the whole project, enforced by `koverVerify`:
  [test-coverage.md](../rules/test-coverage.md).
- A `minified` build type is `release` with the debug key:
  [r8-minified.md](../rules/android/r8-minified.md).
- XcodeGen generates the Xcode project, and the result is committed:
  [xcode-project.md](../rules/ios/xcode-project.md).
- Every iOS version comes from the catalog: [versions.md](../rules/ios/versions.md).
- One iOS framework, `Shared`: [kmp.md "The iOS framework"](kmp.md#the-ios-framework).

## Deliberate absences

- No reflection-based DI. kotlin-inject generates the graph at compile time, so a missing binding
  fails the build, not the app.
- No general-purpose mocking framework. The JVM ones (MockK, Mockito) do not run on Kotlin/Native,
  which `commonTest` targets; the multiplatform ones were not adopted, since handwritten fakes and
  library mocks cover every double: [testing.md "Test doubles"](testing.md#test-doubles).

Also absent, with no decision recorded: an analytics SDK, a crash-reporting SDK, feature flags,
and a second locale (every `composeResources` has only its default `values` folder). Shipped
stack traces stay decodable: [r8-minified.md](../rules/android/r8-minified.md) on Android,
[release-build.md](../rules/ios/release-build.md) on iOS.
