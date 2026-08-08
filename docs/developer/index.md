# Developer documentation

Two audiences share this directory: people integrating their systems with
Pharos, and people working on Pharos itself.

**Integrating**

- [protocol.md](protocol.md) — the wire contract: envelope, message families,
  addressing, identity, pairing, compatibility rules
- [integrations.md](integrations.md) — worked examples over REST, WebSocket and
  MQTT
- [providers.md](providers.md) — how Pharos connects out, and how each transport
  behaves when it fails
- [renderers.md](renderers.md) — screens, layouts and the widget catalog

**Building**

- [architecture.md](architecture.md) — the shape of the node
- [development.md](development.md) — toolchain, `./dev`, preflight
- [testing.md](testing.md) — what must be tested
- [security.md](security.md) — threat model and the controls that answer it
- [../operations/release.md](../operations/release.md) — versioning and releases

## The three rules everything else follows from

1. **External systems integrate with Pharos.** No part of this codebase knows
   about a specific backend product. If a feature requires that knowledge, it
   belongs on the other side of the protocol.
2. **Every inbound message is untrusted** until it has been size-limited,
   schema-validated and rate-limited. Transports never touch UI state directly.
3. **The command surface is an allowlist, never an interpreter.** No shell, no
   eval, no downloaded code, no unrestricted JavaScript bridge. This is a design
   boundary, not a configuration option.

## Versioning

The app version and the protocol version move independently:

```
App:      0.1.0
Protocol: 1.0
```

An app release does not imply a protocol change, and a `1.x` protocol change is
always backward-compatible within the major version. Both are reported in status
and heartbeat messages so a controller can see exactly what it is talking to.
