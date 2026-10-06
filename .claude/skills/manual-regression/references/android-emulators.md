# Android emulators

## Contents

- Devices and their state
- Scale, orientation and foldables
- Launching and restarting
- Background work and notifications
- Evidence
- Cleanup

## Devices and their state

- Address every command to one device (`adb -s <serial>`), and refuse any serial that is not an
  emulator. Name media folders by AVD name, not by port.
- Settings live in the device's user data and survive cold boots, even with snapshots off. After
  every boot read back and reset font scale, display density, dark mode, rotation, navigation mode,
  battery and network.
- Check the screen reader right after boot. Turn it off and restart the app before walking; turn it
  on only for its own steps.
- Images with Google Play have no `adb root`. Plan every check that needs root (editing app
  databases) on a Google APIs image.
- Bring a permission dialog back without reinstalling, which keeps every other state:
  `pm revoke <pkg> <perm>`, then `pm clear-permission-flags <pkg> <perm> user-set user-fixed`. No
  root is needed. The revoke kills the app's process; start the app again from the launcher.
- An AVD with a hardware keyboard hides the soft keyboard; check the AVD before keyboard steps.
- Airplane mode may leave Wi-Fi on. Check the network really dropped.
- A Play image may block the first `adb install` with a Play Protect dialog.
- When a device comes back from the developer, check which build is installed before trusting its
  state (`dumpsys package <pkg>`: version, debuggable flag).
- Before walking, check the built artifact: not debuggable, its obfuscation mapping present, no
  missing-rules report. Copy it aside so a later build cannot overwrite it.

## Scale, orientation and foldables

- Set font scale and density from the console (`settings put system font_scale`, `wm density`), then
  read them back. Settings sliders are slow and stop short of the maximum.
- Measure the smallest width (`am get-config`) at maximum display size before judging rotation or
  column steps: a tablet can drop below the large-screen threshold.
- Read the current orientation before a swipe. A fast swipe from the bottom of a tablet opens
  recents and looks like a crash. A tablet's natural orientation is often landscape.
- Lock rotation with `wm user-rotation lock <n>`; `settings put system user_rotation` may be
  ignored.
- Foldables: `adb emu fold` / `unfold`; density is per display (`wm density -d <id>`), screenshots
  too (`screencap -d <id>`). Whether an app follows to the outer screen is a system setting; set it
  before fold steps.

## Launching and restarting

- Launch like the launcher: `monkey -p <pkg> -c android.intent.category.LAUNCHER 1`. `am start -n`
  stacks a second activity and fakes lost state. Check the task holds one activity.
- Tell an activity rebuild from process death by the process id. Which configuration changes
  rebuild the activity is decided by the manifest, so check it before calling a rebuild a bug.
- `am kill` works only on a background process; wait until the process is really gone.

## Background work and notifications

- Force a periodic job through its scheduler's own state: with the app stopped and root, move the
  job's last enqueue time back in the scheduler's database. Shifting the device clock makes the
  scheduler skip passes silently for the length of the shift. Shift the clock only where ages
  matter, as the last step on that device, then restore it and reinstall.
- A force-stop cancels the app's scheduled jobs and its notifications.
- A forced pass with no result line in the log is a tool problem; repeat it.
- Edit an app database only while the app is dead. A root edit can leave journal files with the
  wrong security label, which the app then cannot open; delete them.
- Emulators report the charger unplugged and enter Doze early. Record idle state, battery and
  standby bucket; keep the device awake where a real hour must pass.
- Never clear all notifications mid-check (`service call notification 1` does that). A collapsed
  notification group has no tap action: expand it and tap one notification.
- A backup check through the local transport leaves data that restores itself on every later
  install. Wipe it and switch the transport back afterward.

## Evidence

- `uiautomator dump` gives the visible texts cheaply. Parse it as XML. It fails while an endless
  animation runs.
- Record flashes with `screenrecord`; stills miss short states.
- Keep one log capture per device, filtered by package, restarted after root or reboot. Search it
  for `FATAL EXCEPTION`, `ANR in`, `ClassNotFoundException`, `NoSuchMethodException` and
  serialization errors.

## Cleanup

Uninstall what the walk installed and reset every setting it changed. Wipe test backups and
delete the walk's temporary files on the device. Shut each emulator down as soon as its part is
done.
