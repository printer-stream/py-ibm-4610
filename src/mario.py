#!/usr/bin/env python3
"""mario.py - Play the Super Mario Bros. ground theme on the IBM 4610 beeper.

Usage:
    sudo python3 mario.py        # Linux
    python3 mario.py             # macOS/Windows with libusb
"""

import sys
from ibm4610 import IBM4610

# ---------------------------------------------------------------------------
# Note frequencies (Hz)
# ---------------------------------------------------------------------------
E4  = 330;  F4  = 349;  Fs4 = 370;  G4  = 392
Ab4 = 415;  A4  = 440;  Bb4 = 466;  B4  = 494
C5  = 523;  Cs5 = 554;  D5  = 587;  Eb5 = 622
E5  = 659;  F5  = 698;  Fs5 = 740;  G5  = 784
Ab5 = 831;  A5  = 880

# ---------------------------------------------------------------------------
# Duration units (each = 100 ms), tempo ~185 BPM
#   s  = sixteenth  = 1
#   e  = eighth     = 2
#   q  = quarter    = 3
#   dq = dotted qtr = 4
#   h  = half       = 6
# ---------------------------------------------------------------------------
s, e, q, dq, h = 1, 2, 3, 4, 6

# ---------------------------------------------------------------------------
# Super Mario Bros. Ground Theme (main melody)
# Rests are omitted — notes play back-to-back in the printer's command buffer.
# ---------------------------------------------------------------------------
MELODY = [
    # -- Intro --
    (E5, e), (E5, e), (E5, e), (C5, e), (E5, q), (G5, q), (G4, q),

    # -- A phrase --
    (C5, q), (G4, e), (E4, q), (A4, q), (B4, q), (Bb4, e), (A4, q),

    # -- B phrase --
    (G4, e), (E5, e), (G5, e), (A5, q), (F5, e), (G5, e),
    (E5, q), (C5, e), (D5, e), (B4, h),

    # -- A phrase (repeat) --
    (C5, q), (G4, e), (E4, q), (A4, q), (B4, q), (Bb4, e), (A4, q),

    # -- B phrase (repeat) --
    (G4, e), (E5, e), (G5, e), (A5, q), (F5, e), (G5, e),
    (E5, q), (C5, e), (D5, e), (B4, h),

    # -- C phrase (underground feel) --
    (G5, e), (Fs5, e), (F5, e), (Eb5, q), (E5, e),
    (Ab4, e), (A4, e), (C5, e), (A4, e), (C5, e), (D5, e),

    # -- D phrase --
    (G5, e), (Fs5, e), (F5, e), (Eb5, q), (E5, e),
    (C5, e), (C5, e), (C5, h),

    # -- C phrase (repeat) --
    (G5, e), (Fs5, e), (F5, e), (Eb5, q), (E5, e),
    (Ab4, e), (A4, e), (C5, e), (A4, e), (C5, e), (D5, e),

    # -- Ending run --
    (Eb5, q), (D5, q), (C5, h),
]


def play(p: IBM4610, melody: list, volume: int = 80) -> None:
    """Buffer all notes then flush once — printer plays them sequentially."""
    for freq, dur in melody:
        p.beep(duration_100ms=dur, frequency=freq, volume=volume)
    p.flush()


def main() -> None:
    print("IBM 4610 SureMark - Super Mario Bros. theme")
    print("Connecting (VID=0x04B3, PID=0x4535) ...")
    try:
        with IBM4610() as p:
            print(f"Printer found. Queuing {len(MELODY)} notes ...")
            play(p, MELODY)
            print("Done - enjoy the music!")
    except RuntimeError as exc:
        print(f"\nERROR: {exc}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
