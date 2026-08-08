# Installing Pharos

> **Status: planned.** There is no published release yet. Until there is, build
> from source — see [../developer/development.md](../developer/development.md).

Pharos installs as an ordinary APK. It requires no root, no bootloader
modification, and no Google Play Services.

## Requirements

| | |
|---|---|
| Android | 8.0 (API 26) or newer |
| Google services | not required, and never required |
| Permissions | network access; camera and microphone only if you enable those features |
| Storage | tens of megabytes; Pharos does not accumulate media or history |

Fire OS 8 (API 30) on a Fire TV Stick 4K is the reference target — every release
is verified on one.

## From a release

1. Download the APK and its checksum from the
   [Releases page](https://github.com/nikolareljin/pharos/releases).
2. Verify it before installing:
   ```sh
   sha256sum -c pharos-<version>.apk.sha256
   ```
   A checksum that does not match means you did not get the file we published.
   Do not install it.
3. Install:
   ```sh
   adb install -r --user 0 pharos-<version>.apk
   ```

`--user 0` matters. Without it the install can land in a work profile or secure
folder, report `Success`, and leave the app missing from the launcher. Confirm
where it actually went:

```sh
adb shell pm list packages --user 0 | grep pharos
```

## On a phone or tablet, without a computer

Enable installation from unknown sources for your browser or file manager, open
the downloaded APK, and accept the prompt. Verify the checksum first if your
file manager can show it.

## On a TV box or streaming stick

TV devices generally have no browser, so install over the network with ADB:

```sh
adb connect <DEVICE_IP>:5555     # e.g. 192.0.2.42:5555
adb devices                      # confirm it says "device", not "unauthorized"
adb install -r --user 0 pharos-<version>.apk
adb shell am start -n io.github.nikolareljin.pharos/.MainActivity
```

Approve the debugging prompt that appears on the TV the first time. The Fire TV
specifics — where developer options hide, and how to find the address — are in
[fire-tv.md](fire-tv.md).

**Turn ADB off again when you are done.** An open ADB port is a full remote
shell on that device for anyone on the network.

## Upgrading

Install the new APK over the old one with `-r`. Configuration, node identity and
paired credentials survive. Downgrading is not supported; uninstall first, which
clears the node identity — a node that comes back with a new identity has to be
re-paired.

## Uninstalling

```sh
adb uninstall io.github.nikolareljin.pharos
```

This removes the node identity and all local configuration. If the node was
paired with a controller, revoke it there as well; an uninstalled node still
occupies its registration until you do.
