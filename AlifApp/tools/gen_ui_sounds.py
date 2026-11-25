#!/usr/bin/env python3
"""Генерирует короткие звуки интерфейса (не речь): правильно / неправильно / урок завершён."""
import math, struct, wave
from pathlib import Path

OUT = Path(__file__).resolve().parent.parent / "app/src/main/assets/audio/ui"
RATE = 22050

def tone(notes, path):
    frames = []
    for freq, dur in notes:
        n = int(RATE * dur)
        for i in range(n):
            t = i / RATE
            env = min(1.0, i / (RATE * 0.01)) * math.exp(-3.5 * t / dur)
            v = env * (0.6 * math.sin(2 * math.pi * freq * t) + 0.2 * math.sin(4 * math.pi * freq * t))
            frames.append(int(v * 0.5 * 32767))
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", f) for f in frames))

OUT.mkdir(parents=True, exist_ok=True)
tone([(784, 0.09), (1175, 0.22)], OUT / "correct.wav")
tone([(330, 0.12), (247, 0.28)], OUT / "wrong.wav")
tone([(523, 0.12), (659, 0.12), (784, 0.12), (1047, 0.4)], OUT / "complete.wav")
print("ok")
