# Principles

The ideas the rest of the design follows, each with its reason and the design files that apply
it. A principle explains why the code looks the way it does; the linked files say how to write
it. When a new case fits no existing pattern, these decide it.

Read when: a task has no pattern to copy, or two patterns pull in different directions; before
proposing a new kind of class, module or mechanism; when reviewing a design or a spec.

## KMP first

Shared code is the default, and a platform source set is the exception that needs a reason. Code
in `commonMain` is written, tested and measured once for both apps. The guard itself is a rule:
[source-sets.md](../rules/source-sets.md). Where code goes, and the one iOS exception, is in
[kmp.md "Where code lives"](kmp.md#where-code-lives).

## Mirror Android and iOS

What both platforms need is built in the same shape on both: the same class name, the same
package and the same order of steps. A reader who knows one platform then finds the other without
a map. Every difference is a choice and is written down. Applied in
[platform-mirroring.md](platform-mirroring.md) and in the platform components of
[dependency-injection.md](dependency-injection.md). Example:
[build.gradle.kts](../../core-kmp/di-app/build.gradle.kts)

## The platform owns lifecycle and saved state

Each OS already keeps a screen's lifecycle and state its own documented way. The project hands
that work to the platform and shares only what sits on top. Shared code receives a ready context
from the screen host. Applied in [state-restoration.md](state-restoration.md) and
[concurrency-and-lifecycle.md](concurrency-and-lifecycle.md). The choice itself is in
[decisions.md "Platform-owned root state"](decisions.md#platform-owned-root-state).

## No reinventing

Before building a mechanism, look at what the platform, the libraries and their official samples
already do, and copy the recommended pattern. Own code is written only for a gap nothing covers,
and that gap is named. Self-made machinery is hard to read and duplicates work the platform does
better. Named gaps today: paging that fits MVI, and saving screen state on iOS. Example:
[Paginator](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/paging/Paginator.kt)
Library-first also covers tests: [testing.md "Test doubles"](testing.md#test-doubles).

## Fail fast inside, tolerate untrusted input

A broken invariant in the app's own code crashes at once instead of hiding behind a default. A
value set once and read later is a `lateinit var`, so a read too early fails loudly. Input from
outside the app is different: reading a payload any app can send does not throw, and a bad one
reads as `null`. Applied in [error-handling.md](error-handling.md) and
[security-and-privacy.md](security-and-privacy.md).

- Example:
  [IosApp](../../core-kmp/di-app/src/iosMain/kotlin/com/alekseivinogradov/anoti/di/kmp/IosApp.kt)
- Example:
  [NavRootDeepLink](../../core-kmp/navigation/src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/NavRootDeepLink.kt)

## Persisted data has one owner

Each piece of persisted data belongs to one module, and everything else reads it through that
module. Stores that show it converge on the same source, so a write from one reaches all of them.
No copy needs syncing. Applied in [data-layer.md "Persistence"](data-layer.md#persistence) and
[decisions.md "Unscoped store bindings"](decisions.md#unscoped-store-bindings).

## The creator disposes

Whoever creates a store, a scope or a subscription also closes it, and nothing else does. Every
object then has one obvious place where its life ends. Applied in
[concurrency-and-lifecycle.md](concurrency-and-lifecycle.md) and [mvi.md](mvi.md). Example:
[RootHost](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHost.kt)

## Contracts in api, implementations in impl

An `api` package holds what outside code programs against. Everything else, composables and
implementations included, sits in `impl`. The contract stays small, and an implementation changes
without touching its consumers. The exact criterion is in
[module-anatomy.md "Packages"](module-anatomy.md#packages).

## Small modules, explicit dependencies

A module does one job and declares what it uses. A dependency is `api(...)` only when a public
signature exposes its types, and a comment next to it says which. Every other dependency stays
`implementation`, off the consumers' compile classpath. Applied in
[new-module.md](new-module.md) and [project-structure.md](project-structure.md). Example:
[build.gradle.kts](../../core-kmp/di-app/build.gradle.kts)

## Minimal classes with obvious purpose

A new class earns its place: its name says what it is, and its purpose is clear without a tour.
Fewer, plainer classes are easier to review than layers of helpers. The kinds the project already
has are in [entities.md](entities.md); its conventions are in [code-style.md](code-style.md).

## Shared values live in the closest common dependency

A constant, a string or a piece of wording that two modules need is built once, in the nearest
module both already depend on. Two copies drift apart, and nothing but review would notice.
Applied in [ui-compose.md "Design tokens"](ui-compose.md#design-tokens) and
[kmp.md "Where code lives"](kmp.md#where-code-lives).
