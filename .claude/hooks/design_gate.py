"""PreToolUse gate: refuses a Write or Edit in a design area until the session has read the area's
design file with the Read tool since its last compaction.

The areas are the path-decidable triggers of the thin rules and of
.claude/rules/technical-design.md; when one of those changes, change AREAS in the same commit.
A partial Read counts. Any failure here lets the write through.
"""
import json
import os
import re
import sys

# (globs, design file, section or None). Globs are repo-relative, gitignore-style, case-sensitive.
AREAS = [
    (["**/src/*Main/**/store/**/*.kt"], "mvi.md", "Executors and state"),
    (["**/src/*Main/**/presentation/compose/**/*.kt", "**/src/*Main/**/*Route.kt",
      "**/src/*Main/**/*{Dimens,Fonts,Colors,Const,Consts}.kt"], "ui-compose.md", "Design tokens"),
    (["**/src/*Test/**", "**/src/test/**", "**/fake/**", "**/core-kmp/test-utils/**",
      "**/iosApp/iosAppUITests/**"], "testing.md", None),
    (["**/src/androidHostTest/**", "**/src/androidDeviceTest/**", "**/androidApp/src/test/**",
      "**/androidApp/src/androidTest/**"], "testing-platforms.md", "Android"),
    (["**/src/iosTest/**", "**/src/iosMain/**/*.kt"], "testing-platforms.md", "iOS"),
    (["**/iosApp/iosApp/**/*.swift", "**/iosApp/iosApp/Info.plist",
      "**/core-kmp/di-app/src/iosMain/**"], "ios-host.md", None),
    (["**/iosApp/iosApp/PrivacyInfo.xcprivacy"], "security-and-privacy.md", "iOS privacy manifest"),
    (["/core-kmp/di-app/**"], "kmp.md", "The iOS framework"),
    (["**/src/androidMain/**/*.kt", "**/src/iosMain/**/*.kt"], "kmp.md", "Where code lives"),
    (["**/di/**"], "dependency-injection.md", None),
    (["**/navigation/**"], "navigation.md", None),
    (["**/data/**", "**/usecase/**"], "data-layer.md", None),
    (["**/savedstate/**"], "state-restoration.md", None),
    (["**/composeResources/**"], "ui-compose.md", "Resources and localization"),
    (["**/AndroidManifest.xml"], "module-anatomy.md", "Manifests and resources"),
    (["**/*Worker.kt"], "platform-mirroring.md", None),
    (["/settings.gradle.kts", "*/**/build.gradle.kts"], "new-module.md", None),
    (["/build.gradle.kts"], "build-and-tooling.md", None),
    (["/gradle/libs.versions.toml"], "tech-stack.md", None),
]
# A worktree nested in the checkout is matched as its own repository.
NESTED_WORKTREE = re.compile(r"^(?:\.claude/worktrees|\.worktrees)/[^/]+/")
# Agent setup, planning docs, build output and Markdown are not code areas.
SKIPPED = re.compile(r"^(?:\.claude|docs)/|(?:^|/)build/|\.md$")


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
    """`path` is repo-relative with forward slashes. A glob matches the path or any parent."""
    body = pattern.lstrip("/")
    prefix = "" if pattern.startswith("/") or "/" in body else "(?:.*/)?"
    regex = re.compile(prefix + translate(body) + "$")
    parts = path.split("/")
    return any(regex.match("/".join(parts[:k])) for k in range(len(parts), 0, -1))


def is_compaction(line):
    if '"compact_boundary"' not in line:
        return False
    try:
        entry = json.loads(line)
    except ValueError:
        return False
    return entry.get("type") == "system" and entry.get("subtype") == "compact_boundary"


def design_reads(transcript, design_dirs):
    """Lower-cased names of the design files one agent read with the Read tool, in one of
    `design_dirs`, since its last compaction."""
    names = set()
    with open(transcript, encoding="utf-8", errors="replace") as handle:
        for line in handle:
            if is_compaction(line):
                names.clear()
                continue
            if '"Read"' not in line or "design" not in line:
                continue
            try:
                entry = json.loads(line)
            except ValueError:
                continue
            for block in (entry.get("message") or {}).get("content") or []:
                if not isinstance(block, dict) or block.get("type") != "tool_use" \
                        or block.get("name") != "Read":
                    continue
                path = str((block.get("input") or {}).get("file_path") or "")
                if os.path.normcase(os.path.dirname(os.path.abspath(path))) in design_dirs:
                    names.add(os.path.basename(path).lower())
    return names


def main():
    event = json.loads(sys.stdin.buffer.read().decode("utf-8"))
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
    # The design of the checkout, and of a worktree nested in it, which may hold its own copy.
    design_dirs = {os.path.normcase(os.path.abspath(os.path.join(root, ".claude", "design")))}
    nested = NESTED_WORKTREE.match(rel)
    if nested:
        design_dirs.add(os.path.normcase(os.path.abspath(
            os.path.join(root, nested.group(0), ".claude", "design"))))
        rel = rel[nested.end():]
    if rel.startswith("../") or SKIPPED.search(rel):
        return
    due = []
    for globs, name, section in AREAS:
        if any(matches(g, rel) for g in globs) and (name, section) not in due:
            due.append((name, section))
    if not due:
        return
    read = design_reads(transcript, design_dirs)
    missing = [(n, s) for n, s in due if n not in read]
    if not missing:
        return
    files = "; ".join(f".claude/design/{n}" + (f', section "{s}"' if s else "") for n, s in missing)
    reason = (f"Before writing {rel}, read with the Read tool: {files}. "
              "It says how code in this area is written. Then make this change again.")
    print(json.dumps({"hookSpecificOutput": {"hookEventName": "PreToolUse",
                                             "permissionDecision": "deny",
                                             "permissionDecisionReason": reason}}))


if __name__ == "__main__":
    try:
        main()
    except Exception:  # noqa: BLE001 - a broken gate must never block work.
        pass
