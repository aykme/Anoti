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
[test-coverage.md](test-coverage.md) before writing tests; [android/tests.md](android/tests.md)
for `androidHostTest` and composables; [ios/tests.md](ios/tests.md) for `iosTest` and iOS code;
[tests-on-device.md](tests-on-device.md) before a test that runs on a device or simulator or
launches the real app.

## Structure

- Every test body has three sections marked `//Given`, `//When`, `//Then`, even a short test.
  This is the convention across the suites (e.g. `SafeApiImplTest`).
- `commonTest` is the default home for a test, and shared code is the priority when effort has
  to be split. Platform code is covered too: an Android implementation gets its tests in
  `androidHostTest`, an iOS one in `iosTest`. The pair of `AnimeDatabaseContinuityTest` classes in
  `core-kmp:anime-database` shows the shape.
- A module that is not multiplatform keeps its host tests in `src/test/kotlin`, and its
  instrumented tests in `src/androidTest/kotlin`.
- Every automated test runs on a debug build. A minified or Release build is built on CI only when
  a run asks for it, and no automated test runs on it; the manual regression walks it.
- A test must pass on its own, in any order. It never leans on what another test or an earlier try
  left behind; a shared setup goes into a preparation step every test runs.
- The code under test is the real thing, wiring included; the fakes start at what it reaches for.
  A test may build a real DI component, as long as everything handed to it is a handwritten fake:
  no real database, no network, no background work.
- Drive time and concurrency through the test infrastructure: `runTest` and its virtual clock,
  `advanceTimeBy`/`advanceUntilIdle`, and `UnconfinedTestDispatcher` or `StandardTestDispatcher`
  installed via `Dispatchers.setMain`. Threads too: a test dispatcher, never a real one.

## What to cover

- Cover what you write. The coverage floor is a safety net, never a finish line.
- Cover the main cases, the risky ones, the bottlenecks and the boundaries. Where concurrency is
  real, cover races and ordering as well. A test written only to move the number is worse than no
  test.
- Composables get tests too, judged by what they assert; their coverage number is ignored.

## Test doubles

- Where a library already mocks the thing being stubbed, use it rather than a handwritten double.
  Ktor's `MockEngine` for an HTTP client is the example. No general-purpose mocking framework is
  used: `commonTest` targets Kotlin/Native (iOS) alongside Android, and none of them run there.
  Everything a library does not cover is a handwritten fake.
- **A double is never duplicated.** One class per thing being faked, across the whole repository.
  Two copies are unacceptable even when their bodies differ; the one class takes on what both
  needed. A double used by more than one module lives in `commonMain` of the module that owns the
  type it stands in for, never in a test source set: those are invisible to other modules.
- **`Fake` goes at the end of the name, never at the front.** `SafeApi` is faked by `SafeApiFake`,
  not `FakeSafeApi`. This holds for every double, down to a `private` one nested in a single test
  class. A name saying what the double does still ends in `Fake`: `NoOpSourceFake`,
  `HangingSourceFake`, `RecordingUsecaseFake`.
- The suffix also keeps the class out of the coverage numbers: the Kover filter matches on it. A
  wrong name skews the measurement, not just the reading.
- Put the class in the package of the real type, with `fake` as the last segment:
  `impl/data/fake/SafeApiFake.kt` alongside `api/data/SafeApi.kt`. One double per file, named after
  it. A double `private` to one test class stays nested there and only follows the naming. It gets
  a file in a `fake` package the moment a second test needs it.
- A double used by exactly one module still follows the naming. It stays in that module's own test
  source set until a second module needs it.
