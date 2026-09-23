# Background update — manual regression

This part of the app has no screen of its own. It re-reads the whole saved anime library from
the server about once an hour, writes back whatever changed, and asks for a notification about
every episode that has newly aired. Everything below is checked through what it does to the
Favorites screen and to the notification shade.

Run the script once. Font size, display size and orientation change nothing here, because
nothing in this part draws anything. How a notification itself looks at each scale belongs to
the anime-notification script.

Where a step says "let a pass run", either leave the phone online and untouched for an hour,
or ask a developer to start one — it takes them seconds. Both amount to the same thing.

## Platforms

The steps below name Android's screens. What they go through — force-stopping, battery
optimization, the sleeping-apps list, swiping away from recents — is Android's own. An iOS run
makes the same checks through the Background App Refresh settings, and expects the same results.
Two things differ there. The app asks for its next pass about fifteen minutes ahead rather than
an hour. The system, not the app, decides when that pass actually runs, so it can arrive much
later than asked. Turning Background App Refresh off is what airplane mode and a force-stop
stand for.

## Before you start

1. Install the app and open it. Allow notifications when asked. Without that permission every
   check that expects a notification silently shows none.
2. Open Favorites and subscribe to at least three animes that are still airing, plus one that
   finished airing long ago. Leave the bell on for each.
3. Let a pass run, then write down the episode count on every row. Those numbers are the
   baseline for everything that follows.

## 1. The hourly update

1. Close the app, stay online, and let a pass run.
   - Every anime whose episode count moved on the server now shows the new count on its row.
   - Every anime that gained an episode carries the new-episode mark.
   - Animes that gained nothing look exactly as they did before.
2. Look at the notification shade straight after.
   - One notification per anime that gained an episode, naming it and the episode number.
   - One grouping notification above them.
   - Nothing at all for the animes that gained nothing.
3. Let a second pass run without anything changing on the server in between.
   - No new notification. No row changes.
4. The anime that finished airing long ago.
   - Its row keeps its final episode count. It never gains the new-episode mark and never
     produces a notification.
5. An anime that is announced but has not started airing.
   - Its row updates if the server changed anything about it, and it produces no notification.
6. Wait until a real new episode is out for one of the airing animes, then let a pass run.
   - Exactly one notification for it, naming the number of the episode that aired.

## 2. The update the Favorites screen asks for

Pulling the Favorites list down starts a pass of its own, separate from the hourly one.

1. Open Favorites and pull the list down.
   - The list shows its brief loading state, then the animes.
   - Every new-episode mark disappears.
2. Wait up to a minute, then look again.
   - Rows whose data changed on the server now show the new values, and the marks come back
     on those that gained an episode.
3. Pull down several times in a row, quickly.
   - The screen behaves the same every time.
   - No second notification arrives for an episode that was already announced.
4. Pull down while an hourly pass happens to be running.
   - The refresh still works: marks clear, the list reloads, fresh data arrives.
   - It is never ignored because the other pass is busy.

## 3. The two updates together

The hourly update and the one the screen asks for are separate. Either can be running when the
other starts, and neither may break the other.

1. Let an hourly pass run, then pull the Favorites list down, then let another hourly pass run.
   - All three happen. The pull does not stop the hourly updates from continuing afterward.
2. Start an hourly pass, and pull the list down while it is still running.
   - Both finish. The screen refreshes, and the subscriptions end up to date.
   - Expected for now: an episode that aired can be announced twice when the two passes
     overlap like this. Note it, do not treat it as a new fault.
3. Pull the list down, and let an hourly pass start while that one is still running.
   - Both finish, with the same result as above.
4. Open and close the app several times in a row over a few minutes, then wait for the hourly
   update.
   - It still arrives about an hour after the first one, not an hour after the last time the
     app was opened.
5. Let a pass fail (turn the connection off just as it starts), then wait for the next hourly
   one.
   - It arrives about an hour later, not within a minute and not hours later.
6. Let a pull-to-refresh pass fail the same way, then pull down again.
   - The second pull starts a new pass rather than being ignored.

## 4. Connection

1. Turn on airplane mode, close the app, and wait an hour.
   - No notification, no row changes.
   - The app does not appear in the battery usage list for that hour.
2. Turn airplane mode off and let a pass run.
   - The update happens exactly as in section 1.
3. Join a Wi-Fi network that demands a sign-in page, and do not sign in. Let a pass run.
   - No notification and no row changes. The app does not crash, and Favorites still opens.
4. Turn airplane mode on while a pass is running, then off a minute later, and let the next
   pass run.
   - Whatever the interrupted pass had already written is still there.
   - The next pass fills in the rest, and nothing is announced twice.
5. Switch from Wi-Fi to mobile data and let a pass run.
   - The update happens. Mobile data is not treated differently from Wi-Fi.

## 5. Restarts, force-stops and app updates

1. Restart the phone, open the app once, close it, and let a pass run.
   - The update happens as before.
2. Swipe the app away from recents, then let a pass run without opening it.
   - The update still happens.
3. Force-stop the app from Android settings, then let a pass run without opening it.
   - Nothing happens, and that is correct — Android stops all background work for an app that
     was force-stopped. Open the app once and updates resume from then on.
4. Install a newer build over the one already on the phone, without uninstalling first. Open
   it once, then let a pass run.
   - Every subscribed anime is still there with its counts and its marks.
   - The update happens as before.
5. Straight after step 4, turn on airplane mode and wait an hour.
   - Nothing happens, exactly as in section 4. The newer build's rule about needing a
     connection reaches the installation that was already on the phone.

## 6. Different data

1. Subscribe to more than twenty animes and let a pass run.
   - Every one of them is updated, not only the first twenty.
2. Subscribe to around a hundred and let a pass run.
   - All of them are updated. The phone stays usable and the app does not stop responding.
3. An anime the server has no picture for, gaining an episode.
   - Its notification appears with the title and episode number, and no picture.
4. An anime with a very long title, gaining an episode.
   - Its notification shows the title cut-off at the end rather than overflowing.
5. Remove every subscription and let a pass run.
   - Nothing happens and nothing goes wrong. Favorites still opens on its empty panel.

## 7. The phone's own power rules

1. Turn on battery saver and let a pass run.
   - The update may arrive late, but when it arrives it behaves as in section 1.
2. Leave the phone unplugged and untouched overnight.
   - In the morning the subscriptions are up to date, and every episode that aired overnight
     has its notification.
3. Exclude the app from battery optimization in Android settings, then repeat step 2.
   - The same and the updates arrive closer to once an hour.
4. On a Samsung phone, open Device care and check the sleeping-apps list.
   - If the app is listed there, take it out. A sleeping app gets no updates at all, and that
     is a phone setting rather than anything the app can change.

## 8. While the app is open

1. Let a pass run with Favorites on screen.
   - Rows update in place. The screen does not flicker and does not drop back to loading.
2. Let a pass run while another screen is open, then go to Favorites.
   - The list shows the updated data.
