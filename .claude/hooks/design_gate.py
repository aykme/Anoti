"""PreToolUse gate: refuses a Write or Edit in a design area until the session has read the area's
design file with the Read tool since its last compaction.

The areas mirror the triggers of the thin rules and of .claude/rules/technical-design.md; when one of
those changes, change AREAS below in the same commit. Any failure here lets the write through.
"""
import json
import os
import re
import sys

# (globs, design file, section or None, condition). Globs are repo-relative, gitignore-style.
# Condition: None (every write), "new" (only a file that does not exist yet), "nofake" (skips doubles).
AREAS = [
    (["**/src/*Main/**/store/**/*.kt"], "mvi.md", "Executors and state", "nofake"),
    (["**/src/*Main/**/presentation/compose/**/*.kt",
      "**/src/*Main/**/*{Dimens,Fonts,Colors,Const,Consts}.kt"], "ui-compose.md", "Design tokens", None),
    (["**/src/*Test/**", "**/src/test/**", "**/fake/**", "**/core-kmp/test-utils/**",
      "**/iosApp/iosAppUITests/**"], "testing.md", None, None),
    (["**/src/androidHostTest/**", "**/src/androidDeviceTest/**", "**/androidApp/src/test/**",
      "**/androidApp/src/androidTest/**", "**/src/iosTest/**"], "testing-platforms.md", None, None),
    (["**/iosApp/iosApp/**/*.swift", "**/iosApp/iosApp/Info.plist"], "ios-host.md", None, None),
    (["**/iosApp/iosApp/PrivacyInfo.xcprivacy"], "security-and-privacy.md", None, None),
    (["/core-kmp/di-app/build.gradle.kts"], "kmp.md", "The iOS framework", None),
    (["**/src/androidMain/**/*.kt", "**/src/iosMain/**/*.kt"], "kmp.md", "Where code lives", "new"),
    (["**/di/**"], "dependency-injection.md", None, None),
    (["**/navigation/**"], "navigation.md", None, None),
    (["**/data/**", "**/usecase/**"], "data-layer.md", None, None),
    (["**/savedstate/**"], "state-restoration.md", None, None),
    (["**/composeResources/**"], "ui-compose.md", "Resources and localization", None),
    (["**/AndroidManifest.xml"], "module-anatomy.md", "Manifests and resources", None),
    (["**/*Worker.kt"], "platform-mirroring.md", None, None),
    (["/settings.gradle.kts", "*/**/build.gradle.kts"], "new-module.md", None, None),
    (["/build.gradle.kts"], "build-and-tooling.md", None, None),
    (["/gradle/libs.versions.toml"], "tech-stack.md", None, None),
]
# Paths no area applies to, even when a glob above matches them.
SKIPPED = re.compile(r"^(?:\.claude|docs)/|(?:^|/)build/")


def translate(pattern):
    """Turns one gitignore-style glob into a regex body."""
    out, i = "", 0
    while i < len(pattern):
        if pattern.startswith("**/", i):
            out, i = out + "(?:.*/)?", i + 3
        elif pattern.startswith("**", i):
            out, i = out + ".*", i + 2
        elif pattern[i] == "*":
            out, i = out + "[^/]*", i + 1
        elif pattern[i] == "{":
            end = pattern.index("}", i)
            out += "(?:" + "|".join(re.escape(p) for p in pattern[i + 1:end].split(",")) + ")"
            i = end + 1
        else:
            out, i = out + re.escape(pattern[i]), i + 1
    return out


def matches(pattern, path):
    """`path` is repo-relative with forward slashes. A glob matches the path or any of its parents."""
    body = pattern.lstrip("/")
    prefix = "" if pattern.startswith("/") or "/" in body else "(?:.*/)?"
    regex = re.compile(prefix + translate(body) + "$", re.I)
    parts = path.split("/")
    return any(regex.match("/".join(parts[:k])) for k in range(len(parts), 0, -1))


def design_reads(transcript):
    """Lower-cased names of the design files one agent read with the Read tool since its last
    compaction."""
    names = set()
    with open(transcript, encoding="utf-8", errors="replace") as handle:
        for line in handle:
            if "compact_boundary" in line:
                names.clear()
            if '"Read"' not in line or "design" not in line:
                continue
            try:
                entry = json.loads(line)
            except ValueError:
                continue
            for block in (entry.get("message") or {}).get("content") or []:
                if isinstance(block, dict) and block.get("type") == "tool_use" and block.get("name") == "Read":
                    file_path = str((block.get("input") or {}).get("file_path") or "").replace("\\", "/")
                    if "/.claude/design/" in file_path:
                        names.add(file_path.rsplit("/", 1)[-1].lower())
    return names


def main():
    event = json.load(sys.stdin)
    tool_input = event.get("tool_input") or {}
    target = tool_input.get("file_path") or tool_input.get("notebook_path")
    root = os.environ.get("CLAUDE_PROJECT_DIR") or event.get("cwd")
    transcript = event.get("transcript_path")
    if transcript and event.get("agent_id"):
        # A subagent counts only its own reads, kept beside the session's transcript.
        transcript = os.path.join(os.path.splitext(transcript)[0], "subagents",
                                  f"agent-{event['agent_id']}.jsonl")
    if not target or not root or not transcript or not os.path.isfile(transcript):
        return
    rel = os.path.relpath(os.path.abspath(target), os.path.abspath(root)).replace("\\", "/")
    if rel.startswith("../") or SKIPPED.search(rel):
        return
    due = []
    for globs, name, section, condition in AREAS:
        if condition == "new" and os.path.exists(target):
            continue
        if condition == "nofake" and "/fake/" in rel.lower():
            continue
        if any(matches(g, rel) for g in globs) and (name, section) not in due:
            due.append((name, section))
    if not due:
        return
    read = design_reads(transcript)
    missing = [(n, s) for n, s in due if n not in read]
    if not missing:
        return
    files = "; ".join(f".claude/design/{n}" + (f', section "{s}"' if s else "") for n, s in missing)
    reason = (f"Before writing {rel}, read with the Read tool: {files}. "
              "It says how code in this area is written. Then make this change again.")
    print(json.dumps({"hookSpecificOutput": {"hookEventName": "PreToolUse",
                                             "permissionDecision": "deny",
                                             "permissionDecisionReason": reason}}))


try:
    main()
except Exception:  # noqa: BLE001 - a broken gate must never block work.
    pass
