# Providers

A provider owns one connection to one source. Its entire public surface is two
flows and a lifecycle:

```kotlin
interface Provider {
    val id: String
    val state: StateFlow<ProviderState>
    val messages: Flow<PharosEnvelope>
    suspend fun start()
    suspend fun stop()
}
```

It cannot show anything, hide anything or navigate. That restriction is what
makes the UI testable without a network and makes every transport behave
identically once a message is inside.

## Provider state

```text
DISABLED → CONNECTING → CONNECTED
                ↓            ↓
             FAILED ← RECONNECTING ← DEGRADED
```

`DEGRADED` is connected but unwell — slow responses, partial failures, a growing
queue. It exists so a display can show "something is wrong" without pretending
the connection dropped.

## Retry

Exponential backoff with jitter, cancellable, reset after a stable connection:

```text
1s, 2s, 4s, 8s, 15s, 30s, 60s (capped)
```

The jitter is not decoration. A fleet of nodes that all lose the same access
point will otherwise reconnect in lockstep and knock over the server they are
reconnecting to.

Not everything is retryable. A 401 with a bad credential will still be a 401 in
sixty seconds; that goes to `FAILED` with `AUTHENTICATION_FAILED`, and waits for
a human.

## HTTP provider

For polling systems that already speak REST.

Supports: `GET` JSON, configurable interval, custom headers, bearer-token auth,
optional basic auth, query parameters, timeouts, exponential backoff with
jitter, ETag and conditional requests, TLS, a cache policy, and an explicit
error state.

Rules:

- **Never log tokens or sensitive headers**, and redact credential-bearing query
  parameters in any error message.
- Configuration **references** a secret rather than containing it:
  `{"secretRef": "metrics-api"}`.
- A conditional request that returns `304` is a success, not a no-op — it resets
  the backoff and refreshes the last-seen timestamp.

## WebSocket provider

The preferred transport for managed, push-driven installations.

Supports: authenticated connection, reconnect with exponential backoff and
jitter, ping/pong liveness, a **bounded** inbound queue, validation before
routing, duplicate handling by envelope id, and provider health reporting.

The queue bound matters more than it looks. An unbounded queue turns a burst
from a misbehaving publisher into an out-of-memory kill on a 2 GB device.
Bounded means the oldest low-priority messages are dropped and the drop is
counted in Diagnostics — visible loss beats invisible death.

## MQTT provider

> **Status: planned** (`PLAN.md` phase 7). Implementation waits on validating a
> maintained Android client against API 30 on real Fire OS hardware — the
> commitment to a client library is harder to reverse than the provider around
> it.

Target: MQTT 3.1.1 (MQTT 5 if the chosen client is solid on Fire OS), TCP and
TLS, username/password or token credentials, QoS 0 and 1, retained messages
where the semantics are safe, reconnect, and a last-will status message.

Topic convention — `<plane>/<id>/<channel>`, with the device→hub plane kept
separate from the hub→display plane so that content and status do not share a
prefix:

```text
pharos/nodes/{nodeId}/content
pharos/nodes/{nodeId}/events
pharos/nodes/{nodeId}/commands
pharos/nodes/{nodeId}/config
pharos/nodes/{nodeId}/status
pharos/nodes/{nodeId}/heartbeat
pharos/groups/{groupId}/content
pharos/groups/{groupId}/events
pharos/groups/{groupId}/commands
pharos/broadcast/content
pharos/broadcast/events
```

Retain and QoS are properties of the topic class, stated in the spec rather than
left to each publisher's habits:

| Topic class | QoS | Retained | Why |
|---|---|---|---|
| `content`, `config` | 1 | yes | A node joining late must recover its current state without waiting for the next publish |
| `commands` | 1 | **no** | A retained command re-executes on every reconnect. That is a bug generator, not a feature |
| `events` | 1 | no | Events are moments; a stale one is misinformation |
| `status`, `heartbeat` | 0 | yes (status) | Cheap and frequent; the last known status is worth keeping, the last heartbeat is not |

Privileged broadcast commands are not enabled by default. "Every screen in the
building, do this" is exactly the capability you do not want reachable from a
compromised sensor.

## Local provider and demo mode

Pharos has to be useful with no infrastructure at all: a clock, a static
message, example metrics, local device status, an image slideshow, a demo
dashboard.

Demo mode works fully offline and demonstrates screen rotation plus an alert
that interrupts and restores. It exists for adoption — someone should be able to
see what this does before wiring anything up — and it doubles as the known-good
comparison when an integration misbehaves.

## Optional local control API

> **Status: not planned for MVP** (`PLAN.md` §27).

If it ships:

```text
POST /api/v1/content
POST /api/v1/events
POST /api/v1/commands
GET  /api/v1/status
GET  /api/v1/capabilities
```

Disabled by default; explicit enable; authentication required; the bind scope
shown plainly in the UI; rate limits; payload size limits; no arbitrary
execution; no secrets in responses; and TLS or documented trusted-LAN
deployment. An unauthenticated control endpoint on a LAN is a remote control for
whoever else is on that LAN.
