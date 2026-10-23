"""Builds the Play review source under apps/web/review: an M3U list, an XMLTV guide and the artwork they point to.

Only open-licensed films (Blender Foundation, CC BY) and public test streams, so Google's reviewer and the store
screenshots never show content we have no right to (PLAY_STORE_READINESS §2.2, §3.2). The posters and logos are our
own drawings, not the films' artwork. The app never offers this list; it is named only in Play Console's App access.

    python tools/make_review_source.py
"""
from datetime import datetime, timedelta, timezone
from pathlib import Path
from xml.sax.saxutils import escape

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "apps" / "web" / "review"
FONT = ROOT / "apps" / "android-tv" / "app" / "src" / "main" / "res" / "font" / "roboto.ttf"
BASE = "https://antenio.app/review"

LIVE = [
    ("live", "Live Test Stream", "Test Channels", "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
     ["Live Test Stream", "Live Test Signal"], "Akamai public HLS test stream.", ((22, 64, 96), (18, 140, 150))),
    ("bipbop", "Bip Bop Test", "Test Channels",
     "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
     ["Bip Bop Test Pattern", "Bip Bop Audio Sync"], "Apple HLS example stream.", ((70, 40, 110), (160, 70, 140))),
    ("bbb", "Big Buck Bunny Channel", "Open Movies", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
     ["Big Buck Bunny (2008)", "Big Buck Bunny: Encore"], "Blender Foundation open movie, CC BY 3.0. Mux test stream.",
     ((40, 90, 40), (150, 170, 60))),
    ("tos", "Tears of Steel Channel", "Open Movies",
     "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
     ["Tears of Steel (2012)", "Tears of Steel: Encore"], "Blender Foundation open movie, CC BY 3.0. Unified Streaming demo.",
     ((60, 60, 70), (120, 130, 150))),
]
MOVIES = [
    ("elephants-dream", "Elephants Dream (2006)", "https://download.blender.org/ED/elephantsdream-720-h264-st-aac.mov",
     "CC BY 2.5", ((30, 50, 80), (200, 120, 60))),
    ("sintel", "Sintel (2010)", "https://download.blender.org/durian/movies/Sintel.2010.1080p.mkv",
     "CC BY 3.0", ((80, 30, 30), (220, 160, 90))),
    ("tears-of-steel", "Tears of Steel (2012)", "https://download.blender.org/demo/movies/ToS/tears_of_steel_720p.mov",
     "CC BY 3.0", ((20, 40, 50), (90, 170, 190))),
]
SHOW = ("open-movie-trailers", "Blender Open Movie Trailers", ((35, 45, 60), (28, 236, 171)))
EPISODES = [
    ("S01E01 Big Buck Bunny", "https://download.blender.org/peach/trailer/trailer_720p.mov"),
    ("S01E02 Sintel", "https://download.blender.org/durian/trailer/sintel_trailer-480p.mp4"),
]


def font(size, weight="Bold"):
    f = ImageFont.truetype(str(FONT), size)
    f.set_variation_by_name(weight)
    return f


def gradient(size, top, bottom):
    w, h = size
    image = Image.new("RGB", size)
    draw = ImageDraw.Draw(image)
    for y in range(h):
        t = y / max(1, h - 1)
        draw.line([(0, y), (w, y)], fill=tuple(round(a + (b - a) * t) for a, b in zip(top, bottom)))
    return image


def wrap(draw, text, face, width):
    lines, line = [], ""
    for word in text.split():
        trial = f"{line} {word}".strip()
        if draw.textlength(trial, font=face) <= width or not line:
            line = trial
        else:
            lines.append(line); line = word
    return lines + [line]


