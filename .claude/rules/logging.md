---
paths:
  - "**/*.{kt,swift}"
  - "**/.github/scripts/ios-restore-checks.sh"
---

# Logging

- The app logs with `println` and nothing else: no `Log`, no `NSLog`, no logging library or
  wrapper of its own. Swift never logs; SwiftLint bans `print`.
- Every line reads `println("$ANOTI_TAG <Place>: <what happens>")`. `<Place>` is a fixed string,
  written inline or as a file-level `TAG` constant. It is never `this::class.simpleName`: R8
  renames classes in the build a regression walks.
- A line marks a state change worth following in a regression: a screen built, a permission
  decision, a load and its count, a background pass and its result, a notification handed over.
- Never on a hot path: not per list item, recomposition, state emission, keystroke or scroll, and
  not in the orientation callback UIKit calls at its own rate.
- Never user input or anything that may carry it: the search text, the saved screen state, or an
  exception message quoting either. Such a failure is logged by its kind or its class name.
- Some lines are found by their wording: the iOS restore checks in
  `.github/scripts/ios-restore-checks.sh`, and module regression files. Keep that wording, and use
  none of those phrases in any other line.
