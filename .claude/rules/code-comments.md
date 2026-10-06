---
paths:
  - "**/*.{kt,kts,swift,sh,py,yml,pro,toml,xcconfig,xml,properties}"
---

# Code comments

- Comments and KDoc describe the current code, not the task that produced it. Never write what
  existed before the change, what didn't exist yet, or other task/migration history/status
  ("this used to be X", "doesn't exist in this repo yet", "tracked here so it isn't
  forgotten"). That belongs in the commit message or PR description; in the code it rots the
  moment it's no longer true.
- Never compare the current code to a previous/removed implementation, even indirectly ("X used
  to draw this as...", "unlike the old View-based version..."). Describe only what the code in
  front of the reader does and why, as if no earlier version ever existed.
- Inline comments (not class/interface-level KDoc) are only for code that genuinely isn't
  self-evident: a hidden constraint, a race being guarded against, a workaround, a non-obvious
  magic value. If the code is clear on its own, add no comment at all.
- A warranted comment is the shortest phrase that states what and why: one line next to the
  non-obvious part. Don't restate the code, explore alternatives or explain the implementation
  step by step.
- The same brevity applies to KDoc on classes/interfaces/functions: a short "what this is and why
  it exists," not a walkthrough of how it's implemented.
- Keep sentences short: roughly 20 words, never around 40. The IDE flags long sentences. Split a
  long sentence in two instead of joining clauses with commas, "and" or em dashes.
- Before writing KDoc or documenting code, use the `code-documentation` skill.
