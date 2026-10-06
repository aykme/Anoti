---
paths:
  - "**/.claude/rules/android/ci.md"
  - "**/.github/workflows/android.yml"
---

# Running Android on CI

When it runs, and how to start and follow a run, is in [../ci-github.md](../ci-github.md).

- `android.yml` runs the project's checks: the build, every host test, detekt and `koverVerify`.
- It never reaches the instrumented tests. They stay on the developer's emulators.
- Started with `-f minified=true`, it builds the minified app and nothing else. Its APK and
  `mapping.txt` stay on GitHub for seven days.
