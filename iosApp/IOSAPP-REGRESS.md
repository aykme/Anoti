# Manual regression — iOS app

This file checks what the iOS app's Swift host sets on its own: the launch screen, the status
bar's style and the icon. Everything else the iPhone shows comes from the shared modules and is
checked in their files. Rotation, the system bars in both themes, and the state kept or dropped
across a termination are in [the app shell's file](../main/MAIN-REGRESS.md): see "Rotation",
"System bars and the window" and "Leaving and coming back" there.

Every step can be walked in a Simulator: nothing here needs hardware. Start from a clean
installation with the app never opened before.

## The icon and the name

1. Install the app and look at the home screen.
   The app's icon shows the Anoti artwork, filling the rounded square with no white border, and
   the name under it reads "Anoti".
2. Open the app switcher with the app open.
   The same icon and name sit above the app's card.

## Launch

1. With the app fully closed, open it from the home screen and watch until the list appears.
   The screen is black from the first frame. Nothing white or gray flashes before the list.
2. Do the same with the device set to light theme, then dark theme.
   The launch looks the same both times.

## The status bar

1. Open the app on any screen, in light theme and then in dark theme.
   The clock, signal and battery are white over the dark screen both times.
