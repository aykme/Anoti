# Testing

How tests are written in this project: which kind of test proves what and where it lives, the
shape of a test body, test doubles, DI components in tests, and virtual time. What a task must
cover, and when tests run, stay in the rules this file links.

Read when: writing or changing a test, a test double or a shared test utility; deciding which
source set a test belongs in.

## Test map

| Kind | Source set | Proves | Rule |
|---|---|---|---|
| Common test | `commonTest` | Shared logic: stores, executors, sources, usecases, screen components with real DI | [tests.md](../rules/tests.md), [test-coverage.md](../rules/test-coverage.md) |
| Host test | `androidHostTest`; `src/test/kotlin` in a non-KMP module | Android implementations and composables, on the JVM under Robolectric | [android/tests.md](../rules/android/tests.md) |
| Device test | `androidDeviceTest`; `src/androidTest/kotlin` in a non-KMP module | What a host test cannot reach, on an emulator | [android/instrumented-tests.md](../rules/android/instrumented-tests.md), [tests-on-device.md](../rules/tests-on-device.md) |
| iOS test | `iosTest` | What only a real iOS runtime can answer | [ios/tests.md](../rules/ios/tests.md) |
| iOS UI test | `iosApp/iosAppUITests` | The real app on a simulator | [tests-on-device.md](../rules/tests-on-device.md), [ios/ci.md](../rules/ios/ci.md) |
| Restore and theme checks | `.github/scripts/ios-restore-checks.sh` | Saved screen state and the theme on the installed iOS app, on CI | [tests-on-device.md](../rules/tests-on-device.md), [ios/ci.md](../rules/ios/ci.md) |
| Manual regression | `*-REGRESS.md` | Only what cannot be checked from the code | [module-docs.md](../rules/module-docs.md) |

- How each platform's test source sets are set up and what they may do is in
  [.claude/design/testing-platforms.md](testing-platforms.md).
