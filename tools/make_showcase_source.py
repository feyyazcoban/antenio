"""Builds a synthetic showcase library for the store screenshots: an M3U list, an XMLTV guide and painted artwork.

The channels, films and series are invented, and the posters are drawn here (no photographs, no real titles), so the
store screenshots show a full library without content we have no right to (PLAY_STORE_READINESS §3.2). The streams
behind the entries are the public test streams and Blender Foundation open films of the review source, each with its
own #fragment (never sent to the server) because the app keeps one entry per address; nothing here is published. The list is served to the emulator from the output folder:

    python tools/make_showcase_source.py OUT_DIR BASE_URL
    (e.g. OUT_DIR=…/scratchpad/showcase, BASE_URL=http://10.0.2.2:8766)
"""
import math
import random
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path
from xml.sax.saxutils import escape

from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))
from make_review_source import font, wrap  # noqa: E402

LIVE_STREAMS = [
    "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
    "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
    "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
    "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
]
FILMS = [
    "https://download.blender.org/ED/elephantsdream-720-h264-st-aac.mov",
    "https://download.blender.org/demo/movies/ToS/tears_of_steel_720p.mov",
    "https://download.blender.org/durian/movies/Sintel.2010.1080p.mkv",
]
TRAILERS = [
    "https://download.blender.org/peach/trailer/trailer_720p.mov",
    "https://download.blender.org/durian/trailer/sintel_trailer-480p.mp4",
]

# name, group, colours, programme titles
CHANNELS = [
    ("Aurora News", "News", ((14, 40, 72), (36, 110, 170)), ["Morning Briefing", "World Today", "Market Watch", "Evening Edition", "Night Desk"]),
    ("Metro 24", "News", ((70, 20, 30), (190, 60, 70)), ["City Live", "Traffic & Weather", "Metro Tonight", "Talk of the Town"]),
    ("Terra Docs", "Documentary", ((18, 60, 40), (80, 160, 90)), ["Wild Coasts", "Rivers of Ice", "The Hidden Forest", "Planet Builders"]),
    ("Ocean Life", "Documentary", ((8, 50, 80), (40, 150, 190)), ["Reef Stories", "Deep Blue", "Whale Roads", "Tides"]),
    ("Nova Movies", "Movies", ((40, 20, 70), (140, 70, 190)), ["Saltwater Summer", "Glasshouse Nights", "Lanterns of Vela", "Midnight Ferry"]),
    ("Retro Classics", "Movies", ((70, 50, 20), (200, 150, 70)), ["Golden Hour", "The Long Drive", "Starlit Avenue", "Silver Screen Stories"]),
    ("Arena Sports", "Sports", ((20, 60, 30), (90, 190, 80)), ["Matchday Live", "Goal Review", "Track & Field", "Sports Tonight"]),
    ("Pulse Music", "Music", ((80, 20, 70), (230, 80, 160)), ["Top 20", "Acoustic Sessions", "Live on Stage", "Night Mix"]),
    ("Orbit Kids", "Kids", ((90, 60, 10), (250, 180, 40)), ["Little Explorers", "Robot Friends", "Story Time", "Space Pals"]),
    ("Kitchen Club", "Lifestyle", ((80, 40, 20), (220, 120, 60)), ["Fresh Start", "Street Food Atlas", "Bake Along", "Supper Club"]),
    ("Horizon Travel", "Lifestyle", ((20, 50, 80), (230, 150, 80)), ["Weekend Escapes", "Rail Journeys", "Island Hopping", "City Walks"]),
    ("Studio Comedy", "Entertainment", ((60, 60, 20), (210, 200, 60)), ["Stand-Up Night", "The Late Show", "Sketch Club", "Panel Games"]),
]

