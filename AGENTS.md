# Repository instructions

Pharos is a **public** Android application. Read `PLAN.md` first — it is the
master roadmap and answers most design questions, including the ones answered
*no*.

## Hard rules

1. **Nothing private enters this tree.** No private project names, internal
   hostnames, real LAN addresses, credentials, tokens, certificates, personal
   information, or code copied from a closed project. Examples use the RFC 5737
   documentation ranges (`192.0.2.0/24`, `198.51.100.0/24`, `203.0.113.0/24`), an
   mDNS `.local` name, or loopback. `scripts/check-private-names.sh` enforces
   the mechanical half; it does not replace reading what you wrote.
2. **This repository builds standalone.** No dependency — submodule, copy, or
   build-time fetch — on any private repository. The only shared dependencies
   are the public `script-helpers` submodule and the public `ci-helpers`
   reusable workflows.
3. **No co-authorship or sign-off trailer naming a tool, model or assistant**,
   in any commit, amend, squash or rebase, ever.
4. **Branch before writing files.** `feature/<topic>` or `fix/<topic>`; never
   commit to `main`. Open a pull request and stop there — do not merge, do not
   create tags, do not force-push.
5. **No arbitrary execution reaches the device.** Commands are a fixed
   allowlist. No shell, no eval, no downloaded code, no unrestricted JavaScript
   bridge. This is not a configurable trade-off.

## Working here

- `./dev` is the entry point: `build`, `test`, `deploy`, `devices`, `logs`,
  `clean`, `preflight`. Repo-specific behaviour belongs in `scripts/project.sh`,
  never in `scripts/cli.sh` — that file is a template refreshed from upstream.
- `./dev preflight` runs everything CI runs plus the public-boundary scan and
  the CHANGELOG header check. The pre-push hook calls it.
- Icons and banners are generated: edit `brand/*.svg`, then run
  `bash scripts/gen_icons.sh` and commit the output. Never hand-edit a
  generated PNG.
- CI calls the shared reusable workflows pinned at `@production`. Not `@main`,
  not a version tag, not a commit SHA.
- `CHANGELOG.md` headers must read `## YYYY-MM-DD — vX.Y.Z` with an em-dash.
  A hyphen makes the release notes silently fall back to a commit list.

## Definition of done

`PLAN.md` §69. In short: implementation complete, tests present, TV remote
behaviour verified for UI changes, failure states handled, logs sanitized, docs
updated, no private references, CI green.
