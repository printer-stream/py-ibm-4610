package com.ibm.posj.printer.ibm4689;

import com.ibm.jutil.Util;
import com.ibm.jutil.logging.LogHelper;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.GeneralPOSPrinterCmd;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleCmdVisitor;
import com.ibm.posj.HandleException;
import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.IBM4689PrinterCmdConst;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;
import com.ibm.posj.bus.printer.cmds.ibm4689.Print4689CmdFactory;
import com.ibm.posj.printer.AbstractCmdCompleteVisitor;
import com.ibm.posj.printer.CmdAdjuster;
import com.ibm.posj.printer.Default4689CmdAdjuster;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.IBMPrinterState;
import com.ibm.posj.printer.ImageStreamProducer;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.parser.PrinterParser;
import com.ibm.posj.printer.parser.ibm4689.Printer4689Parser;
import com.ibm.posj.util.DefaultHandleCmdV;
import com.ibm.posj.util.IBM4689PrinterCmdV;

public class IBM4689Imp extends IBMPrinterImp {
   private static Tracer tracer = TracerFactory.getInstance().createTracer("I4689Imp");
   private Print4689CmdFactory factory4689 = null;
   private byte cmdCount = 0;
   private HandleCmdVisitor chaseVisitor;
   private boolean newCmdSet = true;
   private AbstractCmdCompleteVisitor ccVisitor = null;
   private IBM4689Imp.StationVisitor stationV = new IBM4689Imp.StationVisitor(this.getPrinterState(), this.getHandle());
   private byte lastStation = 2;
   private CmdAdjuster adjuster = null;
   protected static final int CONTINUATION_LENGTH = 2;
   public String WRITER_MINIMUM = "posj.IBM4689Writer.minimum";

   public IBM4689Imp(PrinterWriter mainWriter, DefaultPOSPrinterHandle handle, LogHelper logger) {
      super(mainWriter, handle, logger);
   }

   public void clearOutput() {
      this.stationV.clearOutput();
      super.clearOutput();
   }

   public String getSerialNumber() {
      return "";
   }

   public boolean adjusted(DefaultPOSPrinterCmd dpc) {
      PrintCmd pc = dpc.getPrintCmd();
      if (pc.getDataSize() > this.getMaxCmdLen() && dpc instanceof GeneralPOSPrinterCmd.PrintNormalCmd) {
         this.adjuster.adjustPOSPrinterCmd(dpc);
         return true;
      } else {
         return false;
      }
   }

   public void setPOSPrintCmdFactory(POSPrinterCmd.Factory factory) {
      super.setPOSPrintCmdFactory(factory);
      this.adjuster = new Default4689CmdAdjuster((DefaultPOSPrinterCmd.Factory)factory, this.writer.getMaxCmdLen(), this.writer);
   }

   protected void submitList(DefaultPOSPrinterCmd dpc) {
      if (this.traceOn()) {
         this.trace("reseting variables.");
      }

      this.newCmdSet = true;
      dpc.accept(this.stationV);
      super.submitList(dpc);
   }

   public void receivePrintDataEvent(PrintDataEvent pde) {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("receivePrintDataEvent printimp " + pde.getType());
      }

