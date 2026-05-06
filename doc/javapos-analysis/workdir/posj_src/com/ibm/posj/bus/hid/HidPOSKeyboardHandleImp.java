package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.ReportEvent;
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
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.kbd.DefaultPOSKeyboardIndicator;
import com.ibm.posj.kbd.POSIndicator;
import com.ibm.posj.kbd.POSKeyboardConfig;
import com.ibm.posj.kbd.POSKeyboardIndicator;
import com.ibm.posj.kbd.PS2Indicator;
import com.ibm.posj.util.DefaultPOSKeyboardCmdV;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PosjUtil;

public class HidPOSKeyboardHandleImp extends AbstractHidHandleImp implements HidHandleImp, POSKeyboardHandleImp, POSKeyboardConst, HidPOSKeyboardConst {
   private int deviceId = -1;
   private Object lockObj = new Object();
   private HidPOSKeyboardHandleImp.SubmitPosKbdCmdV submitPosKbdCmdV = new HidPOSKeyboardHandleImp.SubmitPosKbdCmdV();
   private POSKeyboardIndicator poskeyboardIndicator = new DefaultPOSKeyboardIndicator();
   private HidPOSKeyboardStrategy strategy = null;
   private PS2Indicator ps2IndicatorState = null;

   public HidPOSKeyboardHandleImp(HandleKey key, HidDevice device, HidPOSKeyboardStrategy strategy) {
      super(key, device);
      this.strategy = strategy;
      this.turnIndicatorsOff();
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitPOSKeyboard(this);
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidPOSKeyboardHandleImp(this);
   }

   public DevCat getDevCat() {
      return DevCats.POSKEYBOARD_DEVCAT;
   }

   public void init() throws HandleException {
      super.init();
      this.initialize();
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
            } else if (cmd.getCode() != 102) {
               if (cmd.getCode() == 703) {
                  this.submitEnableCmd((POSKeyboardCmd.EnableCmd)cmd);
               } else if (cmd.getCode() == 104) {
                  this.submitCmd(cmd.getName(), cmd.toBytes());
               } else {
                  this.submitCmd(cmd.getName(), cmd.toBytes());
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

   protected void submitEnableCmd(POSKeyboardCmd.EnableCmd enableCmd) {
   }

   public PS2Indicator getPS2IndicatorState() {
      return this.getPs2IndicatorState();
   }

   public boolean isLegacyKbd() {
      return this.strategy instanceof DefaultHidPOSKeyboardStrategy.LegacyModeDBCSPOSKeyboardStrategy
         || this.strategy instanceof DefaultHidPOSKeyboardStrategy.LegacyModeSBCSPOSKeyboardStrategy;
   }

   public boolean isNonBootModeOn() {
      return this.strategy instanceof DefaultHidPOSKeyboardStrategy.NonBootModeDBCSPOSKeyboardStrategy
         || this.strategy instanceof DefaultHidPOSKeyboardStrategy.NonBootModeSBCSPOSKeyboardStrategy;
   }

   protected POSKeyboardHandle getPOSKeyboardHandle() {
      return (POSKeyboardHandle)this.getHandle();
   }

   protected void initialize() throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("-->initialize() Using strategy : " + this.getStrategy() + "<--");
      }

      this.deviceId = -1;
   }

   protected void reinitialize() throws HandleException {
      this.initialize();
      this.getPOSKeyboardHandle().reset();
      this.turnIndicatorsOff();
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) throws HandleException {
      this.submitCmd(devInfoCmd.getName(), devInfoCmd.toBytes());
      if (this.deviceId == -1) {
         synchronized (this.lockObj) {
            try {
               this.lockObj.wait(100L);
            } catch (InterruptedException var8) {
            }
         }

         if (this.deviceId == -1) {
            this.submitCmd(devInfoCmd.getName(), devInfoCmd.toBytes());
            synchronized (this.lockObj) {
               try {
                  this.lockObj.wait(100L);
               } catch (InterruptedException var6) {
               }
            }
         }
      }

      if (this.deviceId == -1) {
         devInfoCmd.setDeviceId(3601);
      } else {
         devInfoCmd.setDeviceId(this.deviceId);
      }

      devInfoCmd.setFirmwareLevel(this.getBCDLevel());
      devInfoCmd.setSerialNumber(this.getSerialNumber());
   }

