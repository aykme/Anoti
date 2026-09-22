# Anime list — manual regression

The anime list is the screen the app opens on, reached by the "Main" button in the bottom bar.
It has three sections: "On air", "Soon", and search. Only this screen is checked here. The
bottom bar itself, the error banners the app shows at the bottom, and the saved-anime list
behind the bell are checked in their own modules' files.

Unless a step says otherwise, start from a fresh installation with the device online.

## 0. How to run this file

Every section below is run **four times**, once per combination:

| Pass | System font size and display size | Orientation |
|------|-----------------------------------|-------------|
| 1    | default                           | portrait    |
| 2    | default                           | landscape   |
| 3    | both at maximum                   | portrait    |
| 4    | both at maximum                   | landscape   |

Pass 1 is the one that must be perfect. Passes 2–4 are looking for the same failures every
time: text cut off or overlapping, a control pushed off-screen or shrunk until it cannot be
tapped, a row that wraps in one pass and clips in another, and anything that stops responding
to a tap because it moved. Where a step behaves differently by scale or orientation on purpose,
that step says so.

Changing the font or display size restarts the app. Changing orientation does not — the screen
keeps its state, which is itself checked in section 12.

## 1. The three states of the section area

The area below the top bar is always in exactly one of three states. Learn them first; the rest
of this file refers to them by name.

- **Loading** — one large spinner, centered, inset well away from the edges. No items, no
  picture.
- **Error** — one large broken-plug picture, grayed, centered. No items, no spinner.
- **List** — the scrollable list of items.

The top bar is drawn over all three. It never disappears.

## 2. Every transition between those states

Each section keeps its own state. Check these on "On air" first, then repeat 1–6 on "Soon", and
1–8 on search.

1. Cold start, online: Loading → List.
   - The spinner appears first, on its own, and is replaced by items.
   - The spinner is never shown together with items.
2. Cold start, offline: Loading → Error.
   - The spinner appears first, then the broken-plug picture replaces it.
3. Error → List: with the picture shown, turn the network on and pull the picture down.
   - Loading appears, then items.
4. List → Loading → List: with items shown, pull the list down.
   - The spinner replaces the items, then items come back.
   - The spinner is visible for a moment even when the network answers instantly. It must not
     blink in and out.
5. List → Loading → Error: with items shown, turn the network off and pull down.
   - The spinner replaces the items, then the picture replaces the spinner.
   - The old items are gone; they do not stay behind the picture.
6. Error → Loading → Error: with the picture shown and still offline, pull it down.
   - The spinner appears briefly and the picture comes back.
7. Switching into a section that has never loaded: it starts at Loading and follows 1 or 2.
8. Switching into a section that already holds items: it goes straight to List, with no spinner
   and no reload.

## 3. The loading flash when a section changes state

1. Have "On air" showing items. Switch to "Soon" and watch the area closely.
   - Exactly one brief flash of the spinner, then the announced items.
   - It must not flicker between spinner and list two or three times before settling.
2. Pull "On air" down to refresh, and while the spinner is up, keep watching until items return.
   - One continuous spinner, then items. No intermediate flash of a half-filled list.

## 4. The poster on an item

The poster is loaded separately from the item, so it has its own three states inside the item's
own picture area. The rest of the item — title, episodes line, score, status, bell — is drawn
immediately and does not wait for the poster.

1. Scroll quickly through a freshly installed app so items appear before their pictures.
   - Where a picture has not arrived, a spinner spins inside the poster area, inset from its
     edges. The dark strip with the title and the bottom row is already drawn over it.
   - The spinner is replaced by the picture when it arrives.
2. Turn the network off, clear the app's storage, open it, and let items load from nothing.
   - The poster area shows a spinner first, then a grayed broken-image icon.
   - The title, episodes line, score, status and bell are all still readable over it.
3. Turn the network on and pull to refresh.
   - The broken-image icons are replaced by real pictures.
4. Find an anime the server has no picture for at all.
   - Its poster area ends at the broken-image icon. The item is otherwise complete.

## 5. What an item shows, by data

1. An ordinary item.
   - Poster; over its lower part a dark strip carrying the title and the "Episodes:" line;
     below them a row with a star icon, the score, the release status and a bell.
2. Total episode count unknown.
   - The line reads "Episodes: <aired> / ?".
3. A "Released" anime.
   - The aired number equals the total, not a smaller number.