      IBMPrinterState ibmState = (IBMPrinterState)this.getPrinterHandleState();
      if ((1 & pde.getType()) > 0) {
         int printerType = pde.byteAt((byte)1) + 2;
         int printerId = pde.byteAt((byte)2);
         this.setPrinterStateId(printerType, printerId);
         if ((pde.byteAt((byte)4) & 1) != 0) {
            ibmState.setDBCSPrinter(false);
         } else {
            ibmState.setDBCSPrinter(true);
         }

         ibmState.setFlipperPresent(false);
         ibmState.setMICRPresent(false);
      }
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
         this.ccVisitor = new CmdComplete4689Visitor((IBMPrinterState)this.getPrinterHandleState());
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
         this.chaseVisitor = new IBM4689Imp.ChaseVisitor();
      }

      return this.chaseVisitor;
   }

   protected boolean addToOutgoingList(PrintCmd cmd) {
      if (this.isClearOutput()) {
         return false;
      } else {
         this.writeSubmit();
         this.cmdCount++;
         this.preAddToCmdList(cmd);
         if (!this.getPendingList().addCommand(cmd)) {
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
   }

   protected void writeSubmit() {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("4689 writeSubmit");
      }

      super.writeSubmit();
      this.cmdCount = 0;
   }

   protected void preAddToCmdList(PrintCmd cmd) {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println(">>preAdd2CmdList PrintCmdList" + Util.toHexString(this.getPendingList().hashCode()));
      }

      if (!this.isClearOutput()) {
         byte cst = cmd.getHandleCmd().getStation();
         cmd.getHandleCmd().accept(this.stationV);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("isContinuation4689Cmd-->" + this.stationV.isContinuation4689Cmd());
         }

         if ((this.newCmdSet || cst != this.lastStation) && 0 != cst && -1 != cst) {
            DefaultPOSPrinterCmd c = (DefaultPOSPrinterCmd)cmd.getHandleCmd();
            if (!c.getOwnerCmd().isMemoryCmd()) {
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
      if (this.traceOn()) {
         this.trace("add2pcl<< " + cmd);
      }

      if (this.traceOn()) {
         this.trace("addpcl size " + cmd.getDataSize());
      }

      cmd.setParent(this);
      if (!cmd.waitToFinish() && !cmd.waitToStart()) {
         this.preAddToCmdList(cmd);
         if (this.addToOutgoingList(cmd)) {
            if (this.traceOn()) {
               this.trace("add2pcl>> " + this.getPendingList() + " " + this.getPendingList().hasCommands());
            }
         }
      } else {
         if (cmd.waitToFinish()) {
            cmd.setBlockKey(this.inputQ.getBlockKey());
         }

         if (this.getPendingList().hasCommands()) {
            this.writeSubmit();
         }

         this.preAddToCmdList(cmd);
         this.addToOutgoingList(cmd);
         this.writeSubmit();
         if (this.traceOn()) {
            this.trace("add2pcl>> early");
         }
      }
   }

   protected void preSubmit(PrintCmdList l) {
      this.newCmdSet = true;
      if (l.hasCommands()) {
         this.lastStation = 0;
      }
   }

   protected PrinterHandleState createPrinterHandleState() {
      return new IBM4689Imp.IBM4689PrinterState(this.logger, this.getHandle());
   }

   private Print4689CmdFactory get4689Factory() {
      if (null == this.factory4689) {
         PrintCmdFactory y = ((DefaultPOSPrinterCmd.Factory)this.getHandle().getHandleCmdFactory()).getPrintCmdFactory();
         this.factory4689 = (Print4689CmdFactory)y;
      }

      return this.factory4689;
   }

   protected Tracer getTracer() {
      return tracer;
   }

   class ChaseVisitor extends DefaultHandleCmdV {
      public void visitPOSPrinterCmd(POSPrinterCmd cmd) {
      }
   }

   protected class IBM4689PrinterState extends IBMPrinterState {
      private POSPrinterHandle handle;
      byte recCharWidth = 1;
      byte jrnCharWidth = 1;
      int spacingWidth = 0;
      ImageStreamProducer streamProducer = null;
      protected boolean inError = false;
      PrinterParser printerParser = null;
      protected final int[] PRT_BC_TABLE_TEXTPOSITION = new int[]{-11, -12, -13};
      protected final int PRT_REC_BC_MIN_VER_SIZE = 1;
      protected final int PRT_REC_BC_MAX_VER_SIZE = 15;
      protected static final int PRT_REC_MAX_PRT_BMP_SIZE = 7732;
      protected static final int PRT_REC_BMP_MAX_HEIGHT = 144;
      protected final int PRT_REC_BC_MIN_HOR_SIZE = 0;
      protected final int PRT_REC_BC_MAX_HOR_SIZE = 1;
      protected final int[] PRT_TABLE_ALIGNMENT = new int[]{-1, -2, -3};
      protected static final int PRT_REC_DOTS_PER_INCH_WIDE = 205;
      protected static final int PRT_JRN_DOTS_PER_INCH_WIDE = 205;
      protected static final int PRT_REC_DOTS_PER_INCH_HIGH = 206;
      protected static final int PRT_JRN_DOTS_PER_INCH_HIGH = 206;
      protected static final int PRT_REC_LINE_WIDTH = 432;
      protected static final int PRT_JRN_LINE_WIDTH = 432;
      protected static final int PRT_REC_BC_MAX_HEIGHT = 255;
      protected static final byte DEFAULT_REC_CHAR_WIDTH = 12;
      protected static final byte DEFAULT_JRN_CHAR_WIDTH = 12;
      protected static final byte DEFAULT_REC_SPACING_WIDTH = 2;
      protected static final byte DEFAULT_JRN_SPACING_WIDTH = 2;
      protected static final int DEFAULT_REC_SIDEWAYS_LINE_WIDTH = 256;
      protected static final int DEFAULT_REC_SIDEWAYS_MAX_CHARS = 256;
      protected static final int DEFAULT_REC_SIDEWAYS_MAX_LINES = 14;
      protected static final boolean DEFAULT_CAP_REC_PARTIAL_PRT = false;
      protected static final boolean DEFAULT_CAP_JRN_PARTIAL_PRT = false;
      protected static final int DEFAULT_REC_STEPS_PER_INCH = 205;
      protected static final int DEFAULT_JRN_STEPS_PER_INCH = 205;

      IBM4689PrinterState(LogHelper logger, POSPrinterHandle handle) {
         super(logger);
         this.fontTable = IBM4689PrinterCmdConst.FONT_TYPE_TABLE;
         this.alignmentList = this.PRT_TABLE_ALIGNMENT;
         this.textPositionList = this.PRT_BC_TABLE_TEXTPOSITION;
         this.recBarCodeMaxHeight = 255;
         this.recBarCodeMaxWidth = 432;
         this.recBarCodeMinHorSize = 0;
         this.recBarCodeMaxHorSize = 1;
         this.setMaxPrintBitmapSize((byte)2, 7732);
         this.recBitmapMaxHeight = 144;
         this.setDefaults();
         this.setPrinterCapabilities();
         this.setPrinterParserParamMaxValues();
         this.handle = handle;
      }

      private void setDefaults() {
         this.recCharWidth = 12;
         this.jrnCharWidth = 12;
         this.spacingWidth = 2;
         this.setCharHeight((byte)2, (byte)24);
         this.setCharWidth((byte)2, (byte)12);
         this.setCharHeight((byte)8, (byte)24);
         this.setCharWidth((byte)8, (byte)12);
         this.setStationWidth((byte)2, 432);
         this.setStationWidth((byte)8, 432);
         this.capRecPartialLine = false;
         this.capJrnPartialLine = false;
         this.recFeedStepsPerInch = 205;
         this.jrnFeedStepsPerInch = 205;
         this.recSidewaysLineWidth = 256;
         this.recSidewaysMaxChars = 256;
         this.recSidewaysMaxLines = 14;
      }

      private void setPrinterCapabilities() {
         this.capConcurrentJrnRec = true;
         this.capCoverSensor = true;
         this.capTransaction = true;
         this.setBitSet(2, true, this.recbools);
         this.setBitSet(2, true, this.jrnbools);
         this.setBitSet(3, true, this.recbools);
         this.setBitSet(3, true, this.jrnbools);
         this.setBitSet(1, true, this.recbools);
         this.setBitSet(1, true, this.jrnbools);
         this.setBitSet(5, true, this.recbools);
         this.setBitSet(5, true, this.jrnbools);
         this.setBitSet(4, true, this.recbools);
         this.setBitSet(4, true, this.jrnbools);
         this.capRecDwideDhigh = true;
         this.capJrnDwideDhigh = true;
         this.capRecEmptySensor = true;
         this.capJrnEmptySensor = true;
         this.capRecNearEndSensor = true;
         this.capJrnNearEndSensor = true;
         this.setBitSet(0, true, this.recbools);
         this.setBitSet(0, true, this.jrnbools);
         this.setBitSet(7, true, this.recbools);
         this.setBitSet(7, true, this.jrnbools);
         this.setBitSet(6, true, this.recbools);
         this.setBitSet(6, true, this.jrnbools);
         this.capRecBarCode = true;
         this.capRecPapercut = true;
         this.capRecRight90 = true;
         this.capRecStamp = true;
         this.capRecBitmap = true;
         this.capJrnPapercut = false;
         this.capJrnStamp = true;
      }

      private void setPrinterParserParamMaxValues() {
         this.recMaxColorNumber = 1;
         this.recMaxLogoNumber = 2;
         this.recMaxScaleHorizontalNumber = 2;
         this.recMaxScaleVerticalNumber = 2;
         this.recMaxLinesToFeed = 255;
         this.recMaxBitmapNumber = 20;
         this.recMaxUnderlineNumber = 255;
         this.jrnMaxColorNumber = 1;
         this.jrnMaxScaleHorizontalNumber = 2;
         this.jrnMaxScaleVerticalNumber = 2;
         this.jrnMaxLinesToFeed = 255;
         this.jrnMaxUnderlineNumber = 255;
      }

      public PrinterParser getParser() {
         if (this.printerParser == null) {
            this.printerParser = new Printer4689Parser(this.handle.getPOSPrinterCmdFactory(), this);
         }

         return this.printerParser;
      }

      public int getMicronsPerStep(byte station) {
         int aret = 1;
         switch (station) {
            case 2:
               aret = 125;
               break;
            case 8:
               aret = 125;
         }

         return aret;
      }

      public int getDotsPerInchWide(byte station) {
         int dpi;
         switch (station) {
            case 2:
               dpi = 205;
               break;
            case 8:
            default:
               dpi = 205;
         }

         return dpi;
      }

      public int getDotsPerInchHigh(byte station) {
         int dpi;
         switch (station) {
            case 2:
               dpi = 206;
               break;
            case 8:
            default:
               dpi = 206;
         }

         return dpi;
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
            case 8:
               return true;
            case 32:
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
               break;
            case 8:
               ret = this.jrnCharWidth;
         }

         return ret;
      }

      public void setCharWidth(byte station, byte dots) {
         switch (station) {
            case 2:
               this.recCharWidth = dots;
               break;
            case 8:
               this.jrnCharWidth = dots;
         }
      }

      public int getSpacingWidth(byte station) {
         return this.spacingWidth;
      }

      public void setSpacingWidth(byte station, int dots) {
         this.spacingWidth = dots;
      }

      public ImageStreamProducer getImageStreamProducer() {
         if (this.streamProducer == null) {
            this.streamProducer = new Image4689StreamProducer();
         }

         return this.streamProducer;
      }
   }

   class StationVisitor extends IBM4689PrinterCmdV {
      private boolean rotation = false;
      private boolean flag = true;
      private boolean flagSetLogo = false;
      private boolean ccFlag = false;
      private boolean endCmd = false;
      private IBM4689Imp.IBM4689PrinterState state = null;
      private DefaultPOSPrinterHandle handle = null;

      public StationVisitor(IBMPrinterState state, DefaultPOSPrinterHandle handle) {
         this.state = (IBM4689Imp.IBM4689PrinterState)state;
         this.handle = handle;
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

      public boolean isContinuation4689Cmd() {
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

      public void visitContinuation4689Cmd(IBM4689PrinterCmd.Continuation4689Cmd cc) {
         this.visit(cc);
         this.flag = false;
         this.ccFlag = true;
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
            this.handle.submit(parser.getMasterPrinterCmd());
         } catch (HandleException var5) {
         }
      }

      public void visitRotatePrintCmd(POSPrinterCmd.RotatePrintCmd rpc) {
         int rot = rpc.getRotation();
         switch (rot) {
            case 0:
            case 90:
            case 180:
            default:
               this.rotation = false;
               break;
            case 270:
               this.rotation = true;
         }
      }
   }
}
