# Phase 1 questions

Ask all of them at once, before the spec. Each comes with a recommended answer and its
trade-off; skip the ones the brief already settled. After the answers, the walk does not stop.

| Question | Usual recommendation |
|---|---|
| Which build is walked? | The build users get: release settings, shrunk and obfuscated where the platform does it, signed with a debug key if needed to install. |
| Which devices, and which role does each play? | Emulators or simulators only. One per screen class: phone, foldable, tablet, plus the oldest OS level the app supports. Give each device the passes it alone can show. |
| Which old build is used for the upgrade steps? | The previous release, from its release branch or tag. It doubles as the reference for "was it always like this". |
| How is periodic background work checked? | Forced through its scheduler's own state for most passes, and one real pass left to run untouched. |
| What happens to steps no tool can reach? | Marked not verified, with the reason. No fake substitute. |
| Are screen-reader steps walked? | Only the screen-reader steps themselves, then the reader off and the app restarted. |
| Can steps be judged from screenshots? | Yes, with video where something moves or flashes. |
| Where does the media go? | A folder the developer names, one subfolder per run and device. Never deleted. |
| What form does the report take? | A page with problems only, by severity, each with a screenshot and a proposal, plus a short chat summary. |
| App bugs and script errors found: report or fix? | Report only. Fix after triage, on a separate branch. |
| Temporary walk code (scripts, test hooks): kept or deleted? | Deleted with its branch once the report is done. |
| May the walk branch be pushed for CI? | Ask. Push only with a yes. |

Before offering options, learn what is available. Don't propose paid services or new hardware
when the constraint is not yet known.
