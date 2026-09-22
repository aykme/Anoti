# Anime list — manual regression

The anime list is the screen the app opens on, reached by the "Main" button in the bottom bar.
It has three sections: "On air", "Soon", and search. Only this screen is checked here. The
bottom bar itself, the error banners the app shows at the bottom, and the saved-anime list
behind the bell are checked in their own modules' files.

Unless a step says otherwise, start from a fresh install with the device online.

## 1. Opening the screen

1. Install the app and open it. Allow notifications when asked.
   - The "On air" section opens by itself.
   - While it loads, a spinner fills the screen.
   - The spinner is replaced by a list of anime. Nothing else is needed to make it appear.
2. Look at the top of the screen.
   - Two buttons: "On air" and "Soon". "On air" is the highlighted one.
   - To their right, a magnifier button.

## 2. What one list item shows

Pick the first item.

1. Check its parts.
   - A poster picture filling the width of the item.
   - Over the bottom of the poster, on a dark strip: the title, a line reading
     "Episodes: <aired> / <total>", and below them a row with a star icon, a score, the release
     status, and a bell button.
2. Find an item whose total episode count is unknown.
   - Its line reads "Episodes: <aired> / ?".
3. Find an item with a very long title.
   - The title takes at most four lines and ends with "…". It never pushes the episodes line or
     the bottom row off the item.
4. Turn the device off-line and scroll to an item whose poster has not been loaded yet.
   - A spinner shows in place of the poster first, then a broken-image icon. The rest of the
     item still renders.
   - Turn the network back on before continuing.

## 3. The episode-info button on an item

1. Tap the round button at the right of the "Episodes:" line of an "Ongoing" item.
   - The line changes to "Next episode:" with the date on the next line.
   - The button's icon changes to its filled variant.
2. Tap it again.
   - The line goes back to "Episodes: <aired> / <total>".
3. Do the same on an item in "Soon".
   - The line reads "Beginning of the show:" with the date, followed by "(Inaccurate)".
4. Find a "Released" item (the search section has many) and do the same.
   - The line reads "Show is finished:" with the date.
5. Open the extra info on an item whose date the server does not have.
   - The line shows the label and "No data" in place of the date.
6. Open the extra info on one item, then on a second one.
   - Both stay open at the same time.

## 4. The bell on an item

1. Tap the bell on an item. Its description reads "Turn on notifications" before the tap.
   - The bell fills in and its description becomes "Turn off notifications".
2. Go to "Favorites" in the bottom bar.
   - That anime is in the list.
3. Come back to "Main".
   - The bell on that item is still filled.
4. Tap the filled bell.
   - The bell empties. The anime disappears from "Favorites".
5. Tap the bell on an item quickly, several times in a row.
   - The bell ends in the state matching the number of taps, and no duplicate appears in
     "Favorites".

## 5. Switching sections

1. Tap "Soon".
   - The highlight moves to "Soon".
   - A spinner shows briefly, then a list of announced anime. Their status reads "Announced"
     and their episode line reads "Episodes: 0 / ?".
   - The loading flash happens once. The screen must not flicker between the spinner and the
     list several times.
2. Tap "On air".
   - The ongoing list comes back without loading again, at the same scroll position it had.
3. Scroll "On air" halfway down, switch to "Soon", then back to "On air".
   - The scroll position is where you left it.
4. In "On air", open the extra info on an item, switch to "Soon" and back.
   - The extra info is still open on that item.

## 6. Paging

1. In "On air", scroll to the bottom of the list.
   - More items load and the list grows. This repeats as you keep scrolling.
   - No spinner covers the screen while this happens; the items already shown stay put.
2. Keep scrolling to the very end of what the server has.
   - Scrolling stops growing the list and nothing breaks.
3. Go off-line and scroll to the bottom.
   - The list stops growing. The items already shown stay on screen and are not replaced by an
     error screen.
   - Turn the network back on.

## 7. Search

1. Tap the magnifier.
   - The two section buttons are replaced by a text field with the hint "Enter the name of
     anime" and a cross button at its right.
   - The keyboard opens.
2. Type `naruto`.
   - After a short pause, the list below shows anime matching the query.
   - The list must not reload on every letter — it reloads once you stop typing.
3. Keep typing more letters.
   - The results follow the new query.
4. Clear the field.
   - The list reloads with unfiltered results.
5. Type a query no anime matches.
   - The list ends up empty. No error screen.
6. Paste or type more than 75 characters into the field.
   - The field stops accepting input at 75 characters.
7. Tap the cross button.
   - The field closes and the two section buttons come back. The section shown is still search
     with its results.
8. Tap the magnifier again.
   - The field reopens with the text you typed still in it.
9. Open extra info on a search result, then run a new search.
   - The extra info closes for every item and the list starts from the top.

## 8. Pull to refresh

1. In "On air", pull the list down from the top and release.
   - The list reloads from its first page.
   - Any extra info left open closes.
   - The list goes back to the top.
2. Do the same in "Soon" and in search.
   - Each refreshes only its own section.
3. Go off-line and pull to refresh.
   - The screen shows a large broken-plug picture in place of the list.
4. Turn the network back on and pull that picture down.
   - The list loads again and the picture goes away.

## 9. The error screen

1. Turn the network off before opening the app, then open it.
   - The screen shows the large broken-plug picture instead of a list.
2. Switch to "Soon" while still off-line.
   - The same picture is shown.
3. Turn the network on and pull the picture down.
   - The section loads.

## 10. Leaving and coming back

1. Open "On air", scroll halfway, open the extra info on one item.
2. Press Home, then return to the app from the task switcher.
   - The same section, the same scroll position, the same extra info open. Nothing reloads.
3. Switch to search, type `bleach` and wait for results.
4. Press Home. From a terminal on a development machine, force-stop the app's process
   (`adb shell am kill com.alekseivinogradov.anoti`), then open the app again from the launcher.
   - The screen comes back on the search section with `bleach` still in the field, and its
     results load again.
   - No error screen, no crash, no empty list.
5. Repeat step 4 with "Soon" selected and a few pages scrolled in.
   - The screen comes back on "Soon" with roughly as many items as before (up to eighty), not
     just the first page.

## 11. Device settings

1. Rotate the device to landscape while the list is shown.
   - The list stays where it was and does not reload.
   - The title, episodes line, score, status and bell all still fit on each item.
2. Rotate back.
3. Turn on the system dark theme.
   - Text stays readable against the dark strip over the poster; no white-on-white or
     black-on-black.
4. Set the system font size and display size to their largest.
   - Every item still shows its title, episodes line, score, status and bell. The bell keeps
     its full size; it is the release status that shortens with "…" when space runs out.
5. Return both settings to normal.
