package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.Util;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.KeylockHandleImp;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.KeylockUtil;
import com.ibm.posj.util.PosjUtil;

public class HidKeylockHandleImp extends AbstractHidHandleImp implements HidHandleImp, KeylockHandleImp {
   private HidKeylockStrategy strategy = null;
   private int deviceId = -1;
   private Object lockObj = new Object();
   public static final int DELAY = 500;
   public static final int ERROR_BIT = 7;
   public static final int DEV_INFO_BIT = 2;
   public static final int POSITION_BYTE1 = 4;
   public static final int POSITION_BYTE2 = 5;
   public static final byte[] KL_STATUS_REQUEST_CMD = new byte[]{0, 32};
   public static final byte[] KL_DEVINFO_REQUEST_CMD = new byte[]{0, 0, 1};

   public HidKeylockHandleImp(HandleKey key, HidDevice device, HidKeylockStrategy strategy) {
      super(key, device);
      this.strategy = strategy;
   }

   public void init() throws HandleException {
      super.init();
      if (this.isTracerOn()) {
         this.traceNormal("Using strategy: " + this.getStrategy());
      }
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidKeylockHandleImp(this);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitKeylock(this);
   }

   public DevCat getDevCat() {
      return DevCats.KEYLOCK_DEVCAT;
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (this.isLocked.isTrue()) {
         throw new HandleException("Attempting to submit to Keylock while flashing");
      } else if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            if (cmd.getCode() == 101) {
               this.submitCmd(cmd.getName(), KL_STATUS_REQUEST_CMD);
            } else {
               if (cmd.getCode() != 103) {
                  throw new HandleException("Invalid SystemCmd object submitted!");
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

   public boolean isComposite() {
      return true;
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) throws HandleException {
      if (this.deviceId == -1) {
         synchronized (this.lockObj) {
            this.submitCmd(devInfoCmd.getName(), KL_DEVINFO_REQUEST_CMD);

            try {
               this.lockObj.wait(500L);
            } catch (InterruptedException var8) {
            }
         }

         if (this.deviceId == -1) {
            synchronized (this.lockObj) {
               this.submitCmd(devInfoCmd.getName(), KL_DEVINFO_REQUEST_CMD);

               try {
                  this.lockObj.wait(500L);
               } catch (InterruptedException var6) {
               }
            }
         }
      }

      if (this.deviceId == -1) {
         devInfoCmd.setDeviceId(2910);
      } else {
         devInfoCmd.setDeviceId(this.deviceId);
      }

      devInfoCmd.setFirmwareLevel(this.getBCDLevel());
      devInfoCmd.setSerialNumber(this.getSerialNumber());
   }

   protected void parseAndFireDataEvent(byte[] report) {
      int handlePosition = -1;
      boolean bit4 = false;
      boolean bit5 = false;
      Handle.EventHelper eventHelper = null;
      if (this.getHandle() != null) {
         eventHelper = this.getHandle().getEventHelper();

         try {
            this.checkForCmdRejectError(report);
         } catch (HandleException var7) {
            eventHelper.fireErrorEvent(new ErrorEvent(this, -101, "Keylock Command Rejected"));
         }

         bit4 = PosjUtil.isBitSelected(report[this.getStrategy().getKeylockStatusByteIndex()], 4);
         bit5 = PosjUtil.isBitSelected(report[this.getStrategy().getKeylockStatusByteIndex()], 5);
         if (report.length == 16
            && report[0] == 0
            && report[1] == 0
            && report[2] == 0
            && report[3] == 0
            && report[4] == 0
            && report[5] == 0
            && report[6] == 0
            && report[7] == 0
            && report[8] == 0
            && report[9] == 0
            && report[10] == 0
            && report[11] == 0
            && report[12] == 0
            && report[13] == 0
            && report[14] == 0
            && report[15] == 0) {
            if (this.isTracerOn()) {
               this.traceNormal(" This event is likely a bogus event from disconnection, and will be ignored");
            }
         } else {
            byte var8;
            if (KeylockUtil.isDBCS(this.deviceId)) {
               if (bit5) {
                  if (bit4) {
                     var8 = 3;
                  } else {
                     var8 = 2;
                  }
               } else if (bit4) {
                  var8 = 1;
               } else {
                  var8 = 4;
               }
            } else if (bit4) {
               var8 = 3;
            } else {
               var8 = 2;
            }

            eventHelper.fireStatusEvent(new StatusEvent(this, var8));
            if (this.isTracerOn()) {
               this.traceNormal(" ----> fireStatusEvent :" + var8);
            }
         }
      }
   }

   protected void checkForCmdRejectError(byte[] response) throws HandleException {
      byte Rejected = response[this.getStrategy().getErrorStatusByteIndex()];
      if (PosjUtil.isBitSelected(Rejected, 7)) {
         throw new HandleException("Command Rejected by Keylock");
      }
   }

   protected void reportEventOccurred(ReportEvent rE) {
      try {
         if (this.getHandle() == null || !this.getHandle().isInit()) {
            return;
         }
      } catch (HandleException var5) {
      }

      if (this.isTracerOn()) {
         this.traceNormal("->reportEventOccurred : " + Util.toFormatedHexString(rE.getData()));
      }

      if (this.isDevInfoResult(rE.getData()) && this.deviceId == -1) {
         this.deviceId = this.getStrategy().getDeviceId(rE.getData());
         synchronized (this.lockObj) {
            this.lockObj.notifyAll();
         }

         if (this.isTracerOn()) {
            this.traceNormal("reportEventOccurred(): deviceId to use ->" + this.deviceId + ", SBCS=" + !KeylockUtil.isDBCS(this.deviceId));
         }
      }

      this.parseAndFireDataEvent(rE.getData());
      if (this.isTracerOn()) {
         this.traceNormal("<- reportEventOccurred() ");
      }
   }

   protected boolean isDevInfoResult(byte[] response) {
      return response.length >= 6 ? PosjUtil.isBitSelected(response[this.getStrategy().getDevInfoPosition()], 2) : false;
   }

   protected HidKeylockStrategy getStrategy() {
      return this.strategy;
   }

   protected void reinitialize() throws HandleException {
      this.submit(this.getHandle().getSystemCmdFactory().createStatusRequestCmd());
   }
}
