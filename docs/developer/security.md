# Security model

For reporting a vulnerability, see [SECURITY.md](https://github.com/nikolareljin/pharos/blob/main/SECURITY.md). This page
is the design rationale.

## The situation Pharos is in

A Pharos node sits on a screen, on a network the operator does not fully
control, driven by systems it did not write, unattended, for months. It holds
credentials to the things it displays. Nobody is watching it.

That shapes the threats:

| Threat | Answer |
|---|---|
| Unauthorized display control | Authenticate every controller and provider; fail closed |
| A compromised device on the same LAN | No unauthenticated control surface, ever, by default |
| A malicious or broken publisher | Schema validation, size limits, per-source rate limiting, alert-storm collapsing |
| Token theft from the device | Keystore-backed storage; exports omit secrets |
| Malformed or oversized JSON | Size limit *before* parse; validation before routing |
| Replayed commands | Unique command ids, replay window, idempotency |
| Hostile web content | URL allowlist, controlled navigation, no unnecessary bridges |
| Media parsing edge cases | Platform decoders only; failures degrade to an error state |
| Credential leakage through logs | Redaction as a logging-layer rule, not a per-call discipline |

## Controls

1. Authenticate controllers and providers.
2. TLS wherever it is feasible.
3. Schema-validate every message.
4. Enforce payload size limits — before parsing, not after.
5. Commands come from an allowlist.
6. Redact logs at the logging layer.
7. Store secrets in Keystore-backed storage.
8. Never execute anything arbitrary.
9. Rate-limit per source.
10. Protect configuration changes.
11. Anti-replay for privileged actions.
12. Fail closed. When authentication or validation is uncertain, nothing
    happens.

## Why there is no execution surface

The most useful feature request Pharos will keep refusing is "let the controller
run a command on the device". It would make one integration much easier and
would make every Pharos node a remote shell for whoever gets hold of a
publisher's credentials.

The line is fixed: no shell, no `eval`, no downloading and running code, no
unrestricted filesystem access, no unrestricted JavaScript bridge. Commands are
a finite list of application actions, each one implemented in this repository
and reviewable. Anything requiring general-purpose execution belongs in the
system driving Pharos, on hardware where that capability is expected.

## Secrets

Secrets are API tokens, MQTT credentials, WebSocket tokens, basic-auth
credentials and client private keys.

They are held in Android Keystore-backed storage where the platform allows it,
and configuration **references** them rather than containing them:

```json
{ "secretRef": "mqtt-primary" }
```

That indirection is what makes configuration shareable. An exported
configuration carries providers, dashboards, mappings, themes and preferences —
and no secrets, by default. A config file that can be pasted into an issue
without thinking is a config file people will actually share when asking for
help.

## Identity

Node identity is a locally generated UUID plus a keypair; only the public half
ever leaves the device. Nothing is derived from a MAC address, serial number or
account id.

A node id arriving in a payload is addressing information, not proof of origin.
A controller attaches the authenticated node id from the session. Trusting an
identity because the sender wrote it down is the mistake this design exists to
avoid.

Pairing tokens are random, single-use and short-lived (about two minutes). A
pairing token that outlives the pairing is a credential lying around.

## Logging

Structured, levelled, bounded and rotating. Production rules: no secrets, no
auth headers, redacted URLs and query strings, payload fields marked secret
redacted, and no unbounded growth.

Diagnostics exports follow the same rules — they are written to be pasted into
public issues. **A secret appearing in a log or an export is a vulnerability**,
not a papercut, and should be reported as one.

## Privacy

No cloud telemetry. No analytics. No crash reporting. The node talks only to the
sources you configure.

AI features are opt-in and explicit. Camera frames, dashboard payloads,
credentials and personal data are never sent to a remote service on the app's
own initiative — a display node that quietly uploads what is on screen is a
surveillance device with a dashboard on it.

## Configuration import

Import validates the schema and version, shows what will change, rejects
incompatible content, and requires confirmation before a destructive overwrite.
Restoring a node identity is deliberately a separate, explicit action, because
two nodes sharing an identity produces a failure that looks like a network
problem for a very long time.
