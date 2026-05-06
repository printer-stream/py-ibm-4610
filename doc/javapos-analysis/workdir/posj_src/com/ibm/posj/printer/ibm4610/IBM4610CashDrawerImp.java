package com.ibm.posj.printer.ibm4610;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.IBMPrinterComposite;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.printer.IBMPrinterCashDrawerLinker;
import com.ibm.posj.printer.event.PrintErrorEvent;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.DevBuses;

public class IBM4610CashDrawerImp extends AbstractSubDeviceImp implements IBMPrinterCashDrawerLinker {
   private int cashDrawerNumber = 0;
   private StatusEvent openDrawerEvent;
   private StatusEvent closeDrawerEvent;
   private PrintStatus pStatus = null;
   private boolean cmdFailure = false;
   public static final byte DEFAULT_DRAWER1 = 0;
   public static final byte DEFAULT_DRAWER2 = 1;
   public static final byte DEFAULT_WIDTH = 3;
   public static final int CD1 = 1;
   public static final int CD2 = 2;
   public static final int TIMEOUT = 30000;
   public static final String TIMEOUT_ERROR_MSG = "Timeout : The openDrawer command did not finalize";

   public IBM4610CashDrawerImp(HandleKey key, IBMPrinterComposite printerParent, int cdNum) {
      super(key, printerParent);
      this.cashDrawerNumber = cdNum;
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.submitPreCond(cmd);

      try {
         if (cmd.getCode() == 100) {
            this.submitOpenDrawerCmd(cmd);
         } else {
            if (!(cmd instanceof SystemCmd)) {
               throw new HandleException("Invalid CashDrawerCmd object submitted!");
            }

            if (cmd.getCode() == 101) {
               this.submitStatusCmd();
            } else if (cmd.getCode() == 103) {
               this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)cmd);
            }
         }
      } catch (HandleException var6) {
         cmd.getResult().setInError(true);
         throw var6;
      } finally {
         cmd.setCompleted(true);
      }
   }

   public void handleError(PrintCmd err, PrintErrorEvent event) {
      this.cmdFailure = true;
      err.getHandleCmd().setCompleted(true);
   }

   public void handleCmdComplete(PrintCmd printCmd) {
      this.cmdFailure = false;
      printCmd.getHandleCmd().setCompleted(true);
   }

   public void receivePrintStatus(PrintStatus ps) {
      if (null == this.openDrawerEvent) {
         this.openDrawerEvent = new StatusEvent(this, 1);
         this.closeDrawerEvent = new StatusEvent(this, 0);
      }

      if (this.pStatus != null && this.pStatus != ps) {
         this.pStatus.recycle();
      }

      this.pStatus = ps;
      if (this.isTracerOn() && this.isHandleInit()) {
         this.traceNormal("Is CD open? " + ps.getStatus(13));
      }

      if (ps.getStatus(13)) {
         if (this.isInit()) {
            this.getEventHelper().fireStatusEvent(this.openDrawerEvent);
         }
      } else if (this.isInit()) {
         this.getEventHelper().fireStatusEvent(this.closeDrawerEvent);
      }
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
      if (this.getHandleImp().getDevBus().equals(DevBuses.RS232_DEVBUS)) {
         if (this.getCashDrawerNumber() == 1) {
            cmd.setDeviceId(2312);
         } else {
            cmd.setDeviceId(2313);
         }
      } else if (this.getHandleImp().getDevBus().equals(DevBuses.RS485_DEVBUS)) {
         if (this.getCashDrawerNumber() == 1) {
            cmd.setDeviceId(2314);
         } else {
            cmd.setDeviceId(2315);
         }
      } else if (this.getCashDrawerNumber() == 1) {
         cmd.setDeviceId(2316);
      } else {
         cmd.setDeviceId(2317);
      }

      cmd.setFirmwareLevel(this.getPrinterParent().getPrinterID_microcodeLevel() & 0xFF);
      cmd.setSerialNumber(this.getPrinterParent().getDeviceSerialNumber());
      if (this.isTracerOn()) {
         this.traceNormal("Using Device ID : " + cmd.getDeviceId());
      }
   }

   protected void submitOpenDrawerCmd(HandleCmd cmd) throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("--> SubmitOpenDrawerCmd");
      }

      PrintCmd cdCmd = this.createDrawerOpenCmd();
      this.cmdFailure = false;
      this.getDevQueue().submit(cdCmd);
      cdCmd.getHandleCmd().waitUntilCompleted(30000L);
      if (this.isTracerOn()) {
         this.traceNormal("--> At this point the CD should have been pop up");
      }

      if (this.cmdFailure) {
         throw new HandleException("Timeout : The openDrawer command did not finalize");
      } else {
         cdCmd.recycle();
         if (this.isTracerOn()) {
            this.traceNormal("<-- SubmitOpenDrawerCmd");
         }
      }
   }

   protected PrintCmd createDrawerOpenCmd() {
      if (this.isTracerOn()) {
         this.traceNormal("--> createDrawerOpenCmd");
      }

      DefaultPOSPrinterCmd ppc = null;
      PrintCmd printCmd = null;
      byte drawerNumber = 0;
      if (this.getCashDrawerNumber() == 2) {
         drawerNumber = 1;
      }

      ppc = (DefaultPOSPrinterCmd)this.get4610PrinterCmdFactory().createOpenDrawerCmd(drawerNumber, (byte)3, (byte)3);
      printCmd = ppc.getPrintCmd();
      printCmd.setParent(this);
      if (this.isTracerOn()) {
         this.traceNormal("<-- createDrawerOpenCmd");
      }

      return printCmd;
   }

   protected int getCashDrawerNumber() {
      return this.cashDrawerNumber;
   }
}
