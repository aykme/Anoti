# Information dialog — manual regression

The dialog the anime list opens from its info button, with links to the app's privacy policy and
support pages. Only the dialog is checked here: what it shows, how it fits each screen, how it
closes, and that it stays open for as long as the app keeps the screen.

The info button and the list behind the dialog are checked in the anime list's file. How a page
opens in the browser, and what happens when no app can open it, are checked in the web page
opener's file.

## 0. How to run this file

Run it once per pass listed under "Passes" in the root `ANOTI-FULL-REGRESS.md`, from the
starting point given there.

Steps run on both platforms unless they name one.

## 1. Reaching the dialog

1. Open the app on "Main" with the search field closed. Tap the info button at the left end of
   the top bar.
   - The dialog described below appears over the list.

## 2. What the dialog shows

A panel centered on the screen, holding top to bottom:

1. The title "Information", in white.
2. "Privacy Policy", and under it "Support", both light gray and underlined.
3. "Close", in red-orange, at the bottom right of the panel.

Check each of these:

- The title and both links start at the same distance from the panel's left edge.
- "Close" is the only text at the bottom of the panel.
- The panel is near-black and clearly lighter than the dimmed screen around it.
- No text touches or crosses the panel's edge. No word is cut off or broken across two lines.
- The panel is about as wide on a tablet or an unfolded foldable as on a phone. It never
  stretches across a wide screen.

## 3. Behind the dialog

- The list is still visible around the panel, and dimmed. The bottom navigation bar is dimmed
  too.
- The screen behind cannot be used while the dialog is open. A tap outside the panel only closes
  the dialog, as section 5 checks. It presses, scrolls and switches nothing behind it.

## 4. The links

1. Tap "Privacy Policy".
   - The browser opens on the page headed "Anoti Privacy Policy". The web page opener's file
     checks the browser's part.
2. Come back to the app.
   - The dialog is still open, with the same contents.
3. Tap "Support", then come back to the app.
   - The browser opened on the page headed "Anoti Support". The dialog is still open.
4. Tap each link on the empty part of its row, to the right of its words.
   - The page opens all the same. Each link answers across the panel's whole width, not only on
     its words.

## 5. Closing it

Each one starts with the dialog open. Open it again before the next.

1. Tap "Close".
   - The dialog closes. Nothing else happens: no browser opens, and the list behind does not
     change.
   - The info button is still there. Tapping it opens the dialog again, with the same contents.
2. Tap the dimmed area outside the panel.
   - The same result as step 1.
3. Use the system back gesture or button.
   - The same result as step 1. The app does not close, and the list does not navigate away.
4. Open a page from the dialog, come back to the app, and tap "Close".
   - The same result as step 1.

## 6. Large type and short windows

1. At maximum font size and display size, open the dialog.
   - The panel grows to fit. Every word stays whole and on screen, and "Information" is not
     broken mid-word.
   - Both links and "Close" stay fully readable, and each still responds to a tap.
2. Wherever the window has room for the whole dialog, try to scroll the links.
   - Nothing in the dialog moves.
3. Make the window too short for the whole dialog: set the system font size to maximum, and put
   the app in split screen in the smaller half. Open the dialog.
   - The links scroll between the title and "Close". The title and "Close" stay in view the whole
     time.

## 7. The screen changing while it is open

Each of these changes the screen while the app runs:

- rotating a wide screen: a tablet, or a foldable unfolded, since a narrower one keeps the app
  upright;
- folding or unfolding a foldable;
- switching the device between dark and light theme;
- changing the system font size;
- changing the display size.

1. Open the dialog and make each change.
   - The dialog stays open, with the same contents, laid out for the new screen. It does not
     close, and does not appear twice.
2. Close it, then make each change again.
   - None of them brings the dialog back.

## 8. Leaving and coming back

1. Open the dialog, go to the home screen, and come back to the app from the recent apps.
   - The dialog is still open, with the same contents.
2. Open the dialog and go to the home screen. End the app's process from a development machine
   with `adb shell am kill com.alekseivinogradov.anoti`, and open the app again from the
   launcher.
   - The dialog is open again, and appears once.
3. Repeat step 2 with the dialog closed.
   - The app comes back with the dialog closed.
4. Open the dialog. Force-stop the app from the system settings, then open it from the launcher.
   - The app starts fresh, with the dialog closed.
5. Set the device up so that the app asks for notifications at its next launch, as section 1 of
   the notifications rationale dialog's file says. Then repeat step 2.
   - Both the information dialog and the notifications dialog appear, one over the other, in
     either order.
   - Each one closes on its own and leaves the other open and usable.

## 9. Screen reader

With the screen reader on, open the dialog.

- It reads the title "Information".
- Each link is read by its own text, with "Open in browser" as what activating it does.
  Activating it opens the page.
- "Close" is read as a button.
- Nothing behind the dialog is reachable while it is open.

## 10. Platforms

On the iPhone:

- There is no system back button, so step 3 of section 5 does not apply.
- VoiceOver reads each link as a button, with "Open in browser" as its hint.
- An iPhone has no split screen. Step 3 of section 6 runs on an iPad instead, in a Stage Manager
  window made as short as it goes.
- A narrow screen keeps the app upright, as on Android. Section 7 rotates a wide one: an iPad, or
  a foldable iPhone unfolded.
- A new text size reaches the iPhone app only when it is opened again, and a Display Zoom change
  restarts the iPhone. Section 7 skips both changes there.
- To end the app in section 8, follow step 2 of "Leaving and coming back" in `main`'s file, with
  its iPhone notes. Instead of a force-stop, swipe the app away in the app switcher.
- Section 8, step 5 follows the rationale dialog's iPhone route: decline the system's question on
  the first launch.
