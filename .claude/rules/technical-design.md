---
paths:
  - "**/di/**"
  - "**/navigation/**"
  - "**/data/**"
  - "**/usecase/**"
  - "**/savedstate/**"
  - "**/composeResources/**"
  - "**/AndroidManifest.xml"
  - "**/*Worker.kt"
  - "**/settings.gradle.kts"
  - "**/build.gradle.kts"
  - "**/gradle/libs.versions.toml"
  - "**/PrivacyInfo.xcprivacy"
---

# Technical design by area

- Before writing or editing in one of the areas below, a test or a fake in it included, read its
  design file, even when the task's main area is another one. The whole design starts at
  [.claude/design/TECHNICAL-DESIGN.md](../design/TECHNICAL-DESIGN.md).

| Area | Design file |
|---|---|
| `di/` packages | [dependency-injection.md](../design/dependency-injection.md) |
| `navigation/` packages | [navigation.md](../design/navigation.md); when saved state is touched, also [state-restoration.md](../design/state-restoration.md) |
| `data/` and `usecase/` packages | [data-layer.md](../design/data-layer.md) |
| `savedstate/` packages | [state-restoration.md](../design/state-restoration.md) |
| `composeResources/` | [ui-compose.md](../design/ui-compose.md), section "Resources and localization" |
| `AndroidManifest.xml` | [module-anatomy.md](../design/module-anatomy.md), section "Manifests and resources"; a permission: also [security-and-privacy.md](../design/security-and-privacy.md), section "Permissions" |
| `*Worker.kt` | [platform-mirroring.md](../design/platform-mirroring.md) |
| a module's `build.gradle.kts`, `settings.gradle.kts` | [new-module.md](../design/new-module.md) |
| the root `build.gradle.kts` | [build-and-tooling.md](../design/build-and-tooling.md) |
| `gradle/libs.versions.toml` | [tech-stack.md](../design/tech-stack.md); an app or platform version: also [versioning-and-release.md](../design/versioning-and-release.md) |
| `PrivacyInfo.xcprivacy` | [security-and-privacy.md](../design/security-and-privacy.md), section "iOS privacy manifest" |
