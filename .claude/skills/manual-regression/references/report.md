# The report

## Contents

- Principles
- Skeleton
- The finding card
- Severity
- Tone
- Making the page
- PDF, on request

## Principles

- Problems only, ordered by severity. No per-step table, no pass counts per step, no file and
  line causes, no run numbers, no talk about tools or helpers. The developer reads it to decide,
  not to audit the walk.
- Every problem carries its screenshot, taken from the evidence already gathered. A comparison
  shows both sides next to each other: the previous release, the other platform, the other device.
- Every problem carries a proposal. Code names appear only inside the proposal.
- Written in the developer's language. Next to the page goes a chat summary of a few lines with
  the link.

## Skeleton

1. **Header.**
   - A small eyebrow line: "Full regression · <platform> · <build kind>".
   - The title.
   - A lead paragraph: what was walked, on which build and devices; that only problems follow, by
     severity; that each was checked by skeptics against the code, the screenshots and the
     previous release; that nothing was fixed.
   - Chips: commit, build and version, each device with its OS level, the date.
   - A tally line: counts per severity, regression-file fixes, not verified, crashes and
     shrinker or obfuscation errors.
2. **App problems.** First line: whether there were crashes, ANRs or obfuscation errors (usually
   "none"). The definition used: a regression is what worked in the previous release. Then one
   card per finding.
3. **Regression-file fixes.** A short list: new step, wrong step, remove, clarify, each with the
   file and section.
4. **Not verified, or verified approximately.** Each item with why, and how it could be done.
5. **Decisions taken.** The disputed calls made alone during the walk, for discussion.
6. **Evidence.** Where all screenshots, videos and logs are.

## The finding card

- Tags: severity; origin ("regression" or "also in the previous release"); scope ("all devices",
  "tablet, maximum display size", "upgrade only", "offline").
- Title: the symptom as the user sees it, not the cause.
- **What happens.** Plain words. Cite the regression step it contradicts.
- **How to reproduce.** Numbered, only where it is not obvious.
- **Consequence.** Only where the impact needs saying: "the first notification comes about an hour
  late, once".
- **Proposal.** What to change. Where the app may be right, offer "or fix the step's wording" as
  the alternative.
- Screenshots with captions, beside the text on a wide screen and below it on a phone.

## Severity

| Level | Criteria |
|---|---|
| High | Data lost, a crash, a feature unusable, on a common device or path |
| Medium | A feature works wrongly or repeats itself on a common path, with a workaround |
| Low–medium | Wrong on an unusual screen or setting, the user still gets through |
| Low | Cosmetic loss, or a one-time effect on an uncommon path |

## Tone

- "After folding the phone, the notification explanation appears again, though it was closed."
  Not: "The root host re-runs the permission check on recreation."
- "On a tablet at the largest display size the explanation is cut off and cannot be scrolled."
  Not: "Text overflow in the dialog at a smallest width of 533 dp."

## Making the page

- Theme-aware, one reading column, cards stacking on a phone.
- Screenshots downscaled (a few hundred pixels wide) and embedded, so the page stands alone.
- If the harness publishes pages, publish it privately and give the link; otherwise an HTML file
  in the media folder.

## PDF, on request

- Add a print stylesheet to a copy of the page: A4 with margins of about 13–16 mm, light theme
  values forced for every theme selector, `print-color-adjust: exact`, `break-inside: avoid` on
  cards and list items, `break-after: avoid` on headings, body text around 10.5 pt, smaller
  images, `word-break` on code.
- Print it with a headless Chromium (`--headless=new --print-to-pdf=... --no-pdf-header-footer`),
  giving web fonts time to load (`--virtual-time-budget`).
- Check every page before sending it: if no PDF renderer is installed, screenshot the page at
  print width and read it in parts.
- Never put a contact sheet of many video frames into the PDF; it becomes unreadable. Use two or
  three frames instead.
- Use only what the machine already has; ask before installing anything.
