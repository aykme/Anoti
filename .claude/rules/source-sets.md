---
paths:
  - "**/src/**/*.kt"
  - "**/src/**/*.java"
---

# Platform source sets (androidMain/iosMain)

- This is a KMP app. `commonMain` is the default location for everything. A platform source set
  (`androidMain`, `iosMain`) is only for code genuinely impossible to write in `commonMain`: a
  real platform API with no KMP equivalent (`Context`, `PendingIntent`, `NotificationChannel`,
  `WorkManager`, an actual `Activity`/`Application` subclass, and the like). It is not a retreat
  for when a KMP-compatible way wasn't worked out yet, nor a default at the first friction.
- Before adding or leaving anything in a platform source set, check whether it needs a
  platform-only type anywhere in its own signature or body. An interface, data holder, or plain
  function with zero platform imports belongs in `commonMain`, even if its only current
  implementer/caller is platform-specific.
- One exception, for iOS only. Code that exists only because iOS lacks a mechanism Android's OS
  provides lives in `iosMain`, even when it is portable. `IosRootHolder`, `IosRootContent`,
  `SaveableStateCodec` and `ChildLifecycle` in `main` are the case. They keep the screen state
  Android keeps in its saved instance state, and rebuild the root from it. Such code is written in
  `commonMain` first and moved once its tests pass (see [ios/tests.md](ios/tests.md)). Logic both
  platforms share stays in `commonMain`, whoever calls it.
- This applies to Compose code too. A composable only needs `androidMain`/`iosMain` if it directly
  touches a platform-only API (e.g. a `View`/`ComposeView` bridge). A composable built entirely
  from `compose.runtime`/`compose.foundation`/`compose.material3` and other `commonMain` types
  belongs in `commonMain`, regardless of which platform currently calls it.
- When portable logic needs a platform-specific value or condition (e.g. an Android-only
  OS-version check), compute it in the platform layer and pass the *result* in as a plain
  parameter (a `Boolean`, a `Modifier`, a `Dp`). Don't let the platform concept itself (its name,
  its reasoning) leak into the `commonMain` signature.
- Where both platforms need the same thing built (wording, an id, a format), build it once in
  `commonMain` and have both call it. Two copies drift, and review is not what should hold them
  together. `newEpisodeNotificationText` in `feature-kmp:anime-notification` is that shape.
- Re-verify this placement whenever a task removes or restructures platform-specific code (e.g. a
  `Fragment`→Compose migration). Code that was platform-only because of something now deleted (a
  `Fragment`, a `View`) often has no remaining reason to stay there. Move it to `commonMain` in
  that same task.
- Source directories are `kotlin`, never `java`, in any source set (`src/main/kotlin`,
  `src/test/kotlin`, `src/androidTest/kotlin`).
