#!/usr/bin/env python3
"""Generate adaptive + legacy launcher icon PNGs for kickstart-mobile.

Reads app/src/main/res/drawable/kickstart_icon.png (the source-of-truth
512x512 logo on green canvas) and writes:
  app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_foreground.png
  app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png
  app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_round.png

Foregrounds use a 108dp canvas with the source image centered at CONTENT_SCALE
of the canvas size. The transparent margin lets squircle/circle launcher masks
reveal more of the logo — the outer 18dp on each side of an adaptive foreground
is parallax bleed that the launcher hides anyway.

Legacy bitmaps stay at full bleed because adaptive launchers ignore them.
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

# Fraction of the 108dp adaptive foreground canvas occupied by the source image.
CONTENT_SCALE = 0.85

FOREGROUND_PX = {
    "mdpi": 108,
    "hdpi": 162,
    "xhdpi": 216,
    "xxhdpi": 324,
    "xxxhdpi": 432,
}
LEGACY_PX = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

APP_DIR = Path(__file__).resolve().parent.parent
RES = APP_DIR / "app" / "src" / "main" / "res"
SOURCE = RES / "drawable" / "kickstart_icon.png"


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
