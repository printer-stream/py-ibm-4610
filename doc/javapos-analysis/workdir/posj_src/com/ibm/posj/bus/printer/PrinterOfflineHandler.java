package com.ibm.posj.bus.printer;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;

public class PrinterOfflineHandler implements Runnable {
   private static int EXTRA_TIMEOUT = 11000;
   private static int PRIVATE_TIMEOUT = 50000;
   private static Tracer tracer = TracerFactory.getInstance().createTracer("PrtOfflineHdler");
   private boolean offline = false;
   private Thread myThread = null;
   private PrinterOfflineHandler.OfflineCallback callback = null;
   private byte[] statusCmd = null;
   private int estTimeout = 5000;
   private boolean dataSent = false;
   private BooleanMonitor idleMonitor = new BooleanMonitor(true);
   private BooleanMonitor timeOutMonitor = new BooleanMonitor(false);

   public PrinterOfflineHandler(PrinterOfflineHandler.OfflineCallback c) {
      this.callback = c;
      this.myThread = new Thread(this);
      this.myThread.start();
   }

   public void pulseSend() {
      if (tracer.isOn()) {
         tracer.println("pulseSend");
      }

      this.dataSent = true;
      this.idleMonitor.set(false);
      this.timeOutMonitor.set(false);
   }

   public void receiveThread() {
      if (this.offline) {
         this.callback.setOnline();
      }

      this.timeOutMonitor.set(true);
      this.idleMonitor.set(false);
      this.dataSent = false;
   }

   public void setEstimatedTimeout(int est) {
      this.estTimeout = est;
   }

   public int getEstimatedTimeout() {
      return this.estTimeout;
   }

   public void run() {
      Thread.currentThread().setPriority(1);

      try {
         this.idleMonitor.waitForFalse(-1);
      } catch (Exception var2) {
      }

      while (true) {
         try {
            if (tracer.isOn()) {
               tracer.println("IdleMonitor sleep");
            }

            this.idleMonitor.waitForFalse(PRIVATE_TIMEOUT);
         } catch (Exception var3) {
            if (tracer.isOn()) {
               tracer.println("Check Status");
            }

            this.callback.statusCheck();
         }

         if (!this.dataSent) {
            if (tracer.isOn()) {
               tracer.println("IdleMonitor set true loop");
            }

            this.idleMonitor.set(true);
         } else {
            try {
               if (tracer.isOn()) {
                  tracer.println("TimeoutMonitor sleep");
               }

               this.timeOutMonitor.waitForTrue(this.getEstimatedTimeout() + EXTRA_TIMEOUT);
               this.offline = false;
            } catch (Exception var4) {
               if (!this.offline) {
                  this.offline = true;
                  this.callback.setOffline();
               }
            }
         }
      }
   }

   public interface OfflineCallback {
      void statusCheck();

      void setOffline();

      void setOnline();
   }
}
