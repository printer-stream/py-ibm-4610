package com.ibm.posj.printer.ibm4610;

import com.ibm.jutil.Util;
import com.ibm.jutil.logging.LogHelper;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.DefaultPOSPrinterHandle;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleCmdVisitor;
import com.ibm.posj.HandleException;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.IBM4610PrinterCmdConst;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.POSPrinterHandle;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;
import com.ibm.posj.bus.printer.cmds.ibm4610.Print4610CmdFactory;
import com.ibm.posj.printer.AbstractCmdCompleteVisitor;
import com.ibm.posj.printer.CmdAdjuster;
import com.ibm.posj.printer.DefaultCmdAdjuster;
import com.ibm.posj.printer.DefaultImageStreamProducer;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.IBMPrinterState;
import com.ibm.posj.printer.ImageStreamProducer;
import com.ibm.posj.printer.PrinterHandleState;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.printer.SlipCmdVisitor;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.printer.parser.PrinterParser;
import com.ibm.posj.printer.parser.ibm4610.Printer4610Parser;
import com.ibm.posj.util.IBM4610PrinterCmdV;
import sun.io.ByteToCharUnicode;

public class IBM4610Imp extends IBMPrinterImp {
   private static Tracer tracer = TracerFactory.getInstance().createTracer("I4610Imp");
   private SlipCmdVisitor slpCmdV = new IBM4610SlipCmdVisitor();
   protected HandleCmdVisitor chaseVisitor;
   private boolean inSetLogo = false;
   private AbstractCmdCompleteVisitor ccVisitor = null;
   private ChaseVisitors.PmMultiVisitor pageModeV = new ChaseVisitors.PmMultiVisitor();
   private IBM4610Imp.StationVisitor stationV = new IBM4610Imp.StationVisitor();
   private CmdAdjuster adjuster = null;
   protected static final int MC_30_MAXBITMAP = 8000;
   protected static final int TI1234_MAXBITMAP = 4000;
   protected static final int CONTINUATION_LENGTH = 2;
   public String WRITER_MINIMUM = "posj.IBM4610Writer.minimum";

   public IBM4610Imp(PrinterWriter mainWriter, DefaultPOSPrinterHandle handle, LogHelper logger) {
      super(mainWriter, handle, logger);
   }

   public void clearOutput() {
      this.stationV.clearOutput();
      super.clearOutput();
      this.pageModeV.clearOutput();
   }

   public String getSerialNumber() {
      byte[] sn = new byte[8];
      byte[] snCmds = new byte[]{105, 106, 107, 108};
      StringBuffer buffer = new StringBuffer();

      try {
         this.fillPrinerSerialNumber(this.getHandle(), sn, snCmds, 0, 0);
         char[] k = ByteToCharUnicode.getDefault().convertAll(sn);

         for (int i2 = 0; i2 < 8; i2++) {
            buffer.append(String.valueOf(k[i2]));
         }
      } catch (Exception var6) {
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().print(var6);
         }

         return "";
      }

