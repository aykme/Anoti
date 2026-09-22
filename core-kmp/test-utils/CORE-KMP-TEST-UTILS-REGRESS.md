# Manual regression — none

No separate manual testing is needed for this module. Nothing in it reaches the installed app:
it is only used while the automated tests run, and it is not part of what a person can open on a
device. A fault here makes those tests fail or hang, which is visible in the test run itself.
