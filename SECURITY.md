# Security Policy

## Reporting a vulnerability

Report privately through
[GitHub Security Advisories](https://github.com/nikolareljin/pharos/security/advisories/new).
Do not open a public issue for a vulnerability.

Include what you did, what happened, and what you expected — a reproduction is
worth more than a severity rating. Expect an acknowledgement within a week. If a
fix is warranted you will be credited in the advisory unless you ask otherwise.

Pharos has no release yet, so there is no supported-version table. Until 1.0,
the supported version is `main`.

## What Pharos assumes about its environment

Pharos runs on a screen on a network you do not fully control, driven by systems
it did not write. The threat model is in `PLAN.md` §36; the short form:

- **Every inbound message is untrusted.** Schema-validated, size-limited, and
  rate-limited before it reaches anything that changes what is on screen.
- **Commands are an allowlist, never an interpreter.** There is no shell, no
  eval, no downloaded-and-executed code, no unrestricted JavaScript bridge, and
  no arbitrary filesystem access. This is a design boundary, not a setting.
- **Control surfaces are closed by default.** Any local API is disabled until
  explicitly enabled, and requires authentication when it is.
- **Web content is untrusted relative to the app.** The optional WebView runs
  behind a URL allowlist with controlled navigation.
- **Privileged operations fail closed.** When authentication or validation is
  uncertain, the operation does not happen.

## What Pharos does with secrets

- Tokens, broker credentials and keys are held in Android Keystore-backed
  storage where the platform allows it.
- Configuration *references* a secret (`{"secretRef": "..."}`) rather than
  containing it, so a configuration file can be shared and an export can omit
  secrets by default.
- Logs and exported diagnostics are redacted: no auth headers, no tokens, no
  credential-bearing URLs, no camera credentials. If you find a secret in a log,
  that is a vulnerability — report it as one.

## What Pharos sends where

Nothing, by default. No cloud telemetry, no analytics, no crash reporting. The
node talks only to the sources you configure. Any AI feature is opt-in and
explicit; camera frames, dashboard payloads and credentials are never sent to a
remote service on the app's own initiative (`PLAN.md` §41, §53).

## Scope

In scope: the Pharos application, its protocol handling, its storage of secrets,
and its published examples.

Out of scope: vulnerabilities in Android or Fire OS themselves, physical attacks
on an unlocked device, and misconfiguration of a third-party system that Pharos
merely displays.
