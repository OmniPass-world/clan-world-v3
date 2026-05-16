#!/usr/bin/env python3
"""
Strip white backgrounds from per-clan walk-sheet PNGs.

The walk-sheet PNGs at apps/web/src/assets/clansmen/clan-*-walk.png ship as
RGB (no alpha channel) with near-white (~240-254) backgrounds. PixiJS renders
those backgrounds as opaque white rectangles behind each living clansman
sprite on the world map (see issue #325).

This script does a 4-connected flood-fill from each PNG edge — any pixel
reachable from an edge whose RGB are all >= THRESHOLD becomes transparent.
Flood-fill (rather than a bare RGB threshold) preserves near-white pixels
inside the figure silhouette (shield emblems, teeth, armor highlights, etc.)
that aren't connected to the outer background.

Idempotent: re-running on an already-transparent file changes nothing.

Usage:
    python3 scripts/strip-walk-sheet-white-bg.py            # process all sheets
    python3 scripts/strip-walk-sheet-white-bg.py --check    # exit 1 if any sheet still has opaque white bg
"""
from __future__ import annotations

import sys
from pathlib import Path
from PIL import Image

THRESHOLD = 235  # R, G, B all >= this counts as "near-white background"
ASSET_DIR = Path(__file__).resolve().parents[1] / "apps" / "web" / "src" / "assets" / "clansmen"


def strip_white_bg(path: Path) -> tuple[bool, int]:
    """Returns (changed, transparent_pixel_count)."""
    img = Image.open(path)
    was_rgb = img.mode != "RGBA"
    img = img.convert("RGBA")
    pixels = img.load()
    w, h = img.size

    # Seed flood-fill stack from all 4 edges.
    stack: list[tuple[int, int]] = []
    for x in range(w):
        for y in (0, h - 1):
            r, g, b, _ = pixels[x, y]
            if r >= THRESHOLD and g >= THRESHOLD and b >= THRESHOLD:
                stack.append((x, y))
    for y in range(h):
        for x in (0, w - 1):
            r, g, b, _ = pixels[x, y]
            if r >= THRESHOLD and g >= THRESHOLD and b >= THRESHOLD:
                stack.append((x, y))

    visited = bytearray(w * h)
    changed_count = 0
    while stack:
        x, y = stack.pop()
        if x < 0 or x >= w or y < 0 or y >= h:
            continue
        idx = y * w + x
        if visited[idx]:
            continue
        r, g, b, a = pixels[x, y]
        if r >= THRESHOLD and g >= THRESHOLD and b >= THRESHOLD:
            visited[idx] = 1
            if a != 0:
                pixels[x, y] = (r, g, b, 0)
                changed_count += 1
            stack.extend(((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)))

    if was_rgb or changed_count > 0:
        img.save(path, optimize=True)
        return True, changed_count
    return False, 0


def check_white_bg(path: Path) -> bool:
    """Returns True if path has any opaque near-white pixel along its outer
    border (i.e. issue #325 bug present).

    Walks the full top/bottom rows + left/right columns rather than just the
    four corners — a sheet could have transparent corners but still leak
    opaque-white bg along an interior edge segment, which would still render
    as a visible halo in PixiJS.
    """
    img = Image.open(path)
    if img.mode != "RGBA":
        return True
    w, h = img.size
    pixels = img.load()

    def is_opaque_white(x: int, y: int) -> bool:
        r, g, b, a = pixels[x, y]
        return a > 0 and r >= THRESHOLD and g >= THRESHOLD and b >= THRESHOLD

    # Top + bottom rows.
    for x in range(w):
        if is_opaque_white(x, 0) or is_opaque_white(x, h - 1):
            return True
    # Left + right columns (skip already-checked corners).
    for y in range(1, h - 1):
        if is_opaque_white(0, y) or is_opaque_white(w - 1, y):
            return True
    return False


def main() -> int:
    check_mode = "--check" in sys.argv
    sheets = sorted(ASSET_DIR.glob("clan-*-walk.png"))
    if not sheets:
        print(f"ERROR: no walk-sheet PNGs found in {ASSET_DIR}", file=sys.stderr)
        return 2

    if check_mode:
        bad = [p for p in sheets if check_white_bg(p)]
        if bad:
            print("Opaque white-bg walk-sheets (issue #325 regression):", file=sys.stderr)
            for p in bad:
                print(f"  {p.relative_to(ASSET_DIR.parents[3])}", file=sys.stderr)
            return 1
        print(f"OK: all {len(sheets)} walk-sheets have transparent backgrounds")
        return 0

    for sheet in sheets:
        changed, n = strip_white_bg(sheet)
        rel = sheet.relative_to(ASSET_DIR.parents[3])
        if changed:
            print(f"  {rel}: transparented {n} pixels")
        else:
            print(f"  {rel}: already transparent (skipped)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
