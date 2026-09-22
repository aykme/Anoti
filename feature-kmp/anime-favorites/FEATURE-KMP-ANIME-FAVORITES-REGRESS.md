# Anime favorites — manual regression

The favorites screen lists the anime the user subscribed to, reached by the "Favorites" button
in the bottom bar. Only this screen is checked here. The bottom bar itself, the error banners
the app shows at the bottom, the anime list the subscriptions come from, and the background
update that marks new episodes are checked in their own modules' files.

Unless a step says otherwise, start from a fresh install with the device online.

## 1. The empty screen

1. Install the app, open it and go to "Favorites" without subscribing to anything.
   - A spinner shows briefly, then a picture of a character next to a panel of text starting
     "You haven't subscribed to notifications about new anime series yet."
   - The picture sits at the top left, the text panel to its right. Neither is cut off.
2. Leave to "Main" and come back.
   - The same empty screen. The spinner shows again briefly each time.

## 2. Filling the screen

1. Go to "Main" and turn on the bell on three anime — pick one "Ongoing", one "Announced" and
   one "Released".
2. Go to "Favorites".
   - A spinner shows briefly, then all three are listed.
   - They are listed in the order they were added.

## 3. What one item shows

1. Look at an item.
   - On the left, a poster taking a bit over a third of the item's width. Under the poster's
     top edge, a bar with a star icon, the score and a round button.
   - On the right, the title, a line "Episodes: <aired> / <total>", the release status and a
     filled bell.
2. Find an item whose total episode count is unknown.
   - Its line reads "Episodes: <aired> / ?".
3. Check a "Released" item.
   - Its aired count equals its total, not a smaller number.
4. Find an item with a very long title.
   - The title takes at most three lines and ends with "…", and the rest of the panel still
     fits.
5. Turn the device off-line and open the screen with an item whose poster is not cached.
   - A spinner shows in place of the poster first, then a broken-image icon. The rest of the
     item still renders.
   - Turn the network back on.

## 4. The extra-info button

1. Tap the round button on the poster's score bar. Its description reads "Turn on the display
   of extra information" before the tap.
   - The right-hand panel changes: instead of "Episodes:" and the status it shows a date line
     and a line "Episodes viewed:" with a number and a minus and a plus button.
   - For an "Ongoing" anime the date line reads "Next:" with the date below it.
   - For an "Announced" one it reads "Beginning:" with the date and "(Inaccurate)".
   - For a "Released" one it reads "Finished:" with the date.
   - Where the server has no date, "No data" is shown in its place.
2. Tap the button again.
   - The panel goes back to the title, episodes line and status.
3. Long-press anywhere on the item instead of using the button.
   - It switches the same way.
4. Open the extra info on two items at once.
   - Both stay open.
5. Open the extra info on an "Ongoing" anime whose next-episode date the screen has not fetched
   before.
   - The date appears within a moment. Closing and reopening the extra info does not fetch it
     again — it appears immediately.

## 5. Counting viewed episodes

1. Open the extra info on an "Ongoing" item with several aired episodes. The counter starts
   at 0.
2. Tap the plus button once.
   - The counter reads 1.
3. Press and hold the plus button.
   - The counter climbs on its own while held, and stops when released.
4. Keep holding until the counter reaches the number of aired episodes.
   - It stops there and goes no higher, however long you keep holding.
5. Tap the minus button once.
   - The counter drops by one.
6. Press and hold the minus button down to 0.
   - It stops at 0 and does not go negative.
7. Check the ceiling on the other statuses.
   - On a "Released" anime the counter stops at its total episode count.
   - On an "Announced" anime the plus button does nothing; the counter stays at 0.
8. Leave to "Main" and come back.
   - The counter shows the value you left it at.

## 6. The bell

1. Tap the filled bell on an item.
   - The item disappears from the list.
2. Remove every item this way.
   - The screen changes to the empty state from section 1.
3. Go to "Main" and check the anime you removed.
   - Its bell is empty there.

## 7. Opening an item with a new episode

A new-episode mark needs the background update to have run and found a newly aired episode;
subscribe to an ongoing anime whose next episode is due shortly and leave the app installed
overnight, or trigger the update as your team normally does.

1. Open "Favorites" with such an item present.
   - The item shows a "New episode" mark on its poster.
2. Tap the item's body once.
   - The mark goes away and does not come back when you leave and return.

## 8. Pull to refresh

1. Open the extra info on one item.
2. Pull the list down from the top and release.
   - A spinner replaces the list briefly, then the list comes back.
   - The extra info closes on every item.
   - Any "New episode" marks are cleared.
3. Pull to refresh three times in a row without pausing.
   - Every time the list comes back. The screen must never stay on the spinner.
4. Pull to refresh with a single item in the list, then with an empty list.
   - With one item, the item comes back. With none, the empty screen comes back — not an
     endless spinner.
5. Go off-line and pull to refresh.
   - The list comes back unchanged. The screen does not stay on the spinner and does not go
     empty.
   - Turn the network back on.

## 9. Leaving and coming back

1. Open "Favorites", open the extra info on one item and scroll the list.
2. Switch to "Main" and back to "Favorites".
   - A spinner shows briefly, then the list.
   - The extra info is closed again on every item. This is expected on every arrival at the
     screen.
3. Press Home and return to the app from the task switcher.
   - The list is as you left it and does not reload.
4. Open the extra info on one item. Press Home, force-stop the app's process from a development
   machine (`adb shell am kill com.alekseivinogradov.anoti`), then open the app again from the
   launcher.
   - The screen comes back on "Favorites" with the list loaded.
   - The extra info you left open is still open — unlike an ordinary arrival at the screen,
     coming back from a killed process keeps it.
   - No crash, no error screen, no endless spinner.

## 10. Many items

1. Subscribe to thirty or more anime from "Main", then open "Favorites".
   - All of them are listed, and the list scrolls smoothly to the end.
   - The last item is fully reachable and not cut off by the bottom bar.
2. Open the extra info on an item near the bottom, scroll to the top and back.
   - It is still open, and no other item opened by itself.

## 11. Device settings

1. Rotate the device to landscape.
   - The list stays where it was and does not reload.
   - Each item still shows its poster, score, title, episodes line, status and bell.
2. Rotate back.
3. Turn on the system dark theme.
   - Text in the right-hand panel and over the poster stays readable.
4. Set the system font size and display size to their largest.
   - Every item still shows all of its parts. The poster keeps its share of the width rather
     than squeezing the panel out.
   - With the extra info open, the score bar may wrap its button onto a second line; the score
     and the icon must still be visible.
5. Return both settings to normal.
