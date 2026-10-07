# Tech stack

The libraries and Gradle plugins the project is built on, what each one is for and where it is
used, the conventions of the version catalog that pins them, and the steps for adding a library.
Why a library was chosen over its alternatives is in [decisions.md](decisions.md).

Read when: adding, removing or bumping a library or a plugin; editing
`gradle/libs.versions.toml`; looking for the library that already does something.

## Libraries

Every entry lives in [gradle/libs.versions.toml](../../gradle/libs.versions.toml). Aliases are
written as build files use them, after `libs.`.

Build plugins:

| Library | Role | Where | Alias |
|---|---|---|---|
| Kotlin Multiplatform | KMP targets and source sets | every module but the Android host | `plugins.kotlinMultiplatform` |
| AGP KMP library plugin | The Android target of a KMP module | every KMP module | `plugins.androidKotlinMultiplatformLibrary` |
| AGP application plugin | The Android host | Android host | `plugins.android.application` |
| Compose Multiplatform + Compose compiler | Compose and its resources; always applied together | modules with UI or resources, and the composition root for the framework | `plugins.composeMultiplatform`, `plugins.kotlinCompose` |
| kotlinx.serialization plugin | `@Serializable` classes | navigation, network, shared base module, screen features | `plugins.kotlinSerialization` |
| KSP | Code generation for kotlin-inject components and Room | only modules with a `@Component` or Room | `plugins.ksp` |
| Room plugin | Room schema settings | persistence | `plugins.androidx.room` |
| detekt + formatting + Compose rules | Static analysis | every module, set up by the root build | `plugins.detekt`, `detekt.formatting`, `detekt.compose` |
| Kover | Coverage | root build, applied to every module | `plugins.kover` |

Shared code:

| Library | Role | Where | Alias |
|---|---|---|---|
| kotlinx.coroutines | Concurrency, flows | almost every module | `kotlinx.coroutines.core` |
| kotlinx.serialization JSON | JSON for the network, deep-link payloads and iOS saved state | network, navigation, entry module, features | `kotlinx.serialization.json` |
| kotlinx.collections.immutable | Immutable collections in UI models | screen features | `kotlinx.collections.immutable` |
| kotlinx-datetime | Date formatting | UI kit | `kotlinx.datetime` |
| kotlin-inject | Compile-time DI | runtime in every module with DI annotations; compiler where a `@Component` lives | `kotlin.inject.runtime.kmp`, `kotlin.inject.compiler.ksp` |
| Decompose | Root child stack and screen components | navigation, entry module, screen features | `decompose`, `decompose.extensions.compose` (entry module only) |
| Essenty | Lifecycle and state keeper under Decompose and MVIKotlin | entry module, screen features, root-level element | `essenty.lifecycle`, `essenty.state.keeper` |
| MVIKotlin | Stores, executors, binders | UI kit, persistence, features, entry module | `mvikotlin`, `mvikotlin.extensions.coroutines`, `mvikotlin.main` |
| Ktor client | HTTP with content negotiation and JSON | network; core also in the shared base module | `ktor.client.core`, `ktor.client.content.negotiation`, `ktor.serialization.kotlinx.json` |
| Ktor engines | Platform HTTP engines | network `androidMain`, `iosMain` | `ktor.client.okhttp`, `ktor.client.darwin` |
| Room + bundled SQLite | Persistence on both platforms | persistence | `androidx.room.runtime`, `androidx.room.compiler`, `androidx.sqlite.bundled` |
| Coil | Image loading over Ktor | screen features (Compose), notification module (posters) | `coil`, `coil.compose`, `coil.network.ktor3` |
| Compose Multiplatform | Runtime, foundation, UI, resources | every module with UI | `compose.runtime`, `compose.foundation`, `compose.ui`, `compose.components.resources` |
| Material 3 | Material components and theme; pinned at its last stable release | UI kit and UI modules | `compose.material3` |
| runtime-saveable | `rememberSaveable` codec on iOS | entry module `iosMain` | `compose.runtime.saveable` |
| Compose tooling | Previews | UI modules; `ui-tooling` on `androidRuntimeClasspath` | `compose.ui.tooling.preview`, `compose.ui.tooling` |

Android only:

| Library | Role | Where | Alias |
|---|---|---|---|
| AndroidX Activity | `ComponentActivity`, `setContent`, edge-to-edge, permission launcher | entry module `androidMain` | `androidx.activity`, `androidx.activity.compose` |
| AndroidX Core | `NotificationCompat`, permission checks | entry module, notification module | `androidx.core` |
| AndroidX Window | Window metrics for orientation | entry module `androidMain` | `androidx.window` |
| WorkManager | Background update | background update module `androidMain`, composition root (KSP), Android host | `androidx.work.runtime` |
| Play services app set | Backs the `AD_ID` permission; the reason is in the Android host's build file | Android host | `play.services.appset` |

Tests:

