package com.ibm.posj.printer.parser;

public interface PrinterParserState {
   int PTR_4610_STATE_TYPE = 1;
   int PTR_4689_STATE_TYPE = 2;
   int PTR_SUREONE_STATE_TYPE = 3;

   void setMaxPrintChars(byte var1, int var2);

   void setLineChars(byte var1, int var2);

   void setLineOffset(byte var1, int var2);

   void setLineWidth(byte var1, int var2);

   void setAlignment(byte var1, byte var2);

   void setFontWideMagnification(byte var1, byte var2);

   void setFontHighMagnification(byte var1, byte var2);

   void setCharacterSet(int var1);

   int getCharacterSet();

   int getMaxPrintChars(byte var1);

   int getLineChars(byte var1);

   int getLineOffset(byte var1);

   int getCharSize(byte var1);

   int getLineWidth(byte var1);

   byte getAlignment(byte var1);

   byte getFontWideMagnification(byte var1);

   byte getFontHighMagnification(byte var1);

   void resetFontWideMagnification(byte var1);

   void resetFontHighMagnification(byte var1);

   void resetAlignment(byte var1);

   void setRecLinesToPaperCut(int var1);

   int getRecLinesToPaperCut();

   void setConversionFactor(byte var1, float var2);

   float getConversionFactor(byte var1);

   void setRotation(byte var1, int var2);

   int getRotation(byte var1);

   void setCap2Color(byte var1, boolean var2);

   boolean getCap2Color(byte var1);

   void setCurrentTabs(byte var1, int[] var2);

   boolean areNewTabs(byte var1, int[] var2);

   void clearTabs();

   int getPrinterStateType();

   boolean isPageModeOn(byte var1);

   void setPageModeState(byte var1, boolean var2);

   void setNormalWithLFState(int var1, byte var2);

   int getNormalWithLFState();

   byte getLastAlignment();
}
