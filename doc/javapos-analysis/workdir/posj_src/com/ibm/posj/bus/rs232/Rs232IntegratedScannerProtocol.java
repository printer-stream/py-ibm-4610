package com.ibm.posj.bus.rs232;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.HandleException;
import com.ibm.posj.util.DevCats;
import java.io.IOException;
import java.io.InputStream;

public abstract class Rs232IntegratedScannerProtocol {
   public static final int OK = 1;
   public static final int WAIT = 2;
   public static final int ERROR = 3;
   private static final int MIN_INTERVAL = 5;
   private static final String RECOVERY_NOT_ENABLED_STRING = "Recovery not enabled.";
   private static final String PROTOCOL_ALREADY_RECOVERING_STRING = "Protocol already recovering.";
   private static final String PROTOCOL_NOT_RECOVERING_STRING = "Protocol not recovering.";
   private static final String POLLER_THREAD_NAME = "DevicePollerThread";
   protected Tracer tracer = TracerFactory.getInstance().createTracer(DevCats.SCANNER_DEVCAT.toString(), this.getClass().getName());
   protected Rs232IntegratedScannerProtocol.DevicePoller devicePoller = null;
   protected Rs232IntegratedScannerProtocolListener protocolListener = null;
   private boolean isRecovering = false;

   Rs232IntegratedScannerProtocol(Rs232IntegratedScannerProtocolListener protocolListener, int pollIntervalInMinutes) {
      Thread pollerThread = null;
      this.protocolListener = protocolListener;
      if (pollIntervalInMinutes > 0) {
         if (pollIntervalInMinutes < 5) {
            pollIntervalInMinutes = 5;
         }

         this.devicePoller = new Rs232IntegratedScannerProtocol.DevicePoller(pollIntervalInMinutes);
         pollerThread = new Thread(this.devicePoller);
         pollerThread.setName("DevicePollerThread");
         pollerThread.start();
         this.devicePoller.startPolling();
      }
   }

   public void write(byte[] buffer) throws HandleException {
      if (this.tracer.isOn()) {
         this.tracer.println("-->write");
      }

      while (true) {
         switch (this.getCurrentState().write(buffer)) {
            case 1:
               if (this.tracer.isOn()) {
                  this.tracer.println("write ok");
                  this.tracer.println("<--write");
               }

               return;
            case 2:
               if (this.tracer.isOn()) {
                  this.tracer.println("waiting...");
               }

               try {
                  Thread.sleep(50L);
               } catch (Exception var3) {
               }
               break;
            case 3:
               if (this.tracer.isOn()) {
                  this.tracer.println("write exception");
                  this.tracer.println("<--write");
               }

               throw new HandleException("Communication Error with device");
         }
      }
   }

   public void flushInputStream(InputStream is) {
      if (this.tracer.isOn()) {
         this.tracer.println(3, "Flushing input stream...");
      }

      try {
         while (is.available() > 0) {
            is.read();
         }
      } catch (IOException var3) {
         if (this.tracer.isOn()) {
            this.tracer.print(var3);
         }
      }
   }

   public void submitToAdapter(byte[] data) {
      if (this.tracer.isOn()) {
         this.tracer.println("-->submitToAdapter");
      }

      this.protocolListener.writeToPort(data);
      if (this.tracer.isOn()) {
         this.tracer.println("<--submitToAdapter");
      }
   }

   public void fireDataEvent(byte[] data) {
      this.protocolListener.fireDataEvent(data);
   }

   public void fireErrorEvent(int type) {
      this.protocolListener.fireErrorEvent(type);
   }

   public void fireDirectIOEvent(byte[] data) {
      this.protocolListener.fireDirectIOEvent(data);
   }

   public void fireOnlineEvent() {
      this.protocolListener.fireOnlineEvent();
   }

   public void fireOfflineEvent() {
      this.protocolListener.fireOfflineEvent();
   }

   public void startRecovering() {
      if (this.tracer.isOn()) {
         this.tracer.println("-->startRecovering()");
      }

      if (!this.isRecoveringEnabled()) {
         if (this.tracer.isOn()) {
            this.tracer.println("Recovery not enabled.");
         }
      } else if (this.isRecovering()) {
         if (this.tracer.isOn()) {
            this.tracer.println("Protocol already recovering.");
         }
      } else {
         this.isRecovering = true;
         this.fireErrorEvent(-100);
         this.fireOfflineEvent();
         this.devicePoller.stopPolling();
         this.restart();
         this.devicePoller.startPolling();
         if (this.tracer.isOn()) {
            this.tracer.println("<--startRecovering()");
         }
      }
   }

   public void stopRecovering() {
      if (this.tracer.isOn()) {
         this.tracer.println("-->stopRecovering()");
      }

      if (!this.isRecoveringEnabled()) {
         if (this.tracer.isOn()) {
            this.tracer.println("Recovery not enabled.");
         }
      } else if (!this.isRecovering()) {
         if (this.tracer.isOn()) {
            this.tracer.println("Protocol not recovering.");
         }
      } else {
         this.isRecovering = false;
         this.fireOnlineEvent();
         if (this.tracer.isOn()) {
            this.tracer.println("<--stopRecovering()");
         }
      }
   }

   public boolean isRecovering() {
      return this.isRecovering;
   }

   public boolean isRecoveringEnabled() {
      return this.devicePoller != null;
   }

   protected void pollDevice() throws HandleException {
   }

   protected void processPollDeviceException() {
      this.restart();
   }

   protected void start() {
   }

   protected void stop() {
   }

   protected void restart() {
      this.stop();
      this.start();
   }

   protected abstract Rs232IntegratedScannerState getCurrentState();

   private class DevicePoller implements Runnable {
      private long intervalInMillis = 0L;
      private boolean isPolling = false;

      public DevicePoller(int intervalInSeconds) {
         this.intervalInMillis = (long)(intervalInSeconds * 1000);
      }

      public void startPolling() {
         this.isPolling = true;
      }

      public void stopPolling() {
         this.isPolling = false;
      }

      public boolean isPolling() {
         return this.isPolling;
      }

      public void run() {
         while (true) {
            if (this.isPolling) {
               synchronized (this) {
                  try {
                     this.wait(this.intervalInMillis);
                  } catch (InterruptedException var5) {
                  }
               }

               try {
                  Rs232IntegratedScannerProtocol.this.pollDevice();
               } catch (HandleException var4) {
                  Rs232IntegratedScannerProtocol.this.processPollDeviceException();
               }
            }
         }
      }
   }
}