      return buffer.toString();
   }

   public void receivePrintDataEvent(PrintDataEvent pde) {
      if (PrinterWriter.getTracer().isOn()) {
         PrinterWriter.getTracer().println("receivePrintDataEvent printimp " + pde.getType());
      }

      IBMPrinterState ibmState = (IBMPrinterState)this.getPrinterHandleState();
      if ((1 & pde.getType()) > 0) {
         int printerType = pde.byteAt((byte)1);
         int printerId = pde.byteAt((byte)2);
         if (printerType == 49 || printerType == 48 && printerId == 1 && (pde.byteAt((byte)4) & 2) == 2) {
            ibmState.setPrinterType(3802);
            if (this.isTI9(pde.byteAt((byte)4))) {
               printerId = 10;
            } else {
               printerId = 9;
            }
         } else if (printerType == 48) {
            ibmState.setPrinterType(3801);
         }

         this.setPrinterStateId(printerType, printerId);
         switch (ibmState.getPrinterID()) {
            case 3813:
            case 3816:
            case 3817:
            case 3818:
            case 3821:
            case 3824:
            case 3825:
            case 3826:
            case 3828:
            case 3831:
            case 3832:
            case 3833:
               ibmState.setDBCSPrinter(true);
            case 3814:
            case 3815:
            case 3819:
            case 3820:
            case 3822:
            case 3823:
            case 3827:
            case 3829:
            case 3830:
            default:
               if ((pde.byteAt((byte)4) & 1) != 0) {
                  ibmState.setStationWidth((byte)2, 400);
               } else {
                  ibmState.setStationWidth((byte)2, 576);
               }

               if ((pde.byteAt((byte)3) & 4) != 0 && (pde.byteAt((byte)3) & 16) != 0) {
                  ibmState.setDoubleByteMode(true);
               }

               if ((pde.byteAt((byte)3) & 2) != 0) {
                  ibmState.setFlipperPresent(true);
               } else {
                  ibmState.setFlipperPresent(false);
               }

               if ((pde.byteAt((byte)3) & 1) != 0) {
                  ibmState.setMICRPresent(true);
               } else {
                  ibmState.setMICRPresent(false);
               }

               ibmState.setECLevel(pde.byteAt((byte)5) & 255);
               boolean oldTi345 = printerId > 0 && ibmState.getPrinterEC() < 48;
               if (printerId != 0 && !oldTi345) {
                  ibmState.setMaxPrintBitmapSize((byte)2, 8000);
                  ibmState.setMaxPrintBitmapSize((byte)4, 8000);
               } else {
                  ((IBM4610Imp.IBM4610PrinterState)ibmState).setOldStyle4610(true);
                  PrintCmdFactory pf = ((DefaultPOSPrinterCmd.Factory)this.getHandle().getPOSPrinterCmdFactory()).getPrintCmdFactory();
                  ((Print4610CmdFactory)pf).setOlderModel(true);
                  ibmState.setMaxPrintBitmapSize((byte)2, 4000);
                  ibmState.setMaxPrintBitmapSize((byte)4, 4000);
               }
         }
      }
   }

   public void setPOSPrintCmdFactory(POSPrinterCmd.Factory factory) {
      super.setPOSPrintCmdFactory(factory);
      this.adjuster = new DefaultCmdAdjuster((DefaultPOSPrinterCmd.Factory)factory, this.writer.getMaxCmdLen(), this.writer);
   }

   public boolean adjusted(DefaultPOSPrinterCmd dpc) {
      PrintCmd pc = dpc.getPrintCmd();
      if (pc.getDataSize() > this.getMaxCmdLen() && !(dpc instanceof IBM4610PrinterCmd.ContinuationCmd)) {
         this.adjuster.adjustPOSPrinterCmd(dpc);
         return true;
      } else {
         return false;
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
         this.ccVisitor = new CmdCompleteVisitor((IBMPrinterState)this.getPrinterHandleState());
      }

      return this.ccVisitor;
   }

   public void busException(Object e) {
   }

   public void setChaseVisitor(HandleCmdVisitor h) {
      this.chaseVisitor = h;
   }

   public void receivePrintStatus(PrintStatus ps) {
      if (!ps.getStatus(9) && !ps.getStatus(11)) {
         ((IBMPrinterState)this.getPrinterHandleState()).setPrinterBusy(true);
      } else {
         ((IBMPrinterState)this.getPrinterHandleState()).setPrinterBusy(false);
      }

      this.getHandle().setActive();
      POSPrinterCmd pc = this.writer.getNextProcessCmd();
      if (null != pc) {
         pc.accept(this.getSlipCmdV());
         if (this.getSlipCmdV().isSlpManipulator()) {
            if (ps.isSlipPaperPresent() && !ps.isSlipNonPaperError()) {
               ps.setDisableSlipStatus(true);
               this.getHandle().getEventHelper().fireStatusEvent(ps);
            } else {
               this.getHandle().getEventHelper().fireStatusEvent(ps);
            }
         } else {
            this.getHandle().getEventHelper().fireStatusEvent(ps);
         }
      } else {
         this.getHandle().getEventHelper().fireStatusEvent(ps);
      }
   }

   public void handleCmdComplete(PrintCmd ccmd) {
      if (ccmd.getHandleCmd() instanceof IBM4610PrinterCmd.ChangePrintSideCmd) {
         this.updatePrinterSide();
      }

      super.handleCmdComplete(ccmd);
   }

   public void cancelRotateMode() {
      POSPrinterCmd cmd = this.getPrintCmdFactory().createRotatePrintCmd((byte)2, 0);
      cmd.appendPOSPrinterCmd(this.getPrintCmdFactory().createRotatePrintCmd((byte)4, 0));
      this.submit(cmd);
   }

   protected void updatePrinterSide() {
      byte side = this.getPrinterState().getSlpPrintSide();
      if (side == 1) {
         side = 2;
      } else {
         side = 1;
      }

      this.getPrinterState().setSlpPrintSide(side);
   }

   protected SlipCmdVisitor getSlipCmdV() {
      return this.slpCmdV;
   }

   protected String getMinimumPropertyText() {
      return this.WRITER_MINIMUM;
   }

   protected HandleCmdVisitor getChaseVisitor() {
      if (null == this.chaseVisitor) {
         this.chaseVisitor = new ChaseVisitors.EcLevelChaseVisitor(this);
      }

      return this.chaseVisitor;
   }

   protected boolean addToOutgoingList(PrintCmd cmd) {
      if (this.isClearOutput() && !cmd.getHandleCmd().getOwnerCmd().isSettingsCmd()) {
         this.trace("failed clearOutput");
         return false;
      } else if (!this.getPendingList().addCommand(cmd)) {
         PrintCmd pre = null;
         if (cmd.getHandleCmd() instanceof IBM4610PrinterCmd.ECLevelRequestCmd) {
            pre = this.getPendingList().removeLastCmd();
         }

         this.writeSubmit();
         if (null != pre) {
            this.preAddToCmdList(pre);
            if (!this.getPendingList().addCommand(pre) && PrinterWriter.getTracer().isOn()) {
               PrinterWriter.getTracer().println("Major IBM4610 problem");
            }
         }

         this.preAddToCmdList(cmd);
         boolean added = this.getPendingList().addCommand(cmd);
         if (PrinterWriter.getTracer().isOn()) {
            PrinterWriter.getTracer().println("cmd added 2nd time " + added);
         }

         if (added) {
            this.getHandle().timeStamp().addToCmdList(cmd, this.getPendingList());
         }

         return added;
      } else {
         this.getHandle().timeStamp().addToCmdList(cmd, this.getPendingList());
         return true;
      }
   }

   protected void preAddToCmdList(PrintCmd cmd) {
      if (!this.isClearOutput()) {
         cmd.getHandleCmd().accept(this.stationV);
         if (this.inSetLogo && !this.stationV.isSetLogoCmd()) {
            cmd.getHandleCmd().setMemoryCmd();
            if (null == this.getPendingList().getPending() && !this.stationV.isContinuationCmd()) {
               DefaultPOSPrinterCmd cc = (DefaultPOSPrinterCmd)((IBM4610PrinterCmd.Factory)this.getPrintCmdFactory()).createContinuationCmd();
               this.getPendingList().forceImmediateCmd(cc.getPrintCmd());
               cmd.getHandleCmd().getOwnerCmd().addAssociatedCmd(cc);
            }
         }

         if (this.stationV.appendStationSettings() && !this.inSetLogo) {
            if (PrinterWriter.getTracer().isOn()) {
               PrinterWriter.getTracer().println("<<preAdd2CmdList PrintCmdList" + Util.toHexString(this.getPendingList().hashCode()));
            }
         } else {
            if (PrinterWriter.getTracer().isOn()) {
               PrinterWriter.getTracer().println("<<preAdd2CmdList PrintCmdList" + Util.toHexString(this.getPendingList().hashCode()));
            }
         }
      }
   }

   protected void submitList(DefaultPOSPrinterCmd dpc) {
      dpc.accept(this.stationV);
      if (this.stationV.isSlpRotationActive()) {
      }

      if (this.stationV.isSetLogoCmd()) {
         this.inSetLogo = true;
      }

      dpc.acceptMultiVisitor(this.pageModeV);
      super.submitList(dpc);
      this.inSetLogo = false;
   }

   protected void preSubmit(PrintCmdList l) {
      if (l.hasCommands()) {
         ;
      }
   }

   protected PrinterHandleState createPrinterHandleState() {
      return new IBM4610Imp.IBM4610PrinterState(this.logger, this.getHandle());
   }

   private boolean isTI9(byte f2) {
      boolean flag = false;
      if ((f2 & 4) != 0 || (f2 & 8) != 0 || (f2 & 16) != 0) {
         flag = true;
      }

      return flag;
   }

   private void fillPrinerSerialNumber(Handle handle, byte[] sn, byte[] snCmds, int index, int iterateIdx) throws HandleException {
      POSPrinterCmd cmd = ((POSPrinterCmd.Factory)handle.getHandleCmdFactory()).createMCTReadCmd(snCmds[iterateIdx++]);
      this.getHandle().getHandleImp().submit(cmd);
      cmd.waitUntilCompleted();
      if (cmd.isCompleted() && ((DefaultPOSPrinterCmd)cmd).getPrintCmd().isSucceeded()) {
         sn[index++] = ((DefaultPOSPrinterCmd)cmd).getRequestData().getBytes()[0];
         sn[index++] = ((DefaultPOSPrinterCmd)cmd).getRequestData().getBytes()[1];
      }

      if (iterateIdx < snCmds.length) {
         this.fillPrinerSerialNumber(handle, sn, snCmds, index, iterateIdx);
      }
   }

   protected Tracer getTracer() {
      return tracer;
   }

   protected class IBM4610PrinterState extends IBMPrinterState implements IIBM4610PrinterState {
      private POSPrinterHandle handle;
      byte recCharWidth = 1;
      byte slpCharWidth = 1;
      int spacingWidth = 0;
      ImageStreamProducer streamProducer = null;
      protected boolean inError = false;
      PrinterParser printerParser = null;
      POSPrinterCmd.SetBitmapCmd[] bitmapLoaded = null;
      POSPrinterCmd.SetLogoCmd[] logoLoaded = null;
      protected static final int PRT_SLP_ARRAY_OFFSET = 20;
      protected final int[] PRT_BC_TABLE_TEXTPOSITION = new int[]{-11, -12, -13};
      protected final int PRT_REC_BC_MIN_VER_SIZE = 1;
      protected final int PRT_REC_BC_MAX_VER_SIZE = 255;
      protected final int PRT_REC_BC_MIN_HOR_SIZE = 2;
      protected final int PRT_REC_BC_MAX_HOR_SIZE = 4;
      protected final int[] PRT_TABLE_ALIGNMENT = new int[]{-1, -2, -3};
      protected boolean oldStyle4610 = false;
      protected static final int PRT_REC_DOTS_PER_INCH_WIDE = 203;
      protected static final int PRT_SLP_DOTS_PER_INCH_WIDE = 152;
      protected static final int PRT_REC_DOTS_PER_INCH_HIGH = 223;
      protected static final int PRT_SLP_DOTS_PER_INCH_HIGH = 114;
      protected static final int PRT_REC_LINE_WIDTH = 576;
      protected static final int PRT_REC_LINE_NARROW_WIDTH = 400;
      protected static final int PRT_SLP_LINE_WIDTH = 474;
      protected static final int PRT_REC_BC_MAX_HEIGHT = 255;
      protected static final int PRT_REC_MAX_PRT_BMP_SIZE = 4000;
      protected static final int PRT_SLP_MAX_PRT_BMP_SIZE = 4000;
      protected static final int PRT_REC_BMP_MAX_HEIGHT = 2040;
      protected static final int PRT_SLP_BMP_MAX_HEIGHT = 40;
      protected static final byte DEFAULT_REC_CHAR_WIDTH = 10;
      protected static final byte DEFAULT_SLP_CHAR_WIDTH = 7;
      protected static final byte DEFAULT_REC_SPACING_WIDTH = 3;
      protected static final int DEFAULT_REC_SIDEWAYS_LINE_WIDTH = 794;
      protected static final boolean DEFAULT_CAP_REC_PARTIAL_PRT = false;
      protected static final boolean DEFAULT_CAP_SLP_PARTIAL_PRT = true;
      protected static final int DEFAULT_REC_STEPS_PER_INCH = 204;
      protected static final int DEFAULT_SLP_STEPS_PER_INCH = 52;
      protected static final int DEFAULT_SLP_LANDSCAPE_STEPS_PER_INCH = 127;

      IBM4610PrinterState(LogHelper logger, POSPrinterHandle handle) {
         super(logger);
         this.fontTable = IBM4610PrinterCmdConst.FONT_TYPE_TABLE;
         this.alignmentList = this.PRT_TABLE_ALIGNMENT;
         this.textPositionList = this.PRT_BC_TABLE_TEXTPOSITION;
         this.recBarCodeMaxHeight = 255;
         this.recBarCodeMaxWidth = 576;
         this.slpBarCodeMaxHeight = 255;
         this.slpBarCodeMaxWidth = 474;
         this.recBarCodeMinHorSize = 2;
         this.recBarCodeMaxHorSize = 4;
         this.slpBarCodeMinHorSize = 2;
         this.slpBarCodeMaxHorSize = 4;
         this.setMaxPrintBitmapSize((byte)2, 4000);
         this.setMaxPrintBitmapSize((byte)4, 4000);
         this.recBitmapMaxHeight = 2040;
         this.slpBitmapMaxHeight = 40;
         this.setDefaults();
         this.setPrinterCapabilities();
         this.setPrinterParserParamMaxValues();
         this.handle = handle;
      }

      private void setDefaults() {
         this.recCharWidth = 10;
         this.slpCharWidth = 7;
         this.spacingWidth = 3;
         this.setCharHeight((byte)2, (byte)20);
         this.setCharWidth((byte)2, (byte)10);
         this.setCharHeight((byte)4, (byte)9);
         this.setCharWidth((byte)4, (byte)7);
         this.setStationWidth((byte)2, 576);
         this.setStationWidth((byte)4, 474);
         this.capRecPartialLine = false;
         this.capSlpPartialLine = true;
         this.recFeedStepsPerInch = 204;
         this.slpFeedStepsPerInch = 52;
         this.slpLandscapeSPI = 127;
         this.recSidewaysLineWidth = 794;
      }

      private void setPrinterCapabilities() {
         this.capConcurrentRecSlp = true;
         this.capCoverSensor = true;
         this.capSlpPartialLine = true;
         this.capRec2Color = true;
         this.setBitSet(1, true, this.recbools);
         this.setBitSet(1, true, this.slpbools);
         this.setBitSet(2, true, this.slpbools);
         this.setBitSet(2, true, this.recbools);
         this.setBitSet(3, true, this.slpbools);
         this.setBitSet(3, true, this.recbools);
         this.capRecDwideDhigh = true;
         this.capSlpDwideDhigh = true;
         this.capRecEmptySensor = true;
         this.capSlpEmptySensor = true;
         this.setBitSet(0, true, this.recbools);
         this.capRecBarCode = true;
         this.capRecBitmap = true;
         this.capSlpBitmap = true;
         this.capSlpLeft90 = true;
         this.capRecRotate180 = true;
         this.capRecPapercut = true;
         this.capRecStamp = true;
         this.capFullslip = true;
         this.capTransaction = true;
         this.setBitSet(4, true, this.recbools);
         this.setBitSet(7, true, this.recbools);
         this.setBitSet(6, true, this.recbools);
         this.setBitSet(7, true, this.slpbools);
         this.setBitSet(6, true, this.slpbools);
      }

      public PrinterParser getParser() {
         if (this.printerParser == null) {
            this.printerParser = new Printer4610Parser(this.handle.getPOSPrinterCmdFactory(), this, !this.isOldStyle4610());
         }

         return this.printerParser;
      }

      private void setPrinterParserParamMaxValues() {
         this.recMaxBitmapNumber = 40;
         this.slpMaxBitmapNumber = 40;
         this.recMaxLogoNumber = 25;
         this.slpMaxLogoNumber = 25;
         this.recMaxColorNumber = 2;
         this.recMaxScaleHorizontalNumber = 7;
         this.recMaxScaleVerticalNumber = 7;
         this.slpMaxScaleHorizontalNumber = 1;
         this.slpMaxScaleVerticalNumber = 1;
         this.recMaxLinesToFeed = 255;
         this.slpMaxLinesToFeed = 255;
         this.recMaxUnderlineNumber = 255;
      }

      public int getMicronsPerStep(byte station) {
         int aret = 1;
         switch (station) {
            case 2:
               aret = 125;
               break;
            case 4:
               aret = 529;
         }

         return aret;
      }

      public int getDotsPerInchWide(byte station) {
         int dpi;
         switch (station) {
            case 2:
               dpi = 203;
               break;
            case 4:
            default:
               dpi = 152;
               break;
            case 20:
               dpi = 127;
         }

         return dpi;
      }

      public int getDotsPerInchHigh(byte station) {
         int dpi;
         switch (station) {
            case 2:
               dpi = 223;
               break;
            case 4:
            default:
               dpi = 114;
               break;
            case 20:
               dpi = 127;
         }

         return dpi;
      }

      public ImageStreamProducer getImageStreamProducer() {
         if (this.streamProducer == null) {
            this.streamProducer = new DefaultImageStreamProducer();
         }

         return this.streamProducer;
      }

      public boolean errorPending() {
         return this.inError;
      }

      public int getColorCapability(byte station) {
         if (this.getPrinterEC() < 48) {
            return 0;
         } else {
            switch (station) {
               case 2:
                  return 3;
               default:
                  return 0;
            }
         }
      }

      public boolean stationPresent(byte station) {
         switch (station) {
            case 2:
               return true;
            case 4:
               int ID = this.getPrinterID();
               switch (ID) {
                  case 3815:
                  case 3817:
                  case 3818:
                  case 3823:
                  case 3825:
                  case 3826:
                  case 3830:
                  case 3832:
                  case 3833:
                     return false;
                  case 3816:
                  case 3819:
                  case 3820:
                  case 3821:
                  case 3822:
                  case 3824:
                  case 3827:
                  case 3828:
                  case 3829:
                  case 3831:
                  default:
                     return true;
               }
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
            case 4:
            case 20:
               ret = this.slpCharWidth;
         }

         return ret;
      }

      public void setCharWidth(byte station, byte dots) {
         switch (station) {
            case 2:
               this.recCharWidth = dots;
               break;
            case 4:
            case 20:
               this.slpCharWidth = dots;
         }
      }

      public int getSpacingWidth(byte station) {
         return this.spacingWidth;
      }

      public void setSpacingWidth(byte station, int dots) {
         this.spacingWidth = dots;
      }

      public void setSetBitmapCmd(POSPrinterCmd.SetBitmapCmd setBmpCmd) {
         if (this.bitmapLoaded == null) {
            this.bitmapLoaded = new POSPrinterCmd.SetBitmapCmd[this.recMaxBitmapNumber];
            int i = 0;

            while (i < this.recMaxBitmapNumber) {
               this.bitmapLoaded[i++] = null;
            }
         }

         if (setBmpCmd.getStation() == 2) {
            this.bitmapLoaded[setBmpCmd.getBitmapNo() - 1] = setBmpCmd;
         }

         if (setBmpCmd.getStation() == 4) {
            this.bitmapLoaded[setBmpCmd.getBitmapNo() - 1 + 20] = setBmpCmd;
         }
      }

      public void cleanBitmaps(byte station) {
         if (this.bitmapLoaded != null) {
            if (station == 2) {
               int i = 0;

               while (i < this.recMaxBitmapNumber - 20) {
                  this.bitmapLoaded[i++] = null;
               }
            } else if (station == 4) {
               int i = 20;

               while (i < this.recMaxBitmapNumber) {
                  this.bitmapLoaded[i++] = null;
               }
            }
         }
      }

      public void deleteBitmap(byte location, byte station) {
         if (this.bitmapLoaded != null) {
            if (station == 2) {
               this.bitmapLoaded[location - 1] = null;
            } else if (station == 4) {
               this.bitmapLoaded[location - 1 + 20] = null;
            }
         }
      }

      public POSPrinterCmd.SetBitmapCmd getSetBitmapCmd(byte location, byte station) {
         POSPrinterCmd.SetBitmapCmd setBitmapCmd = null;
         if (this.bitmapLoaded != null && location - 1 < this.recMaxBitmapNumber - 20) {
            if (station == 2) {
               setBitmapCmd = this.bitmapLoaded[location - 1];
            } else if (station == 4) {
               setBitmapCmd = this.bitmapLoaded[location - 1 + 20];
            }
         }

         return setBitmapCmd;
      }

      public void setSetLogoCmd(POSPrinterCmd.SetLogoCmd setLogoCmd) {
         if (setLogoCmd.getLocation() != 100) {
            if (this.logoLoaded == null) {
               this.logoLoaded = new POSPrinterCmd.SetLogoCmd[this.recMaxLogoNumber];
               int i = 0;

               while (i < this.recMaxLogoNumber) {
                  this.logoLoaded[i++] = null;
               }
            }

            this.logoLoaded[setLogoCmd.getLocation() - 1] = setLogoCmd;
         }
      }

      public void deleteLogo(byte location) {
         if (this.logoLoaded != null) {
            this.logoLoaded[location - 1] = null;
         }
      }

      public void cleanLogos() {
         if (this.logoLoaded != null) {
            int i = 0;

            while (i < this.recMaxLogoNumber) {
               this.logoLoaded[i++] = null;
            }
         }
      }

      public POSPrinterCmd.SetLogoCmd getSetLogoCmd(byte location) {
         POSPrinterCmd.SetLogoCmd setLogoCmd = null;
         if (this.logoLoaded != null && location < this.recMaxLogoNumber) {
            setLogoCmd = this.logoLoaded[location - 1];
         }

         return setLogoCmd;
      }

      public POSPrinterCmd.SetBitmapCmd getTopBitmapCmd(byte station) {
         POSPrinterCmd.SetBitmapCmd ret = null;
         if (station == 2) {
            int i = 0;

            while (i < 20 && this.bitmapLoaded[i] == null) {
               i++;
            }

            ret = i < 20 ? this.bitmapLoaded[i] : null;
         } else if (station == 4) {
            int i = 20;

            while (i < this.recMaxBitmapNumber && this.bitmapLoaded[i] == null) {
               i++;
            }

            ret = i < this.recMaxBitmapNumber ? this.bitmapLoaded[20 + i] : null;
         }

         return ret;
      }

      public boolean isOldStyle4610() {
         return this.oldStyle4610;
      }

      public void setOldStyle4610(boolean oldStyle4610) {
         this.oldStyle4610 = oldStyle4610;
         if (oldStyle4610) {
            this.recMaxScaleHorizontalNumber = 1;
            this.recMaxScaleVerticalNumber = 1;
         }
      }
   }

   class StationVisitor extends IBM4610PrinterCmdV {
      private boolean rotation = false;
      private boolean flag = true;
      private boolean flagSetLogo = false;
      private boolean ccFlag = false;
      private boolean endCmd = false;

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

      public void visitChangePrintSideCmd(IBM4610PrinterCmd.ChangePrintSideCmd cmd) {
         this.flag = false;
      }

      public void visitContinuationCmd(IBM4610PrinterCmd.ContinuationCmd cc) {
         this.visit(cc);
         this.flag = false;
         this.ccFlag = true;
      }

      public void visitSetLogoCmd(POSPrinterCmd.SetLogoCmd setLogoCmd) {
         this.visit(setLogoCmd);
         if (100 != setLogoCmd.getLocation()) {
            this.flagSetLogo = true;
         } else {
            this.endCmd = true;
            this.flagSetLogo = false;
         }
      }

      public boolean isSlpRotationActive() {
         return this.rotation;
      }

      public void visitRotatePrintCmd(POSPrinterCmd.RotatePrintCmd rpc) {
         int rot = rpc.getRotation();
         switch (rot) {
            case 0:
            case 180:
            default:
               this.rotation = false;
               break;
            case 90:
            case 270:
               this.rotation = true;
         }
      }
   }
}