4. Score missing.
   - The place where the score goes is blank. The star icon, the status and the bell keep
     their positions and do not slide over.
5. Release status unknown.
   - No status word is shown, and the two thin dividers around it are gone too. The star,
     score and bell spread out evenly across the row instead.
6. A very long title.
   - At most four lines, ending in "…". It never pushes the episodes line or the bottom row
     off the item.
7. A very long status word at a large font size — see section 13.

## 6. The episode-info button

This is the round button at the right end of the "Episodes:" line. It switches that one line
between two modes and changes nothing else on the item.

1. On an "Ongoing" item, tap it.
   - The line becomes "Next episode:" with the date on the line below.
   - The button's icon changes from outlined to filled.
   - The title, score, status and bell do not move or change.
2. Tap it again.
   - The line returns to "Episodes: <aired> / <total>" and the icon returns to outline.
3. Repeat on an item in "Soon".
   - The line reads "Beginning of the show:" with the date, then " (Inaccurate)".
4. Repeat on a "Released" item — the search section has many.
   - The line reads "Show is finished:" with the date.
5. Repeat on an item whose status is unknown.
   - The line shows the date alone, with no label before it.
6. Open the extra info on an item the server has no date for.
   - The label is shown with "No data" where the date would be.
7. On an "Ongoing" item that has never had its date fetched, tap the button and watch.
   - The line switches immediately, and the date fills in a moment later when it arrives.
   - Turning the mode off and on again does not fetch it a second time: the date is there at
     once.
8. Turn the network off and open the extra info on an "Ongoing" item whose date is not yet
   known.
   - The line switches and shows "No data". An error banner appears at the bottom of the
     screen.
9. Open the extra info on three items at once.
   - All three stay open independently. Closing one leaves the others open.
10. With extra info open on an item, scroll it far off-screen and back.
    - It is still open, and no other item has opened by itself.

## 7. The bell

1. Tap an empty bell. Its description before the tap is "Turn on notifications".
   - It fills in at once and its description becomes "Turn off notifications".
2. Go to "Favorites" in the bottom bar.
   - The anime is there.
3. Return to "Main".
   - The bell is still filled.
4. Tap the filled bell.
   - It empties at once, and the anime is gone from "Favorites".
5. Tap a bell five times quickly.
   - The final state matches an odd or even number of taps, and "Favorites" holds either one
     copy or none — never two.
6. Turn a bell on, then pull to refresh.
   - After the refresh the bell is still filled.
7. Turn a bell on in "On air", then find the same anime through search.
   - Its bell is filled there too.
8. Turn a bell on with the item's extra info open.
   - The bell fills in and the extra info stays open.

## 8. Switching sections

1. Tap "Soon" from "On air".
   - The highlight moves to "Soon" and the section area follows section 2, rule 7 or 8.
2. Tap "On air" again.
   - Its items come straight back, at the scroll position they had, with no spinner.
3. Scroll "On air" a long way down, go to "Soon", come back.
   - The scroll position is preserved.
4. Open the extra info on an item in "On air", go to "Soon" and back.
   - It is still open.
5. Turn a bell on in "On air", go to "Soon" and back.
   - It is still filled.
6. Go to "Soon" while "On air" is still loading.
   - "Soon" starts loading on its own. Coming back to "On air" shows it either still loading
     or already loaded — never empty and never in error.
7. Tap "On air" while already on "On air".
   - Nothing happens. No reload, no flash, no scroll jump.

## 9. Paging

1. In "On air", scroll to the bottom.
   - More items append and the list grows. The items already shown do not move or reload.
   - No full-screen spinner appears while this happens.
2. Keep scrolling. Repeat several times.
   - Each time more items append.
3. Scroll to the very end of what the server has.
   - The list stops growing and nothing breaks.
4. Scroll to the bottom, then immediately scroll up about ten items and back down while the
   next page is still arriving.
   - Only one page is added, not two. No item appears twice.
5. While a page is loading, pull down to refresh.
   - The list is replaced by the refreshed first page. The page that was loading must not be
     appended to it afterward.
6. Go offline and scroll to the bottom.
   - The list stops growing. The items already shown stay. An error banner appears at the
     bottom of the screen. The section does not switch to Error.
7. Turn the network back on and scroll up about ten items, then down again.
   - Paging resumes.
8. Repeat 1–7 in "Soon" and in search.

## 10. Search

