# Manual regression — the installed app

This module is the app itself: the entry in the launcher, the registering of the notification
channel, and the hourly background check for new episodes. It draws no screens of its own.
Everything a screen shows is checked in its own module's file.

Unless a step says otherwise, start from a clean installation with the app never opened before, on
a device that is online, using a build that has been shrunk and obfuscated the way the released
one is.

## The app in the launcher

1. Install the app and look at the launcher.
   An entry named **Anoti** is there, with the app's icon.
2. Open it.
   The app opens. No crash, no blank screen, no dialog about the app having stopped.
3. Close the app fully and open it again from the launcher.
   It opens again the same way.

## The notification channel

The app registers its channel the first time it is opened, not when it is installed. What the
channel is called and how loud it is belongs to the module that builds it. Only its arrival and
its survival are checked here.

1. Install the app, and without opening it, go to the system settings, to this app's page, and
   open its notification settings.
   No channel is listed yet.
2. Open the app, wait about five seconds, then go back to that same settings page.
   Exactly one channel is listed.
3. Switch that channel off, close the app fully, and open it again. Return to the settings page.
   The channel is still switched off. The app does not turn it back on.
4. Switch the channel back on, then lower its importance to the quietest level. Close the app
   fully, open it again, and return to the settings page.
   The importance stays at the level you chose. The app does not raise it.

## The hourly check for new episodes

Requires at least one saved anime whose next episode is released while the test runs, so a
notification has a reason to appear. Save it, then close the app fully before each check below.

1. Leave the device alone, online and unplugged, for just over an hour.
   A notification about a new episode appears, without the app having been opened.
2. Leave the device alone for another hour.
   The check happens again. Nothing has to be opened to restart it.
3. Turn the device's network off and leave it for just over an hour.
   No notification appears and the app does not crash.
4. Turn the network back on, without opening the app, and leave it for another hour.
   The notification appears again. The failed hour did not stop the check.
5. Take the app's notification permission away in the system settings, then leave it for over an
   hour.
   Nothing appears, nothing crashes, and the permission is still switched off afterward.

## The check survives the device being interrupted

Each of these starts from an app that has been opened at least once.

1. Force-stop the app from the system settings, then leave it closed for over an hour.
   No new-episode notification arrives. A force-stop cancels the app's background work until
   the app is opened again.
2. Open the app once, close it, and leave it for over an hour.
   The notification arrives again.
3. Restart the device, and do not open the app afterward. Leave it for over an hour.
   The notification arrives. Opening the app is not needed after a restart.
4. In the system settings, set this app's battery usage to **Restricted**, then leave it closed
   for several hours without charging the device.
   No notification arrives. The system, not the app, is holding the check back.
5. Set the battery usage back to unrestricted, open the app once, and leave it for over an hour.
   The notification arrives again.
6. On a Samsung device, open the battery settings and find the list of sleeping apps. Take this
   app out of that list if it is there, open the app once, and leave it for over an hour.
   The notification arrives.

## Updating, reinstalling and removing the app

1. Install a newer build over the existing one, without uninstalling first, and do not open the
   app afterward. Leave it for over an hour.
   The notification arrives, and the channel is still listed in the settings. An update does not
   have to be followed by opening the app.
2. With saved anime in the app, uninstall it, install it again and open it.
   On a device with system backup switched on, the saved anime comes back. On a device without
   it, the app opens empty.
3. Close the app fully and leave it for over an hour.
   If anything is saved, the new-episode notification arrives. A fresh installation schedules the
   check the same way an old one does.
4. Uninstall the app and look at the system settings.
   The app's page is gone, and so is its notification channel.
