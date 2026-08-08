# Releasing

## Two version numbers

```text
App:      0.1.0     semantic versioning, in VERSION and the APK
Protocol: 1.0       independent; only changes when the wire contract does
```

An app release does not imply a protocol change. A protocol change within `1.x`
is additive and backward-compatible; anything breaking is `2.0`. Both are
reported in `status` and `heartbeat` so a controller can see exactly what it is
talking to.

## The flow

1. Branch `release/X.Y.Z` from `main`.
2. Update `VERSION`, the app's `versionName` / `versionCode`, and `CHANGELOG.md`.
3. Open a pull request. The release-tag gate fails it if `X.Y.Z` already exists
   — early, while it is still free to fix.
4. Merge with CI green.
5. The auto-tag workflow creates the tag from the merged release branch.
6. The release build publishes a signed APK with checksums and notes.

Nothing here is done by hand on `main`. No force-pushed tags, no `--admin`
merges, no locally created version tags.

## CHANGELOG format

The header is load-bearing. `script-helpers` parses it to extract release notes,
and a malformed header silently falls back to an auto-generated commit list —
the failure is invisible until someone reads a release page and finds it
useless.

```markdown
## 2026-08-07 — v0.1.0
```

`YYYY-MM-DD`, space, **em-dash** (`—`, not `-`), space, `vX.Y.Z`. Verify before
opening the release pull request:

```sh
./dev preflight     # includes changelog_check_header
```

## Signing

Release APKs are signed with a keystore held in repository secrets as
`KEYSTORE_B64`, decoded at build time, with the password, alias and key password
alongside it. **No signing material is ever committed** — the boundary scan
fails the build on a committed `.jks`, `.keystore`, `.p12` or `.pem`, and
`.gitignore` covers them as well.

When the secret is absent the release build still produces an unsigned APK
rather than failing, so a fork can build without access to the signing key.

## Publishing

Each release carries the APK, its SHA-256 checksum, and notes extracted from the
CHANGELOG.

Distribution, in the intended order: GitHub Releases first; ADB and local
sideload; then an evaluation of the Amazon Appstore, Google Play and F-Droid.
The application id `io.github.nikolareljin.pharos` must be settled before any
store submission — changing it afterwards is a migration, not a rename.

## Before tagging a release

- [ ] `./dev preflight` clean
- [ ] CI green on `main`
- [ ] Verified on physical `AFTKM` hardware against the
      [Fire TV acceptance checklist](../user/fire-tv.md#release-acceptance-checklist)
- [ ] Docs updated for anything that changed behaviour
- [ ] `CHANGELOG.md` header formatted correctly
- [ ] No private references anywhere in the tree
- [ ] Protocol version confirmed — unchanged, or bumped with schemas, examples
      and contract tests moving together
