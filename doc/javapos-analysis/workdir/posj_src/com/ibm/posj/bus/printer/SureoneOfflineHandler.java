package com.ibm.posj.bus.printer;

import com.ibm.jutil.BooleanMonitor;

public class SureoneOfflineHandler implements Runnable {
   private static int EXTRA_TIMEOUT = 2000;
   private static int PRIVATE_TIMEOUT = 4000;
   private boolean offline = false;
   private Thread myThread = null;
   private SureoneOfflineHandler.OfflineCallback callback = null;
   private byte[] statusCmd = null;
   private int estTimeout = 2000;
   private boolean dataSent = false;
   private BooleanMonitor idleMonitor = new BooleanMonitor(true);
   private BooleanMonitor timeOutMonitor = new BooleanMonitor(false);

   public SureoneOfflineHandler(SureoneOfflineHandler.OfflineCallback c) {
      this.callback = c;
      this.myThread = new Thread(this);
      this.myThread.start();
   }

   public void pulseSend() {
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
      } catch (Exception var3) {
      }

      while (true) {
         try {
            this.idleMonitor.waitForFalse(PRIVATE_TIMEOUT);
         } catch (Exception var2) {
            this.callback.statusCheck();
         }

         if (!this.dataSent) {
            this.idleMonitor.set(true);
         } else {
            try {
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