1. Tap the magnifier.
   - The two section buttons are replaced by a text field with the hint "Enter the name of
     anime" and a cross button at its right. The keyboard opens.
   - The section area shows whatever the search section holds: Loading on the first ever
     visit, otherwise its previous results.
2. Type `naruto` and stop.
   - Nothing happens for a moment, then Loading, then results.
   - The list must not reload while you are still typing.
3. Type three more letters quickly.
   - Still only one reload, after you stop.
4. Delete the whole query.
   - The list reloads with unfiltered results.
5. Type a query nothing matches.
   - The section area ends up as an empty list — no items, no spinner, no picture.
6. Type more than 75 characters.
   - The field stops accepting input at 75. The text already there is not truncated or
     cleared.
7. Scroll the results down, then change the query.
   - The new results start from the top, not from where you were.
8. Open the extra info on a result, then change the query.
   - The new results all show the plain "Episodes:" line.
9. Tap the cross.
   - The field closes and the two section buttons come back, with neither highlighted — the
     search section is still the one showing.
   - The keyboard closes.
10. Tap the magnifier again.
    - The field reopens with the previous text still in it, and the results below are unchanged.
11. With the field open, tap "On air"… — there is no way to; the section buttons are hidden
    while the field is open. Close the field first.
12. Press system back with the keyboard up.
    - The keyboard closes. The field stays open with its text.

## 11. Pull to refresh

1. From List, in each of the three sections in turn.
   - Section 2, rule 4 applies: Loading, then items.
   - Extra info closes on every item.
   - Bells keep their state.
   - The list returns to the top.
2. From Error, in each section.
   - Section 2, rules 3 and 6 apply.
3. From an empty search result.
   - The same query is run again.
4. Pull down only slightly and release, without reaching the trigger point.
   - Nothing reloads.
5. Pull to refresh three times in a row without pausing.
   - Every time the section ends in List or Error. It must never stay on the spinner.

## 12. Leaving and coming back

1. In "On air", scroll halfway and open the extra info on one item. Press Home, then return
   from the task switcher.
   - The same section, the same scroll position, the same item open. Nothing reloads.
2. Rotate while in the background and return — see section 13.
3. Switch to search, type `bleach`, wait for results, press Home.
4. Force-stop the app's process from a development machine
   (`adb shell am kill com.alekseivinogradov.anoti`) and open it again from the launcher.
   - It comes back on the search section with `bleach` still in the field, and the results
     load again.
   - No crash, no error picture, no empty list.
5. Repeat step 4 with the search bar closed but the search section selected.
   - It comes back on the search section with the bar closed and the query still applied.
6. Repeat step 4 with "Soon" selected and several pages scrolled in.
   - It comes back on "Soon" with about as many items as before — up to eighty — not just the
     first page.
   - Items that had their extra info open have it open again.
   - The bells are as they were.
7. Repeat step 4 with "On air" selected and nothing scrolled.
   - It comes back on "On air" with the first page.

## 13. What changes with scale and orientation

The four passes from section 0 cover the whole file. These are the specific differences to look
for, and the checks that only make sense once.

1. Rotating mid-session, from every state: List, Loading, Error, and with the search field open.
   - The state is kept. Nothing reloads, the scroll position holds, open extra info stays open,
     bells keep their state, and text typed into the search field is still there.
2. Rotating in landscape, with items shown.
   - Each item is wider and shorter. Title, episodes line, score, status and bell are all still
     present and none overlaps another.
3. At maximum font and display size, an item's bottom row.
   - The bell keeps its full size. The status word is the one that shortens with "…" when the
     row runs out of room — never the bell, never the score.
   - The star icon, the score and the bell stay on one line.
4. At maximum size, an item with an unknown release status.
   - No status word and no dividers; the star, score and bell spread evenly and still fit.
5. At maximum size, a long title.
   - Still at most four lines with "…", and the episodes line and bottom row are still visible.
6. At maximum size, with the extra info open.
   - The date line takes at most three lines and is not cut mid-word.
7. At maximum size, the top bar.
   - Both section labels stay on one line each and remain tappable across their full height.
   - With the search field open, the hint or the typed text does not run under the cross
     button.
8. At maximum size, the empty-search result and the Error picture.
   - Both still fill the area sensibly and are not cut off.
9. Turn on the system dark theme and repeat pass 1 of section 0 in outline.
   - Text over the poster's dark strip stays readable, and so do the top bar's labels.
   - No white-on-white or black-on-black anywhere.
