package com.ibm.posj.bus.printer.cmds.sureone;

import com.ibm.posj.bus.printer.cmds.AbstractByteCmd;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;

public class CmdSureone extends AbstractByteCmd {
   public byte[] SEL_CHAR_TABLE = new byte[]{27, 84};
   public byte[] SEL_XON_XOFF = new byte[]{27, 17};
   public byte[] SEL_DTR = new byte[]{27, 18};
   public byte[] SEL_IBM_2CHAR_TBL = new byte[]{27, 54};
   public byte[] SEL_IBM_1CHAR_TBL = new byte[]{27, 55};
   public byte[] SEL_NORMAL_CHAR_SPACNG = new byte[]{27, 77};
   public byte[] SEL_MEDIUM_CHAR_SPACNG = new byte[]{27, 103};
   public byte[] SEL_WIDE_CHAR_SPACNG = new byte[]{27, 80};
   public byte[] SEL_EXTRA_WIDE_CHAR_SPACNG = new byte[]{27, 58};
   public byte[] SEL_2XCHAR_WIDTH_MODE = new byte[]{14};
   public byte[] CANCEL_2XCHAR_WIDTH_MODE = new byte[]{20};
   public byte[] SET_PRNT_MAGNFIED_DBL_CHARH = new byte[]{27, 14};
   public byte[] RST_PRNT_MAGNFIED_CHARH = new byte[]{27, 20};
   public byte[] SEL_EMPHASZD_PRNT_MODE = new byte[]{27, 69};
   public byte[] CANCEL_EMPHASIZED_PRINTNG = new byte[]{27, 72};
   public byte[] CANCEL_EMPHAZSD_PRNT_MODE = new byte[]{27, 70};
   public byte[] CANCEL_UNDERLINE_MODE = new byte[]{27, 45, 48};
   public byte[] SET_OVERLINE_MODE = new byte[]{27, 95, 49};
   public byte[] CANCEL_OVERLINE_MODE = new byte[]{27, 95, 48};
   public byte[] SEL_HIGHLIGHTED_PRNT_MODE = new byte[]{27, 52};
   public byte[] CANCEL_HIGHLIGHTED_PRNT_MODE = new byte[]{27, 53};
   public byte[] CANCEL_INVERTED_PRNT_MODE = new byte[]{18};
   public byte[] LINE_FEED = new byte[]{10};
   public byte[] COMPACT_LINE_SPACNG = new byte[]{27, 48};
   public byte[] TIGHT_LINE_SPACNG = new byte[]{27, 49};
   public byte[] TIME_MICRO_LINE_FEED = new byte[]{27, 74};
   public byte[] TIME_BACKFEED = new byte[]{27, 106};
   public byte[] TIME_8MM_LINE_FEED = new byte[]{27, 73};
   public byte[] CROWDED_LINE_SPACNG = new byte[]{27, 122, 48};
   public byte[] SET_PAGE_LENGTH_LINES = new byte[]{27, 67};
   public byte[] SET_PAGE_LENGTH_INCHES = new byte[]{27, 67, 0};
   public byte[] VERTICAL_TAB = new byte[]{11};
   public byte[] SET_VERTICAL_TAB_POSITION = new byte[]{27, 66};
   public byte[] SET_BOTTOM_MARGIN = new byte[]{27, 78};
   public byte[] CANCEL_BOTTOM_MARGIN = new byte[]{27, 79};
   public byte[] SET_LEFT_MARGIN = new byte[]{27, 108};
   public byte[] SET_RIGHT_MARGIN = new byte[]{27, 81};
   public byte[] HORIZONTAL_TAB = new byte[]{9};
   public byte[] NORMAL_DENSITY_GRAPHS = new byte[]{27, 75};
   public byte[] HIGH_DENSITY_GRAPHS = new byte[]{27, 76};
   public byte[] FINE_DENSITY_GRAPHS_1DOT = new byte[]{27, 107};
   public byte[] FINE_DENSITY_GRAPHS_8DOTS = new byte[]{27, 88};
   public byte[] DELETE_DOWNLOAD_CHAR = new byte[]{27, 38, 49, 48};
   public byte[] ENABLE_DOWNLOAD_CHAR = new byte[]{27, 37, 49};
   public byte[] DISABLE_DOWNLOAD_CHAR = new byte[]{27, 37, 48};
   public byte[] ADJUST_PULSE_4_CD = new byte[]{27, 7};
   public byte[] DEFERRED_CMD_4_CD = new byte[]{7};
   public byte[] IGNORE_CMD_4_CD = new byte[]{28};
   public byte[] CANCEL_PRNT_DATA = new byte[]{24};
   public byte[] IGNORE_REINIT_PRNTR = new byte[]{27, 64};
   public byte[] SEL_BARCODE = new byte[]{27, 98};
   public byte[] PARTIAL_CUT = new byte[]{27, 100, 49};
   public byte[] SET_MEM_SWITCH = new byte[]{27, 35};
   public byte[] SET_MAGNIFICATION_RATE_CHAR_WandH = new byte[]{27, 105};
   public byte[] SEL_SLASH_ZERO = new byte[]{27, 47, 49};
   public byte[] SEL_NORMAL_ZERO = new byte[]{27, 47, 48};
   public byte[] REQ_SEND_FIRMWARE_VER = new byte[]{27, 35, 42, 10, 0};
   public byte[] REQ_SEND_MEM_SWITCH = new byte[]{27, 35};
   public byte[] VERTICAL_COL_ALIGN = new byte[]{27, 23, 64};
   public byte[] PRNT_DENSITY = new byte[]{27, 123, 48, 48};

   public CmdSureone() {
      this.CR_COMM = new byte[]{13};
      this.SELECT_USER_CHARSET = new byte[]{27, 82};
      this.SET_DOT_SPACING = new byte[]{27, 32};
      this.DOUBLE_WIDE_MODE = new byte[]{27, 87};
      this.DOUBLE_HIGH_MODE = new byte[]{27, 104};
      this.EMPHASIZE_MODE = new byte[]{27, 71};
      this.UNDERLINE_MODE = new byte[]{27, 45, 49};
      this.INVERT_MODE = new byte[]{15};
      this.LINE_SPACING = new byte[]{27, 122, 49};
      this.FEED_UNITS = new byte[]{27, 97};
      this.FORMFEED_LENGTH = new byte[]{12};
      this.DOWNLOAD_FONT = new byte[]{27, 38, 49, 49};
      this.BEEPER = new byte[]{30};
      this.RESET = new byte[]{27, 63, 10, 0};
      this.STATUS_REQUEST = new byte[]{5};
      this.CUT_PAPER = new byte[]{27, 100, 48};
      this.TAB_SET = new byte[]{27, 68};
   }

   public DevBus getBusType() {
      return DevBuses.RS232_DEVBUS;
   }
}
