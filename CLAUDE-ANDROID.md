# CLAUDE-ANDROID.md

The rules for the Android side of the project. They are part of [CLAUDE.md](CLAUDE.md), which
holds the rules both platforms share and loads this file.

The finishing checks below apply to every task, not only to one that touched Android code.

## Tests on Android

- Composables get tests too, but not from `commonTest`: `runComposeUiTest` compiles there and then
  fails at runtime on the Android host test. They belong in `androidHostTest`, driven by
  Robolectric and `androidx.compose.ui.test.junit4.v2.createComposeRule` — the non-`v2` rule is
  deprecated, and v2 defaults to `StandardTestDispatcher`, so coroutines need the scheduler
  advanced. The module needs `robolectric` and `compose-ui-test-junit4` in that source set, both
  already in the version catalog, plus
  `withHostTestBuilder {}.configure { isIncludeAndroidResources = true }` — without the merged
  resources the rendered screens find neither their theme nor their Compose resources. Add
  `compose-ui-test-manifest` only where the rule has to launch its own host activity; a test that
  launches the module's own activity does not need it.
- No test ever boots the real app. A host test stays on the JVM with Robolectric standing in for
  the framework, and never uses the app's own `Application` — it supplies a stub of its own. The
  one exception is the `androidApp` module, where that `Application` is the subject. Robolectric
  creates it there and the test drives it directly, with every background service it reaches
  still faked.
- An instrumented test on a device is the furthest a test may go, and only where a host test
  genuinely cannot reach. It is never the first tool reached for.
- The SDK Robolectric emulates is set for the whole project, from `robolectricSdk` in the version
  catalog: the root build writes it into a `robolectric.properties` on each module's host-test
  classpath. Don't put `@Config(sdk = ...)` on a test — it belongs there only when that one class
  genuinely needs a different level, and then it says why. Name the level through the generated
  `MIN_SDK` where that is the one it needs; the root build writes that constant from the catalog
  too, since an annotation cannot read one. Left to itself Robolectric targets
  `compileSdk` and dies inside `ApplicationSharedMemory.create`, which it cannot emulate; the
  message it prints blames the JRE rather than the SDK level.

## Instrumented tests and the emulator

- The instrumented tests are part of running every test, on every task and not only on one
  that touched the UI. They need a device, so
  `./gradlew allTests :androidApp:testDebugUnitTest` never reaches them —
  `./gradlew :androidApp:connectedDebugAndroidTest` is what runs them. They drive the app
  against the live backend, so the emulator needs a connection. Report their result with the
  rest. The one exception is a task that changes no logic, described in "Finishing a task" in
  `CLAUDE.md`.
- Every check that needs the app running belongs on an emulator. A physical device attached for
  development is the developer's own and is not a test bench. An emulator also allows what a
  phone refuses — `adb root`, forcing an orientation, and picking the API level a branch needs.
- When running UI (instrumented/`androidTest`) tests, always do a clean installation of the app
  first — uninstall it from the emulator before installing and running, so a stale build
  doesn't mask a failure or fake a pass.

## Proving a task that changes no logic

- Build the minified variant before the change and after it. Compare `mapping.txt` and the
  entries of the two APKs. `unzip -v` lists every entry with its checksum.
- `mapping.txt` must match, apart from the renamed names and shifted source line numbers.
- Every APK entry must keep its checksum, apart from the ones the change is known to touch.
- An edit that only shifts line numbers still rewrites `classes.dex` and the profile files
  under `assets/dexopt/`. Those are expected to differ then; nothing else is.
- `META-INF/version-control-info.textproto` is left out of the comparison. It holds the commit
  hash, so it differs on every commit.
- The APK size is not compared. That one file compresses to a different length from one commit
  to the next, so the size can differ with nothing changed and can match by luck.
- Remove the module's build directory before each of the two builds. An incremental APK is not
  byte-stable.

## R8 and the minified build

- `release` is shrunk and obfuscated: `isMinifyEnabled` and `isShrinkResources` are both on for
  it, over `proguard-android-optimize.txt` plus `androidApp/proguard-rules.pro`.
- It carries no signing config, so what actually goes on a device is `minified` — `initWith`
  release plus the debug key, and identical to it in everything R8 does. Build it with
  `./gradlew :androidApp:assembleMinified`; the two variants' `mapping.txt` files match byte for
  byte.
- `isDebuggable` must stay off on both. AGP runs R8 in debug mode for a debuggable variant, which
  silently skips obfuscation — the part of R8 most likely to break something. Measured on the
  same variant: debuggable gave 0 renames and 18 170 429 bytes, non-debuggable 698 renames and
  9 664 344 bytes.
- Run this whenever the change touches anything reached by name: reflection, `Class.forName`,
  kotlinx.serialization, Room entities and DAOs, WorkManager workers, or a class the manifest
  names. A change that touches none of those does not need the pass.
- Look for a library's own rules before writing any. An AAR carries `proguard.txt` or
  `consumer-rules.pro` inside it, and AGP merges those automatically. Everything actually
  applied, and where it came from, is listed in
  `androidApp/build/outputs/mapping/minified/configuration.txt`. Only add a rule to
  `androidApp/proguard-rules.pro` once that file shows nobody supplied it.
- Read the other artifacts next to it. `missing_rules.txt` appears only when something needed a
  keep rule. `seeds.txt` lists what was kept and why. `usage.txt` lists what was stripped.
  `mapping.txt` shows what was renamed — check there that the names that must survive did.
- A successful build proves nothing on its own. Install the minified APK on an emulator, clean,
  and walk the flows the change touches. Confirm they really ran, that the log holds no
  `ClassNotFoundException` or `NoSuchMethodException`, and that no screen fell back to an empty
  or error state the unminified build doesn't show.
- `androidApp/proguard-rules.pro` keeps `SourceFile` and `LineNumberTable` and renames the source
  file to a constant, so an obfuscated stack trace stays decodable through `mapping.txt` with
  retrace while leaking nothing.
