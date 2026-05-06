package com.ibm.posj.printer.ibm4610;

import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.posj.AbstractHandleCmd;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.MICRCmd;
import com.ibm.posj.MICRHandleCmdFilterV;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.SystemCmdVisitor;
import com.ibm.posj.bus.IBMPrinterComposite;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.printer.IBMPrinterMICRLinker;
import com.ibm.posj.printer.event.PrintDataEvent;
import com.ibm.posj.printer.event.PrintErrorEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.DevBuses;

public class IBM4610MICRImp extends AbstractSubDeviceImp implements IBMPrinterMICRLinker {
   private IBM4610MICRImp.MICRCmdVisitor micrCmdVisitor = new IBM4610MICRImp.MICRCmdVisitor(this);
   private MICRHandleCmdFilterV handleCmdFilterV = new MICRHandleCmdFilterV();
   private PrintStatus lastPrintStatus = null;
   boolean status;
   private AbstractHandleCmd.DefaultResult defaultResult = new AbstractHandleCmd.DefaultResult();
   private HandleCmd endInsertionCmd = null;

   public IBM4610MICRImp(HandleKey key, IBMPrinterComposite printerParent) {
      super(key, printerParent);
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.handleCmdFilterV.reset();
      cmd.accept(this.handleCmdFilterV);
      if (this.handleCmdFilterV.isMICRCmd()) {
         ((MICRCmd)cmd).accept(this.micrCmdVisitor);
      } else {
         if (!this.handleCmdFilterV.isSystemCmd()) {
            throw new HandleException("Invalid MICRCmd object submitted!");
         }

         ((SystemCmd)cmd).accept(this.micrCmdVisitor);
      }
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
      if (this.getHandleImp().getDevBus().equals(DevBuses.RS232_DEVBUS)) {
         cmd.setDeviceId(3102);
      } else if (this.getHandleImp().getDevBus().equals(DevBuses.RS485_DEVBUS)) {
         cmd.setDeviceId(3101);
      } else {
         cmd.setDeviceId(3103);
      }

      if (this.isTracerOn()) {
         this.traceNormal("Using Device ID : " + cmd.getDeviceId());
      }

      cmd.setSerialNumber(this.getPrinterParent().getPrinterHandleState().getSerialNumber());
      cmd.setFirmwareLevel(this.getPrinterParent().getPrinterHandleState().getPrinterEC());
      cmd.setCompleted(true);
   }

   public void handleError(PrintCmd err, PrintErrorEvent event) {
      this.traceNormal("In IBM4610MICRImp -> handleError");
      if (event.getErrorType() == 0) {
         this.defaultResult.setErrorCode(1);
         this.defaultResult.setInError(true);
      } else if (event.getErrorType() == 16 || event.getErrorType() == 2) {
         this.defaultResult.setErrorCode(0);
         this.defaultResult.setInError(true);
      }

      err.getOtherHandleCmd().setResult(this.defaultResult);
      err.getOtherHandleCmd().setCompleted(true);
      PrintCmd pc = null;
      this.getDevQueue().processErrorResponse(pc, (byte)0);
   }

   public void handleCmdComplete(PrintCmd printCmd) {
      printCmd.getOtherHandleCmd().setResult(this.defaultResult);
      printCmd.getOtherHandleCmd().setCompleted(true);
      printCmd.getHandleCmd().setCompleted(true);
      printCmd.getHandleCmd().recycle();
   }

   public void receivePrintDataEvent(PrintDataEvent pde) {
      if ((pde.getType() & 16) > 0) {
         ByteBuffer micrData = pde.getDataBuffer();
         if (this.isTracerOn()) {
            this.traceNormal("Data->" + Util.toFormatedHexString(micrData.getBytes()));
         }

         if (this.endInsertionCmd != null) {
            this.endInsertionCmd.setCompleted(true);
            this.endInsertionCmd.setResult(new AbstractHandleCmd.DefaultResult());
         }

         this.getEventHelper().fireDataEvent(new DataEvent(this, micrData.getBytes()));
      }
   }

   public void receivePrintStatus(PrintStatus ps) {
      this.lastPrintStatus = ps;
      if (this.isInit()) {
         this.getEventHelper().fireStatusEvent(ps);
      }
   }

   protected void submitBeginInsertionCmd(HandleCmd cmd) throws HandleException {
      this.status = this.getLastPrintStatus().getStatus(4);
      if (this.status) {
         DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)this.get4610PrinterCmdFactory().createRegisterSlipCmd(true);
         this.submitPrintCmd(cmd, dpc);
      }
   }

   protected void submitEndInsertionCmd(HandleCmd cmd) throws HandleException {
      this.status = this.getLastPrintStatus().getStatus(2);
      if (this.status) {
         DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)this.get4610PrinterCmdFactory().createReadSlipCmd();
         this.submitPrintCmd(cmd, dpc);
      }

      this.endInsertionCmd = cmd;
   }

   protected void submitBeginRemovalCmd(HandleCmd cmd) throws HandleException {
      this.status = this.getLastPrintStatus().getStatus(2);
      if (this.status) {
         DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)this.get4610PrinterCmdFactory().createEjectSlipCmd();
         this.submitPrintCmd(cmd, dpc);
      }
   }

   protected void submitEndRemovalCmd(HandleCmd cmd) {
      this.status = this.getLastPrintStatus().getStatus(3);
      if (!this.status) {
      }
   }

   protected void submitStatisticCmd(SystemCmd.StatisticCmd cmd) {
      DefaultPOSPrinterCmd dpc = (DefaultPOSPrinterCmd)this.get4610PrinterCmdFactory().createStatisticCmd(cmd.getStatisticType());
      this.submitPrintCmd(cmd, dpc);
      dpc.waitUntilCompleted(60000L);
      cmd.setStatisticResult(dpc.getRequestData().getBytes());
   }

   protected void submitPrintCmd(HandleCmd cmd, DefaultPOSPrinterCmd dpc) throws IllegalArgumentException {
      PrintCmd pc = dpc.getPrintCmd();
      pc.setParent(this);
      pc.setOtherHandleCmd(cmd);
      this.getDevQueue().submit(pc);
   }

   protected PrintStatus getLastPrintStatus() {
      return this.lastPrintStatus;
   }

   public class MICRCmdVisitor implements com.ibm.posj.MICRCmdVisitor, SystemCmdVisitor {
      private IBM4610MICRImp parent;

      public MICRCmdVisitor(IBM4610MICRImp parent) {
         this.parent = parent;
      }

      public void visitBeginInsertionCmd(MICRCmd cmd) throws HandleException {
         this.parent.submitBeginInsertionCmd(cmd);
      }

      public void visitEndInsertionCmd(MICRCmd cmd) throws HandleException {
         this.parent.submitEndInsertionCmd(cmd);
      }

      public void visitBeginRemovalCmd(MICRCmd cmd) throws HandleException {
         this.parent.submitBeginRemovalCmd(cmd);
      }

      public void visitEndRemovalCmd(MICRCmd cmd) {
         this.parent.submitEndRemovalCmd(cmd);
      }

      public void visitStatisticCmd(SystemCmd.StatisticCmd cmd) {
         this.parent.submitStatisticCmd(cmd);
      }

      public void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
      }

      public void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
         IBM4610MICRImp.this.submitStatusCmd();
      }

      public void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
         IBM4610MICRImp.this.submitDevInfoCmd(cmd);
      }

      public void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
      }

      public void visitDirectWriteCmd(SystemCmd.DirectWriteCmd cmd) {
      }
   }
}
