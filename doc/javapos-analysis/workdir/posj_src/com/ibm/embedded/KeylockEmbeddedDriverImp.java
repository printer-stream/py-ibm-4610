package com.ibm.embedded;

import com.ibm.embedded.event.EmbeddedEvent;
import com.ibm.embedded.event.EmbeddedListener;
import java.util.ArrayList;
import java.util.Iterator;

public class KeylockEmbeddedDriverImp implements KeylockEmbeddedDriver, Runnable {
   private Thread socketThread;
   private ArrayList listeners = new ArrayList();

   public KeylockEmbeddedDriverImp() {
      this.socketThread = new Thread(this);
      this.socketThread.start();
   }

   public byte getStatus() throws EmbeddedException {
      return (byte)((int)this.nativeGetKLStatus());
   }

   public void addEmbeddedEventListener(EmbeddedListener listener) throws EmbeddedException {
      if (this.listeners != null) {
         this.listeners.add(listener);
      }
   }

   public void removeEmbeddedEventListener(EmbeddedListener listener) throws EmbeddedException {
      this.listeners.remove(listener);
   }

   public void run() {
      byte stat = (byte)((int)this.nativeGetKLStatus());
      byte oldStat = (byte)((int)(this.nativeGetKLStatus() - 1L));

      while (true) {
         try {
            Thread.sleep(500L);
         } catch (InterruptedException var4) {
         }

         stat = (byte)((int)this.nativeGetKLStatus());
         if (oldStat != stat) {
            oldStat = stat;
            this.firePciEvent(new EmbeddedEvent(this, stat));
         }
      }
   }

   public synchronized void firePciEvent(EmbeddedEvent pe) {
      Iterator i = this.listeners.iterator();

      while (i.hasNext()) {
         ((EmbeddedListener)i.next()).statusEventOccurred(pe);
      }
   }

   public void accept(EmbeddedDriverVisitor visitor) {
      visitor.visitKeylock(this);
   }

   public native long nativeGetKLStatus();

   static {
      try {
         System.loadLibrary("aipposembedded");
      } catch (UnsatisfiedLinkError var1) {
      }
   }
}
