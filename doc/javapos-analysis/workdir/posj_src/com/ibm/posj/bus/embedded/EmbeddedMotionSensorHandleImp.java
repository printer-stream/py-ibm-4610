package com.ibm.posj.bus.embedded;

import com.ibm.embedded.EmbeddedException;
import com.ibm.embedded.MotionSensorEmbeddedDriver;
import com.ibm.embedded.event.EmbeddedEvent;
import com.ibm.embedded.event.EmbeddedListener;
import com.ibm.jutil.Util;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.MotionSensorHandleImp;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

class EmbeddedMotionSensorHandleImp extends AbstractEmbeddedHandleImp implements MotionSensorHandleImp {
   private EmbeddedListener listener = null;
   private MotionSensorEmbeddedDriver embeddedDriver = null;
   protected int msStatusCode = 0;
   public static final String ERROR_GETTING_STATUS_MSG = "An error occurred while trying to get status from the device";
   public static byte MS_PRESENCE = 1;
   public static byte MS_ABSENCE = 0;

   public EmbeddedMotionSensorHandleImp(HandleKey key, MotionSensorEmbeddedDriver driver) {
      super(key);
      this.embeddedDriver = driver;
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitMotionSensor(this);
   }

   public DevCat getDevCat() {
      return DevCats.MOTIONSENSOR_DEVCAT;
   }

   public void init() throws HandleException {
      this.listener = new EmbeddedMotionSensorHandleImp.MSEmbeddedListener();

      try {
         this.getEmbeddedDriver().addEmbeddedEventListener(this.listener);
      } catch (EmbeddedException var2) {
         if (this.isTracerOn()) {
            this.getTracer().print(var2);
         }

         this.getLogHelper().addLogEntry(1003, "Could not add listener", "MotionSensor", 3);
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
                  throw new HandleException("Invalid Motion Sensor object submitted!");
               }

               ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceId(3202);
            }
         } catch (HandleException var6) {
            this.setHandleCmdResultInError(cmd, true);
            throw var6;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   protected void submitStatusReqCmd() throws HandleException {
      byte statusByte = 0;
      statusByte = this.getStatus();
      this.msStatusCode = this.getStatusCode(statusByte);
      this.getHandle().getEventHelper().fireStatusEvent(new StatusEvent(this, this.msStatusCode));
   }

   protected byte getStatus() throws HandleException {
      byte status = 0;

      try {
         status = this.getEmbeddedDriver().getStatus();
      } catch (EmbeddedException var3) {
         if (this.isTracerOn()) {
            this.getTracer().print(var3);
            this.traceNormal("An error occurred while trying to get status from the device");
         }

         throw new HandleException("An error occurred while trying to get status from the device");
      }

      if (this.isTracerOn()) {
         this.traceNormal("-->getStatus()... status  = " + Util.toHexString(status) + "<--");
      }

      return status;
   }

   protected MotionSensorEmbeddedDriver getEmbeddedDriver() {
      return this.embeddedDriver;
   }

   protected int getStatusCode(byte status) throws HandleException {
      if (status == MS_PRESENCE) {
         return 1;
      } else if (status == MS_ABSENCE) {
         return 2;
      } else {
         throw new HandleException("An error occurred while trying to get status from the device");
      }
   }

   protected class MSEmbeddedListener implements EmbeddedListener {
      public void statusEventOccurred(EmbeddedEvent event) {
         byte statusByte = event.getStatus();
         if (EmbeddedMotionSensorHandleImp.this.isTracerOn()) {
            EmbeddedMotionSensorHandleImp.this.traceNormal("->statusEventOccurred()" + Util.toHexString(statusByte));
         }

         try {
            EmbeddedMotionSensorHandleImp.this.msStatusCode = EmbeddedMotionSensorHandleImp.this.getStatusCode(statusByte);
         } catch (HandleException var4) {
            EmbeddedMotionSensorHandleImp.this.getLogHelper().addLogEntry(12001, "Wrong status received", "MotionSensor", 4);
            if (EmbeddedMotionSensorHandleImp.this.isTracerOn()) {
               EmbeddedMotionSensorHandleImp.this.getTracer().print(var4);
               EmbeddedMotionSensorHandleImp.this.traceNormal("Wrong status received from EmbeddedDriver");
            }
         }

         EmbeddedMotionSensorHandleImp.this.getHandle()
            .getEventHelper()
            .fireStatusEvent(new StatusEvent(this, EmbeddedMotionSensorHandleImp.this.msStatusCode));
         if (EmbeddedMotionSensorHandleImp.this.isTracerOn()) {
            EmbeddedMotionSensorHandleImp.this.traceNormal("<-statusEventOccurred() - statusCode = " + EmbeddedMotionSensorHandleImp.this.msStatusCode);
         }
      }
   }
}
