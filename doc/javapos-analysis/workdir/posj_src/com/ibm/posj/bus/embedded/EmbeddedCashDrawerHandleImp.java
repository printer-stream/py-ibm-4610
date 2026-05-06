package com.ibm.posj.bus.embedded;

import com.ibm.embedded.CashDrawerEmbeddedDriver;
import com.ibm.embedded.EmbeddedException;
import com.ibm.embedded.event.EmbeddedEvent;
import com.ibm.embedded.event.EmbeddedListener;
import com.ibm.jutil.Timer;
import com.ibm.jutil.Timerable;
import com.ibm.jutil.Util;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.CashDrawerHandleImp;
import com.ibm.posj.bus.CashDrawerStatusByteParser;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PosjUtil;

public class EmbeddedCashDrawerHandleImp extends AbstractEmbeddedHandleImp implements CashDrawerHandleImp {
   private EmbeddedListener listener = null;
   private int cdNum = 1;
   private CashDrawerEmbeddedDriver embeddedDriver = null;
   private CashDrawerStatusByteParser statusParser = null;
   private Object openCmdPending = new Object();
   private EmbeddedCashDrawerHandleImp.OfflineTimerable timerable = new EmbeddedCashDrawerHandleImp.OfflineTimerable();
   private Timer offlineTimer = new Timer(this.timerable, 500);
   private boolean isOpenCmdPending = false;
   public static final int CD1 = 1;
   public static final int CD2 = 2;
   public static final long OPEN_CD_COMMAND_TIMEOUT = 500L;
   public static final int CD_1_OPEN_BIT_POSITION = 7;
   public static final int CD_1_PRESENT_BIT_POSITION = 6;
   public static final int CD_2_OPEN_BIT_POSITION = 5;
   public static final int CD_2_PRESENT_BIT_POSITION = 4;
   public static final int CD_INTERRUPTS_BIT_POSITION = 3;
   public static final int CD_INTERFACE_BIT_POSITION = 2;
   public static final int CD_PULSE_BIT_POSITION = 1;
   public static final int CD_OPEN_ARM_BIT_POSITION = 0;
   public static final String STATUS_REQ_TIMEOUT_MSG = "EmbeddedCashDrawer: timeout while waiting for statusRequest command to complete";
   public static final String ERROR_GETTING_STATUS_MSG = "An error occurred while trying to get status from the device";
   public static final String DEVICE_REMOVED_STRING = "Embedded Cash Drawer removed";
   public static final String DEVICE_ADDED_STRING = "Embedded Cash Drawer added";
   private static final int MAX_TIMEOUT_RETRY = 6;

   public EmbeddedCashDrawerHandleImp(HandleKey key, CashDrawerEmbeddedDriver driver, int cdNum) {
      super(key);
      this.embeddedDriver = driver;
      this.cdNum = cdNum;
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitCashDrawer(this);
   }

   public DevCat getDevCat() {
      return DevCats.CASHDRAWER_DEVCAT;
   }

   public void init() throws HandleException {
      this.listener = new EmbeddedCashDrawerHandleImp.CDEmbeddedListener();

      try {
         this.getEmbeddedDriver().addEmbeddedEventListener(this.listener);
      } catch (EmbeddedException var2) {
         this.getLogHelper().addLogEntry(1003, "Could not add listener", "CashDrawer", 3);
      }

      this.statusParser = new EmbeddedCashDrawerHandleImp.EmbeddedCDStatusParser();
      this.submitStatusReqCmd();
   }

   public short getECLevel() {
      return -1;
   }

   public boolean isFlashable() {
      return false;
   }

