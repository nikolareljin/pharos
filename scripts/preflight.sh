#!/usr/bin/env bash
# SCRIPT: preflight.sh
# DESCRIPTION: Every check CI runs, plus the two it cannot run for us — the
#              public-boundary scan and the CHANGELOG header format. The
#              pre-push hook calls this through `./dev preflight`.
# USAGE: ./dev preflight [--quick]
#
# PARAMETERS:
#   --quick   Skip the APK assemble (the slowest step). Lint and unit tests
#             still run.
#   -h        Show this help message.
#
# EXIT_CODES:
#   0  Everything passed.
#   1  A check failed. The failing check names itself.
#   2  Bad arguments.
# ----------------------------------------------------
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

SCRIPT_HELPERS_DIR="${SCRIPT_HELPERS_DIR:-$SCRIPT_DIR/script-helpers}"
if [[ ! -f "$SCRIPT_HELPERS_DIR/helpers.sh" ]]; then
  echo "script-helpers not initialized. Run: git submodule update --init --recursive" >&2
  exit 1
fi
# shellcheck source=/dev/null
source "$SCRIPT_HELPERS_DIR/helpers.sh"
shlib_import logging help changelog

QUICK=false
while [[ $# -gt 0 ]]; do
  case "$1" in
    --quick) QUICK=true; shift ;;
    -h|--help) show_help "${BASH_SOURCE[0]}"; exit 0 ;;
    *) log_error "unknown argument: $1"; exit 2 ;;
  esac
done

cd "$ROOT_DIR"

FAILED=()
run_check() {
  local name="$1"; shift
  log_info "preflight: $name"
  if "$@"; then
    return 0
  fi
  log_error "preflight: $name FAILED"
  FAILED+=("$name")
  return 0
}

# --- Checks that apply from the very first commit -----------------------------

run_check "public-boundary scan" bash "$SCRIPT_DIR/check-private-names.sh"
run_check "CHANGELOG header format" changelog_check_header "$ROOT_DIR/CHANGELOG.md"

if command -v shellcheck >/dev/null 2>&1; then
  # Only the scripts this repository owns. scripts/cli.sh, scripts/_bootstrap.sh
  # and the ./dev shims are upstream templates — they are refreshed from
  # script-helpers, cannot be edited here, and linting them would fail this repo
  # for a finding it is not allowed to fix.
  run_check "shellcheck" shellcheck -S warning \
    "$SCRIPT_DIR/preflight.sh" \
    "$SCRIPT_DIR/check-private-names.sh" \
    "$SCRIPT_DIR/gen_icons.sh" \
    "$SCRIPT_DIR/project.sh"
else
  log_warn "preflight: shellcheck not installed — skipping (CI still runs it)"
fi

# --- Protocol schemas, once schemas/ exists -----------------------------------

if [[ -d "$ROOT_DIR/schemas" ]]; then
  if command -v python3 >/dev/null 2>&1; then
    run_check "schema contract tests" python3 "$SCRIPT_DIR/validate_schemas.py"
  else
    log_warn "preflight: python3 not installed — schema tests skipped"
  fi
fi

# --- Android, once the Gradle project exists ----------------------------------

if [[ -f "$ROOT_DIR/settings.gradle.kts" ]]; then
  shlib_import gradle
  run_check "android lint" gradle_run "$ROOT_DIR" lintDebug
  run_check "unit tests" gradle_run "$ROOT_DIR" testDebugUnitTest
  if [[ "$QUICK" == "true" ]]; then
    log_info "preflight: skipping assembleDebug (--quick)"
  else
    run_check "assemble debug APK" gradle_run "$ROOT_DIR" assembleDebug
  fi
else
  log_info "preflight: no Gradle project yet — Android checks skipped"
fi

# --- Verdict ------------------------------------------------------------------

if [[ ${#FAILED[@]} -gt 0 ]]; then
  log_error "preflight: ${#FAILED[@]} check(s) failed: ${FAILED[*]}"
  exit 1
fi
log_info "preflight: all checks passed"
