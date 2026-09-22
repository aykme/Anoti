# Anime notification external — manual regression

No separate manual testing is needed for this module.

It holds one contract and no code that runs. The contract lets the notification module open a
screen without depending on the module that owns the screens. Both halves of that arrangement
are already checked elsewhere: tapping a notification and landing on the favorites screen is
checked in the anime notification module's file, and the implementation behind it is checked in
`main`'s file.

There is nothing this module adds that a tester could observe on its own, so there are no steps
here. If it ever gains code that runs, this file gets the steps for it.
