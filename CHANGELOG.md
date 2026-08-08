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
- `docs/developer/fire-tv-debugging.md` — the full debugging workflow for a Fire
  TV: why the cable is not an option, enabling ADB, connecting and authorising,
  installing into the right profile, driving the remote from a keyboard, reading
  logs, running the instrumented tests, and switching debugging off again.
- Android application bootstrap: Kotlin, Jetpack Compose, Gradle 8.13 wrapper,
  AGP 8.7, a `gradle/libs.versions.toml` version catalog, `minSdk` 26 /
  `targetSdk` 35, and no dependency on Google Play Services.
- Persistent random node identity, generated once and stable across restarts.
- Capability detection (leanback, touch, audio, camera, microphone, web).
- Logical input abstraction mapping remote, keyboard and gamepad keys to one set
  of actions.
- Focus-aware UI with a focus indicator that changes colour, border and scale
  together rather than relying on colour alone.
- Diagnostics screen: versions, node id, device, memory, storage, display and
  capabilities.
- Launcher icons, adaptive and themed icon layers, and the TV banner, all
  generated from `brand/`.
- 16 unit tests covering identity, capability mapping and input; instrumented
  D-pad navigation tests.

## 2026-08-07 — v0.1.0

### Added
- `PLAN.md` — the master architecture and implementation roadmap.

[Unreleased]: https://github.com/nikolareljin/pharos/compare/0.1.0...HEAD
