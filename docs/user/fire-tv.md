# Fire TV

The Amazon Fire TV Stick 4K (2nd generation, model `AFTKM`) is the first
reference device. Every meaningful release is verified on one. Pharos is not a
Fire-TV-only application — it targets Android generally — but this is the device
whose constraints the design is measured against.

## The device

| | |
|---|---|
| Model / build | `AFTKM` |
| OS | Fire OS 8, Android 11 compatibility level, API 30 |
| SoC | MediaTek MT8696D, quad-core Cortex-A55 up to ~1.7 GHz |
| RAM | 2 GB |
| Storage | 8 GB, considerably less free |
| Network | Wi-Fi 6 |
| Output | HDMI, 4K capable, hardware video decode |
| Input | Bluetooth remote and controllers |

Amazon's own references:
[identifying devices](https://developer.amazon.com/docs/device-specs/identify-fire-tv-devices.html),
[device specifications](https://developer.amazon.com/docs/device-specs/device-specifications-fire-tv-streaming-media-player.html),
[Fire OS 8](https://developer.amazon.com/docs/fire-tv/fire-os-8.html),
[Fire OS overview](https://developer.amazon.com/docs/fire-tv/fire-os-overview.html).

## What is different about Fire OS

- **No Google Play Services.** Anything that needs GMS does not run. Pharos
  never requires it; any Google-specific capability stays optional and isolated
  behind an interface.
- **The launcher is Amazon's.** Pharos does not replace it and does not claim
  to. Launching on boot is best-effort where the platform allows it, never a
  promise.
- **Leanback rules apply.** The app appears on the TV home screen through its
  banner, not its icon. Without `android:banner` it can be invisible there while
  installing perfectly.
- **The remote is the whole input surface.** No touch, no pointer. Anything
  reachable only by touch is unreachable.

## Enabling developer access

1. **Settings → My Fire TV → About**
2. Select the **device name** repeatedly — around seven times — until it
   confirms developer options are enabled
3. Back to **My Fire TV → Developer Options**
4. Turn on **ADB Debugging**

The stick's port supplies power only, so debugging happens over the network.
The full workflow — connecting, installing, driving the remote from a keyboard,
reading logs, and turning debugging off again afterwards — is in
[Debugging on a Fire TV](../developer/fire-tv-debugging.md).

**ADB over the network is a remote shell on that device.** Enable it for a
session on a network you control, and switch it off when you are done.

## Release acceptance checklist

A release is not made until these pass on physical `AFTKM` hardware. The list
mirrors `PLAN.md` §66.

- [ ] Installs via ADB
- [ ] Launches
- [ ] No Google Play dependency failure
- [ ] D-pad navigation works throughout
- [ ] Focus is always visible
- [ ] Back behaves predictably everywhere
- [ ] 1080p layout is correct
- [ ] 4K scaling is acceptable
- [ ] Recovers from Wi-Fi loss
- [ ] WebSocket reconnects
- [ ] MQTT works when enabled
- [ ] Media plays when enabled
- [ ] Extended idle does not break the app
- [ ] No obvious memory growth over a long run
- [ ] No secrets in logs
- [ ] Alert interruption works
- [ ] The previous screen is restored afterwards
- [ ] Every settings screen is usable with the remote
