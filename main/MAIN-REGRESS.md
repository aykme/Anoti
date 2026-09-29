# Manual regression — app shell

This module is the shell the app runs inside: which screen opens, how the two sections are
reached, how the window sits against the system bars and the keyboard, and what survives
leaving the app and coming back. The content of the two sections, the look of the bottom bar
and the look of the permission dialog are each checked in their own module's file.

Unless a step says otherwise, start from a clean installation with the app never opened before.
Steps run on both platforms unless they name one. The iPhone steps are unverified until the
iPhone app has been built and run.

## First launch

1. Install the app and open it from the home screen.
   The anime list opens. The bottom bar shows two items, and the left one is the selected one.
2. The system asks whether the app may send notifications, on top of the list.
   Choose **Allow**.
   The question disappears and the list stays where it was.
3. Close the app and open it again.
   The question does not come back.
4. Uninstall, install again and open it. When the system asks, choose **Don't allow**.
   The question disappears and the list stays where it was.
5. Close the app and open it again.
   The app explains in its own dialog why it wants notifications, with a refusing button and an
   accepting one.
6. Press the refusing button.
   The dialog closes. Nothing else appears. The list is usable.
7. Close the app and open it again, then press the accepting button on the dialog.
   Android 13 and later: the system's own notification question appears on top.
   iPhone: the Settings app opens on this app's notification settings, since iOS asks only once.
8. Android only, since only Android 10 to 12 lack the question: repeat step 4 on a device running
   Android 10, 11 or 12.
   The system never asks its own question. Instead, on the second launch the app's dialog
   appears, and its accepting button opens this app's page in the system settings, on the
   screen where notifications are switched on and off.

## Opening from a new-episode notification

Requires at least one saved anime that has a new episode, so a notification actually arrives.

1. With the app fully closed, open the notification list and tap the new-episode notification.
   The app opens on the favorites section. The right bottom-bar item is the selected one. On
   the iPhone the screen the app was saved on may show for a moment first.
2. Leave the app open on the anime list. Go to the home screen. Tap the new-episode notification.
   The app comes to the front on the favorites section, not on the list.
3. Leave the app open on favorites, scrolled down. Go to the home screen and tap the
   notification.
   The app comes to the front on favorites, rebuilt: the list is back at the top.
4. Android only, since the iPhone has no back button: from there, press system back until the
   app closes. Open the app from the launcher.
   The app opens on the anime list, not on favorites.
5. iPhone only: from there, close the app in the app switcher and open it again.
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
3. Look at the area below the bottom bar: Android's navigation bar, or the iPhone's home
   indicator.
   It is solid black, matching the bottom bar above it. There is no lighter strip and no
   translucent overlay between them.
4. Android only, since the iPhone has one way to navigate: switch the device to gesture
   navigation, then to three-button navigation.
   In both cases the bottom bar's two items stay fully visible and tappable, and nothing of the
   app is hidden underneath the system navigation.

## Rotation

1. On a phone, folded if it folds, open the app and turn the device on its side, with the
   device's own rotation lock off.
   The app stays upright. It does not turn.
2. On a wide screen, do the same: a tablet, an iPad, or a foldable phone or iPhone unfolded.
   The app turns with the device. The bottom bar stretches across the wider edge, still with
   its two items, and the section that was open stays open.
3. Turn it back.
   The app returns to the upright layout and the same section is still open.

## The keyboard

1. Go to the anime list and open its search input, so the keyboard appears.
   The search input stays where it is. The screen does not slide up as a whole.
2. While the keyboard is up, do something that makes the app show a message strip, such as
   turning the network off and letting a request fail.
   The message strip sits directly above the keyboard. It is not hidden behind it.
3. Close the keyboard while a message strip is showing.
   The strip moves down and comes to rest directly above the bottom bar, not behind it and not
   overlapping it.

## Leaving and coming back

1. Open favorites and scroll it down. Go to the home screen. Open several other heavy apps, then
   return to this app from the recent apps.
   The app comes back on favorites, with the right bottom-bar item selected and the list where
   it was scrolled.
2. Open favorites and scroll it down. Go to the home screen. Then end the app the way the system
   does:
   - Android: in the device's developer options turn on "Don't keep activities", then return to
     the app from recents. Turn the setting back off afterward.
   - iPhone: with the app started from Xcode, stop it in Xcode, then open it again from the home
     screen.

   The app comes back on favorites, not on the anime list, and the list where it was scrolled.
3. Open favorites. Go to the home screen. Close the app the way the user does:
   - Android: force-stop the app from the system settings, then open it from the launcher;
   - iPhone: swipe the app away in the app switcher, then open it from the home screen.

   The app opens on the anime list — a fresh start, not a restored one.

## Back

Android only, since the iPhone has no back button.

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

## Platforms

| What | Android | iPhone |
| --- | --- | --- |
| Asking for the permission again | Android 13+ asks again after one refusal | asks once; a refusal leads to the app's dialog and Settings |
| A cold notification tap | opens favorites directly | may show the saved screen for a moment first |
| Dark mode or text size changes | the screen is rebuilt and its state restored | the screen stays as it is |
| After a reboot | the saved screen is gone | may come back, since the window session can survive |
| A launch the system starts in the background | not applicable | the state it restores is lost if the system then ends the app |
| System back | closes the app | none |
