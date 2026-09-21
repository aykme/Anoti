# Manual regression — moving between the app's screens

This module owns which screen the app is showing and the switch between them. There are two:
the main screen and **Favorites**. Only one is ever kept — switching away throws the previous one
away rather than stacking it behind. What each screen itself contains is checked in its own file.

Unless a step says otherwise, start with the app online and opened at least once.

## Where the app starts

1. Install the app and open it for the first time from the launcher.
   The main screen is showing, and **Main** is the selected item in the bottom bar.
2. Switch to **Favorites**, close the app fully, and open it again from the launcher.
   The main screen is showing, not **Favorites**. A launcher start always begins at the main
   screen.

## Starting from a notification

Requires a saved anime whose next episode is released while the test runs, so a new-episode
notification arrives.

1. With the app closed, tap the new-episode notification.
   The app opens on **Favorites**, not on the main screen, and **Favorites** is the selected item
   in the bottom bar.
2. Press the system back button.
   The app closes. There is no main screen stacked behind **Favorites** to go back to.
3. Open the app again from the launcher.
   The main screen is showing.
4. With the app already open on the main screen, tap a new-episode notification.
   The app moves to **Favorites**, and the bottom bar follows — the selected item and the screen
   shown are never out of step.

## Switching

1. Tap **Favorites** in the bottom bar.
   **Favorites** replaces the main screen, and **Favorites** becomes the selected item.
2. Tap **Main**.
   The main screen comes back and **Main** is selected again.
3. Switch back and forth ten times, quickly.
   The screen shown always matches the selected item. No screen is drawn on top of another and
   the app does not crash.
4. Tap the item that is already selected.
   Nothing happens: the screen does not flash, reload or reset.

## The system back button

1. On the main screen, press the system back button.
   The app closes and the launcher is shown.
2. Open the app, switch to **Favorites**, then press back.
   The app closes. It does not go back to the main screen — the switch replaced it rather than
   stacking it.

## Being sent to the background

1. On **Favorites**, press the home button, open two or three other apps, then return to Anoti
   from the recents list.
   **Favorites** is still showing.
2. In the system developer options, switch on **Don't keep activities**. Open the app, go to
   **Favorites**, press home, then return to the app from recents.
   **Favorites** is still showing, not the main screen, and the app does not crash.
3. With that setting still on, go to the main screen, press home and return from recents.
   The main screen is showing.
4. Switch **Don't keep activities** back off.
