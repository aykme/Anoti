# Anime notification external — manual regression

No separate manual testing is needed for this module.

It holds two contracts and no code that runs. They let the notification module open a screen
without depending on the module that owns the screens, one for each platform's notifications.
Both halves of that arrangement are already checked elsewhere. Tapping a notification and
landing on the favorites screen is checked in the anime notification module's file. The
implementations behind it are checked in `main`'s file.

There is nothing this module adds that a tester could observe on its own, so there are no steps
here. If it ever gains code that runs, this file gets the steps for it.
