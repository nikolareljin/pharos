# Pharos

<img src="assets/banner.png" alt="Pharos" width="320">

Pharos turns an Android device with a screen into a programmable display node:
dashboards, telemetry, alerts, media, camera views, signage, kiosks and
AI-assisted panels, driven by whatever system you already run.

The architectural rule that shapes everything else: **external systems integrate
with Pharos, not the reverse.** Pharos knows nothing about any particular
backend. It speaks a public, versioned, schema-validated JSON protocol over
HTTP, WebSocket and MQTT, and anything that can produce that JSON can drive it.

## Status

Bootstrapping. [`PLAN.md`](https://github.com/nikolareljin/pharos/blob/main/PLAN.md)
is the complete architecture and roadmap; the application is being built against
it. Pages below marked **Planned** describe designed behaviour that is not
implemented yet — they are written now because the protocol they describe is a
public contract, and a contract designed after the code is a contract shaped by
accidents.

## Start here

**Running it on a device**

- [user/install.md](user/install.md) — sideloading, including the Fire TV path
- [user/fire-tv.md](user/fire-tv.md) — the reference device, and what is
  different about Fire OS
- [user/onboarding.md](user/onboarding.md) — first launch: demo, standalone or
  managed
- [user/troubleshooting.md](user/troubleshooting.md) — when the screen is not
  showing what you expect

**Driving it from your own systems**

- [developer/protocol.md](developer/protocol.md) — the wire contract
- [developer/integrations.md](developer/integrations.md) — worked examples over
  REST, WebSocket and MQTT
- [developer/providers.md](developer/providers.md) — how Pharos connects out
- [developer/renderers.md](developer/renderers.md) — screens, layouts, widgets

**Working on Pharos**

- [developer/architecture.md](developer/architecture.md) — how the node is built
- [developer/development.md](developer/development.md) — toolchain and the
  `./dev` CLI
- [developer/testing.md](developer/testing.md) — what must be tested and how
- [developer/security.md](developer/security.md) — threat model and controls
- [operations/release.md](operations/release.md) — versioning and releases

## What Pharos is not

A network scanner, an observability backend, a home-automation server, an NVR,
or a monitoring database. Those systems stay where they are and talk to Pharos
over the protocol. Keeping that boundary is what lets a node stay small enough
to run well on a 2 GB streaming stick.
