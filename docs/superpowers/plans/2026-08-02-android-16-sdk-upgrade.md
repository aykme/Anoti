# Android 16 (API 36) SDK Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Raise `compileSdk`/`targetSdk` from 35 to 36 (Google Play's current minimum requirement, enforced 2026-08-31), upgrading the minimum-required build tooling along the way, while keeping the app's runtime behavior on real devices byte-for-byte identical to today.

**Architecture:** All 25 Gradle modules read `compileSdk`/`targetSdk`/`minSdk`/`agp` from the single version catalog `gradle/libs.versions.toml` — no module hardcodes these values (verified by repo-wide grep). The upgrade is therefore: (1) bump the catalog + Gradle wrapper to the minimum versions that officially support API 36, (2) neutralize the one behavior-change that would otherwise visibly alter the app (large-screen orientation/resizability enforcement), (3) verify nothing else regresses via full build/test + targeted manual QA on both a phone-size and a tablet-size Android 16 emulator.

**Tech Stack:** Gradle 8.13, AGP 8.13.0, Kotlin 2.0.0 (unchanged), KSP 2.0.20-1.0.24 (unchanged), Android SDK Platform 36.1 (already installed locally at `F:\Programing\AndroidSDK\platforms\android-36.1`).

## Global Constraints

- Target exactly API **36**, not 37 — 36 is Google Play's current minimum and avoids a forced migration to AGP 9.x (which would force-upgrade Kotlin/KSP to ≥2.2.10).
- Do not bump Kotlin, KSP, or any other library version unless the build actually fails without it — per `developer.android.com/build/kotlin-support`, compileSdk and Kotlin version are independent axes, verified for this project's exact toolchain (AGP 8.13.0 supports Kotlin metadata up to 2.3, so 2.0.0 is well inside range).
- `minSdk` stays at 26 — out of scope, not affected by this change.
- No visible behavior change for end users on any currently supported device/screen size. This is the primary acceptance criterion — treat every finding below as a regression to prevent, not a feature to add.
- Every module resolves its SDK versions from `gradle/libs.versions.toml` exclusively — do not introduce a per-module override.

## Audit Findings (basis for the tasks below)

Full repo grep against the official "Behavior changes: apps targeting Android 16" checklist (`developer.android.com/about/versions/16/behavior-changes-16`, fetched and cross-checked 2026-08-02):

| Behavior change | Found in repo? | Risk | Action |
|---|---|---|---|
| Large-screen orientation/resizability ignored (sw≥600dp) | **Yes** — `main/src/main/java/com/alekseivinogradov/main/impl/presentation/MainActivity.kt:138` calls `setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)`, app has zero landscape/tablet layouts (`values-land`, `values-sw600dp` etc. only exist inside `build/` — pulled in transitively from AndroidX, not authored by this app) | **High** — on a tablet/foldable this lock would be silently dropped, and there is no landscape layout to fall back to | Task 3: add `PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY` |
| Edge-to-edge enforced, no opt-out | Yes, but already fully handled — `enableEdgeToEdge()` already called unconditionally for API≥29 in `MainActivity.kt:101-106`/`EdgeToEdge.kt`, no use of the now-defunct `windowOptOutEdgeToEdgeEnforcement` | Low — already targets this exact behavior | Task 5 manual QA only (visual confirmation) |
| Predictive back gesture / `onBackPressed()` no longer called | No overrides of `onBackPressed()`, `KEYCODE_BACK`, or manual `FragmentManager` back-stack handling anywhere in the repo — navigation is 100% via `NavHostFragment`/Navigation Component, which already integrates with `OnBackPressedDispatcher` | Low | Task 5 manual QA only (swipe-back gesture smoke test) |
| `elegantTextHeight` deprecated | No usage found | None | No action |
| `scheduleAtFixedRate()` skips missed executions | Not used — periodic work goes through `WorkManager` (`AnimeUpdateWorker.kt`), unaffected by this JDK-`Timer`-specific change | None | No action |
| `BODY_SENSORS` → granular health permissions | No usage found (not a fitness app) | None | No action |
| Bluetooth bond-loss/encryption intents | No Bluetooth usage found | None | No action |
| `MediaStore.getVersion()` lockdown | No `MediaStore` usage found | None | No action |
| Safer intents strict matching | Opt-in only (`intentMatchingFlags`), not present, not being added | None | No action |
| Local Network permission (phased opt-in) | No `NsdManager`/raw local-socket usage found; all network I/O goes through `core-platform:network`/`core-kmp:network` via Retrofit/OkHttp against public endpoints | None | No action |
| Photo picker pre-selection | No photo/video picker usage found | None | No action |
| 16 KB page size (Play requirement, not targetSdk-gated) | No `jniLibs/`, no `.so` anywhere in build output — pure Kotlin/JVM, no native code | None | No action |

