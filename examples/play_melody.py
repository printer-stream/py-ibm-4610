#!/usr/bin/env python3
"""play_melody.py — Play a melody on the IBM 4610 beeper.

Demonstrates:
  - playing a melody using the IBM 4610 beeper

Run with the printer connected over USB:

    python examples/play_melody.py
"""

import logging

from py_ibm_4610 import IBM4610_1NR, play_melody
from py_ibm_4610.beeper import MARIO_MELODY

logging.basicConfig(level=logging.WARNING)


with IBM4610_1NR() as p:
    play_melody(p, MARIO_MELODY)
