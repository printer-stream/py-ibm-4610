package com.ibm.posj;

import com.ibm.jutil.ByteBuffer;
import com.ibm.posj.printer.event.PrintErrorEvent;
import com.ibm.posj.printer.event.PrintStatus;
import java.util.Iterator;

public interface POSPrinterCmd extends HandleCmd {
   boolean isClean();

   boolean compareStations(byte var1);

   void setMemoryCmd();

   boolean isMemoryCmd();

   byte getStation();

   void setStation(byte var1);

   byte getImpliedStations();

   void setRoleAsSettings(boolean var1);

   boolean isSettingsCmd();

   boolean isAsyncCmd();

   void setAsyncCmd(boolean var1);

   void setImmediate(boolean var1);

   boolean isImmediate();

   boolean isOutputCmd();

   void accept(POSPrinterCmdVisitor var1);

   void setOutputID(int var1);

   int getOutputID();

   void appendPOSPrinterCmd(POSPrinterCmd var1) throws IllegalStateException;

   void addAssociatedCmd(POSPrinterCmd var1);

   POSPrinterCmd getMasterCmd();

   POSPrinterCmd getOwnerCmd();

   ByteBuffer getOutboundPacket();

   Iterator iterator();

   int getAppendCnt();

   boolean isSlave();

   int charCnt();

   void setCode(byte var1);

   void setID(byte var1);

   byte getID();

   void acceptMultiVisitor(POSPrinterCmdVisitor var1);

   public interface AlignPositionCmd extends POSPrinterCmd.SelectStationCmd {
      byte getOption();
   }

   public interface AlignmentCmd extends POSPrinterCmd.SelectStationCmd {
      byte getAlignment();
   }

   public interface AlterWideHighCmd extends POSPrinterCmd.SelectStationCmd {
      short getOption();
   }

   public interface BoldCmd extends POSPrinterCmd.SelectStationCmd {
      boolean isActive();
   }

   public interface CutPaperCmd extends POSPrinterCmd.SelectStationCmd {
      short getPercentage();

      short getLinesToPaperCut();
   }

   public interface DataRequesterCmd extends POSPrinterCmd {
      ByteBuffer getRequestData();
   }

   public interface DevInfoCmd extends POSPrinterCmd.DataRequesterCmd, SystemCmd.DeviceInfoRequestCmd {
      boolean isBuffered();
   }

   public interface DotSpacingCmd extends POSPrinterCmd.SelectStationCmd {
      void setDotSpacing(int var1);

      int getDotSpacing();

      boolean isDBCS();
   }

   public interface DualPrintCmd extends POSPrinterCmd.SelectStationCmd {
      POSPrinterCmd getCmd1();

      POSPrinterCmd getCmd2();
   }

   public interface EjectSlipCmd extends POSPrinterCmd {
   }

   public interface EnableColorModeCmd extends POSPrinterCmd.SelectStationCmd {
      boolean isActive();
   }

   public interface EndRegisterSlipCmd extends POSPrinterCmd {
   }

   public interface EraseFlashSectorCmd extends POSPrinterCmd.SelectStationCmd {
      byte getSector();
   }

   public interface Factory extends HandleCmd.Factory {
      POSPrinterCmd createSelectStationCmd(byte var1);

      POSPrinterCmd createMarkFeedCmd(byte var1);

      POSPrinterCmd createPrintBarCodeCmd(byte var1, String var2, int var3, byte var4, byte var5, byte var6, byte var7);

      POSPrinterCmd createSetLogoCmd(byte var1, String var2);

      POSPrinterCmd createPrintSetLogoCmd(byte var1, byte var2);

      POSPrinterCmd createSetBitmapCmd(byte var1, String var2, byte[] var3, byte var4, byte var5, byte var6, byte var7, int var8);

      POSPrinterCmd createPrintBitmapCmd(byte var1, String var2, byte[] var3, byte var4, byte var5, byte var6, byte var7);

