---
name: Bug report
about: Pharos did something wrong on a device
title: ""
labels: ["type:bug"]
assignees: []
---

## Area

- [ ] Display / dashboard rendering
- [ ] Remote / D-pad navigation or focus
- [ ] Provider (HTTP / WebSocket / MQTT)
- [ ] Alerts and priority interruption
- [ ] Media or camera
- [ ] Settings / onboarding
- [ ] Build, CI or tooling

## What happened

What you saw, and what you expected instead.

## Steps to reproduce

1.
2.
3.

## Environment

- Pharos version (Diagnostics screen, or the release tag):
- Protocol version, if a message is involved:
- Device model and Android / Fire OS version:
- Display resolution:
- Operating mode: standalone / managed / demo

## Logs

`adb logcat -d | grep -i pharos`, or the sanitized export from the Diagnostics
screen. **Redact tokens and credentials before pasting** — and if you find one
in the log itself, report that privately as a security issue instead.

```
<logs here>
```
