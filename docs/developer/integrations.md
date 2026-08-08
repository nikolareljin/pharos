# Integrating your systems with Pharos

> **Status: the protocol is designed, the providers are being built.** The
> payloads below are the contract; `schemas/v1/` will make them enforceable and
> `examples/` will carry runnable versions of each snippet.

Pharos has no adapter for your product and never will. It consumes a public JSON
protocol, and the integration work happens on your side, where the data already
lives. That is the whole point: a new integration should not require a change to
Pharos.

All addresses below are from the RFC 5737 documentation ranges
(`192.0.2.0/24`, `198.51.100.0/24`, `203.0.113.0/24`). All credentials are fake.

## Which transport

| Your system | Use |
|---|---|
| Already serves JSON over HTTP | **HTTP provider** — Pharos polls it. Nothing to build |
| Pushes events as they happen | **WebSocket** — lowest latency, one connection per node |
| Already has a broker | **MQTT** — one publisher reaches many nodes, and late joiners recover from retained topics |

You can use all three at once. A node polls metrics over HTTP, holds a socket to
a controller, and subscribes to an alert topic, without any of them knowing
about the others.

## 1. REST: a dashboard from an endpoint you already have

Point the HTTP provider at any endpoint returning JSON on an interval. The
simplest useful shape is a metric list:

```json
{
  "protocolVersion": "1.0",
  "id": "0c2e6b3a-6a3f-4a1e-8f11-2c0d9b4a7e51",
  "timestamp": "2026-08-07T22:00:00Z",
  "type": "screen",
  "payload": {
    "screenId": "overview",
    "title": "House",
    "layout": "grid",
    "widgets": [
      {"type": "metric", "id": "net.latency", "label": "Internet", "value": 18, "unit": "ms"},
      {"type": "metric", "id": "env.temp",    "label": "Study",    "value": 21.4, "unit": "°C"},
      {"type": "status", "id": "svc.backup",  "label": "Backups",  "value": "ok"},
      {"type": "clock",  "id": "clock"}
    ]
  }
}
```

Fetching it:

```sh
curl -sS \
  -H "Authorization: Bearer $PHAROS_DEMO_TOKEN" \
  -H "Accept: application/json" \
  https://198.51.100.10/api/v1/screens/overview
```

Notes that save time later:

- Support `ETag` / `If-None-Match` if you can. Pharos treats a `304` as a
  successful poll, which keeps a node quiet on a screen that rarely changes.
- Keep the response small. This is parsed on a 1.7 GHz A55 every interval.
- Put the token in a header, never a query string. Query strings end up in logs
  on machines you do not administer.

## 2. WebSocket: push a metric the moment it changes

```python
# pip install websockets
import asyncio, json, uuid, datetime
import websockets

URL = "wss://198.51.100.10/pharos/v1/stream"
TOKEN = "example-token-not-a-real-one"

def envelope(kind, payload, node=None):
    return {
        "protocolVersion": "1.0",
        "id": str(uuid.uuid4()),
        "timestamp": datetime.datetime.now(datetime.timezone.utc)
                       .isoformat(timespec="seconds").replace("+00:00", "Z"),
        "type": kind,
        "source": "example-publisher",
        "target": {"nodeIds": [node] if node else [], "groups": []},
        "payload": payload,
    }

async def main():
    async with websockets.connect(URL, additional_headers={"Authorization": f"Bearer {TOKEN}"}) as ws:
        await ws.send(json.dumps(envelope("metric", {
            "id": "net.latency", "label": "Internet", "value": 18, "unit": "ms",
        })))

asyncio.run(main())
```

Every message carries a unique `id`. Resending after a timeout is safe — the
node deduplicates — which means your publisher can retry without reasoning about
whether the first attempt landed.

## 3. MQTT: one publisher, many screens

Publish to the node, the group, or everything:

```sh
mosquitto_pub -h 198.51.100.20 -p 8883 --capath /etc/ssl/certs \
  -u publisher -P "example-password" \
  -t 'pharos/groups/kitchen/content' \
  -m '{"protocolVersion":"1.0","id":"9f1c...","timestamp":"2026-08-07T22:00:00Z","type":"content","payload":{...}}'
```

Retain `content` and `config` so a node that reboots recovers immediately.
**Never retain `commands`** — a retained command re-executes on every reconnect,
which turns one "pause the video" into a device that will not play anything
again until someone finds the retained message.

### Mapping an existing event stream into an alert

Most systems already emit something event-shaped. Rather than making them speak
Pharos natively, translate at the edge. A common shape — a severity, a numeric
impact score, a title and a summary — maps onto the priority bands directly:

```python
BANDS = {"low": 25, "medium": 45, "high": 65, "critical": 90}

def to_pharos_alert(event):
    """Translate a generic event into a Pharos alert.

    severity sets the band; impact_score nudges within it. The dedupe key is the
    event's stable identity, not its text — a re-published event with a reworded
    title must collapse onto the original rather than stacking a second card on
    a screen nobody is standing in front of.
    """
    base = BANDS.get(event["severity"], 25)
    priority = min(100, base + int(event.get("impact_score", 0)))
    return envelope("alert", {
        "alertId":   event["event_id"],
        "dedupeKey": f"{event['hazard_type']}:{event.get('location', {}).get('name', 'global')}",
        "priority":  priority,
        "title":     event["title"],
        "body":      event.get("summary", ""),
        "ttlSeconds": 900,
        "sticky":    priority >= 95,
        "acknowledgeable": priority >= 60,
    })
```

Three things to get right, in order of how much trouble they cause:

1. **`dedupeKey` is identity, not text.** Anything that changes between
   republications of the same condition — a timestamp, a reworded title, a
   sequence number — creates an alert storm made entirely of one event.
2. **`ttlSeconds` is a promise to clear.** An alert with no TTL and no
   acknowledgement path stays on the screen after the condition is gone, and the
   next real alert arrives on a display people have learned to ignore.
3. **Priority is a claim on someone's attention.** Everything above 80 takes
   over a screen that was showing something else. If everything is urgent,
   interruption stops meaning anything.

## Fleet management

Pharos does not need a fleet server, and does not ship one. The protocol is
shaped so an independent controller can list and register nodes, observe
online/offline state, assign groups, push configuration, request status, update
playlists, send alerts and query capabilities — using nothing but the public
contract.

## Building it right

- **Validate against the schema in your own CI**, not against a node. A node
  rejecting your message at 3 a.m. is a worse test than a red build.
- **Send `source`.** Per-source rate limiting and storm collapsing need it, and
  so does anyone reading Diagnostics trying to work out who is shouting.
- **Do not send secrets in payloads.** Nodes log message metadata, and a node is
  a device on a shelf that people can pick up.
- **Treat unknown fields as ignorable**, in both directions. That is what makes
  a `1.x` upgrade a non-event.
