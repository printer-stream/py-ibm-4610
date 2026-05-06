package com.ibm.posj.bus.rs232.javaxcomm;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.rs232.Rs232Exception;
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232OfflineEvent;
import com.ibm.rs232.event.Rs232OnlineEvent;

class Rs232OnlineWatcher implements Rs232Port.OnlineWatcher {
   private boolean isEnable;
   private byte[] command = new byte[0];
   private Rs232Port rs232Port;
   private Rs232Port.EventHelper eventHelper;
   private Rs232OnlineWatcher.Online online = null;
   private final Tracer tracer = TracerFactory.getInstance().createTracer("Rs232OnlineWatcher");

   Rs232OnlineWatcher(Rs232Port port, Rs232Port.EventHelper helper) {
      this.rs232Port = port;
      this.eventHelper = helper;
      this.isEnable = false;
   }

   public void setEnable(boolean b, byte[] cmd, long time) {
      if (this.tracer.isOn()) {
         this.trace("->setEnable()," + b);
      }

      if (b) {
         this.getOnline().setPollTime(time);
         this.command = cmd;
         this.getOnline().resume();
      } else {
         this.getOnline().pause();
      }

      this.isEnable = b;
      if (this.tracer.isOn()) {
         this.trace("<-setEnable()");
      }
   }

   public boolean isEnable() {
      return this.isEnable;
   }

   protected void checkOnline() {
      if (this.tracer.isOn()) {
         this.trace("->CheckOnline");
      }

      try {
         this.rs232Port.submit(this.command);
      } catch (Rs232Exception var2) {
         if (this.tracer.isOn()) {
            if (var2.getOriginalException() != null) {
               this.tracer.print(var2.getOriginalException());
            } else {
               this.tracer.print(var2);
            }
         }

         this.processOffline();
      }

      if (this.tracer.isOn()) {
         this.trace("<-CheckOnline");
      }
   }

   protected void processOffline() {
      this.eventHelper.fireOfflineEvent(new Rs232OfflineEvent(this));
      this.rs232Port.close("RS232");

      try {
         this.rs232Port.open("RS232");
         this.eventHelper.fireOnlineEvent(new Rs232OnlineEvent(this));
      } catch (Rs232Exception var2) {
         if (this.tracer.isOn()) {
            if (var2.getOriginalException() != null) {
               this.tracer.print(var2.getOriginalException());
            } else {
               this.tracer.print(var2);
            }
         }
      }
   }

   protected Rs232OnlineWatcher.Online getOnline() {
      if (this.online == null) {
         this.online = new Rs232OnlineWatcher.Online();
         new Thread(this.online).start();
      }

      return this.online;
   }

   private void trace(String msg) {
      this.tracer.println(2, this.rs232Port.getRs232config().getPortName() + " " + msg);
   }

   class Online implements Runnable {
      private boolean paused = false;
      private long pollTime = 3000L;
      private Object resumeLock = new Object();

      void pause() {
         if (!this.paused) {
            this.paused = true;
         }
      }

      public void resume() {
         if (this.paused) {
            this.paused = false;
            synchronized (this.resumeLock) {
               this.resumeLock.notifyAll();
            }
         }
      }

      public void setPollTime(long time) {
         this.pollTime = time;
      }

      public void run() {
         while (true) {
            synchronized (this) {
               try {
                  this.wait(this.pollTime);
               } catch (InterruptedException var7) {
               }
            }

            Rs232OnlineWatcher.this.checkOnline();

            while (this.paused) {
               synchronized (this.resumeLock) {
                  try {
                     this.resumeLock.wait();
                  } catch (InterruptedException var5) {
                  }
               }
            }
         }
      }
   }
}