## Tasks

### Task 1: Bump Gradle wrapper to 8.13

**Files:**
- Modify: `gradle/wrapper/gradle-wrapper.properties`
- Modify (auto-generated): `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `gradlew.bat`

**Interfaces:**
- Produces: a Gradle 8.13 wrapper, which Task 2's AGP 8.13.0 requires as its declared minimum.

- [ ] **Step 1: Run the wrapper upgrade task (updates properties + jar + scripts atomically)**

```bash
./gradlew wrapper --gradle-version 8.13 --distribution-type bin
```

- [ ] **Step 2: Verify the wrapper now reports 8.13**

```bash
./gradlew --version
```

Expected: output contains `Gradle 8.13`.

- [ ] **Step 3: Confirm the properties file was rewritten correctly**

Read `gradle/wrapper/gradle-wrapper.properties` and confirm:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
```

- [ ] **Step 4: Commit**

```bash
git add gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.jar gradlew gradlew.bat
git commit -m "Gradle wrapper was upgraded to 8.13"
```

---

### Task 2: Bump AGP and compileSdk/targetSdk in the version catalog

**Files:**
- Modify: `gradle/libs.versions.toml:3` (`compileSdk`), `gradle/libs.versions.toml:5` (`targetSdk`), `gradle/libs.versions.toml:11` (`agp`)
- Modify: `app/src/main/AndroidManifest.xml:16` (`tools:targetApi`)

**Interfaces:**
- Consumes: Task 1's Gradle 8.13 wrapper (AGP 8.13.0 requires it).
- Produces: `libs.versions.compileSdk` / `libs.versions.targetSdk` = `"36"`, consumed identically by all 25 modules' `build.gradle.kts` files (no other file needs to change — they all already read `libs.versions.compileSdk.get().toInt()` / `libs.versions.targetSdk.get().toInt()`).

- [ ] **Step 1: Edit the version catalog**

In `gradle/libs.versions.toml`, change:
```toml
compileSdk = "35"
minSdk = "26"
targetSdk = "35"
```
to:
```toml
compileSdk = "36"
minSdk = "26"
targetSdk = "36"
```
and change:
```toml
agp = "8.5.2"
```
to:
```toml
agp = "8.13.0"
```

- [ ] **Step 2: Update the manifest's `tools:targetApi` lint hint to match**

In `app/src/main/AndroidManifest.xml`, change:
```xml
        tools:targetApi="35">
```
to:
```xml
        tools:targetApi="36">
```

- [ ] **Step 3: Sync and build to confirm AGP 8.13.0 + compileSdk 36 resolve cleanly**

```bash
./gradlew clean build --stacktrace
```

Expected: `BUILD SUCCESSFUL`. If any module fails to compile because a *specific* library is incompatible with AGP 8.13.0/compileSdk 36 (not Kotlin/KSP — those are pre-verified compatible), read the exact error, identify the minimum version of that one library that resolves it, bump only that single `[versions]` entry in `gradle/libs.versions.toml`, and re-run this step. Do not preemptively bump any other dependency.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/src/main/AndroidManifest.xml
git commit -m "AGP was upgraded to 8.13.0 and compileSdk/targetSdk were raised to 36"
```

---

### Task 3: Preserve the portrait-lock/restricted-resizability behavior on large screens

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Consumes: Task 2's `targetSdk = 36`, which is what activates the large-screen enforcement this task neutralizes.
- Produces: identical windowing behavior to today on tablets/foldables (`sw≥600dp`), i.e. `MainActivity.kt:138`'s `setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)` keeps being honored by the OS.

- [ ] **Step 1: Add the compat property to the `<application>` tag**

In `app/src/main/AndroidManifest.xml`, inside `<application ...>` (after the closing `>` of the opening tag, before the existing `<provider>` block), add:
```xml
        <property
            android:name="android.window.PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY"
            android:value="true" />
```

Resulting `<application>` block:
```xml
    <application
        android:name="com.alekseivinogradov.app.impl.presentation.AnotiApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Anoti"
        tools:targetApi="36">

        <property
            android:name="android.window.PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY"
            android:value="true" />

        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge">
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                android:value="androidx.startup"
                tools:node="remove" />
        </provider>

    </application>
```

- [ ] **Step 2: Build to confirm the manifest merges cleanly**

```bash
./gradlew :app:assembleDebug --stacktrace
```

Expected: `BUILD SUCCESSFUL`, no manifest merger errors.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/AndroidManifest.xml
git commit -m "Restricted resizability was preserved on large screens after targetSdk 36 bump"
```

