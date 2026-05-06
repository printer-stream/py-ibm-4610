package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jutil.Util;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.POSKeyboardCmd;
import com.ibm.posj.POSKeyboardConst;
import com.ibm.posj.POSKeyboardHandle;
import com.ibm.posj.RuntimePosException;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.POSKeyboardHandleImp;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.kbd.DefaultPOSKeyboardIndicator;
import com.ibm.posj.kbd.POSIndicator;
import com.ibm.posj.kbd.POSKeyboardConfig;
import com.ibm.posj.kbd.POSKeyboardIndicator;
import com.ibm.posj.kbd.PS2Indicator;
import com.ibm.posj.util.DefaultPOSKeyboardCmdV;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.POSKeyboardUtil;
import com.ibm.posj.util.PosjUtil;
import com.ibm.posj.util.RetryHelper;

public class Rs485POSKeyboardHandleImp extends AbstractRs485HandleImp implements POSKeyboardHandleImp, POSKeyboardConst, Rs485POSKeyboardConst {
   private byte[] lastConfigCmd = null;
   private int deviceId = -1;
   private boolean isSBCS = false;
   private Object lockObj = new Object();
   private POSKeyboardIndicator poskeyboardIndicator = new DefaultPOSKeyboardIndicator();
   private PS2Indicator ps2IndicatorState = null;
   private Rs485POSKeyboardHandleImp.SubmitPosKbdCmdV submitPosKbdCmdV = new Rs485POSKeyboardHandleImp.SubmitPosKbdCmdV();
   private RetryHelper retryHelper = null;
   private static final int DEV_INFO_RESPONSE_BIT = 2;
   private static final int DEV_INFO_RESPONSE_BYTE = 1;
   private static final int DEV_INFO_ID_BYTE = 5;
   private static final int CMD_REJECT_BYTE = 1;
   private static final int CMD_REJECT_RES_BIT = 7;
   private static final int STATUS_RESPONSE_LENGTH = 4;
   public static final long COMMAND_TIMEOUT = 500L;

   public Rs485POSKeyboardHandleImp(HandleKey key, SioDevice sioDevice) {
      super(key, sioDevice);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitPOSKeyboard(this);
   }

   public DevCat getDevCat() {
      return DevCats.POSKEYBOARD_DEVCAT;
   }

   public PS2Indicator getPS2IndicatorState() {
      if (this.ps2IndicatorState == null) {
         this.ps2IndicatorState = new DefaultPOSKeyboardIndicator();
      }

      return this.ps2IndicatorState;
   }

