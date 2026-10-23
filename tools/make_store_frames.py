"""Renders the captioned store images (design/store/frames) and the feature graphic from the TV screenshots.

Each frame is 1920×1080: the brand background, the logo, a headline and a line in English or Turkish on the left, and
an unmodified TV screenshot (design/store/screenshots) in a TV-like bezel on the right. Play's phone section takes
these; its Android TV section keeps the bare screenshots. The feature graphic sets the logo and a line beside a fan
of showcase posters (tools/make_showcase_source.py). Chrome draws the pages headless, as in make_store_graphics.

    python tools/make_store_frames.py SHOWCASE_ART_DIR
"""
import re
import subprocess
import sys
import tempfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
STORE = ROOT / "design" / "store"
SHOTS = STORE / "screenshots"
OUT = STORE / "frames"
CHROME = Path(r"C:\Program Files\Google\Chrome\Application\chrome.exe")
FONT = (ROOT / "apps/android-tv/app/src/main/res/font/roboto.ttf").as_uri()
LOGO = (ROOT / "design" / "brand" / "antenio-yatay.svg").read_text(encoding="utf-8")
LOGO = re.sub(r'width="[^"]+" height="[^"]+"', 'width="100%" height="100%"', LOGO, count=1)

FRAMES = [
    ("01-home", {"en": ("Your TV.<br>Your library.", "Live channels, movies and series from your own source, in one place."),
                 "tr": ("Televizyonun.<br>Senin kütüphanen.", "Kendi kaynağındaki canlı kanallar, filmler ve diziler tek yerde.")}),
    ("02-guide", {"en": ("A real<br>TV guide", "XMLTV guide, channel numbers and CH+/CH− on your remote."),
                  "tr": ("Gerçek bir<br>TV rehberi", "XMLTV rehberi, kanal numaraları ve kumandada CH+/CH−.")}),
    ("03-movies", {"en": ("Browse like a<br>streaming app", "Posters, categories, sorting and search across your library."),
                   "tr": ("Dijital platform<br>gibi gez", "Afişler, kategoriler, sıralama ve kütüphanende arama.")}),
    ("07-phone-setup", {"en": ("Set up from<br>your phone", "Scan the QR code and paste your link. No typing with the remote."),
                        "tr": ("Telefonundan<br>kur", "QR kodu okut, bağlantını yapıştır. Kumandayla yazmaya gerek yok.")}),
    ("06-player", {"en": ("Made for<br>the remote", "Quick seeking, audio and subtitle tracks, favorites."),
                   "tr": ("Kumanda için<br>tasarlandı", "Hızlı sarma, ses ve altyazı seçenekleri, favoriler.")}),
    ("04-live-channels", {"en": ("What's on,<br>at a glance", "Every channel with the current and next programme."),
                          "tr": ("Her kanalda<br>şimdi ne var", "Şu anki ve sıradaki program bir bakışta.")}),
]

STYLE = f"""
@font-face {{ font-family: R; src: url('{FONT}'); font-weight: 100 900; }}
* {{ margin: 0; box-sizing: border-box; }}
html, body {{ width: 1920px; height: 1080px; overflow: hidden; }}
body {{ font-family: R, sans-serif; color: #F0F3F1; background:
  radial-gradient(1100px 760px at 78% 52%, rgba(28,236,171,.20), transparent 70%),
  radial-gradient(900px 700px at 8% 100%, rgba(80,120,255,.12), transparent 70%),
  linear-gradient(160deg, #1A2026 0%, #101418 100%); }}
.logo {{ position: absolute; left: 110px; top: 96px; width: 300px; height: 66px; }}
.copy {{ position: absolute; left: 110px; top: 300px; width: 560px; }}
h1 {{ font-weight: 700; font-size: 84px; line-height: 1.04; letter-spacing: -1.5px; }}
p {{ margin-top: 34px; font-size: 32px; line-height: 1.38; color: #B7C1C7; font-weight: 400; }}
.bar {{ width: 88px; height: 8px; border-radius: 4px; background: #1CECAB; margin-bottom: 40px; }}
.tv {{ position: absolute; right: 90px; top: 190px; width: 1100px; height: 619px; padding: 12px;
  background: #07090C; border-radius: 22px; box-shadow: 0 40px 90px rgba(0,0,0,.55), 0 0 0 2px rgba(255,255,255,.06); }}
.tv img {{ width: 100%; height: 100%; border-radius: 12px; display: block; }}
.stand {{ position: absolute; right: 540px; top: 822px; width: 200px; height: 14px; border-radius: 7px; background: #07090C; }}
"""


def render(html, width, height, png, alpha=False):
    with tempfile.TemporaryDirectory() as tmp:
        page = Path(tmp) / "page.html"
        page.write_text(html, encoding="utf-8")
        subprocess.run([str(CHROME), "--headless=new", "--disable-gpu", "--hide-scrollbars", "--no-first-run",
                        "--allow-file-access-from-files", f"--user-data-dir={tmp}/profile",
                        f"--window-size={width},{height}", f"--screenshot={png}", page.as_uri()],
                       check=True, capture_output=True, timeout=90)
    image = Image.open(png)
    assert image.size == (width, height), (png, image.size)
    image.convert("RGBA" if alpha else "RGB").save(png, optimize=True)


def frame(shot, headline, line, lang, png):
    html = (f'<!doctype html><html lang="{lang}"><head><meta charset="utf-8"><style>{STYLE}</style></head><body>'
            f'<div class="logo">{LOGO}</div><div class="copy"><div class="bar"></div><h1>{headline}</h1><p>{line}</p></div>'
            f'<div class="tv"><img src="{(SHOTS / (shot + ".png")).as_uri()}"></div><div class="stand"></div></body></html>')
    render(html, 1920, 1080, png)


def feature(art, png):
    posters = "".join(
        f'<img src="{(Path(art) / name).as_uri()}" style="left:{486 + i * 82}px; top:{70 + abs(2 - i) * 18}px; '
        f'transform: rotate({(i - 2) * 5}deg); z-index:{10 - abs(2 - i)}">'
        for i, name in enumerate(["m7.png", "m1.png", "m5.png", "s1.png", "m9.png"]))
    html = (f'<!doctype html><html><head><meta charset="utf-8"><style>@font-face {{ font-family: R; src: url("{FONT}"); font-weight: 100 900; }}'
            '* { margin: 0; } html, body { width: 1024px; height: 500px; overflow: hidden; }'
            'body { font-family: R, sans-serif; background: radial-gradient(700px 420px at 75% 50%, rgba(28,236,171,.22), transparent 70%),'
            ' linear-gradient(160deg, #1A2026, #0F1317); }'
            '.logo { position: absolute; left: 64px; top: 170px; width: 400px; height: 88px; }'
            '.line { position: absolute; left: 66px; top: 286px; width: 420px; color: #B7C1C7; font-size: 26px; line-height: 1.35; }'
            'img { position: absolute; width: 190px; height: 285px; border-radius: 14px; box-shadow: 0 18px 40px rgba(0,0,0,.6); }'
            f'</style></head><body><div class="logo">{LOGO}</div><div class="line">Your own source, made for the remote.</div>'
            f'{posters}</body></html>')
    render(html, 1024, 500, png)


def main(art):
    for lang in ("en", "tr"):
        (OUT / lang).mkdir(parents=True, exist_ok=True)
        for shot, copy in FRAMES:
            headline, line = copy[lang]
            frame(shot, headline, line, lang, OUT / lang / f"{shot}.png")
    feature(art, STORE / "feature-1024x500.png")


if __name__ == "__main__":
    main(sys.argv[1])
