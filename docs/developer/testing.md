# Testing

Pharos runs unattended for weeks on hardware nobody is looking at. The
interesting tests are therefore not the ones proving it works — they are the
ones proving it survives.

## Unit

Pure logic, no device, fast enough to run on every change:

- protocol parsing and validation, including every rejection path
- priority ordering, including ties at equal priority
- alert deduplication
- provider state machines
- retry and backoff sequences, including the jitter bounds and the reset
- screen selection
- command authorization — especially what is refused
- configuration migration

## Integration

- HTTP provider against `MockWebServer`: success, `304`, `401`, `429`, `500`,
  timeout, malformed body
- WebSocket reconnect: drop mid-stream, reconnect, verify no duplicate delivery
- MQTT connect and reconnect, retained-message recovery, last-will
- persistence across process death
- media lifecycle: acquire, release, and release again on an error path
- cache bounds actually bound

## UI

- D-pad traversal reaches every focusable element on every screen
- focus is visible at all times, and restored on return
- dashboard rendering at 1080p and 4K
- alert interruption **and restoration of the previous screen's state** —
  scroll position, playlist index, media position
- settings are fully operable with a remote
- connection and error states render as human sentences, never raw transport
  errors

## Resilience

Simulate, and assert the app neither crashes nor blanks:

Wi-Fi loss · DNS failure · server restart · broker restart · malformed JSON ·
duplicate messages · oversized payloads · alert floods · invalid credentials ·
expired credentials · HTTP 401/403/429/500 · slow endpoints · media timeout ·
process death · low storage

The passing condition is not "no crash". It is: the last valid content stays on
screen, a quiet status hint appears, retries back off with jitter, and recovery
is automatic when the cause goes away.

## Security

- an unauthorized connection is refused
- tokens are absent from logs and from diagnostic exports
- a replayed command is rejected inside the replay window
- the WebView cannot navigate outside its allowlist
- dangerous URI schemes are refused
- the command allowlist rejects everything not on it
- an exported configuration contains no secrets

These are regression tests for decisions, not features. They exist so that a
future change that quietly relaxes one of them fails loudly.

## On device

Every meaningful release is verified on physical `AFTKM` hardware against the
checklist in [../user/fire-tv.md](../user/fire-tv.md#release-acceptance-checklist).
An emulator is not a substitute for a claim about performance, memory or cold
start.

Soak testing (`PLAN.md` phase 13): 24 hours, 72 hours, one week. Track RAM, CPU,
Wi-Fi reconnects, media resource handles, process restarts, screen state and
content recovery. Most of what breaks an unattended display only shows up on day
three.

## Running them

```sh
./dev test                 # unit tests
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest   # instrumented, needs a device
./dev preflight            # everything CI runs, plus the boundary scan
```

## What "tested" means in a pull request

Paste the output, not the intention. If something could not be verified — no
Fire TV to hand, an emulator instead of hardware — say so in the pull request. A
gap named is a known limitation; a gap left out is a bug someone else finds
later, without the context you had.
