# Manual regression — iOS app

This file checks what the iOS app's Swift host sets on its own: the icon and the name. The launch
screen and the status bar are the host's too. They are checked in both themes in
[the app shell's file](../main/MAIN-REGRESS.md), steps 2 and 5 of "System bars and the window".
Rotation and the state kept across a termination are there as well, under "Rotation" and
"Leaving and coming back".

Every step can be walked in a Simulator: nothing here needs hardware. Start from a clean
installation with the app never opened before.

## The icon and the name

1. Install the app and look at the home screen.
   The app's icon shows the Anoti artwork with no white border, and the name under it reads
   "Anoti".
2. Open the app switcher with the app open.
   The same icon and name show with the app's card.
