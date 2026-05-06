package com.ibm.posj.bus.rs232;

import com.ibm.jutil.Timerable;

final class Rs232FiscalPrinterTimer implements Runnable {
   private Thread timerThread = new Thread(this);
   private Timerable timerable = null;
   private boolean started = false;
   private long time = 0L;
   private Object waitObj = new Object();
   private boolean pleaseEnd = false;

   public Rs232FiscalPrinterTimer(Timerable timerable) {
      this.setTimerable(timerable);
      this.timerThread.setPriority(10);
      this.timerThread.start();
   }

   public void run() {
      while (!this.pleaseEnd) {
         boolean interrupted = false;

         while (!this.started) {
            synchronized (this.waitObj) {
               try {
                  this.waitObj.wait();
               } catch (InterruptedException var6) {
               }
            }
         }

         try {
            Thread.sleep(this.time);
         } catch (InterruptedException var5) {
            interrupted = true;
         }

         if (!interrupted && !this.pleaseEnd) {
            this.started = false;
            if (this.timerable != null) {
               this.timerable.timerExpired();
            }
         }
      }
   }

   public synchronized void start() {
      this.pleaseEnd = false;
      this.started = true;
      synchronized (this.waitObj) {
         this.waitObj.notifyAll();
      }
   }

   public synchronized void stop() {
      this.started = false;
      if (this.timerThread.isAlive()) {
         this.timerThread.interrupt();
      }
   }

   public synchronized void pleaseEnd() {
      this.pleaseEnd = true;
   }

   public synchronized void setTime(int i) throws IllegalArgumentException {
      if (i < 0) {
         throw new IllegalArgumentException();
      } else {
         this.time = (long)i;
      }
   }

   public synchronized long getTime() {
      return this.time;
   }

   public void setTimerable(Timerable t) {
      this.timerable = t;
   }
}
