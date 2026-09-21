# Manual regression — the saved anime store

This module is where the app keeps the anime a person saved: which ones they follow, how many
episodes they have watched, and which have a new episode waiting. Every screen reads the same
copy of that list, so the checks here are about the list itself — that it reads the same
everywhere, that it survives, and that nothing is lost or duplicated. How each screen draws it
is checked in that screen's own file.

Unless a step says otherwise, start from a clean installation, with the app online and opened at
least once.

## One list, shared by every screen

1. On the main screen, switch on the bell on the first anime, then open **Favorites**.
   The same anime is listed there.
2. Go back to the main screen without closing the app.
   The bell on that anime is still switched on.
3. On **Favorites**, switch the bell off on that anime, then return to the main screen.
   The bell on that anime is off there too, and **Favorites** no longer lists it.
4. Save three different anime, open **Favorites** and switch one of them off.
   Only that one disappears. The other two stay, unchanged.
5. Switch the bell on and off on the same anime five times in a row, as fast as you can tap.
   The bell ends in the state of your last tap. **Favorites** lists that anime if the bell is
   on and does not if it is off, and it is never listed twice.
6. Save an anime, then switch its bell on again wherever it can still be tapped.
   **Favorites** lists it exactly once.

## The list survives

1. Save two anime, then close the app fully and open it again.
   Both are still in **Favorites**, and their bells are still on.
2. With two saved anime, restart the device, then open the app.
   Both are still there.
3. Save an anime, raise its watched-episode count above zero, close the app fully and open it
   again. Open that anime's extra information.
   The count is the number you left it at, not zero.
4. With saved anime, turn the device's network off and open the app, then open **Favorites**.
   The saved anime are listed. A saved list does not need a connection to be read.

## Counting watched episodes

Do these on **Favorites**, with the anime's extra information open.

1. Press the plus button once.
   The count goes up by exactly one.
2. Press and hold the plus button for about five seconds, then release.
   The count climbs while held and stops where it was at release. It does not keep climbing
   afterwards and it does not jump back.
3. Close the app fully, open it again and open that anime's extra information.
   The count is the number you stopped at.
4. Press and hold the minus button for longer than it takes to reach zero.
   The count stops at zero. It never shows a negative number.

## Updating, reinstalling and removing the app

1. Install an older build, save two anime and raise the watched-episode count on one of them.
2. Install the newer build over it, without uninstalling, and open **Favorites**.
   Both anime are still listed, with the watched count unchanged. Nothing is emptied and the app
   does not crash on first open.
3. Uninstall the app, install it again and open **Favorites**.
   On a device with system backup switched on, the saved anime come back. On a device without
   it, the list is empty and the app works normally.
4. Uninstall the app, install it again, and without saving anything open **Favorites**.
   The screen shows its empty state, not an error, and the app does not crash.

## A new episode

Requires a saved anime whose next episode is released while the test runs.

1. Wait for the new-episode notification to arrive and do not open the app.
2. Open the app and go to **Favorites**.
   That anime is marked as having a new episode.
3. Close the app fully without touching that anime, open it again and return to **Favorites**.
   The mark is still there. It is not cleared just by the app being opened.
4. Tap that anime, then leave **Favorites** and come back.
   The mark is gone, and it is still gone after the app is closed fully and opened again.
