package com.ibm.posj.printer.parser;

import com.ibm.jutil.ByteEncoder;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterCmdVisitor;
import com.ibm.posj.printer.PrinterHandleState;
import java.util.HashMap;

public interface PrinterEscCmdProcessor {
   byte PAR_SINGLE_WIDEHIGH = 1;
   byte PAR_DOUBLE_WIDE = 2;
   byte PAR_DOUBLE_HIGH = 3;
   byte PAR_DOUBLE_WIDEHIGH = 4;
   int PAR_DEFAULT_PRECENT = 100;
   int PAR_DEFAULT_ALTER_COLOR = 1;
   int PAR_DEFAULT_SCALE_HOR = 0;
   int PAR_DEFAULT_SCALE_VER = 0;
   int PAR_DEFAULT_RGB_COLOR = 0;
   int PAR_DEFAULT_FEED_LINES = 1;
   int PAR_DEFAULT_BITMAP = 1;
   int PAR_DEFAULT_FONT_FACE = 0;
   int PAR_DEFAULT_UNDERLINE = 1;
   int PAR_DEFAULT_FONT_MAG = 1;
   int DEFAULT_CODE_PAGE = 998;

   PrinterEscCmdVisitor getCheckCapCmdVisitor();

   PrinterEscCmdVisitor getCheckParamCmdVisitor();

   PrinterEscCmdVisitor getCrtPOSPrtCmdVisitor();

   PrinterEscCmdVisitor getUpdateParserStateVisitor();

   POSPrinterCmdVisitor getClonPOSPrinterCmdVisitor();

   HashMap getEscCmdHashMap();

   POSPrinterCmd.Factory getFactory();

   PrinterParserState getParserState();

   PrinterHandleState getHandleState();

   ByteEncoder getByteEncoder();
}
