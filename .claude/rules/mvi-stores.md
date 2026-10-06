---
paths:
  - "**/store/**/*.kt"
---

# MVI stores

- Executors are pure orchestration. Mutable data that reflects real application state (a flag, a
  counter, a "has this happened before" marker) lives in the Store's `State`, changed only through
  a `Message`/reducer. Never as a bare `private var` field on the Executor.
- A `private var` on an Executor is acceptable only for non-observable coroutine plumbing that
  isn't application state: a `Job` handle, a `MutableStateFlow` used to debounce/trigger work, a
  `Paginator` instance. Never for anything the reducer or UI would otherwise need to reason about.
- When a decision needs "has this already happened", derive it from an existing `State` field
  (e.g. a `contentType` still at its untouched default). Don't add a tracking property for that
  one case.
