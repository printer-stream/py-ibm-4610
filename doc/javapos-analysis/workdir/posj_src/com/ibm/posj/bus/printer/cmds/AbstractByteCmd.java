package com.ibm.posj.bus.printer.cmds;

import com.ibm.posj.util.DevBus;

public abstract class AbstractByteCmd {
   public byte[] MCT_READ = null;
   public byte[] MCT_WRITE = null;
   public byte[] CR_COMM = null;
   public byte[] DIP_COMM = null;
   public byte[] DIL_COMM = null;
   public byte[] SJ_COMM = null;
   public byte[] STATION = null;
   public byte[] RESET = null;
   public byte[] CLEAR = null;
   public byte[] RESUME = null;
   public byte[] TEST_REQ = null;
   public byte[] STATUS_REQUEST = null;
   public byte[] TAB_SET = null;
   public byte[] SET_ALIGNMENT = null;
   public byte[] UNI_DIRECTIONAL = null;
   public byte[] ERROR_RECOVERY = null;
   public byte[] REPRINT_CHAR = null;
   public byte[] UPSIDE_DOWN = null;
   public byte[] FIX_FONT = null;
   public byte[] COLOR_SELECT = null;
   public byte[] BARCODE_PRINTBAR = null;
   public byte[] BARCODE_HORIZ = null;
   public byte[] BARCODE_VERT = null;
   public byte[] BARCODE_HRI_POSI = null;
   public byte[] ERASE_LOGOS = null;
   public byte[] PRINT_LOGOS = null;
   public byte[] DOWNLOAD_LOGOS = null;
   public byte[] ERASE_MESSAGES = null;
   public byte[] INTER_CHARSPACING_SB = null;
   public byte[] INTER_CHARSPACING_DB = null;
   public byte[] SELECT_USER_CHARSET = null;
   public byte[] INVERT_CHARS = null;
   public byte[] ROTATE_CHARS90 = null;
   public byte[] CUT_PAPER = null;
   public byte[] DI_EJECT = null;
   public byte[] PULSE_DRAWER = null;
   public byte[] BEEPER = null;
   public byte[] DEVICE_INFO = null;
   public byte[] MICR_READ = null;
   public byte[] OPEN_JAWS = null;
   public byte[] REGISTER_DOC = null;
   public byte[] END_REGISTER_DOC = null;
   public byte[] GRAB_SLIP = null;
   public byte[] START_SCAN = null;
   public byte[] PRINT_SCANNED_IMG = null;
   public byte[] SCANNER_CALIB = null;
   public byte[] RETRIEVE_IMAGE = null;
   public byte[] GET_NEXT_IMG_LOC = null;
   public byte[] GET_FIRST_UNREAD_IMG_LOC = null;
   public byte[] STORE_SCANNED_IMG = null;
   public byte[] FLIP_CHECK = null;
   public byte[] FORMFEED_LENGTH = null;
   public byte[] BUTTON_ENABLE = null;
   public byte[] LINE_SPACING = null;
   public byte[] PRINT_QUALITY = null;
   public byte[] BEGIN_INSERTION = null;
   public byte[] END_INSERTION = null;
   public byte[] CONTINUATION_CMD = null;
   public byte[] SET_BITMAP = null;
   public byte[] BUFFERED_DEV_INFO = null;
   public byte[] SET_RELATIVE_POS = null;
   public byte[] SET_LEFT_MARGIN_POS = null;
   public byte[] EMPHASIZE_MODE = null;
   public byte[] UNDERLINE_MODE = null;
   public byte[] DOUBLE_WIDE_MODE = null;
   public byte[] DOUBLE_HIGH_MODE = null;
   public byte[] INVERT_MODE = null;
   public byte[] NORMAL_MODE = null;
   public byte[] LINE_FEED_CHAR = new byte[]{10};
   public byte[] SELECT_FONT_FACE = null;
   public byte[] SET_DOT_SPACING = null;
   public byte[] SET_DBCS_DOT_SPACING = null;
   public byte[] FEED_REVERSE = null;
   public byte[] FONT_SCALE = null;
   public byte[] FONT_COLOR = null;
   public byte[] FONT_COLOR_MODE = null;
   public byte[] FEED_UNITS = null;
   public byte[] RETURN_HOME = null;
   public byte[] PRINT_SET_BITMAP = null;
   public byte[] PRINT_SET_LOGO = null;
   public byte[] SET_LOGO = null;
   public byte[] SET_CHASE_MODE = null;
   public byte[] RESIDENT_CHAR_SET = null;
   public byte[] SELECT_CODE_PAGE = null;
   public byte[] PRINT_IN_RAW_MODE = null;
   public byte[] PRINTER_READ = null;
   public byte[] FISCAL = null;
   public byte[] SET_STAMP = null;
   public byte[] DOWNLOAD_FONT = null;
   public byte[] DOWNLOAD_DBCS_FONT = null;
   public byte[] PRINT_STATION_SETTING = null;
   public byte[] USER_CHAR_SET = null;
   public byte[] ERASE_FLASH_DL_GRAPHICS = null;
   public byte[] ERASE_FLASH_PRE_MESSAGES = null;
   public byte[] ERASE_FLASH_USR_DEF_IMPACT_CHAR_SETS = null;
   public byte[] ERASE_FLASH_USR_DEF_THERMAL_CHAR_SETS = null;
   public byte[] ERASE_FLASH_USR_FLA_STORAGE = null;
   public byte[] ERASE_FLASH_ALL_DBL_BYTE_CHARS = null;
   public byte[] ERASE_FLASH_CHECK_IMAGES = null;
   public byte[] BARCODE_PRINTED_COUNT = null;
   public byte[] BARCODE_PRINTED_COUNT_REMAINDER = null;
   public byte[] FORM_INSERTION_COUNT = null;
   public byte[] FORM_INSERTION_COUNT_REMAINDER = null;
   public byte[] HOME_ERROR_COUNT = null;
   public byte[] JOURNAL_CHARACTER_PRINTED_COUNT = null;
   public byte[] JOURNAL_LINE_PRINTED_COUNT = null;
   public byte[] MAXIMUM_TEMP_REACHED_COUNT = null;
   public byte[] NVRAM_WRITE_COUNT = null;
   public byte[] PAPER_CUT_COUNT = null;
   public byte[] PAPER_CUT_COUNT_REMAINDER = null;
   public byte[] FAILED_PAPER_CUT_COUNT = null;
   public byte[] PRINTER_FAULT_COUNT = null;
   public byte[] PRINT_SIDE_CHANGE_COUNT = null;
   public byte[] PRINT_SIDE_CHANGE_COUNT_REMAINDER = null;
   public byte[] FAILED_PRINT_SIDE_CHANGE_COUNT = null;
   public byte[] FAILED_PRINT_SIDE_CHANGE_COUNT_REMAINDER = null;
   public byte[] RECEIPT_CHARACTER_PRINTED_COUNT = null;
   public byte[] RECEIPT_COVER_OPEN_COUNT = null;
   public byte[] RECEIPT_LINE_FEED_COUNT = null;
   public byte[] RECEIPT_LINE_FEED_COUNT_REMAINDER = null;
   public byte[] RECEIPT_LINE_PRINTED_COUNT = null;
   public byte[] SLIP_CHARACTER_PRINTED_COUNT = null;
   public byte[] SLIP_CHARACTER_PRINTED_COUNT_REMAINDER = null;
   public byte[] SLIP_COVER_OPEN_COUNT = null;
   public byte[] SLIP_LINE_FEED_COUNT = null;
   public byte[] SLIP_LINE_FEED_COUNT_REMAINDER = null;
   public byte[] SLIP_LINE_PRINTED_COUNT = null;
   public byte[] STAMP_FIRED_COUNT = null;
   public byte[] RECEIPT_CHARACTER_PRINTED_COUNT_HIGH = null;
   public byte[] RECEIPT_CHARACTER_PRINTED_COUNT_LOW = null;

   public byte[] append(byte[] first, byte[] second) {
      byte[] total = new byte[first.length + second.length];
      System.arraycopy(first, 0, total, 0, first.length);
      System.arraycopy(second, 0, total, first.length, second.length);
      return total;
   }

   public byte[] append(byte[] first, byte second) {
      byte[] total = new byte[first.length + 1];
      System.arraycopy(first, 0, total, 0, first.length);
      total[first.length] = second;
      return total;
   }

   public byte[] set(byte[] someArray, byte value) {
      byte[] cpy = new byte[someArray.length];
      System.arraycopy(someArray, 0, cpy, 0, someArray.length);
      cpy[this.len(someArray) - 1] = value;
      return cpy;
   }

   public int len(byte[] someArray) {
      return someArray.length;
   }

   public abstract DevBus getBusType();
}
