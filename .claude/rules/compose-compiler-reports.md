---
paths:
  - "**/.claude/rules/compose-compiler-reports.md"
---

# Compose compiler reports

- When a commit writes or changes Compose UI, run the Compose compiler reports over it:
  `./gradlew :androidApp:assembleDebug -PcomposeCompilerReports`. They land in
  `<module>/build/compose_compiler/`, and only modules that recompiled get fresh files. Read them
  for the entities being committed, not for the whole project:
    - every `restartable` composable must also be `skippable`;
    - no composable parameter is an `unstable` type you introduced;
    - UI models read `stable class`, not `runtime class`. A model that misses it only because of a
      generic (e.g. `ImmutableList<T>`) gets `@Immutable`. Annotate only when every property is a
      `val` that never changes after construction.
- Compose annotations stay out of domain types. A `@Stable` interface in `api/domain` leaks the UI
  layer into it. Leave it alone and note the report entry instead.
