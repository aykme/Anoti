# iOS simulators on a CI runner

For walking an iOS app with no Mac at hand. The walk runs as UI tests and scripts on a macOS CI
runner. It is judged afterward from its screenshots, videos and logs.

## Contents

- Shape of an automated walk
- Simulator facts
- Evidence and artifacts
- Failures

## Shape of an automated walk

- Probe first: one short run that tries the tools (permission answers, keyboard, network cut,
  maximum text size, notifications, scene restore) and nothing else.
- Split the walk into parts that run in parallel jobs, each with its own media folder. Give each
  part a judge afterward (`reviews.md`).
- Keep the walk code on its own branch, and review it and its helpers before the first run. Delete
  the branch when the report is done, unless phase 1 decided otherwise.
- Record rather than assert: a step that fails an assertion stops recording what came after.

## Simulator facts

- `xcrun simctl ui <udid> content_size <size>` sets the text size; `appearance` sets dark mode.
- Display Zoom cannot be set on a simulator: no command exists, and the simulator's Settings has
  no Display & Brightness section. Maximum-scale passes run at the largest text size and count as
  approximated.
- A UI test that launches the app loses the app's standard output. To read the app's logs, start it
  with `simctl launch --console` or `--stdout=<file>` and attach the test to it by bundle id.
- After a UI test has launched the app, iOS returns no kept scene state to later launches until the
  simulator restarts. Restart it before restore checks, and let the script, not the test, launch
  the app in them.
- Judge orientation by what the screen shows, not by what the app requests.
- The home indicator and other system chrome may not appear in simulator screenshots.
- `activate()` on an app that is not running launches it, and that launch silently drops the kept
  scene state.
- Record the environment state (network, appearance, text size, permissions) at the start of each
  segment, so a later surprise can be traced.

## Evidence and artifacts

- Screenshots lag a tap by half a second or more, and a few frames per second miss short states.
  Wait for the element, and use video for motion.
- Put videos into their own artifact with a short retention; large media downloads take hours.
- Download each artifact into its own new folder; two downloads into one folder collide.
- Check that the developer's media folder exists under its name before writing to it.
- Prove new tests actually ran from the JUnit or result-bundle counts, not from a green job.

## Failures

- Sort every failed job: the app, the check code, or the CI environment.
- A job cancelled for lack of runners is environment; run it again.
- A tool fault that could have tainted every part (a proxy certificate race, a network rule that
  did not apply) invalidates the whole run.
