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

# Resolve the package id from the built APK.
#
# The shared helper cannot be used here for two reasons, both of which make it
# report the wrong package for this repo:
#
#   1. Its aapt2 parse is greedy, so on build-tools 37 it captures the last
#      name='...' on the badging line — compileSdkVersionCodename='15' — and
#      returns "15".
#   2. Its fallback greps applicationId out of Gradle, which by design does not
#      know about applicationIdSuffix, so the debug build resolves to the
#      release package.
#
# Either one turns the post-install visibility check into a false alarm, and a
# check that cries wolf is a check people start passing --force to. Reported
# upstream; this override goes away when the helper is fixed.
pharos_package_name() {
  local artifact="$1" aapt badging
  aapt="$(android_sdk_tool aapt2 2>/dev/null)" || aapt="$(android_sdk_tool aapt 2>/dev/null)" || return 1

  # Captured rather than piped: under `set -o pipefail`, `grep -m1` closes the
  # pipe on its first match, aapt2 dies of SIGPIPE, and the whole pipeline
  # reports failure on a successful read.
  badging="$("$aapt" dump badging "$artifact" 2>/dev/null)" || return 1

  grep -m1 -oE "^package: name='[^']+'" <<<"$badging" |
    sed -E "s/^package: name='([^']+)'/\1/"
}

project_deploy() {
  shlib_import adb android gradle

  local mode=debug
  [[ "$DEV_RELEASE" == "true" ]] && mode=release

  local serial="$DEV_DEVICE"
  if [[ -z "$serial" ]]; then
    mapfile -t _serials < <(adb_ready_serials)
    if [[ ${#_serials[@]} -ne 1 ]]; then
      log_error "deploy: ${#_serials[@]} devices ready — pass --device <serial>"
      adb_list_devices
      return 1
    fi
    serial="${_serials[0]}"
  fi

  android_build "$DEV_REPO_ROOT" "$mode" apk || return 1

  local artifact
  artifact="$(android_artifact "$DEV_REPO_ROOT" "$mode" apk)" || {
    log_error "deploy: no APK found for variant $mode"
    return 1
  }

  local pkg
  pkg="$(pharos_package_name "$artifact")" || {
    log_error "deploy: could not read the package name from $artifact"
    return 1
  }

  log_info "deploy: $pkg ($mode) -> $serial"
  adb_install_verified "$serial" "$artifact" "$pkg" --user "$DEV_USER"
}
