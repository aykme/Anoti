# Anime notification — manual regression

The notification the app posts when a new episode of a subscribed anime airs. Only the
notification is checked here: the channel it uses, how it looks, and what tapping it does.

What decides that an episode aired, and when it looks, belongs to the background update module
and is checked there. The favorites screen is checked in its own file.

## 0. How to run this file

Run it through every pass in the matrix in the root `ANOTI-FULL-REGRESS.md`, and take the
preconditions from there.

Font and display size change the notification too, since the system draws it at the current
size. Check sections 3 and 4 in each pass.

## 1. Getting a notification to appear

The app must be allowed to post notifications first. Grant the permission when asked, or turn
notifications on for the app in system settings.

Then:

1. Open "Main" and subscribe to an ongoing anime whose next episode is due shortly. Use its
   bell.
2. Leave the app and wait for that episode to air.
   - A notification appears without the app being opened.

This wait is the only path a tester has. If it is impractical, subscribe to several ongoing
anime and leave the device overnight — at least one episode will air.

## 2. The channel it uses

Open system settings for the app, then its notification categories.

- There is exactly one category, named "Anime notification channel".
- Its description reads "Notifications about anime".
- It is set to the default importance, the level that shows a banner and makes a sound.
- Vibration is on for it.
- Turning that category off stops the notifications, and nothing else about the app changes.

## 3. What one notification shows

- The small icon in the status bar is the app's own icon.
- The title is the anime's name, the same name the favorites screen shows for it.
- The line below reads "Episode aired: " followed by the episode number.
- The anime's poster is shown as the large image at the right.
- The notification is tinted with the app's own color rather than the system default gray.

Check these variants:

- An anime whose catalog entry has no poster.
  - The notification appears with no large image, and is otherwise complete.
- An anime with a very long name.
  - The title is shortened with an ellipsis rather than pushing anything off the notification.
- An anime whose name or episode number the app does not know.
  - That spot reads "No data" instead. The notification still appears.

## 4. Several notifications at once

1. Get new episodes for two or more subscribed anime.
   - Each one appears as its own notification. A later one never replaces an earlier one.
   - They are collected under one group rather than scattered through the shade.
   - The group's summary line reads "New Episodes".
2. Expand the group.
   - Every anime is listed, each with its own title and episode line.
3. Swipe one notification away and leave the rest.
   - Only that one goes. The others and the group summary stay.
4. Swipe the group summary away.
   - The whole group goes.

## 5. Tapping one

1. Tap a notification while the app is closed.
   - The app opens on the favorites screen.
   - The notification disappears from the shade on its own.
2. Tap one while the app is already open on "Main".
   - The app comes forward and moves to the favorites screen.
3. Tap one while the app is already open on "Favorites".
   - The app comes forward and stays there. Nothing is opened twice.

## 6. Without the permission

1. Deny the notifications permission, or turn the app's notifications off in system settings.
2. Cause a new episode to air for a subscribed anime.
   - No notification appears, and the app does not crash.
   - Opening the app afterward still shows the new-episode mark on the favorites screen.

## 7. With the device in the way

- Get a notification while the screen is locked.
  - It appears on the lock screen, with the same title and line.
- Get one while the device is in battery saver.
  - It still appears, possibly later than it otherwise would.
- Get one, then clear the shade without tapping it.
  - Nothing is left behind. Opening the app still shows the new-episode mark.
