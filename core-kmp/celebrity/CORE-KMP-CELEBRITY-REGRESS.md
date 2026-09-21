# Manual regression — shared UI and behaviour

This module owns pieces the whole app reuses: the colours every screen is painted in, the
spinner shown while something loads, the error banner shown when something fails, the way dates
are written out, the press-and-hold buttons, and the loading of further pages as a list is
scrolled. Each is described in full here, with the screens a tester can reach it on. What a
screen does with the piece afterwards is checked in that screen's own file.

Unless a step says otherwise, start with the app online and opened at least once.

## Colours and readability

1. Open the app on a device with the system set to dark mode.
   The background is black. Text on it is white and readable. The accent colour — used on the
   selected item of the bottom bar — is a red-orange.
2. Switch the system to light mode while the app is open, then look at the app again.
   The app looks exactly the same. It has one palette and does not follow the system setting.
3. Go through the main screen and **Favorites**.
   No text anywhere is black on black, and no text is invisible against its background.

## The loading spinner

1. Turn the device's network off, open the app and go to the main screen.
   Whatever the screen shows, no spinner is left turning forever once the screen has settled.
2. Turn the network back on and refresh the main screen.
   A spinner appears while the list loads, turns smoothly and clockwise at a steady speed, and
   disappears when the list arrives.
3. Repeat on a slow connection, for example with the device's mobile data throttled.
   The spinner keeps turning the whole time, without stopping and restarting.

## The error banner

1. Turn the device's network off and refresh the main screen.
   A banner appears at the top of the screen reading **Connection error**, then goes away by
   itself.
2. While that banner is on screen, cause a second failure — refresh again.
   The banner on screen is replaced by the new one instead of a second banner stacking below it.
3. Turn the network back on and refresh.
   No banner appears.
4. Cause a failure, then immediately switch to **Favorites** while the banner is showing.
   The banner does not follow you onto the other screen or freeze there.

## Dates

1. Open **Favorites** with a saved ongoing anime and open its extra information.
   The next episode's date reads as a day, a three-letter month and a four-digit year — for
   example `5 Jan 2026`. A single-digit day has no leading zero.
2. Find a saved anime whose next episode date is not known.
   The field shows the screen's own placeholder text, not a blank, not a raw date string with
   dashes and a `T` in it.

## Press and hold

Do these on **Favorites**, on a saved anime with its extra information open.

1. Tap the plus button once and let go.
   The count goes up by exactly one. It does not go up twice.
2. Press and hold the plus button.
   The count goes up once immediately, pauses briefly, then climbs at a steady rate for as long
   as you hold.
3. Release.
   The count stops immediately. It does not creep up afterwards.
4. Press and hold, then drag your finger off the button before releasing.
   The count stops when the finger leaves the button.
5. Press and hold the plus button, and while holding, use another finger to press elsewhere on
   the same row.
   Only the count changes. The row itself does not also react as if it had been tapped.
6. Turn on the device's screen reader and double-tap the plus button.
   The count goes up by one, and the button is announced as a button.

## Loading further pages

1. On the main screen, scroll to the bottom of a section list.
   More items load and are appended below the ones already there. Nothing already on screen is
   replaced or reordered.
2. Keep scrolling to the bottom repeatedly until no more items arrive.
   The list stops growing and no spinner is left turning at the bottom.
3. Turn the network off, then scroll to the bottom of a list that has more to load.
   The connection-error banner appears and the items already loaded stay on screen.
4. Turn the network back on and scroll to the bottom again.
   The next page loads, and it is the page that failed — no block of items is missing between
   what was there before and what has just arrived.
5. Refresh the list from the top while more items are still loading at the bottom.
   The list comes back as a fresh first page, and scrolling down from there loads pages in
   order, with nothing skipped.

## Away from the edges of the screen

1. Open the app on a device with a notch or a punch-hole camera, held upright.
   No text or control is hidden behind the status bar or the camera cutout.
2. Rotate the device to landscape.
   No text or control is hidden behind the side system bars, on either side.
3. Rotate back.
   The screen lays out correctly again.