      POSPrinterCmd createPrintBitmapCmd(byte var1, String var2, byte[] var3, int var4, byte var5, byte var6, byte var7, byte var8);

      POSPrinterCmd createPrintSetBitmapCmd(byte var1, byte var2, byte var3, byte var4, byte var5, byte var6, int var7);

      POSPrinterCmd createDualPrintCmd(byte var1, POSPrinterCmd var2, POSPrinterCmd var3);

      POSPrinterCmd createPrintNormalCmd(byte var1, String var2);

      POSPrinterCmd createPrintNormalCmd(byte var1, byte[] var2, int var3, int var4, byte var5);

      POSPrinterCmd createPrintNormalCmd(byte var1, ByteBuffer var2, int var3, int var4, byte var5);

      POSPrinterCmd createRotatePrintCmd(byte var1, int var2);

      POSPrinterCmd createTextAttribCmd(byte var1, byte var2, boolean var3);

      POSPrinterCmd createAlignmentCmd(byte var1, byte var2);

      POSPrinterCmd createScaleFontCmd(byte var1, byte var2, byte var3);

      POSPrinterCmd createPrinterResetCmd();

      POSPrinterCmd createPassThruCmd(byte[] var1);

      POSPrinterCmd createDevInfoCmd(boolean var1);

      POSPrinterCmd createSetUserDefinedCharCmd(byte var1, byte var2, byte var3, byte var4, byte[] var5);

      POSPrinterCmd createSetProportionalCharCmd(byte var1, byte var2, byte var3, byte var4, byte[] var5);

      POSPrinterCmd createSelectCodePageCmd(byte var1);

      POSPrinterCmd createLineSpacingCmd(byte var1, byte var2);

      POSPrinterCmd createFormFeedLengthCmd(byte var1, byte var2);

      POSPrinterCmd createSetTabStopsCmd(byte var1, int[] var2, boolean var3);

      POSPrinterCmd createLeftMarginCmd(byte var1, byte var2);

      POSPrinterCmd createRelativePositionCmd(byte var1, byte var2);

      POSPrinterCmd createPrintQualityCmd(byte var1, boolean var2);

      POSPrinterCmd createRegisterSlipCmd();

      POSPrinterCmd createRegisterSlipCmd(boolean var1);

      POSPrinterCmd createEndRegisterSlipCmd();

      POSPrinterCmd createEjectSlipCmd();

      POSPrinterCmd createReadSlipCmd();

      POSPrinterCmd createMCTReadCmd(byte var1);

      POSPrinterCmd createMCTValueCmd(byte var1, byte var2, byte var3);

      POSPrinterCmd createEraseFlashSectorCmd(byte var1);

      POSPrinterCmd createFontTypeCmd(byte var1, byte var2);

      POSPrinterCmd createDotSpacingCmd(byte var1, int var2, boolean var3);

      POSPrinterCmd createDotSpacingCmd(byte var1, int var2);

      POSPrinterCmd createUnderlineCmd(short var1);

      POSPrinterCmd createBoldCmd(boolean var1);

      POSPrinterCmd createReverseVideoCmd(boolean var1);

      POSPrinterCmd createCutPaperCmd(short var1);

      POSPrinterCmd createCutPaperCmd(short var1, short var2);

      POSPrinterCmd createAlterWideHighCmd(short var1);

      POSPrinterCmd createFeedLinesCmd(short var1);

      POSPrinterCmd createFeedLinesCmd(short var1, byte var2);

      POSPrinterCmd createFeedUnitsCmd(short var1);

      POSPrinterCmd createFeedReverseCmd(short var1);

      POSPrinterCmd createNormalModeCmd(boolean var1);

      POSPrinterCmd createAlignPositionCmd(byte var1, byte var2);

      POSPrinterCmd createFontColorCmd(byte var1);

      POSPrinterCmd createEnableColorModeCmd(byte var1, boolean var2);

      POSPrinterCmd createSetLeftMarginCmd(byte var1, int var2);
   }

