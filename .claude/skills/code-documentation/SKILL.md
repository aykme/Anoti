---
name: code-documentation
description: Write or tighten module READMEs and KDoc for this project (KMP/Android multi-module). Covers both the short index-style README per module AND the KDoc on the entities it points to — the two are one job, not two. Use this whenever the user asks to create, write, add, or update a README for a module/package/library, document a module's public entities, write or review code comments/KDoc, or asks "is this documented" — even if they just say "add a README" or "document this module" without more detail. Also use it to review or shorten an existing module README that has grown too long, reads like an essay, lists every type it finds instead of only the major ones, or duplicates explanations that belong in the code's own doc comments. Call this after finishing any change to a module's public API (new module, new public entity, changed signature) — documentation is part of finishing the change, not a follow-up task.
---

# Code Documentation (READMEs + KDoc)

## Why this shape

A module README that re-explains every class's behavior turns into a second source of truth
that drifts from the first. Six months later the KDoc says one thing and the README says
another, and nobody notices until it's wrong in a way that costs someone time.

The fix: the README is an **index**, not documentation. It tells a reader what exists and
where to look. The actual explanation — what something does, what its parameters mean, what
its variants are — lives in the code itself (KDoc), right next to the thing it describes. It
can't silently drift there, and an IDE shows it on hover.

Practically, this means writing a module README is two tasks, not one:

1. Make sure the entities you're about to list actually have a short doc comment on them.
   If one is missing, add it — that's part of the job, not a follow-up.
2. Write the README itself as a thin index pointing at them.

Skipping straight to step 2 is how READMEs turn into essays — without a place for the real
explanation to live, everything worth saying ends up crammed into the README instead.

## File name

Name the file after the module's full Gradle path, not just `README.md`: uppercase, colons
replaced with dashes, `-README.md` on the end. `:core-kmp:celebrity` becomes
`CORE-KMP-CELEBRITY-README.md`; `:feature-kmp:anime-base` becomes
`FEATURE-KMP-ANIME-BASE-README.md`. Put it at the module's root, next to `build.gradle.kts`.
Only KMP modules (`core-kmp/*`, `feature-kmp/*`) get one — see `CLAUDE.md` for the full rule
and the root `README.md` exception.

## Before you write anything: confirm the entity list

Survey the module's public types, and apply "What counts as major" (below) to shortlist
candidates. Then show the user a table before touching any file — don't jump straight to
writing KDoc or the README. Getting the entity list wrong is the most common way this goes
sideways: too many entities, an impl class snuck in, a Store's internals listed separately.
It's cheap to check up front and expensive to unwind after the fact.

| Entity             | File                                 | Proposed one-liner                        | Notes                                 |
|--------------------|--------------------------------------|-------------------------------------------|---------------------------------------|
| SafeApi            | `.../api/data/SafeApi.kt`            | Safe API calls with retries.              | —                                     |
| CallResult         | `.../api/domain/model/CallResult.kt` | Outcome of a call made through `SafeApi`. | —                                     |
| SafeApiImpl        | `.../impl/data/SafeApiImpl.kt`       | —                                         | excluded: implementation of `SafeApi` |
| SHIKIMORI_BASE_URL | `.../api/domain/Const.kt`            | —                                         | excluded: constant                    |

List excluded candidates too, with the reason — not just what made the cut. That's what lets
the user catch a wrong call in either direction, not just rubber-stamp what you already picked.
Ask directly whether the list looks right or something should be added/dropped, and only start
adding doc comments or writing the README file once they confirm.

## What goes in the README

1. **One intro line** — what the module is for. Not a paragraph.
2. **Entities list** — one bullet per **major** entity a consumer would actually reach for and
   use on its own:
   `- [Name](path/to/File.kt) — one short line.`
   The line is a *label*, not a summary of behavior. "Safe API calls with retries" — not
   "runs a network call and returns its outcome as CallResult instead of throwing, retrying
   failures with an increasing delay between attempts". If the true description needs more
   than about ten words, that sentence belongs in the entity's own doc comment, and the
   README bullet just points there. See "What counts as major" below — most modules have
   fewer major entities than types.
3. **How to include it** — always present, two things:
    - The Gradle coordinate to depend on: `implementation(project(":core-kmp:celebrity"))`.
    - How an instance is actually obtained at runtime. If the project wires things through DI,
      say so and say where — e.g. "provided via the platform module's Dagger setup" — without
      re-explaining how DI works. If there's no DI (a plain constructor, a factory function),
      say that instead. A reader who found this module and wants to use it needs this; it's
      not optional the way the usage example is.
4. **Usage example** (optional, see "Code example vs. prose" below for when to actually write
   one) — a few lines showing how to obtain and drive the module's main entry point. One
   example for the primary entity is normally enough — don't write one per listed entity.
   Skip the section entirely if the entity list plus "how to include it" already make usage
   obvious, as in a pure data/model module.

