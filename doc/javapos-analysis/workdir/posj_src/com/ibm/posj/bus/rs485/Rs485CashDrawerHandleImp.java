package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.event.SioDeviceDataEvent;
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
import com.ibm.posj.util.RetryHelper;

public class Rs485CashDrawerHandleImp extends AbstractRs485HandleImp implements CashDrawerHandleImp {
   private int cashDrawerNumber;
   private CashDrawerStatusByteParser statusParser = null;
   private RetryHelper retryHelper = null;
   public static final int CD1 = 1;
   public static final int CD2 = 2;
   public static final byte[] OPEN_CD1_CMD = new byte[]{1};
   public static final byte[] OPEN_CD2_CMD = new byte[]{2};
   public static final byte[] TEST_REQUEST_CMD = new byte[]{0, 16};
   public static final byte[] STATUS_REQUEST_CMD = new byte[]{0, 32};
   public static final int CD_1_OPEN_BIT_POSITION = 6;
   public static final int CD_2_OPEN_BIT_POSITION = 5;
   public static final int CD_1_PRESENT_BIT_POSITION = 4;
   public static final int CD_2_PRESENT_BIT_POSITION = 3;
   public static final String DEVICE_REMOVED_STRING = "RS-485 Cash Drawer removed";
   public static final String DEVICE_ADDED_STRING = "RS-485 Cash Drawer added";

   public Rs485CashDrawerHandleImp(HandleKey key, SioDevice sioDevice, int cdNumber) {
      super(key, sioDevice);
      this.cashDrawerNumber = cdNumber;
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitCashDrawer(this);
   }

   public DevCat getDevCat() {
      return DevCats.CASHDRAWER_DEVCAT;
   }

   public void init() throws HandleException {
      super.init();
      this.statusParser = new Rs485CashDrawerHandleImp.Rs485CDStatusParser();
      this.submitStatusCmd();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            if (cmd.getCode() == 100) {
               this.submitOpenDrawerCmd(cmd);
            } else if (cmd.getCode() == 101) {
               this.submitStatusCmd();
            } else {
               if (cmd.getCode() != 103) {
                  throw new HandleException("Invalid CashDrawerCmd object submitted!" + cmd.getName());
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

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) {
      if (this.cashDrawerNumber == 1) {
         devInfoCmd.setDeviceId(2308);
      } else {
         devInfoCmd.setDeviceId(2309);
      }

      if (this.isTracerOn()) {
         this.traceNormal("-->submitDevInfoCmd() : devInfoCmd.getDeviceId() = " + devInfoCmd.getDeviceId() + "<--");
      }
   }

   protected void submitCmd(HandleCmd handleCmd, byte[] cmd, int times, int waitFor) throws HandleException {
      RetryHelper rh = this.getRetryHelper();
      rh.setLastCmd(handleCmd);
      rh.setCmdPending(true);
      int retry = 0;

      while (rh.isCmdPending() && retry++ < times) {
         this.submitSync(cmd);

         try {
            rh.waitCmdPending((long)waitFor);
         } catch (InterruptedException var8) {
         }

         if (!rh.isCmdPending()) {
            return;
         }
      }

      if (retry >= times && rh.isCmdPending()) {
         throw new HandleException("timeout while waiting for " + handleCmd.getName() + " command to complete");
      }
   }

   protected RetryHelper getRetryHelper() {
      if (this.retryHelper == null) {
         this.retryHelper = new RetryHelper();
      }

      return this.retryHelper;
   }

   protected void dataEventOccurred(SioDeviceDataEvent event) {
      byte statusByte = event.getData()[0];
      if (this.isTracerOn()) {
         this.traceNormal("Cash Drawer Status byte is: " + this.statusParser.toString());
      }

      this.statusParser.init(statusByte);
      if (this.statusParser.isCDConnected()) {
         int cdStatusCode = this.statusParser.getCashDrawerStatusCode();
         if (this.isTracerOn()) {
            this.traceNormal("Rs485CashDrawerCDHandleImp... CDStatusCode (0=Closed 1=Open) = " + cdStatusCode);
         }

         this.getHandle().getEventHelper().fireStatusEvent(new StatusEvent(this, cdStatusCode));
      }

      this.getRetryHelper().setCmdPending(false);
   }

   protected void submitOpenDrawerCmd(HandleCmd handleCmd) throws HandleException {
      if (this.statusParser.isCDConnected()) {
         byte[] openCdCmd = this.getCdNumber() == 1 ? OPEN_CD1_CMD : OPEN_CD2_CMD;
         this.submitCmd(handleCmd, openCdCmd, 3, 400);
      }
   }

   protected void submitStatusCmd() throws HandleException {
      this.submitSync(STATUS_REQUEST_CMD);
   }

   protected int getCdNumber() {
      return this.cashDrawerNumber;
   }

   protected void reinitialize() throws HandleException {
      this.submitStatusCmd();
   }

   private class Rs485CDStatusParser extends CashDrawerStatusByteParser {
      private Rs485CDStatusParser() {
      }

      public void init(byte status) {
         super.init(status);
         if (!this.isCDConnected()) {
            if (Rs485CashDrawerHandleImp.this.getHandle().getState().isOnline()) {
               if (Rs485CashDrawerHandleImp.this.isTracerOn()) {
                  Rs485CashDrawerHandleImp.this.traceNormal(
                     "Rs485CDStatusParser: CD" + Rs485CashDrawerHandleImp.this.getCdNumber() + " is disconnected, will set handle offline"
                  );
               }

               Rs485CashDrawerHandleImp.this.getHandle().getState().setOnline(false);
               Rs485CashDrawerHandleImp.this.getLogHelper().addLogEntry(2006, "RS-485 Cash Drawer removed", "CashDrawer");
               Rs485CashDrawerHandleImp.this.getHandle().getEventHelper().fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
            }
         } else if (!Rs485CashDrawerHandleImp.this.getHandle().getState().isOnline()) {
            Rs485CashDrawerHandleImp.this.traceNormal(
               "Rs485CDStatusParser: CD" + Rs485CashDrawerHandleImp.this.getCdNumber() + " is connected, will set handle online"
            );
            Rs485CashDrawerHandleImp.this.getHandle().getState().setOnline(true);
            Rs485CashDrawerHandleImp.this.getLogHelper().addLogEntry(2005, "RS-485 Cash Drawer added", "CashDrawer");
            Rs485CashDrawerHandleImp.this.getHandle().getEventHelper().fireOnlineEvent(new OnlineEvent(this, System.currentTimeMillis()));
         }
      }

      public boolean isCDOpened() {
         return Rs485CashDrawerHandleImp.this.getCdNumber() == 1 ? this.isCD1Opened() : this.isCD2Opened();
      }

      public boolean isCDConnected() {
         return Rs485CashDrawerHandleImp.this.getCdNumber() == 1 ? this.isCD1Connected() : this.isCD2Connected();
      }

      public boolean isCD1Connected() {
         return !PosjUtil.isBitSelected(this.getStatusByte(), 4);
      }

      public boolean isCD2Connected() {
         return !PosjUtil.isBitSelected(this.getStatusByte(), 3);
      }

      public boolean isCD1Opened() {
         return PosjUtil.isBitSelected(this.getStatusByte(), 6);
      }

      public boolean isCD2Opened() {
         return PosjUtil.isBitSelected(this.getStatusByte(), 5);
      }
   }
}
