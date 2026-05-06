package com.ibm.posj.bus.rs232;

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
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232DataEvent;
import com.ibm.rs232.event.Rs232ErrorEvent;

public class Rs232CashDrawerHandleImp extends AbstractRs232HandleImp implements CashDrawerHandleImp {
   private int cdNum = 1;
   private boolean isOpenCmdPending = false;
   private Object openCmdPending = new Object();
   private boolean isStatusCmdPending = false;
   private Object statusCmdLock = new Object();
   private boolean unsolicitedStaus = false;
   protected CashDrawerStatusByteParser statusParser = null;
   public static final int CD1 = 1;
   public static final int CD2 = 2;
   public static final byte[] OPEN_CD1_CMD = new byte[]{7};
   public static final byte[] OPEN_CD2_CMD = new byte[]{27, 7};
   public static final byte[] STATUS_REQUEST_CMD = new byte[]{6};
   public static final byte[] SET_UNSOLICITED_STATUS_CMD = new byte[]{27, 6};
   public static final long OPEN_CD_COMMAND_TIMEOUT = 500L;
   public static final long STATUS_REQUEST_CMD_TIMEOUT = 250L;
   public static final int MAX_TIMEOUT_RETRY = 6;
   public static final long OPEN_CMD_WAIT_TIME = 350L;
   public static final byte CD1_OPEN_SENSOR_MASK = 64;
   public static final byte CD2_OPEN_SENSOR_MASK = 32;
   public static final int CD_1_OPEN_BIT_POSITION = 6;
   public static final int CD_2_OPEN_BIT_POSITION = 5;
   public static final int CD_1_PRESENT_BIT_POSITION = 4;
   public static final int CD_2_PRESENT_BIT_POSITION = 3;
   public static final int UNSOLICITED_STATUS_BIT_POSITION = 2;
   public static final String ERROR_SENDING_CMD_MSG = "Error while sending OpenDrawerCmd to Rs232 CashDrawer";
   public static final String STATUS_REQ_TIMEOUT_MSG = "Rs232CashDrawer: timeout while waiting for statusRequest command to complete";
   public static final String OPEN_CMD_TIMEOUT_MSG = "Rs232CashDrawer: timeout while waiting for open command to complete";
   public static final String HW_ERROR_MSG = "Rs232 CashDrawer hardware error";
   public static final String CMD_NOT_SUPPORTED_MSG = "Command not supported for Rs232 CashDrawer";
   public static final String DEVICE_REMOVED_STRING = "RS-232 Cash Drawer removed";
   public static final String DEVICE_ADDED_STRING = "RS-232 Cash Drawer added";

   public Rs232CashDrawerHandleImp(HandleKey key, Rs232Port rs232Port, int cdNum) {
      super(key, rs232Port);
      this.cdNum = cdNum;
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitCashDrawer(this);
   }

   public DevCat getDevCat() {
      return DevCats.CASHDRAWER_DEVCAT;
   }

   public short getECLevel() {
      return -1;
   }

   public boolean isFlashable() {
      return false;
   }

   public void init() throws HandleException {
      super.init();
      this.statusParser = new Rs232CashDrawerHandleImp.Rs232CDStatusParser();
      if (this.isTracerOn()) {
         this.traceNormal("init: will send status request cmd ");
      }

      this.submitStatusReqCmd();
      if (!this.unsolicitedStaus) {
         this.submit(SET_UNSOLICITED_STATUS_CMD);
      }
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

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) throws HandleException {
      if (this.getCdNum() == 1) {
         devInfoCmd.setDeviceId(2306);
      } else {
         devInfoCmd.setDeviceId(2307);
      }

      if (this.isTracerOn()) {
         this.traceNormal("-->submitDevInfoCmd() : devInfoCmd.getDeviceId() = " + devInfoCmd.getDeviceId() + "<--");
      }
   }

   protected void dataEventOccurred(Rs232DataEvent event) {
      byte[] data = event.getData();
      if (this.isTracerOn()) {
         this.traceNormal("Rs323CashDrawerHandleImp:: dataEventOccurred.. event = " + Util.toFormatedHexString(data));
      }

      byte statusByte = data[0];
      this.statusParser.init(statusByte);
      if (this.isTracerOn()) {
         this.traceNormal("Cash Drawer Status = " + this.statusParser.toString());
      }

      this.unsolicitedStaus = this.statusParser.getUnsolicitedStatus();
      int cdStatusCode = this.statusParser.getCashDrawerStatusCode();
      this.getHandle().getEventHelper().fireStatusEvent(new StatusEvent(this, cdStatusCode));
      if (this.isStatusCmdPending) {
         this.isStatusCmdPending = false;
         synchronized (this.statusCmdLock) {
            this.statusCmdLock.notifyAll();
         }
      }

      if (this.isOpenCmdPending && this.statusParser.isCDOpened()) {
         if (this.isTracerOn()) {
            this.traceNormal(" Drawer open! ");
         }

         this.isOpenCmdPending = false;
         synchronized (this.openCmdPending) {
            this.openCmdPending.notifyAll();
         }
      }
   }

   protected void errorEventOccurred(Rs232ErrorEvent event) {
      if (this.isTracerOn()) {
         this.traceNormal("Rs232CashDrawerHandleImp:: errorEvent occurred");
      }

      this.getLogHelper().addLogEntry(1000, "Rs232 CashDrawer hardware error", "AllDevices", 4);
   }

