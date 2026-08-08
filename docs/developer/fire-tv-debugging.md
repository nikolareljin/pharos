# Debugging on a Fire TV

Everything here also applies to Android TV boxes and other headless Android
devices. The Fire TV Stick is called out because it is the reference device and
because two of its properties surprise people: there is no usable USB data port,
and the app appears on the home screen through its banner rather than its icon.

All addresses below are from the RFC 5737 documentation ranges. Substitute your
own device's address.

## Connect over the network, not the cable

**The stick's port supplies power. It does not carry ADB.** Plugging it into a
laptop charges it and nothing else — no device appears in `adb devices`, and
`lsusb` shows nothing to attach to. Every debugging session with a stick goes
over the network.

Larger boxes and some developer hardware do expose USB debugging; if `adb
devices` lists your device when plugged in, use that and skip to
[Install and launch](#install-and-launch).

## 1. Enable developer access on the device

Menu wording changes between Fire OS versions; the shape is stable.

1. **Settings → My Fire TV → About**
2. Select the **device name** repeatedly — around seven times — until it
   confirms developer options are enabled
3. Back to **My Fire TV → Developer Options**
4. Turn on **ADB Debugging**

`Apps from Unknown Sources` is a separate switch and is **not** needed for
`adb install`. Leave it off unless you are sideloading through a file manager on
the device itself.

## 2. Find the device address

**Settings → My Fire TV → About → Network** shows the IP address.

If you would rather not walk the menus, your router's client list works too.
What does *not* work is scanning for the ADB port before step 1: the port is
closed until ADB Debugging is enabled, so a scan finds nothing and tells you
only that you have not finished step 1 yet.

## 3. Connect and authorise

```sh
adb connect 192.0.2.42:5555
adb devices
```

The first connection raises a dialog **on the television** asking whether to
allow debugging from this computer. Accept it. Tick *always allow* if this is
your own machine, otherwise you will re-approve on every reconnect.

`adb devices` must show `device`:

```
List of devices attached
192.0.2.42:5555    device
```

Anything else means you are not connected yet — see
[Troubleshooting](#troubleshooting).

## Install and launch

The project CLI handles the whole cycle, including a check that catches a
failure mode `adb` reports as success:

```sh
./dev deploy --device 192.0.2.42:5555
```

By hand:

```sh
./gradlew assembleDebug
adb -s 192.0.2.42:5555 install -r --user 0 app/build/outputs/apk/debug/app-debug.apk
adb -s 192.0.2.42:5555 shell pm list packages --user 0 | grep pharos
```

Two things to know before the first install:

**`--user 0`, then verify.** An install into another profile prints `Success`
and leaves the app absent from the launcher. The `pm list packages --user 0`
line above is the confirmation; `./dev deploy` runs it for you and fails loudly
when the package is not visible.

**Debug builds carry a `.debug` suffix.** The debug application id is
`io.github.nikolareljin.pharos.debug`, so debug and release can sit side by side
on one device. Use the suffixed name in every `adb shell` command against a
debug build:

```sh
adb shell am start -n io.github.nikolareljin.pharos.debug/io.github.nikolareljin.pharos.MainActivity
```

Confirm it is actually in the foreground rather than trusting the launch
command:

```sh
adb shell dumpsys activity activities | grep -i topResumedActivity
```

## Driving the remote from a keyboard

Pharos maps physical keys to logical actions, so a Fire remote, a game
controller and a Bluetooth keyboard all produce the same navigation. That also
means you can drive the whole UI over ADB without touching the remote:

| Key event | Action |
|---|---|
| `19` | Up |
| `20` | Down |
| `21` | Left |
| `22` | Right |
| `23` | Select |
| `4` | Back |
| `82` | Menu |
| `85` | Play/Pause |

```sh
adb shell input keyevent 20    # down
adb shell input keyevent 23    # select
```

This is the fastest way to check focus behaviour: walk the whole screen with
`20`/`22` and watch whether every focusable element is reachable and whether the
focus indicator is visible at television viewing distance.

## Watching what the app does

```sh
adb logcat -c                                   # clear first, so you read this run
adb logcat -d | grep -i pharos                  # dump what happened
adb logcat -v time | grep -iE 'pharos|AndroidRuntime'   # follow, including crashes
```

Include `AndroidRuntime` — that is where an uncaught exception lands, and it
will not contain the word "pharos".

**Logs are written to be shareable**: no tokens, no auth headers, no
credential-bearing URLs. If you ever see a secret in `logcat` or in a
Diagnostics export, that is a vulnerability rather than an inconvenience —
report it through a
[private advisory](https://github.com/nikolareljin/pharos/security/advisories/new)
rather than pasting it into an issue.

## Screenshots and recordings

```sh
adb exec-out screencap -p > screen.png
adb shell screenrecord --time-limit 20 /sdcard/demo.mp4
adb pull /sdcard/demo.mp4
```

`./dev screenshot` and `./dev record` wrap these and drop the output in
`docs/screenshots/`, which is ignored by git.

## Instrumented tests

The focus and navigation tests need a real device or an emulator:

```sh
./gradlew connectedDebugAndroidTest
```

Run them against the Fire TV when you change anything about focus, navigation or
layout. A phone will happily pass tests that a D-pad-only device fails, because
a phone can reach a control by touch that the remote cannot reach at all.

## Device facts worth capturing in a bug report

```sh
adb shell getprop ro.product.model            # AFTKM on the reference stick
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk        # 30 on Fire OS 8
adb shell getprop ro.product.cpu.abi
adb shell cat /proc/meminfo | head -3
adb shell df -h /data
adb shell dumpsys meminfo io.github.nikolareljin.pharos.debug
```

The in-app **Diagnostics** screen reports most of this too, and its export is
already sanitised for sharing.

## Troubleshooting

| Symptom | Cause | What to do |
|---|---|---|
| `unable to connect ... Connection refused` | ADB Debugging is off, or the address is wrong | Re-check step 1; re-read the address from **About → Network** |
| Device shows as `unauthorized` | The dialog on the TV was not accepted | Look at the screen and accept it. If no dialog appeared, `adb kill-server && adb connect …` |
| Device shows as `offline` | Stale connection after a reboot or sleep | `adb disconnect <addr>` then `adb connect <addr>` |
| `device not found` after it worked | The stick slept or changed address | Reconnect; give it a DHCP reservation if it moves often |
| `Success`, but nothing in the launcher | Installed into another profile, or the banner is missing | Check `pm list packages --user 0`; a TV entry needs `android:banner` |
| App installed but `am start` cannot find it | Using the unsuffixed id against a debug build | Add `.debug` to the application id |
| Focus disappears while navigating | A bug, not a configuration problem | File an issue with the screen, the key pressed, and `logcat` output |

## Turn it off when you are finished

ADB over the network is a remote shell on that device. Once a workstation key is
authorised it stays authorised, and the port stays open to the network for
anyone else to attempt a connection.

```sh
adb disconnect 192.0.2.42:5555
```

Then switch **ADB Debugging** back off under **Developer Options**. Treat leaving
it on as a decision rather than a default: acceptable on a lab network you
control for the duration of a debugging session, not on a shared or guest
network, and not indefinitely on a device sitting in a communal room.

If you ever need to revoke previously granted workstations, **Developer Options
→ Revoke USB debugging authorisations** (wording varies) clears every key the
device has accepted.
