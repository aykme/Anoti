# Manual regression — shared UI and behavior

This module owns pieces the whole app reuses: the colors every screen is painted in, the
spinner shown while something loads, the message banner shown when something fails, the way
dates are written out, the press-and-hold buttons, and the paging that loads a list one page at
a time. Each is described in full here, with the screens a tester can reach it on. What a screen
does with the piece afterward is checked in that screen's own file.

Unless a step says otherwise, start with the app online and opened at least once.

**How to open an anime's extra information**, needed by several sections below: on **Favorites**,
either press and hold anywhere on the anime's card, or tap the small icon next to its rating.
Either one opens and closes it.

## Colors and readability

1. Open the app on a device with the system set to dark mode.
   The background is black. Text on it is white and readable. The selected item of the bottom bar
   is a red-orange.
2. Switch the system to light mode, close the app fully and open it again.
   The app looks exactly the same — black background, white text, the same red-orange. It has one
   palette and does not follow the system setting.
3. On the main screen and on **Favorites**, read every line of text on screen, including the
   anime titles, the episode counts, the release status and the score.
   Every one of them is legible. None is drawn in a color close enough to its background to
   disappear.

## The loading spinner

1. Turn the device's network off, close the app fully and open it.
   The main screen shows a spinner and then settles into its error state. The spinner is gone.
2. Turn the network back on and refresh the main screen.
   A spinner fills the screen while the list loads, turns clockwise at a steady speed without
   stopping or jumping, and disappears when the anime arrives.
3. Throttle the connection hard — for example, set the emulator's network speed to the slowest
   setting — and refresh again.
   The spinner keeps turning at the same steady speed for the whole wait.

## The message banner

The banner appears at the **bottom** of the screen, just above the bottom bar, and goes away by
itself after a few seconds.

1. Turn the device's network off and refresh the main screen.
   A banner reading **Connection error** appears above the bottom bar, then disappears on its own.
2. Cause a second failure while the first banner is still on screen — refresh again.
   The banner on screen is replaced by the new one. A second banner does not stack above or below
   it.
3. Cause a failure, and while the banner is showing, switch to **Favorites**.
   The banner stays on screen and finishes its own time there. It belongs to the app, not to the
   screen it appeared on.
4. Turn the network back on and refresh.
   No banner appears.

## Dates

1. On **Favorites**, open the extra information of a saved ongoing anime whose next episode date
   is known.
   The date reads as a day, a three-letter month and a four-digit year — for example
   `5 Jan 2026`. A single-digit day has no leading zero.
2. Find a saved anime whose next episode date is not known — an announced one, or one that has
   finished airing — and open its extra information.
   The field reads exactly **No data**. It is never blank, and never a raw string with dashes and
   a `T` in it.

## Press and hold

Do these on **Favorites**, with the anime's extra information open. Use an **ongoing** anime that
has aired far more episodes than the count shows, so the buttons have room to work.

1. Tap the plus button once and let go.
   The count goes up by exactly one. It does not go up twice.
2. Press and hold the plus button.
   The count goes up once immediately, pauses briefly, then climbs at a steady rate for as long as
   you hold.
3. Release.
   The count stops immediately. It does not creep up afterward.
4. Press and hold again, then drag your finger off the button before releasing.
   The count stops the moment the finger leaves the button.
5. Press and hold the plus button and, while holding, tap elsewhere on the same card with another
   finger.
   Only the count changes. The card itself does not also react as if it had been tapped.
6. Turn on the device's screen reader and double-tap the plus button.
   The count goes up by one, and the button is announced as a button.
7. Keep holding plus until the count stops climbing on its own.
   It stops at the number of episodes that have aired and goes no further. Holding longer changes
   nothing and nothing crashes.
8. On an **announced** anime — one with no aired episodes — press and hold plus.
   The count stays at zero. The button does nothing at all, and the app does not crash.

## Loading further pages

1. On the main screen, scroll to the bottom of a section list.
   More anime load and are appended below the ones already there. Nothing already on screen is
   replaced or reordered.
2. Keep scrolling to the bottom until no more arrive.
   The list stops growing, nothing flickers, and the app does not crash.
3. Turn the network off, then scroll to the bottom of a list that still has more to load.
   The **Connection error** banner appears and everything already loaded stays on screen.
4. Turn the network back on and scroll to the bottom again.
   The next page loads, and it is the page that failed — no block of anime is missing between
   what was there before and what has just arrived.
5. Refresh the list from the top while more anime are still loading at the bottom.
   The list comes back as a fresh first page, and scrolling down from there loads pages in order,
   with nothing skipped and nothing repeated.

## Away from the system bars

The app is locked to portrait on a phone, so these need a tablet or a foldable opened flat.

1. Open the app on a device with a notch or a punch-hole camera.
   No text or control is hidden behind the status bar or the cutout.
2. Turn the device to landscape.
   No text or control is hidden behind the side system bars, on either side, and nothing is cut
   off at the edges.
