# Web page opener — manual regression

This module hands a web page to the system's browser. It draws nothing of its own. Its code runs
when a link in the information dialog on "Main" is tapped, the only place the app opens a page.
Only the hand-over is checked here: which page opens, where it opens, coming back, and what
happens when nothing can open it.

The information dialog itself, its look and how it closes, is checked in its own file.

## 0. How to run this file

Run it once, from the starting point given under "Passes" in the root `ANOTI-FULL-REGRESS.md`.
The browser draws the page, so font size, display size and orientation change nothing this module
decides.

Steps run on both platforms unless they name one.

## 1. Reaching it

Open the device's browser once before the walk, so its own first-run screens are out of the way.
If the device asks which app should open a page, pick a browser. That question is the system's.

1. Open the app on "Main" with the search field closed. Tap the info button at the left end of
   the top bar.
   - The information dialog opens, with the links "Privacy Policy" and "Support".

## 2. Opening each page

1. Tap "Privacy Policy".
   - The browser opens and fills the screen. The page is not shown inside the app.
   - Its address is `https://aykme.github.io/Anoti/privacy/`, and its heading reads "Anoti
     Privacy Policy".
2. Press system Back in the browser.
   - The app comes back as it was left, with the information dialog still open.
3. Tap "Support".
   - The browser opens on `https://aykme.github.io/Anoti/support/`, and its heading reads "Anoti
     Support".
4. Return to the app from the recent apps instead of pressing Back.
   - The app comes back as it was left, with the information dialog still open.
5. Turn the device's network off and tap either link.
   - The browser opens and shows its own page about the missing connection. The app shows no
     error message, since the page was handed over.
6. Turn the network back on. Tap a link twice quickly.
   - The page opens, and the app does not crash. Coming back, the dialog is open once, not
     stacked on itself.

## 3. When no app can open a page

Android only: every iPhone has Safari, so a page always has an app to open it.

Preparation, on an emulator or a device connected to a development machine:

1. List the apps that open web pages:
   `adb shell cmd package query-activities --brief -a android.intent.action.VIEW -c
   android.intent.category.BROWSABLE -d https://aykme.github.io/Anoti/privacy/`.
2. Disable every app on that list: `adb shell pm disable-user --user 0 <package>`.

The steps:

1. Open the information dialog and tap "Privacy Policy".
   - Nothing opens, and the app does not crash.
   - The dialog stays open.
   - An "Unknown error" banner appears above the bottom bar and goes away on its own. While the
     dialog is open, the banner sits under the dialog's dimmed backdrop. That is expected.
2. Tap "Support".
   - The same result as step 1.
3. Enable every app the preparation disabled: `adb shell pm enable <package>`. Tap a link
   again.
   - The page opens, as in section 2.

## 4. Leaving and coming back

1. Open a page from the dialog. While the browser is on screen, end the app's process from a
   development machine: `adb shell am kill com.alekseivinogradov.anoti`. Then press Back in the
   browser.
   - The app starts again where it was left, with the information dialog open over it.
   - It does not crash, and the dialog appears once.

## 5. Platforms

On the iPhone:

- The page opens in Safari, or in the browser the user chose as the default.
- There is no system Back. Step 2 of section 2 returns to the app with the "◀ Anoti" link iOS
  shows in the top-left corner of the screen.
- Section 3 does not apply.
- Section 4 ends the app as step 2 of "Leaving and coming back" in `main`'s file says, with its
  iPhone notes, while Safari is on screen. Return with the "◀ Anoti" link.
