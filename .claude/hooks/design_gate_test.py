"""Tests the design gate: run `python -I .claude/hooks/design_gate_test.py` from any directory.

It runs the hook the way Claude Code does, on synthetic transcripts and on real repository paths,
and checks that every design file the hook demands is one a rule loaded on that path names.
Exits non-zero on a failure.
"""
import json
import os
import random
import re
import subprocess
import sys
import tempfile
import time

HOOKS = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HOOKS))
HOOK = os.environ.get("DESIGN_GATE_UNDER_TEST") or os.path.join(HOOKS, "design_gate.py")
sys.path.insert(0, HOOKS)
import design_gate  # noqa: E402

TMP = tempfile.mkdtemp(prefix="design-gate-test-")
failures = []


def read_line(name, root=ROOT):
    path = os.path.join(root, ".claude", "design", name)
    return {"type": "assistant", "message": {"content": [
        {"type": "tool_use", "name": "Read", "input": {"file_path": path}}]}}


COMPACT = {"type": "system", "subtype": "compact_boundary", "content": "Conversation compacted"}


def transcript(name, lines, raw_prefix=""):
    path = os.path.join(TMP, name + ".jsonl")
    with open(path, "w", encoding="utf-8", newline="") as handle:
        handle.write(raw_prefix)
        handle.writelines(json.dumps(line) + "\n" for line in lines)
    return path


def run(target, transcript_path, tool="Edit", extra=None, stdin=None, root=ROOT):
    """Returns the hook's stdout and exit code for one event."""
    key = "notebook_path" if tool == "NotebookEdit" else "file_path"
    event = {"hook_event_name": "PreToolUse", "tool_name": tool,
             "tool_input": {key: target}, "transcript_path": transcript_path, "cwd": root}
    event.update(extra or {})
    data = stdin if stdin is not None else json.dumps(event).encode("utf-8")
    done = subprocess.run([sys.executable, "-I", HOOK], input=data, capture_output=True,
                          env=dict(os.environ, CLAUDE_PROJECT_DIR=root), timeout=30)
    return done.stdout.decode("utf-8"), done.returncode


def decision(out):
    if not out.strip():
        return "allow"
    return json.loads(out)["hookSpecificOutput"]["permissionDecision"]


def check(label, ok, detail=""):
    if not ok:
        failures.append(f"{label}: {detail}")


def at(rel):
    return os.path.join(ROOT, *rel.split("/"))


# Every area: a real file of it is refused with no read, and passes once its files are read.
files = subprocess.run(["git", "-C", ROOT, "ls-files"], capture_output=True, text=True,
                       check=True).stdout.splitlines()
empty = transcript("empty", [{"type": "user", "message": {"content": "hi"}}])
for globs, name, section in design_gate.AREAS:
    samples = [f for f in files if not design_gate.SKIPPED.search(f)
               and any(design_gate.matches(g, f) for g in globs)]
    check(f"area {name} {globs}", samples, "no real file in the repository matches it")
    for sample in random.Random(0).sample(samples, min(3, len(samples))):
        out, code = run(at(sample), empty)
        check(f"deny {sample}", code == 0 and decision(out) == "deny", out)
        due = re.findall(r"\.claude/design/([\w-]+\.md)", out)
        check(f"names {name} for {sample}", name in due, out)
        all_read = transcript("all", [read_line(n) for n in due])
        out, code = run(at(sample), all_read)
        check(f"allow after reading {due} for {sample}", decision(out) == "allow", out)

# Every demanded design file is named by a rule that loads on that path.
rules = {}
for directory, _, names in os.walk(os.path.join(ROOT, ".claude", "rules")):
    for rule in names:
        text = open(os.path.join(directory, rule), encoding="utf-8").read()
        head = re.match(r"---\npaths:\n((?:\s+- .*\n)+)---", text)
        globs = re.findall(r'"([^"]+)"', head.group(1)) if head else []
        rules[os.path.join(directory, rule)] = (globs, text)
for sample in files:
    if design_gate.SKIPPED.search(sample):
        continue
    for globs, name, _ in design_gate.AREAS:
        if not any(design_gate.matches(g, sample) for g in globs):
            continue
        named = [r for r, (rg, text) in rules.items()
                 if any(design_gate.matches(g, sample) for g in rg) and f"design/{name}" in text]
        check(f"rule names {name} for {sample}", named, "no loaded rule sends there")

# Paths that pass with no read.
for rel in ["README.md", "core-kmp/navigation/CORE-KMP-NAVIGATION-README.md",
            ".claude/design/kmp.md", ".claude/rules/tests.md", "docs/superpowers/x.kt",
            "androidApp/build/generated/di/X.kt", "iosApp/project.yml",
            "androidApp/src/main/kotlin/a/presentation/compose/X.kt"]:
    out, _ = run(at(rel), empty)
    check(f"pass {rel}", decision(out) == "allow", out)
