package com.ibm.posj;

import com.ibm.jutil.ByteArrayCollector;
import com.ibm.jutil.ByteBuffer;
import com.ibm.posj.bus.printer.cmds.CheckScannerCmdFactory;
import com.ibm.posj.bus.printer.cmds.MICRCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;
import com.ibm.posj.bus.printer.cmds.ibm4610.Font4610CmdFactory;
import com.ibm.posj.bus.printer.cmds.ibm4610.Gen4610CmdFactory;
import com.ibm.posj.bus.printer.cmds.ibm4610.General4610CmdFactory;
import com.ibm.posj.bus.printer.cmds.ibm4610.Grap4610CmdFactory;
import com.ibm.posj.bus.printer.cmds.ibm4610.Print4610CmdFactory;
import com.ibm.posj.printer.StatusVisitor;

public abstract class IBM4610PrinterCmd extends DefaultPOSPrinterCmd {
   public IBM4610PrinterCmd(HandleCmd.Factory factory, String name, byte station) {
      super(factory, name, station);
   }

   public IBM4610PrinterCmd(HandleCmd.Factory factory, String name) {
      super(factory, name);
   }

   public static class BeeperCmd extends IBM4610PrinterCmd {
      private boolean toneOn = false;
      private boolean toneTimed = false;
      private int toneDuration = 0;
      private int toneFrequency = 0;
      private int toneVolume = 0;

      BeeperCmd(HandleCmd.Factory factory, String name, boolean on, boolean timed, int duration, int frequency, int volume) {
         super(factory, name, (byte)0);
         this.toneOn = on;
         this.toneTimed = timed;
         this.toneDuration = duration;
         this.toneFrequency = frequency;
         this.toneVolume = volume;
      }

      public boolean getOn() {
         return this.toneOn;
      }

      public boolean getTimed() {
         return this.toneTimed;
      }

      public int getDuration() {
         return this.toneDuration;
      }

      public int getFrequency() {
         return this.toneFrequency;
      }

      public int getVolume() {
         return this.toneVolume;
      }

      protected PrintCmd buildPrintCmd() {
         return ((General4610CmdFactory)this.factory.getPrintCmdFactory().getGeneralFactory()).createBeeperCmd(this);
      }
   }

   public static class ChangePrintSideCmd extends DefaultPOSPrinterCmd {
      ChangePrintSideCmd(HandleCmd.Factory factory, String name) {
         super(factory, name, (byte)4);
         this.setOutputCmd();
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createChangePrintSideCmd(this);
      }

      public void accept(IBM4610PrinterCmdVisitor visitor) {
         visitor.visitChangePrintSideCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitChangePrintSideCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitChangePrintSideCmd(this);
      }
   }

   public static class CheckSumFlashCmd extends DefaultPOSPrinterCmd.EraseFlashSectorCmd {
      public CheckSumFlashCmd(HandleCmd.Factory factory, String name, byte sector) {
         super(factory, name, sector);
      }
   }

