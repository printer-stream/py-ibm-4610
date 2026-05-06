package com.ibm.posj.printer.sureone;

import com.ibm.jutil.logging.LogHelper;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleCmdVisitor;
import com.ibm.posj.HandleException;
import com.ibm.posj.IBMSureonePrinterCmd;
import com.ibm.posj.IBMSureonePrinterCmdConst;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;
import com.ibm.posj.printer.AbstractCmdCompleteVisitor;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.IBMPrinterState;
import com.ibm.posj.printer.ImageStreamProducer;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.parser.PrinterParser;
import com.ibm.posj.printer.parser.sureone.PrinterSureoneParser;
import com.ibm.posj.util.DefaultHandleCmdV;
import com.ibm.posj.util.IBMSureonePrinterCmdV;

public class IBMSureoneImp extends IBMPrinterImp {
   private static Tracer tracer = TracerFactory.getInstance().createTracer("ISureoneImp");
   private HandleCmdVisitor chaseVisitor;
   private boolean inSetLogo = false;
   private AbstractCmdCompleteVisitor ccVisitor = null;
   private IBMSureoneImp.StationVisitor stationV = new IBMSureoneImp.StationVisitor(this.getPrinterState(), this.getHandle());
   private boolean newCmdSet = true;
   private byte lastStation = 2;
   protected static final int CONTINUATION_LENGTH = 2;
   public String WRITER_MINIMUM = "posj.IBMSureoneWriter.minimum";

   public IBMSureoneImp(PrinterWriter mainWriter, DefaultPOSPrinterHandle handle, LogHelper logger) {
      super(mainWriter, handle, logger);
   }

   public void clearOutput() {
      this.stationV.clearOutput();
      super.clearOutput();
   }

   public String getSerialNumber() {
      return "";
   }

