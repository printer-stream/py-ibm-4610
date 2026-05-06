package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.Util;
import com.ibm.posj.CashDrawerCmd;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.CashDrawerHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.util.DefaultHandleCmdV;
import com.ibm.posj.util.DefaultSystemCmdV;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PosjUtil;

public class HidCashDrawerHandleImp extends AbstractHidHandleImp implements HidHandleImp, CashDrawerHandleImp {
   private int cdNumber = 1;
   private boolean openState = false;
   private boolean isOpenCmdPending = false;
   private Object openCmdPending = new Object();
   private boolean isInitCmdPending = false;
   private Object initCmdLock = new Object();
   private HidCashDrawerHandleImp.HandleCmdFilterV handleCmdFilterV = new HidCashDrawerHandleImp.HandleCmdFilterV();
   private HidCashDrawerHandleImp.CDSystemCmdV cdSystemCmdV = new HidCashDrawerHandleImp.CDSystemCmdV();
   private HidCashDrawerHandleImp.CDStatusParser cdStatusParser = new HidCashDrawerHandleImp.CDStatusParser();
   public static final int CD1 = 1;
   public static final int CD2 = 2;
   public static final long OPEN_CD_COMMAND_TIMEOUT = 300L;
   public static final long INIT_CMD_TIMEOUT = 250L;
   public static final int MAX_TIMEOUT_RETRY = 6;
   public static final long OPEN_CMD_WAIT_TIME = 350L;
   public static final byte COMMAND_REJECT_MASK = -128;
   public static final byte CD1_OPEN_SENSOR_MASK = 64;
   public static final byte CD2_OPEN_SENSOR_MASK = 32;
   public static final byte CD1_OFFLINE_SENSOR_MASK = 16;
   public static final byte CD2_OFFLINE_SENSOR_MASK = 8;
   public static final byte COMMAND_COMPLETE_MASK = 8;
   public static final byte CD_FLASH_BUSY_MASK = 2;
   public static final byte SRAM_TEST_BAD_MASK = 4;
   public static final int COMMAND_REJECT_SB1_BIT_POS = 7;
   public static final int CD1_SENSOR_SB1_BIT_POS = 6;
   public static final int CD2_SENSOR_SB1_BIT_POS = 5;
   public static final int CD1_DRIVER_SENSOR_SB1_BIT_POS = 4;
   public static final int CD2_DRIVER_SENSOR_SB1_BIT_POS = 3;
   public static final int ALARM_SET_SB1_BIT_POS = 0;
   public static final int CD1_DRIVER_ON_DURING_OPEN_CYCLE_SB2_BIT_POS = 6;
   public static final int CD2_DRIVER_ON_DURING_OPEN_CYCLE_SB2_BIT_POS = 5;
   public static final int CD_COMMAND_COMPLETE_SB2_BIT_POS = 3;
   public static final int SRAM_TEST_BAD_SB2_BIT_POS = 2;
   public static final int CD_BUSY_SB2_BIT_POS = 1;
   public static final byte[] OPEN_CD1_CMD = new byte[]{1};
   public static final byte[] OPEN_CD2_CMD = new byte[]{2};
   public static final byte[] TEST_REQUEST_CMD = new byte[]{16};
   public static final byte[] STATUS_REQUEST_CMD = new byte[]{32};
   public static final byte[] RESET_REQUEST_CMD = new byte[]{64};
   public static final byte[] DEVICE_INFO_REQUEST_CMD = new byte[]{-128};
   public static final String DEVICE_REMOVED_STRING = "Cash Drawer removed";
   public static final String DEVICE_ADDED_STRING = "Cash Drawer added";

   public HidCashDrawerHandleImp(HandleKey key, HidDevice hidDevice, int cdNumber) {
      super(key, hidDevice);
      this.cdNumber = cdNumber;
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidCashDrawerHandleImp(this);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitCashDrawer(this);
   }