   public interface FeedLinesCmd extends POSPrinterCmd.SelectStationCmd {
      short getLines();
   }

   public interface FeedReverseCmd extends POSPrinterCmd.SelectStationCmd {
      short getLines();
   }

   public interface FeedUnitsCmd extends POSPrinterCmd.SelectStationCmd {
      short getUnits();
   }

   public interface FontColorCmd extends POSPrinterCmd.SelectStationCmd {
      byte getFontColor();
   }

   public interface FontDownloadCmd extends POSPrinterCmd.SelectStationCmd {
   }

   public interface FontTypeCmd extends POSPrinterCmd.SelectStationCmd {
      void setFontType(byte var1);

      byte getFontType();
   }

   public interface FormFeedLengthCmd extends POSPrinterCmd.SelectStationCmd {
      byte getFormFeedLength();
   }

   public interface GetFirstUnreadImgLocCmd extends POSPrinterCmd {
   }

   public interface GetNextImgLocCmd extends POSPrinterCmd {
   }

   public interface GetScannedImgCmd extends POSPrinterCmd {
      short getLocation();

      int getOffset();

      short getNumBytes();
   }

   public interface IDCmd extends POSPrinterCmd {
      void setID(byte var1);

      byte getID();

      void accept(POSPrinterCmdVisitor var1);
   }

   public interface LeftMarginCmd extends POSPrinterCmd.SelectStationCmd {
      byte getDots();
   }

   public interface LineSpacingCmd extends POSPrinterCmd.SelectStationCmd {
      byte getDots();
   }

   public interface MCTCmd extends POSPrinterCmd, POSPrinterCmd.DataRequesterCmd {
      byte getMCT();
   }

   public interface MCTValueCmd extends POSPrinterCmd {
      byte getMatrixValue();

      byte getByteHighValue();

      byte getByteLowValue();
   }

   public interface MarkFeedCmd extends POSPrinterCmd {
      byte getType();
   }

   public interface NormalModeCmd extends POSPrinterCmd.SelectStationCmd {
      boolean isActive();

      void setBoldOn(boolean var1);

      void setUnderlineOn(boolean var1);

      void setReverseVideoOn(boolean var1);

      void setScaleOn(boolean var1);

      void setDoubleWideHighOn(boolean var1);

      void setAlignmentOn(boolean var1);

      void setColorOn(boolean var1);

      boolean isBoldOn();

      boolean isUnderlineOn();

      boolean isReverseVideoOn();

      boolean isScaleOn();

      boolean isDoubleWideHighOn();

      boolean isAlignmentOn();

      boolean isColorOn();
   }

   public interface PassThruCmd extends POSPrinterCmd {
   }

   public interface PrintBarCodeCmd extends POSPrinterCmd.SingleRotateCmd {
      String getData();

      int getSymbol();

      byte getHeight();

      byte getWidth();

      byte getAlignment();

      byte getTextPosition();
   }

   public interface PrintBitmapCmd extends POSPrinterCmd.SingleRotateCmd {
      String getFileName();

      byte getWidth();

      byte getHeight();

      byte getAlignment();

      byte getDensity();

      byte[] getBitmapStream();

      int getBlockIndex();
   }

   public interface PrintDownloadMessageCmd extends POSPrinterCmd.SelectStationCmd {
   }

   public interface PrintNormalCmd extends POSPrinterCmd.SelectStationCmd {
      byte[] getData();

      void setData(String var1);

      int getIndex();

      int getCount();

      void setAppendByte(byte var1);

      byte appendByte();

      boolean isLineCompleted();

      void incompleteLine();

      void completeLine();

      short getVerticalScale();

      void setVerticalScale(short var1);
   }

   public interface PrintQualityCmd extends POSPrinterCmd.SelectStationCmd {
      boolean getQuality();
   }

   public interface PrintScannedImgCmd extends POSPrinterCmd {
   }

   public interface PrintSetBitmapCmd extends POSPrinterCmd.SelectStationCmd {
      byte getBitmapNo();

