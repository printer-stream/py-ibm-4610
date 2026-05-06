#!/usr/bin/env python3
"""mario.py - Play the Super Mario Bros. ground theme on the IBM 4610 beeper.

Usage:
    sudo python3 mario.py        # Linux
    python3 mario.py             # macOS/Windows with libusb
"""

import sys
import time
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

# REST: host-side sleep — no beep command is sent, giving true silence.
REST = 0

# ---------------------------------------------------------------------------
# Super Mario Bros. Ground Theme (main melody)
# Rests are omitted — notes play back-to-back in the printer's command buffer.
# ---------------------------------------------------------------------------
MELODY = [
    # -- Intro --
    (E5, e), (REST, s), (E5, e), (REST, s), (E5, e), (REST, e),
    (C5, e), (E5, q), (REST, s), (G5, q), (REST, q), (G4, q),

    # -- A phrase --
    (REST, q),
    (C5, q), (REST, e), (G4, e), (REST, q),
    (E4, q), (REST, q),
    (A4, q), (REST, q), (B4, q), (REST, s), (Bb4, e), (A4, q),

    # -- B phrase --
    (G4, e), (E5, e), (G5, e), (A5, q), (REST, s), (F5, e), (G5, e),
    (REST, s), (E5, q), (REST, s), (C5, e), (D5, e), (B4, h),

    # -- A phrase (repeat) --
    (REST, q),
    (C5, q), (REST, e), (G4, e), (REST, q),
    (E4, q), (REST, q),
    (A4, q), (REST, q), (B4, q), (REST, s), (Bb4, e), (A4, q),

    # -- B phrase (repeat) --
    (G4, e), (E5, e), (G5, e), (A5, q), (REST, s), (F5, e), (G5, e),
    (REST, s), (E5, q), (REST, s), (C5, e), (D5, e), (B4, h),

    # -- C phrase (underground feel) --
    (REST, s),
    (G5, e), (REST, s), (Fs5, e), (F5, e), (REST, s),
    (Eb5, q), (REST, s), (E5, e), (REST, s),
    (Ab4, e), (A4, e), (C5, e), (REST, s), (A4, e), (C5, e), (D5, e),

    # -- D phrase --
    (REST, s),
    (G5, e), (REST, s), (Fs5, e), (F5, e), (REST, s),
    (Eb5, q), (REST, s), (E5, e), (REST, s),
    (C5, e), (REST, s), (C5, e), (C5, h),

    # -- C phrase (repeat) --
    (REST, s),
    (G5, e), (REST, s), (Fs5, e), (F5, e), (REST, s),
    (Eb5, q), (REST, s), (E5, e), (REST, s),
    (Ab4, e), (A4, e), (C5, e), (REST, s), (A4, e), (C5, e), (D5, e),

    # -- Ending run --
    (REST, s), (Eb5, q), (REST, s), (D5, q), (REST, s), (C5, h),
]


def play(p: IBM4610, melody: list, volume: int = 80) -> None:
    """Play melody with absolute-deadline scheduling to prevent timing drift.

    Each note/rest is assigned an absolute start time.  The host sleeps until
    that deadline rather than sleeping a relative duration — any USB overhead
    or sleep overshoot is automatically absorbed into the next sleep, so drift
    never accumulates across the melody.
    """
    tick = 0.1   # seconds per duration unit (100 ms)
    deadline = time.monotonic()
    for freq, dur in melody:
        deadline += dur * tick
        if freq != REST:
            p.beep(duration_100ms=dur, frequency=freq, volume=volume)
            p.flush()
        remaining = deadline - time.monotonic()
        if remaining > 0:
            time.sleep(remaining)


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
