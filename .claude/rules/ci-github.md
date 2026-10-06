---
paths:
  - "**/.claude/rules/ci-github.md"
  - "**/.github/**"
---

# CI on GitHub

The repository, the remote and the `gh` path are in [CLAUDE.md](../../CLAUDE.md). What each
workflow checks is in [android/ci.md](android/ci.md) and [ios/ci.md](ios/ci.md).

- Two workflows run on GitHub Actions: `android.yml` on Linux and `ios.yml` on macOS. Both run on
  every push to `develop`. A push to any other branch starts neither; there they run only on the
  developer's word.
- To start a workflow by hand, push the branch first (a push needs the developer's word, as
  `CLAUDE.md` says), then run `gh workflow run <android.yml|ios.yml> -R aykme/Anoti --ref <branch>`.
- To find that run, take the newest dispatch on the branch:
  `gh run list -R aykme/Anoti --workflow <file> --branch <branch> --event workflow_dispatch
  --limit 1`. For a run a push started, filter by `--commit <sha>`, the full SHA, instead. A run
  shows up a few seconds after it starts.
- Wait for it with `gh run watch <run id> -R aykme/Anoti --interval 60`, or in the background: an
  iOS run takes 25 to 50 minutes, most of it linking the framework, and a Release run about 15.
  `gh run view <run id> -R aykme/Anoti` shows the jobs, and `--log-failed` the failed steps.
- A job's whole log comes from `gh api --allow-escape-sequences
  repos/aykme/Anoti/actions/jobs/<job id>/logs`. It is there only once the job has ended.
- Artifacts come down with `gh run download <run id> -R aykme/Anoti -n <name> -D <folder>`. Where
  an iOS run's artifacts go is in [ios/ci.md](ios/ci.md).