   public static class ClearPageModeCmd extends IBM4610PrinterCmd {
      public ClearPageModeCmd(HandleCmd.Factory factory, String name) {
         super(factory, name, (byte)2);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4610CmdFactory)this.factory.getGraphicCmdFactory()).createClearPageModeCmd(this);
      }
   }

   public static class ContinuationCmd extends DefaultPOSPrinterCmd.ByteArrayStoringCmd {
      private boolean escNeeded = false;
      private boolean simple = false;
      private int start = 0;
      private int length = 0;
      private ByteBuffer header = null;

      public ContinuationCmd(HandleCmd.Factory factory, String name, byte station, ByteBuffer head, byte[] data, boolean escNeeded, int start, int len) {
         super(factory, name, station, data, (byte)0);
         this.setPreWrittenBuffer(head);
         this.escNeeded = escNeeded;
         this.length = len;
         this.start = start;
      }

      public ContinuationCmd(HandleCmd.Factory factory, String name) {
         super(factory, name, (byte)0, null, (byte)0);
         this.simple = true;
         this.escNeeded = true;
      }

      public int getStart() {
         return this.start;
      }

      public boolean isEscapeNeeded() {
         return this.escNeeded;
      }

      public byte[] getData() {
         return super.getArray();
      }

      public void setData(byte[] data) {
         super.setArray(data);
      }

      public int getLength() {
         return this.length;
      }

      public boolean isSimple() {
         return this.simple;
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitContinuationCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitContinuationCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createContinuationCmd(this);
      }
   }

   public static class DownloadDBCSFontCmd extends IBM4610PrinterCmd implements POSPrinterCmd.FontDownloadCmd {
      private short cnt = 1;
      private byte firstChar = 0;
      private byte secondChar = 0;
      private byte[] data = null;

      public DownloadDBCSFontCmd(HandleCmd.Factory factory, String name, byte station, short charCnt, byte[] data) {
         super(factory, name, station);
         this.data = data;
         this.cnt = charCnt;
         this.setMemoryCmd();
      }

      public byte[] getData() {
         return this.data;
      }

      public short getNumOfChars() {
         return this.cnt;
      }

      public void clean() {
         ByteArrayCollector.getCollector().collect(this.data);
         super.clean();
      }

      protected PrintCmd buildPrintCmd() {
         return ((Font4610CmdFactory)this.factory.getFontCmdFactory()).createDownloadDBCSFontCmd(this);
      }
   }

   public static class DownloadFontCmd extends IBM4610PrinterCmd {
      private byte[] data = null;

      public DownloadFontCmd(HandleCmd.Factory factory, String name, byte[] data) {
         super(factory, name);
         this.data = data;
      }

      public byte[] getData() {
         return this.data;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Font4610CmdFactory)this.factory.getFontCmdFactory()).createDownloadFontCmd(this);
      }
   }

   public static class ECLevelRequestCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd {
      public ECLevelRequestCmd(HandleCmd.Factory factory, String name, boolean buffered) {
         super(factory, name, buffered);
      }

      public boolean isBuffered() {
         return this.getBoolean();
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createECLevelRequestCmd(this);
      }

      public void accept(IBM4610PrinterCmdVisitor visitor) {
         visitor.visitECLevelRequestCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitECLevelRequestCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitECLevelRequestCmd(this);
      }
   }

   public static class EnableFeedButtonCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd {
      EnableFeedButtonCmd(HandleCmd.Factory factory, String name, byte button, boolean enabled) {
         super(factory, name, (byte)0, enabled, button);
      }

      public boolean enabled() {
         return this.getBoolean();
      }

      public byte getButton() {
         return this.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createEnableFeedButtonCmd(this);
      }
   }

   public static class EnableLineCntCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd {
      public EnableLineCntCmd(HandleCmd.Factory factory, String name, boolean enable) {
         super(factory, name, enable);
      }

      public boolean isEnabled() {
         return this.getBoolean();
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createEnableLineCntCmd(this);
      }
   }

   public static class ErrorRecoveryCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd {
      boolean autoHome;
      boolean hold4Doc;
      boolean holdFlip;

      public ErrorRecoveryCmd(
         HandleCmd.Factory factory, String name, boolean printBuffRelease, boolean autoHomeErrRetry, boolean holdForDoc, boolean holdFlipError
      ) {
         super(factory, name, printBuffRelease);
         this.autoHome = autoHomeErrRetry;
         this.hold4Doc = holdForDoc;
         holdFlipError = this.holdFlip;
      }

      public boolean isPrintBufferRelease() {
         return this.getBoolean();
      }

      public boolean isAutoHomeErrorRetry() {
         return this.autoHome;
      }

      public boolean isHoldForDocument() {
         return this.hold4Doc;
      }

      public boolean isHoldForFlipError() {
         return this.holdFlip;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createErrorRecoveryCmd(this);
      }
   }

   public static class Factory extends DefaultPOSPrinterCmd.Factory {
      public Factory(PrintCmdFactory lowerFactory) {
         this.setPrintCmdFactory(lowerFactory);
      }

      public MICRCmdFactory getMICRCmdFactory() {
         return ((Print4610CmdFactory)this.pcFactory).getMICRFactory();
      }

      public CheckScannerCmdFactory getCheckScannerCmdFactory() {
         return ((Print4610CmdFactory)this.pcFactory).getCheckScannerFactory();
      }

      public POSPrinterCmd createHeadMovementCmd(byte position) {
         return new IBM4610PrinterCmd.HeadMovementCmd(this, "HeadMovementCmd", position);
      }

      public POSPrinterCmd createChangePrintSideCmd() {
         return new IBM4610PrinterCmd.ChangePrintSideCmd(this, "ChangePrintSideCmd");
      }

      public POSPrinterCmd createEnableFeedButtonCmd(byte button, boolean enabled) {
         return new IBM4610PrinterCmd.EnableFeedButtonCmd(this, "EnableFeedButtonCmd", button, enabled);
      }

      public POSPrinterCmd createContinuationCmd(byte station, ByteBuffer head, byte[] data, boolean escNeeded, int start, int len) {
         return new IBM4610PrinterCmd.ContinuationCmd(this, "ContinuationCmd", station, head, data, escNeeded, start, len);
      }

      public POSPrinterCmd createContinuationCmd() {
         return new IBM4610PrinterCmd.ContinuationCmd(this, "ContinuationCmd");
      }

      public POSPrinterCmd createReinitPrinterCmd() {
         return new IBM4610PrinterCmd.ReinitPrinterCmd(this, "ReinitPrinterCmd");
      }

      public POSPrinterCmd createCheckSumFlashCmd(byte sector) {
         return new IBM4610PrinterCmd.CheckSumFlashCmd(this, "CheckSumFlashCmd", sector);
      }

      public POSPrinterCmd createFlashTestCmd() {
         return new IBM4610PrinterCmd.FlashTestCmd(this, "FlashTestCmd");
      }

      public POSPrinterCmd createStationSettingsCmd(byte station) {
         return new IBM4610PrinterCmd.StationSettingsCmd(this, "StationSettingsCmd", station);
      }

      public POSPrinterCmd createOpenDrawerCmd(byte cdNo, byte widthOn, byte widthOff) {
         return new IBM4610PrinterCmd.OpenDrawerCmd(this, "OpenDrawerCmd", cdNo, widthOn, widthOff);
      }

      public POSPrinterCmd createEnableLineCntCmd(boolean enable) {
         return new IBM4610PrinterCmd.EnableLineCntCmd(this, "EnableLineCntCmd", enable);
      }

      public POSPrinterCmd createResetLineCntCmd() {
         return new IBM4610PrinterCmd.ResetLineCntCmd(this, "ResetLineCnt");
      }

      public POSPrinterCmd createECLevelRequestCmd(boolean buffered) {
         return new IBM4610PrinterCmd.ECLevelRequestCmd(this, "ECLevelRequestCmd", buffered);
      }

      public POSPrinterCmd createErrorRecoveryCmd(boolean printBuffRelease, boolean autoHomeErrRetry, boolean holdForDoc, boolean holdFlipError) {
         return new IBM4610PrinterCmd.ErrorRecoveryCmd(this, "ErrorRecoveryCmd", printBuffRelease, autoHomeErrRetry, holdForDoc, holdFlipError);
      }

      public POSPrinterCmd createReleasePrintBufferCmd(boolean cancel) {
         return new IBM4610PrinterCmd.ReleasePrintBufferCmd(this, "ReleasePrintBufferCmd", cancel);
      }

      public POSPrinterCmd createDownloadFontCmd(byte[] data) {
         return new IBM4610PrinterCmd.DownloadFontCmd(this, "DownloadFontCmd", data);
      }

      public POSPrinterCmd createDownloadDBCSFontCmd(byte station, short charCnt, byte[] data) {
         return new IBM4610PrinterCmd.DownloadDBCSFontCmd(this, "DownloadDBCSFontCmd", station, charCnt, data);
      }

      public POSPrinterCmd createSelectCharSetCmd(byte charSet) {
         return new IBM4610PrinterCmd.SelectCharSetCmd(this, "SelectCharSetCmd", charSet);
      }

      public POSPrinterCmd createSetPrintStationSettingCmd(byte command) {
         return new IBM4610PrinterCmd.SetPrintStationSettingCmd(this, "SetPrintStationSettingCmd", command);
      }

      public POSPrinterCmd createUseUserDefinedCodePageCmd(byte station, byte value) {
         return new IBM4610PrinterCmd.UseUserDefinedCodePageCmd(this, "UseUserDefinedCodePageCmd", station, value);
      }

      public POSPrinterCmd createXonXoffModeCmd(boolean transparent) {
         return new IBM4610PrinterCmd.XonXoffModeCmd(this, "XonXoffModeCmd", transparent);
      }

      public POSPrinterCmd createBeeperCmd(boolean on, boolean timed, int duration, int frequency, int volume) {
         return new IBM4610PrinterCmd.BeeperCmd(this, "BeeperCmd", on, timed, duration, frequency, volume);
      }

      public POSPrinterCmd createStatusSentCmd(
         boolean buffEmpty, boolean frontDIChange, boolean topDIChange, boolean lineCnt, boolean cdChange, boolean keyChange, boolean coverOpen
      ) {
         return new IBM4610PrinterCmd.StatusSentCmd(this, "StatusSentCmd", buffEmpty, frontDIChange, topDIChange, lineCnt, cdChange, keyChange, coverOpen);
      }

      public POSPrinterCmd createSolicitStatusCmd(boolean enable) {
         return new IBM4610PrinterCmd.SolicitStatusCmd(this, "SolicitStatusCmd", enable);
      }

      public POSPrinterCmd createPageModeCmd(boolean enable) {
         return new IBM4610PrinterCmd.PageModeCmd(this, "PageModeCmd", enable);
      }

      public POSPrinterCmd createPrintPageModePageCmd(boolean clear, boolean enable) {
         return new IBM4610PrinterCmd.PrintPageModePageCmd(this, "PrintPageModePageCmd", clear, enable);
      }

      public POSPrinterCmd createPMPositioningCmd(byte position) {
         return new IBM4610PrinterCmd.PMPositioningCmd(this, "PMPositioningCmd", position);
      }

      public POSPrinterCmd createPMPageDefineCmd(int x, int y, int dx, int dy) {
         return new IBM4610PrinterCmd.PMPageDefineCmd(this, "PMPageDefineCmd", x, y, dx, dy);
      }

      public POSPrinterCmd createPageModeNormalCmd(boolean enable) {
         return new IBM4610PrinterCmd.PMPageModeNormalCmd(this, "PMPageModeNormalCmd", enable);
      }

      public POSPrinterCmd createHorizontalPositionCmd(int hPosition, int vPosition) {
         return new IBM4610PrinterCmd.HorizontalPositionCmd(this, "HorizontalPositionCmd", hPosition, vPosition);
      }

      public POSPrinterCmd createVerticalPositionCmd(int hPosition, int vPosition) {
         return new IBM4610PrinterCmd.VerticalPositionCmd(this, "VerticalPositionCmd", hPosition, vPosition);
      }

      public POSPrinterCmd createClearPageModeCmd() {
         return new IBM4610PrinterCmd.ClearPageModeCmd(this, "ClearPageModeCmd");
      }

      public POSPrinterCmd createStartScanCmd(byte mode) {
         return new IBM4610PrinterCmd.StartScanCmd(this, "StartScanCmd", (byte)4, mode);
      }

      public POSPrinterCmd createPrintScannedImgCmd(short loc, int top_left, int offset, short scale, boolean option) {
         return new IBM4610PrinterCmd.PrintScannedImgCmd(this, "PrintScannedImgCmd", loc, top_left, offset, scale, option);
      }

      public POSPrinterCmd createScannerCalibCmd() {
         return new IBM4610PrinterCmd.ScannerCalibCmd(this, "ScannerCalibCmd", (byte)4);
      }

      public POSPrinterCmd createSelCompressionFormatCmd(byte format, byte contrast) {
         return new IBM4610PrinterCmd.SelCompressionFormatCmd(this, "SelCompressionFormatCmd", format, contrast);
      }

      public POSPrinterCmd createGetScannedImgCmd(short location, int offset, short numBytes) {
         return new IBM4610PrinterCmd.GetScannedImgCmd(this, "GetScannedImgCmd", location, offset, numBytes);
      }

      public POSPrinterCmd createStoreScannedImgCmd(byte method, int top_left, int offset, int[] top_left_sub, int[] offset_sub, String tag) {
         return new IBM4610PrinterCmd.StoreScannedImgCmd(this, "StoreScannedImg", method, top_left, offset, top_left_sub, offset_sub, tag);
      }

      public POSPrinterCmd createStoreScannedImgCmd(byte method, int top_left, int offset, String tag) {
         return new IBM4610PrinterCmd.StoreScannedImgCmd(this, "StoreScannedImg", method, top_left, offset, tag);
      }

      public POSPrinterCmd createGetFirstUnreadImgLocCmd() {
         return new IBM4610PrinterCmd.GetFirstUnreadImgLocCmd(this, "GetFirstUnreadImgLocCmd");
      }

      public POSPrinterCmd createGetNextImgLocCmd() {
         return new IBM4610PrinterCmd.GetNextImgLocCmd(this, "GetNextImgLocCmd");
      }

      public POSPrinterCmd createHoldBufferCmd() {
         return new IBM4610PrinterCmd.HoldBufferCmd(this, "HoldBufferCmd");
      }
   }

   public static class FlashTestCmd extends IBM4610PrinterCmd {
      public FlashTestCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      protected PrintCmd buildPrintCmd() {
         return null;
      }
   }

   public static class GetFirstUnreadImgLocCmd extends IBM4610PrinterCmd implements POSPrinterCmd.GetFirstUnreadImgLocCmd, POSPrinterCmd.DataRequesterCmd {
      ByteBuffer requestedData;

      GetFirstUnreadImgLocCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Print4610CmdFactory)this.factory.getPrintCmdFactory()).getCheckScannerFactory().createGetFirstUnreadImgLocCmd(this);
      }

      public void accept(IBM4610PrinterCmdVisitor visitor) {
         visitor.visitDataRequesterCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitDataRequesterCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitDataRequesterCmd(this);
      }

      public ByteBuffer getRequestData() {
         return this.requestedData;
      }

      public void setRequestedData(ByteBuffer d) {
         this.requestedData = d;
      }
   }

   public static class GetNextImgLocCmd extends IBM4610PrinterCmd implements POSPrinterCmd.GetNextImgLocCmd, POSPrinterCmd.DataRequesterCmd {
      ByteBuffer requestedData;

      GetNextImgLocCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Print4610CmdFactory)this.factory.getPrintCmdFactory()).getCheckScannerFactory().createGetNextImgLocCmd(this);
      }

      public void accept(IBM4610PrinterCmdVisitor visitor) {
         visitor.visitDataRequesterCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitDataRequesterCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitDataRequesterCmd(this);
      }

      public ByteBuffer getRequestData() {
         return this.requestedData;
      }

      public void setRequestedData(ByteBuffer d) {
         this.requestedData = d;
      }
   }

   public static class GetScannedImgCmd extends IBM4610PrinterCmd implements POSPrinterCmd.GetScannedImgCmd, POSPrinterCmd.DataRequesterCmd {
      ByteBuffer requestedData;
      private short location;
      private int offset;
      private short numBytes;

      GetScannedImgCmd(HandleCmd.Factory factory, String name, short loc, int from, short num) {
         super(factory, name);
         this.location = loc;
         this.offset = from;
         this.numBytes = num;
      }

      public short getLocation() {
         return this.location;
      }

      public int getOffset() {
         return this.offset;
      }

      public short getNumBytes() {
         return this.numBytes;
      }

      public void accept(IBM4610PrinterCmdVisitor visitor) {
         visitor.visitDataRequesterCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitDataRequesterCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitDataRequesterCmd(this);
      }

      public ByteBuffer getRequestData() {
         return this.requestedData;
      }

      public void setRequestedData(ByteBuffer d) {
         this.requestedData = d;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Print4610CmdFactory)this.factory.getPrintCmdFactory()).getCheckScannerFactory().createGetScannedImgCmd(this);
      }
   }

   public static class HeadMovementCmd extends DefaultPOSPrinterCmd.ByteStoringCmd {
      HeadMovementCmd(HandleCmd.Factory factory, String name, byte position) {
         super(factory, name, (byte)2, position);
      }

      public byte getPosition() {
         return this.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createHeadMovementCmd(this);
      }
   }

   public static class HoldBufferCmd extends IBM4610PrinterCmd {
      public HoldBufferCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createHoldBufferCmd(this);
      }
   }

   public static class HorizontalPositionCmd extends IBM4610PrinterCmd {
      int hPosition;
      int vPosition;

      public HorizontalPositionCmd(HandleCmd.Factory factory, String name, int hPosition, int vPosition) {
         super(factory, name, (byte)2);
         this.hPosition = hPosition;
         this.vPosition = vPosition;
      }

      public int getHorizontalPosition() {
         return this.hPosition;
      }

      public int getVerticalPosition() {
         return this.vPosition;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4610CmdFactory)this.factory.getGraphicCmdFactory()).createHorizontalPositionCmd(this);
      }
   }

   public static class OpenDrawerCmd extends DefaultPOSPrinterCmd.ByteStoringCmd {
      byte widthOff = 0;
      byte widthOn = 0;
      byte cdNumber = 0;
      public static final byte CD1 = 0;
      public static final byte CD2 = 1;

      public OpenDrawerCmd(HandleCmd.Factory factory, String name, byte cdNo, byte widthOn, byte widthOff) {
         super(factory, name, (byte)0, cdNo);
         this.widthOn = widthOn;
         this.widthOff = widthOff;
         this.cdNumber = cdNo;
      }

      public byte getCDNumber() {
         if (this.cdNumber == 0) {
            return 0;
         } else {
            return this.cdNumber == 1 ? 1 : this.cdNumber;
         }
      }

      public byte getWidthOff() {
         return this.widthOff;
      }

      public byte getWidthOn() {
         return this.widthOn;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createOpenDrawerCmd(this);
      }
   }

   public static class PMPageDefineCmd extends IBM4610PrinterCmd {
      int x;
      int y;
      int dx;
      int dy;

      public PMPageDefineCmd(HandleCmd.Factory factory, String name, int x, int y, int dx, int dy) {
         super(factory, name, (byte)2);
         this.x = x;
         this.y = y;
         this.dx = dx;
         this.dy = dy;
      }

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }

      public int getDX() {
         return this.dx;
      }

      public int getDY() {
         return this.dy;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4610CmdFactory)this.factory.getGraphicCmdFactory()).createPMPageDefineCmd(this);
      }
   }

   public static class PMPageModeNormalCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd {
      public PMPageModeNormalCmd(HandleCmd.Factory factory, String name, boolean enable) {
         super(factory, name, enable);
         this.setOutputCmd();
      }

      public boolean enabled() {
         return this.getBoolean();
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4610CmdFactory)this.factory.getGraphicCmdFactory()).createPageModeNormalCmd(this);
      }

      public void accept(IBM4610PrinterCmdVisitor visitor) {
         visitor.visitPageModeNormalCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitPageModeNormalCmd(this);
      }
   }

   public static class PMPositioningCmd extends DefaultPOSPrinterCmd.ByteStoringCmd {
      public PMPositioningCmd(HandleCmd.Factory factory, String name, byte position) {
         super(factory, name, (byte)2, position);
      }

      public byte getPosition() {
         return super.getNumber();
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4610CmdFactory)this.factory.getGraphicCmdFactory()).createPMPositioningCmd(this);
      }
   }

   public static class PageModeCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd {
      public PageModeCmd(HandleCmd.Factory factory, String name, boolean enable) {
         super(factory, name, enable);
      }

      public boolean enabled() {
         return this.getBoolean();
      }

      public boolean isOutputCmd() {
         return false;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4610CmdFactory)this.factory.getGraphicCmdFactory()).createPageModeCmd(this);
      }

      public void accept(IBM4610PrinterCmdVisitor visitor) {
         visitor.visitPageModeCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitPageModeCmd(this);
      }
   }

   public static class PrintPageModePageCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd {
      boolean enable = false;

      public PrintPageModePageCmd(HandleCmd.Factory factory, String name, boolean clear, boolean enablePM) {
         super(factory, name, (byte)2, clear, (byte)0);
         this.setOutputCmd();
         this.enable = enablePM;
      }

      public boolean getClear() {
         return super.getBoolean();
      }

      public boolean getEnable() {
         return this.enable;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4610CmdFactory)this.factory.getGraphicCmdFactory()).createPrintPageModePageCmd(this);
      }

      public void accept(IBM4610PrinterCmdVisitor visitor) {
         visitor.visitPrintPageModePageCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitPrintPageModePageCmd(this);
      }
   }

   public static class PrintScannedImgCmd extends IBM4610PrinterCmd implements POSPrinterCmd.PrintScannedImgCmd {
      private short location;
      private int corners;
      private int offset;
      private short scale;
      private boolean rotate;

      PrintScannedImgCmd(HandleCmd.Factory factory, String name, short loc, int top_left, int offset, short scale, boolean option) {
         super(factory, name);
         this.location = loc;
         this.corners = top_left;
         this.offset = offset;
         this.scale = scale;
         this.rotate = option;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Print4610CmdFactory)this.factory.getPrintCmdFactory()).getCheckScannerFactory().createPrintScannedImgCmd(this);
      }

      public short getLocation() {
         return this.location;
      }

      public int getCorners() {
         return this.corners;
      }

      public int getOffset() {
         return this.offset;
      }

      public short getScale() {
         return this.scale;
      }

      public boolean rotateImage() {
         return this.rotate;
      }
   }

   public static class ReinitPrinterCmd extends IBM4610PrinterCmd {
      public ReinitPrinterCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createReinitPrinterCmd(this);
      }
   }

   public static class ReleasePrintBufferCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd {
      public ReleasePrintBufferCmd(HandleCmd.Factory factory, String name, boolean cancel) {
         super(factory, name, cancel);
      }

      public boolean isCancelCmd() {
         return super.getBoolean();
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createReleasePrintBufferCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         if (visitor instanceof IBM4610PrinterCmdVisitor) {
            ((IBM4610PrinterCmdVisitor)visitor).visitReleasePrintBufferCmd(this);
         }
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitReleasePrintBufferCmd(this);
      }
   }

   public static class ResetLineCntCmd extends IBM4610PrinterCmd {
      public ResetLineCntCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createResetLineCntCmd(this);
      }
   }

   public static class ScannerCalibCmd extends IBM4610PrinterCmd implements POSPrinterCmd.ScannerCalibCmd {
      ScannerCalibCmd(HandleCmd.Factory factory, String name, byte station) {
         super(factory, name, station);
      }

      protected PrintCmd buildPrintCmd() {
         return ((Print4610CmdFactory)this.factory.getPrintCmdFactory()).getCheckScannerFactory().createScannerCalibCmd(this);
      }
   }

   public static class SelCompressionFormatCmd extends IBM4610PrinterCmd implements POSPrinterCmd.SelCompressionFormatCmd {
      private byte format;
      private byte contrast;

      SelCompressionFormatCmd(HandleCmd.Factory factory, String name, byte frmt, byte ctrt) {
         super(factory, name);
         this.format = frmt;
         this.contrast = ctrt;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Print4610CmdFactory)this.factory.getPrintCmdFactory()).getCheckScannerFactory().createSelCompressionFormatCmd(this);
      }

      public byte getCompressionFormat() {
         return this.format;
      }

      public byte getContrast() {
         return this.contrast;
      }
   }

   public static class SelectCharSetCmd extends IBM4610PrinterCmd {
      private byte number;

      SelectCharSetCmd(HandleCmd.Factory factory, String name, byte charSet) {
         super(factory, name);
         this.number = charSet;
      }

      public byte getSelectedCharSet() {
         return this.number;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Font4610CmdFactory)this.factory.getFontCmdFactory()).createSelectCharSetCmd(this);
      }
   }

   public static class SetPrintStationSettingCmd extends IBM4610PrinterCmd {
      private byte printerStation;

      SetPrintStationSettingCmd(HandleCmd.Factory factory, String name, byte station) {
         super(factory, name);
         this.printerStation = station;
      }

      public byte getStation() {
         return this.printerStation;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Font4610CmdFactory)this.factory.getFontCmdFactory()).createSetPrintStationSettingCmd(this);
      }
   }

   public static class SolicitStatusCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd {
      public SolicitStatusCmd(HandleCmd.Factory f, String name, boolean enable) {
         super(f, name, enable);
      }

      public boolean enable() {
         return super.getBoolean();
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createSolicitStatusCmd(this);
      }
   }

   public static class StartScanCmd extends IBM4610PrinterCmd implements POSPrinterCmd.StartScanCmd, POSPrinterCmd.DataRequesterCmd {
      ByteBuffer requestedData;
      private boolean isScanAndMicrRead;
      private byte mode;

      StartScanCmd(HandleCmd.Factory factory, String name, byte station, byte mode) {
         super(factory, name, station);
         switch (mode) {
            case 1:
            case 3:
            case 4:
               this.isScanAndMicrRead = true;
               break;
            case 2:
            default:
               this.isScanAndMicrRead = false;
         }

         this.mode = mode;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Print4610CmdFactory)this.factory.getPrintCmdFactory()).getCheckScannerFactory().createStartScanCmd(this);
      }

      public void clearScanRead() {
         this.isScanAndMicrRead = false;
      }

      public boolean isScanAndMicrRead() {
         return this.isScanAndMicrRead;
      }

      public byte getMode() {
         return this.mode;
      }

      public void accept(IBM4610PrinterCmdVisitor visitor) {
         visitor.visitStartScanCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitStartScanCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitStartScanCmd(this);
      }

      public ByteBuffer getRequestData() {
         return this.requestedData;
      }

      public void setRequestedData(ByteBuffer d) {
         this.requestedData = d;
      }
   }

   public static class StationSettingsCmd extends IBM4610PrinterCmd {
      public StationSettingsCmd(HandleCmd.Factory factory, String name, byte station) {
         super(factory, name, station);
      }

      protected PrintCmd buildPrintCmd() {
         return null;
      }
   }

   public static class StatusSentCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd {
      boolean frontDI = true;
      boolean topDI = true;
      boolean lineCnt = true;
      boolean cdChange = true;
      boolean keyChange = true;
      boolean coverOpen = true;

      public StatusSentCmd(
         HandleCmd.Factory f,
         String name,
         boolean buffEmpty,
         boolean frontDIChange,
         boolean topDIChange,
         boolean lineCnt,
         boolean cdChange,
         boolean keyChange,
         boolean coverOpen
      ) {
         super(f, name, buffEmpty);
         this.frontDI = frontDIChange;
         this.topDI = topDIChange;
         this.lineCnt = lineCnt;
         this.cdChange = cdChange;
         this.keyChange = keyChange;
         this.coverOpen = coverOpen;
      }

      public boolean isBuffEmptyOn() {
         return this.getBoolean();
      }

      public boolean isFrontDIOn() {
         return this.frontDI;
      }

      public boolean isTopDIOn() {
         return this.topDI;
      }

      public boolean isLineCntOn() {
         return this.lineCnt;
      }

      public boolean isCDChangeOn() {
         return this.cdChange;
      }

      public boolean isKeyChangeOn() {
         return this.keyChange;
      }

      public boolean isCoverOpenOn() {
         return this.coverOpen;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createStatusSentCmd(this);
      }
   }

   public static class StoreScannedImgCmd extends IBM4610PrinterCmd implements POSPrinterCmd.StoreScannedImgCmd {
      ByteBuffer requestedData;
      private byte storageMethod = 0;
      private int corner = 0;
      private int offset = 0;
      private int[] cornerSub = new int[3];
      private int[] offsetSub = new int[3];
      private String tagData = null;

      StoreScannedImgCmd(HandleCmd.Factory factory, String name, byte method, int top_left, int offset, int[] top_left_sub, int[] offset_sub, String tag) {
         super(factory, name);
         this.storageMethod = method;
         this.corner = top_left;
         this.offset = offset;
         this.cornerSub = top_left_sub;
         this.offsetSub = offset_sub;
         this.tagData = tag;
      }

      StoreScannedImgCmd(HandleCmd.Factory factory, String name, byte method, int top_left, int offset, String tag) {
         super(factory, name);
         this.storageMethod = method;
         this.corner = top_left;
         this.offset = offset;
         this.tagData = tag;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Print4610CmdFactory)this.factory.getPrintCmdFactory()).getCheckScannerFactory().createStoreScannedImgCmd(this);
      }

      public byte getStorageMethod() {
         return this.storageMethod;
      }

      public int getCorner() {
         return this.corner;
      }

      public int getOffset() {
         return this.offset;
      }

      public int[] getCornerSub() {
         return this.cornerSub;
      }

      public int[] getOffsetSub() {
         return this.offsetSub;
      }

      public String getTagData() {
         return this.tagData;
      }

      public void accept(IBM4610PrinterCmdVisitor visitor) {
         visitor.visitStoreScannedImageCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitStoreScannedImageCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBM4610PrinterCmdVisitor)visitor).visitStoreScannedImageCmd(this);
      }

      public ByteBuffer getRequestData() {
         return this.requestedData;
      }

      public void setRequestedData(ByteBuffer d) {
         this.requestedData = d;
      }
   }

   public static class UseUserDefinedCodePageCmd extends IBM4610PrinterCmd {
      private byte value;

      UseUserDefinedCodePageCmd(HandleCmd.Factory factory, String name, byte station, byte value) {
         super(factory, name, station);
         this.value = value;
      }

      public byte getValue() {
         return this.value;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Font4610CmdFactory)this.factory.getFontCmdFactory()).createUseUserDefinedCodePageCmd(this);
      }
   }

   public static class VerticalPositionCmd extends IBM4610PrinterCmd {
      int hPosition;
      int vPosition;

      public VerticalPositionCmd(HandleCmd.Factory factory, String name, int hPosition, int vPosition) {
         super(factory, name, (byte)2);
         this.hPosition = hPosition;
         this.vPosition = vPosition;
      }

      public int getHorizontalPosition() {
         return this.hPosition;
      }

      public int getVerticalPosition() {
         return this.vPosition;
      }

      protected PrintCmd buildPrintCmd() {
         return ((Grap4610CmdFactory)this.factory.getGraphicCmdFactory()).createVerticalPositionCmd(this);
      }
   }

   public static class XonXoffModeCmd extends DefaultPOSPrinterCmd.BooleanStoringCmd {
      public XonXoffModeCmd(HandleCmd.Factory factory, String name, boolean transparent) {
         super(factory, name, (byte)-1, transparent, (byte)0);
      }

      public boolean isTransparent() {
         return super.getBoolean();
      }

      protected PrintCmd buildPrintCmd() {
         return ((Gen4610CmdFactory)this.factory.getGeneralCmdFactory()).createXonXoffModeCmd(this);
      }
   }
}
