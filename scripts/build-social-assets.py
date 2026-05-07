#!/usr/bin/env python3
"""
Build derived social/PWA/favicon assets for apps/landing and apps/web.

Inputs (in repo-root images/):
  - square-logo-main.png   -> source for all favicons + PWA icons
  - banner-main.png        -> source for og-image (1200x630 letterboxed)

Outputs (in apps/<app>/public/):
  - favicon.ico            (multi-resolution: 16x16 + 32x32)
  - favicon-16x16.png
  - favicon-32x32.png
  - apple-touch-icon.png   (180x180)
  - icon-192.png           (PWA)
  - icon-512.png           (PWA)
  - og-image.png           (1200x630, letterboxed onto BG_COLOR)

Reproducible: re-run on any source-image change.
Usage: python3 scripts/build-social-assets.py
"""
from __future__ import annotations

import os
import sys
from pathlib import Path

from PIL import Image

REPO_ROOT = Path(__file__).resolve().parent.parent
IMAGES_DIR = REPO_ROOT / "images"
APPS = ["apps/landing/public", "apps/web/public"]

# Background for og-image letterboxing — matches theme_color in manifests.
BG_COLOR = (0x0D, 0x1A, 0x0D)  # #0d1a0d


def derive_favicons(square_src: Path, out_dir: Path) -> None:
    """Generate favicon.ico, favicon-16, favicon-32, apple-touch, icon-192, icon-512."""
    src = Image.open(square_src).convert("RGBA")

    sizes = {
        "favicon-16x16.png": (16, 16),
        "favicon-32x32.png": (32, 32),
        "apple-touch-icon.png": (180, 180),
        "icon-192.png": (192, 192),
        "icon-512.png": (512, 512),
    }
    for name, size in sizes.items():
        img = src.resize(size, Image.LANCZOS)
        img.save(out_dir / name, format="PNG", optimize=True)
        print(f"  wrote {out_dir / name}: {size}")

    # Multi-res ICO containing 16 + 32
    ico_path = out_dir / "favicon.ico"
    src.save(ico_path, format="ICO", sizes=[(16, 16), (32, 32)])
    print(f"  wrote {ico_path}: ICO[16,32]")


def derive_og_image(banner_src: Path, out_dir: Path) -> None:
    """Resize banner to fit 1200x630 preserving aspect, paste centered onto BG_COLOR canvas."""
    target_w, target_h = 1200, 630
    src = Image.open(banner_src).convert("RGBA")

    src_w, src_h = src.size
    scale = min(target_w / src_w, target_h / src_h)
    new_w = int(round(src_w * scale))
    new_h = int(round(src_h * scale))
    resized = src.resize((new_w, new_h), Image.LANCZOS)

    canvas = Image.new("RGB", (target_w, target_h), BG_COLOR)
    paste_x = (target_w - new_w) // 2
    paste_y = (target_h - new_h) // 2
    # Composite RGBA onto RGB canvas using alpha as mask
    canvas.paste(resized, (paste_x, paste_y), resized)

    out_path = out_dir / "og-image.png"
    canvas.save(out_path, format="PNG", optimize=True)
    print(f"  wrote {out_path}: {canvas.size} (banner {(new_w, new_h)} centered)")


def main() -> int:
    square = IMAGES_DIR / "square-logo-main.png"
    banner = IMAGES_DIR / "banner-main.png"

    for src in (square, banner):
        if not src.exists():
            print(f"ERROR: source missing: {src}", file=sys.stderr)
            return 2

    for app in APPS:
        out_dir = REPO_ROOT / app
        out_dir.mkdir(parents=True, exist_ok=True)
        print(f"\n[{app}]")
        derive_favicons(square, out_dir)
        derive_og_image(banner, out_dir)

    print("\nAll assets written.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
