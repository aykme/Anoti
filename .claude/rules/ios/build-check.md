---
paths:
  - "**/.claude/rules/ios/build-check.md"
  - "**/src/iosMain/**"
  - "**/src/commonMain/**"
  - "**/build.gradle.kts"
---

# Building for iOS off macOS

- Off macOS, as on the developer's Windows machine, Kotlin/Native compiles the iOS targets.
  Nothing is linked there and nothing runs there.
- Every link task reports `SKIPPED`, and the build still ends with `BUILD SUCCESSFUL`. Read the
  task's own line, never the last one.
- Run the local iOS check on every task that touches `commonMain`, `iosMain` or a build file. It is
  these five tasks, run from the root:

  ```
  ./gradlew compileKotlinIosArm64 compileKotlinIosSimulatorArm64 \
    compileTestKotlinIosSimulatorArm64 compileCommonMainKotlinMetadata \
    compileIosMainKotlinMetadata
  ```

- `compileCommonMainKotlinMetadata` compiles the shared code as metadata. It catches an error the
  IDE shows in `commonMain` and no target compile does.
- `DiAppComponent` in the `iosMain` of `core-kmp:di-app` is the iOS dependency graph. Its compiling
  is the proof that every iOS binding wires together.
