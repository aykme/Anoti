# Agent setup

How the knowledge an agent works from is organized here, so it can be extended and copied: the
always-loaded instructions, the rule files and how they load, the skills, this design, the local
planning docs and the agent's memory. It ends with the two checks that prove the setup works: the
link check and the reachability walk.

Read when: adding or changing a rule, a skill or a design file; changing a rule's `paths:`;
checking that links resolve and rules still reach their readers; repeating the reachability walk;
looking for where a piece of agent knowledge lives.

## Layers

| Layer | Where | Loads |
|---|---|---|
| Instructions | [CLAUDE.md](../../CLAUDE.md), [CLAUDE-ANDROID.md](../rules/android/CLAUDE-ANDROID.md), [CLAUDE-IOS.md](../rules/ios/CLAUDE-IOS.md) | every session, at start |
| Rules | `.claude/rules/**/*.md` | by path, or when read through an index line |
| Design | `.claude/design/*.md` | read on purpose, through [TECHNICAL-DESIGN.md](TECHNICAL-DESIGN.md) or a rule that points here |
| Skills | `.claude/skills/<name>/SKILL.md` | when invoked, or when Claude finds the description relevant |
| Planning docs | `docs/superpowers/specs/`, `docs/superpowers/plans/` | read on purpose; ignored by git |
| Memory | outside the repository, per project | its `MEMORY.md` index, every session |

- `CLAUDE.md` holds the rules every task needs and the index of the rest. The two platform indexes
  live under `.claude/rules/` without `paths:`, so they load at start with it.