---

### Task 4: Full build and automated test verification across all 25 modules

**Files:** none (verification only)

**Interfaces:**
- Consumes: Tasks 1-3's fully upgraded toolchain.

- [ ] **Step 1: Full clean build of every module**

```bash
./gradlew clean build --stacktrace
```

Expected: `BUILD SUCCESSFUL`, all `:app`, `:main`, `:ui-core:*`, `:core-kmp:*`, `:core-platform:*`, `:feature-kmp:*`, `:feature-platform:*` modules compile (Android + iOS KMP targets).

- [ ] **Step 2: Run all unit tests**

```bash
./gradlew test
```

Expected: `BUILD SUCCESSFUL`, 0 failures. If any test fails, treat it as a real regression introduced by the SDK bump (e.g. a Robolectric config pinned to an old SDK) — investigate via `superpowers:systematic-debugging` before touching production code.

- [ ] **Step 3: Run Android instrumentation tests (requires a connected device/emulator)**

```bash
./gradlew connectedAndroidTest
```

Expected: `BUILD SUCCESSFUL`. Covers `app/src/androidTest/.../AnimeFavoritesUserFlowTest.kt` and other `androidTest` sources.

- [ ] **Step 4: Fix forward** — if Steps 1-3 reveal issues beyond what Tasks 1-3 anticipated, fix them in this task (do not silently skip failing checks), then re-run the failed step until green.

---

### Task 5: Manual QA — phone-size regression check (Android 16 emulator)

**Files:** none (manual verification only)

**Interfaces:**
- Consumes: a built, installable debug APK from Task 4.

- [ ] **Step 1: Create/launch an Android 16 (API 36) phone emulator** (e.g. Pixel 8 profile, sw < 600dp) via Android Studio Device Manager or `avdmanager`/`emulator` CLI using the already-installed `system-images;android-36...` if present, else install one first.

- [ ] **Step 2: Install and launch the app**

```bash
./gradlew :app:installDebug
```

- [ ] **Step 3: Visual/behavioral checklist (compare against current production build side-by-side if possible)**
  - App launches directly into portrait; rotating the physical/virtual device does **not** rotate the app (same as before Task 3).
  - Status bar and navigation bar are solid black, edge-to-edge content does not visually shift or double-pad against the bottom navigation bar (this is the exact regression called out in the code comment at `MainActivity.kt:113-117`).
  - Swiping back from the anime list / favorites / notification screens shows the standard predictive-back preview animation and lands on the correct previous screen — no crash, no stuck state.
  - The notification permission rationale dialog (`showNotificationsRationale()`) still triggers correctly on first launch.
  - Background anime-list update still runs (trigger `AnimeUpdateWorker` manually via `adb shell am broadcast` or wait for its schedule, confirm via logcat that it completes without SDK-related errors).

- [ ] **Step 4: Record findings.** Any visual/behavioral difference from the pre-upgrade build is a regression — fix the root cause before proceeding, then repeat Steps 2-3.

---

### Task 6: Manual QA — large-screen regression check (validates Task 3's fix directly)

**Files:** none (manual verification only)

**Interfaces:**
- Consumes: the same debug APK as Task 5.

- [ ] **Step 1: Create/launch an Android 16 (API 36) tablet emulator** with `sw ≥ 600dp` (e.g. "Pixel Tablet" AVD profile).

- [ ] **Step 2: Install and launch the app** (same command as Task 5 Step 2, different running AVD).

- [ ] **Step 3: Confirm the app behaves exactly like Task 5's phone run** — this is the direct test of Task 3's fix:
  - App is portrait-locked and non-resizable, not force-expanded to fill the tablet window or letterboxed/pillarboxed by the system in a way that differs from a phone.
  - No layout stretching, clipping, or overlap in bottom navigation, lists, or dialogs.

- [ ] **Step 4: If the app is NOT portrait-locked on this emulator**, `PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY` did not take effect — re-check Task 3's manifest placement (must be a direct child of `<application>`, correctly merged — inspect the merged manifest via `app/build/intermediates/merged_manifest/debug/AndroidManifest.xml` after a build) and fix before proceeding.

---

### Task 7: Final review

**Files:** none

- [ ] **Step 1: Review the full diff**

```bash
git diff main --stat
git log main..HEAD --oneline
```

- [ ] **Step 2: Confirm scope** — the diff should touch only: `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `gradlew.bat`, `gradle/libs.versions.toml`, `app/src/main/AndroidManifest.xml`. Any other file changed is out of scope for this plan and should be justified or reverted.

- [ ] **Step 3: Run `superpowers:requesting-code-review`** before merging, per project convention.
