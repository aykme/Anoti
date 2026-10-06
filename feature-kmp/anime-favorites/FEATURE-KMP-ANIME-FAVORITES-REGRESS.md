# Anime favorites — manual regression

The favorites screen lists the anime the user subscribed to, reached by the "Favorites" button
in the bottom bar. Only this screen is checked here. The bottom bar itself, the error banners
the app shows at the bottom, the anime list the subscriptions come from, and the background
update that marks new episodes are checked in their own modules' files.

Unless a step says otherwise, start from a fresh installation with the device online.

## 0. How to run this file

Every section below is run once per pass listed under "Passes" in the root
`ANOTI-FULL-REGRESS.md`.

On Android, changing the font or display size rebuilds the screen, and it keeps its state. The
iPhone keeps its screen as it is. Changing orientation rebuilds nothing — the screen keeps its
state, which is itself checked in section 11.

## 1. The three states of the screen

The area below the status bar is always in exactly one of three states.

- **Loading** — one large spinner, centered, inset well away from the edges.
- **Empty** — a picture of a character at the top left, and to its right a panel of text
  starting "You haven't subscribed to notifications about new anime series yet." The picture
  is as wide and as tall as an item's poster would be on the same screen.
- **List** — the scrollable list of subscribed anime.

There is no error state. Losing the network never takes this screen out of List or Empty,
because the list comes from the device, not the server.

## 2. Every transition between those states

1. Arriving with nothing subscribed: Loading → Empty.
   - The spinner shows for a moment first. It is never skipped, even though the list is read
     from the device and answers instantly.
2. Arriving with something subscribed: Loading → List.
   - Same brief spinner, then the items.
3. Empty → List, without leaving the screen: go to "Main", subscribe to an anime, come back.
   - Arriving runs Loading → List as in rule 2.
4. List → Empty, without leaving the screen: remove the last remaining item with its bell.
   - The item disappears, a spinner takes over the screen for a moment, and then the empty
     panel appears.
   - The spinner step is expected. The empty panel must not appear instantly.
5. List → Loading → List: pull the list down.
   - Spinner, then the items come back.
6. Empty: pull the empty panel down.
   - There is nothing to pull; the empty panel does not scroll. Confirm that pulling it does
     nothing at all and leaves the panel alone.
7. Removing one of several items.
   - That item disappears from the list. No spinner, no reload of the others.
8. Leaving and arriving again, from each of Empty and List.
   - Every arrival runs its brief Loading first.

## 3. The poster on an item

The poster is loaded separately, so it has its own states inside the item's left-hand column.
The right-hand panel is drawn immediately and does not wait for it.

1. Subscribe to several anime, then open the screen before the pictures have been fetched.
   - Where a picture has not arrived, a spinner spins inside the poster column, inset from its
     edges. The score bar over it and the whole right-hand panel are already drawn.
2. Android only, since the iPhone cannot clear an app's cache alone: with several anime
   subscribed, close the app and clear its cache, not its storage, in the system settings. Turn
   the network off and open the screen.
   - The poster column shows a spinner first, then a grayed broken-image icon, centered in the
     column with a margin around it.
   - The score bar, title, episodes line, status and bell are all still readable.
3. Turn the network on and pull to refresh.
   - Real pictures replace the broken-image icons.

## 4. What an item shows in the main mode

1. An ordinary item.
   - Left: the poster, taking a bit over a third of the item's width, with a bar across its
     lower part carrying a star icon, the score, and the info-mode button.
   - Right: the title, a line "Episodes: <aired> / <total>", and at the bottom the release
     status with a filled bell at its right.
2. Total episode count unknown.
   - The line reads "Episodes: <aired> / ?".
3. A "Released" anime.
   - The aired number equals the total.
4. An "Announced" anime.
   - The line reads "Episodes: 0 / ?" or with whatever counts exist.
5. A very long title.
   - At most three lines ending in "…", and the episodes line, status and bell are all still
     visible.
6. The bell is filled on every item, always. An empty bell here would be a bug — an item is
   only on this screen because it is subscribed.

## 5. The info-mode button

The info-mode button on the poster's bar switches the whole right-hand panel between two
modes. This is the part to check most carefully: it is not a partial change.

1. On an "Ongoing" item, tap it. Its description before the tap is "Turn on the display of
   extra information".
   - The title, the "Episodes:" line, the release status **and the bell** all disappear
     together.
   - In their place: a date line reading "Next:" with the date below it, then a line
     "Episodes viewed:", then a row with a minus button, a number and a plus button.
   - The button's description becomes "Turn off the display of extra information" and its icon
     changes.
