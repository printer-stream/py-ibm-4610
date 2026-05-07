"""
    Supported frequency range by the IBM4610 1NR:
    - 261 Hz (lowest)
    - 3958 Hz (highest)
"""

import time

from .printer.ibm4610 import IBM4610


# ---------------------------------------------------------------------------
# Note frequencies (Hz)
# ---------------------------------------------------------------------------
E4  = 330;  F4  = 349;  Fs4 = 370;  G4  = 392
Ab4 = 415;  A4  = 440;  Bb4 = 466;  B4  = 494
C5  = 523;  Cs5 = 554;  D5  = 587;  Eb5 = 622
E5  = 659;  F5  = 698;  Fs5 = 740;  G5  = 784
Ab5 = 831;  A5  = 880; Bb5 = 932;  B5  = 988

# ---------------------------------------------------------------------------
# Duration units (each = 100 ms), tempo ~185 BPM
#   s  = sixteenth  = 1
#   e  = eighth     = 2
#   q  = quarter    = 3
#   dq = dotted qtr = 4
#   h  = half       = 6
# ---------------------------------------------------------------------------
s, e, q, dq, h = 1, 2, 3, 4, 6

REST = 0  # REST: host-side sleep — no beep command is sent, giving true silence.

MARIO_MELODY = [
    # -- A phrase --
    (REST, q),
    (C5, q), (REST, e), (G4, e), (REST, q),
    (E4, q), (REST, q),
    (A4, q), (REST, q), (B4, q), (REST, s), (Bb4, e), (A4, q),

    # -- B phrase --
    (G4, e), (E5, e), (G5, e), (A5, q), (REST, s), (F5, e), (G5, e),
    (REST, s), (E5, q), (REST, s), (C5, e), (D5, e), (B4, h),
]


def play_melody(p: IBM4610, melody: list, volume: int = 80) -> None:
    """Play melody with absolute-deadline scheduling to prevent timing drift.

    Each note/rest is assigned an absolute start time.  The host sleeps until
    that deadline rather than sleeping a relative duration — any USB overhead
    or sleep overshoot is automatically absorbed into the next sleep, so drift
    never accumulates across the melody.
    """
    tick = 0.1
    deadline = time.monotonic()
    for freq, dur in melody:
        deadline += dur * tick
        if freq != REST:
            p.beep(duration_100ms=dur, frequency=freq, volume=volume)
            p.flush()
        remaining = deadline - time.monotonic()
        if remaining > 0:
            time.sleep(remaining)

