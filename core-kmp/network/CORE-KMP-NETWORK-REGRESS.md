# Manual regression — talking to the server

This module is how the app reaches the anime service: it makes the request, gives up after a
bounded wait, retries a failure a few times, and classifies what went wrong. It has no screen of
its own. Every check below is done on a screen that loads anime and looks only at whether the
request behaved — how the screen then reacts is checked in its own file.

Unless a step says otherwise, start with the app opened at least once.

## A request that works

1. With the device online, open the app on the main screen.
   Anime load and are shown. Nothing hangs.
2. Switch between the sections of the main screen.
   Each section loads its own anime.
3. Leave the app on a loaded list for a few minutes without touching it, then scroll to load more.
   The next page loads. A connection that has been idle does not have to be re-established by
   hand.

## No connection at all

1. Put the device in airplane mode, close the app fully and open it again.
   The main screen ends in its error state. It does not sit on a spinner: it gives up within
   about half a minute.
2. Leave airplane mode on and refresh.
   The same thing happens again, with about the same wait. The app does not take longer with each
   attempt and does not crash.
3. Turn airplane mode off and refresh.
   The anime load.

## A connection that drops mid-request

1. With the main screen loaded, turn the network off and scroll to the bottom to load more.
   The screen reports a failure and the anime already on screen stay.
2. Turn the network back on within a few seconds, without touching the app, and scroll to the
   bottom again.
   The next page loads. The earlier failure left nothing broken.

## A connection that is very slow

1. Throttle the device's connection hard — for example, set the emulator's network speed to the
   slowest setting — then refresh the main screen.
   Either the anime arrive, or the screen ends in its error state within about half a minute. It
   never stays on a spinner indefinitely.
2. Remove the throttling and refresh.
   The anime load.

## The server answering badly

Needs a way to make the service answer differently — for example a proxy in front of the device.

1. Make every request come back as **500 Internal Server Error**, then refresh the main screen.
   The screen ends in its error state after about half a minute, not instantly: the request is
   retried several times before it gives up.
2. Make every request come back as **404 Not Found** and refresh.
   The same: retried, then the error state after about the same wait. A refused request is
   treated no more quickly than a broken one.
3. Make the service answer **200** with a body that is not the expected data — plain text, for
   example — and refresh.
   The screen ends in its error state after about the same wait, and the message it shows is the
   one for an unexpected problem, not the one about the connection.
4. Make the service answer correctly but with extra unknown fields added to each anime.
   The anime load normally. Unknown fields are ignored, not treated as an error.
5. Remove the proxy and refresh.
   The anime load.

## Leaving a screen mid-request

1. On the main screen with the network throttled, start a refresh and immediately switch to
   **Favorites**.
   No failure message appears on **Favorites** because of the abandoned request, and the app does
   not crash.
2. Go back to the main screen.
   It loads normally.
