# Manual regression — app shell

This module is the shell the app runs inside: which screen opens, how the two sections are
reached, how the window sits against the system bars and the keyboard, and what survives
leaving the app and coming back. The content of the two sections, the look of the bottom bar
and the look of the permission dialog are each checked in their own module's file.

Unless a step says otherwise, start from a clean installation with the app never opened before.

## First launch

1. Install the app and open it from the launcher.
   The anime list opens. The bottom bar shows two items, and the left one is the selected one.
2. The system asks whether the app may send notifications, on top of the list.
   Choose **Allow**.
   The question disappears and the list stays where it was.
3. Close the app and open it again from the launcher.
   The question does not come back.
4. Uninstall, install again and open it. When the system asks, choose **Don't allow**.
   The question disappears and the list stays where it was.
5. Close the app and open it again.
   The app explains in its own dialog why it wants notifications, with a refusing button and an
   accepting one.
6. Press the refusing button.
   The dialog closes. Nothing else appears. The list is usable.
7. Close the app and open it again, then press the accepting button on the dialog.
   The system's own notification question appears on top.
8. Repeat step 4 on a device running Android 10, 11 or 12.
   The system never asks its own question. Instead, on the second launch the app's dialog
   appears, and its accepting button opens this app's page in the system settings, on the
   screen where notifications are switched on and off.

## Opening from a new-episode notification

Requires at least one saved anime that has a new episode, so a notification actually arrives.

1. With the app fully closed, pull down the notification shade and tap the new-episode
   notification.
   The app opens on the favorites section. The right bottom-bar item is the selected one.
2. Leave the app open on the anime list. Send the app to the background with the home button.
   Tap the new-episode notification.
   The app comes to the front on the favorites section, not on the list.
3. From there, press system back until the app closes. Open the app from the launcher.
   The app opens on the anime list, not on favorites.

## Moving between the two sections

1. From the anime list, tap the right bottom-bar item.
   The favorites section replaces the list. The right item becomes the selected one and the
   left one stops being selected.
2. Tap the left bottom-bar item.
   The anime list comes back and the left item is selected again.
3. Tap the left item once more, while the list is already open.
   Nothing moves. The list stays exactly where it was, including how far it was scrolled.
4. Scroll the anime list down, switch to favorites, then switch back.
   The list is reloaded from the top — it does not keep the previous scroll position.

## System bars and the window

1. Open the app on any screen.
   The content runs to the very top and bottom edges of the screen. The status bar has no
   background of its own — the screen's own content shows through behind the clock and icons.
2. Look at the clock, battery and signal icons in the status bar.
   They are light against the dark content behind them, on a device set to light theme and on
   one set to dark theme alike.
3. Look at the area below the bottom bar, where the system's navigation gesture bar sits.
   It is solid black, matching the bottom bar above it. There is no lighter strip and no
   translucent overlay between them.
4. Switch the device to gesture navigation, then to three-button navigation.
   In both cases the bottom bar's two items stay fully visible and tappable, and nothing of the
   app is hidden underneath the system navigation.

## Rotation

1. On a phone, open the app and turn the device on its side, with the device's own rotation
   lock off.
   The app stays upright. It does not turn.
2. On a tablet or an unfolded foldable, do the same.
   The app turns with the device. The bottom bar stretches across the wider edge, still with
   its two items, and the section that was open stays open.
3. Turn it back.
   The app returns to the upright layout and the same section is still open.

## The keyboard

1. Go to the anime list and open its search input, so the keyboard appears.
2. While the keyboard is up, do something that makes the app show a message strip, such as
   turning the network off and letting a request fail.
   The message strip sits directly above the keyboard. It is not hidden behind it.
3. Close the keyboard while a message strip is showing.
   The strip moves down and comes to rest directly above the bottom bar, not behind it and not
   overlapping it.

## Leaving and coming back

1. Open favorites. Press home. Open several other heavy apps, then return to this app from the
   recents list.
   The app comes back on favorites, with the right bottom-bar item selected.
2. Open favorites. Press home. In the device's developer options turn on "Don't keep
   activities", then return to the app from recents.
   The app comes back on favorites, not on the anime list. Turn the setting back off afterward.
3. Open favorites. Press home. Force-stop the app from the system settings. Open it again from
   the launcher.
   The app opens on the anime list — a fresh start, not a restored one.

## Back

1. On the anime list, press system back.
   The app closes and the launcher appears.
2. Switch to favorites, then press system back.
   The app closes. It does not step back to the anime list first.

## The rest of the device

1. Switch the device to dark theme, then to light theme, with the app open.
   The app looks the same in both. It is dark either way, and nothing turns white or becomes
   unreadable.
2. In the device's accessibility settings raise the font size to the largest setting and open
   the app.
   The bottom bar's two labels stay on one line each and stay readable. Neither item's label
   runs into the other.
3. Turn the network off completely and open the app from a clean start.
   The app opens on the anime list and shows its own failure state. It does not close by
   itself and shows no system crash dialog.
