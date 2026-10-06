# Manual regression — app shell

This module is the shell the app runs inside: which screen opens, how the two sections are
reached, how the window sits against the system bars and the keyboard, and what survives
leaving the app and coming back. The content of the two sections, the look of the bottom bar
and the look of the permission dialog are each checked in their own module's file.

Unless a step says otherwise, start from a clean installation with the app never opened before.
Steps run on both platforms unless they name one.

## First launch

1. Install the app and open it from the home screen.
   The anime list opens. The bottom bar shows two items, and the left one is the selected one.
2. Android 13 and later, and iPhone: the system asks whether the app may send notifications, on
   top of the list. Android 10 to 12 never ask; step 9 covers them. Choose **Allow**.
   The question disappears and the list stays where it was.
3. Close the app and open it again.
   The question does not come back.
4. Uninstall, install again and open it. When the system asks, choose **Don't allow**.
   The question disappears and the list stays where it was.
   iPhone: if the question does not come back after the reinstallation, erase the Simulator or use a
   device that never had the app.
5. Close the app and open it again.
   The app explains in its own dialog why it wants notifications, with a refusing button and an
   accepting one.
6. Press the refusing button.
   The dialog closes. Nothing else appears. The list is usable.
7. Close the app and open it again, then press the accepting button on the dialog.
   Android 13 and later: the system's own notification question appears on top.
   iPhone: the Settings app opens on this app's notification settings, since iOS asks only once.
   A Simulator may open the Settings start page instead, so check this on a device.
8. Android 13 and later only: when the system's question appears, refuse it again. Close the
   app and open it again.
   Neither the system's question nor the app's dialog appears. Android stops asking after a
   second refusal.
9. Android 10 to 12 only, since they never ask: install fresh, open the app once and close it.
   Turn this app's notifications off in the system settings, then open the app again.
   The system asks nothing. The app's dialog appears, and its accepting button opens this app's
   notification settings, on the screen where notifications are switched on and off.

## Opening from a new-episode notification

Requires at least one saved anime that has a new episode, so a notification actually arrives.
When several notifications have arrived, the system may show them as one group. Expand it
and tap one notification in it.

1. With the app fully closed, open the notification list and tap the new-episode notification.
   The app opens on the favorites section. The right bottom-bar item is the selected one. On
   the iPhone another screen may show for a moment first.
2. Go to the home screen, then open the app from the launcher or the home screen.
   The app comes back on favorites, as it was left. It does not open a second, fresh anime
   list.
3. Leave the app open on the anime list. Go to the home screen. Tap the new-episode notification.
   The app comes to the front on the favorites section, not on the list.
4. Leave the app open on favorites, scrolled down, with one item showing its extra info. Go to
   the home screen and tap the notification.
   The app comes to the front on favorites exactly as it was left: the same scroll position, the
   same item still showing its extra info. Nothing reloads and no screen animates in.
5. Android only, since the iPhone has no back button: from there, press system back until the
   app closes. Open the app from the launcher.
   The app opens on the anime list, not on favorites.
6. From there, close the app the way a user does: swipe it away in recents or in the app
   switcher. Open it again.
   The app opens on the anime list, not on favorites.
7. Android 10 to 12 only, where the app itself opens its notification settings. Open them from
   the app's dialog as in step 9 of "First launch", and switch notifications on there. Leave the
   settings open and tap a new-episode notification once one arrives.
   The settings close and the app shows favorites. Pressing system back from there closes the
   app; it does not return to the settings.
8. Open the anime list and type something into its search. Go to the home screen and end the app
   the way the system does, as in step 2 of "Leaving and coming back". Tap the notification.
   The app opens on favorites with the right bottom-bar item selected. Switch to the list and
   open its search: the field is empty, and no old text comes back.

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

The app is dark whatever theme the device is set to. Walk steps 1–3 and 5–6 twice: first with
the device set to light theme, then set to dark theme. Every result is the same both times. A
light area in the light theme is a bug, even when the dark theme looks right.

1. Open the app on any screen, scrolled to the top.
   The content runs to the very top and bottom edges of the screen. The status bar has no
   background of its own — the screen's own content shows through behind the clock and icons.
   With the screen at its top, the area behind them is dark.
2. Look at the clock, battery and signal icons in the status bar.
   They are light against the dark area behind them.
