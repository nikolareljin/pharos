# Changelog

All notable changes to Pharos are recorded here. The format is one section per
release, newest first. The app version and the protocol version move
independently — a protocol change is called out explicitly.

The header format is load-bearing: release notes are generated from it, and a
malformed header silently falls back to an auto-generated commit list.

## [Unreleased]

### Added
- Repository foundation: MIT license, contribution and security policy, issue
  and pull-request templates, `.editorconfig`.
- Shared tooling: `script-helpers` as a submodule at `scripts/script-helpers`
  and the unified `./dev` CLI.
- `scripts/preflight.sh` — everything CI runs, plus the public-boundary scan and
  the CHANGELOG header check. Wired to the pre-push hook.
- `scripts/check-private-names.sh` — fails the build on a private project name,
  a private-range IP literal, a tooling co-authorship trailer, or committed
  signing material.
- Brand assets under `brand/` and `scripts/gen_icons.sh` to rasterize them.
- CI through the shared reusable workflows: build/lint/test, PR gate, release
  tag gate, auto-tag on merge, and a secret scan.
- Documentation set under `docs/`, split by audience.

## 2026-08-07 — v0.1.0

### Added
- `PLAN.md` — the master architecture and implementation roadmap.

[Unreleased]: https://github.com/nikolareljin/pharos/compare/0.1.0...HEAD