| Library | Role | Where | Alias |
|---|---|---|---|
| kotlin-test | Assertions | every `commonTest` | `kotlin.test` |
| kotlin-test-junit | kotlin-test wired to JUnit 4 for a non-KMP module; JUnit rules | Android host tests, test utilities | `kotlin.test.junit` |
| kotlinx-coroutines-test | Virtual time | tests | `kotlinx.coroutines.test` |
| Ktor MockEngine | HTTP doubles | tests | `ktor.client.mock` |
| Robolectric | Android on the JVM for host tests; its SDK is pinned in the catalog | every module with `androidHostTest` sources, Android host tests | `robolectric`, `versions.robolectricSdk` |
| Compose ui-test (multiplatform) | Compose tests in common code and on iOS | test utilities, entry module `iosTest` | `compose.ui.test` |
| Compose ui-test-junit4 + ui-test-manifest | Compose rules for Android tests, and the activity they launch | both in `androidHostTest`; ui-test-junit4 also in Android host instrumented tests | `compose.ui.test.junit4`, `compose.ui.test.manifest` |
| AndroidX Test rules | `GrantPermissionRule` | instrumented tests | `androidx.rules` |
| WorkManager testing | WorkManager in test mode | background update module and Android host host tests | `androidx.work.testing` |
| Espresso, Activity | Version constraints only, for instrumented tests | Android host | `androidx.espresso.core`, `androidx.activity` |
| Bundled SQLite JVM natives | The native SQLite for Room in host tests | persistence build file, by coordinates at `versions.sqlite` | none |

Non-library entries:

| Entry | Holds | Read by |
|---|---|---|
| `versionCode`, `versionName` | App versions both apps share | Android host build file, iOS xcconfig task; see [versioning-and-release.md](versioning-and-release.md) |
| `compileSdk`, `minSdk`, `targetSdk`, `jvmTarget` | Android build settings | build files |
| `iosDeploymentTarget`, `xcode`, `xcodeGen`, `xcodeGenSha256` | iOS build settings and toolchain | iOS xcconfig task, CI |
| `swiftLint`, `swiftLintSha256*` | SwiftLint version and archive hashes | `iosApp/scripts/swiftlint.sh` |
| `robolectricSdk` | The SDK Robolectric emulates | root build |

The iOS version entries are governed by [versions.md](../rules/ios/versions.md).

## Catalog conventions

- `[versions]`, `[libraries]` and `[plugins]` are each split by comment headers into KMP, Android
  and iOS sections. An entry goes under the platform that owns it. Values both apps share, such as
  `versionCode` and `versionName`, are KMP.
- `[versions]` also has app-version sections (KMP, Android and iOS app versions) above the library
  versions.
- Each platform has a "Non maxed suppressed … versions" subsection. It holds versions kept below
  the newest on purpose, each with a comment saying why. Where the IDE flags the newer version,
  `#noinspection NewerVersionAvailable` silences it.
  Example: `composeMaterial3`, `composeRules` and `agp` in the catalog.
- Empty headers, such as the iOS plugins header, are kept.
- Entries are sorted alphabetically within a section.
- An entry whose version or presence is not self-evident carries a comment with the reason: what
  pins it, what reads it, what to update with it. Example: `composeUiTest`, `robolectricSdk`,
  `kotlin-test-junit`.
- Essenty has no version. Decompose and MVIKotlin both bring it in, and all its artifacts must stay
  on one release.
- A version no library references, read only through `libs.versions.<name>` or by a script,
  carries `#noinspection UnusedVersionCatalogEntry`. The IDE does not recognize those reads as a
  use.
- CI and the SwiftLint script read some entries with a line-based `sed`, as `name = "value"` on one
  line. Example: [ios-toolchain action](../../.github/actions/ios-toolchain/action.yml).
- A transitive artifact the project does not pin gets no entry of its own: OkHttp follows Ktor's
  engine. The decision is in [decisions.md](decisions.md).
- A reason that belongs to a dependency's use, not its version, is a comment at the
  `implementation(...)` line in the build file. Example:
  [androidApp build file](../../androidApp/build.gradle.kts) for `play.services.appset`.

## Adding a library

1. Check that neither the platform nor a library already in the table does the job; see
   [principles.md](principles.md).
2. Decide whether it is KMP, Android or iOS, and whether it is a library, a plugin or a version
   only.
3. Add the version under that platform's library-versions header, sorted, with a reason comment if
   it is not self-evident.
4. Add the library or plugin entry under the same platform's header in `[libraries]` or
   `[plugins]`, with `version.ref`.
5. Add it to the source set that uses it, as `implementation` unless public signatures name its
   types. Dependency placement is in [project-structure.md](project-structure.md), section
   "Dependency edges".
6. If the library is reached by name (reflection, serialization, Room, workers, manifest
   classes), run the minified check in [r8-minified.md](../rules/android/r8-minified.md).
7. If it touches `commonMain`, `iosMain` or a build file, run the iOS check in
   [build-check.md](../rules/ios/build-check.md).
8. Add a row to the table above, and an entry to [decisions.md](decisions.md) if it settles a
   choice between alternatives.
