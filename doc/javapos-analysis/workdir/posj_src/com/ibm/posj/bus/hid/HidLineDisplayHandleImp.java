package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.Util;
import com.ibm.posj.GenLineDisplayCmdV;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.LineDisplayCmd;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.LineDisplayHandleImp;
import com.ibm.posj.util.DefaultHandleCmdV;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PosjUtil;

public class HidLineDisplayHandleImp extends AbstractHidHandleImp implements HidHandleImp, LineDisplayHandleImp {
   protected HidLineDisplayHandleImp.LineDisplaySubmitV ldSubmitV = new HidLineDisplayHandleImp.LineDisplaySubmitV();
   protected int devIDPosition = 2;
   protected int statusByte = 0;
   private HidLineDisplayHandleImp.HandleCmdFilterV handleCmdFilterV = new HidLineDisplayHandleImp.HandleCmdFilterV();
   private int deviceId = -1;
   private int deviceType = -1;
   private Object lockObj = new Object();

   public HidLineDisplayHandleImp(HandleKey key, HidDevice device) {
      super(key, device);
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            this.handleCmdFilterV.reset();
            cmd.accept(this.handleCmdFilterV);
            if (this.handleCmdFilterV.isSystemCmd()) {
               if (cmd.getCode() == 101) {
                  this.getLDSubmitV();
                  this.submitCmd("STATUS_REQUEST_CMD", HidLineDisplayHandleImp.LineDisplaySubmitV.STATUS_REQUEST_CMD);
               } else if (cmd.getCode() == 100) {
                  this.getLDSubmitV();
                  this.submitCmd("TEST_REQUEST_CMD", HidLineDisplayHandleImp.LineDisplaySubmitV.TEST_REQUEST_CMD);
               } else {
                  if (cmd.getCode() != 103) {
                     throw new HandleException("Invalid SystemCmd submitted!");
                  }

                  this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)cmd);
               }
            } else {
               if (!this.handleCmdFilterV.isLineDisplayCmd()) {
                  throw new HandleException("Invalid LineDisplayCmd object submitted!");
               }

               LineDisplayCmd lineDisplayCmd = (LineDisplayCmd)cmd;
               if (lineDisplayCmd.getCode() != 400) {
                  this.getLDSubmitV().setIsClearLastCmd(false);
               }

               this.getLDSubmitV().clearHandleException();
               lineDisplayCmd.accept(this.getLDSubmitV());
               if (null != this.getLDSubmitV().getHandleException()) {
                  throw this.getLDSubmitV().getHandleException();
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

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidLineDisplayHandleImp(this);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitLineDisplay(this);
   }

   public DevCat getDevCat() {
      return DevCats.LINEDISPLAY_DEVCAT;
   }

   protected void reportEventOccurred(ReportEvent rE) {
      if (this.isTracerOn()) {
         this.traceNormal("-->reportEventOccurred() : Event->" + Util.toFormatedHexString(rE.getData()));
      }

      if (this.isDevInfoResult(rE.getData()) && this.deviceId == -1) {
         this.deviceId = this.getDeviceId(rE.getData());
         this.deviceType = this.getDeviceType(rE.getData());
         synchronized (this.lockObj) {
            this.lockObj.notifyAll();
         }

         if (this.isTracerOn()) {
            this.traceNormal("reportEventOccurred(): deviceId to use ->" + this.deviceId + ", deviceType to use ->" + this.deviceType);
         }
      }

      if (this.isTracerOn()) {
         this.traceNormal("<--reportEventOccurred() ");
      }
   }

   protected boolean isDevInfoResult(byte[] response) {
      return response.length >= 1 ? PosjUtil.isBitSelected(response[this.getStatusByte()], 4) : false;
   }

   protected int getDeviceId(byte[] response) {
      byte devId = response[this.getDevIDPosition()];
      switch (devId) {
         case 1:
            return 3015;
         case 2:
            return 3016;
         case 3:
         case 5:
         default:
            return 3004;
         case 4:
            return 3017;
         case 6:
            return 3012;
         case 7:
            return ((HidHandleKey)this.getHandleKey()).getUsageID() == 9216 ? 3013 : 3014;
      }
   }

   protected int getDeviceType(byte[] response) {
      byte devId = response[5];
      switch (devId) {
         case 0:
            return 3001;
         case 1:
            return 3002;
         case 2:
            return 3003;
         default:
            return 2001;
      }
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) throws HandleException {
      HidLineDisplayHandleImp.LineDisplaySubmitV var10000 = this.getLDSubmitV();
      this.getLDSubmitV();
      var10000.submit(HidLineDisplayHandleImp.LineDisplaySubmitV.DEVICE_INFO_REQUEST, "DEVICE_INFO_REQUEST_CMD");
      if (this.deviceId == -1) {
         synchronized (this.lockObj) {
            try {
               this.lockObj.wait(300L);
            } catch (InterruptedException var8) {
            }
         }

         if (this.deviceId == -1) {
            var10000 = this.getLDSubmitV();
            this.getLDSubmitV();
            var10000.submit(HidLineDisplayHandleImp.LineDisplaySubmitV.DEVICE_INFO_REQUEST, "DEVICE_INFO_REQUEST_CMD");
            synchronized (this.lockObj) {
               try {
                  this.lockObj.wait(300L);
               } catch (InterruptedException var6) {
               }
            }
         }
      }

      if (this.deviceId == -1) {
         devInfoCmd.setDeviceId(3004);
      } else {
         devInfoCmd.setDeviceId(this.deviceId);
         devInfoCmd.setDeviceType(this.deviceType);
      }

      devInfoCmd.setFirmwareLevel(this.getBCDLevel());
      devInfoCmd.setSerialNumber(this.getSerialNumber());
   }

   protected void restoreGlyphs() throws HandleException {
      for (int i = 0; i < this.getLDSubmitV().writeRamDisplayBKP.length; i++) {
         byte var10000 = this.getLDSubmitV().writeRamDisplayBKP[i][1];
         this.getLDSubmitV();
         if (var10000 == 64) {
            this.getLDSubmitV().submit(this.getLDSubmitV().writeRamDisplayBKP[i], "could not submit writeRamCmd");
         }
      }
   }

   protected void reinitialize() throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("reintilize() has been called");
      }

      this.getLDSubmitV().submit(this.getLDSubmitV().characterSetCmd, "could not submit characterSetCmd");
      this.restoreGlyphs();
      byte[] var10000 = this.getLDSubmitV().shortCmd;
      this.getLDSubmitV();
      var10000[0] = 1;
      this.getLDSubmitV().shortCmd[1] = 0;
      this.getLDSubmitV().submit(this.getLDSubmitV().shortCmd, "could not submit clearCmd");
      var10000 = this.getLDSubmitV().shortCmd;
      this.getLDSubmitV();
      var10000[0] = 2;
      this.getLDSubmitV().shortCmd[1] = 0;
      this.getLDSubmitV().submit(this.getLDSubmitV().shortCmd, "could not submit clearCmd");
      if (!this.getLDSubmitV().getIsClearLastCmd()) {
         this.getLDSubmitV().submit(this.getLDSubmitV().writeTopLineCmd, "could not submit writeTopCmd");
         this.getLDSubmitV().submit(this.getLDSubmitV().writeBottomLineCmd, "could not submit writeBottomCmd");
      }
   }

   protected HidLineDisplayHandleImp.LineDisplaySubmitV getLDSubmitV() throws HandleException {
      return this.ldSubmitV;
   }

   protected void setDevIDPosition(int devID_position) {
      this.devIDPosition = devID_position;
   }

   protected int getDevIDPosition() {
      return this.devIDPosition;
   }

   protected void setStatusByte(int status_Byte) {
      this.statusByte = status_Byte;
   }

   protected int getStatusByte() {
      return this.statusByte;
   }

   protected class HandleCmdFilterV extends DefaultHandleCmdV {
      private boolean isLineDisplayCmd = false;
      private boolean isSystemCmd = false;
      private boolean isInvalidCmd = false;

      public boolean isSystemCmd() {
         return this.isSystemCmd;
      }

      public boolean isLineDisplayCmd() {
         return this.isLineDisplayCmd;
      }

      public boolean isInvalidCmd() {
         return this.isInvalidCmd;
      }

      public void reset() {
         this.isInvalidCmd = false;
         this.isSystemCmd = false;
         this.isLineDisplayCmd = false;
      }

      protected void visitHandleCmd(HandleCmd cmd) {
         this.isInvalidCmd = true;
      }

      public void visitSystemCmd(SystemCmd cmd) {
         this.isSystemCmd = true;
      }

      public void visitLineDisplayCmd(LineDisplayCmd cmd) {
         this.isLineDisplayCmd = true;
      }
   }

   protected class LineDisplaySubmitV extends GenLineDisplayCmdV {
      protected void submit(byte[] data, String errorMsg) {
         try {
            HidLineDisplayHandleImp.this.getHidDevice().setReport((byte)2, (byte)0, (short)data.length, data);
         } catch (HidException var4) {
            this.handleException = new HandleException(errorMsg, var4);
         }
      }
   }
}