That's the whole shape. A README for a handful of major entities is normally 15-30 lines. If
it's growing past that, something in it belongs in the code instead.

Do **not** add a "what's intentionally not here" / caveats section as a standard part of the
template. It sounds useful but in practice becomes a place to restate implementation detail
that already doesn't belong in the README. If there's a genuine caveat a reader would
otherwise get wrong, fold it into the intro line or the entity's own bullet instead — it
doesn't need a dedicated section.

### Code example vs. prose for "How to use it"

Don't default to a code block. Decide based on what kind of entry point the module exposes:

- **A plain call-and-get-a-result entry point** (an interface with one or two methods, a
  manager, a usecase, a factory function) — a short, real code snippet showing the call is
  usually the clearest option. Base it on an actual caller in the codebase (grep for where the
  type is used) rather than inventing plausible-looking code. A snippet that doesn't match how
  the type is really called is worse than no snippet, because it reads as authoritative.
- **A Store alone, or a Store + View + Controller trio** (any Store-shaped module — see
  "Store-shaped modules" below) — prefer a **short prose description over a code block**, in
  both variants, not just the View+Controller one. The wiring pattern is mechanically identical
  across every such module: subscribe to `states`/`labels`, call `accept(Intent)`, and — if a
  View/Controller exists — implement the view, dispatch intents from UI callbacks, render the
  model, construct the controller, call `onViewCreated`. A hand-written code block just repeats
  that same shape with different names — it reads as filler rather than help, and it's one more
  place that can silently drift from the real consumer if the Store's `Intent`/`Label`/`State`
  shape changes, exactly the "second source of truth" problem this whole skill exists to avoid.
  Say it in one or two sentences instead:

  > Subscribe to `XStore.states`/`labels` and call `accept(Intent)` to read and mutate
  > [what the store owns].

  or, with a View/Controller:

  > Implement `XView` (an `XViewImpl`): render `UiModel` in `render()` and call
  > `dispatch(Intent)` from the relevant UI callbacks. On the screen hosting it, construct
  > `XController` with the store(s) and the screen's lifecycle, then call
  > `controller.onViewCreated(viewImpl, viewLifecycle)`.

  If you're not sure which case you're in, this is itself decided by the entity list — see the
  refined "Store-shaped modules" rule below.

**Marking a platform-specific example.** If a code example demonstrates something that
currently only exists for one platform (e.g. only an Android implementation exists, no iOS
one yet), say so **as a comment inside the code block itself** —
`// Android example (no iOS example yet):` — not as separate prose bolted onto "How to
include it" or elsewhere. The note's only job is to stop a reader from assuming the snippet
compiles/works on every target; it has no reason to exist once there's no code for it to sit
next to. If you later remove the code example (e.g. switching to the prose form above), remove
the note with it — a leftover "this is an Android example" sentence with no code attached is
confusing, not informative.

### What counts as "major"

List an entity only if a consumer would import and use it **directly, on its own**. Do not
list:

