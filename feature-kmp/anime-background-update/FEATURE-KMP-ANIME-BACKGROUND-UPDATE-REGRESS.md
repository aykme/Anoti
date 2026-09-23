# Background update — manual regression

This part of the app has no screen of its own. It re-reads the whole saved anime library from
the server on a schedule, writes what changed back, and asks for a notification about every
episode that has newly aired. Everything below is checked through its effects on the Favorites
screen and in the notification shade.

Run the script once. Text size, display size and orientation change nothing here, because
nothing in this part draws anything — the notification's own look is checked in the
anime-notification script instead.

## Before you start

1. Install the app and open it.
2. Allow notifications when asked. Without that permission every check below that expects a
   notification will silently show none.
3. Open Favorites and save at least three anime that are still airing, plus one that has
   already finished. Turn the bell on for each.
4. Leave the app for at least a minute so the first pass has run, then note for each saved
   anime the episode count shown on its row. Those numbers are the baseline for everything
   that follows.

The update runs by itself about once an hour. Where a step says "let a pass run", either leave
the phone online and untouched for an hour, or ask a developer to start one for you — they can
do it in seconds. Both amount to the same thing.

## 1. The repeating update

| # | Step | Expected result |
|---|---|---|
| 1.1 | With the app closed and the phone online, let a pass run. | Every saved anime whose episode count has moved on the server shows the new count on its Favorites row. |
| 1.2 | Look at the notification shade after 1.1. | One notification per anime that gained an episode, plus a single grouping notification above them. Nothing for anime that gained none. |
| 1.3 | Open Favorites after 1.1. | Each anime that gained an episode carries the new-episode mark. Anime that gained none look exactly as before. |
| 1.4 | Let a second pass run without anything changing on the server. | No new notification appears. No row changes. |
| 1.5 | Save an anime that finished airing long ago, then let a pass run. | Its row shows the final episode count. It does not gain a new-episode mark, and no notification appears for it. |
| 1.6 | Take an anime that is still airing, and wait until a real new episode is out for it. Let a pass run. | Exactly one notification for it, naming the anime and the number of the episode that aired. |

## 2. The update the Favorites screen asks for

| # | Step | Expected result |
|---|---|---|
| 2.1 | Open Favorites and pull the list down to refresh. | The list shows its loading state briefly, then the saved anime. Every new-episode mark disappears. |
| 2.2 | Wait up to a minute after 2.1, then look at the list again. | Rows whose data changed on the server now show the new values. |
| 2.3 | Pull to refresh several times quickly. | The screen behaves the same every time. No duplicate notification appears for an episode that was already announced. |
| 2.4 | Pull to refresh while a repeating pass happens to be running. | The refresh still works: marks clear and the list reloads. It is not ignored. |

## 3. Connection

| # | Step | Expected result |
|---|---|---|
| 3.1 | Turn on airplane mode. Close the app and wait an hour. | Nothing happens: no notification, no change to any row, and the app does not appear in battery usage for that hour. |
| 3.2 | Turn airplane mode off and let a pass run. | The update happens normally, exactly as in section 1. |
| 3.3 | Connect to a Wi-Fi network that needs a sign-in page and do not sign in. Let a pass run. | No notification and no row changes. The app does not crash and Favorites still opens normally. |
| 3.4 | Turn airplane mode on while a pass is running, then off again a minute later. Let the next pass run. | Whatever the interrupted pass had already written stays. The next pass fills in the rest. Nothing is announced twice. |
| 3.5 | Switch from Wi-Fi to mobile data and let a pass run. | The update happens. Mobile data is not treated differently from Wi-Fi. |

## 4. Surviving restarts and updates

| # | Step | Expected result |
|---|---|---|
| 4.1 | Restart the phone. Open the app once, then close it and let a pass run. | The update happens as before. |
| 4.2 | Swipe the app away from recents, then let a pass run without opening it. | The update still happens. |
| 4.3 | Force-stop the app from Android settings, then let a pass run without opening it. | Nothing happens — this is expected. Open the app once, and updates resume from then on. |
| 4.4 | Install a newer build of the app over the one already installed, without uninstalling. Open it once, then let a pass run. | Every saved anime is still there with its counts and marks. The update happens as before. |
| 4.5 | After 4.4, turn on airplane mode and wait an hour. | Nothing happens, exactly as in 3.1. The newer build's rule about needing a connection applies to the install that was already on the phone. |

## 5. Different data

| # | Step | Expected result |
|---|---|---|
| 5.1 | Save more than twenty anime, then let a pass run. | Every one of them is updated, not just the first twenty. |
| 5.2 | Save around a hundred anime and let a pass run. | All of them are updated. The phone stays usable throughout and the app does not stop responding. |
| 5.3 | Save an anime with no cover image and let a pass run so it gains an episode. | The notification appears with its title and episode number, and no picture. |
| 5.4 | Save an anime with a very long title and let it gain an episode. | The notification shows the title, cut off at the end rather than overflowing. |
| 5.5 | Save an anime that has been announced but has not started airing, and let a pass run. | Its row updates if anything about it changed. No notification appears for it. |
| 5.6 | Remove every saved anime and let a pass run. | Nothing happens and nothing goes wrong. Favorites still opens and shows its empty state. |

## 6. Device conditions

| # | Step | Expected result |
|---|---|---|
| 6.1 | Turn on battery saver, then let a pass run. | The update may be delayed, but once it runs it behaves exactly as in section 1. |
| 6.2 | Leave the phone untouched and unplugged overnight. | In the morning the saved anime are up to date, and any episode that aired overnight has its notification. |
| 6.3 | Exclude the app from battery optimization in Android settings, then repeat 6.2. | The same, and the updates arrive closer to every hour. |
| 6.4 | On a Samsung phone, check that the app is not listed as a sleeping app in Device care. | If it is listed, remove it from the list. Sleeping apps do not get their updates, and this is a phone setting rather than something the app can change. |
| 6.5 | Let a pass run while the app is open on the Favorites screen. | Rows update in place. The screen does not flicker or jump back to its loading state. |
| 6.6 | Let a pass run while the app is open on a different screen, then go to Favorites. | The list shows the updated data. |