   protected int getCDNum() {
      return this.cdNum;
   }

   protected void submitOpenDrawerCmd(HandleCmd cashDrawerCmd) throws HandleException {
      if (this.statusParser.isCDConnected()) {
         if (this.isTracerOn()) {
            this.traceNormal("cdNum = " + this.cdNum);
         }

         byte[] openCdCmd = this.getCDNum() == 1 ? OPEN_CD1_CMD : OPEN_CD2_CMD;
         if (this.isTracerOn()) {
            this.traceNormal("submitOpenDrawerCmd: openCdCmd = " + Util.toFormatedHexString(openCdCmd) + "\t to CD Number = " + this.cdNum);
         }

         this.isOpenCmdPending = true;
         this.waitForOpenCmdToComplete(openCdCmd);
      }
   }

   protected void submitStatusReqCmd() throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("StatusReqCmd submitted");
      }

      this.isStatusCmdPending = true;
      this.submit(STATUS_REQUEST_CMD);
      this.waitForStatusCmdComplete();
   }

   protected void waitForStatusCmdComplete() throws HandleException {
      int retry;
      for (retry = 0; this.isStatusCmdPending && retry < 6; retry++) {
         synchronized (this.statusCmdLock) {
            try {
               this.statusCmdLock.wait(250L);
            } catch (InterruptedException var5) {
            }
         }

         if (!this.isStatusCmdPending) {
            return;
         }

         if (this.isTracerOn()) {
            this.traceNormal("statusCmd is still pending, will send command again");
         }

         this.submit(STATUS_REQUEST_CMD);
      }

      if (retry >= 6 && this.isStatusCmdPending) {
         throw new HandleException("Rs232CashDrawer: timeout while waiting for statusRequest command to complete");
      }
   }

   protected void waitForOpenCmdToComplete(byte[] openCmd) throws HandleException {
      int retry = 0;

      while (this.isOpenCmdPending && retry < 6) {
         synchronized (this.openCmdPending) {
            this.submit(openCmd);

            try {
               this.openCmdPending.wait(500L);
            } catch (InterruptedException var6) {
            }
         }

         if (!this.isOpenCmdPending) {
            return;
         }

         if (this.getCDNum() == 1 && this.statusParser.isCD1Opened() || this.getCDNum() == 2 && this.statusParser.isCD2Opened()) {
            this.isOpenCmdPending = false;
            return;
         }

         retry++;
         if (this.isTracerOn()) {
            this.traceNormal("OpenCmd is still pending. retry = " + retry);
         }
      }

      if (retry >= 6 && this.isOpenCmdPending) {
         throw new HandleException("Rs232CashDrawer: timeout while waiting for open command to complete");
      }
   }

   protected class Rs232CDStatusParser extends CashDrawerStatusByteParser {
      public void init(byte status) {
         super.init(status);
         if (!this.isCDConnected()) {
            if (Rs232CashDrawerHandleImp.this.isTracerOn()) {
               Rs232CashDrawerHandleImp.this.traceNormal(
                  "Rs232CDStatusParser: CD" + Rs232CashDrawerHandleImp.this.getCdNum() + " not connected, will set handle offline"
               );
            }

            if (Rs232CashDrawerHandleImp.this.getHandle().getState().isOnline()) {
               Rs232CashDrawerHandleImp.this.getHandle().getState().setOnline(false);
               Rs232CashDrawerHandleImp.this.getLogHelper().addLogEntry(2006, "RS-232 Cash Drawer removed", "CashDrawer");
               Rs232CashDrawerHandleImp.this.getHandle().getEventHelper().fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
            }
         } else if (!Rs232CashDrawerHandleImp.this.getHandle().getState().isOnline()) {
            if (Rs232CashDrawerHandleImp.this.isTracerOn()) {
               Rs232CashDrawerHandleImp.this.traceNormal(
                  "Rs232CDStatusParser: CD" + Rs232CashDrawerHandleImp.this.getCdNum() + " is connected, will set handle online"
               );
            }

            Rs232CashDrawerHandleImp.this.getHandle().getState().setOnline(true);
            Rs232CashDrawerHandleImp.this.getLogHelper().addLogEntry(2005, "RS-232 Cash Drawer added", "CashDrawer");
            Rs232CashDrawerHandleImp.this.getHandle().getEventHelper().fireOnlineEvent(new OnlineEvent(this, System.currentTimeMillis()));
         }
      }

      public boolean isCDOpened() {
         return Rs232CashDrawerHandleImp.this.getCdNum() == 1 ? this.isCD1Opened() : this.isCD2Opened();
      }

      public boolean isCD1Opened() {
         return PosjUtil.isBitSelected(this.getStatusByte(), 6);
      }

      public boolean isCD2Opened() {
         return PosjUtil.isBitSelected(this.getStatusByte(), 5);
      }

      public boolean isCDConnected() {
         return Rs232CashDrawerHandleImp.this.getCdNum() == 1 ? this.isCD1Connected() : this.isCD2Connected();
      }

      public boolean isCD1Connected() {
         return PosjUtil.isBitSelected(this.getStatusByte(), 4);
      }

      public boolean isCD2Connected() {
         return PosjUtil.isBitSelected(this.getStatusByte(), 3);
      }

      public boolean getUnsolicitedStatus() {
         return PosjUtil.isBitSelected(this.getStatusByte(), 2);
      }
   }
}
