package com.ibm.posj;

public interface IBMSureonePrinterCmdConst extends POSPrinterCmdConst {
   byte REC_FONTA = 0;
   byte REC_FONTB = 1;
   byte REC_FONTC = 2;
   byte REC_FONTA_WIDTH = 12;
   byte REC_FONTB_WIDTH = 12;
   byte REC_FONTC_WIDTH = 8;
   byte REC_FONTA_HEIGHT = 24;
   byte REC_FONTB_HEIGHT = 24;
   byte REC_FONTC_HEIGHT = 16;
   byte RESIDENT_CS = 0;
   byte NONRESIDENT_CS = 1;
   byte DEFAULT_SPACING = 3;
   byte REC_DOT_MIN = 0;
   byte REC_DOT_MAX = 8;
   byte REC_DBCS_DOT_MIN = 0;
   byte REC_DBCS_DOT_MAX = 32;
   int ER_RELEASE_AFTER_CORRECTION = 1;
   int ER_AUTO_RETRY_AFTER_HOME_ERROR = 2;
   int ER_WAIT_FOR_DOCUMENT = 4;
   byte[][] FONT_TYPE_TABLE = new byte[][]{{2, 0, 12}};
}