def poster(name, title, note, colors, centred=False):
    """A 2:3 poster: the title large over a two-colour gradient, the licence small under it. A [centred] one keeps
    both inside the middle 16:9 band, as episode cards crop the series artwork to it."""
    image = gradient((600, 900), *colors)
    draw = ImageDraw.Draw(image)
    face = font(76)
    lines = wrap(draw, title, face, 500)
    y = (450 - (len(lines) * 86 + 60) // 2) if centred else 900 - 170 - len(lines) * 86
    for line in lines:
        draw.text((50, y), line, font=face, fill=(240, 243, 241)); y += 86
    draw.text((50, y + 24 if centred else 900 - 110), note, font=font(30, "Medium"), fill=(240, 243, 241))
    draw.rectangle([50, 60, 130, 68], fill=(168, 223, 203))
    image.save(OUT / "art" / f"{name}.png", optimize=True)


def logo(name, label, colors):
    """A square channel logo: initials of the channel name on its colour."""
    image = gradient((400, 400), *colors)
    draw = ImageDraw.Draw(image)
    initials = "".join(word[0] for word in label.split() if word[0].isalnum())[:3].upper()
    face = font(150)
    box = draw.textbbox((0, 0), initials, font=face)
    draw.text(((400 - box[2] - box[0]) / 2, (400 - box[3] - box[1]) / 2), initials, font=face, fill=(240, 243, 241))
    image.save(OUT / "art" / f"{name}.png", optimize=True)


def m3u():
    lines = [f'#EXTM3U url-tvg="{BASE}/epg.xml"',
             "# Antenio review source: open-licensed films and public test streams only.",
             "# Elephants Dream, Big Buck Bunny, Sintel and Tears of Steel are (c) Blender Foundation, "
             "licensed CC BY (https://www.blender.org/about/projects/).",
             "# Test streams: Akamai, Apple, Mux and Unified Streaming public HLS examples."]
    for key, name, group, url, *_ in LIVE:
        lines += [f'#EXTINF:-1 tvg-id="antenio.demo.{key}" tvg-logo="{BASE}/art/{key}.png" group-title="{group}",{name}', url]
    for key, title, url, *_ in MOVIES:
        lines += [f'#EXTINF:-1 tvg-logo="{BASE}/art/{key}.png" group-title="Open Movies",{title}', url]
    for episode, url in EPISODES:
        lines += [f'#EXTINF:-1 tvg-logo="{BASE}/art/{SHOW[0]}.png" group-title="Open Movie Trailers",{SHOW[1]} {episode}', url]
    (OUT / "demo.m3u").write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")


def epg(start, days):
    """Hour-long programmes for each live channel, alternating its two titles, from [start] for [days] days."""
    stamp = lambda t: t.strftime("%Y%m%d%H%M%S +0000")
    out = ['<?xml version="1.0" encoding="UTF-8"?>', '<tv generator-info-name="Antenio review source">']
    for key, name, *_ in LIVE:
        out.append(f'  <channel id="antenio.demo.{key}"><display-name>{escape(name)}</display-name></channel>')
    for key, _, _, _, titles, desc, _ in LIVE:
        for hour in range(days * 24):
            begin = start + timedelta(hours=hour)
            out.append(f'  <programme start="{stamp(begin)}" stop="{stamp(begin + timedelta(hours=1))}" '
                       f'channel="antenio.demo.{key}"><title>{escape(titles[hour % 2])}</title>'
                       f'<desc>{escape(desc)}</desc></programme>')
    out.append("</tv>")
    (OUT / "epg.xml").write_text("\n".join(out) + "\n", encoding="utf-8", newline="\n")


def main():
    (OUT / "art").mkdir(parents=True, exist_ok=True)
    for key, name, _, _, _, _, colors in LIVE:
        logo(key, name, colors)
    for key, title, _, licence, colors in MOVIES:
        poster(key, title, f"Blender Foundation · {licence}", colors)
    poster(SHOW[0], SHOW[1], "Blender Foundation · CC BY", SHOW[2], centred=True)
    m3u()
    today = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)
    epg(today - timedelta(days=1), 75)


if __name__ == "__main__":
    main()
