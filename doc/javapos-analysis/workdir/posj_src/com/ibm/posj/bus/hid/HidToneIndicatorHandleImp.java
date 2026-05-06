package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.Util;
import com.ibm.posj.GenToneIndicatorCmd;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.ToneIndicatorCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.ToneIndicatorHandleImp;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PosjUtil;

public class HidToneIndicatorHandleImp extends AbstractHidHandleImp implements HidHandleImp, ToneIndicatorHandleImp {
   protected GenToneIndicatorCmd genTICmd = new GenToneIndicatorCmd();
   private HidToneIndicatorStrategy strategy = null;
   private int deviceId = -1;
   private Object lockObj = new Object();
   public static final int DELAY = 500;
   public static final int DEV_INFO_BIT = 2;

   public HidToneIndicatorHandleImp(HandleKey key, HidDevice device, HidToneIndicatorStrategy strategy) {
      super(key, device);
      this.strategy = strategy;
      this.getGenTICmd().createCommonFrequencies = this.getStrategy().getFrequencyValues();
   }

   public void init() throws HandleException {
      super.init();
      if (this.isTracerOn()) {
         this.traceNormal("->init() :Using strategy: " + this.getStrategy());
         int[][] f = this.getGenTICmd().createCommonFrequencies;
         String freq = "Frequencies :";
         if (f.length > 0 && f[0].length >= 3) {
            freq = freq + f[0][0] + "," + f[0][1] + "," + f[0][2];
         }

         this.traceNormal("<-init() :Frequencies " + freq);
      }
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (this.isLocked.isTrue()) {
         throw new HandleException("Attempting to submit to ToneIndicator while flashing");
      } else if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            if (cmd.getCode() == 1000) {
               this.submitToneCmd((ToneIndicatorCmd.ToneCmd)cmd);
            } else if (cmd.getCode() == 101) {
               this.submitStatusCmd((SystemCmd.StatusRequestCmd)cmd);
            } else if (cmd.getCode() == 100) {
               this.submitTestCmd((SystemCmd.TestRequestCmd)cmd);
            } else if (cmd.getCode() == 103) {
               this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)cmd);
            } else {
               if (cmd.getCode() != 102) {
                  throw new HandleException("Invalid ToneIndicatorCmd object submitted!");
               }

               this.submitResetCmd((SystemCmd.ResetRequestCmd)cmd);
            }
         } catch (HandleException var6) {
            this.setHandleCmdResultInError(cmd, true);
            throw var6;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidToneIndicatorHandleImp(this);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitToneIndicator(this);
   }

   public DevCat getDevCat() {
      return DevCats.TONEINDICATOR_DEVCAT;
   }

   public boolean isComposite() {
      return true;
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) throws HandleException {
      this.getGenTICmd();
      devInfoCmd.setCmdBytes(GenToneIndicatorCmd.TONEINDICATOR_DEV_INFO_REQUEST_CMD);
      this.submitCmd(devInfoCmd.getName(), devInfoCmd.toBytes());
      if (this.deviceId == -1) {
         synchronized (this.lockObj) {
            this.submitCmd(devInfoCmd.getName(), devInfoCmd.toBytes());

            try {
               this.lockObj.wait(500L);
            } catch (InterruptedException var8) {
            }
         }

         if (this.deviceId == -1) {
            synchronized (this.lockObj) {
               this.submitCmd(devInfoCmd.getName(), devInfoCmd.toBytes());

               try {
                  this.lockObj.wait(500L);
               } catch (InterruptedException var6) {
               }
            }
         }
      }

      if (this.deviceId == -1) {
         devInfoCmd.setDeviceId(4401);
      } else {
         devInfoCmd.setDeviceId(this.deviceId);
      }

      devInfoCmd.setFirmwareLevel(this.getBCDLevel());
      devInfoCmd.setSerialNumber(this.getSerialNumber());
   }

   protected void submitStatusCmd(SystemCmd.StatusRequestCmd statusCmd) throws HandleException {
      this.getGenTICmd();
      statusCmd.setCmdBytes(GenToneIndicatorCmd.POSKEYBOARD_STATUS_REQUEST_CMD);
      this.submitCmd(statusCmd.getName(), statusCmd.toBytes());
   }

   protected void submitTestCmd(SystemCmd.TestRequestCmd testCmd) throws HandleException {
      this.getGenTICmd();
      testCmd.setCmdBytes(GenToneIndicatorCmd.POSKEYBOARD_TEST_REQUEST_CMD);
      this.submitCmd(testCmd.getName(), testCmd.toBytes());
   }

   protected void submitResetCmd(SystemCmd.ResetRequestCmd resetCmd) throws HandleException {
   }

   protected void submitToneCmd(ToneIndicatorCmd.ToneCmd toneCmd) throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal(
            "ToneCmd parameters for this submit are : Duration-> "
               + toneCmd.getDuration()
               + " Frequency-> "
               + toneCmd.getFrequency()
               + " Volume-> "
               + toneCmd.getVolume()
         );
      }

      this.getGenTICmd().formatToneIndicatorCmd(toneCmd);
      this.getGenTICmd();
      toneCmd.setCmdBytes(GenToneIndicatorCmd.TONEINDICATOR_CMD);
      if (this.isTracerOn()) {
         StringBuffer var10001 = new StringBuffer().append("ToneCmd to submit is -> :");
         this.getGenTICmd();
         this.traceNormal(var10001.append(Util.toFormatedHexString(GenToneIndicatorCmd.TONEINDICATOR_CMD)).toString());
      }

      this.submitCmd(toneCmd.getName(), toneCmd.toBytes());
      this.getGenTICmd().lockUntilSoundCompleted(toneCmd.getDuration());
      if (this.getGenTICmd().isToneActive) {
         this.getGenTICmd().isToneActive = false;
         this.getHandle().getEventHelper().fireStatusEvent(new StatusEvent(this, 1));
      }
   }

   protected HidToneIndicatorStrategy getStrategy() {
      return this.strategy;
   }

   protected void reportEventOccurred(ReportEvent rE) {
      if (this.isHandleInit()) {
         byte[] data = rE.getData();
         int byteNumber = this.getStrategy().getByteToneState();
         Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
         if (this.isTracerOn()) {
            this.traceNormal("reportEventOccurred -> " + Util.toFormatedHexString(data));
         }

         if (this.isDevInfoResult(rE.getData()) && this.deviceId == -1) {
            this.deviceId = this.getStrategy().getDeviceId(rE.getData());
            synchronized (this.lockObj) {
               this.lockObj.notifyAll();
            }

            if (this.isTracerOn()) {
               this.traceNormal("reportEventOccurred(): deviceId to use ->" + this.deviceId);
            }
         }

         if (PosjUtil.isBitSelected(data[byteNumber], 0)) {
            this.getGenTICmd().isToneActive = true;
            eventHelper.fireStatusEvent(new StatusEvent(this, 0));
         } else {
            this.getGenTICmd().isToneActive = false;
            eventHelper.fireStatusEvent(new StatusEvent(this, 1));
            synchronized (this.getGenTICmd().lockobj) {
               this.getGenTICmd().lockobj.notifyAll();
            }
         }
      }
   }

   protected boolean isDevInfoResult(byte[] response) {
      return response.length >= 6 ? PosjUtil.isBitSelected(response[this.getStrategy().getDevInfoPosition()], 2) : false;
   }

   protected GenToneIndicatorCmd getGenTICmd() {
      return this.genTICmd;
   }
}