   protected void submitIndicatorCmd(POSKeyboardCmd.IndicatorCmd indicatorCmd) throws HandleException {
      this.submitCmd(indicatorCmd.getName(), indicatorCmd.toBytes());
      this.getPOSKeyboardHandle().setPOSIndicatorState(this.getPOSKeyboardIndicator());
      this.setPS2IndicatorState(this.getPOSKeyboardIndicator());
   }

   protected void setPOSKeyboardIndicator(POSKeyboardIndicator i) {
      this.poskeyboardIndicator = i;
   }

   protected POSKeyboardIndicator getPOSKeyboardIndicator() {
      return this.poskeyboardIndicator;
   }

   protected void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
      cmd.setCmdBytes(HidPOSKeyboardConst.POSKEYBOARD_TEST_REQUEST_CMD);
   }

   protected void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
      cmd.setCmdBytes(HidPOSKeyboardConst.POSKEYBOARD_STATUS_REQUEST_CMD);
   }

   protected void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
      cmd.setCmdBytes(HidPOSKeyboardConst.POSKEYBOARD_DEV_INFO_REQUEST_CMD);
   }

   protected void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
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

      byte[][] arrayValues = HidPOSKeyboardConst.TYPEMATIC_DELAY_VALUES;
      byte valueToSearch = config.getTypematicDelay();
      byte rdDelayByte = this.searchValue(valueToSearch, arrayValues);
      if (rdDelayByte == -1) {
         throw new RuntimePosException("Invalid Typematic delay Configuration received");
      } else {
         arrayValues = HidPOSKeyboardConst.TYPEMATIC_RATE_VALUES;
         valueToSearch = config.getTypematicRate();
         byte rdRateByte = this.searchValue(valueToSearch, arrayValues);
         if (rdRateByte == -1) {
            throw new RuntimePosException("Invalid Fat-finger Configuration received");
         } else {
            arrayValues = HidPOSKeyboardConst.FAT_FINGER_VALUES;
            valueToSearch = config.getFatFingerTimeout();
            byte ttByte = this.searchValue(valueToSearch, arrayValues);
            if (ttByte == -1) {
               throw new RuntimePosException("Invalid Fat-finger Configuration received");
            } else {
               HidPOSKeyboardConst.POSKEYBOARD_CONFIG_REQUEST_CMD[1] = ppByte;
               HidPOSKeyboardConst.POSKEYBOARD_CONFIG_REQUEST_CMD[2] = (byte)(rdDelayByte | rdRateByte);
               HidPOSKeyboardConst.POSKEYBOARD_CONFIG_REQUEST_CMD[3] = ttByte;
               cmd.setCmdBytes(HidPOSKeyboardConst.POSKEYBOARD_CONFIG_REQUEST_CMD);
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

      HidPOSKeyboardConst.POSKEYBOARD_INDICATOR_REQUEST_CMD[1] = this.getStrategy().createIndicatorConfig(kbdI);
      cmd.setCmdBytes(HidPOSKeyboardConst.POSKEYBOARD_INDICATOR_REQUEST_CMD);
      this.setPOSKeyboardIndicator(kbdI);
   }

   protected boolean isDevInfoResult(byte[] response) {
      return response.length >= 6 ? PosjUtil.isBitSelected(response[this.getStrategy().getDevInfoPosition()], 2) : false;
   }

   protected byte searchValue(byte valueToSearch, byte[][] arrayValues) {
      for (int count = 0; count < arrayValues.length; count++) {
         if (arrayValues[count][0] == valueToSearch) {
            return arrayValues[count][1];
         }
      }

      return -1;
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

   protected void turnAll(POSKeyboardIndicator indicator, boolean action) {
      indicator.setWait(action);
      indicator.setOffline(action);
      indicator.setMsgPendOrSysMsg(action);
      indicator.setBlankOrReady(action);
      indicator.setNumLock(action);
      indicator.setCapsLock(action);
      indicator.setScrollLock(action);
   }

   protected boolean isCmdRejected(byte[] response) {
      return response.length >= 4 ? PosjUtil.isBitSelected(response[1], 7) : false;
   }

   protected void turnIndicatorsOff() {
      try {
         this.submitCmd("INDICATOR_POSKEYBOARD_CMD_NAME", HidPOSKeyboardConst.POSKEYBOARD_INDICATOR_REQUEST_CMD);
      } catch (HandleException var2) {
         this.getTracer().print(var2);
         this.traceNormal("Ctor() : Error at indicatorCmd submition->" + var2.toString());
      }
   }

   protected HidPOSKeyboardStrategy getStrategy() {
      return this.strategy;
   }

   protected PS2Indicator getPs2IndicatorState() {
      if (this.ps2IndicatorState == null) {
         this.ps2IndicatorState = new DefaultPOSKeyboardIndicator();
      }

      return this.ps2IndicatorState;
   }

   protected void setPS2IndicatorState(PS2Indicator indicator) {
      this.ps2IndicatorState = indicator;
   }

   protected void reportEventOccurred(ReportEvent rE) {
      byte[] data = rE.getData();
      if (this.isHandleInit()) {
         if (this.isTracerOn()) {
            this.traceNormal("reportEventOccurred() : Event->" + Util.toFormatedHexString(rE.getData()));
         }

         if (this.getHandle().isDirectIOMode()) {
            this.getHandle().getEventHelper().fireDirectIOEvent(new DirectIOEvent(this, data));
            if (this.isTracerOn()) {
               this.traceNormal("DirectIOEvent Ocurred");
            }
         }

         if (this.isCmdRejected(rE.getData())) {
            this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, -101));
         } else if (this.isDevInfoResult(rE.getData())) {
            if (this.deviceId == -1) {
               this.deviceId = this.getStrategy().getDevID(rE.getData());
               synchronized (this.lockObj) {
                  this.lockObj.notifyAll();
               }

               if (this.isTracerOn()) {
                  this.traceNormal("reportEventOccurred(): deviceId to use ->" + this.deviceId);
               }
            }
         } else {
            byte[] dataArray = new byte[rE.getData().length - this.getStrategy().getStatusLength()];
            System.arraycopy(rE.getData(), this.getStrategy().getStatusLength(), dataArray, 0, dataArray.length);
            if (this.getHandle() != null) {
               this.getHandle().getEventHelper().fireDataEvent(new DataEvent(this.getClass(), dataArray));
            }
         }
      }
   }

   protected class SubmitPosKbdCmdV extends DefaultPOSKeyboardCmdV {
      public void visitTestSystemCmd(SystemCmd.TestRequestCmd cmd) {
         HidPOSKeyboardHandleImp.this.visitTestSystemCmd(cmd);
      }

      public void visitStatusSystemCmd(SystemCmd.StatusRequestCmd cmd) {
         HidPOSKeyboardHandleImp.this.visitStatusSystemCmd(cmd);
      }

      public void visitDevInfoSystemCmd(SystemCmd.DeviceInfoRequestCmd cmd) {
         HidPOSKeyboardHandleImp.this.visitDevInfoSystemCmd(cmd);
      }

      public void visitResetSystemCmd(SystemCmd.ResetRequestCmd cmd) {
         HidPOSKeyboardHandleImp.this.visitResetSystemCmd(cmd);
      }

      public void visitConfigCmd(POSKeyboardCmd.ConfigCmd cmd) {
         HidPOSKeyboardHandleImp.this.visitConfigCmd(cmd);
      }

      public void visitIndicatorCmd(POSKeyboardCmd.IndicatorCmd cmd) {
         HidPOSKeyboardHandleImp.this.visitIndicatorCmd(cmd);
      }
   }
}