# title, year, genre, scene, palette (sky top, sky bottom, accent)
MOVIES = [
    ("Lanterns of Vela", 2025, "Adventure", "mountains", ((12, 18, 48), (240, 140, 90), (255, 214, 140))),
    ("Saltwater Summer", 2024, "Drama", "ocean", ((30, 70, 120), (250, 190, 150), (255, 240, 200))),
    ("Glasshouse Nights", 2025, "Thriller", "city", ((8, 10, 30), (60, 40, 110), (255, 200, 90))),
    ("The Copper Desert", 2023, "Western", "desert", ((90, 40, 30), (240, 170, 90), (255, 230, 170))),
    ("Orbit of Ash", 2026, "Sci-Fi", "space", ((5, 5, 20), (30, 20, 70), (120, 200, 255))),
    ("Midnight Ferry", 2024, "Mystery", "ocean", ((10, 15, 40), (40, 70, 120), (220, 230, 255))),
    ("Northern Quiet", 2025, "Drama", "forest", ((20, 40, 60), (150, 190, 200), (240, 250, 255))),
    ("Paper Kites", 2023, "Family", "abstract", ((250, 200, 120), (240, 120, 120), (255, 255, 230))),
    ("Starlit Avenue", 2024, "Romance", "city", ((40, 20, 60), (220, 110, 120), (255, 220, 180))),
    ("The Ninth Harbor", 2026, "Action", "ocean", ((60, 20, 20), (230, 110, 60), (255, 200, 120))),
    ("Ember Valley", 2025, "Fantasy", "mountains", ((40, 10, 30), (230, 90, 60), (255, 190, 90))),
    ("Signal Lost", 2024, "Sci-Fi", "space", ((0, 10, 20), (10, 60, 80), (120, 255, 220))),
]
SERIES = [
    ("Harbor Lights", "Drama", "ocean", ((15, 30, 60), (90, 150, 200), (255, 230, 160)), 6),
    ("The Quiet Valley", "Mystery", "forest", ((10, 25, 20), (90, 140, 110), (230, 250, 230)), 8),
    ("Night Market", "Comedy", "city", ((30, 10, 40), (200, 70, 120), (255, 210, 120)), 10),
    ("Far Meridian", "Sci-Fi", "space", ((5, 5, 25), (40, 30, 90), (255, 160, 220)), 6),
]

W, H, S = 600, 900, 2  # poster size and supersampling


