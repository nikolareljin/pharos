# Pharos

<img src="assets/banner.png" alt="Pharos" width="360">

Pharos turns an Android device with a screen into a programmable display node:
dashboards, telemetry, alerts, media, camera views, signage, kiosks and
AI-assisted panels, driven by whatever system you already run.

A spare Fire TV Stick, an old tablet, a phone with a cracked digitiser — each is
already a screen, a network card and a CPU. What they lack is something to point
at them.

!!! warning "Status: bootstrapping"

    The application launches, renders and is navigable with a remote. The
    providers, the protocol implementation and the dashboard runtime are being
    built against
    [`PLAN.md`](https://github.com/nikolareljin/pharos/blob/main/PLAN.md).
    **There is no installable release yet.** Pages marked *Status: planned*
    describe designed behaviour that is not implemented — they are written now
    because the protocol they describe is a public contract, and a contract
    written after the code is a contract shaped by accidents.

## The one rule everything follows from

**External systems integrate with Pharos, not the reverse.**

Pharos contains no knowledge of any particular backend, and never will. It
speaks a public, versioned, schema-validated JSON protocol over HTTP, WebSocket
and MQTT. Anything that can produce that JSON can drive it — which means adding
an integration never requires changing Pharos.

It is deliberately **not** a network scanner, an observability backend, a
home-automation server, an NVR or a monitoring database. Those stay where they
are. Keeping that boundary is what lets a node stay small enough to run well on
a 2 GB streaming stick.

## Installing

Requires Android 8.0 (API 26) or newer. **No Google Play Services**, ever — the
reference device does not have them.

```sh
# Verify what you downloaded before you install it
sha256sum -c pharos-<version>.apk.sha256

adb install -r --user 0 pharos-<version>.apk
```

`--user 0` matters: without it an install can land in a work profile, report
`Success`, and leave the app missing from the launcher. Full instructions,
including the Fire TV path over the network, are in
[Install](user/install.md) and [Fire TV](user/fire-tv.md).

Until there is a release, build from source — see
[Development](developer/development.md):

```sh
git clone --recurse-submodules https://github.com/nikolareljin/pharos.git
cd pharos
./dev build android
./dev deploy --device <serial>
```

## Using it with your own systems

Pick the transport that matches what you already have. You can use all three at
once, and none of them requires a Pharos server to exist anywhere.

| What you already run | Use | What you write |
|---|---|---|
| An HTTP endpoint returning JSON | **HTTP provider** — Pharos polls it | Usually nothing; point Pharos at the URL |
| Something that pushes events | **WebSocket** | A small publisher that sends envelopes |
| An MQTT broker | **MQTT** | Publish to a node, a group, or everything |
| Nothing yet | **Demo mode** | Nothing — it works offline |

Every message is the same envelope regardless of transport:

```json
{
  "protocolVersion": "1.0",
  "id": "6f1b0f3e-1a2b-4c3d-8e4f-5a6b7c8d9e0f",
  "timestamp": "2026-08-07T22:00:00Z",
  "type": "metric",
  "source": "example-publisher",
  "target": { "nodeIds": [], "groups": ["kitchen"] },
  "payload": {
    "id": "net.latency",
    "label": "Internet",
    "value": 18,
    "unit": "ms"
  }
}
```

Note what the payload does *not* contain: a colour, a position, a font size or a
threshold for "bad". The sender says what the number is; the node decides how it
looks on a television across a room. That split is what lets one publisher drive
a phone, a tablet and a 4K screen without knowing which is which.

[Feeding Pharos](developer/integrations.md) has worked examples in curl, Python
and MQTT, including how to map an existing event stream onto the priority bands
so the right things interrupt and the rest do not.

## Design commitments

Constraints, not aspirations — each one is testable and each has a section in
`PLAN.md`.

| | |
|---|---|
| **Works offline** | Demo mode needs no server. A network outage never blanks the screen — the last valid content stays up with a quiet status hint |
| **Remote-first** | D-pad navigation is a release-quality requirement, not a port. Focus is always visible and always restorable |
| **No Google services** | Nothing requires GMS, because the reference device does not have it |
| **Nothing arbitrary executes** | Commands come from a fixed allowlist. No shell, no eval, no downloaded code, no unrestricted JavaScript bridge |
| **Bounded everything** | Caches, queues, logs and alert storms all have ceilings. 2 GB of RAM is the design target, not the fallback |
| **Secrets stay out of sight** | Keystore-backed storage, redacted logs, and configuration that references secrets instead of containing them |

## Where to go next

<div class="grid cards" markdown>

- **Run it on a device** — [Install](user/install.md) ·
  [Fire TV](user/fire-tv.md) · [First launch](user/onboarding.md) ·
  [Troubleshooting](user/troubleshooting.md)

- **Drive it from your systems** — [Feeding Pharos](developer/integrations.md) ·
  [Protocol v1](developer/protocol.md) · [Providers](developer/providers.md)

- **Understand it** — [Architecture](developer/architecture.md) ·
  [Screens and widgets](developer/renderers.md) ·
  [Security model](developer/security.md)

- **Work on it** — [Development](developer/development.md) ·
  [Testing](developer/testing.md) · [Releasing](operations/release.md)

</div>

## Licence

MIT. Issues and pull requests are welcome — see
[CONTRIBUTING.md](https://github.com/nikolareljin/pharos/blob/main/CONTRIBUTING.md).
Security reports go through
[a private advisory](https://github.com/nikolareljin/pharos/security/advisories/new),
never a public issue.