3. Look at the bottom bar and the area below it, down to the bottom edge of the screen.
   The bottom bar is dark, and the area below it is black, matching the bar above it. There is
   no lighter background and no translucent overlay between them. Only the system's own
   navigation controls are drawn over that area.
4. Android only, since the iPhone has one way to navigate: switch the device to gesture
   navigation, then to three-button navigation.
   In both cases the bottom bar's two items stay fully visible and tappable, and nothing of the
   app is hidden underneath the system navigation.
5. Close the app fully, then open it from the home screen and watch until the list appears.
   The screen is dark from the first moment. No white or light screen flashes before the list.
6. Open the app, go to the home screen, then open the recent apps: Android's recents button or
   gesture, or the iPhone's app switcher (swipe up from the bottom edge and hold).
   The app's card shows its dark screen with a dark background. It is not a light card.

## Rotation

1. On a phone, folded if it folds, open the app and turn the device on its side, with the
   device's own rotation lock off.
   The app stays upright. It does not turn.
2. On a wide screen, do the same: a tablet, an iPad, or a foldable phone or iPhone unfolded.
   The app turns with the device. The bottom bar stretches across the wider edge, still with
   its two items, and the section that was open stays open.
3. Turn it back.
   The app returns to the upright layout and the same section is still open.
4. On a tablet or an iPad, set the font size and the display size to their largest, then repeat
   step 2.
   The app still turns with the device and fills the whole screen. No black bars appear at its
   sides.

## Folding and unfolding

On a foldable only. Set it to keep apps open on the outer screen when it folds, so folding does
not lock it.

1. Open the anime list, fold the device, then unfold it.
   The same section stays open and the bottom bar keeps its selected item. Folded, the app stays
   upright when the device turns. Unfolded, it turns with the device.
2. Do the same on favorites.
   Favorites stays open, with the right bottom-bar item selected.
3. Android only, since the iPhone sets one display size for the whole device: give the outer and
   the inner screen different display sizes, then repeat steps 1 and 2.
   The results are the same. Nothing reloads, and the screen does not flash empty.

## The keyboard

1. Go to the anime list, open its search and tap the search field, so the keyboard appears.
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
   - Android, a second time with the setting off: end the process with
     `adb shell am kill com.alekseivinogradov.anoti`, then return to the app from recents.
   - iPhone: with the app started from Xcode, go to the home screen first and wait five seconds.
     Only then stop it in Xcode, and open it again from the home screen. The run before this one
     must also have ended in the background, not on screen.

   The app comes back on favorites, not on the anime list, and the list where it was scrolled.

   iPhone: iOS keeps the screen's state only for an app the system ended in the background. An
   app ended while on screen counts as a crash or a force quit: stopped from Xcode while open,
   crashed, or swiped away. Then iOS drops the kept state, and the next launch opens on the anime
   list. The run after it drops its state too, so walk the step once more from a fresh launch.
   On a Simulator that has run the app's UI tests, iOS keeps no state at all until the Simulator
   restarts, so restart it before this step.
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
   The app looks the same in both, the bottom bar's colors, text and icons included. It is dark
   either way, and nothing turns white or becomes unreadable. The system bars are checked in both
   themes under "System bars and the window".
2. In the device's accessibility settings raise the font size to the largest setting and open
   the app.
   The bottom bar's two labels stay on one line each and stay readable. Neither item's label
   runs into the other.
3. Turn the network off completely and open the app from a clean start.
   The app opens on the anime list and shows its own failure state. It does not close by
   itself and shows no system crash dialog.

## Platforms

- Asking for the permission again. Android 13 and later ask a second time after one refusal:
  the app's dialog appears on the next launch, and its accepting button brings back the system's
  question. After a second refusal they stop asking, and the dialog no longer appears. Android
  10 to 12 never ask: the dialog appears while notifications are off, and its accepting button
  opens the notification settings. The iPhone asks once. After a refusal, every launch from a
  closed app shows the app's dialog, and its accepting button opens Settings.
- A cold notification tap. Android opens favorites directly. The iPhone may show another screen
  for a moment first.
- Dark mode. Android rebuilds the screen and restores its state. On the iPhone the screen stays
  as it is.
- A new text size. Android rebuilds the screen with it. The iPhone app shows it only after it is
  closed and opened again.
- After a reboot. Android opens on the anime list. The iPhone may reopen on the screen it was
  left on.
- After an app update. The iPhone opens on the anime list, since a state another build saved is
  dropped.
- System back. Android closes the app. The iPhone has none.
