# Anime notification — manual regression

The notification the app posts when a new episode of a subscribed anime airs. Only the
notification is checked here: the channel it uses, how it looks, and what tapping it does.

What decides that an episode aired, and when it looks, belongs to the background update module
and is checked there. The favorites screen is checked in its own file.

## 0. How to run this file

Run it once, from the starting point given under "Passes" in the root `ANOTI-FULL-REGRESS.md`.
The system draws the notification, so font size, display size and orientation change nothing
the app decides.

Steps run on both platforms unless they name one.

## 1. Getting a notification to appear

The app must be allowed to post notifications first. Grant the permission when asked, or turn
notifications on for the app in system settings. iPhone: Background App Refresh must also be on
for the app, and Low Power Mode off, or the update that posts notifications never runs.

Then:

1. Open "Main" and subscribe to an ongoing anime whose next episode is due shortly. Use its
   bell.
2. Leave the app and wait for that episode to air.
   - A notification appears without the app being opened.

This wait is the only path a tester has. If it is impractical, subscribe to several ongoing
anime and leave the device overnight — at least one episode will air.

## 2. The channel it uses

iPhone: iOS has no notification categories. Check instead that the app's page in Settings, under
Notifications, shows Sounds switched on.

Android: open system settings for the app, then its notification categories.

- There is exactly one category, named "Anime notification channel".
- Its description reads "Notifications about anime".
- It is set to the default importance, the level that makes a sound and does not pop up over
  the screen.
- Vibration is on for it.
- Turning that category off stops the notifications, and nothing else about the app changes.

## 3. What one notification shows

- It arrives with the device's default notification sound, and vibrates where the device
  vibrates for notifications.
- Android: the small icon in the status bar is the app's notification icon.
- The title is the anime's name, the same name the favorites screen shows for it.
- The line below reads "Episode aired: " followed by the episode number.
- The anime's poster is shown in the notification.

## 4. Several notifications at once

1. Get new episodes for two or more subscribed anime.
   - Each one appears as its own notification. While fewer than twenty are in the shade, not
     counting the group summary, a later one never replaces an earlier one. Section 8
     checks what happens at twenty, on Android only.
   - They are collected under one group rather than scattered through the shade.
   - Android: the group's summary line reads "New Episodes". iPhone: they show as one stack,
     with no summary line.
2. Expand the group.
   - Every anime is listed, each with its own title and episode line.
3. Swipe one notification away and leave the rest.
   - Only that one goes. The others stay. Android: the group summary stays too.
4. Android only, since the iPhone stack has no summary: swipe the group summary away.
   - The whole group goes.
5. With the app open, get a new notification. iPhone: the hourly update never runs while the app
   is open. Once an episode has aired and is not yet announced, open "Favorites" and pull the
   list down instead.
   - It plays its sound and goes into the notification list. No banner slides over the app.
     The iPhone shows a banner only while the app is not on screen, as the user's Settings
     choose.

## 5. Tapping one

With several notifications in the shade, they form one group, as section 4 checks. Expand it
and tap one notification in it.

1. Tap a notification while the app is closed.
   - The app opens on the favorites screen. On the iPhone another screen may show for a moment
     first.
   - The notification disappears from the shade on its own.
   - Go to the home screen and open the app from the launcher. It comes back on favorites, not
     on a second, fresh list.
2. Tap one while the app is already open on "Main".
   - The app comes forward and moves to the favorites screen.
3. Tap one while the app is already open on "Favorites", scrolled down.
   - The app comes forward on favorites as it was left, still scrolled down. Nothing reloads
     and nothing is opened twice.

## 6. Without the permission

1. Deny the notifications permission, or turn the app's notifications off in system settings.
2. Cause a new episode to air for a subscribed anime.
   - No notification appears, and the app does not crash.
   - Opening the app afterward still shows the new-episode mark on the favorites screen.

## 7. With the device in the way

