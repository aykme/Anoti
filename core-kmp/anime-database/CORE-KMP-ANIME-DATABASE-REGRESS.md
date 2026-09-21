# Manual regression — the saved anime store

This module is where the app keeps the anime a person saved: which ones they follow, how many
episodes they have watched, and which have a new episode waiting. Every screen reads the same
copy of that list, so the checks here are about the list itself — that it reads the same
everywhere, that it survives, and that nothing is lost or duplicated. How each screen draws it,
and what the buttons on it feel like, are checked in those screens' own files.

Unless a step says otherwise, start from a clean installation, with the app online and opened at
least once.

**How to open an anime's extra information**, needed below: on **Favorites**, either press and
hold anywhere on the anime's card, or tap the small icon next to its rating.

**Beware of pull-to-refresh on Favorites.** It clears the "new episode" mark on *every* saved
anime at once. Do not use it in the middle of the last section.

## One list, shared by every screen

1. On the main screen, switch on the bell on the first anime, then open **Favorites**.
   The same anime is listed there.
2. Go back to the main screen without closing the app.
   The bell on that anime is still switched on.
3. On **Favorites**, switch the bell off on that anime, then return to the main screen.
   The bell on that anime is off there too, and **Favorites** no longer lists it.
4. Save three different anime, open **Favorites** and switch one of them off.
   Only that one disappears. The other two stay, unchanged.
5. Tap the same bell five times in a row as fast as you can, then wait two seconds.
   The app does not crash, the anime is never listed twice on **Favorites**, and the bell and the
   list agree with each other — whichever state they settle on, they settle on the same one.
   (The bell only ever shows what is really saved, so it does not follow taps faster than the
   save can finish.)
6. Save an anime, then switch its bell on again wherever it can still be tapped.
   **Favorites** lists it exactly once.

## The list survives

1. Save two anime, then close the app fully and open it again.
   Both are still in **Favorites**, and their bells are still on.
2. With two saved anime, restart the device, then open the app.
   Both are still there.
3. Save an ongoing anime, open its extra information, raise its watched-episode count above zero,
   close the app fully and open it again. Open its extra information again.
   The count is the number you left it at, not zero.
4. With saved anime, turn the device's network off and open the app, then open **Favorites**.
   The saved anime are listed. A saved list does not need a connection to be read.
5. Save an anime, then remove it, then save it again.
   Its watched-episode count is back at zero. Removing an anime from **Favorites** does not keep
   anything about it.

## Updating, reinstalling and removing the app

1. Install an older build, save two anime and raise the watched-episode count on one of them.
2. Install the newer build over it, without uninstalling, and open **Favorites**.
   Both anime are still listed, with the watched count unchanged. Nothing is emptied and the app
   does not crash on first open.
3. Uninstall the app, install it again, and without saving anything open **Favorites**.
   The screen shows its empty state, not an error, and the app does not crash.
4. Save an anime, uninstall the app and install it again. On a device with system backup switched
   on, trigger a backup and a restore.
   The saved anime comes back with its watched count. On a device without backup, the list is
   empty and the app works normally.

## A new episode

Requires a saved anime whose next episode is released while the test runs. Do not pull to refresh
anywhere in this section until the last step.

1. Wait for the new-episode notification to arrive and do not open the app.
2. Open the app and go to **Favorites**.
   That anime is marked as having a new episode.
3. Close the app fully without touching that anime, open it again and return to **Favorites**.
   The mark is still there. It is not cleared just by the app being opened.
4. Tap that anime, then leave **Favorites** and come back.
   The mark is gone, and it is still gone after the app is closed fully and opened again.
5. With two saved anime both marked as having a new episode, pull down on the **Favorites** list
   to refresh it.
   Both marks are cleared at once. This is deliberate: a refresh means the whole list has been
   looked at.
