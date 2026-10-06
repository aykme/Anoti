---
name: manual-regression
description: Use when asked to run a manual or full regression of an app, to walk its regression test scripts on emulators, simulators or a CI runner, to write the regression report, or to triage and fix what a regression found. Also when planning such a walk or reviewing its findings.
---

# Manual regression

## Overview

A manual regression walks the app as a user would, from written test scripts, on the build users
get, and reports only what is wrong for the user. It is not a diff between the app and the
wording of a script, nor between one OS and another.

The project's own instructions override this skill. Where the project has rules or a
documentation skill that own its regression files, those win. This skill's
`references/regression-files.md` is then only a fallback and the filter for the cleanup after a
walk. Everything below marked *default* is a starting point that phase 1 confirms with the
developer.

## Prerequisites

- Regression files: one test script per module or feature, and one root index linking them all.
  Each step says where to go, what to do and what should happen, for a tester who has never seen
  the code. Steps are common to every platform the app ships on; a platform-only step is rare and
  says why.
- If the files are missing or stale, writing them comes first. See
  `references/regression-files.md`, or the project's documentation rules.

## Phases

Copy this checklist into the working notes and tick it off.

```
[ ] 0 Brief        scope, build, devices, media folder, what must not be touched
[ ] 1 Questions    all disputed points in one go, each with a recommendation
[ ] 2 Probe        try the tools on the target, not the steps
[ ] 3 Spec         + review: up to 2 reviewers, up to 2 skeptics
[ ] 4 Plan         + review: the same
[ ] 5 Walk         walk log; nothing fixed
[ ] 6 Findings     skeptic pass; paused steps walked
[ ] 7 Report       page + chat summary; devices and scratch cleaned
[ ] 8 Triage       with the developer, one finding at a time
[ ] 9 Scripts      regression files cleaned, then swept
[ ] 10 Fixes       own branch, the project's finishing checks
```

0. **Brief.** *Default:* the build users get (shrunk and obfuscated where the platform does it),
   on emulators or simulators only, never the developer's own device. The media folder is named by
   the developer; if it is missing or renamed, ask instead of adapting.
1. **Questions.** Ask everything disputed at once, each with a recommended option and its
   trade-off. The usual list is in `references/questions.md`. Learn the constraints before
   offering options, and explain any term in plain words. Agree here how heavy the gates are: one
   module needs a short plan and one skeptic pass, not the full set.
2. **Probe.** Before writing hundreds of steps, try each tool once on the target: forcing
   background work, cutting the network, answering permissions, maximum scale, notifications. Sort
   every step as real, approximated or impossible from what the probe proved.
3. **Spec.** Steps by kind, device roles, scarce state used before destructive steps. Reviewed
   (`references/reviews.md`).
4. **Plan.** Device by device; each block's starting state; destructive steps last; cleanup at the
   end. Reviewed the same way. For an automated walk, review the walk code and harden its helpers
   before the first run.
5. **Walk.** Keep the resumable walk log (`references/walk-log.md`). Run the pre-FAIL checklist
   before any FAIL. A PASS counts only with its precondition present. Fix nothing. Keep evidence
   for every non-PASS. Release each device as soon as its part is done. For an automated walk, sort
   every failed run as app, check code or environment. A tool fault that could taint every part
   re-runs every part.
6. **Findings.** One skeptic pass over all candidates, up to two skeptics, high-stakes candidates
   to one of them. They try to refute. Then re-read the walk log for paused or skipped steps and
   walk them before reporting.
7. **Report.** As `references/report.md` says, plus a short chat summary. Then clean the devices
   (every state the walk created) and the scratch files the developer does not need.
8. **Triage.** Each finding with a proposal; the developer answers yes, no or otherwise. Reproduce
   on demand and find the exact cause before proposing code.
9. **Scripts.** Clean the regression files by the project's rules, then have a reviewer and
   skeptics sweep all of them. Re-check every edit against the behavior seen on each platform.
10. **Fixes.** *Default:* on their own branch, with the project's finishing checks, verified on
    every screen class and kind of restart they touch. The other platform only when the developer
    says.

## Autonomy

