#!/usr/bin/env python3
"""Generate adaptive + legacy launcher icon PNGs for kickstart-mobile.

Reads app/src/main/res/drawable/kickstart_icon.png (the source-of-truth
512x512 logo on green canvas) and writes:
  app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_foreground.png
  app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png
  app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_round.png

Foreground bitmaps fill the entire 108dp adaptive frame (no transparent
margin). The launcher mask clips the corners — that's intentional, the
source corners are solid green and survive clipping cleanly.
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

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


def main() -> None:
    src = Image.open(SOURCE).convert("RGBA")
    if src.size[0] != src.size[1]:
        raise SystemExit(f"source must be square: got {src.size}")

    for density, px in FOREGROUND_PX.items():
        out = RES / f"mipmap-{density}" / "ic_launcher_foreground.png"
        write(out, src.resize((px, px), Image.LANCZOS))

    for density, px in LEGACY_PX.items():
        resized = src.resize((px, px), Image.LANCZOS)
        write(RES / f"mipmap-{density}" / "ic_launcher.png", resized)
        write(RES / f"mipmap-{density}" / "ic_launcher_round.png", resized)

    print(f"wrote launcher icons under {RES}")


if __name__ == "__main__":
    main()