   public void init() throws HandleException {
      super.init();
      this.submit(this.getHandle().getSystemCmdFactory().createDeviceInfoRequestCmd());
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            if (cmd instanceof POSKeyboardCmd) {
               ((POSKeyboardCmd)cmd).accept(this.submitPosKbdCmdV);
            } else {
               if (!(cmd instanceof SystemCmd)) {
                  throw new HandleException("Invalid POSKeyboardCmd object submitted!");
               }

               ((SystemCmd)cmd).accept(this.submitPosKbdCmdV);
            }

            if (cmd.getCode() == 702) {
               this.submitIndicatorCmd((POSKeyboardCmd.IndicatorCmd)cmd);
            } else if (cmd.getCode() == 103) {
               this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)cmd);
            } else if (cmd.getCode() == 102) {
               this.submitSync(cmd.toBytes());
            } else if (cmd.getCode() != 703) {
               if (cmd.getCode() == 104) {
                  this.submitCmd(cmd, 1);
               } else {
                  this.submitCmd(cmd, 1);
               }
            }
         } catch (HandleException var6) {
            this.setHandleCmdResultInError(cmd, true);
            throw var6;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   protected void reinitialize() throws HandleException {
      this.getPOSKeyboardHandle().reset();
      if (this.lastConfigCmd != null) {
         this.submitSync(this.lastConfigCmd);
      }

      POSKeyboardIndicator kbdI = this.getCurrentSettings();
      if (this.isSBCS) {
         Rs485POSKeyboardConst.POSKEYBOARD_INDICATOR_REQUEST_CMD[1] = this.createSBCSIndicatorConfig(kbdI);
      } else {
         Rs485POSKeyboardConst.POSKEYBOARD_INDICATOR_REQUEST_CMD[1] = this.createDBCSIndicatorConfig(kbdI);
      }

      this.submitSync(Rs485POSKeyboardConst.POSKEYBOARD_INDICATOR_REQUEST_CMD);
      this.setPOSKeyboardIndicator(kbdI);
      this.getPOSKeyboardHandle().setPOSIndicatorState(this.getPOSKeyboardIndicator());
      this.setPS2IndicatorState(this.getPOSKeyboardIndicator());
   }

   protected RetryHelper getRetryHelper() {
      if (this.retryHelper == null) {
         this.retryHelper = new RetryHelper();
      }

      return this.retryHelper;
   }

   protected void submitCmd(HandleCmd cmd, int times) throws HandleException {
      RetryHelper rh = this.getRetryHelper();
      rh.setLastCmd(cmd);
      rh.setCmdPending(true);
      this.submitSync(cmd.toBytes());

      int retry;
      for (retry = 0; rh.isCmdPending() && retry < times; retry++) {
         try {
            rh.waitCmdPending(500L);
         } catch (InterruptedException var6) {
         }

         if (!rh.isCmdPending()) {
            if (this.isCmdRejected(rh.getLastResponse())) {
               throw new HandleException(" Command rejected for " + cmd.getName());
            }

            return;
         }

         this.submitSync(cmd.toBytes());
      }

      if (retry >= times && rh.isCmdPending()) {
         throw new HandleException("timeout while waiting for " + cmd.getName() + " command to complete");
      }
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) throws HandleException {
      this.submitCmd(devInfoCmd, 1);
      if (this.deviceId == -1) {
         devInfoCmd.setDeviceId(3601);
      } else {
         devInfoCmd.setDeviceId(this.deviceId);
      }
   }

   protected void submitIndicatorCmd(POSKeyboardCmd.IndicatorCmd indicatorCmd) throws HandleException {
      this.submitCmd(indicatorCmd, 1);
      this.getPOSKeyboardHandle().setPOSIndicatorState(this.getPOSKeyboardIndicator());
      this.setPS2IndicatorState(this.getPOSKeyboardIndicator());
   }

   protected void setPOSKeyboardIndicator(POSKeyboardIndicator i) {
      this.poskeyboardIndicator = i;
   }

   protected POSKeyboardIndicator getPOSKeyboardIndicator() {
      return this.poskeyboardIndicator;
   }

   protected POSKeyboardHandle getPOSKeyboardHandle() {
      return (POSKeyboardHandle)this.getHandle();
   }

   protected int getDeviceId(byte[] response) {
      int devId = POSKeyboardUtil.getRs485POSKeyboardID(response);
      if (POSKeyboardUtil.isDBCS(devId)) {
         this.isSBCS = false;
      } else {
         this.isSBCS = true;
      }

      return devId;
   }

   protected boolean isDevInfoResult(byte[] response) {
      return response.length >= 6 ? PosjUtil.isBitSelected(response[1], 2) : false;
   }

   protected boolean isCmdRejected(byte[] response) {
      return response.length >= 4 ? PosjUtil.isBitSelected(response[1], 7) : false;
   }

   protected void dataEventOccurred(SioDeviceDataEvent event) {
      byte[] data = event.getData();
      if (this.isTracerOn()) {
         this.traceNormal("-->dataEventOcurred()");
         this.traceNormal("Event->" + Util.toFormatedHexString(data));
      }

      if (this.getHandle().isDirectIOMode()) {
         this.getHandle().getEventHelper().fireDirectIOEvent(new DirectIOEvent(this, data));
         if (this.isTracerOn()) {
            this.traceNormal("DirectIOEvent Ocurred");
         }
      }

      if (this.isDevInfoResult(data)) {
         if (this.deviceId == -1) {
            byte[] dataArray = new byte[data.length - 4];
            System.arraycopy(data, 4, dataArray, 0, dataArray.length);
            this.deviceId = this.getDeviceId(dataArray);
            this.traceNormal("DeviceId to use ->" + this.deviceId);
         }
      } else if (data.length > 4) {
         byte[] dataArray = new byte[data.length - 4];
         System.arraycopy(data, 4, dataArray, 0, dataArray.length);
         if (this.getHandle() != null) {
            this.getHandle().getEventHelper().fireDataEvent(new DataEvent(this.getClass(), dataArray));
         }
      }

      RetryHelper rh = this.getRetryHelper();
      if (rh.isCmdPending()) {
         rh.setCmdPending(false);
         rh.setLastResponse(data);
         rh.notifyAllCmdPending();
      }

      if (this.isTracerOn()) {
         this.traceNormal("<--dataEventOcurred()");
      }
   }

   protected POSKeyboardIndicator getCurrentSettings() {
      POSKeyboardIndicator kbdI = new DefaultPOSKeyboardIndicator();
      POSIndicator posIndicator = this.getPOSKeyboardHandle().getPOSIndicatorState();
      PS2Indicator ps2Indicator = this.getPS2IndicatorState();
      kbdI.setWait(posIndicator.getWait());
      kbdI.setOffline(posIndicator.getOffline());
      kbdI.setMsgPendOrSysMsg(posIndicator.getMsgPendOrSysMsg());
      kbdI.setBlankOrReady(posIndicator.getBlankOrReady());
      kbdI.setNumLock(ps2Indicator.getNumLock());
      kbdI.setCapsLock(ps2Indicator.getCapsLock());
      kbdI.setScrollLock(ps2Indicator.getScrollLock());
      return kbdI;
   }

   protected void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
      cmd.setCmdBytes(Rs485POSKeyboardConst.POSKEYBOARD_TEST_REQUEST_CMD);
   }

   protected void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
      cmd.setCmdBytes(Rs485POSKeyboardConst.POSKEYBOARD_STATUS_REQUEST_CMD);
   }

   protected void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
      cmd.setCmdBytes(Rs485POSKeyboardConst.POSKEYBOARD_DEV_INFO_REQUEST_CMD);
   }

   protected void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
      cmd.setCmdBytes(Rs485POSKeyboardConst.POSKEYBOARD_RESET_REQUEST_CMD);
   }

   protected void visitConfigCmd(POSKeyboardCmd.ConfigCmd cmd) throws RuntimePosException {
      POSKeyboardConfig config = cmd.getConfig();
      byte ppByte = 0;
      if (config.getClick() == 2) {
         ppByte = 8;
      } else if (config.getClick() == 1) {
         ppByte = 4;
      } else {
         if (config.getClick() != 0) {
            throw new RuntimePosException("Invalid Click Configuration received");
         }

         ppByte = 0;
      }

      if (config.getTypematic()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 1);
      }

      if (config.getKbdScanning()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 0);
      }

      byte[][] arrayValues = Rs485POSKeyboardConst.TYPEMATIC_DELAY_VALUES;
      byte valueToSearch = config.getTypematicDelay();
      byte rdDelayByte = this.searchValue(valueToSearch, arrayValues);
      if (rdDelayByte == -1) {
         throw new RuntimePosException("Invalid Typematic delay Configuration received");
      } else {
         arrayValues = Rs485POSKeyboardConst.TYPEMATIC_RATE_VALUES;
         valueToSearch = config.getTypematicRate();
         byte rdRateByte = this.searchValue(valueToSearch, arrayValues);
         if (rdRateByte == -1) {
            throw new RuntimePosException("Invalid Fat-finger Configuration received");
         } else {
            arrayValues = Rs485POSKeyboardConst.FAT_FINGER_VALUES;
            valueToSearch = config.getFatFingerTimeout();
            byte ttByte = this.searchValue(valueToSearch, arrayValues);
            if (ttByte == -1) {
               throw new RuntimePosException("Invalid Fat-finger Configuration received");
            } else {
               Rs485POSKeyboardConst.POSKEYBOARD_CONFIG_REQUEST_CMD[1] = ppByte;
               Rs485POSKeyboardConst.POSKEYBOARD_CONFIG_REQUEST_CMD[2] = (byte)(rdDelayByte | rdRateByte);
               Rs485POSKeyboardConst.POSKEYBOARD_CONFIG_REQUEST_CMD[3] = ttByte;
               this.lastConfigCmd = Rs485POSKeyboardConst.POSKEYBOARD_CONFIG_REQUEST_CMD;
               cmd.setCmdBytes(Rs485POSKeyboardConst.POSKEYBOARD_CONFIG_REQUEST_CMD);
            }
         }
      }
   }

   protected void visitIndicatorCmd(POSKeyboardCmd.IndicatorCmd cmd) {
      POSKeyboardIndicator kbdI = this.getCurrentSettings();
      int led = cmd.getIndicator();
      boolean action = cmd.getAction();
      if (1 == led) {
         kbdI.setWait(action);
      } else if (2 == led) {
         kbdI.setOffline(action);
      } else if (3 == led) {
         kbdI.setMsgPendOrSysMsg(action);
      } else if (4 == led) {
         kbdI.setBlankOrReady(action);
      } else if (5 == led) {
         kbdI.setNumLock(action);
      } else if (6 == led) {
         kbdI.setCapsLock(action);
      } else if (7 == led) {
         kbdI.setScrollLock(action);
      } else {
         if (0 != led) {
            throw new RuntimePosException("Illegal indicator received");
         }

         this.turnAll(kbdI, action);
      }

      if (this.isSBCS) {
         Rs485POSKeyboardConst.POSKEYBOARD_INDICATOR_REQUEST_CMD[1] = this.createSBCSIndicatorConfig(kbdI);
      } else {
         Rs485POSKeyboardConst.POSKEYBOARD_INDICATOR_REQUEST_CMD[1] = this.createDBCSIndicatorConfig(kbdI);
      }

      cmd.setCmdBytes(Rs485POSKeyboardConst.POSKEYBOARD_INDICATOR_REQUEST_CMD);
      this.setPOSKeyboardIndicator(kbdI);
   }

   protected byte searchValue(byte valueToSearch, byte[][] arrayValues) {
      for (int count = 0; count < arrayValues.length; count++) {
         if (arrayValues[count][0] == valueToSearch) {
            return arrayValues[count][1];
         }
      }

      return -1;
   }

   protected byte createSBCSIndicatorConfig(POSKeyboardIndicator indicators) {
      byte ppByte = 0;
      if (indicators.getWait()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 0);
      }

      if (indicators.getOffline()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 1);
      }

      if (indicators.getMsgPendOrSysMsg()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 2);
      }

      if (indicators.getBlankOrReady()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 3);
      }

      if (indicators.getNumLock()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 5);
      }

      if (indicators.getCapsLock()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 6);
      }

      if (indicators.getScrollLock()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 4);
      }

      return ppByte;
   }

   protected byte createDBCSIndicatorConfig(POSKeyboardIndicator indicators) {
      byte ppByte = 0;
      if (indicators.getWait()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 2);
      }

      if (indicators.getOffline()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 1);
      }

      if (indicators.getMsgPendOrSysMsg()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 0);
      }

      if (indicators.getBlankOrReady()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 3);
      }

      if (indicators.getNumLock()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 5);
      }

      if (indicators.getCapsLock()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 6);
      }

      if (indicators.getScrollLock()) {
         ppByte = (byte)PosjUtil.setBit(ppByte, 4);
      }

      return ppByte;
   }

   protected void turnAll(POSKeyboardIndicator indicator, boolean action) {
      indicator.setWait(action);
      indicator.setOffline(action);
      indicator.setMsgPendOrSysMsg(action);
      indicator.setBlankOrReady(action);
      indicator.setNumLock(action);
      indicator.setCapsLock(action);
      indicator.setScrollLock(action);
   }

   protected void setPS2IndicatorState(PS2Indicator indicator) {
      this.ps2IndicatorState = indicator;
   }

   private class SubmitPosKbdCmdV extends DefaultPOSKeyboardCmdV {
      private SubmitPosKbdCmdV() {
      }

      public void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
         Rs485POSKeyboardHandleImp.this.visitTestSystemCmd(cmd);
      }

      public void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
         Rs485POSKeyboardHandleImp.this.visitStatusSystemCmd(cmd);
      }

      public void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
         Rs485POSKeyboardHandleImp.this.visitDevInfoSystemCmd(cmd);
      }

      public void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
         Rs485POSKeyboardHandleImp.this.visitResetSystemCmd(cmd);
      }

      public void visitConfigCmd(POSKeyboardCmd.ConfigCmd cmd) {
         Rs485POSKeyboardHandleImp.this.visitConfigCmd(cmd);
      }

      public void visitIndicatorCmd(POSKeyboardCmd.IndicatorCmd cmd) {
         Rs485POSKeyboardHandleImp.this.visitIndicatorCmd(cmd);
      }
   }
}
