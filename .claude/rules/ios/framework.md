---
paths:
  - "**/core-kmp/di-app/**"
  - "**/build.gradle.kts"
---

# The iOS framework

- One iOS framework exists, `Shared`. `:core-kmp:di-app` links it, since it sits above every other
  module.
- No other module declares a framework. A second one would bring a second copy of the Kotlin
  runtime and of the shared state.
- `core-kmp:di-app` applies the Compose plugins for the framework's sake. Compose copies the
  resources of the modules below into an app bundle through the module that links it.
- The Compose compiler is switched on for the native targets only in that module. On Android it
  would change the DI classes, and with them what R8 produces. Keep the restriction.
