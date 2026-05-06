package com.ibm.posj.bus.embedded;

import com.ibm.embedded.EmbeddedException;
import com.ibm.embedded.KeylockEmbeddedDriver;
import com.ibm.embedded.event.EmbeddedEvent;
import com.ibm.embedded.event.EmbeddedListener;
import com.ibm.jutil.Timer;
import com.ibm.jutil.Timerable;
import com.ibm.jutil.Util;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.KeylockHandleImp;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class EmbeddedKeylockHandleImp extends AbstractEmbeddedHandleImp implements KeylockHandleImp {
   private EmbeddedListener listener = null;
   private KeylockEmbeddedDriver embeddedDriver = null;
   protected int keylockStatusCode = 0;
   protected int lastKeylockStatusCode = 0;
   private volatile boolean threadFlag = false;
   private EmbeddedKeylockHandleImp.KeylockTimerable kTimerable = new EmbeddedKeylockHandleImp.KeylockTimerable();
   private Timer kTimer = new Timer(this.kTimerable);
   public static final String ERROR_GETTING_STATUS_MSG = "An error occurred while trying to get status from the device";
   public final int WAIT_FOR_STATUS = 300;

   public EmbeddedKeylockHandleImp(HandleKey key, KeylockEmbeddedDriver driver) {
      super(key);
      this.embeddedDriver = driver;
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitKeylock(this);
   }

   public DevCat getDevCat() {
      return DevCats.KEYLOCK_DEVCAT;
   }

   public void init() throws HandleException {
      this.listener = new EmbeddedKeylockHandleImp.KeylockEmbeddedListener();

      try {
         this.getEmbeddedDriver().addEmbeddedEventListener(this.listener);
      } catch (EmbeddedException var2) {
         if (this.isTracerOn()) {
            this.getTracer().print(var2);
         }

         this.getLogHelper().addLogEntry(1003, "Could not add listener", "Keylock", 3);
         throw new HandleException("Could not add listener");
      }

      this.submitStatusReqCmd();
   }

   public boolean isFlashable() {
      return false;
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            if (cmd.getCode() == 101) {
               this.submitStatusReqCmd();
            } else {
               if (cmd.getCode() != 103) {
                  throw new HandleException("Invalid Keylock object submitted! : " + cmd.getName());
               }

               ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceId(2911);
            }
         } catch (HandleException var6) {
            this.setHandleCmdResultInError(cmd, true);
            throw var6;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   public void statusEventOccurred(EmbeddedEvent event) {
      byte statusByte = event.getStatus();
      this.setThreadFlag(false);
      if (this.isTracerOn()) {
         this.traceNormal("-->statusEventOccurred(): statusByte= " + Util.toHexString(statusByte));
      }

      this.lastKeylockStatusCode = this.keylockStatusCode;
      this.keylockStatusCode = this.getStatusCode(statusByte);
      if (this.validateLockPosition()) {
         this.fireStatusEvent(this.keylockStatusCode);
      }
   }

   public boolean getThreadFlag() {
      return this.threadFlag;
   }

   public void setThreadFlag(boolean threadFlag) {
      this.threadFlag = threadFlag;
   }

   protected void submitStatusReqCmd() throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("--> submitStatusReqCmd() : getStatus() " + this.getStatus());
      }

      this.lastKeylockStatusCode = this.keylockStatusCode;
      this.keylockStatusCode = this.getStatusCode(this.getStatus());
      if (this.isTracerOn()) {
         this.traceNormal("firing status  = " + Util.toHexString(this.keylockStatusCode));
      }

      this.fireStatusEvent(this.keylockStatusCode);
      if (this.isTracerOn()) {
         this.traceNormal("<-- submitStatusReqCmd()");
      }
   }

   protected byte getStatus() throws HandleException {
      byte status = 0;

      try {
         return this.getEmbeddedDriver().getStatus();
      } catch (EmbeddedException var3) {
         if (this.isTracerOn()) {
            this.getTracer().print(var3);
            this.traceNormal("An error occurred while trying to get status from the device");
         }

         throw new HandleException("An error occurred while trying to get status from the device");
      }
   }

   protected KeylockEmbeddedDriver getEmbeddedDriver() {
      return this.embeddedDriver;
   }

   protected int getStatusCode(byte status) {
      if (status == 7) {
         return 1;
      } else {
         return status == 6 ? 2 : 3;
      }
   }

   protected void fireStatusEvent(int status) {
      if (this.isTracerOn()) {
         this.traceNormal("--> ******* fireStatusEvent:  status " + status);
      }

      this.getHandle().getEventHelper().fireStatusEvent(new StatusEvent(this, status));
   }

   protected boolean validateLockPosition() {
      boolean validEvent = true;
      if (this.keylockStatusCode == 1) {
         if (this.lastKeylockStatusCode == 2) {
            this.setThreadFlag(true);
            this.kTimer.setTime(300);
            this.kTimer.start();
            validEvent = false;
         } else if (this.lastKeylockStatusCode == 3) {
            validEvent = false;
         }
      }

      return validEvent;
   }

   protected class KeylockEmbeddedListener implements EmbeddedListener {
      public void statusEventOccurred(EmbeddedEvent event) {
         EmbeddedKeylockHandleImp.this.statusEventOccurred(event);
      }
   }

   protected class KeylockTimerable implements Timerable {
      public void timerExpired() {
         if (EmbeddedKeylockHandleImp.this.getThreadFlag()) {
            if (EmbeddedKeylockHandleImp.this.isTracerOn()) {
               EmbeddedKeylockHandleImp.this.traceNormal("-->timerExpired()");
            }

            EmbeddedKeylockHandleImp.this.fireStatusEvent(1);
            EmbeddedKeylockHandleImp.this.setThreadFlag(false);
         }
      }
   }
}