      byte getDensity();

      byte getAlignment();

      int getMargin();
   }

   public interface PrintSetLogoCmd extends POSPrinterCmd.SelectStationCmd {
      byte getLocation();
   }

   public interface PrinterResult extends HandleCmd.Result {
      PrintErrorEvent getErrorEvent();
   }

   public interface ReadSlipCmd extends POSPrinterCmd.DataRequesterCmd {
   }

   public interface RegisterSlipCmd extends POSPrinterCmd {
      boolean isGrabber();
   }

   public interface RelativePositionCmd extends POSPrinterCmd.SelectStationCmd {
      byte getOffset();
   }

   public interface ResetCmd extends POSPrinterCmd, SystemCmd.ResetRequestCmd {
   }

   public interface ReverseVideoCmd extends POSPrinterCmd.SelectStationCmd {
      boolean isActive();
   }

   public interface RotatePrintCmd extends POSPrinterCmd.SelectStationCmd {
      int getRotation();
   }

   public interface ScaleFontCmd extends POSPrinterCmd.SelectStationCmd {
      byte getWidth();

      byte getHeight();
   }

   public interface ScannerCalibCmd extends POSPrinterCmd {
   }

   public interface SelCompressionFormatCmd extends POSPrinterCmd {
      byte getCompressionFormat();
   }

   public interface SelectCodePageCmd extends POSPrinterCmd.SelectStationCmd {
      byte getSelectedCodePage();

      byte getStation();
   }

   public interface SelectStationCmd extends POSPrinterCmd {
   }

   public interface SetBitmapCmd extends POSPrinterCmd.SelectStationCmd {
      byte getBitmapNo();

      String getFileName();

      byte getWidth();

      byte getHeight();

      byte getAlignment();

      byte[] getBitmapStream();

      int getMargin();
   }

   public interface SetChaseModeCmd extends POSPrinterCmd.SelectStationCmd {
   }

   public interface SetLeftMarginCmd extends POSPrinterCmd.SelectStationCmd {
      int getLeftMarginValue();
   }

   public interface SetLogoCmd extends POSPrinterCmd.SelectStationCmd {
      byte getLocation();

      String getData();
   }

   public interface SetProportionalCharCmd extends POSPrinterCmd.SetUserDefinedCharCmd {
      byte getEnd();

      byte getStart();

      byte getCodePage();
   }

   public interface SetTabStopsCmd extends POSPrinterCmd.SelectStationCmd {
      int[] getTabStops();

      int getTabSize();

      boolean isCenter();
   }

   public interface SetUserDefinedCharCmd extends POSPrinterCmd.FontDownloadCmd {
      byte[] getCharData();

      byte getEnd();

      byte getStart();

      byte getCodePage();
   }

   public interface SingleRotateCmd extends POSPrinterCmd.SelectStationCmd {
      void setRotation(int var1);

      int getRotation();
   }

   public interface StartScanCmd extends POSPrinterCmd {
      boolean isScanAndMicrRead();
   }

   public interface StatisticCmd extends POSPrinterCmd.DataRequesterCmd, SystemCmd.StatisticCmd {
      String getStatisticType();
   }

   public interface StatusRequestCmd extends POSPrinterCmd, SystemCmd.StatusRequestCmd {
      PrintStatus getStatus();
   }

   public interface StoreScannedImgCmd extends POSPrinterCmd {
      byte getStorageMethod();

      int getCorner();

      int getOffset();

      int[] getCornerSub();

      int[] getOffsetSub();

      String getTagData();
   }

   public interface TestReqCmd extends POSPrinterCmd, SystemCmd.TestRequestCmd {
   }

   public interface TextAttribCmd extends POSPrinterCmd.SelectStationCmd {
      boolean enabled();

      byte getAttribute();
   }

   public interface UnderlineCmd extends POSPrinterCmd.SelectStationCmd {
      short getThickness();
   }
}