def lerp(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def sky(draw, w, h, top, bottom, horizon):
    for y in range(h):
        draw.line([(0, y), (w, y)], fill=lerp(top, bottom, min(1, y / horizon)))


def glow(img, centre, radius, colour, strength=0.9):
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    for k in range(12, 0, -1):
        r = radius * (1 + k * 0.35)
        d.ellipse([centre[0] - r, centre[1] - r, centre[0] + r, centre[1] + r], fill=colour + (int(18 * strength),))
    layer = layer.filter(ImageFilter.GaussianBlur(radius * 0.4))
    img.alpha_composite(layer)
    ImageDraw.Draw(img).ellipse([centre[0] - radius, centre[1] - radius, centre[0] + radius, centre[1] + radius],
                                fill=colour + (255,))


def ridge(rng, w, base, amp, step):
    pts, y = [], base
    for x in range(0, w + step, step):
        y = base + amp * (math.sin(x / (w / (2 + rng.random() * 2)) + rng.random()) * 0.6 + (rng.random() - 0.5) * 0.8)
        pts.append((x, y))
    return pts + [(w, 10 ** 5), (0, 10 ** 5)]


def paint(scene, palette, seed, w, h):
    """A landscape for [scene] in [palette], RGBA, size w×h."""
    rng = random.Random(seed)
    top, bottom, accent = palette
    img = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(img)
    horizon = int(h * 0.62)
    sky(d, w, h, top, bottom, horizon)
    if scene in ("space", "city", "ocean", "forest") or rng.random() < 0.3:
        for _ in range(int(w * h / 9000)):
            x, y, r = rng.random() * w, rng.random() * h * 0.6, rng.random() * 2.2 * S / 2 + 0.6
            d.ellipse([x - r, y - r, x + r, y + r], fill=(255, 255, 255, rng.randint(90, 230)))
    if scene == "space":
        cx, cy, r = w * (0.35 + rng.random() * 0.3), h * 0.42, w * 0.28
        glow(img, (cx, cy), r, accent, 0.6)
        d = ImageDraw.Draw(img)
        # Lit from the upper left: discs shrink towards the highlight, from the shadowed rim to the bright spot.
        lx, ly = cx - r * 0.35, cy - r * 0.35
        for k in range(int(r), 0, -1):
            t = k / r
            ox, oy = lx + (cx - lx) * t, ly + (cy - ly) * t
            d.ellipse([ox - k, oy - k, ox + k, oy + k], fill=lerp(lerp(accent, (255, 255, 255), 0.35), lerp(top, accent, 0.25), t) + (255,))
        d.ellipse([cx - r * 1.9, cy - r * 0.32, cx + r * 1.9, cy + r * 0.32], outline=accent + (200,), width=max(2, int(w / 150)))
        d.ellipse([cx - r * 1.7, cy - r * 0.26, cx + r * 1.7, cy + r * 0.26], outline=accent + (120,), width=max(1, int(w / 300)))
    else:
        glow(img, (w * (0.3 + rng.random() * 0.4), horizon - h * (0.08 + rng.random() * 0.12)), w * 0.11, accent)
        d = ImageDraw.Draw(img)
    if scene in ("mountains", "forest", "desert"):
        layers = 4
        for i in range(layers):
            t = (i + 1) / (layers + 1)
            colour = lerp(lerp(bottom, top, 0.5), (8, 10, 18), t)
            pts = ridge(rng, w, horizon - h * 0.12 + i * h * 0.07, h * (0.09 if scene != "desert" else 0.04), max(4, w // 40))
            d.polygon(pts, fill=colour + (255,))
            if scene == "forest":
                for _ in range(26 + i * 10):
                    x = rng.random() * w
                    base = horizon - h * 0.1 + i * h * 0.075 + rng.random() * h * 0.05
                    th = h * (0.05 + i * 0.03) * (0.7 + rng.random() * 0.6)
                    d.polygon([(x, base - th), (x - th * 0.3, base), (x + th * 0.3, base)], fill=colour + (255,))
    elif scene == "ocean":
        d.rectangle([0, horizon, w, h], fill=lerp(top, (5, 10, 25), 0.5) + (255,))
        for k in range(60):
            y = horizon + (h - horizon) * (k / 60) ** 1.4
            half = w * (0.05 + 0.3 * k / 60) * (0.6 + rng.random() * 0.8)
            cx = w * 0.5 + (rng.random() - 0.5) * w * 0.1
            d.line([(cx - half, y), (cx + half, y)], fill=accent + (int(160 * (1 - k / 60)),), width=max(1, int(S * 1.5)))
    elif scene == "city":
        x = 0
        while x < w:
            bw = w * (0.05 + rng.random() * 0.08)
            bh = h * (0.12 + rng.random() * 0.3)
            shade = lerp((10, 12, 24), top, 0.25)
            d.rectangle([x, horizon + h * 0.12 - bh, x + bw, h], fill=shade + (255,))
            for wy in range(int(horizon + h * 0.12 - bh + 8), h, max(6, int(h / 70))):
                for wx in range(int(x + 4), int(x + bw - 4), max(6, int(w / 60))):
                    if rng.random() < 0.35:
                        d.rectangle([wx, wy, wx + w / 160, wy + h / 220], fill=accent + (rng.randint(140, 255),))
            x += bw + w * 0.005
        d.rectangle([0, horizon + h * 0.12, w, h], fill=(6, 8, 16, 255))
    elif scene == "abstract":
        for _ in range(14):
            r = w * (0.08 + rng.random() * 0.25)
            cx, cy = rng.random() * w, rng.random() * h * 0.8
            colour = lerp(accent, bottom, rng.random())
            layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
            ImageDraw.Draw(layer).ellipse([cx - r, cy - r, cx + r, cy + r], fill=colour + (110,))
            img.alpha_composite(layer.filter(ImageFilter.GaussianBlur(r * 0.08)))
        d = ImageDraw.Draw(img)
    return img


def finish(img, w, h):
    """Bottom shade for the title, a soft vignette and fine grain."""
    shade = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    sd = ImageDraw.Draw(shade)
    for y in range(int(h * 0.5), h):
        sd.line([(0, y), (w, y)], fill=(5, 7, 12, int(235 * ((y - h * 0.5) / (h * 0.5)) ** 1.3)))
    img.alpha_composite(shade)
    noise = Image.effect_noise((w, h), 18).convert("L").point(lambda v: 128 + (v - 128) // 3)
    img = Image.blend(img.convert("RGB"), Image.merge("RGB", (noise, noise, noise)), 0.05).convert("RGBA")
    return img


def poster(path, title, line, scene, palette, seed):
    big = finish(paint(scene, palette, seed, W * S, H * S), W * S, H * S)
    d = ImageDraw.Draw(big)
    face = font(74 * S)
    lines = wrap(d, title, face, (W - 90) * S)
    y = H * S - 120 * S - len(lines) * 82 * S
    d.text((48 * S, y - 46 * S), line.upper(), font=font(22 * S, "SemiBold"), fill=palette[2] + (255,))
    for text in lines:
        d.text((46 * S, y), text, font=face, fill=(246, 246, 242, 255))
        y += 82 * S
    big.convert("RGB").resize((W, H), Image.LANCZOS).save(path, optimize=True)


def logo(path, name, colours):
    size = 400 * S
    img = Image.new("RGBA", (size, size))
    d = ImageDraw.Draw(img)
    for y in range(size):
        d.line([(0, y), (size, y)], fill=lerp(colours[0], colours[1], y / size) + (255,))
    words = name.split()
    mark = font(150 * S)
    initials = "".join(w[0] for w in words)[:2].upper()
    box = d.textbbox((0, 0), initials, font=mark)
    d.text(((size - (box[2] - box[0])) / 2 - box[0], size * 0.18 - box[1]), initials, font=mark, fill=(255, 255, 255, 255))
    label = font(44 * S, "SemiBold")
    box = d.textbbox((0, 0), name.upper(), font=label)
    d.text(((size - (box[2] - box[0])) / 2 - box[0], size * 0.72), name.upper(), font=label, fill=(255, 255, 255, 220))
    img.convert("RGB").resize((400, 400), Image.LANCZOS).save(path, optimize=True)


def main(out, base):
    out = Path(out)
    (out / "art").mkdir(parents=True, exist_ok=True)
    lines = [f'#EXTM3U url-tvg="{base}/epg.xml"']
    for i, (name, group, colours, _) in enumerate(CHANNELS):
        key = f"ch{i + 1}"
        logo(out / "art" / f"{key}.png", name, colours)
        lines += [f'#EXTINF:-1 tvg-id="showcase.{key}" tvg-logo="{base}/art/{key}.png" group-title="{group}",{name}',
                  f"{LIVE_STREAMS[i % len(LIVE_STREAMS)]}#{key}"]
    for i, (title, year, genre, scene, palette) in enumerate(MOVIES):
        key = f"m{i + 1}"
        poster(out / "art" / f"{key}.png", title, f"{genre} · {year}", scene, palette, 100 + i)
        lines += [f'#EXTINF:-1 tvg-logo="{base}/art/{key}.png" group-title="{genre}",{title} ({year})',
                  f"{FILMS[i % len(FILMS)]}#{key}"]
    for i, (title, genre, scene, palette, count) in enumerate(SERIES):
        key = f"s{i + 1}"
        poster(out / "art" / f"{key}.png", title, f"{genre} · Series", scene, palette, 200 + i)
        for e in range(1, count + 1):
            lines += [f'#EXTINF:-1 tvg-logo="{base}/art/{key}.png" group-title="{genre} Series",{title} S01E{e:02d}',
                      f"{TRAILERS[(i + e) % len(TRAILERS)]}#{key}e{e}"]
    (out / "showcase.m3u").write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")
    rng = random.Random(7)
    start = datetime.now(timezone.utc).replace(minute=0, second=0, microsecond=0) - timedelta(hours=6)
    stamp = lambda t: t.strftime("%Y%m%d%H%M%S +0000")
    xml = ['<?xml version="1.0" encoding="UTF-8"?>', '<tv>']
    for i, (name, *_rest) in enumerate(CHANNELS):
        xml.append(f'  <channel id="showcase.ch{i + 1}"><display-name>{escape(name)}</display-name></channel>')
    for i, (_, _, _, titles) in enumerate(CHANNELS):
        t, k = start, rng.randrange(len(titles))
        while t < start + timedelta(days=3):
            length = timedelta(minutes=rng.choice([30, 30, 60, 60, 90]))
            xml.append(f'  <programme start="{stamp(t)}" stop="{stamp(t + length)}" channel="showcase.ch{i + 1}">'
                       f'<title>{escape(titles[k % len(titles)])}</title></programme>')
            t, k = t + length, k + 1
    xml.append("</tv>")
    (out / "epg.xml").write_text("\n".join(xml) + "\n", encoding="utf-8", newline="\n")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2].rstrip("/"))
