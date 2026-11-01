#!/usr/bin/env bash
# Builds a rigged character's runtime assets from its SVG sources.
#
#   tools/build-character.sh arnold [--scale 0.5]
#
# Reads  assets-src/characters/<name>/{rig.json,parts/*.svg}
# Writes assets/sprites/characters/<name>/*.png   (alpha-trimmed part images)
#        assets/data/rigs/<name>.json             (runtime rig: pivots + trim offsets)
#
# The source parts are full-canvas and pre-aligned, so every part SVG rasterizes to the same
# square and the art sits wherever it belongs on that canvas. Shipping them that way would cost
# one full canvas of VRAM per part, so each is trimmed to its opaque bounds here and the offset
# that trimming removed is recorded in the generated rig.
#
# Every coordinate in the generated rig stays in SOURCE CANVAS SPACE (1024, y-down), independent
# of --scale. The runtime converts to world space (y-up) exactly once, in RiggedEntity. Changing
# --scale therefore changes only image resolution, never the rig.
#
# Requires: rsvg-convert, python3 with Pillow. Outputs are committed - CI has neither.
set -euo pipefail

NAME="${1:-}"
SCALE="0.5"

if [ -z "$NAME" ] || [ "$NAME" = "--help" ]; then
  echo "usage: tools/build-character.sh <name> [--scale <factor>]" >&2
  exit 1
fi
shift

while [ $# -gt 0 ]; do
  case "$1" in
    --scale) SCALE="${2:-}"; shift 2 ;;
    *) echo "unknown option: $1" >&2; exit 1 ;;
  esac
done

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="$ROOT/assets-src/characters/$NAME"
OUT_SPRITES="$ROOT/assets/sprites/characters/$NAME"
OUT_RIG="$ROOT/assets/data/rigs/$NAME.json"

[ -d "$SRC" ] || { echo "no character sources at $SRC" >&2; exit 1; }
command -v rsvg-convert >/dev/null || { echo "rsvg-convert not found" >&2; exit 1; }

CANVAS="$(python3 -c "import json;d=json.load(open('$SRC/rig.json'));print(d['canvas'][0])")"
RASTER="$(python3 -c "print(round($CANVAS * $SCALE))")"

echo "building $NAME: ${CANVAS}px canvas at scale $SCALE -> ${RASTER}px raster"

STAGE="$(mktemp -d)"
trap 'rm -rf "$STAGE"' EXIT

for svg in "$SRC"/parts/*.svg; do
  rsvg-convert -w "$RASTER" -h "$RASTER" "$svg" -o "$STAGE/$(basename "${svg%.svg}").png"
done

rm -rf "$OUT_SPRITES"
mkdir -p "$OUT_SPRITES" "$(dirname "$OUT_RIG")"

NAME="$NAME" SRC="$SRC" STAGE="$STAGE" OUT_SPRITES="$OUT_SPRITES" OUT_RIG="$OUT_RIG" \
SCALE="$SCALE" CANVAS="$CANVAS" python3 - <<'PY'
import json, os
from PIL import Image

name, src, stage = os.environ["NAME"], os.environ["SRC"], os.environ["STAGE"]
out_sprites, out_rig = os.environ["OUT_SPRITES"], os.environ["OUT_RIG"]
scale, canvas = float(os.environ["SCALE"]), int(os.environ["CANVAS"])

source = json.load(open(os.path.join(src, "rig.json")))

bones = []
fx0 = fy0 = float("inf")
fx1 = fy1 = float("-inf")

for bone_id, bone in source["bones"].items():
    # The simple kit uses "part" (one part per bone); the cap kit uses "parts" (several). Only
    # the one-to-one form is supported for now - several parts on one bone needs the slot layer.
    parts = bone["parts"] if "parts" in bone else [bone["part"]]
    if len(parts) != 1:
        raise SystemExit(
            f"bone '{bone_id}' has {len(parts)} parts; multi-part bones need a slot layer, "
            "which this build does not support yet"
        )
    part = parts[0]

    image = Image.open(os.path.join(stage, f"{part}.png")).convert("RGBA")
    box = image.getchannel("A").getbbox()
    if box is None:
        raise SystemExit(f"part '{part}' is fully transparent")

    image.crop(box).save(os.path.join(out_sprites, f"{part}.png"))

    # Raster pixels back to source canvas coordinates, so the rig is scale-independent.
    x0, y0, x1, y1 = (v / scale for v in box)
    fx0, fy0, fx1, fy1 = min(fx0, x0), min(fy0, y0), max(fx1, x1), max(fy1, y1)

    bones.append({
        "id": bone_id,
        "parent": bone["parent"],
        "pivotX": float(bone["pivot"][0]),
        "pivotY": float(bone["pivot"][1]),
        "part": f"sprites/characters/{name}/{part}.png",
        "trimX": x0,
        "trimY": y0,
        "partWidth": x1 - x0,
        "partHeight": y1 - y0,
        # Draw order is the part's file-number prefix, NOT the bone tree: the far arm draws
        # behind the torso and the near arm in front, though both hang off the same bone.
        "z": int(part.split("_", 1)[0]),
    })

bones.sort(key=lambda b: b["z"])

json.dump({
    "id": name,
    "canvas": canvas,
    "rasterScale": scale,
    "figureX": fx0,
    "figureY": fy0,
    "figureWidth": fx1 - fx0,
    "figureHeight": fy1 - fy0,
    "bones": bones,
}, open(out_rig, "w"), indent=2)
open(out_rig, "a").write("\n")

print(f"  {len(bones)} parts -> {out_sprites}")
print(f"  figure {fx1 - fx0:.0f}x{fy1 - fy0:.0f} at ({fx0:.0f},{fy0:.0f}) in {canvas}px canvas")
print(f"  rig -> {out_rig}")
PY
