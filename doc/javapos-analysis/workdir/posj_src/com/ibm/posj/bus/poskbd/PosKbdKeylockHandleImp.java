package com.ibm.posj.bus.poskbd;

import com.ibm.posj.AbstractHandleCmd;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.KeylockCmd;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.KeylockHandleImp;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.KeylockUtil;
import com.ibm.poskbd.StatusFunction;
import com.ibm.poskbd.event.StatusEvent;
import com.ibm.poskbd.event.StatusListener;

public class PosKbdKeylockHandleImp extends AbstractPosKbdHandleImp implements KeylockHandleImp {
   private int deviceId = -1;
   private int kbdType = 0;
   private StatusFunction statusFunction = null;
   private PosKbdKeylockHandleImp.KeylockStatusListener statusListener = new PosKbdKeylockHandleImp.KeylockStatusListener();
   public static final byte KEYLOCK_POSITION_MASK = 48;
   public static final byte KL_STATUS_REQUEST_CMD = 32;
   public static final int KL_STATUS_BYTE_INDEX = 2;
   public static final int ERR_STATUS_BYTE_INDEX = 1;
   public static final int KP_SB_OPERATOR = 0;
   public static final int KP_SB_MANAGER = 1;
   public static final int KP_DB_SYSTEM = 0;
   public static final int KP_DB_INACTIVE = 1;
   public static final int KP_DB_OPERATOR = 2;
   public static final int KP_DB_MANAGER = 3;

   public PosKbdKeylockHandleImp(HandleKey key, StatusFunction statusFunc) {
      super(key);
      this.statusFunction = statusFunc;
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitKeylock(this);
   }

   public DevCat getDevCat() {
      return DevCats.KEYLOCK_DEVCAT;
   }

   public void init() throws HandleException {
      this.statusFunction.addStatusListener(this.statusListener);
      this.submit(this.getHandle().getSystemCmdFactory().createDeviceInfoRequestCmd());
      this.submit(this.getHandle().getSystemCmdFactory().createStatusRequestCmd());
   }

   public void submit(HandleCmd cmd) throws HandleException {
      KeylockCmd keylockCmd = null;
      if (cmd.getCode() == 300) {
         this.submitGetKeyPositionCmd(keylockCmd);
      } else {
         if (!(cmd instanceof SystemCmd)) {
            throw new HandleException("Invalid KeylockCmd object submitted!");
         }

         this.submitSystemCmd((SystemCmd)cmd);
      }
   }

   public void setKbdType(int type) {
      if (this.isTracerOn()) {
         this.traceNormal("-->setKbdType() KeyboardType == " + type + "<--");
      }

      this.kbdType = type;
   }

   public int getKbdType() {
      return this.kbdType;
   }

   protected void submitGetKeyPositionCmd(KeylockCmd keylockCmd) throws HandleException {
   }

   protected void submitSystemCmd(SystemCmd systemCmd) throws HandleException {
      try {
         if (systemCmd.getCode() == 101) {
            byte[] statusBytes = this.statusFunction.getStatus();
            this.parseStatusAndFireEvent(statusBytes);
         } else {
            if (systemCmd.getCode() != 103) {
               throw new HandleException("Invalid SystemCmd.getCode() value!");
            }

            this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)systemCmd);
         }
      } catch (HandleException var3) {
         ((AbstractHandleCmd.DefaultResult)systemCmd.getResult()).setInError(true);
         throw new HandleException("Error while sending SystemCmd to PosKbd Keylock", var3);
      }
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) throws HandleException {
      if (this.deviceId == -1) {
         this.deviceId = this.getDeviceId((short)this.getKbdType());
         if (this.isTracerOn()) {
            this.traceNormal("-->deviceId == " + this.deviceId + "<--");
         }
      }

      devInfoCmd.setDeviceId(this.deviceId);
   }

   protected int getDeviceId(short response) {
      return KeylockUtil.getPs2KeylockID(response);
   }

   protected void handleStatusEvent(StatusEvent event) {
      byte[] statusBytes = event.getStatus();
      this.parseStatusAndFireEvent(statusBytes);
   }

   protected void parseStatusAndFireEvent(byte[] statusBytes) {
      try {
         this.checkForCmdRejectError(statusBytes);
      } catch (HandleException var5) {
      }

      byte statusByte = statusBytes[2];
      byte keylockPosition = (byte)((statusByte & 48) >> 4);
      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      if (KeylockUtil.isDBCS(this.deviceId)) {
         this.dbcsFireStatus(keylockPosition, eventHelper);
      } else {
         this.sbcsFireStatus(keylockPosition, eventHelper);
      }
   }

   protected void dbcsFireStatus(byte klPosition, Handle.EventHelper eventHelper) {
      if (this.isTracerOn()) {
         this.traceNormal("-->dbcsFireStatus == " + klPosition + "<--");
      }

      if (klPosition == 0) {
         eventHelper.fireStatusEvent(new com.ibm.posj.event.StatusEvent(this, 4));
      } else if (klPosition == 1) {
         eventHelper.fireStatusEvent(new com.ibm.posj.event.StatusEvent(this, 1));
      } else if (klPosition == 2) {
         eventHelper.fireStatusEvent(new com.ibm.posj.event.StatusEvent(this, 2));
      } else if (klPosition == 3) {
         eventHelper.fireStatusEvent(new com.ibm.posj.event.StatusEvent(this, 3));
      } else {
         eventHelper.fireErrorEvent(new ErrorEvent(this, -100));
      }
   }

   protected void sbcsFireStatus(byte klPosition, Handle.EventHelper eventHelper) {
      if (this.isTracerOn()) {
         this.traceNormal("-->sbcsFireStatus == " + klPosition + "<--");
      }

      if (klPosition == 0) {
         eventHelper.fireStatusEvent(new com.ibm.posj.event.StatusEvent(this, 2));
      } else if (klPosition == 1) {
         eventHelper.fireStatusEvent(new com.ibm.posj.event.StatusEvent(this, 3));
      } else {
         eventHelper.fireErrorEvent(new ErrorEvent(this, -100));
      }
   }

   protected void checkForCmdRejectError(byte[] statusBytes) throws HandleException {
      byte statusByte = statusBytes[1];
      byte cmdRejectFlag = (byte)(statusByte & 128);
      if (0 != cmdRejectFlag) {
         throw new HandleException("Command Rejected by Keylock");
      }
   }

   private class KeylockStatusListener implements StatusListener {
      private KeylockStatusListener() {
      }

      public void statusReceived(StatusEvent event) {
         PosKbdKeylockHandleImp.this.handleStatusEvent(event);
      }
   }
}