- Each index line names a topic file and the situation that makes reading it due. The working
  instructions for loading are in
  [CLAUDE.md "How the rules load"](../../CLAUDE.md#how-the-rules-load).

## How rules load

Verified on Claude Code 2.1.292 with an `InstructionsLoaded` hook, and in the Claude Code memory
docs:

- A rule without `paths:` loads at session start.
- A rule with `paths:` loads when the Read, Write or Edit tool touches a matching file. It loads
  once per session, and again on the next match after a compaction.
- On a Write of a new file, the rule arrives after the write. So the first file of a new area is
  written before its rule is seen, unless an index line or the task table sent the agent first.
- Shell reads, Grep and Glob load nothing.
- Files outside the session's project root match nothing, a sibling worktree included.
- A process file whose `paths:` lists only itself never auto-loads. Reading it shows it once. It
  is reached by its trigger line in an index. Example: [committing.md](../rules/committing.md).
- Subagents, worktree-isolated ones too, load rules the same way.
- Only `paths` is read from the frontmatter. It takes a YAML list or a comma-separated string. A
  frontmatter that does not parse is ignored, and the rule then loads at every session start.

Globs are gitignore-style:

- A pattern without a slash matches at any depth.
- A leading `/` anchors to the project root: `/build.gradle.kts` is the root build file only. A
  worktree nested under the root needs its own pattern; see the `paths:` of
  [test-coverage.md](../rules/test-coverage.md).
- `**/` crosses dot directories, so `**/.github/**` matches.
- Braces expand: `**/*.{kt,swift}`.

## Rules and design

- Rules say what to do and when: processes, checks, gates, what to cover. The design says how to
  write: where code lives, how an entity is built and named. A fact has one owner; the other side
  links it.
- A thin rule keeps its `paths:` and holds a bullet that sends to a named design file and
  section, due whenever the task does what the bullet names. Bullets that stay rules sit beside
  it. Example: [mvi-stores.md](../rules/mvi-stores.md).
- The area-pointer rule [technical-design.md](../rules/technical-design.md) loads on DI,
  navigation, data, usecase, saved-state, resource, manifest, worker and build paths. Its table
  names the design file of each area.
- A rule stays full when its guard is needed on almost every edit (comments, logging, the
  `commonMain` default), when it guards the very file it names (R8, the iOS Release settings), or
  when it is one situation's whole policy (device tests). Examples:
  [logging.md](../rules/logging.md), [android/r8-minified.md](../rules/android/r8-minified.md).
- New-file work is covered by `CLAUDE.md` "Technical design" and the task table of
  [TECHNICAL-DESIGN.md](TECHNICAL-DESIGN.md), since a path rule arrives too late for it.

## Skills

- A project skill is `.claude/skills/<name>/SKILL.md` with `name` and `description` frontmatter.
  Longer material sits in its `references/` folder and is read when the skill points there.
- Here: [code-documentation](../skills/code-documentation/SKILL.md) for READMEs, KDoc and
  regression files, and [manual-regression](../skills/manual-regression/SKILL.md) for walks.
- User-level skills and plugins also appear in a session. They belong to the machine, not to
  the repository. `CLAUDE.md` "Skills" says how skills are picked.

## Planning docs and memory

- Specs and plans for a large task are local files under `docs/superpowers/`, ignored by git.
  Nothing tracked links them; they are named in text. Their place in a task is in
  [development-workflow.md](development-workflow.md).
- Claude's auto memory lives in `~/.claude/projects/<project>/memory/`, where `<project>` is
  derived from the repository. Every worktree of the repository shares it. It is machine-local.
- It holds `MEMORY.md`, an index with one line per note, and one file per fact: a preference, a
  decision, a machine fact, a lesson. The first 200 lines or 25 KB of the index load every
  session; a note is read when its line points there.
- Memory is not linkable from the repository and is not copied to a new project. A fact every
  session needs belongs in a rule; a fact about how code is written belongs here in the design.

## Adding a rule

1. Decide between a rule and a design section with the split above. How-to content goes to the
   design, with a thin rule only where a path should deliver it.
2. Write `paths:` as a YAML list of quoted globs, then `# Title` and `-` bullets. A process file
   lists only itself. Leave `paths:` out only for what every session needs.
3. Check the globs against real paths, including the first file a new area creates.
4. Add an index line to `CLAUDE.md`, or to a platform index for a platform rule: the file and the
   situation that makes it due. A thin rule's line says which design file it sends to.
5. Run the link check, and the loading check when `paths:` changed.

## Adding a design file

1. Start with `# Title`, one scope paragraph, then `Read when: <situations>.` as its own
   paragraph.
2. Copy the `Read when:` text verbatim into the index of [TECHNICAL-DESIGN.md](TECHNICAL-DESIGN.md).
3. Add or extend a row of its task table, three files at most, in reading order.
4. Link rules for what they own, never retell them. Feature code appears only as `Example:` links.
   Departures found in the code are not described as the approach: they are noted as `As found:`,
   reported, and fixed or accepted by the developer.
5. Point a thin rule or the area-pointer rule at it where a path should deliver it.
6. Run the link check.

## Link check

Run it over `CLAUDE.md`, every `.md` under `.claude/rules/` and every file in `.claude/design/`:

1. Take the known paths from `git ls-files -co --exclude-standard`: tracked files plus untracked
   ones not ignored. Add every directory prefix of them.
2. Skip lines inside fenced code blocks. On the other lines, take every inline link: text in
   brackets, then the target in parentheses. Skip images and targets with a scheme (`https:`,
   `mailto:`).
3. A target containing a backslash fails.
4. Split the target at `#`. Resolve the path part against the linking file's folder; an empty
   path means the file itself. Normalize it.
5. A result starting with `..` leaves the repository and fails.
6. The result must equal a known path exactly, case included. Windows and macOS ignore case on
   disk, so a disk check passes a link that breaks on Linux and GitHub.
7. For an anchor into a `.md` file, build each heading's slug outside fences: lower case, drop
   backticks, asterisks and underscores, drop every other character but letters, digits, hyphens
   and spaces, then turn spaces into hyphens. The anchor must equal one of them. Slugs of headings
   with symbols differ between renderers, so anchors point only at plain-word headings.
8. Report each failure as file and line. Zero problems is the pass.

## Loading check

- One fresh headless session per rule and glob: `claude -p` with a `--settings` file holding an
  `InstructionsLoaded` hook, only the Read tool allowed, and a prompt to read one representative
  file. Write the tool list as `--allowedTools=Read`; without the `=` the flag swallows the prompt.
- Compute the expected loads with a gitignore-style matcher over every rule's `paths:`, and
  compare them with the logged `file_path` and `load_reason`.
- Add one session that reads every design file (no rule may load) and one that writes a new file
  (the rule arrives after the write).

## Reachability walk

It proves that real sessions doing real tasks read the due design file before writing in its
area. It can be repeated without scripts:

- **Checkouts.** One clean worktree per parallel slot, outside the main checkout, detached at the
  commit under test. Seeded scenarios get commits of their own on top. Before each run, reset hard,
  clean ignored files too, and confirm the commit and an empty `git status --ignored`.
- **Memory.** Worktrees share auto memory, so a run would read and write the real one. Give each
  slot `autoMemoryDirectory` pointing at a fresh copy, through the `--settings` file. Prove the
  isolation once with a canary note, and compare the real memory's hash before and after.
- **Hooks.** In the same settings file: `PostToolUse` and `PostToolUseFailure` with matcher `.*`,
  `InstructionsLoaded` and `SessionStart`. Each appends one JSON line to a log per `session_id`:
  time, event, agent, `tool_use_id`, tool, normalized path, offset and limit, the lines returned,
  error, skill, rule file, load reason, trigger file.
- **Guard.** A `PreToolUse` hook on writing and shell tools denies writes outside the slot and
  commands that build, push, touch worktrees or reach the network.
- **Runs.** Headless `claude -p --output-format stream-json --verbose` with a session id, the
  settings file, the model and permission mode the developer really uses, a budget cap and a
  timeout. A run that ends with a question and no write is resumed with one fixed approval line.
  Keep the stream, the hook log, the diff and the cost.
- **Prompts.** Real tasks in the developer's language and style that never mention the design or
  the rules. Several paraphrases per scenario. Scenarios cover every thin rule, the area-pointer
  rule, the task-table rows, and a new project started by path.
- **Obligations.** Before the first run, freeze a table from the committed rules and base file:
  rule glob → design file and the line range of its section; task row → files.
- **Checker.** Deterministic, per run and per agent context (the main session and each
  subagent). For each Write or Edit in the checkout, the due set is the design file of every thin
  or area rule whose glob matches, plus the scenario's task-row files before the first code write.
  Each due file must have been read successfully in an earlier assistant message, covering the
  section's lines; a "file unchanged" read refers back to the earlier one. It also reports extra
  design reads, shell reads of area files, foreign skills and runs that never attempted the task.
- **Grader.** A separate agent gets the rubrics, the checker output and the diffs, not the
  transcripts, and judges only whether each change matches its rubric.
- **Failures.** Classify each: rule never loaded, hop skipped, trigger not recognized, task row
  wrong, partial read, subagent wrote, foreign skill, shell bypass, no attempt. Fix the artifact
  the class names, then re-run the scenarios sharing it on fresh paraphrases.
- **Cleanup.** Remove the slot worktrees and their folders under `~/.claude/projects/`, then check
  the real memory's hash again.
