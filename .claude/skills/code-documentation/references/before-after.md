# Before/after: the same module's README

Real example from a KMP network layer (`SafeApi` retry wrapper, `CallResult` result type,
`createHttpClient` factory). Same module, same three major entities — one version is 77 lines
and reads like a design doc, the other reads like an index.

## Before (77 lines) — what's wrong with it

See `before-bad.md` for the full text. Line by line, what got cut and why:

- **Lines 4-10, "Why this module exists"** — a prose section explaining that the module is
  "fully in `commonMain`" and "DI-framework-agnostic". Both true, neither useful: KMP code
  living in `commonMain` is the default, not something worth a paragraph, and
  "DI-framework-agnostic" is jargon that doesn't actually tell the reader what to do with the
  module. Cut entirely — a one-line intro sentence covers the module's purpose.
- **Lines 22-24, the `CallResult` bullet** — re-lists `HttpError`/`NetworkError`/`OtherError`
  and explains the `Failure` marker. All of that is already `CallResult`'s own KDoc. Repeating
  it means two places to keep in sync, and the second one (the README) is the one nobody
  remembers to update. Cut down to one line; the class doc is the source of truth.
- **Lines 25-26, the `SafeApiImpl` bullet** — lists the implementation class right next to the
  `SafeApi` interface it implements. A caller depends on `SafeApi`; nobody consuming this
  module needs to know the impl class's name up front. Removed from the index entirely.
- **Lines 27-28, the `SafeApiFake` bullet** — a test double, listed as if it were a
  production entity. Removed — it's for people writing tests, who'll find it in the test
  sources.
- **Lines 30-32, the `createHttpClient` bullet** — spells out that it configures JSON content
  negotiation and `expectSuccess = true`. That's the function's own implementation, described
  in its own KDoc; the README bullet only needs to say what the function is *for*.
- **Lines 33-34, the `DesiredCallResult` bullet** — same problem as `SafeApiFake`: a
  test-only enum listed as a first-class module entity. Removed.
- **Lines 35-36, the `SHIKIMORI_BASE_URL` bullet** — a constant, listed as if it were an entity
  a consumer reaches for on its own. Removed — see "What counts as major" in `SKILL.md`.
- **Lines 64-69, "Note on `SafeApiImpl`'s classification"** — a whole section explaining an
  implementation nuance (which exception types get recognized) that belongs in
  `SafeApiImpl`'s own KDoc, not the README.
- **Lines 71-77, "What's intentionally not here"** — a caveats section that, once everything
  above is already cut, has nothing left to earn its keep. Dropped as a standard section; see
  `SKILL.md`.

What survived: the intro's first sentence, the entity *links* themselves (trimmed to one line
each), and the usage example. A "How to include it" section was added — the before version
never said how to actually depend on the module, which is a bigger miss than anything it was
over-explaining.

## After

See `after-good.md` for the full text — this is the target shape: one intro line, an entity
list with one label-length line each, how to include the module, and a short usage example.
Nothing in it needs a second doc to explain what it means.
