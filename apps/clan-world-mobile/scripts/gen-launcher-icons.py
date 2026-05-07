#!/usr/bin/env python3
"""Generate adaptive + legacy launcher icon PNGs from the source square logo.

Run from any cwd. Reads <repo>/images/square-logo-main.png and writes:
  app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_foreground.png
  app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png
  app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_round.png

Legacy bitmaps (ic_launcher.png / ic_launcher_round.png) are only used by the
rare custom launcher that ignores mipmap-anydpi-v26/. Adaptive icons (foreground +
background color) are used by all stock Android 8+ launchers.

Foregrounds use a 108dp canvas with the source image centered at CONTENT_SCALE
of the canvas size. The transparent margin lets squircle/circle launcher masks
reveal more of the title lockup — the outer 18dp on each side of an adaptive
foreground is parallax bleed that the launcher hides anyway, so a snug source
gets clipped into more than expected.

Legacy bitmaps stay at full bleed because adaptive launchers ignore them; the
rare custom launcher that uses them gets a tighter logo, which still reads.
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

# Fraction of the 108dp adaptive foreground canvas occupied by the source image.
# 0.85 leaves ~8dp of transparent margin on each side, so squircle masks (which
# typically reveal the inner ~80dp) show essentially the whole logo.
CONTENT_SCALE = 0.85

# 108 dp adaptive foreground at each density bucket.
FOREGROUND_PX = {
    "mdpi": 108,
    "hdpi": 162,
    "xhdpi": 216,
    "xxhdpi": 324,
    "xxxhdpi": 432,
}
# 48 dp legacy launcher bitmap at each density bucket.
LEGACY_PX = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

APP_DIR = Path(__file__).resolve().parent.parent  # apps/clan-world-mobile
REPO_ROOT = APP_DIR.parent.parent  # repo root
SOURCE = REPO_ROOT / "images" / "square-logo-main.png"
RES = APP_DIR / "app" / "src" / "main" / "res"


def write(target: Path, img: Image.Image) -> None:
    target.parent.mkdir(parents=True, exist_ok=True)
    img.save(target, format="PNG", optimize=True)


def make_foreground(src: Image.Image, frame_px: int) -> Image.Image:
    """Center src at CONTENT_SCALE inside a transparent frame_px canvas."""
    content_px = max(1, round(frame_px * CONTENT_SCALE))
    resized = src.resize((content_px, content_px), Image.LANCZOS)
    canvas = Image.new("RGBA", (frame_px, frame_px), (0, 0, 0, 0))
    pad = (frame_px - content_px) // 2
    canvas.paste(resized, (pad, pad), resized)
    return canvas


def main() -> None:
    src = Image.open(SOURCE).convert("RGBA")
    if src.size[0] != src.size[1]:
        raise SystemExit(f"source must be square: got {src.size}")

    for density, px in FOREGROUND_PX.items():
        out = RES / f"mipmap-{density}" / "ic_launcher_foreground.png"
        write(out, make_foreground(src, px))

    for density, px in LEGACY_PX.items():
        resized = src.resize((px, px), Image.LANCZOS)
        write(RES / f"mipmap-{density}" / "ic_launcher.png", resized)
        write(RES / f"mipmap-{density}" / "ic_launcher_round.png", resized)

    print(f"wrote launcher icons under {RES}")


if __name__ == "__main__":
    main()
