"""Tests for py_ibm_4610.beeper — note constants, MARIO_MELODY, play_melody.

All tests run offline (no USB hardware required).
The printer argument to play_melody() is replaced by a MagicMock so that
beep() and flush() calls can be inspected without touching real hardware.
time.monotonic() and time.sleep() are patched via the module reference so
timing behaviour can be exercised deterministically.
"""

from unittest.mock import MagicMock, patch

import pytest

from py_ibm_4610 import play_melody, MARIO_MELODY
from py_ibm_4610.beeper import (
    REST,
    s, e, q, dq, h,
    E4, F4, Fs4, G4, Ab4, A4, Bb4, B4,
    C5, Cs5, D5, Eb5, E5, F5, Fs5, G5, Ab5, A5, Bb5, B5,
)

# Hardware-supported frequency range for the IBM 4610 1NR.
_HW_MIN = 261
_HW_MAX = 3958

_ALL_NOTES = [
    E4, F4, Fs4, G4, Ab4, A4, Bb4, B4,
    C5, Cs5, D5, Eb5, E5, F5, Fs5, G5, Ab5, A5, Bb5, B5,
]


# ---------------------------------------------------------------------------
# Note constants
# ---------------------------------------------------------------------------

class TestNoteConstants:
    def test_all_notes_in_hw_range(self):
        for freq in _ALL_NOTES:
            assert _HW_MIN <= freq <= _HW_MAX, (
                f"{freq} Hz is outside the hardware range {_HW_MIN}–{_HW_MAX}"
            )

    def test_octave_4_ascending(self):
        octave4 = [E4, F4, Fs4, G4, Ab4, A4, Bb4, B4]
        assert octave4 == sorted(octave4)

    def test_octave_5_ascending(self):
        octave5 = [C5, Cs5, D5, Eb5, E5, F5, Fs5, G5, Ab5, A5, Bb5, B5]
        assert octave5 == sorted(octave5)

    def test_octave_boundary(self):
        # B4 must be lower than C5 (octave boundary)
        assert B4 < C5

    def test_a4_tuning_reference(self):
        assert A4 == 440

    def test_a5_is_one_octave_above_a4(self):
        # One octave up = double the frequency
        assert A5 == 880


# ---------------------------------------------------------------------------
# Duration / REST constants
# ---------------------------------------------------------------------------

class TestDurationConstants:
    def test_values(self):
        assert s == 1
        assert e == 2
        assert q == 3
        assert dq == 4
        assert h == 6

    def test_ordering(self):
        assert s < e < q < dq < h

    def test_rest_is_zero(self):
        assert REST == 0


# ---------------------------------------------------------------------------
# MARIO_MELODY integrity
# ---------------------------------------------------------------------------

class TestMarioMelody:
    def test_non_empty(self):
        assert len(MARIO_MELODY) > 0

    def test_all_entries_are_pairs(self):
        for i, entry in enumerate(MARIO_MELODY):
            assert len(entry) == 2, f"Entry {i} is not a 2-tuple: {entry!r}"

    def test_all_frequencies_valid(self):
        for i, (freq, _) in enumerate(MARIO_MELODY):
            assert freq == REST or (_HW_MIN <= freq <= _HW_MAX), (
                f"Entry {i}: frequency {freq} is neither REST nor in "
                f"hw range {_HW_MIN}–{_HW_MAX}"
            )

    def test_all_durations_positive(self):
        for i, (_, dur) in enumerate(MARIO_MELODY):
            assert dur > 0, f"Entry {i}: duration {dur} must be positive"

    def test_exported_from_package(self):
        # MARIO_MELODY must be importable from the top-level package
        from py_ibm_4610 import MARIO_MELODY as m
        assert m is MARIO_MELODY


# ---------------------------------------------------------------------------
# play_melody()
# ---------------------------------------------------------------------------

class TestPlayMelody:
    """Tests use a MagicMock printer so no USB hardware is needed."""

    @staticmethod
    def _make_printer():
        p = MagicMock()
        p.beep.return_value = p   # chainable, like the real implementation
        p.flush.return_value = p
        return p

    def test_beep_called_for_each_note(self):
        p = self._make_printer()
        melody = [(A4, e), (REST, q), (C5, q)]
        with patch("py_ibm_4610.beeper.time") as t:
            t.monotonic.return_value = 0.0
            play_melody(p, melody)
        assert p.beep.call_count == 2

    def test_beep_not_called_for_rest(self):
        p = self._make_printer()
        with patch("py_ibm_4610.beeper.time") as t:
            t.monotonic.return_value = 0.0
            play_melody(p, [(REST, q)])
        p.beep.assert_not_called()

    def test_flush_called_after_each_beep(self):
        p = self._make_printer()
        melody = [(A4, e), (REST, q), (C5, q)]
        with patch("py_ibm_4610.beeper.time") as t:
            t.monotonic.return_value = 0.0
            play_melody(p, melody)
        assert p.flush.call_count == 2

    def test_flush_not_called_for_rest(self):
        p = self._make_printer()
        with patch("py_ibm_4610.beeper.time") as t:
            t.monotonic.return_value = 0.0
            play_melody(p, [(REST, q)])
        p.flush.assert_not_called()

    def test_beep_receives_correct_args(self):
        p = self._make_printer()
        with patch("py_ibm_4610.beeper.time") as t:
            t.monotonic.return_value = 0.0
            play_melody(p, [(A4, e)], volume=42)
        p.beep.assert_called_once_with(duration_100ms=e, frequency=A4, volume=42)

    def test_default_volume_is_80(self):
        p = self._make_printer()
        with patch("py_ibm_4610.beeper.time") as t:
            t.monotonic.return_value = 0.0
            play_melody(p, [(A4, q)])
        assert p.beep.call_args.kwargs["volume"] == 80

    def test_sleep_called_when_time_remaining(self):
        p = self._make_printer()
        # monotonic always returns 0 → deadline is always in the future → sleep is called
        with patch("py_ibm_4610.beeper.time") as t:
            t.monotonic.return_value = 0.0
            play_melody(p, [(A4, q)])
        t.sleep.assert_called_once()
        assert t.sleep.call_args[0][0] > 0

    def test_sleep_skipped_when_past_deadline(self):
        p = self._make_printer()
        # First monotonic() call sets the initial deadline (0.0);
        # second call (remaining check) returns far in the future so remaining < 0.
        with patch("py_ibm_4610.beeper.time") as t:
            t.monotonic.side_effect = [0.0, 1_000_000.0]
            play_melody(p, [(A4, q)])
        t.sleep.assert_not_called()

    def test_empty_melody(self):
        p = self._make_printer()
        with patch("py_ibm_4610.beeper.time"):
            play_melody(p, [])
        p.beep.assert_not_called()
        p.flush.assert_not_called()

    def test_rest_only_melody(self):
        p = self._make_printer()
        with patch("py_ibm_4610.beeper.time") as t:
            t.monotonic.return_value = 0.0
            play_melody(p, [(REST, q), (REST, h)])
        p.beep.assert_not_called()
        p.flush.assert_not_called()