   public void receivePrintDataEvent(PrintDataEvent pde) {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("receivePrintDataEvent printimp " + pde.getType());
      }
   }

   public int getMaxCmdLen() {
      return this.inSetLogo ? this.writer.getMaxCmdLen() - 2 : super.getMaxCmdLen();
   }

   public IBMPrinterState getPrinterState() {
      return (IBMPrinterState)this.getPrinterHandleState();
   }

   public void submit(HandleCmd cmd1) {
      cmd1.accept(this.getChaseVisitor());
      super.submit(cmd1);
   }

   public AbstractCmdCompleteVisitor getCmdCompleteVisitor() {
      if (null == this.ccVisitor) {
         this.ccVisitor = new CmdCompleteSOVisitor((IBMPrinterState)this.getPrinterHandleState());
      }

      return this.ccVisitor;
   }

   public void busException(Object e) {
   }

   protected String getMinimumPropertyText() {
      return this.WRITER_MINIMUM;
   }

   protected HandleCmdVisitor getChaseVisitor() {
      if (null == this.chaseVisitor) {
         this.chaseVisitor = new IBMSureoneImp.ChaseVisitor();
      }

      return this.chaseVisitor;
   }

   protected boolean addToOutgoingList(PrintCmd cmd) {
      if (this.isClearOutput()) {
         return false;
      } else if (!this.getPendingList().addCommand(cmd)) {
         PrintCmd pre = null;
         this.writeSubmit();
         this.preAddToCmdList(cmd);
         boolean added = this.getPendingList().addCommand(cmd);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("cmd added 2nd time " + added);
         }

         if (added && this.getTracer().isOn()) {
            this.getHandle().timeStamp().addToCmdList(cmd, this.getPendingList());
         }

         return added;
      } else {
         this.getHandle().timeStamp().addToCmdList(cmd, this.getPendingList());
         return true;
      }
   }

   protected void preAddToCmdList(PrintCmd cmd) {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println(">>preAdd2CmdList PrintCmdList" + this.getPendingList().hashCode());
      }

      if (!this.isClearOutput()) {
         byte cst = cmd.getHandleCmd().getStation();
         cmd.getHandleCmd().accept(this.stationV);
         if ((this.newCmdSet || cst != this.lastStation) && 0 != cst && -1 != cst) {
            DefaultPOSPrinterCmd c = (DefaultPOSPrinterCmd)cmd.getHandleCmd();
            if (!c.getOwnerCmd().isMemoryCmd()) {
               c = (DefaultPOSPrinterCmd)c.getStationCmd();
               c.getPrintCmd().setParent(this);
               this.lastStation = cst;
            }
         }

         this.newCmdSet = false;
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("<<preAdd2CmdList PrintCmdList" + this.getPendingList().hashCode());
         }
      }
   }

   protected void addToPrintCmdList(PrintCmd cmd) {
      cmd.setParent(this);
      if (!cmd.waitToFinish() && !cmd.waitToStart()) {
         this.preAddToCmdList(cmd);
         if (this.addToOutgoingList(cmd)) {
            ;
         }
      } else {
         if (cmd.waitToFinish()) {
            cmd.setBlockKey(this.inputQ.getBlockKey());
         }

         if (this.getPendingList().hasCommands()) {
            this.writeSubmit();
         }

         this.writeSubmit();
      }
   }

   protected void submitList(DefaultPOSPrinterCmd dpc) {
      dpc.accept(this.stationV);
      if (this.stationV.isSetLogoCmd()) {
         this.inSetLogo = true;
      }

      super.submitList(dpc);
      this.inSetLogo = false;
   }

   protected void preSubmit(PrintCmdList l) {
      if (l.hasCommands()) {
         ;
      }
   }

   protected PrinterHandleState createPrinterHandleState() {
      return new IBMSureoneImp.SureonePrinterState(this.logger, this.getHandle());
   }

   protected Tracer getTracer() {
      return tracer;
   }

   class ChaseVisitor extends DefaultHandleCmdV {
      public void visitPOSPrinterCmd(POSPrinterCmd cmd) {
      }
   }

   class StationVisitor extends IBMSureonePrinterCmdV {
      private boolean rotation = false;
      private boolean flag = true;
      private boolean flagSetLogo = false;
      private boolean ccFlag = false;
      private boolean endCmd = false;
      private IBMSureoneImp.SureonePrinterState state = null;
      private DefaultPOSPrinterHandle handle = null;

      public StationVisitor(IBMPrinterState state, DefaultPOSPrinterHandle handle) {
         this.state = (IBMSureoneImp.SureonePrinterState)state;
         this.handle = handle;
      }

      public boolean appendStationSettings() {
         return this.flagSetLogo ? false : this.flag;
      }

      public void clearOutput() {
         this.rotation = false;
         this.flag = true;
         this.flagSetLogo = false;
         this.ccFlag = false;
         this.endCmd = false;
      }

      public boolean isSetLogoCmd() {
         return this.flagSetLogo;
      }

      public boolean isEndSetLogoCmd() {
         return this.endCmd;
      }

      public boolean isContinuationCmd() {
         if (this.ccFlag) {
            this.ccFlag = false;
            return true;
         } else {
            return false;
         }
      }

      public void resetLogo() {
         this.flagSetLogo = false;
      }

      public void visit(POSPrinterCmd x) {
         this.flag = true;
         this.flagSetLogo = this.endCmd = this.ccFlag = false;
      }

      public void visitSetLogoCmd(POSPrinterCmd.SetLogoCmd setLogoCmd) {
         this.state.setSetLogoCmd(setLogoCmd);
         this.visit(setLogoCmd);
         if (100 != setLogoCmd.getLocation()) {
            this.flagSetLogo = true;
         } else {
            this.endCmd = true;
            this.flagSetLogo = false;
         }
      }

      public void visitSetBitmapCmd(POSPrinterCmd.SetBitmapCmd setBitmapCmd) {
         this.state.setSetBitmapCmd(setBitmapCmd);
         this.visit(setBitmapCmd);
         if (setBitmapCmd.getBitmapNo() == 1) {
            this.flagSetLogo = true;
         } else {
            this.endCmd = true;
            this.flagSetLogo = false;
         }
      }

      public void visitPrintSetBitmapCmd(POSPrinterCmd.PrintSetBitmapCmd ptrSetBitmapCmd) {
         this.visit(ptrSetBitmapCmd);
         IBMSureonePrinterCmd.Factory pf = (IBMSureonePrinterCmd.Factory)this.handle.getPOSPrinterCmdFactory();
         byte location = ptrSetBitmapCmd.getBitmapNo();
         POSPrinterCmd cmd = this.state.getSetBitmapCmd(location, ptrSetBitmapCmd.getStation());
      }

      public void visitPrintSetLogoCmd(POSPrinterCmd.PrintSetLogoCmd ptrSetLogoCmd) {
         this.visit(ptrSetLogoCmd);
         String data = null;
         byte location = ptrSetLogoCmd.getLocation();
         if (location == 1) {
            data = this.state.getSetLogoCmd((byte)1).getData();
         } else {
            data = this.state.getSetLogoCmd((byte)2).getData();
         }

         try {
            PrinterParser parser = this.state.getParser();
            parser.parseData(ptrSetLogoCmd.getStation(), data);
            POSPrinterCmd cmd = parser.getMasterPrinterCmd();
            this.handle.submit(cmd);
            cmd.waitUntilCompleted(30L);
         } catch (HandleException var6) {
         }
      }

      public void visitRotatePrintCmd(POSPrinterCmd.RotatePrintCmd rpc) {
         int rot = rpc.getRotation();
         switch (rot) {
            case 0:
            case 90:
            case 270:
            default:
               this.rotation = false;
               break;
            case 180:
               this.rotation = true;
         }
      }
   }

   protected class SureonePrinterState extends IBMPrinterState {
      private POSPrinterHandle handle;
      byte recCharWidth = 1;
      int spacingWidth = 0;
      ImageStreamProducer streamProducer = null;
      protected boolean inError = false;
      PrinterParser printerParser = null;
      protected final int[] PRT_BC_TABLE_TEXTPOSITION = new int[]{-11, -13};
      protected final int PRT_REC_BC_MIN_VER_SIZE = 1;
      protected final int PRT_REC_BC_MAX_VER_SIZE = 255;
      protected final int PRT_REC_BC_MIN_HOR_SIZE = 2;
      protected final int PRT_REC_BC_MAX_HOR_SIZE = 4;
      protected final int[] PRT_TABLE_ALIGNMENT = new int[]{-1, -2, -3};
      protected static final int PRT_REC_DOTS_PER_INCH_WIDE = 203;
      protected static final int PRT_REC_DOTS_PER_INCH_HIGH = 223;
      protected static final int PRT_REC_LINE_WIDTH = 576;
      protected static final int PRT_REC_LINE_NARROW_WIDTH = 400;
      protected static final int PRT_REC_BC_MAX_HEIGHT = 255;
      protected static final int PRT_REC_MAX_PRT_BMP_SIZE = 4000;
      protected static final int PRT_REC_BMP_MAX_HEIGHT = 2040;
      protected static final byte DEFAULT_REC_CHAR_WIDTH = 10;
      protected static final byte DEFAULT_REC_SPACING_WIDTH = 3;
      protected static final int DEFAULT_REC_SIDEWAYS_LINE_WIDTH = 794;
      protected static final boolean DEFAULT_CAP_REC_PARTIAL_PRT = false;
      protected static final int DEFAULT_REC_STEPS_PER_INCH = 203;

      SureonePrinterState(LogHelper logger, POSPrinterHandle handle) {
         super(logger);
         this.setPrinterID(3836);
         this.setPrinterType(3804);
         this.fontTable = IBMSureonePrinterCmdConst.FONT_TYPE_TABLE;
         this.alignmentList = this.PRT_TABLE_ALIGNMENT;
         this.textPositionList = this.PRT_BC_TABLE_TEXTPOSITION;
         this.recBarCodeMaxHeight = 255;
         this.recBarCodeMaxWidth = 576;
         this.recBarCodeMinHorSize = 2;
         this.recBarCodeMaxHorSize = 4;
         this.setMaxPrintBitmapSize((byte)2, 4000);
         this.recBitmapMaxHeight = 2040;
         this.setDefaults();
         this.setPrinterCapabilities();
         this.setPrinterParserParamMaxValues();
         this.handle = handle;
      }

      private void setDefaults() {
         this.recCharWidth = 10;
         this.spacingWidth = 3;
         this.setCharHeight((byte)2, (byte)24);
         this.setCharWidth((byte)2, (byte)12);
         this.setStationWidth((byte)2, 576);
         this.capRecPartialLine = false;
         this.recFeedStepsPerInch = 203;
         this.recSidewaysLineWidth = 794;
      }

      private void setPrinterCapabilities() {
         this.capCoverSensor = true;
         this.capTransaction = true;
         this.capRecBarCode = true;
         this.capRecBitmap = true;
         this.capRecDwideDhigh = true;
         this.capRecEmptySensor = true;
         this.capRecPapercut = true;
         this.capRecRotate180 = true;
         this.capRecStamp = true;
         this.setBitSet(1, true, this.recbools);
         this.setBitSet(2, true, this.recbools);
         this.setBitSet(3, true, this.recbools);
         this.setBitSet(0, true, this.recbools);
         this.setBitSet(7, true, this.recbools);
         this.setBitSet(6, true, this.recbools);
         this.setBitSet(5, true, this.recbools);
      }

      private void setPrinterParserParamMaxValues() {
         this.recMaxBitmapNumber = 20;
         this.recMaxLogoNumber = 20;
         this.recMaxColorNumber = 1;
         this.recMaxScaleHorizontalNumber = 5;
         this.recMaxScaleVerticalNumber = 5;
         this.recMaxLinesToFeed = 255;
         this.recMaxUnderlineNumber = 255;
      }

      public PrinterParser getParser() {
         if (this.printerParser == null) {
            this.printerParser = new PrinterSureoneParser(this.handle.getPOSPrinterCmdFactory(), this);
         }

         return this.printerParser;
      }

      public int getMicronsPerStep(byte station) {
         int aret = 1;
         switch (station) {
            case 2:
               aret = 125;
            default:
               return aret;
         }
      }

      public int getDotsPerInchWide(byte station) {
         int dpi = 0;
         switch (station) {
            case 2:
               dpi = 203;
            default:
               return dpi;
         }
      }

      public int getDotsPerInchHigh(byte station) {
         int dpi = 0;
         switch (station) {
            case 2:
               dpi = 223;
            default:
               return dpi;
         }
      }

      public ImageStreamProducer getImageStreamProducer() {
         if (this.streamProducer == null) {
            this.streamProducer = new SureoneImageStreamProducer();
         }

         return this.streamProducer;
      }

      public boolean errorPending() {
         return this.inError;
      }

      public int getColorCapability(byte station) {
         return 0;
      }

      public boolean stationPresent(byte station) {
         switch (station) {
            case 2:
               return true;
            default:
               return false;
         }
      }

      public byte getCharWidth(byte station) {
         byte ret = 0;
         switch (station) {
            case 2:
               ret = this.recCharWidth;
            default:
               return ret;
         }
      }

      public void setCharWidth(byte station, byte dots) {
         switch (station) {
            case 2:
               this.recCharWidth = dots;
         }
      }

      public int getSpacingWidth(byte station) {
         return this.spacingWidth;
      }

      public void setSpacingWidth(byte station, int dots) {
         this.spacingWidth = dots;
      }
   }
}
