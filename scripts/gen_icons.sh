#!/usr/bin/env bash
# SCRIPT: gen_icons.sh
# DESCRIPTION: Rasterize the brand SVGs into launcher icons, the TV banner and
#              the documentation assets. Idempotent — a re-run on an unchanged
#              brand/ produces no diff.
# USAGE: bash scripts/gen_icons.sh [--docs-only]
#
# PARAMETERS:
#   --docs-only  Skip the Android resources even when app/ exists.
#   -h           Show this help message.
#
# EXIT_CODES:
#   0  Assets written (or already current).
#   1  No rasterizer available, or a render failed.
#   2  Bad arguments.
#
# NOTES:
#   Outputs are committed. CI has no rasterizer and must never need one: a build
#   that depends on Inkscape being installed is a build that breaks on a machine
#   nobody has looked at yet. The banner's wordmark is live text in DejaVu Sans
#   Bold, so regenerating on a host without that font shifts the lettering —
#   another reason the PNG is committed rather than built.
#
#   Sizes follow the Android asset contract:
#     ic_launcher_foreground 108/162/216/324/432 (108dp adaptive layer)
#     ic_launcher_monochrome same sizes (themed icons, Android 13+)
#     banner                 320x180 in drawable-xhdpi (leanback launcher)
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
shlib_import logging help svg

DOCS_ONLY=false
while [[ $# -gt 0 ]]; do
  case "$1" in
    --docs-only) DOCS_ONLY=true; shift ;;
    -h|--help) show_help "${BASH_SOURCE[0]}"; exit 0 ;;
    *) log_error "unknown argument: $1"; exit 2 ;;
  esac
done

cd "$ROOT_DIR"

svg_rasterizer >/dev/null || {
  log_error "no rasterizer found — install inkscape (preferred) or imagemagick"
  exit 1
}

MASTER="brand/pharos-logo.svg"
MARK="brand/pharos-mark.svg"
BANNER="brand/pharos-banner.svg"
for f in "$MASTER" "$MARK" "$BANNER"; do
  [[ -f "$f" ]] || { log_error "missing brand source: $f"; exit 1; }
done

render() {
  # render <in.svg> <out.png> <width> [height]
  local in="$1" out="$2" w="$3" h="${4:-$3}"
  mkdir -p "$(dirname "$out")"
  if command -v inkscape >/dev/null 2>&1; then
    inkscape "$in" --export-type=png --export-filename="$out" \
      --export-width="$w" --export-height="$h" >/dev/null 2>&1
  elif command -v magick >/dev/null 2>&1; then
    magick -background none "$in" -resize "${w}x${h}!" "$out"
  else
    convert -background none "$in" -resize "${w}x${h}!" "$out"
  fi || { log_error "render failed: $in -> $out"; exit 1; }
  log_info "  $out (${w}x${h})"
}

# --- Documentation assets -----------------------------------------------------

log_info "docs assets"
cp "$MASTER" docs/assets/logo.svg
cp "$BANNER" docs/assets/banner.svg
render "$MASTER" docs/assets/icon-512.png 512
render "$MASTER" docs/assets/icon-192.png 192
render "$MASTER" docs/assets/favicon.png 64
render "$BANNER" docs/assets/banner.png 320 180

# --- Android resources --------------------------------------------------------

RES="app/src/main/res"
if [[ "$DOCS_ONLY" == "true" ]]; then
  log_info "android resources skipped (--docs-only)"
  exit 0
fi
if [[ ! -d "app/src/main" ]]; then
  log_warn "no Android project yet — skipping launcher icons and banner."
  log_warn "Re-run this script once app/ exists."
  exit 0
fi

# minSdk is 26, so the adaptive icon in mipmap-anydpi applies on every
# supported device and legacy square launcher PNGs would be dead weight the
# system never reads. Only the adaptive layers are generated.
log_info "launcher icons"
densities=(mdpi hdpi xhdpi xxhdpi xxxhdpi)
foreground=(108 162 216 324 432)

for i in "${!densities[@]}"; do
  render "$MARK" "$RES/mipmap-${densities[$i]}/ic_launcher_foreground.png" "${foreground[$i]}"
done

# The themed (monochrome) layer is the mark flattened to a single opaque colour;
# Android tints it to match the user's wallpaper. Generated here rather than
# committed as a fourth brand file, because it is purely mechanical.
log_info "monochrome layer"
MONO="$(mktemp -t pharos-mono-XXXXXX.svg)"
trap 'rm -f "$MONO"' EXIT
sed -E -e 's/#(0F172A|1E293B|38BDF8|F59E0B|FEF08A|FBBF24|334155|F8FAFC)/#FFFFFF/gI' \
       -e 's/opacity="[0-9.]+"/opacity="1"/g' "$MARK" > "$MONO"
for i in "${!densities[@]}"; do
  render "$MONO" "$RES/mipmap-${densities[$i]}/ic_launcher_monochrome.png" "${foreground[$i]}"
done

log_info "tv banner"
render "$BANNER" "$RES/drawable-xhdpi/banner.png" 320 180

log_info "done. Commit the generated PNGs — CI does not rasterize."
