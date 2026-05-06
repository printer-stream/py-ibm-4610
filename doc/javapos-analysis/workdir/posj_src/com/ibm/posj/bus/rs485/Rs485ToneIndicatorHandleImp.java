package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.event.SioDeviceDataEvent;
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
import com.ibm.posj.util.ToneIndicatorUtil;

public class Rs485ToneIndicatorHandleImp extends AbstractRs485HandleImp implements ToneIndicatorHandleImp {
   private GenToneIndicatorCmd genTICmd = new GenToneIndicatorCmd();
   private int deviceId = -1;
   public static final int DEV_INFO_RESPONSE_BIT = 2;
   public static final int DEV_INFO_RESPONSE_BYTE = 1;

   public Rs485ToneIndicatorHandleImp(HandleKey key, SioDevice device) {
      super(key, device);
   }

   public void init() throws HandleException {
      super.init();
      this.getGenTICmd();
      this.submitSync(GenToneIndicatorCmd.TONEINDICATOR_DEV_INFO_REQUEST_CMD);
      if (this.deviceId == -1) {
         synchronized (this.getGenTICmd().lockobj) {
            try {
               this.getGenTICmd().lockobj.wait(1000L);
            } catch (InterruptedException var4) {
            }
         }
      }

      if (this.deviceId == -1) {
         throw new HandleException("Unable to identify the Device - timeout 1000 ms");
      } else {
         this.setStatusByteToneState();
         if (this.isTracerOn()) {
            this.traceNormal("ToneIndicatorID -> " + this.deviceId);
         }

         if (this.deviceId == 4437 || this.deviceId == 4441 || this.deviceId == 4439 || this.deviceId == 4442) {
            GenToneIndicatorCmd var7 = this.getGenTICmd();
            int[][] var9 = new int[][]{{600, 1010, 1603}, null};
            int[] var11 = new int[3];
            this.getGenTICmd();
            var11[0] = 0;
            this.getGenTICmd();
            var11[1] = 8;
            this.getGenTICmd();
            var11[2] = 16;
            var9[1] = var11;
            var7.createCommonFrequencies = var9;
         } else if (this.deviceId == 4443) {
            GenToneIndicatorCmd var10000 = this.getGenTICmd();
            int[][] var10001 = new int[][]{{943, 1562, 1603}, null};
            int[] var10004 = new int[3];
            this.getGenTICmd();
            var10004[0] = 0;
            this.getGenTICmd();
            var10004[1] = 8;
            this.getGenTICmd();
            var10004[2] = 16;
            var10001[1] = var10004;
            var10000.createCommonFrequencies = var10001;
         } else {
            GenToneIndicatorCmd var6 = this.getGenTICmd();
            int[][] var8 = new int[][]{{875, 1300, 2000}, null};
            int[] var10 = new int[3];
            this.getGenTICmd();
            var10[0] = 0;
            this.getGenTICmd();
            var10[1] = 8;
            this.getGenTICmd();
            var10[2] = 16;
            var8[1] = var10;
            var6.createCommonFrequencies = var8;
         }
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
      this.submitSync(GenToneIndicatorCmd.TONEINDICATOR_DEV_INFO_REQUEST_CMD);
      if (this.deviceId == -1) {
         devInfoCmd.setDeviceId(2910);
      } else {
         devInfoCmd.setDeviceId(this.deviceId);
      }
   }

   protected void submitStatusCmd(SystemCmd.StatusRequestCmd statusCmd) throws HandleException {
      this.getGenTICmd();
      this.submitSync(GenToneIndicatorCmd.POSKEYBOARD_STATUS_REQUEST_CMD);
   }

   protected void submitTestCmd(SystemCmd.TestRequestCmd testCmd) throws HandleException {
      this.getGenTICmd();
      this.submitSync(GenToneIndicatorCmd.POSKEYBOARD_TEST_REQUEST_CMD);
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
      if (this.isTracerOn()) {
         StringBuffer var10001 = new StringBuffer().append("ToneCmd to submit is -> :");
         this.getGenTICmd();
         this.traceNormal(var10001.append(Util.toFormatedHexString(GenToneIndicatorCmd.TONEINDICATOR_CMD)).toString());
      }

      this.getGenTICmd();
      this.submitSync(GenToneIndicatorCmd.TONEINDICATOR_CMD);
      this.getGenTICmd().lockUntilSoundCompleted(toneCmd.getDuration());
      if (this.getGenTICmd().isToneActive) {
         this.getGenTICmd().isToneActive = false;
         this.getHandle().getEventHelper().fireStatusEvent(new StatusEvent(this, 1));
      }
   }

   private GenToneIndicatorCmd getGenTICmd() {
      return this.genTICmd;
   }

   private void setStatusByteToneState() {
      this.getGenTICmd().statusByteToneState = 2;
   }

   private byte getStatusByteToneState() {
      return this.getGenTICmd().statusByteToneState;
   }

   public void dataEventOccurred(SioDeviceDataEvent event) {
      byte[] data = event.getData();
      if (this.isTracerOn()) {
         this.traceNormal("dataEventOccurred -> " + Util.toFormatedHexString(data));
      }

      if (this.isDevInfoResult(data) && this.deviceId == -1) {
         this.deviceId = this.getDeviceId(data);
         synchronized (this.getGenTICmd().lockobj) {
            this.getGenTICmd().lockobj.notifyAll();
         }
      }

      if (this.getHandle() != null) {
         Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
         if (PosjUtil.isBitSelected(data[this.getStatusByteToneState()], 0)) {
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
      return response.length >= 6 ? PosjUtil.isBitSelected(response[1], 2) : false;
   }

   protected int getDeviceId(byte[] response) {
      return ToneIndicatorUtil.getRs485ToneIndicatorID(response);
   }
}
