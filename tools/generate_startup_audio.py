"""Original procedural launch cue; no samples, melodies or audio from another brand.

Run from any directory. Deterministic 48 kHz mono PCM, 1.12 s, limited to -12 dBFS.
The complete synthesis recipe is part of this project; standard library only.
"""
from pathlib import Path
import math
import struct
import wave

RATE = 48000
DURATION = 1.12
notes = [(0.025, 164.8138, .7), (.105, 246.9417, .5), (.205, 329.6276, .42)]
samples = []
for index in range(round(RATE * DURATION)):
    t = index / RATE
    value = 0.0
    for onset, frequency, gain in notes:
        u = t - onset
        if u >= 0:
            envelope = (1 - math.exp(-u / .018)) * math.exp(-u / .20)
            tone = sum(weight * math.sin(2 * math.pi * frequency * multiple * u)
                       for multiple, weight in [(1, 1), (2, .18), (3, .07)])
            value += gain * envelope * tone
    # Smooth tail prevents a click at the end of the PCM file.
    value *= min(1, max(0, (DURATION - t) / .16)) ** 2
    samples.append(value)
gain = (10 ** (-12 / 20)) / max(abs(x) for x in samples)
audio = b''.join(struct.pack('<h', round(x * gain * 32767)) for x in samples)
target = Path(__file__).resolve().parents[1] / 'apps/android-tv/app/src/main/res/raw/startup.wav'
target.parent.mkdir(parents=True, exist_ok=True)
with wave.open(str(target), 'wb') as wav:
    wav.setnchannels(1)
    wav.setsampwidth(2)
    wav.setframerate(RATE)
    wav.writeframes(audio)
print(f'Original cue: {DURATION:.2f}s, peak -12 dBFS, {len(audio)} PCM bytes')