out, _ = run(os.path.join(os.path.dirname(ROOT), "Other", "di", "X.kt"), empty)
check("pass a file outside the checkout", decision(out) == "allow", out)

DI = "feature-kmp/anime-list/src/commonMain/kotlin/a/impl/di/DiAnimeListComponent.kt"
read_di = read_line("dependency-injection.md")
quoted = {"type": "user", "message": {"content": [{"type": "tool_result", "content":
          '{"type":"system","subtype":"compact_boundary"} and "compact_boundary"'}]}}
cases = [
    ("read", [read_di], DI, "allow"),
    ("read then compacted", [read_di, COMPACT], DI, "deny"),
    ("compacted then read", [read_di, COMPACT, read_di], DI, "allow"),
    ("quoted marker", [read_di, quoted], DI, "allow"),
    ("other repository", [read_line("dependency-injection.md", "C:/other/repo")], DI, "deny"),
    ("other design file", [read_line("mvi.md")], DI, "deny"),
    ("upper-case name", [read_line("DEPENDENCY-INJECTION.md")], DI, "allow"),
    ("nested worktree, its own design", [read_line(
        "dependency-injection.md", os.path.join(ROOT, ".claude", "worktrees", "w"))],
     ".claude/worktrees/w/" + DI, "allow"),
    ("nested worktree, the checkout's design", [read_di], ".claude/worktrees/w/" + DI, "allow"),
    ("nested worktree, no read", [], ".worktrees/x/" + DI, "deny"),
    ("non-ASCII path", [], "feature-kmp/anime-list/src/commonMain/kotlin/a/di/Тест Файл.kt", "deny"),
]
for label, lines, rel, expected in cases:
    out, _ = run(at(rel), transcript(label.replace(" ", "-"), lines, "{broken line\n"))
    check(label, decision(out) == expected, out)
out, _ = run(at(".worktrees/x/build.gradle.kts"), empty)
check("nested root build file", "build-and-tooling.md" in out and "new-module.md" not in out, out)
out, _ = run(at(DI), empty, tool="Write")
check("Write is gated", decision(out) == "deny", out)
out, _ = run(at("feature-kmp/anime-list/src/commonMain/kotlin/a/di/N.ipynb"), empty,
             tool="NotebookEdit")
check("NotebookEdit is gated", decision(out) == "deny", out)
out, _ = run(at(DI).lower(), empty)
check("lower-case drive and path still resolve inside the checkout",
      decision(out) in ("deny", "allow"), out)
out, _ = run(at(DI).replace("\\", "/"), empty)
check("forward slashes", decision(out) == "deny", out)

# A subagent counts only its own reads.
session = transcript("session", [{"type": "user"}])
sub_dir = os.path.join(os.path.splitext(session)[0], "subagents")
os.makedirs(sub_dir)
with open(os.path.join(sub_dir, "agent-a1.jsonl"), "w", encoding="utf-8") as handle:
    handle.write(json.dumps(read_di) + "\n")
check("main agent without a read", decision(run(at(DI), session)[0]) == "deny")
check("subagent with its read",
      decision(run(at(DI), session, extra={"agent_id": "a1"})[0]) == "allow")
check("other subagent", decision(run(at(DI), session, extra={"agent_id": "a2"})[0]) == "allow",
      "a missing subagent transcript lets the write through")

# Broken input never blocks and never prints anything but JSON.
for label, data in [("empty stdin", b""), ("not JSON", b"{oops"), ("no tool_input", b"{}"),
                    ("no transcript", json.dumps({"tool_input": {"file_path": at(DI)}}).encode())]:
    out, code = run(at(DI), empty, stdin=data)
    check(f"fail open: {label}", code == 0 and out == "", out)
out, code = run(at(DI), os.path.join(TMP, "missing.jsonl"))
check("fail open: missing transcript", code == 0 and out == "", out)

# A long transcript stays fast.
big = os.path.join(TMP, "big.jsonl")
# Filler that passes the hook's cheap substring tests, so every line is parsed as JSON.
filler = json.dumps({"type": "assistant", "message": {"content": [
    {"type": "tool_use", "name": "Read", "input": {"file_path": "/x/design/notes.txt"}},
    {"type": "text", "text": "x" * 2000}]}}) + "\n"
with open(big, "w", encoding="utf-8") as handle:
    handle.writelines(filler for _ in range(60000))
start = time.time()
out, _ = run(at(DI), big)
elapsed = time.time() - start
check("long transcript without the read", decision(out) == "deny" and elapsed < 10,
      f"{elapsed:.2f}s {out}")
with open(big, "a", encoding="utf-8") as handle:
    handle.write(json.dumps(read_di) + "\n")
out, _ = run(at(DI), big)
check("long transcript with the read last", decision(out) == "allow", out)

print(f"design gate: {len(failures)} failures, {os.path.getsize(big) // 2**20} MB transcript in "
      f"{elapsed:.2f}s")
for failure in failures:
    print("FAIL", failure)
sys.exit(1 if failures else 0)
