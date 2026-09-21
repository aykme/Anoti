# Manual regression — the app's wiring

This module builds every shared piece of the app once, at startup, and hands it to whatever asks
for it. It draws nothing. A fault here does not look like a wrong value on a screen: it looks
like the app failing to start, or a screen opening with nothing in it at all. The checks below
are only about that.

Unless a step says otherwise, start from a clean installation, with the device online.

## The app starts

1. Install the app and open it.
   It opens onto the main screen within a few seconds. No crash, no blank white or black screen
   that never fills, and no dialog saying the app stopped.
2. Close the app fully and open it again, five times in a row.
   It opens every time.
3. Open the app, press home, and return to it from the recents list.
   It comes back to the screen you left, with its contents still there.

## Every screen gets what it needs

1. Open the app and let the main screen load.
   Anime are listed. A screen that got nothing to talk to the server with would stay empty or
   fall straight into its error state with the network working.
2. Switch on a bell, then open **Favorites**.
   The anime is listed there. A screen that got no store to read would show nothing.
3. Switch between the main screen and **Favorites** twenty times.
   Both keep working. The app does not slow down noticeably or crash after repeated switching.

## Startup with the odds against it

1. Put the device in airplane mode and open the app from cold.
   It still opens onto the main screen and shows its error state there. It does not fail to
   start.
2. Free memory aggressively — open several heavy apps until Android kills Anoti in the
   background — then return to Anoti from recents.
   It rebuilds and shows a working screen rather than crashing.
3. Install the app and, without opening it, wait for the hourly background check to run.
   The check runs without the app having been opened, and afterwards the app still opens
   normally.
