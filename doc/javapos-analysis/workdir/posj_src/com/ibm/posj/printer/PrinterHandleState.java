package com.ibm.posj.printer;

import com.ibm.jutil.ByteBuffer;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.parser.PrinterParser;

public interface PrinterHandleState {
   byte getCurrentStation();

   byte getMaxFontNumber(byte var1);

   byte getFontSize(byte var1, byte var2) throws IllegalArgumentException;

   boolean isMICRPresent();

   boolean isFlipperPresent();

   int getMicronsPerStep(byte var1);

   int maxCmdSize();

   int getColorCapability(byte var1);

   boolean stationPresent(byte var1);

   int getPrinterType();

   boolean isDoubleByteMode();

   boolean isDBCSPrinter();

   int getPrinterID();

   String getSerialNumber();

   int getPrinterEC();

   byte getCharWidth(byte var1);

   byte getCharHeight(byte var1);

   int getSpacingWidth(byte var1);

   int getStationWidth(byte var1);

   int getDotsPerInchWide(byte var1);

   int getDotsPerInchHigh(byte var1);

   int getFeedStepsPerInch(byte var1);

   boolean errorPending();

   int getAlignment(int var1);

   int getTextPosition(int var1);

   int getBarCodeHeight(int var1, int var2);

   int getBarCodeWidth(int var1, byte var2);

   int getBarCodeMaxHeight(int var1);

   int getBarCodeMaxWidth(int var1);

   int getRecSidewaysMaxChars(int var1);

   int getRecSidewaysMaxLines(int var1);

   int getMaxPrintBitmapSize(int var1);

   int getBitmapMaxHeight(int var1);

   ImageStreamProducer getImageStreamProducer();

   PrinterParser getParser();

   int getBitmapHeight(int var1, int var2);

   int getBitmapWidth(int var1, byte var2);

   boolean getCapPartialLinePrint(byte var1);

   boolean getCapConcurrentJrnRec();

   boolean getCapConcurrentJrnSlp();

   boolean getCapConcurrentRecSlp();

   boolean getCapCoverSensor();

   boolean getCapTransaction();

   boolean getCap2Color(byte var1);

   boolean getCapBold(byte var1);

   boolean getCapDhigh(byte var1);

   boolean getCapDwide(byte var1);

   boolean getCapDwideDhigh(byte var1);

   boolean getCapEmptySensor(byte var1);

   boolean getCapItalic(byte var1);

   boolean getCapNearEndSensor(byte var1);

   boolean getCapUnderline(byte var1);

   boolean getCapBarCode(byte var1);

   boolean getCapBitmap(byte var1);

   boolean getCapLeft90(byte var1);

   boolean getCapRight90(byte var1);

   boolean getCapRotate180(byte var1);

   boolean getCapPapercut(byte var1);

   boolean getCapStamp(byte var1);

   boolean getCapFullslip(byte var1);

   boolean getCapFeedReverse(byte var1);

   boolean getCapReverseVideo(byte var1);

   boolean getCapShading(byte var1);

   boolean getCapScaleHorizontally(byte var1);

   boolean getCapScaleVertically(byte var1);

   boolean getCapSubScript(byte var1);

   boolean getCapSuperScript(byte var1);

   byte getMaxBitmapNumber(byte var1);

   byte getMaxLogoNumber(byte var1);

   int getMaxUnderlineNumber(byte var1);

   byte getMaxColorNumber(byte var1);

   byte getMaxScaleHorizontalNumber(byte var1);

   byte getMaxScaleVerticalNumber(byte var1);

   byte getMaxRGBColorNumber(byte var1);

   int getMaxLinesToFeed(byte var1);

   void setSetBitmapCmd(POSPrinterCmd.SetBitmapCmd var1);

   void cleanBitmaps(byte var1);

   void deleteBitmap(byte var1, byte var2);

   POSPrinterCmd.SetBitmapCmd getSetBitmapCmd(byte var1, byte var2);

   void setSetLogoCmd(POSPrinterCmd.SetLogoCmd var1);

   void cleanLogos();

   void deleteLogo(byte var1);

   POSPrinterCmd.SetLogoCmd getSetLogoCmd(byte var1);

   POSPrinterCmd.SetBitmapCmd getTopBitmapCmd(byte var1);

   void setAlignment(byte var1, byte var2);

   byte getAlignment(byte var1);

   int getSidewaysLineWidth(byte var1);

   boolean isMctValueRead();

   void setMCT56Area(ByteBuffer var1);

   void setMctValueRead(boolean var1);

   void setMatrixUDF1Thermal(ByteBuffer var1);

   void setMatrixUDF2Thermal(ByteBuffer var1);

   void setMatrixUDF3Thermal(ByteBuffer var1);

   void setMatrixUDF4Thermal(ByteBuffer var1);

   void setMatrixUDF1Impact(ByteBuffer var1);

   void setMatrixUDF2Impact(ByteBuffer var1);

   ByteBuffer getMCT56Area();

   ByteBuffer getMatrixUDF1Thermal();

   ByteBuffer getMatrixUDF2Thermal();

   ByteBuffer getMatrixUDF3Thermal();

   ByteBuffer getMatrixUDF4Thermal();

   ByteBuffer getMatrixUDF1Impact();

   ByteBuffer getMatrixUDF2Impact();

   void setTransactionMode(boolean var1);

   boolean isTransactionModeOn();

   public interface StateChangeListener {
      void stateChanged(POSPrinterCmd var1);
   }
}
