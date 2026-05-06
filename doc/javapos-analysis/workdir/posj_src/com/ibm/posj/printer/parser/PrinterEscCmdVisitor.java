package com.ibm.posj.printer.parser;

import com.ibm.posj.POSPrinterCmd;

public interface PrinterEscCmdVisitor {
   POSPrinterCmd getPOSPrinterCmd();

   void reset();

   void visitPaperCutEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitFeedPaperCutEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitFeedPaperCutStampEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitFireStampEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitPrintBitmapEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitPrintTopLogoEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitPrintBottomLogoEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitFeedLinesEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitFeedUnitsEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitFeedReverseEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitFontTypefaceEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitBoldEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitUnderlineEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitItalicEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitAlternateColorEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitReverseVideoEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitShadingEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitHighWideControlEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitScaleHorizontallyEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitScaleVerticallyEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitRGBColorEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitSubScriptEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitSuperScriptEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitCenterEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitRightJustifyEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitNormalEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitPassThruEscCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;

   void visitNormalDataCmd(PrinterEscCmd var1) throws IllegalAccessException, IllegalArgumentException;
}
