# Notifications rationale dialog — manual regression

The dialog the app puts up asking for permission to post notifications, while notifications
are not permitted. Only the dialog is checked here: what it shows, and what each way of
answering it does.

What decides to show it, and what the app does once it is answered, are checked in `main`'s
file. The screen behind it is checked in its own file.

## 0. How to run this file

Run it once per pass listed under "Passes" in the root `ANOTI-FULL-REGRESS.md`, from the
starting point given there.

## 1. Reaching the dialog

It appears only while notifications are not permitted, and only just after the app is launched.
Set that up first, then launch.

On Android 13 and newer:

1. Install the app fresh and launch it. A permission prompt appears — that one is the system's,
   not the app's.
2. Decline it, and close the app.
3. Launch the app again.
   - The dialog described below appears, over whichever screen the app opened on.

On Android 12 and older:

1. Install the app fresh, launch it once, then close it.
2. Turn this app's notifications off in system settings.
3. Launch the app again.
   - The dialog described below appears.

Grant the permission at any point and the dialog stops appearing. To get it back on Android 13
and newer and on the iPhone, reinstall the app and decline the system's question again, as in
step 4 of "First launch" in `main`'s file. On Android 12 and older, turn notifications off in
system settings again.

## 2. What the dialog shows

A panel centered on the screen, holding top to bottom:

1. The app's own launcher icon, small, centered.
2. The title "Very nyanportant alert ᨐᵉᵒʷ", in white.
3. The message "Turn on nyatifications to instantly find out information about your favorite
   anime ᓚᘏᗢ", in light gray.
4. Two buttons at the bottom, both pushed to the right edge of the panel.

The buttons are laid out to fit. On a phone-width screen they stack, "Kawaii nya ≽^•⩊•^≼" above
"Angry nya ฅ^•ﻌ•^ฅ". Where there is room for both on one line they sit side by side instead.
Either layout is correct; what matters is that both are fully visible and both can be tapped.

Check each of these:

- Every character above is drawn, the decorative ones included. None is dropped, replaced by an
  empty box, or cut off at either end.
- The panel is near-black and clearly lighter than the dimmed screen around it.
- "Kawaii nya ≽^•⩊•^≼" is red-orange. "Angry nya ฅ^•ﻌ•^ฅ" is light gray. They are the only two
  pieces of text at the bottom of the panel.
- No text touches or crosses the panel's edge.

## 3. Behind the dialog

- The screen the app opened on is still visible around the panel, and dimmed.
- The bottom navigation bar is visible and dimmed too.
- The screen behind cannot be used while the dialog is open. A tap outside the panel only
  closes the dialog, as section 4 checks. It presses, scrolls and switches nothing behind it.

## 4. The four ways to answer it

Each one starts with the dialog open. Bring it back before running the next.

1. Tap "Angry nya ฅ^•ﻌ•^ฅ".
   - The dialog closes. The screen behind it becomes usable again.
   - Nothing else happens: no system prompt, no settings screen, no change to the screen.
2. Tap the dimmed area outside the panel.
   - Same result as 1.
3. Use the system back gesture or button.
   - Same result as 1. The app does not close, and the screen behind does not navigate away.
4. Tap "Kawaii nya ≽^•⩊•^≼".
   - The dialog closes and the app acts on the approval right away.
   - Exactly what follows depends on the platform and is checked in `main`'s file. What
     is checked here is only that the dialog closed and that something followed it — the
     approval was not silently dropped.

After each of 1, 2 and 3, close the app and launch it again. The dialog appears again, since
nothing was granted.

## 5. Long text and large type

- At maximum font size and display size the message wraps onto more lines and the panel grows
  taller. No word is cut off, and the panel stays fully on screen with a margin all round.
- Both buttons stay fully readable at that size, and both still respond to a tap.
- However long the message gets, both buttons stay in view.

## 6. Rotating while it is open

Run this on a screen at least 600dp wide; a narrower one keeps the app in portrait.

1. Open the dialog and rotate the device.
   - The dialog stays open, with the same contents. It does not close, and does not appear
     twice.
2. Answer it with "Angry nya ฅ^•ﻌ•^ฅ", then rotate back.
   - Rotating on its own does not bring the dialog back.

## 7. Leaving and coming back

1. Open the dialog, send the app to the background, and reopen it.
   - The dialog is still open, with the same contents.
2. Open the dialog, kill the app, and launch it again.
   - The dialog appears again, since the permission is still not granted.
   - It appears once, never stacked on top of itself.

## 8. Screen reader

With the screen reader on, open the dialog.

- It reads the title and then the message.
- Both buttons are reachable and are announced by their own labels.
- Nothing behind the dialog is reachable while it is open.

## 9. Platforms

On the iPhone:

- Section 1 follows the Android 13 route: decline the system's prompt on the first launch, and
  the dialog appears from the second launch on. iOS asks only once, so every later launch
  without the permission shows the dialog.
- Accepting the dialog opens the Settings app on this app's notification settings, and never the
  system's prompt again. `main`'s file checks where it leads. A Simulator may open the Settings
  start page instead, so check this on a device.
- There is no system back button, so answer 3 of section 4 does not apply.
- A narrow screen keeps the app upright, as on Android. Section 6 runs on a wide one: an iPad, or
  a foldable iPhone unfolded.
- To kill the app in section 7, start it from Xcode, go to the home screen and stop it in Xcode.
