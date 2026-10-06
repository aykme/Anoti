---
paths:
  - "**/.claude/rules/android/no-logic-proof.md"
---

# Proving a task that changes no logic

- Build the minified variant before the change and after it, with the NDK and LF checkout that
  [r8-minified.md](r8-minified.md) requires. Compare `mapping.txt` and the entries of the two APKs;
  `unzip -v` lists every entry with its checksum.
- `mapping.txt` must match, apart from the renamed names and shifted source line numbers.
- Every APK entry must keep its checksum, apart from the ones the change is known to touch.
- An edit that only shifts line numbers still rewrites `classes.dex` and the profile files under
  `assets/dexopt/`. Those are expected to differ then; nothing else is.
- `META-INF/version-control-info.textproto` is left out of the comparison. It holds the commit
  hash, so it differs on every commit.
- The APK size is not compared. That one file compresses to a different length from one commit to
  the next, so the size can differ with nothing changed and can match by luck.
- Remove the module's build directory before each of the two builds. An incremental APK is not
  byte-stable.
