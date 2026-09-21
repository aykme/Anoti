# Manual regression — talking to the server

This module is how the app reaches the anime service: it makes the request, gives up after a
bounded wait, retries a failure a few times, and reports what went wrong. It has no screen of
its own. Every check below is done on a screen that loads anime, and looks only at whether the
request behaved — the screens themselves are checked in their own files.

Unless a step says otherwise, start with the app opened at least once.

## A request that works

1. With the device online, open the app on the main screen.
   Anime load and are shown. Nothing hangs.
2. Switch between the sections of the main screen several times.
   Each section loads its own anime.

## No connection at all

1. Put the device in airplane mode, close the app fully and open it again.
   The main screen ends in its error state and shows a **Connection error** banner. It does not
   sit on a spinner indefinitely: the attempt gives up within about half a minute.
2. Leave airplane mode on and refresh.
   The same thing happens again, with the same wait. The app does not slow down further with
   each attempt and does not crash.
3. Turn airplane mode off and refresh.
   The anime load.

## A connection that drops mid-request

1. With the app on the main screen and loaded, turn the network off and scroll to the bottom to
   load more.
   A **Connection error** banner appears and the items already on screen stay.
2. Turn the network back on within a few seconds, without touching the app, and scroll to the
   bottom again.
   The next page loads. The earlier failure left nothing broken.

## A connection that is very slow

1. Throttle the device's connection hard — for example, set the emulator's network speed to the
   slowest setting — then refresh the main screen.
   Either the anime arrive, or the screen ends in its error state with the connection banner.
   It does not stay on a spinner forever.
2. Remove the throttling and refresh.
   The anime load.

## The server refusing

Needs a way to make the service answer with an error — for example pointing the device at a
proxy that returns a failure status.

1. Make every request come back as a server error, then refresh the main screen.
   The screen ends in its error state with a **Connection error** banner, after retrying a few
   times rather than giving up on the first failure.
2. Make the service answer with something that is not the expected data — a plain text body, for
   example — then refresh.
   The screen ends in its error state and the banner reads **Unknown error**, not
   **Connection error**. A broken answer is not reported as a lost connection.
3. Remove the proxy and refresh.
   The anime load.

## Leaving a screen mid-request

1. On the main screen with the network throttled, start a refresh and immediately switch to
   **Favorites**.
   No error banner appears on **Favorites** because of the abandoned request, and the app does
   not crash.
2. Go back to the main screen.
   It loads normally.
