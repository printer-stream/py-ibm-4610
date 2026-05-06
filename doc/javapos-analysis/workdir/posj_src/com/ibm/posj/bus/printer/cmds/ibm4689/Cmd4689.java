package com.ibm.posj.bus.printer.cmds.ibm4689;

import com.ibm.posj.bus.printer.cmds.AbstractByteCmd;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;

public class Cmd4689 extends AbstractByteCmd {
   public byte[] READ_UDC = new byte[]{1, 8, 0, 0, 0};
   public byte[] WRITE_UDC = new byte[]{1, 4, 0, 0, 0};
   public byte[] LOAD_UDC = new byte[]{1, 2, 0, 0, 0};
   public byte[] WRITE_MODE = new byte[]{1, 1, 0, 0, 0};
   public byte[] INITIALIZE = new byte[]{1, 0, 16, 0, 0, 0};
   public byte[] JR_COMM = new byte[]{1, 0, 2, 0, 0};
   public byte[] JR_CR_COMM = new byte[]{1, 0, 4, 0, 0};
   public byte[] JR_CR_EJECT = new byte[]{1, 0, 0, 4, 0, 0};
   public byte[] JR_EJECT = new byte[]{1, 0, 0, 2, 0, 0};
   public byte[] CR_EJECT = new byte[]{1, 0, 0, 1, 0, 0};
   public byte[] JR_CR_FEED = new byte[]{1, 0, 0, 0, 4};
   public byte[] JR_FEED = new byte[]{1, 0, 0, 0, 2};
   public byte[] CR_FEED = new byte[]{1, 0, 0, 0, 1};
   public byte[] FEED_UNITS_BACK = new byte[]{27, 75};
   public byte[] CR_PARTIAL_CUT = new byte[]{1, 0, 0, 0, 32, 0};
   public byte[] FULL_CUT_LOGO_PRINT = new byte[]{1, 0, 0, 0, 16, 0};
   public byte[] PARTIAL_CUT = new byte[]{27, 109};
   public byte[] SET_MSG = new byte[]{29, 58};
   public byte[] PRINT_SET_MSG = new byte[]{29, 94};
   public byte[] WRITE_USR_FLASH = new byte[]{27, 39, 0};
   public byte[] READ_USR_FLASH = new byte[]{27, 39, 1};
   public byte[] TEST_USR_FLASH = new byte[]{27, 44};
   public byte[] ERASE_FLASH = new byte[]{27, 35};
   public byte[] END_OF_DATA = new byte[]{27, -1};
   public byte[] MULTI_LINE_MODE = new byte[]{27, 96};
   public byte[] ROTATE_NORMAL = new byte[]{27, 83};
   public byte[] SEND_CHKSUM = new byte[]{27, 34};
   public byte[] BARCODE_HRI_FONT = new byte[]{29, 102};
   public byte[] ERASE_FIRMWARE = new byte[]{1, 69, 84, 65};
   public byte[] ERASE_CHIP = new byte[]{1, 69, 84, 66};
   public byte[] DOWNLOAD_FIRMWARE = new byte[]{1, 68, 78};
   public byte[] IMAGE_CHAR = new byte[]{27, 94};
   public byte[] EC_REQUEST = new byte[]{0, -128, 0, 0, 0, 0};
   public byte[] DISPLAY_ASCII = new byte[]{0, 0, 0};
   public byte[] LINE_ATTRB = new byte[]{27, 64};
   public byte[] CHAR_ATTRB1 = new byte[]{27, 65};
   public byte[] CHAR_ATTRB2 = new byte[]{27, 66};
   public byte[] LINE_ATTRB_NORMAL = new byte[]{this.LINE_ATTRB[0], this.LINE_ATTRB[1], 0};
   public byte[] CHAR_ATTRB_NORMAL = new byte[]{this.CHAR_ATTRB1[0], this.CHAR_ATTRB1[1], 0};
   public byte[] REVERSE_VIDEO = new byte[]{this.CHAR_ATTRB2[0], this.CHAR_ATTRB2[1], 32};
   public byte[] DOUBLE_HIGH_WIDE_MODE = new byte[]{this.CHAR_ATTRB1[0], this.CHAR_ATTRB1[1], -64};
   public byte[] FEED_LINES_RECEIPT = new byte[]{1, 0, 0, 0, 1};
   public byte[] FEED_LINES_JOURNAL = new byte[]{1, 0, 0, 0, 2};
   public byte[] FEED_LINE_4689 = new byte[]{this.LINE_ATTRB[0], this.LINE_ATTRB[1], 0, this.CHAR_ATTRB1[0], this.CHAR_ATTRB1[1], 0};
   public byte[] BITMAP_HD = new byte[]{1, 0, 1, 0, 0, 1};

   public Cmd4689() {
      this.MCT_READ = new byte[]{27, 83};
      this.MCT_WRITE = new byte[]{27, 77};
      this.CR_COMM = new byte[]{1, 0, 1, 0, 0};
      this.FEED_UNITS = new byte[]{27, 74};
      this.FEED_REVERSE = new byte[]{27, 75};
      this.CUT_PAPER = new byte[]{27, 105};
      this.SET_BITMAP = new byte[]{29, 42};
      this.SET_LOGO = new byte[]{29, 47};
      this.DOWNLOAD_FONT = new byte[]{27, 38};
      this.DOWNLOAD_DBCS_FONT = new byte[]{27, 40};
      this.STATION = new byte[]{29, 99};
      this.UNDERLINE_MODE = new byte[]{this.CHAR_ATTRB1[0], this.CHAR_ATTRB1[1], 2};
      this.DOUBLE_HIGH_MODE = new byte[]{this.CHAR_ATTRB1[0], this.CHAR_ATTRB1[1], -128};
      this.DOUBLE_WIDE_MODE = new byte[]{this.CHAR_ATTRB1[0], this.CHAR_ATTRB1[1], 64};
      this.EMPHASIZE_MODE = new byte[]{this.CHAR_ATTRB2[0], this.CHAR_ATTRB2[1], 16};
      this.NORMAL_MODE = new byte[]{this.CHAR_ATTRB1[0], this.CHAR_ATTRB1[1], 0, this.CHAR_ATTRB2[0], this.CHAR_ATTRB2[1], 0};
      this.BARCODE_PRINTBAR = new byte[]{29, 107};
      this.BARCODE_HORIZ = new byte[]{29, 119};
      this.BARCODE_VERT = new byte[]{29, 104};
      this.BARCODE_HRI_POSI = new byte[]{29, 72};
      this.TEST_REQ = new byte[]{0, 16, 0, 0, 0, 0};
      this.STATUS_REQUEST = new byte[]{0, 32, 0, 0, 0, 0};
      this.RESET = new byte[]{0, 64, 0, 0, 0, 0};
      this.DEVICE_INFO = new byte[]{0, 0, 1, 0, 0, 0};
      this.ROTATE_CHARS90 = new byte[]{27, 82};
      this.JOURNAL_LINE_PRINTED_COUNT = new byte[]{0, 32, 0, 0, 0, 0};
      this.PAPER_CUT_COUNT = new byte[]{0, 32, 0, 0, 0, 0};
      this.RECEIPT_LINE_PRINTED_COUNT = new byte[]{0, 32, 0, 0, 0, 0};
   }

   public DevBus getBusType() {
      return DevBuses.HID_DEVBUS;
   }
}