2. Tap it again.
   - The title, episodes line, status and bell all come back, and the date line, the
     "Episodes viewed:" line and the counter row all go.
3. Long-press anywhere on the item body instead of using the button.
   - It switches the same way, in both directions.
4. Repeat 1 on an "Announced" item.
   - The date line reads "Beginning:" with the date and " (Inaccurate)".
5. Repeat 1 on a "Released" item.
   - The date line reads "Finished:" with the date.
6. An item the server has no date for.
   - The label is shown with "No data" in place of the date.
7. An "Ongoing" item whose date has never been fetched.
   - The panel switches immediately and the date fills in a moment later.
   - Switching the mode off and on again does not fetch it again — the date is there at once.
8. Turn the network off and switch an item with no known date into extra mode.
   - The panel switches and shows "No data". An error banner appears at the bottom.
9. Switch three items into extra mode at once.
   - All three stay in extra mode independently.
10. Switch an item into extra mode, scroll it far off-screen and back.
    - It is still in extra mode, and no other item switched by itself.
11. Switch an item into extra mode and leave the screen to "Main" and back.
    - It is back in main mode. Every arrival at this screen resets the mode on every item.

## 6. Counting viewed episodes

Only reachable in extra mode.

1. Open extra mode on an "Ongoing" item with several aired episodes. The counter starts at 0.
2. Tap plus once.
   - The counter reads 1. One tap moves it by exactly one.
3. Press and hold plus.
   - The counter climbs on its own while held and stops the moment you let go.
4. Keep holding plus past the number of aired episodes.
   - It stops at that number and goes no further, however long you hold.
5. Tap minus once.
   - The counter drops by one.
6. Press and hold minus down to 0.
   - It stops at 0 and never goes negative.
7. Tap minus at 0.
   - Nothing happens.
8. Check the ceiling per status.
   - "Ongoing": stops at the aired count.
   - "Released": stops at the total count.
   - "Announced": plus does nothing at all; the counter stays at 0.
9. Set a counter to some value, switch the item out of extra mode and back.
   - The value is still there.
10. Set a counter, leave to "Main" and come back.
    - The value is still there.
11. Set a counter, pull to refresh.
    - The value is still there.

## 7. The bell

1. Tap the filled bell on an item in main mode.
   - The item disappears from the list at once.
2. Go to "Main" and find that anime.
   - Its bell is empty there.
3. Remove items one by one down to the last.
   - Removing the last one follows section 2, rule 4: spinner, then the empty panel.
4. The bell is not reachable in extra mode — confirm that switching an item into extra mode
   leaves no bell on it, and that switching back brings the bell back.

## 8. The new-episode mark

A new-episode mark needs the background update to have run and found a newly aired episode.
Subscribe to an ongoing anime whose next episode is due shortly and leave the app installed
until it airs, or trigger the update the way your team normally does.

1. Open the screen with such an item present.
   - The item shows a "New episode" caption over its poster, in bold with a dark shadow behind
     it.
2. The same item in extra mode.
   - The caption is still on the poster; switching modes does not affect it.
3. Tap the item's body once, in main mode.
   - The caption goes away.
   - It does not come back when you leave the screen and return.
4. Have a marked item and pull to refresh.
   - Every mark on the screen is cleared.
5. Long-press a marked item.
   - It switches to extra mode and the mark stays — a long press is not a tap.

## 9. Pull to refresh

1. From List, with several items and one of them in extra mode.
   - A spinner replaces the list, then the list comes back.
   - Every item is back in main mode.
   - Every new-episode mark is cleared.
   - The viewed-episode counters are unchanged.
   - The items themselves are all still there.
2. Pull down only slightly and release, short of the trigger point.
   - Nothing reloads.
3. Pull to refresh three times in a row without pausing.
   - Every time the screen ends in List. It must never stay on the spinner.
4. Pull to refresh with exactly one item.
   - The item comes back.
5. Turn the network off and pull to refresh.
   - The list comes back unchanged. The screen does not empty and does not stay on the spinner.
6. Pull to refresh while the posters are still loading.
   - The spinner replaces everything, then the list returns and the posters resume loading.
7. A pull also starts a fetch of fresh data for every item, separate from the hourly one the
   app runs on its own. Pull to refresh while that hourly update is running — either wait for
   it or ask a developer to start one.
   - The pull is not ignored: the spinner appears, the marks clear, and the list comes back.
   - Within about a minute the items show whatever the server changed.
   - Expected for now: an episode that aired can be announced twice when the two overlap.
     Note it, do not treat it as a new fault of this screen.
8. Pull to refresh with the network off so the fetch fails, turn the network on, and pull
   again.
   - The second pull starts a new fetch: within about a minute the items show fresh data.
   - It must not be silently swallowed because of the pull that failed.
