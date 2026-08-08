# Pharos protocol v1

> **Status: designed, not yet implemented.** The schemas under `schemas/v1/`
> and the contract tests that validate these examples arrive with the protocol
> implementation. This document is written first on purpose: the protocol is a
> public product surface, and a contract designed after the code is a contract
> shaped by whatever the code happened to do.

The protocol is versioned, JSON-based, transport-independent, schema-validated,
backward-compatible within a major version, and safe by default. It is the same
over HTTP, WebSocket and MQTT — the transport moves bytes and nothing else.

## Envelope

Every message, on every transport, is an envelope:

```json
{
  "protocolVersion": "1.0",
  "id": "6f1b0f3e-1a2b-4c3d-8e4f-5a6b7c8d9e0f",
  "timestamp": "2026-08-07T22:00:00Z",
  "type": "alert",
  "source": "example-controller",
  "target": {
    "nodeIds": [],
    "groups": []
  },
  "payload": {}
}
```

| Field | Required | Notes |
|---|---|---|
| `protocolVersion` | yes | Major version must match; minor differences are tolerated |
| `id` | yes | Unique per message. Used for deduplication and idempotency |
| `timestamp` | yes | ISO-8601 with an explicit offset. UTC recommended |
| `type` | yes | One of the message families below |
| `source` | no | A stable identifier for the publisher; used for per-source rate limiting |
| `target` | no | Absent or empty means every node that receives it |
| `payload` | yes | Family-specific, schema-validated per family |

Addressing: a message applies if `target` is absent or empty, or if this node's
id is in `nodeIds`, or if any of this node's groups is in `groups`. Nodes ignore
messages addressed elsewhere without complaint — on a shared broker that is the
normal case, not an error.

## Message families

| Family | Direction | Purpose |
|---|---|---|
| `content` | in | Data to display, without saying how |
| `screen` | in | A declarative screen definition |
| `metric` | in | A single named measurement |
| `alert` | in | Something that may interrupt what is on screen |
| `event` | in | Something happened; no display obligation |
| `command` | in | A request for one predefined application action |
| `configuration` | in | Settings and provider definitions |
| `status` | out | What this node is doing right now |
| `heartbeat` | out | Liveness, on an interval |
| `acknowledgement` | out | The outcome of a command or alert |
| `capabilities` | out | What this node can actually do |

## Identity

A node generates a random UUID on first launch and persists it. It is never
derived from a MAC address, serial number or account id — those identify
hardware and people, and a display node needs neither.

Alongside the UUID the node generates a **keypair**, and only the public half
ever leaves the device. Identity is therefore derived from a key rather than
claimed in a field, which has one consequence worth stating plainly:

> **A node id on the wire is not an identity.** A controller attaches the node
> id from the authenticated session. A node id carried in an inbound payload is
> addressing information, never proof of who sent it.

```json
{
  "nodeId": "f72a6ef4-2a42-42a9-a39c-9e2dc4f87833",
  "name": "Living Room TV",
  "groups": ["home", "displays"],
  "tags": ["tv", "4k"]
}
```

## Capabilities

A controller should not assume every Android device is the same one. Nodes
publish what they have:

```json
{
  "protocolVersion": "1.0",
  "nodeId": "f72a6ef4-2a42-42a9-a39c-9e2dc4f87833",
  "capabilities": {
    "display": true,
    "touch": false,
    "dpad": true,
    "audio": true,
    "video": true,
    "web": true,
    "camera": false,
    "microphone": false,
    "mqtt": true,
    "websocket": true
  }
}
```

After pairing, a controller also enumerates the capabilities it has **granted**,
so a node's UI can offer only what it is actually permitted to invoke rather
than discovering the refusal at the moment a user presses the button.

## Pairing

> **Status: planned** (`PLAN.md` phase 11). Specified here because the fields it
> needs belong in v1, and adding them later would be a breaking change.

1. Install Pharos; it generates its UUID and keypair.
2. The node displays a QR code and a short code, both carrying the same payload:
   controller address, port, and the controller's certificate fingerprint.
3. The pairing token is **random, single-use, and expires in about two minutes**.
4. The node sends only its public key.
5. The controller establishes the association and issues a durable credential.
6. The one-time token is invalidated.

A cable path (ADB) and a typed short code produce an identical record, so a
device with no camera and no keyboard is not a special case.

## Commands

Commands are an allowlist. Anything not on it is rejected — never interpreted,
never guessed at.

`show_screen` · `next_screen` · `previous_screen` · `refresh` ·
`reload_provider` · `play` · `pause` · `stop` · `dismiss` · `acknowledge` ·
`open_settings` · `request_status` · `set_volume`\* · `set_brightness`\*

\* platform-gated: available only where the platform permits it, and reported in
capabilities.

Never on this list, at any version: shell execution, `eval`, downloading and
running an executable, unrestricted filesystem access, or an unrestricted
JavaScript bridge.

Each command carries the envelope `id`. A node ignores an id it has already
executed inside the replay window, which makes a retried command safe to send
and a replayed one useless to an attacker.

## Errors

Rejections use RFC 7807 `application/problem+json` with a stable, greppable
machine code. The `code` never changes meaning; `title` and `detail` are for
humans and may be reworded.

```json
{
  "type": "https://github.com/nikolareljin/pharos/docs/errors#invalid-message",
  "title": "Message failed schema validation",
  "status": 400,
  "code": "INVALID_MESSAGE",
  "detail": "payload.widgets[2].type is required",
  "instance": "6f1b0f3e-1a2b-4c3d-8e4f-5a6b7c8d9e0f"
}
```

## HTTP conventions

Where Pharos exposes or consumes HTTP, the version lives in the path
(`/api/v1/...`), never in a header, and health endpoints sit outside the version
prefix (`/health`, `/health/ready`) because a readiness probe should not need to
know the API version to ask whether the thing is up.

## Versioning and compatibility

- Within `1.x`: additive only. Unknown optional fields are ignored by readers.
  Existing fields never change meaning.
- A new **required** field, or any change of meaning, is `2.0`.
- Unsupported required behaviour is rejected explicitly, with a code — never
  silently half-applied.
- Schemas for supported versions are retained, and migrations are documented.
- The protocol version is independent of the app version. Both appear in
  `status` and `heartbeat`.

## Schemas

Schemas live under `schemas/v1/`, one file per message family, and are the
normative form of everything above. The layout:

```
schemas/v1/<name>@1.schema.json          $id, title, and a required
                                          {"const": "<name>@1"} self-identifying
                                          field; additionalProperties: false
schemas/v1/testdata/<name>/valid-*.json
schemas/v1/testdata/<name>/invalid-*.json
```

Both directions are required. A schema with no `invalid-*` vectors is untested —
it has never been shown to reject anything — and the validator fails the build
when either set is empty. Examples under `examples/` double as valid vectors, so
a published example that stops validating breaks CI rather than misleading
someone six months from now.
