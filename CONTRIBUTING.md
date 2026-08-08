# Contributing to Pharos

Thanks for considering it. This document is short on ceremony and specific about
the few things that will get a pull request sent back.

## Before you start

Read [`PLAN.md`](PLAN.md). It is the master roadmap — architecture, protocol,
phases, and the definition of done (§69). Most "should Pharos do X?" questions
are already answered there, including the ones answered *no*: §4 lists the
non-goals, and §16 lists why no command will ever execute arbitrary code.

For anything larger than a bug fix, open an issue first and describe the problem
before the solution.

## Setting up

```sh
git clone --recurse-submodules https://github.com/nikolareljin/pharos.git
cd pharos
./dev install
./dev preflight
```

`./dev` is the single entry point — `build`, `test`, `deploy`, `devices`,
`logs`, `clean`, `preflight`. Run `./dev` with no arguments for the full list.
The pre-push hook runs `./dev preflight`; do not bypass it.

Toolchain and device setup live in
[`docs/developer/development.md`](docs/developer/development.md).

## The rules that actually block a merge

**1. This repository is public and stays clean.**
No private project names, internal hostnames, real LAN addresses, credentials,
tokens, certificates, personal information, or code copied from a closed
project. `scripts/check-private-names.sh` enforces the mechanical half of this
and runs in preflight and in CI. Use the RFC 5737 documentation ranges
(`192.0.2.0/24`, `198.51.100.0/24`, `203.0.113.0/24`), an mDNS `.local` name, or
loopback in examples — never a real address.

**2. Untrusted input is validated before it is believed.**
Every inbound message is schema-validated and size-limited before it reaches the
router. A widget type you do not recognise renders a placeholder; it never
crashes the screen.

**3. TV behaviour is part of the change, not a follow-up.**
Any UI change is navigable with a D-pad, shows its focus, and restores focus on
return. "Works on a phone" is half a change.

**4. Failure states are handled where they happen.**
No raw transport errors in the UI (`PLAN.md` §57), no unbounded retry loops, no
logging of tokens or auth headers, no silent catch that discards the payload and
reports success.

**5. Tests come with the code.**
Protocol parsing, priority ordering, deduplication, provider state machines and
retry/backoff are unit-tested. UI changes get a focus/navigation test. See
[`docs/developer/testing.md`](docs/developer/testing.md).

## Commits and branches

- Branch from `main`: `feature/<topic>`, `fix/<topic>`.
- Conventional Commits for the subject line: `feat:`, `fix:`, `docs:`,
  `refactor:`, `test:`, `chore:`.
- **No co-authorship or sign-off trailers naming a tool, model or assistant.**
  The scan rejects them.
- Keep pull requests focused. A protocol change, an app change and a docs
  overhaul are three pull requests.

## Protocol changes

The protocol is a public product surface, and someone else's code depends on it.
Within a major version, changes are additive: unknown optional fields are
ignored, existing fields never change meaning, and anything breaking becomes the
next major version. A protocol change means the schema, the examples, the
contract tests and `docs/developer/protocol.md` all move in the same pull
request. See `PLAN.md` §64.

## Releases

Releases run through a `release/X.Y.Z` branch, a tag created on merge, and a
signed APK published to GitHub Releases with checksums. Maintainers do this; a
contributor never needs to bump a version by hand. See
[`docs/operations/release.md`](docs/operations/release.md).

## Code of conduct

Participation is governed by [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
