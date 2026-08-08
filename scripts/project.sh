#!/usr/bin/env bash
# Repo-specific overrides for ./dev. Sourced by scripts/cli.sh before dispatch.
#
# Keep this file small. Everything the shared template already does correctly for
# a Gradle/Android repo — build, deploy, devices, screenshot, logs — is left
# alone on purpose, so scripts/cli.sh can be refreshed from script-helpers
# without a merge conflict.

# `test` must mean "the unit tests", not the generic quick preflight. Until the
# Android project exists there is nothing to run, and saying so beats a green
# exit that asserted nothing.
project_test() {
  if [[ ! -f "$DEV_REPO_ROOT/settings.gradle.kts" ]]; then
    not_applicable test "no Gradle project yet — this repo is still bootstrapping"
    return
  fi
  shlib_import gradle
  gradle_run "$DEV_REPO_ROOT" testDebugUnitTest
}

# Everything CI runs, plus the checks CI cannot run for us: the public-boundary
# scan and the CHANGELOG header format. The pre-push hook calls this.
project_preflight() {
  bash "$DEV_REPO_ROOT/scripts/preflight.sh" "${DEV_ARGS[@]+"${DEV_ARGS[@]}"}"
}