- A type that only shows up as a parameter or field inside another entity you're already
  listing (a response model referenced only inside the service method that returns it, an enum
  only used as one field's type). Its own doc comment is enough; a reader lands on it by
  following the type from the entity that actually uses it.
- Constants and type aliases, as a rule. A `Const.kt` full of paging/animation/timing numbers
  or a file of `typealias`es is volatile (values and names change) and isn't something a
  consumer "uses" the way they use an interface — don't enumerate what's in it. If a module
  is unusual enough that this is worth a line at all, keep it to a bare pointer ("shared
  constants for the module") and nothing more specific.

A module can genuinely have one major entity and eight minor ones sitting behind it — that's
normal, not a sign you've under-documented. See "Store-shaped modules" below for the pattern
this shows up in most often, and its refinement for when a Store *isn't* alone.

## What does NOT go in the README

- **Subclasses/variants already listed in the parent's own doc comment.** If `CallResult`'s
  KDoc already documents its `Success`/`HttpError`/`NetworkError`/`OtherError` cases, the
  README does not repeat that list — it links to `CallResult` once and stops.
- **Implementation classes when a public contract exists** — an interface, a sealed type, or
  (see below) a Store. List `SafeApi`, not `SafeApiImpl`. This still applies when the
  "implementation" is an abstract base class meant to be subclassed (`CoroutineContextProviderBase`
  extending `CoroutineContextProvider`) — a reader depends on the interface; who extends it and
  how is an implementation detail, not something the README indexes. (If there's genuinely no
  interface at all — just a concrete class or function consumers use directly, like
  `createHttpClient`, or a `*Controller` with no interface that a screen constructs directly —
  list that instead. See "Store-shaped modules" for when a `Controller`/`View` counts.)
- **Test-only entities.** Fakes, test doubles, enums that only exist to drive a fake
  (`SafeApiFake`, `DesiredCallResult`, and similar). People writing tests against this module
  will find them in the test sources; they don't belong in the module's public index.
  Exception: if the module's whole purpose is to *be* a test utility, this rule doesn't apply
  — use judgment about the module's actual audience.
- **Implementation/configuration internals.** Which flags a factory function sets, what
  plugins it installs, the exact retry-backoff formula. That belongs in the function's own
  KDoc (a line or two), never in the README.
- **Design rationale, history, "why we didn't do X instead."** Worth capturing somewhere —
  a design doc, an ADR, a commit message — but not in a module README. A module README is a
  map, not an essay.

If you're unsure whether something crosses the line, read `references/before-after.md` — a
real before/after of the same README, with the specific things that got cut and why.

## Store-shaped modules (MVI, or similar single-orchestrator patterns)

Some modules are built around one orchestrator type that owns all the interaction surface —
an MVIKotlin `Store<Intent, State, Label>` is the common case in this codebase, but the same
shape shows up under other names elsewhere. When a module has one of these, it changes what
"major entity" means — but check which of the two variants below you're actually looking at
before deciding the Store is the *only* major entity. Getting this wrong (either direction) was
the single most common mistake made writing this project's READMEs, so verify against the DI
graph rather than assuming from the module's general shape.

**Variant A — Store alone.** Everything besides the Store is DI-wired and hidden: usecases,
the executor, the reducer, the store factory, the repository interface, domain models used
only in `State`/`Intent` payloads. No `View`/`Controller`-shaped type lives in this module, or
if one does, it's constructed *inside* the module and never reaches the consumer. Trace the DI
graph, and you'll typically find every internal type feeding into a single
`provide...Store(): XStore` function, with nothing else exposed to the component. Here, the
Store genuinely is the only major entity — `core-kmp:anime-database` is the reference example
(see `references/store-pattern.md`).

**Variant B — Store + View + Controller.** The module *also* defines a `View` interface (an
`MviView`-shaped contract) and/or a `Controller` class, and the platform/consumer layer
directly implements the `View` (`class SomeScreenViewImpl(...) : SomeScreenView`) and directly
constructs the `Controller` (`SomeScreenController(lifecycle, store, ...)` called straight in a
`Fragment`/`Activity`, not resolved through `@Provides`). In this variant, the Store, the View,
and the Controller are **all three major entities** — a real consumer reaches for all three,
not just the Store. This project's `bottom-navigation-bar`, `anime-favorites`, and `anime-list`
modules are all Variant B.

To tell which one you're in: grep the platform module(s) that consume this one for the
`View`'s and `Controller`'s names. If you find a `@Provides fun provide...(): XView`, DI hides
it, and it doesn't count on its own — but if you instead find a plain `class XViewImpl(...) :
XView` and/or `XController(...)` being constructed directly, that's the signal for Variant B.

For **Variant A**, follow the original shape:

- **Entities list**: just the Store, one line: `- [XStore](path) — <what it's the store for>.`
- **"How to include it"**: the Gradle coordinate plus how the Store instance itself is
  obtained.
- **"How to use it"**: prose, not a code block — see "Code example vs. prose" above.

For **Variant B**, extend it:

- **Entities list**: the Store, the View, and the Controller — one line each. E.g.
  `- [XController](path) — wires the store to its view and to [whatever else it binds].`
- **"How to include it"**: say how each of the three is obtained — the Store via DI, and
  (typically) that the View/Controller have no DI wiring and are constructed/implemented
  directly by the consumer.
- **"How to use it"**: prose, not a code block — see "Code example vs. prose" above.

For **both variants**, document `State`/`Intent`/`Label` on the Store interface itself, one
short KDoc line per case (see `references/store-pattern.md` for a full worked example). **The
same applies to `Action`/`Message`** (MVIKotlin's executor/reducer plumbing) — see the next
section; don't stop at a single class-level "this is internal" note.

### Documenting `Action`/`Message`: the class-level note is not enough

It's tempting to write one class-level comment — `/** Internal executor plumbing; a consumer
never dispatches this. */` — and consider `Action`/`Message` "documented." That note explains
*why* a reader can skip worrying about calling these, but it says nothing about what any
individual case actually *does*. A reader tracing the Store's internal data flow (which is a
real thing people do — debugging, onboarding, reviewing a PR that touches the reducer) still
needs that per-case explanation, exactly like `Intent`/`Label` get. This was missed once while
writing this project's newer stores and had to be fixed after review — treat it as a checklist
item, not a judgment call:

- Class-level KDoc on `Action`/`Message` itself: **yes** — note that they're internal.
- A KDoc comment on **every** `data class`/`data object` case inside them, same as
  `Intent`/`Label`: **also yes**, not optional.

A good, low-effort pattern for `Message` cases that mirror a `State` field: point at the field
being replaced and describe the parameter.

```kotlin
/**
 * Replaces [State.listItems] wholesale.
 *
 * @param listItems the full, up-to-date list of favorites items.
 */
data class UpdateListItems(val listItems: List<ListItemDomain>) : Message
```

This makes the connection between "what changed in the reducer" and "what it means for the
state" explicit without re-explaining the whole feature.

## Where the description actually lives

For each entity you're about to list, check its current doc comment:

- **Missing or absent** — add one. Keep it to 1-3 lines: what it's for, in the same "label"
  tone you'd want in the README bullet, expanded just enough to cover parameters and
  behavior that aren't obvious from the signature. Use `@param` for anything non-obvious —
  that's the right place for parameter explanations, not the README.
- **Already a wall of text** — trim it. A doc comment that re-explains implementation detail
  paragraph after paragraph is the same antipattern as a bloated README, just one file over.
  Interfaces describe the contract; implementations get a short comment for whatever's
  actually worth noting (a non-obvious classification rule, a gotcha), not a restatement of
  what the code already says.

Documenting stops at nothing less than the full chain: an entity's own doc comment, `@param`
on every one of its constructor parameters, and — for any parameter or property whose type is
itself a project-defined class/interface/enum/sealed-variant/typealias — that type's doc
comment too, recursively, until you hit stdlib/primitive types. Don't stop at the entity you
listed in the README; follow every custom type it exposes outward until the chain ends. A
sealed type's variants each get their own line the same way (see the `Intent`/`Message`
example above — every case has its own `@param`, not just a class-level one-liner).

This does not mean commenting every individual enum constant. Document the enum type itself;
give a specific constant its own comment only if its meaning would genuinely surprise a reader
— a self-descriptive name (`ONGOING`, `RELEASED`, even `UNKNOWN`) doesn't need one just because
you can technically write a sentence about it. Padding every case with a comment that just
restates the name is the same antipattern as an overlong README, one level down.

The same restraint applies one level up: **don't add a method-level comment that only restates
what the interface's own name already says.** If an interface has a single abstract method and
the interface name plus the method name together already say everything the comment would,
skip the method comment — keep the interface-level one-liner and stop.

```kotlin
// Bad — the comment adds nothing the name doesn't already say:
interface UpdateAllAnimeInBackgroundOnceUsecase {
    /** Schedules the update to run as soon as possible. */
    fun execute()
}

// Good — the interface name already carries the meaning:
/**
 * Triggers a one-off background update of the whole saved anime library.
 */
interface UpdateAllAnimeInBackgroundOnceUsecase {
    fun execute()
}
```

Short interface doc comment, this length is the target:

```kotlin
/**
 * Safe API calls with retries.
 */
interface SafeApi {
    /**
     * Runs [apiCall] and returns its outcome as [CallResult]. Retries retryable failures
     * (5xx, network errors) with an increasing delay between attempts.
     *
     * @param callAttempt current attempt number; managed internally, don't pass it explicitly.
     * @param apiCall the network call to run.
     */
    suspend fun <T> call(callAttempt: Int = 1, apiCall: suspend () -> T): CallResult<T>
}
```

## After writing: check for siblings you might have missed

When you finish documenting the module(s) the current task actually touched, take one more
look at their sibling KMP modules (same `core-kmp/`/`feature-kmp/` parent, modules worked on
around the same time). Module work often happens in batches, and it's easy for the last one or
two in a batch to slip through without a README. A quick pass —
`ls core-kmp/*/​*-README.md feature-kmp/*/​*-README.md` and diff against the actual module list
— catches this cheaply. Don't silently create the missing ones, though: surface what's missing
and let the user decide whether to include them in the current task or handle them separately,
same as any other scope decision.

## Reference examples

- `references/after-good.md` — a complete real README written this way: three entities (a
  retry wrapper, a result type, an HTTP client factory), how to include it, a short usage
  example, no essay.
- `references/before-after.md` — the same module's README *before* it was tightened, next to
  the final version, so you can see exactly what got cut and why: a prose section explaining
  `commonMain`/DI-framework-agnostic, the full `CallResult` subclass list repeated outside
  the class, the implementation class and test fake sitting in the entity list next to the
  interface, and `createHttpClient`'s internal config flags spelled out instead of linked.
- `references/store-pattern.md` — worked examples of both Store-shaped variants: Variant A
  (Store alone, `core-kmp:anime-database`) with the Store's own KDoc documenting
  `State`/`Intent`/`Label`/`Action`/`Message` down to the last case, and Variant B (Store +
  View + Controller, `feature-kmp:bottom-navigation-bar`) showing how the entity list and
  "how to use it" section differ.