After the answers, do not stop. Take disputed decisions alone: weigh the alternatives, pick, and
list them under "Decisions taken" in the report. Stop only for work outside the agreed scope,
destructive actions on the developer's resources, a push or merge the project has not allowed,
and a missing media folder. A message from the developer during the walk (a status question, a
device they want back) is answered and the walk goes on.

At each phase change, and whenever asked, say where the walk is, what is left, and an estimate.
Calibrate it: a walk once planned at 25–35 hours took about 5.

## Classifying a candidate

| Verdict | Test |
|---|---|
| App bug | Wrong for the user on the build users get, reproduced, not explained by anything below |
| Script wrong | The app does what was designed; the step says otherwise. Fix the step |
| Noise | One of the categories below |
| Tool artifact | The walk's own action caused it, or the evidence was misread |
| Environment | The emulator, simulator or CI runner, not the app |

### Noise: do not report

- Behavior the OS, vendor, device, system settings, emulator or simulator decide.
- UI the system draws: its bars and indicators, its notification layout, its dialogs.
- Cosmetics at extreme scale that lose nothing: ellipsis, overlap, tight margins, a badge growing
  with the font.
- Intended placeholders and brief flashes while data loads.
- Platform-guideline advice that would not change the shared flow.
- The first tap after a fling, which only stops the fling.

The line for layout is simple. Can the user still read and reach everything, and do orientation
and screen fill hold? If not, it is a finding. A real bug with low impact is reported with its
impact, never filtered out: the developer decides.

### Data the backend never sends

Write no manual step for it, and spend no walk time building it. Cover it with a unit test or a
code read. A defect found that way is reported as latent, with a test proposal. Force such data
only where it is cheap, or where the code read points to a defect.

### What proved valuable

- State across each kind of restart, checked separately: configuration change, activity or scene
  rebuild, process death, relaunch.
- Folding, unfolding and moving between displays.
- Every screen class at maximum font and display size.
- Orientation and letterboxing on large screens.
- Upgrading from the previous release without opening the app.
- Background work forced, and once left to run for real.
- A race repeated several times, its outcomes classified. One try proves nothing.

## Agents

At most two reviewers and two skeptics per gate, each checking a whole list in batches, never one
agent per finding. Use high effort where the harness lets you set it. Reviewers, skeptics and
judges never install packages, use the network, touch devices or edit files. Briefs are in
`references/reviews.md`.

## Logs

If the app logs nothing at its key state changes, propose such logs to the developer before the
walk; do not add them unasked. With them, a walk times screenshots and tells states apart from
the log instead of guessing.

## Working files

Keep the spec, plan, walk log and findings until triage is closed. Then harvest the lessons into
the project's memory or notes, and delete them.

## Common mistakes

- Reporting OS-, vendor- or setting-dependent behavior, and extreme-scale cosmetics.
- Reporting an intended placeholder or flash without checking the design or the previous release.
- Trusting a wrong step instead of the code and the build users have.
- A technical report: per-step tables, file and line causes, run counts, tool talk.
- Steps for data that never occurs live, walked at great cost and then deleted.
- A PASS without its precondition, such as a "marks stay" step with no marks on screen.
- Misreading a screenshot. Look again before calling it.
- Leaving paused steps unresumed after freeing a device.
- A tool action that destroys evidence, such as clearing every notification mid-check, or one
  that drives data out of range.
- Setting scale or settings through slow UI taps instead of the console, and stopping short.
- Renaming or adapting the developer's media folder silently.
- Building a proposed fix before explaining the real cause to the developer.
- Leaving state the walk created on a device. It broke a later test.
- Re-running a whole automated walk for flaws in its own helpers that a probe would have caught.

## References

- `references/questions.md` — phase 1 questions with their usual recommendation.
- `references/regression-files.md` — writing and cleaning the test scripts.
- `references/walk-log.md` — log template, statuses, the pre-FAIL checklist.
- `references/report.md` — report skeleton, finding card, severity, tone, PDF.
- `references/reviews.md` — reviewer, skeptic and judge briefs.
- `references/android-emulators.md` — Android emulator lessons.
- `references/ios-simulators-ci.md` — iOS simulators on a CI runner, without a Mac.
