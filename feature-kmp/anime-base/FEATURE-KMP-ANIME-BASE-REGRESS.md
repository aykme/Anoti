# Anime base — manual regression

This module has no screen of its own. It holds two things a tester can reach: the pull-down
gesture that both anime screens share, and the connection to the anime catalog that fills them.

The screens themselves are checked in their own files. So are the error banners at the bottom
of the app. Only what this module contributes is checked here.

## 0. How to run this file

Run it through every pass in the matrix in the root `ANOTI-FULL-REGRESS.md`, and take the
preconditions from there.

Both sections below are run twice, once on "Main" and once on "Favorites", because the same
piece is used on both.

## 1. Pulling down to reload

The gesture works by dragging the list itself, so it needs a list under the finger. Where the
screen shows a picture and a block of text instead of a list, there is nothing to drag.

1. On a screen showing a list of anime, scrolled to the very top, drag downward.
   - A round spinner is pulled down from under the top edge, following the finger.
   - The spinner is red-orange on a white circle.
2. Keep dragging past a short distance, then let go.
   - The spinner snaps back up and the screen reloads.
3. Drag down only a little, then let go.
   - The spinner snaps back and nothing reloads. The list is left exactly as it was.
4. Drag downward while the list is scrolled part-way down, not at the top.
   - The list scrolls up as usual. No spinner appears, and nothing reloads.
5. Drag upward anywhere.
   - The list scrolls. No spinner appears, and nothing reloads.
6. On a screen showing a picture and a block of text instead of a list, drag downward.
   - Nothing moves at all. No spinner, no reload, no bounce.
7. On a screen showing the broken-plug error picture, drag downward.
   - Nothing moves, for the same reason.
8. Start a pull and, without letting go, drag back up past where the pull started.
   - The spinner retreats and nothing reloads when the finger lifts.
9. Pull down, let go, and pull again immediately while the reload is still running.
   - The second pull is accepted or ignored, but nothing is left half-drawn. The spinner never
     stays stuck on screen.

## 2. Where the anime come from

Everything the two screens show about an anime is read from the online catalog through this
module. These checks confirm it arrives complete and correct.

1. Open "Main" online and look at the "On air" list.
   - Every row carries a title, a poster, an episode count and a status.
   - The status on every row reads "Ongoing".
2. Look at the "Soon" list.
   - The status on every row reads "Announced".
3. Find an anime whose catalog entry has no poster.
   - The row shows the standard placeholder rather than an empty space or a broken image.
4. Compare any row against the same anime on shikimori.io.
   - Title, episode count, score and status match.
   - The score is shown to two decimal places.
5. Turn the network off and open the app fresh.
   - The screens go to their own error or empty state. The app does not crash and does not hang
     on a spinner for good.
6. Turn the network back on and pull down to reload.
   - The lists fill in.
7. Open an anime whose catalog entry is missing a field, for example an announced anime with no
   episode count.
   - The row shows the app's own placeholder text in that spot, never the word "null" and never
     a blank gap.
