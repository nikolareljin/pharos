<!--
Title: Conventional Commits, e.g. "feat(protocol): ..." / "fix(focus): ..." / "docs: ..."
-->

## Summary

What this changes and why.

## Area

- [ ] App / UI
- [ ] Core runtime (screens, widgets, renderers)
- [ ] Protocol or schemas (`schemas/`, `docs/developer/protocol.md`)
- [ ] Provider (HTTP / WebSocket / MQTT)
- [ ] Docs
- [ ] Tooling / CI

## Testing

Paste the output, not the intention.

- [ ] `./dev preflight`
- [ ] `./gradlew testDebugUnitTest` (code changes)
- [ ] Verified on a physical device — model and Android/Fire OS version:
- [ ] D-pad navigation and focus checked (any UI change)

State anything you could not verify and why. A gap named in the pull request is
a known limitation; a gap left out is a bug someone else finds later.

## Checklist

- [ ] Conventional Commit title
- [ ] No private project names, internal hostnames, real LAN addresses, or
      credentials — examples use RFC 5737 documentation ranges
- [ ] No co-authorship or sign-off trailer naming a tool, model or assistant
- [ ] No secrets, `.env` files, or keystores committed
- [ ] Failure states handled; nothing logs a token or auth header
- [ ] Docs and `CHANGELOG.md` updated if behavior changed
- [ ] Protocol change? Schema, examples, contract tests and docs all move here

Related issues:
