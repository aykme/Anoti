# Bottom navigation bar — manual regression

The bar along the bottom of the app, with a "Main" tab and a "Favorites" tab. Only the bar is
checked here: how it looks, what it does when tapped, and the badge on "Favorites".

The two screens it switches between are checked in their own files. So is the dialog that can
appear over it at launch.

## 0. How to run this file

Run it through every pass in the matrix in the root `ANOTI-FULL-REGRESS.md`, and take the
preconditions from there.

## 1. Where it is and what it holds

The bar sits at the very bottom of the screen and is present on both screens. It never
scrolls away and is never covered by the content above it.

Left to right it holds two tabs of equal width:

- "Main" — a house icon above the word "Main".
- "Favorites" — a heart icon above the word "Favorites".

Both labels are always shown, never only on the open tab.

The bar itself is black, and stays black down to the bottom edge of the screen, below the
system navigation area. There is no gap, and no lighter strip, between the bar and that edge.

## 2. Which tab looks open

- The open tab's icon and label are red-orange.
- The other tab's icon and label are light gray.
- Exactly one tab is red-orange at any moment, never both and never neither.

Check this on arrival: the app opens on "Main", so "Main" is the red-orange one.

## 3. Switching between the tabs

1. Tap "Favorites".
   - The screen above the bar changes to the favorites screen.
   - "Favorites" turns red-orange and "Main" turns light gray.
2. Tap "Main".
   - The screen changes back and the colors swap back.
3. Tap back and forth several times in a row, quickly.
   - Every tap lands. The bar never ends up with both tabs gray, or with the highlight on one
     tab while the other tab's screen is showing.

## 4. Tapping the tab that is already open

1. With "Main" open, tap "Main" again.
   - Nothing visibly changes. The screen is not reloaded and does not flash a spinner.
   - The tab stays red-orange.
2. Repeat on "Favorites" with the favorites screen open.
   - Same result.

## 5. The badge on "Favorites"

The badge is a small filled circle at the top right of the heart icon, red-orange with dark
digits. It counts the subscribed anime that have a new episode the user has not looked at.

1. With nothing subscribed, look at the heart icon.
   - There is no badge at all. Not a badge showing "0" — no badge.
2. Subscribe to an anime and let a new episode arrive for it.
   - The badge appears with "1" on it.
   - It appears while the bar is on screen, without leaving or reopening the app.
3. Open the favorites screen and clear that anime's new-episode mark.
   - The badge goes back to showing nothing, again with no "0" left behind.
4. Get new episodes on several subscribed anime.
   - The badge shows how many, and the number goes up and down as marks are set and cleared.

The badge only ever counts anime with a new episode. Subscribing to more anime without new
episodes does not change it.

## 6. What a screen reader says

With the screen reader on, move focus onto each tab.

1. Focus "Main".
   - It reads the word "Main", says it is a tab, and says whether it is selected.
2. Focus "Favorites" while there is no badge.
   - It reads "Favorites", says it is a tab, and says whether it is selected.
3. Focus "Favorites" while the badge shows a number.
   - It reads "Favorites", then that same number with "with new episodes", and still says it is
     a tab and whether it is selected.
   - The number it speaks matches the digits drawn on the badge.
4. Swipe through the whole screen with the reader.
   - Each tab is reached once. The badge is not announced a second time on its own.

## 7. Scale and orientation

- At maximum font size and display size both labels stay fully readable and are not cut off
  at either end. The bar grows no taller than it needs to.
- Both tabs still respond to a tap at that size, across their whole width and height, not only
  on the text.
- On a screen narrower than 600dp the app stays in portrait whichever way the device is held.
  That is expected, and the landscape passes read the same as the portrait ones there.
- On a screen at least 600dp wide the bar spans the full width in landscape, still at the
  bottom, with the two tabs still equal width.

## 8. Leaving and coming back

1. Open "Favorites", send the app to the background, and reopen it.
   - The bar comes back with "Favorites" still red-orange.
2. Open "Favorites", kill the app, and launch it again.
   - The app opens on "Main", with "Main" red-orange.
   - The badge is back to whatever the saved anime actually warrant, not to nothing.
3. Change the font or display size while "Favorites" is open, which restarts the app.
   - The app opens on "Main" again. The bar is drawn at the new size.
