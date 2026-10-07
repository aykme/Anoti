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
- Before creating a file in a platform source set or adding code to one, adding a composable,
  passing a platform-specific value into common code, building the same thing for both platforms,
  or moving code between source sets, read [.claude/design/kmp.md](../design/kmp.md), section
  "Where code lives". It holds the iOS-only exception, Compose code, platform values passed in as
  results, and building a shared thing once.
- Before creating a source directory, read
  [.claude/design/module-anatomy.md](../design/module-anatomy.md), section "Source sets".
- Code kept in `iosMain` under that iOS-only exception is written in `commonMain` first and moved
  once its tests pass (see [ios/tests.md](ios/tests.md)).
- Re-verify this placement whenever a task removes or restructures platform-specific code (e.g. a
  `Fragment`→Compose migration). Code that was platform-only because of something now deleted (a
  `Fragment`, a `View`) often has no remaining reason to stay there. Then it should move to
  `commonMain` as part of that same task, not be left behind.
