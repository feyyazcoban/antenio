"""Renders the Play store graphics into design/store from the brand vectors already in the app.

icon-512.png is the launcher icon (green field, black play mark; drawable/ic_launcher_*), full bleed: Play rounds the
corners itself. tv-banner-1280x720.png is drawable/app_banner at four times its size; feature-1024x500.png sets the
horizontal logo on the app's background. The SVG sources stay next to the PNGs. Chrome draws them headless; the
banner and the feature graphic are saved without alpha as Play asks.

    python tools/make_store_graphics.py
"""
import re
import subprocess
import tempfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "design" / "store"
CHROME = Path(r"C:\Program Files\Google\Chrome\Application\chrome.exe")
LOGO = (ROOT / "design" / "brand" / "antenio-yatay.svg").read_text(encoding="utf-8")
LOGO_INNER = re.sub(r"^.*?<svg[^>]*>|</svg>\s*$", "", LOGO, flags=re.S)
MARK = "".join(f'<path fill="#000" d="{d}"/>' for d in re.findall(
    r'android:pathData="([^"]+)"',
    (ROOT / "apps/android-tv/app/src/main/res/drawable/ic_launcher_foreground.xml").read_text(encoding="utf-8")))


def logo(width, height, logo_width, background):
    scale = logo_width / 591.025
    x, y = (width - logo_width) / 2, (height - 130 * scale) / 2
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">'
            f'{background}<svg x="{x:.3f}" y="{y:.3f}" width="{logo_width}" height="{130 * scale:.3f}" '
            f'viewBox="0 0 591.025 130">{LOGO_INNER}</svg></svg>')


GRAPHICS = {
    "icon-512": (512, 512, True,
                 '<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 108 108">'
                 '<rect width="108" height="108" fill="#1CECAB"/>'
                 f'<g transform="translate(38.83 30) scale(0.324324)">{MARK}</g></svg>'),
    "tv-banner-1280x720": (1280, 720, False, logo(1280, 720, 960, '<rect width="1280" height="720" fill="#000"/>')),
    "feature-1024x500": (1024, 500, False, logo(1024, 500, 560,
        '<defs><radialGradient id="g" cx="50%" cy="50%" r="60%"><stop offset="0" stop-color="#21322D"/>'
        '<stop offset="1" stop-color="#15191E"/></radialGradient></defs><rect width="1024" height="500" fill="url(#g)"/>')),
}


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as profile:
        for name, (width, height, alpha, svg) in GRAPHICS.items():
            source = OUT / f"{name}.svg"
            source.write_text(svg, encoding="utf-8")
            png = OUT / f"{name}.png"
            subprocess.run([str(CHROME), "--headless=new", "--disable-gpu", "--hide-scrollbars", "--no-first-run",
                            f"--user-data-dir={profile}", f"--window-size={width},{height}", f"--screenshot={png}",
                            source.as_uri()], check=True, capture_output=True, timeout=60)
            image = Image.open(png)
            assert image.size == (width, height), (name, image.size)
            image.convert("RGBA" if alpha else "RGB").save(png, optimize=True)


if __name__ == "__main__":
    main()
