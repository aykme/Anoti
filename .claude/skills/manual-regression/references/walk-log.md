# The walk log

One file, written as the walk goes, so the walk can resume after a pause, a lost session or a
device the developer borrowed. Re-read it for paused steps before the findings check.

## Template

```markdown
# Walk log — <app> <version>, <build>, <date>

Phase: <walk / findings / report>    Block: <current block of the plan>

| Device | Build installed | Font / display size | Orientation | Screen reader | Network | Notes |
|---|---|---|---|---|---|---|
| phone-api-37 | 1.4 (14) shipped, verified 12:40 | 1.0 / default | locked portrait | off | online | |

| File | Step | Device | Pass | Status | Observation | Media |
|---|---|---|---|---|---|---|
| list | 4.2 | phone-api-37 | 3 | FAIL | Button cut off at the right edge | phone/p3-list-4.2.png |
| favorites | 7.1 | tablet | 1 | PAUSED | Tablet lent to the developer | |
```

Update the device table whenever a device state changes, and re-check the installed build after a
device was borrowed.

## Statuses

| Status | Meaning |
|---|---|
| PASS | Done as written, the precondition present, the result as expected |
| PASS~ | Done by an approximation the spec allowed; say which |
| FAIL | The app is wrong; the pre-FAIL checklist was run |
| DOC | The app is right; the step is wrong |
| BLOCKED | Could not be done here; say why and how it could be |
| NOT VERIFIED | No tool can reach it on this setup |
| N/A | Does not apply to this device or pass |
| PAUSED | Started, interrupted, must be resumed |

## Pre-FAIL checklist

Before writing FAIL:

1. Reproduce it, from a fresh install if the state could matter.
2. Compare with the previous release on the same device.
3. Compare with the other platform, if the app ships on more than one.
4. Read the code path or the design intent.
5. Rule out the OS, vendor, settings and the emulator or simulator.
6. Rule out the tool: the walk's own action, a dropped tap, a screenshot taken too early.
7. Confirm the step's precondition was present.

## Evidence

- Wait for the screen to settle (two equal frames, or the element the step is about) before a
  screenshot. Use the app's logs to know when a load ended.
- Record video for anything that moves, flashes or animates; stills miss it.
- Name media by device, pass, file and step.