- What a task must cover is in [tests.md](../rules/tests.md#what-to-cover), and how coverage is
  measured is in [test-coverage.md](../rules/test-coverage.md).
- Which build tests run on is in [tests.md](../rules/tests.md).

## Structure

- Every test body has three sections marked `//Given`, `//When`, `//Then`, even a short test.
  This is the convention across the suites. Where the action is the assertion, as with
  `assertFailsWith`, the last two merge into `//When / Then`. Example:
  [SafeApiImplTest](../../core-kmp/network/src/commonTest/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/SafeApiImplTest.kt).
- `commonTest` is the default home for a test, and shared code is the priority when effort has
  to be split. Platform code gets tests of its own as [tests.md](../rules/tests.md) says. The pair
  of continuity test classes in the persistence core module shows the shape. Example:
  [AnimeDatabaseContinuityTest in androidHostTest](../../core-kmp/anime-database/src/androidHostTest/kotlin/com/alekseivinogradov/anoti/animedatabase/android/impl/data/AnimeDatabaseContinuityTest.kt).
  Example:
  [AnimeDatabaseContinuityTest in iosTest](../../core-kmp/anime-database/src/iosTest/kotlin/com/alekseivinogradov/anoti/animedatabase/ios/impl/data/AnimeDatabaseContinuityTest.kt).
- A module that is not multiplatform keeps its host tests in `src/test/kotlin`, and its
  instrumented tests in `src/androidTest/kotlin`.
- A test must pass on its own, in any order. It never leans on what another test or an earlier try
  left behind; a shared setup goes into a preparation step every test runs.
- The code under test is the real thing, wiring included; the fakes start at what it reaches for.
  A test may build a real DI component, as long as nothing handed to it reaches a real database,
  the network or background work: handwritten fakes, `MockEngine`, and library objects such as
  `DefaultStoreFactory`.
- Drive time and concurrency through the test infrastructure: `runTest` and its virtual clock,
  `advanceTimeBy`/`advanceUntilIdle`, and `UnconfinedTestDispatcher` or `StandardTestDispatcher`
  installed via `Dispatchers.setMain`. Threads too: a test dispatcher, never a real one.

## Test doubles

- Where a library already mocks the thing being stubbed, use it rather than a handwritten double.
  Ktor's `MockEngine` for an HTTP client is the example. No general-purpose mocking framework is
  used; why is in [decisions.md "Deliberate absences"](decisions.md#deliberate-absences).
  Everything a library does not cover is a handwritten fake.
- **A double is never duplicated.** One class per thing being faked, across the whole repository.
  Two copies are unacceptable even when their bodies differ; the one class takes on what both
  needed. A double used by more than one module lives in a production source set of the module
  that owns the type it stands in for: `commonMain`, or the platform source set when that type
  exists only there. Never in a test source set: those are invisible to other modules.
- As found: one test class nests a second context provider double. Example:
  [AnimeListExecutorImplTest](../../feature-kmp/anime-list/src/commonTest/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/domain/store/main/AnimeListExecutorImplTest.kt).
- **`Fake` goes at the end of the name, never at the front.** `SafeApi` is faked by `SafeApiFake`,
  not `FakeSafeApi`. This holds for every double, down to a `private` one nested in a single test
  class. A name saying what the double does still ends in `Fake`: `DoNothingWorkerFactoryFake`,
  `SubscriptionsLifecycleFake`.
- The suffix also keeps the class out of the coverage numbers: the Kover filter matches on it. A
  wrong name skews the measurement, not just the reading.
- Put the class in the `impl` counterpart of the real type's package, with `fake` as the last
  segment: `impl/data/fake/SafeApiFake.kt` alongside `api/data/SafeApi.kt`. One double per file,
  named after it. A double `private` to one test class stays nested there and only follows the
  naming. It gets a file in a `fake` package the moment a second test needs it.
- A library type has no package of ours. Its double takes the package of the code that uses the
  type, with `fake` as the last segment, in the using module.
- As found: the entry module's root dependencies fake and host application fake sit outside a
  `fake` package. Example:
  [DiRootDependenciesFake](../../main/src/commonTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/DiRootDependenciesFake.kt).
- A double used by exactly one module still follows the naming. It stays in that module's own test
  source set until a second module needs it.

Examples of the placement:

- A shared double in a production source set. Example:
  [SafeApiFake](../../core-kmp/network/src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/fake/SafeApiFake.kt)
  beside
  [SafeApi](../../core-kmp/network/src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/data/SafeApi.kt).
- A shared double whose constructor parameters let a test hand in its own dispatchers. Example:
  [CoroutineContextProviderFake](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/domain/coroutinecontext/fake/CoroutineContextProviderFake.kt).
- A shared double of a type only one platform has, in that platform's source set. Example:
  [AnimeNotificationIntentProviderFake](../../feature-kmp/anime-notification-external/src/androidMain/kotlin/com/alekseivinogradov/anoti/animenotification/external/android/impl/presentation/provider/fake/AnimeNotificationIntentProviderFake.kt).
- Helpers that several modules' tests use, and that are not doubles, live in the test utilities
  core module `core-kmp:test-utils`. Example:
  [safeComposeInteraction](../../core-kmp/test-utils/src/commonMain/kotlin/com/alekseivinogradov/anoti/testutils/kmp/api/compose/SafeComposeInteraction.kt).

## DI components in tests

- A test builds the feature graph through its real component creator. It hands the creator a fake
  of the dependencies contract, private to the test class when only that test needs it.
- That fake supplies a real `DefaultStoreFactory`, shared doubles such as `SafeApiFake`, and an
  `HttpClient` over `MockEngine` where a real service is wired. The screen component then runs
  with every binding of the module. Example:
  [NavAnimeListScreenComponentTest](../../feature-kmp/anime-list/src/commonTest/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/navigation/NavAnimeListScreenComponentTest.kt).
- The root graph is built the same way, from one fake of its dependencies contract that the entry
  module's tests share. Example:
  [DiRootDependenciesFake](../../main/src/commonTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/DiRootDependenciesFake.kt).
- The DI bindings are measured like any other code; see
  [test-coverage.md](../rules/test-coverage.md).

## Virtual time

- A test class installs its test dispatcher with `Dispatchers.setMain` before each test and calls
  `Dispatchers.resetMain` after it. The teardown also disposes the stores and destroys the
  lifecycles the test created. Example:
  [SearchSectionExecutorImplTest](../../feature-kmp/anime-list/src/commonTest/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/domain/store/searchsection/SearchSectionExecutorImplTest.kt)
  (stores),
  [RootHostTest](../../main/src/commonTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHostTest.kt)
  (lifecycles).
- A debounce or a delay is crossed with `advanceTimeBy` on the virtual clock, never by waiting.
- `MockEngine` answers on `Dispatchers.IO` unless told otherwise. That takes the answer off the
  virtual clock and onto a second thread. Each engine is given the test's dispatcher through
  `MockEngineConfig.dispatcher`. Example:
  [AnimeBackgroundUpdateSourceImplTest](../../feature-kmp/anime-background-update/src/commonTest/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/impl/data/source/AnimeBackgroundUpdateSourceImplTest.kt).
- Coroutine contexts the code reads from its provider come from `CoroutineContextProviderFake`,
  given the test dispatcher where the work must stay on the virtual clock.
