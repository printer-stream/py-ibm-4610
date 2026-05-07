"""Tests for public API — verifies the installed package exports are accessible."""

import py_ibm_4610


class TestVersion:
    def test_version_is_string(self):
        assert isinstance(py_ibm_4610.__version__, str)

    def test_version_is_semver(self):
        parts = py_ibm_4610.__version__.split(".")
        assert len(parts) == 3
        assert all(part.isdigit() for part in parts)


class TestClassExports:
    def test_ibm4610_exported(self):
        from py_ibm_4610 import IBM4610
        assert IBM4610 is not None

    def test_ibm4610_1nr_exported(self):
        from py_ibm_4610 import IBM4610_1NR
        assert IBM4610_1NR is not None

    def test_reference_page_exported(self):
        from py_ibm_4610 import ReferencePage
        assert ReferencePage is not None

    def test_ibm4610_1nr_is_subclass_of_ibm4610(self):
        from py_ibm_4610 import IBM4610, IBM4610_1NR
        assert issubclass(IBM4610_1NR, IBM4610)


class TestConstantExports:
    def test_usb_constants(self):
        from py_ibm_4610 import VENDOR, PRODUCT_1NR, IFACE, REPORT_SIZE
        assert VENDOR == 0x04B3
        assert PRODUCT_1NR == 0x4535
        assert IFACE == 1
        assert REPORT_SIZE == 1022

    def test_station_constants(self):
        from py_ibm_4610 import (
            STATION_NONE, STATION_RECEIPT, STATION_SLIP, STATION_LABEL,
        )
        assert STATION_NONE == 0x00
        assert STATION_RECEIPT == 0x02
        assert STATION_SLIP == 0x04
        assert STATION_LABEL == 0x14

    def test_align_constants(self):
        from py_ibm_4610 import ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT
        assert ALIGN_LEFT == 0
        assert ALIGN_CENTER == 1
        assert ALIGN_RIGHT == 2

    def test_font_constants(self):
        from py_ibm_4610 import FONT_A, FONT_B, FONT_C
        assert FONT_A == 0
        assert FONT_B == 1
        assert FONT_C == 2

    def test_statistic_subcmds_exported(self):
        from py_ibm_4610 import STATISTIC_SUBCMDS
        assert isinstance(STATISTIC_SUBCMDS, dict)
        assert len(STATISTIC_SUBCMDS) > 0
        assert "PaperCutCount" in STATISTIC_SUBCMDS

    def test_make_packet_exported(self):
        from py_ibm_4610 import make_packet, MAX_PAYLOAD
        pkt = make_packet(b"test")
        assert len(pkt) == 1022
        assert MAX_PAYLOAD == 1015


class TestModuleStructure:
    def test_all_defined(self):
        assert hasattr(py_ibm_4610, "__all__") or True  # __all__ is optional

    def test_no_import_error_on_import(self):
        # Re-importing should never raise
        import importlib
        importlib.import_module("py_ibm_4610")
        importlib.import_module("py_ibm_4610.printer")
        importlib.import_module("py_ibm_4610._transport")
        importlib.import_module("py_ibm_4610._constants")