   public CashDrawerEmbeddedDriver getEmbeddedDriver() {
      return this.embeddedDriver;
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            if (cmd.getCode() == 100) {
               this.submitOpenDrawerCmd(cmd);
            } else if (cmd.getCode() == 101) {
               this.submitStatusReqCmd();
            } else {
               if (cmd.getCode() != 103) {
                  throw new HandleException("Invalid CashDrawerCmd object submitted!");
               }

               this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)cmd);
            }
         } catch (HandleException var6) {
            this.setHandleCmdResultInError(cmd, true);
            throw var6;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   public int getCdNum() {
      return this.cdNum;
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) {
      int id = 2300;
      if (this.isTracerOn()) {
         this.traceNormal("-->submitDevInfoCmd() : getAdapterID() = " + Util.toHexString(this.embeddedDriver.getAdapterID()));
      }

      if (this.embeddedDriver.getAdapterID() == 661L) {
         if (this.cdNum == 1) {
            id = 2303;
         } else {
            id = 2304;
         }
      } else if (this.embeddedDriver.getAdapterID() == 665L) {
         id = 2302;
      } else if (this.embeddedDriver.getAdapterID() == 4614L) {
         id = 2301;
      } else if (this.embeddedDriver.getAdapterID() == 5L) {
         id = 2305;
      }

      devInfoCmd.setDeviceId(id);
      if (this.isTracerOn()) {
         this.traceNormal("submitDevInfoCmd() : devInfoCmd.getDeviceId() = " + devInfoCmd.getDeviceId() + "<--");
      }
   }

   protected void submitOpenDrawerCmd(HandleCmd cashDrawerCmd) throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("->submitOpenDrawerCmd()");
      }

      this.isOpenCmdPending = true;

      try {
         this.waitForOpenCmdToComplete();
      } catch (EmbeddedException var6) {
         new HandleException("Error in Embedded bus :", var6);
      } finally {
         this.isOpenCmdPending = false;
      }

      if (this.isTracerOn()) {
         this.traceNormal("<-submitOpenDrawerCmd()");
      }
   }

   protected void waitForOpenCmdToComplete() throws EmbeddedException {
      int retry = 0;

      while (this.isOpenCmdPending && retry < 6) {
         synchronized (this.openCmdPending) {
            this.getEmbeddedDriver().openCD(this.getCdNum());

            try {
               this.openCmdPending.wait(500L);
            } catch (InterruptedException var5) {
            }
         }
      }
   }

   protected void submitStatusReqCmd() throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("submitStatusReqCmd()");
      }

      byte statusByte = this.getStatus();
      this.statusParser.init(statusByte);
      int cdStatusCode = this.statusParser.getCashDrawerStatusCode();
      this.getHandle().getEventHelper().fireStatusEvent(new StatusEvent(this, cdStatusCode));
   }

   protected byte getStatus() throws HandleException {
      byte status = 0;

      try {
         status = this.getEmbeddedDriver().getCDStatus();
         if (this.isTracerOn()) {
            this.traceNormal("getStatus()... status  = " + Util.toHexString(status));
         }

         return status;
      } catch (EmbeddedException var3) {
         throw new HandleException("An error occurred while trying to get status from the device", var3);
      }
   }

   private class CDEmbeddedListener implements EmbeddedListener {
      private CDEmbeddedListener() {
      }

      public void statusEventOccurred(EmbeddedEvent event) {
         byte statusByte = event.getStatus();
         if (EmbeddedCashDrawerHandleImp.this.isTracerOn()) {
            EmbeddedCashDrawerHandleImp.this.traceNormal("statusEventOccurred() = " + Util.toHexString(statusByte));
         }

         EmbeddedCashDrawerHandleImp.this.statusParser.init(statusByte);
         if (EmbeddedCashDrawerHandleImp.this.statusParser.isCDConnected()) {
            int cdStatusCode = EmbeddedCashDrawerHandleImp.this.statusParser.getCashDrawerStatusCode();
            if (EmbeddedCashDrawerHandleImp.this.isTracerOn()) {
               EmbeddedCashDrawerHandleImp.this.traceNormal("EmbeddedCDHandleImp... CDStatusCode = " + cdStatusCode);
            }

            EmbeddedCashDrawerHandleImp.this.getHandle().getEventHelper().fireStatusEvent(new StatusEvent(this, cdStatusCode));
         }

         synchronized (EmbeddedCashDrawerHandleImp.this.openCmdPending) {
            EmbeddedCashDrawerHandleImp.this.isOpenCmdPending = false;
            EmbeddedCashDrawerHandleImp.this.openCmdPending.notifyAll();
         }
      }
   }

   private class EmbeddedCDStatusParser extends CashDrawerStatusByteParser {
      private EmbeddedCDStatusParser() {
      }

      public void init(byte status) {
         super.init(status);
         if (!this.isCDConnected()) {
            EmbeddedCashDrawerHandleImp.this.traceNormal(
               "EmbeddedCDStatusParser: CD" + EmbeddedCashDrawerHandleImp.this.getCdNum() + " not connected, will set handle offline"
            );
            if (EmbeddedCashDrawerHandleImp.this.getHandle().getState().isOnline()) {
               if (!EmbeddedCashDrawerHandleImp.this.offlineTimer.getStarted()) {
                  EmbeddedCashDrawerHandleImp.this.offlineTimer.reset();
               }

               EmbeddedCashDrawerHandleImp.this.offlineTimer.start();
            }
         } else {
            EmbeddedCashDrawerHandleImp.this.traceNormal("started=" + EmbeddedCashDrawerHandleImp.this.offlineTimer.getStarted());
            if (EmbeddedCashDrawerHandleImp.this.offlineTimer.getStarted()) {
               EmbeddedCashDrawerHandleImp.this.offlineTimer.stop();
            }

            if (!EmbeddedCashDrawerHandleImp.this.getHandle().getState().isOnline()) {
               EmbeddedCashDrawerHandleImp.this.traceNormal(
                  "EmbeddedCDStatusParser: CD" + EmbeddedCashDrawerHandleImp.this.getCdNum() + " is connected, will set handle online"
               );
               EmbeddedCashDrawerHandleImp.this.getHandle().getState().setOnline(true);
               EmbeddedCashDrawerHandleImp.this.getLogHelper().addLogEntry(2005, "Embedded Cash Drawer added", "CashDrawer");
               EmbeddedCashDrawerHandleImp.this.getHandle().getEventHelper().fireOnlineEvent(new OnlineEvent(this, System.currentTimeMillis()));
            }
         }
      }

      public boolean isCDOpened() {
         return EmbeddedCashDrawerHandleImp.this.getCdNum() == 1 ? this.isCD1Opened() : this.isCD2Opened();
      }

      public boolean isCD1Opened() {
         return !PosjUtil.isBitSelected(this.getStatusByte(), 7);
      }

      public boolean isCD2Opened() {
         return !PosjUtil.isBitSelected(this.getStatusByte(), 5);
      }

      public boolean isCDConnected() {
         return EmbeddedCashDrawerHandleImp.this.getCdNum() == 1 ? this.isCD1Connected() : this.isCD2Connected();
      }

      public boolean isCD1Connected() {
         return !PosjUtil.isBitSelected(this.getStatusByte(), 6);
      }

      public boolean isCD2Connected() {
         return !PosjUtil.isBitSelected(this.getStatusByte(), 4);
      }

      public boolean getUnsolicitedStatus() {
         return true;
      }
   }

   protected class OfflineTimerable implements Timerable {
      public void timerExpired() {
         EmbeddedCashDrawerHandleImp.this.traceNormal("firing offline");
         EmbeddedCashDrawerHandleImp.this.getLogHelper().addLogEntry(2006, "Embedded Cash Drawer removed", "CashDrawer");
         EmbeddedCashDrawerHandleImp.this.getHandle().getState().setOnline(false);
         EmbeddedCashDrawerHandleImp.this.getHandle().getEventHelper().fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
      }
   }
}
