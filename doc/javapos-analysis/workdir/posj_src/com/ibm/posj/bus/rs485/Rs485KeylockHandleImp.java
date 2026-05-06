package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jutil.Util;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.KeylockHandleImp;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.KeylockUtil;
import com.ibm.posj.util.PosjUtil;

public class Rs485KeylockHandleImp extends AbstractRs485HandleImp implements KeylockHandleImp {
   public static final int STATUS_RESP_LENGTH = 4;
   public static final int CMD_REJECT_RES_BYTE = 1;
   private int deviceId = -1;
   private Object lockObj = new Object();
   public static final byte[] KL_STATUS_CMD = new byte[]{0, 32};
   public static final byte[] KL_DEV_INFO_CMD = new byte[]{0, 0, 1};
   public static final int DEV_INFO_RESPONSE_BIT = 2;
   public static final int DEV_INFO_RESPONSE_BYTE = 1;
   public static final int STATUS_RESPONSE_LENGTH = 4;

   public Rs485KeylockHandleImp(HandleKey key, SioDevice sioDevice) {
      super(key, sioDevice);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitKeylock(this);
   }

   public DevCat getDevCat() {
      return DevCats.KEYLOCK_DEVCAT;
   }

   public void init() throws HandleException {
      super.init();
      if (this.isTracerOn()) {
         this.traceNormal(" Init");
      }

      this.submit(this.getHandle().getSystemCmdFactory().createDeviceInfoRequestCmd());
      this.submitSync(KL_STATUS_CMD);
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            if (cmd.getCode() == 101) {
               this.submitSync(KL_STATUS_CMD);
            } else {
               if (cmd.getCode() != 103) {
                  throw new HandleException("Invalid KeylockCmd object submitted! " + cmd.getName());
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

   protected void dataEventOccurred(SioDeviceDataEvent event) {
      byte[] data = event.getData();
      if (this.isTracerOn()) {
         this.traceNormal("-->dataEventOcurred()");
         this.traceNormal("Event->" + Util.toFormatedHexString(data));
      }

      if (this.isDevInfoResult(data) && this.deviceId == -1) {
         byte[] dataArray = new byte[data.length - 4];
         System.arraycopy(data, 4, dataArray, 0, dataArray.length);
         this.deviceId = this.getDeviceId(dataArray);
         synchronized (this.lockObj) {
            this.lockObj.notifyAll();
         }

         if (this.isTracerOn()) {
            this.traceNormal("  DeviceId to use -> " + this.deviceId);
         }
      }

      if (data.length == 4) {
         this.parseAndFireDataEvent(data);
      }
   }

   protected void parseAndFireDataEvent(byte[] data) {
      int handlePosition = -1;
      int status2 = data[2] & 112;
      if (this.isTracerOn()) {
         this.traceNormal(" in parseAndFireDataEvent data[2] is " + Util.toHexString(status2));
      }

      if (KeylockUtil.isDBCS(this.deviceId)) {
         if (this.isTracerOn()) {
            this.traceNormal("  DoubleByte Keylock");
         }

         switch (status2) {
            case 0:
               handlePosition = 4;
               break;
            case 16:
               handlePosition = 1;
               break;
            case 32:
               handlePosition = 2;
               break;
            case 48:
               handlePosition = 3;
               break;
            case 80:
               handlePosition = 5;
               break;
            case 96:
               handlePosition = 6;
         }
      } else {
         if (this.isTracerOn()) {
            this.traceNormal("  SingleByte Keylock");
         }

         switch (status2) {
            case 0:
               handlePosition = 2;
               break;
            case 16:
               handlePosition = 3;
         }
      }

      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      eventHelper.fireStatusEvent(new StatusEvent(this, handlePosition));
      if (this.isTracerOn()) {
         this.traceNormal(" ----> fireStatusEvent :" + handlePosition);
      }
   }

   protected boolean isDevInfoResult(byte[] response) {
      return response.length >= 6 ? PosjUtil.isBitSelected(response[1], 2) : false;
   }

   protected int getDeviceId(byte[] response) {
      return KeylockUtil.getRs485KeylockID(response);
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) throws HandleException {
      if (this.deviceId == -1) {
         this.submitSync(KL_DEV_INFO_CMD);
         synchronized (this.lockObj) {
            try {
               this.lockObj.wait(500L);
            } catch (InterruptedException var5) {
            }
         }
      }

      if (this.deviceId == -1) {
         devInfoCmd.setDeviceId(2910);
      } else {
         devInfoCmd.setDeviceId(this.deviceId);
      }
   }

   public boolean isComposite() {
      return true;
   }
}
