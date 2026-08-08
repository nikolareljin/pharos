# Architecture

Pharos is a **node**: a stable identity, a set of capabilities, some provider
connections, a state store, a screen runtime and a safe command surface.

## The path a message takes

```text
External systems
      │
 REST │ WebSocket │ MQTT
      │
Provider layer            transport, retry, backoff, connection health
      │
Protocol validation       size limit, schema, version, addressing
      │
Message router
      ├──────────────┬──────────────┬──────────────┐
      │              │              │              │
Configuration    Screen store   Alert engine   Command dispatcher
      └──────────────┴──────────────┴──────────────┘
                     │
              Application state
                     │
              Screen runtime  →  Renderer registry  →  Compose UI
```

Two rules keep this honest:

**Transports never touch the UI.** A provider's only outputs are a state flow
and a message flow. It cannot show anything, hide anything or navigate. Every
visible change goes through application state, which means the same code path
runs whether a message arrived over MQTT, over a socket, or from the demo
provider — and it means the UI can be tested without a network.

**Nothing enters the router unvalidated.** Size limit first, then schema, then
version and addressing. A malformed payload is rejected and counted before it
can reach anything with state.

## The pieces

| Layer | Responsibility |
|---|---|
| **Provider** | Owns one connection. Emits `StateFlow<ProviderState>` and `Flow<PharosEnvelope>`. Handles retry, backoff, jitter and its own bounded inbound queue |
| **Validator** | Size limits, JSON Schema, protocol version, target matching. The boundary between untrusted and trusted |
| **Router** | Dispatches by message family to configuration, screens, alerts or commands. Deduplicates by message id |
| **Screen store** | The declarative screens available now: local, provider-owned, temporary, scheduled |
| **Alert engine** | Priority, TTL, dedup keys, acknowledgement, storm protection, and the record of what to restore afterwards |
| **Command dispatcher** | Allowlist lookup, authorization, idempotency by command id. Unknown or unauthorized commands are rejected, never guessed at |
| **Screen runtime** | Decides what is on screen right now: the default, the playlist position, or an interrupting alert |
| **Renderer registry** | Maps a widget type to a Composable. An unregistered type renders a placeholder |

## Operating modes

**Managed** — a controller decides. **Standalone** — Pharos fetches and
subscribes on its own, with no Pharos server anywhere. **Hybrid** — local
dashboards run normally, and a controller can interrupt them with something
urgent. Hybrid is the shape most installations want, and it falls out of the
architecture rather than being a fourth code path: an alert from any source is
just a higher-priority claim on the screen runtime.

## Interruption and restoration

The priority engine is the defining behaviour:

1. Record the current screen and its state.
2. A qualifying alert interrupts.
3. Show the alert.
4. It expires, is dismissed, or is acknowledged.
5. Restore what was there before, including its state.

Priority bands: `0–19` passive, `20–39` normal, `40–59` warning, `60–79`
important, `80–94` urgent, `95–100` critical. Interruption thresholds are
configurable; the bands are stable so a publisher can reason about them without
knowing the display's configuration.

Step 5 is the part that is easy to get wrong. "Restore" means the previous
screen *and* its scroll position, playlist index and media position — otherwise
every alert silently resets the dashboard.

## Concurrency

- No networking on the main thread, ever.
- No `GlobalScope`. Every coroutine has an owner that can cancel it.
- Provider retries are cancellable, and cancellation propagates.
- Flow collection is lifecycle-aware; a backgrounded app stops collecting.
- Renderers stay fast. Work that is not laying out pixels does not belong in a
  Composable.

## Storage

DataStore holds preferences and small structured state: node id, node name,
provider definitions, the selected screen, dashboard configuration, diagnostic
counters, and the last valid screen where caching it is worthwhile. Room appears
only where structured persistence genuinely justifies it.

What is never persisted: unbounded telemetry, raw video, unbounded logs, or
plaintext secrets in an exportable configuration.

## Errors

Typed, not stringly. `AUTHENTICATION_FAILED`, `CONNECTION_FAILED`, `TIMEOUT`,
`INVALID_MESSAGE`, `UNSUPPORTED_PROTOCOL`, `UNSUPPORTED_WIDGET`, `MEDIA_FAILED`,
`STORAGE_LOW`, `PERMISSION_DENIED`, `RATE_LIMITED`.

Raw exceptions never reach normal UI. `Error 1006` tells a viewer nothing;
"Controller disconnected — retrying automatically — last connected 18:42" tells
them everything they can act on. The technical detail lives in Diagnostics.

## Extension points

Source-level interfaces, extended by editing this repository: `Provider`,
`WidgetRenderer`, `CommandHandler`, `AiProvider`, `MediaSourceAdapter`,
`AuthenticationProvider`.

Dynamic third-party APK plugin loading is deliberately not implemented. It would
mean running someone else's code inside a process that holds your credentials
and drives your screens, and the compatibility cost across Fire OS versions is
its own project.

## Module layout

Modules are split when they have code, not to look organised. Today: `:app`.
`:core:model`, `:core:runtime` and `:core:protocol` split out as the runtime and
protocol land, with `data/` and `feature/` following the tree sketched in
`PLAN.md` §10.
