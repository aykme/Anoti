# Manual regression — none

No separate manual testing is needed for this module. Everything in it is a marker the compiler
reads while the app is being built: it produces no behavior of its own at runtime, draws
nothing, and has no sequence a tester could run to reach it. A fault here stops the app from
being built at all rather than showing up on a device.

What the markers end up wiring together is checked in
[`core-kmp:di-app`](../di-app/CORE-KMP-DI-APP-REGRESS.md)'s file.
