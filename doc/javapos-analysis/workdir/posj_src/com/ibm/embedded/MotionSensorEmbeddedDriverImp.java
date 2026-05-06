package com.ibm.embedded;

import com.ibm.embedded.event.EmbeddedEvent;
import com.ibm.embedded.event.EmbeddedListener;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.util.DevBuses;
import java.util.ArrayList;
import java.util.Iterator;

public class MotionSensorEmbeddedDriverImp implements MotionSensorEmbeddedDriver {
   private ArrayList listenerList = new ArrayList();
   private Thread listenerThread = new MotionSensorEmbeddedDriverImp.ListenerThread();
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevBuses.EMBEDDED_DEVBUS.getName(), "MotionSensorEmbeddedDriverImp");
   public static final int MOTION_SENSOR_POLLING_TIME = 300;
   public static final String MOTION_SENSOR_INVALID_STATUS = "Invalid status value received from the kernel mode driver";
   public static final String MOTION_SENSOR_EMBEDDED_EXCEPTION = "Embedded exception received from the kernel mode driver";
   public static final String MOTION_SENSOR_UNABLE_TO_ADD_LISTENER = "The listener can not be loaded";

   public void accept(EmbeddedDriverVisitor visitor) {
      visitor.visitMotionSensor(this);
   }

   public byte getStatus() throws EmbeddedException {
      return (byte)(nativeMSGetStatus() == 0L ? 1 : 0);
   }

   public synchronized void addEmbeddedEventListener(EmbeddedListener listener) throws EmbeddedException {
      this.listenerList.add(listener);
      if (!this.listenerList.isEmpty()) {
         this.listenerThread.start();
      }
   }

   public synchronized void removeEmbeddedEventListener(EmbeddedListener listener) {
      this.listenerList.remove(listener);
   }

   private static native long nativeMSGetStatus() throws EmbeddedException;

   static {
      System.loadLibrary("aipposembedded");
   }

   class ListenerThread extends Thread {
      public void run() {
         long nativeStatus = 2L;
         long prevStatus = 2L;
         byte status = (byte)2;

         while (!MotionSensorEmbeddedDriverImp.this.listenerList.isEmpty()) {
            try {
               nativeStatus = MotionSensorEmbeddedDriverImp.nativeMSGetStatus();
               if (nativeStatus == 2L) {
                  MotionSensorEmbeddedDriverImp.this.tracer.println("Invalid status value received from the kernel mode driver");
               } else if (nativeStatus != prevStatus) {
                  status = (byte)(nativeStatus == 0L ? 1 : 0);
                  this.fireEmbeddedEvent(new EmbeddedEvent(this, status));
                  prevStatus = nativeStatus;
               }

               this.sleepTime();
            } catch (EmbeddedException var7) {
               MotionSensorEmbeddedDriverImp.this.tracer.println("Embedded exception received from the kernel mode driver");
            }
         }
      }

      private synchronized void fireEmbeddedEvent(EmbeddedEvent embeddedEvent) {
         Iterator listenerIterator = MotionSensorEmbeddedDriverImp.this.listenerList.iterator();
         EmbeddedListener listener = null;

         while (listenerIterator.hasNext()) {
            listener = (EmbeddedListener)listenerIterator.next();
            listener.statusEventOccurred(embeddedEvent);
         }
      }

      private void sleepTime() {
         try {
            Thread.sleep(300L);
         } catch (InterruptedException var2) {
         }
      }
   }
}