   public DevCat getDevCat() {
      return DevCats.CASHDRAWER_DEVCAT;
   }

   public void init() throws HandleException {
      super.init();
      this.isInitCmdPending = true;
      this.submitInitCmd();
      this.waitForInitCmdComplete();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (this.isLocked.isTrue()) {
         throw new HandleException("Attempting to submit commands to handle while flashing");
      } else if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            this.handleCmdFilterV.reset();
            cmd.accept(this.handleCmdFilterV);
            if (this.handleCmdFilterV.isCashDrawerCmd()) {
               this.submitOpenDrawerCmd(cmd);
            } else if (cmd.getCode() == 103) {
               this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)cmd);
            } else {
               if (!this.handleCmdFilterV.isSystemCmd()) {
                  throw new HandleException("Invalid CashDrawerCmd object submitted!");
               }

               this.submitSystemCmd((SystemCmd)cmd);
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
      devInfoCmd.setFirmwareLevel(this.getBCDLevel());
      devInfoCmd.setSerialNumber(this.getSerialNumber());
      if (this.cdNumber == 1) {
         devInfoCmd.setDeviceId(2310);
      } else {
         devInfoCmd.setDeviceId(2311);
      }

      if (this.isTracerOn()) {
         this.traceNormal("-->submitDevInfoCmd() : devInfoCmd.getDeviceId() = " + devInfoCmd.getDeviceId() + "<--");
      }
   }

   protected void waitForOpenCmdToComplete(byte[] cmd) throws HandleException, HidException {
      int retry;
      for (retry = 0; this.isOpenCmdPending && retry < 6; retry++) {
         synchronized (this.openCmdPending) {
            this.getHidDevice().setReport((byte)2, (byte)0, (short)((byte)cmd.length), cmd);

            try {
               this.openCmdPending.wait(300L);
            } catch (InterruptedException var6) {
            }
         }

         if (!this.isOpenCmdPending) {
            return;
         }
      }

      if (retry >= 6 && this.isOpenCmdPending) {
         throw new HandleException("CashDrawer: timeout while waiting for open command to complete");
      }
   }

   protected void waitForInitCmdComplete() throws HandleException {
      int retry;
      for (retry = 0; this.isInitCmdPending && retry < 6; retry++) {
         synchronized (this.initCmdLock) {
            try {
               this.initCmdLock.wait(250L);
            } catch (InterruptedException var5) {
            }
         }

         if (!this.isInitCmdPending) {
            return;
         }

         this.submitInitCmd();
      }

      if (retry >= 6 && this.isInitCmdPending) {
         throw new HandleException("CashDrawer: timeout while waiting for init command to complete");
      }
   }

   protected void checkOnlineOffline(HidCashDrawerHandleImp.CDStatusParser cdStatusParse) {
      if (this.getHandle().isOnline()) {
         if (this.getCdNumber() == 1 && !this.cdStatusParser.isCD1Connected()) {
            this.setHandleOnline(false);
            this.getLogHelper().addLogEntry(2006, "Cash Drawer removed", "CashDrawer");
            this.getHandle().getEventHelper().fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
         } else if (this.getCdNumber() == 2 && !this.cdStatusParser.isCD2Connected()) {
            this.setHandleOnline(false);
            this.getLogHelper().addLogEntry(2006, "Cash Drawer removed", "CashDrawer");
            this.getHandle().getEventHelper().fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
         }
      } else if (this.getCdNumber() == 1 && this.cdStatusParser.isCD1Connected()) {
         this.setHandleOnline(true);
         this.getLogHelper().addLogEntry(2005, "Cash Drawer added", "CashDrawer");
         this.getHandle().getEventHelper().fireOnlineEvent(new OnlineEvent(this, System.currentTimeMillis()));
      } else if (this.getCdNumber() == 2 && this.cdStatusParser.isCD2Connected()) {
         this.setHandleOnline(true);
         this.getLogHelper().addLogEntry(2005, "Cash Drawer added", "CashDrawer");
         this.getHandle().getEventHelper().fireOnlineEvent(new OnlineEvent(this, System.currentTimeMillis()));
      }

      this.getLogHelper().close();
   }

   protected void reportEventOccurred(ReportEvent rE) {
      if (this.getHandle() == null) {
         this.traceNormal("The Handle associated with this HandleImp is null");
      } else if (rE == null) {
         this.traceNormal("Got a null ReportEvent object");
      } else if (rE.getData() == null) {
         this.traceNormal("Got a ReportEvent event with null Data object");
      } else {
         byte firstByte = rE.getData()[0];
         byte secondByte = rE.getData()[1];
         this.cdStatusParser.parse(rE.getData());
         if (this.isTracerOn()) {
            this.traceNormal(this.cdStatusParser.toString());
            this.traceNormal("reportEventOccurred: cdNumber: " + this.getCdNumber() + " : " + Util.toFormatedHexString(rE.getData()));
         }

         byte offlineMask = (byte)(this.getCdNumber() == 1 ? 16 : 8);
         if (this.isTracerOn()) {
            this.traceNormal("( firstByte & offlineMask: " + Integer.toHexString(offlineMask) + " ) = " + Integer.toHexString(firstByte & offlineMask));
         }

         this.checkOnlineOffline(this.cdStatusParser);
         if ((firstByte & offlineMask) != 0 && !this.isOpenCmdPending) {
            this.setHandleOnline(false);
            this.getHandle().getEventHelper().fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
         } else {
            try {
               int cdStatusCode = this.parseCashDrawerStatus(rE.getData());
               this.getHandle().getEventHelper().fireStatusEvent(new StatusEvent(this, cdStatusCode));
            } catch (HandleException var10) {
               this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
            }
         }

         if (this.isOpenCmdPending && (secondByte & 8) == 8) {
            this.isOpenCmdPending = false;
            synchronized (this.openCmdPending) {
               this.openCmdPending.notifyAll();
            }
         }

         if (this.isInitCmdPending) {
            this.isInitCmdPending = false;
            synchronized (this.initCmdLock) {
               this.initCmdLock.notifyAll();
            }
         }
      }
   }

   protected void submitOpenDrawerCmd(HandleCmd cashDrawerCmd) throws HandleException {
      try {
         byte[] openCdCmd = this.getCdNumber() == 1 ? OPEN_CD1_CMD : OPEN_CD2_CMD;
         if (this.isTracerOn()) {
            this.traceNormal("submitOpenDrawerCmd: openCdCmd=" + Util.toFormatedHexString(openCdCmd));
         }

         this.isOpenCmdPending = true;
         this.waitForOpenCmdToComplete(openCdCmd);
      } catch (HidException var7) {
         this.setHandleCmdResultInError(cashDrawerCmd, true);
         String msg = "Error while sending OpenDrawerCmd to HID CashDrawer";
         this.traceNormal(msg);
         throw new HandleException(msg, var7);
      } finally {
         this.isOpenCmdPending = false;
         cashDrawerCmd.setCompleted(true);
      }
   }

   protected void submitSystemCmd(SystemCmd systemCmd) throws HandleException {
      byte[] systemCmdBytes = new byte[0];

      try {
         this.cdSystemCmdV.reset();
         systemCmd.accept(this.cdSystemCmdV);
         systemCmdBytes = this.cdSystemCmdV.getCmdBytes();
         if (this.isTracerOn()) {
            this.traceNormal("submitSystemCmd: systemCmdBytes=" + Util.toFormatedHexString(systemCmdBytes));
         }

         if (systemCmdBytes.length == 0) {
            throw new HandleException("Invalid SystemCmd sent to CashDrawer");
         }

         this.getHidDevice().setReport((byte)2, (byte)0, (short)((byte)systemCmdBytes.length), systemCmdBytes);
      } catch (HidException var9) {
         this.setHandleCmdResultInError(systemCmd, true);
         String msg = "Error while sending SystemCmd to HID CashDrawer";
         this.traceNormal(msg);
         throw new HandleException(msg, var9);
      } catch (HandleException var10) {
         this.traceNormal(var10.getMessage());
         throw var10;
      } finally {
         systemCmd.setCompleted(true);
      }
   }

   protected void submitInitCmd() throws HandleException {
      try {
         byte[] statusRequestCmdBytes = STATUS_REQUEST_CMD;
         if (this.isTracerOn()) {
            this.traceNormal("submitInitCmd: statusRequestCmdBytes=" + Util.toFormatedHexString(statusRequestCmdBytes));
         }

         if (statusRequestCmdBytes.length == 0) {
            throw new HandleException("Invalid statusRequestCmdBytes sent to CashDrawer");
         } else {
            this.getHidDevice().setReport((byte)2, (byte)0, (short)((byte)statusRequestCmdBytes.length), statusRequestCmdBytes);
         }
      } catch (HidException var3) {
         String msg = "Error while sending SystemCmd to HID CashDrawer";
         this.traceNormal(msg);
         throw new HandleException(msg, var3);
      } catch (HandleException var4) {
         this.traceNormal(var4.getMessage());
         throw var4;
      }
   }

   protected int parseCashDrawerStatus(byte[] rawStatus) throws HandleException {
      byte firstByte = rawStatus[0];
      if (this.isTracerOn()) {
         this.traceNormal("parseCashDrawerStatus: rawStatus=" + Util.toFormatedHexString(rawStatus));
      }

      if ((firstByte & -128) == -128) {
         throw new HandleException("Command rejected from CashDrawer");
      } else {
         int cdStatus = -100;
         byte openSensorMask = (byte)(this.getCdNumber() == 1 ? 64 : 32);
         byte var5;
         if ((firstByte & openSensorMask) == openSensorMask) {
            this.openState = true;
            var5 = 1;
         } else {
            this.openState = false;
            var5 = 0;
         }

         return var5;
      }
   }

   protected int getCdNumber() {
      return this.cdNumber;
   }

   protected boolean getOpenState() {
      return this.openState;
   }

   protected class CDStatusParser {
      private byte statusByte0 = 0;
      private byte statusByte1 = 0;

      public CDStatusParser() {
      }

      public void parse(byte[] statusArray) throws IllegalArgumentException {
         if (statusArray == null) {
            throw new IllegalArgumentException("Invalid CD status bytes to parse: cannot be null");
         } else if (statusArray.length < 2) {
            throw new IllegalArgumentException("Invalid CD status bytes to parse: length cannot be < 2");
         } else {
            this.statusByte0 = statusArray[0];
            this.statusByte1 = statusArray[1];
         }
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("<CDStatus byte0=\"0x" + Util.toHexString(this.statusByte0) + "\" byte1=\"0x" + Util.toHexString(this.statusByte1) + "\">\n");
         sb.append("\t<firstByte>\n");
         sb.append("\tcommandReject=\"" + this.isCommandReject() + "\"\n");
         sb.append("\tcd1Opened=\"" + this.isCD1Opened() + "\"\n");
         sb.append("\tcd2Opened=\"" + this.isCD2Opened() + "\"\n");
         sb.append("\tcd1Connected=\"" + this.isCD1Connected() + "\"\n");
         sb.append("\tcd2Connected=\"" + this.isCD2Connected() + "\"\n");
         sb.append("\talarmSet=\"" + this.isAlarmSet() + "\"\n");
         sb.append("\t</firstByte>\n");
         sb.append("\t<secondByte>\n");
         sb.append("\tcd1DriverOnDuringOpenCycle=\"" + this.isCD1DriverOnDuringOpenCycle() + "\"\n");
         sb.append("\tcd2DriverOnDuringOpenCycle=\"" + this.isCD2DriverOnDuringOpenCycle() + "\"\n");
         sb.append("\tcdCommandComplete=\"" + this.isCDCommandComplete() + "\"\n");
         sb.append("\tsramTestBad=\"" + this.isSRAMTestBad() + "\"\n");
         sb.append("\tcdBusy=\"" + this.isCDBusy() + "\"\n");
         sb.append("\t</secondByte>\n");
         sb.append("</CDStatus>");
         return sb.toString();
      }

      public byte getFirstStatusByte() {
         return this.statusByte0;
      }

      public byte getSecondStatusByte() {
         return this.statusByte1;
      }

      public boolean isCommandReject() {
         return PosjUtil.isBitSelected(this.getFirstStatusByte(), 7);
      }

      public boolean isCD1Opened() {
         return PosjUtil.isBitSelected(this.getFirstStatusByte(), 6);
      }

      public boolean isCD2Opened() {
         return PosjUtil.isBitSelected(this.getFirstStatusByte(), 5);
      }

      public boolean isCD1Connected() {
         return !PosjUtil.isBitSelected(this.getFirstStatusByte(), 4);
      }

      public boolean isCD2Connected() {
         return !PosjUtil.isBitSelected(this.getFirstStatusByte(), 3);
      }

      public boolean isAlarmSet() {
         return PosjUtil.isBitSelected(this.getFirstStatusByte(), 0);
      }

      public boolean isCD1DriverOnDuringOpenCycle() {
         return PosjUtil.isBitSelected(this.getSecondStatusByte(), 6);
      }

      public boolean isCD2DriverOnDuringOpenCycle() {
         return PosjUtil.isBitSelected(this.getSecondStatusByte(), 5);
      }

      public boolean isCDCommandComplete() {
         return PosjUtil.isBitSelected(this.getSecondStatusByte(), 3);
      }

      public boolean isSRAMTestBad() {
         return PosjUtil.isBitSelected(this.getSecondStatusByte(), 2);
      }

      public boolean isCDBusy() {
         return PosjUtil.isBitSelected(this.getSecondStatusByte(), 1);
      }
   }

   protected class CDSystemCmdV extends DefaultSystemCmdV {
      private byte[] cmdBytes = new byte[0];

      public void reset() {
         this.cmdBytes = new byte[0];
      }

      public byte[] getCmdBytes() {
         return this.cmdBytes;
      }

      public void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
         this.cmdBytes = HidCashDrawerHandleImp.TEST_REQUEST_CMD;
      }

      public void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
         this.cmdBytes = HidCashDrawerHandleImp.STATUS_REQUEST_CMD;
      }

      public void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
         this.cmdBytes = HidCashDrawerHandleImp.DEVICE_INFO_REQUEST_CMD;
      }

      public void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
         this.cmdBytes = HidCashDrawerHandleImp.RESET_REQUEST_CMD;
      }
   }

   protected class HandleCmdFilterV extends DefaultHandleCmdV {
      private boolean isCashDrawerCmd = false;
      private boolean isSystemCmd = false;
      private boolean isInvalidCmd = false;

      public boolean isSystemCmd() {
         return this.isSystemCmd;
      }

      public boolean isCashDrawerCmd() {
         return this.isCashDrawerCmd;
      }

      public boolean isInvalidCmd() {
         return this.isInvalidCmd;
      }

      public void reset() {
         this.isInvalidCmd = false;
         this.isSystemCmd = false;
         this.isCashDrawerCmd = false;
      }

      protected void visitHandleCmd(HandleCmd cmd) {
         this.isInvalidCmd = true;
      }

      public void visitSystemCmd(SystemCmd cmd) {
         this.isSystemCmd = true;
      }

      public void visitCashDrawerCmd(CashDrawerCmd cmd) {
         this.isCashDrawerCmd = true;
      }
   }
}
