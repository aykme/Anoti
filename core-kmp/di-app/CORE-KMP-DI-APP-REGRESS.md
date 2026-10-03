# Manual regression — the app's wiring

This module builds every shared piece of the app once, at startup, and hands it to whatever asks
for it. It draws nothing. A fault here does not look like a wrong value on a screen: it looks
like the app failing to start, or a screen opening with nothing in it at all. The checks below
are only about that. Everything a screen then shows is checked in that screen's own file.

Unless a step says otherwise, start from a clean installation, with the device online.

## The app starts

1. Install the app and open it.
   It opens onto the main screen within a few seconds. No crash, no blank screen that never
   fills, and no dialog saying the app stopped.
2. Close the app fully and open it again, five times in a row.
   It opens every time.

## Every screen gets what it needs

1. Open the app and let the main screen load.
   Anime are listed. A screen that got nothing to reach the server with would stay empty or fall
   straight into its error state with the network working.
2. Switch on a bell, then open **Favorites**.
   The anime is listed there. A screen that got no store to read from would show nothing.
3. Switch between the main screen and **Favorites** twenty times.
   Both keep working. The app does not slow down noticeably and does not crash.

## Starting when things are against it

1. Put the device in airplane mode and open the app from cold.
   It still opens onto the main screen and shows its error state there. It does not fail to
   start.
2. With the app open on **Favorites**, run `adb shell am kill com.alekseivinogradov.anoti` to kill
   it the way the system would under memory pressure, then return to it from the recents list.
   It rebuilds and shows a working screen rather than crashing.
3. Open the app, then in the system settings force-stop it, then open it again from the launcher.
   It opens normally and the saved anime are still there.

## Platforms

The steps name Android's tools. On the iPhone:

- Instead of `adb shell am kill`, start the app from Xcode, go to the home screen and stop it in
  Xcode, then open it again from the home screen. Instead of a force-stop, swipe the app away in
  the app switcher.
- On an iPhone with Background App Refresh on for the app and Low Power Mode off, start the app
  from Xcode and read its log in Xcode's console. No line ends in "is missing from the
  Info.plist", and none contains "the next background refresh was refused". Either one means the
  app does not update in the background until it is fixed. The Simulator refuses every refresh,
  so there the second line always appears.
