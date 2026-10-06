---
paths:
  - "**/iosApp/project.yml"
  - "**/iosApp/iosApp.xcodeproj/**"
  - "**/iosApp/scripts/compile-kotlin-framework.sh"
  - "**/iosApp/iosApp/**"
  - "**/iosApp/iosAppUITests/**"
---

# The Xcode project

- `iosApp/project.yml` is the source of the Xcode project. XcodeGen generates
  `iosApp/iosApp.xcodeproj` from it, and the generated project is committed, so the app opens in
  Xcode with nothing else to run.
- Never edit the project in Xcode or by hand. Change `project.yml`, and commit it together with the
  project the macOS runner generated from it: `ios.yml` uploads it as the `xcodeproj` artifact.
- After a change to `project.yml`, take the project the run generated with
  `gh run download <run id> -n xcodeproj -D iosApp/iosApp.xcodeproj`. The artifact holds the
  folder's contents, so the folder is named in `-D`.
- The project lists every Swift file by name, so adding or renaming one changes what `project.yml`
  generates. The `drift` job of `ios.yml` fails when the committed project differs from what
  `project.yml` generates.
- The Xcode build phase runs `iosApp/scripts/compile-kotlin-framework.sh`. It finds a Java on its
  own, since Xcode starts it without the user's shell environment. The iOS build needs no Android
  SDK.
- After a change to `compile-kotlin-framework.sh`, add `-f setup_check=true` to the `ios.yml` run:
  that job proves the script finds a Java on a Mac without one in `JAVA_HOME`.
