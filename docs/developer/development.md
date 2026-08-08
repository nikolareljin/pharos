# Development

## Requirements

| | |
|---|---|
| JDK | 17 or newer (CI builds on 17) |
| Android SDK | platform 35 to compile, platform 30 available for testing |
| Gradle | none — use the wrapper in this repository |
| ADB | for installing on a device |
| Inkscape or ImageMagick | only if you regenerate brand assets |
| shellcheck | optional locally; CI runs it |

## Clone

```sh
git clone --recurse-submodules https://github.com/nikolareljin/pharos.git
cd pharos
./dev install
```

If you cloned without `--recurse-submodules`:

```sh
git submodule update --init --recursive
```

`scripts/script-helpers` is a public submodule pinned to its `production`
branch. Nothing in this repository depends on any private repository — Pharos
clones, builds and tests standalone, and it must stay that way.

## The `./dev` CLI

One entry point, shared across projects:

```sh
./dev                      # the verb list
./dev install              # submodules, hooks, dependencies. Idempotent
./dev build android        # assemble the debug APK
./dev test                 # unit tests
./dev preflight            # everything CI runs, plus what CI cannot
./dev deploy --device <id> # build, install, launch
./dev devices              # what is connected
./dev logs                 # filtered device logs
./dev clean                # build output and caches; never user data
```

`--release` switches to the release variant. `--user <id>` picks the Android
profile to install into; it defaults to `0`, the device owner.

Repo-specific behaviour lives in `scripts/project.sh` as `project_<verb>()`
overrides. **Do not edit `scripts/cli.sh`** — it is a shared template, refreshed
from upstream, and local edits are lost.

## Preflight

```sh
./dev preflight            # everything
./dev preflight --quick    # skip the APK assemble
```

It runs the public-boundary scan, the CHANGELOG header check, shellcheck,
schema contract tests (once schemas exist), Android lint, unit tests and the
debug assemble. The pre-push hook calls it. Do not bypass it — a red push
becomes a red pull request, and the red pull request is the one other people
see.

## Building and running

```sh
./gradlew assembleDebug
./gradlew lintDebug testDebugUnitTest
./dev deploy --device <serial>
```

On a TV device, connect over the network first:

```sh
adb connect 192.0.2.42:5555
./dev devices
./dev deploy --device 192.0.2.42:5555
```

Simulate a remote while developing:

```sh
adb shell input keyevent 19 20 21 22   # up down left right
adb shell input keyevent 23            # select
adb shell input keyevent 4             # back
```

## Emulators

Cover, in order of usefulness: Android TV 1080p API 30, current Android TV, a
phone, a tablet.

An emulator does not represent Fire TV performance. Anything about smoothness,
memory or cold start is measured on hardware or not claimed at all.

### One layout quirk worth knowing

The root project's Gradle output is `.build/`, not `build/`, because `./build`
at the repository root is the dev-CLI shim script and the two cannot share a
name — Gradle fails with an unhelpful "could not create directory" if they do.
`:app` is unaffected and keeps the standard `app/build/`.

## Project layout

```
app/            the Android application
brand/          logo, mark, TV banner — every icon is generated from these
docs/           this documentation
schemas/v1/     the protocol contract and its test vectors
examples/       runnable integration samples
scripts/        the ./dev CLI, preflight, boundary scan, icon generation
```

Modules split when they contain code. `:core:model`, `:core:runtime` and
`:core:protocol` appear as the runtime and protocol land — this repository does
not carry empty directories to look organised.

## The documentation site

The pages under `docs/` are published to
[nikolareljin.github.io/pharos](https://nikolareljin.github.io/pharos/) by
`.github/workflows/pages.yml`. Build it locally before changing structure:

```sh
python3 -m venv .venv && .venv/bin/pip install -r requirements-docs.txt
.venv/bin/mkdocs serve      # live reload on http://127.0.0.1:8000
.venv/bin/mkdocs build --strict
```

`--strict` is what CI runs: a broken internal link or a page missing from the
`nav` in `mkdocs.yml` fails the build. Without it MkDocs prints a warning nobody
reads and publishes the broken page anyway. A new page has to be added to the
`nav` — an orphaned file is a build failure, not a silent omission.

Links to files outside `docs/` (`PLAN.md`, `SECURITY.md`, `CONTRIBUTING.md`)
must be full GitHub URLs; MkDocs cannot resolve a relative path above its
`docs_dir`.

## Brand assets

```sh
bash scripts/gen_icons.sh          # everything
bash scripts/gen_icons.sh --docs-only
```

Edit `brand/*.svg`, regenerate, commit the PNGs. The outputs are committed
because CI has no rasterizer and must never need one. The script is idempotent —
a re-run on unchanged sources produces a byte-identical result, so a diff after
running it means a source actually changed.

The banner's wordmark is live text in DejaVu Sans Bold. Regenerating on a host
without that font shifts the lettering; check the diff.

## Code style

- Kotlin, 4-space indent, `PascalCase` types, `camelCase` members.
- Compose for all UI. D-pad navigation is a requirement, not an enhancement.
- `.editorconfig` covers the mechanical rules; Android lint runs in preflight.
- No `GlobalScope`, no networking on the main thread, no lifecycle-unaware Flow
  collection.

## Commits

Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`)
on a `feature/` or `fix/` branch. No co-authorship or sign-off trailer naming a
tool, model or assistant — the boundary scan rejects them.
