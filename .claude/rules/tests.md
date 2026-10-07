---
paths:
  - "**/src/*Test/**"
  - "**/src/test/**"
  - "**/fake/**"
  - "**/core-kmp/test-utils/**"
  - "**/iosApp/iosAppUITests/**"
---

# Tests

Read before writing or changing a test or a test double. Also read, each time it applies:

- [.claude/design/testing.md](../design/testing.md), sections "Structure" and "Test doubles",
  before writing or changing a test or a test double: how a test is built here, and how a double
  is named and where it lives;
- [test-coverage.md](test-coverage.md) before writing tests;
- [android/tests.md](android/tests.md) for `androidHostTest` and composables;
- [ios/tests.md](ios/tests.md) for `iosTest` and iOS code;
- [tests-on-device.md](tests-on-device.md) before a test that runs on a device or simulator, or
  launches the real app.

## What to cover

- Cover what you write. The coverage floor is a safety net, never a finish line.
- Cover the main cases, the risky ones, the bottlenecks and the boundaries. Where concurrency is
  real, cover races and ordering as well. A test written only to move the number is worse than no
  test.
- Platform code is covered too: an Android implementation gets its tests in `androidHostTest`, an
  iOS one in `iosTest`.
- Composables get tests too, judged by what they assert; their coverage number is ignored.

## Which build

- Every automated test runs on a debug build. A minified or Release build is built on CI only when
  a run asks for it, and no automated test runs on it; the manual regression walks it.
