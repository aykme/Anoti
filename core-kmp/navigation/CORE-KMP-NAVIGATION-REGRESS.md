# Manual regression — moving between the app's screens

This module owns which screen the app is showing and the switch between them. There are two:
the main screen and **Favorites**. Only one is ever kept — switching away throws the previous
one away rather than stacking it behind. What each screen itself contains is checked in its own
file.

Unless a step says otherwise, start with the app online and opened at least once.

## Where the app starts

1. Install the app and open it for the first time.
   The main screen is showing, and **Main** is the selected item in the bottom bar.
2. Close the app fully and open it again.
   The main screen is showing again. The app always starts there, whichever screen was open
   last.

## Switching

1. Tap **Favorites** in the bottom bar.
   **Favorites** replaces the main screen, and **Favorites** becomes the selected item in the
   bottom bar.
2. Tap **Main**.
   The main screen comes back and **Main** is selected again.
3. Switch back and forth ten times, quickly.
   The screen shown always matches the selected item in the bottom bar. No screen is drawn on
   top of another and the app does not crash.
4. Tap the item that is already selected.
   Nothing happens: the screen does not flash, reload or reset.

## The system back button

1. On the main screen, press the system back button.
   The app closes and the launcher is shown.
2. Open the app, switch to **Favorites**, then press back.
   The app closes. It does not go back to the main screen — the switch replaced it rather than
   stacking it.
3. Open the app again from the launcher.
   The main screen is showing.

## Rotation and being sent to the background

1. On **Favorites**, rotate the device.
   **Favorites** is still showing afterwards, and **Favorites** is still the selected item.
2. On **Favorites**, press the home button, open two or three other apps, then return to Anoti
   from the recents list.
   **Favorites** is still showing.
3. In the system developer options, switch on **Don't keep activities**. Open the app, go to
   **Favorites**, press home, then return to the app from recents.
   **Favorites** is still showing, not the main screen, and the app does not crash. Switch the
   setting back off afterwards.
