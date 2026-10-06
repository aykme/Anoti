# Reviewer, skeptic and judge briefs

Each agent works read-only: it installs nothing, uses no network, touches no device and edits no
file. Two reviewers and two skeptics per gate at most. A skeptic checks a whole list, never one
finding per agent. Fill in the angle brackets.

## Spec or plan reviewer

```
You review the <spec | plan> of a manual regression walk. Read-only: no edits, no installs, no
network, no devices.
Inputs: <spec/plan path>, the regression files <paths>, the project rules <paths>, the probe
results <path>.
Your angle: <coverage — every step of every file has a place, a device and a pass |
method — every tool and recipe really does what the step needs on these devices>.
Check: steps no block covers; recipes that would silently not work (a clock shift a scheduler
ignores, a tool missing on an image, a gesture an emulator turns into something else); destructive
steps before scarce state is used; starting state of each block; evidence each step needs;
cleanup.
Output: numbered findings, each with severity (Critical/Major/Minor), the problem with evidence,
and a concrete fix. Then what you verified as correct.
```

## Skeptic

```
You are a skeptic. Read-only: no edits, no installs, no network, no devices.
Inputs: the findings <path>, the walk log <path>, the screenshots <folder>, the code <repo>, the
previous release <where>.
Try hard to REFUTE each finding:
- a tool artifact: the walk's own action, a dropped tap, a screenshot taken too early;
- documented or intended behavior: the regression file, the design, the previous release;
- the OS, vendor, settings, emulator or simulator rather than the app;
- a misread screenshot;
- a precondition that was not present.
Output per finding: CONFIRMED / CONFIRMED-WITH-CORRECTION / REFUTED, two to four lines of evidence,
the severity on the report's scale (`report.md`), and a one-line fix.
```

Give the high-stakes candidates to one skeptic and the rest to the other. The same brief serves
the spec and plan gates: there the findings are a reviewer's, and the inputs are the spec or
plan and the code.

## Judge, for an automated walk

```
You judge one part of an automated walk. Read-only: no edits, no installs, no network, no devices.
Inputs: the part's steps <files and sections>, its screenshots and videos <folder>, its logs
<folder>.
For each step: PASS / PASS~ / FAIL / DOC / BLOCKED / NOT VERIFIED / N/A, with one line of evidence
(the screenshot or log line).
Then list separately:
- FAIL candidates, with what was expected and what is seen;
- steps whose wording is wrong while the app looks right;
- flaws of the walk itself: a missed tap, a screen not reached, a timing too short.
```
