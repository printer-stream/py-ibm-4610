package com.ibm.posj.printer.parser;

import com.ibm.posj.POSPrinterCmd;

public interface ParserLine {
   void reset();

   void buildLineMasterCmd();

   void buildLineMasterCmd(boolean var1);

   void setStation(byte var1) throws IllegalArgumentException;

   void init();

   boolean isLineCompleted();

   int appendElement(PrinterParserElement var1);

   POSPrinterCmd getLineMasterCmd();

   POSPrinterCmd getProperties();

   void resetProperties();

   void clearProperties();

   void setEscCmdProcessor(PrinterEscCmdProcessor var1);

   PrinterParserLine.AlignedText getLeftText();

   PrinterParserLine.AlignedText getCenterText();

   PrinterParserLine.AlignedText getRightText();
}