9. Pull to refresh, then leave the screen and come back before a minute has passed.
   - The fresh data still arrives on the items.

## 10. Leaving and coming back

1. Open the screen, switch one item into extra mode, scroll the list.
2. Go to "Main" and back.
   - Brief Loading, then the list.
   - Every item is back in main mode. This is expected on every arrival.
   - New-episode marks stay.
3. Press Home and return from the task switcher.
   - The list is exactly as you left it. Nothing reloads, extra mode is kept, the scroll
     position holds.
4. Scroll the list down and switch an item into extra mode. Press Home, force-stop the app's
   process from a development machine (`adb shell am kill com.alekseivinogradov.anoti`), then
   open it again from the launcher.
   - It comes back on "Favorites" with the list loaded, scrolled where you left it.
   - The item you left in extra mode is still in extra mode — unlike an ordinary arrival, a
     return from a killed process keeps it.
   - No crash, no endless spinner.
5. Repeat step 4 with nothing subscribed.
   - It comes back on the empty panel, not on a spinner.

## 11. What changes with scale and orientation

The four passes cover the whole file. These are the specific differences to look for, and the
checks that only make sense once.

1. Rotating mid-session, from Loading, Empty and List.
   - The state is kept. Nothing reloads, the scroll position holds, items in extra mode stay in
     extra mode, and the viewed counters keep their values.
2. In landscape, with items shown.
   - Each item is wider and shorter. The poster keeps its share of the width rather than
     stretching; the right-hand panel keeps title, episodes line, status and bell.
3. At maximum font and display size, an item in main mode.
   - The poster still takes its share of the width and does not squeeze the panel out.
   - The title still shows at most three lines with "…", and the status and bell are still on
     screen.
4. At maximum size, the poster's score bar.
   - The star icon, the score and the info-mode button all stay visible.
   - If they no longer fit on one line, the button drops onto a second line below the score —
     the score must not slide up out of the bar's dark background or over the picture.
5. At maximum size, an item in extra mode.
   - The date line takes at most three lines and is not cut mid-word.
   - On a narrow screen the "Episodes viewed:" line may shorten with "…".
   - The minus button, the number and the plus button all stay on screen and all stay tappable.
6. At maximum size, the empty panel.
   - The character picture and the text panel are both fully visible, and the text is not cut
     off at the bottom.
   - The picture grows with the text panel, as a poster grows with its item, and stays as wide
     as before.
7. At maximum size, the "New episode" caption.
   - It stays on one line, shortening with "…" if it must, and does not cover the score bar.
8. Turn on the system dark theme and repeat pass 1 in outline.
   - The right-hand panel's text and the caption over the poster stay readable.
   - No white-on-white or black-on-black anywhere.
9. The empty panel on a wide screen: a tablet in either orientation, a foldable unfolded, an
   iPad.
   - The picture is as wide as a poster: a little over a third of the row, not a small square.
   - It fills its frame with no bars at the sides. The frame is wide and low, so the picture is
     cut above and below. The character's eyes and glasses stay in view, with the ears as far as
     they fit.
   - Subscribe to one anime and come back. The item's poster takes exactly the size the picture
     had.
10. Fold and unfold a foldable, once with an item in extra mode and once on the empty panel.
    - The item stays in extra mode, and its viewed counter keeps its value. It is laid out again
      for the new width.
    - The empty panel takes the narrow screen's shape folded and the wide screen's unfolded, as
      step 9 describes.

## 12. Many items

1. Subscribe to thirty or more anime, then open the screen.
   - All of them are listed and the list scrolls smoothly to the end.
   - The last item is fully reachable and not hidden behind the bottom bar.
2. Switch an item near the bottom into extra mode, scroll to the top and back.
   - It is still in extra mode and no other item switched.
3. Scroll fast through the whole list several times.
   - Posters load and stay loaded. No item renders blank or with another item's picture.

## 13. Platforms

The steps name Android's tools. On the iPhone:

- Section 10 ends the app with `adb shell am kill`. On the iPhone, end it as step 2 of "Leaving
  and coming back" in `main`'s file says, with its iPhone notes. Everything section 10 expects
  comes back the same way, the scroll position included.
- Swiping the app away in the app switcher is the user closing it. The app then starts fresh.
- A new text size reaches the iPhone app only when it is opened again. After changing it, close
  the app in the app switcher and open it before walking the pass. Changing Display Zoom restarts
  the iPhone, so that pass starts from a cold launch.
- A narrow screen keeps the app upright, as on Android. The rotation steps of section 11 run on a
  wide one: an iPad, or a foldable iPhone unfolded.