- Android: get one while the device is in battery saver.
  - It still appears, possibly later than it otherwise would.
- iPhone: turn Low Power Mode on and wait for an episode to air.
  - Nothing arrives while it is on, since iOS stops background refresh. Once it is off, the
    notification comes with a later update.
- Get one, then clear the shade without tapping it.
  - Nothing is left behind. Opening the app still shows the new-episode mark.

## 8. Twenty at most, and which one gives way

Android only, since forcing a pass needs `adb` and an emulator. Waiting for twenty real
episodes is impractical, so this section forces the background update on an emulator. Each
forced pass posts one notification for every subscribed anime. A notification that replaces an
older one plays its sound again.

Everything here is checked in the notification shade. Expand the app's group to see each
notification. Each one shows how long ago it arrived. The clock moves a day forward before every
pass, so the five notifications of one pass share an age, and the passes differ by a day.

Preparation:

1. Use an Android emulator running Android 15 or newer, with a Google APIs system image. Images
   with Google Play refuse the `adb root` below.
2. Make sure exactly five ongoing anime are subscribed with their bell, and no others. Every
   subscribed anime posts in every pass, so a sixth one breaks every count below.
3. Allow notifications for the app, keep the network on, and clear the shade.
4. On the emulator, run `adb root`.
5. Turn off automatic date and time: `adb shell settings put global auto_time 0`.

One forced pass is three commands:

1. Make every subscribed anime look behind:
   `adb shell "sqlite3 /data/data/com.alekseivinogradov.anoti/databases/anoti_anime_table
   'UPDATE anoti_anime_table SET episodes_aired = 0, is_new_episode = 0;'"`.
2. Read the device time with `adb shell date +%s`, add 86400 to it, and set the result:
   `adb shell date @<the sum>`. The clock only ever moves forward in this section.
3. Find the update job's number in `adb shell dumpsys jobscheduler`, on the line ending in
   `#AnimeUpdateWorker#`, and run it: `adb shell cmd jobscheduler run -f -n
   androidx.work.systemjobscheduler com.alekseivinogradov.anoti <number>`.

The steps:

1. Start with an empty shade. Make one forced pass.
   - Five notifications appear, plus the group summary.
2. Send the app to the background with the home button. End its process with
   `adb shell am kill com.alekseivinogradov.anoti`. Make one forced pass.
   - Five new notifications appear beside the first five. The first five are all still there,
     one day older.
3. Make two more forced passes.
   - Twenty notifications are in the group: four ages, five of each.
4. Make one more forced pass.
   - Still twenty. The five oldest are gone, and five new ones with the newest age are there.
     The other fifteen are untouched.
5. Swipe away one of the five newest notifications. Make one more forced pass.
   - Still twenty. From the oldest age to the newest, the group holds 1, 5, 5, 4 and 5
     notifications. The swiped one's place went to a new one, and only four of the oldest gave
     way.
6. Tap one of the five newest notifications, then return to the launcher. Make one more forced
   pass.
   - Still twenty. From the oldest age to the newest, the group holds 2, 5, 4, 4 and 5
     notifications. The tapped one freed its place the same way.
7. Turn automatic date and time back on: `adb shell settings put global auto_time 1`.

Optional, for a closer look: `adb shell dumpsys notification --noredact` lists the app's
notifications with their numbers. Singles use 10 to 29, and the summary uses 0. A freed number is
taken first, and when none is free, the number whose notification is oldest is reused.

## 9. Platforms

- The group. Android: a "New Episodes" summary over it. iPhone: a stack with no summary line.
- A notification while the app is open. Android: the sound, no banner. iPhone: the sound, into
  the list, no banner.
- A notification while the app is not on screen. Android: the sound and a status-bar icon, no
  banner. iPhone: a banner, as the user's Settings choose.
- The category in system settings. Android: one, "Anime notification channel". iPhone: none,
  only the app's own notification settings.
