# Regression files

The project's own rules for its test scripts win. Use this file where it has none, and as the
filter for the cleanup after a walk.

## Shape

- One file per module or feature, next to its code. One root index links every file; a file that
  is not linked is skipped silently.
- The index holds no steps. It says which build to walk, the passes (font and display size,
  orientation), and how to read the app's logs if it has any.
- Each step: where to go, what to do, what should happen. Exact text, position, what appears and
  what goes away. A step without an expected result is not a check.
- Each step states its precondition, so a PASS means something.

## One model for every platform

- The steps describe the app, not one platform. The user experience is meant to be the same, so
  the steps are the same.
- A platform-only step is rare. It names the platform and says why only that one can reach it.
  Keep such steps, and a "Platforms" section where a file has one, when steps are merged.
- Where a platform flow genuinely differs, such as a permission prompt, describe each exactly.
- Do not lean the wording toward the platform walked last.

## What stays out

- Checks that depend on the OS, vendor, device or system settings: notification placement and
  tint, icon shapes, battery and sleep policies, scheduling precision.
- What the system draws: its bars and indicators, its notification layout, its dialogs. Check what
  the app draws.
- Data the backend never sends. Cover it with a unit test instead.
- Checks of behavior the developer has accepted as designed.
- Blocked steps the developer has waived.
- Exact margins, or how far a badge grows, at the largest font and display size. There a step
  checks that everything stays readable and reachable. A designed line limit, or which element
  shortens first, may still be stated.

## What goes in

- An expected flash or placeholder is stated in the step: "The date may read 'unknown' for a
  moment while the details load."
- A step for every check a walk needed and could not find, such as folding a foldable with each
  screen open.
- Every way the device can differ: no network, rotation, dark mode, maximum font and display size,
  back from background, process death, fold and unfold.

## After a walk

- Fix every step the walk proved wrong. Remove steps that are outdated or that describe noise.
- Re-check each edit against the behavior actually seen on each platform. There must be no
  mismatch left.
- Then have a reviewer read every file end to end, and skeptics check the reviewer's findings.
