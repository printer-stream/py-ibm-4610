package com.ibm.embedded;

import com.ibm.embedded.event.EmbeddedEvent;
import com.ibm.embedded.event.EmbeddedListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class CashDrawerPciDriverImp implements CashDrawerEmbeddedDriver, Runnable {
   private byte numDrawers;
   private ArrayList listeners;
   private Thread socketThread;
   private long adapterID;
   public static final int PORT = 51717;

   public CashDrawerPciDriverImp(long numberOfCashDrawers, long aid) {
      this.numDrawers = (byte)((int)numberOfCashDrawers);
      this.adapterID = aid;
      this.listeners = new ArrayList();
      this.socketThread = new Thread(this);
      this.socketThread.start();
   }

   public void accept(EmbeddedDriverVisitor visitor) {
      visitor.visitCashDrawer(this);
   }

   public void run() {
      Socket socket = null;
      ServerSocket ss = null;
      BufferedReader in = null;
      PrintWriter out = null;
      String str = "";

      try {
         ss = new ServerSocket(51717);

         while (true) {
            socket = ss.accept();

            try {
               in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
               str = in.readLine();
               str = str.trim();
               byte b3;
               if (this.adapterID == 4614L) {
                  b3 = (byte)(new Integer(str).byteValue() * 32 | 63);
               } else {
                  b3 = new Integer(str).byteValue();
               }

               this.firePciEvent(new EmbeddedEvent(this, b3));
            } catch (IOException var8) {
               socket.close();
            } catch (NumberFormatException var9) {
            }
         }
      } catch (IOException var10) {
      }
   }

   public void disableAsyncCDComm() throws EmbeddedException {
      this.nativeDisableAsyncCDComm();
   }

   public void openCD(int cdNum) throws EmbeddedException {
      this.nativeOpenDrawer((long)cdNum);
   }

   public byte getCDStatus() throws EmbeddedException {
      long status = this.nativeGetCDStatus();
      byte cdStatus = 0;
      if (this.adapterID == 4614L) {
         cdStatus = (byte)((int)(status * 32L | 63L));
      } else {
         cdStatus = (byte)((int)status);
      }

      return cdStatus;
   }

   public byte getNumberOfDrawers() throws EmbeddedException {
      return this.numDrawers;
   }

   public synchronized void addEmbeddedEventListener(EmbeddedListener listener) {
      if (this.listeners != null) {
         this.listeners.add(listener);
      }
   }

   public synchronized void removeEmbeddedEventListener(EmbeddedListener listener) {
      this.listeners.remove(listener);
   }

   public synchronized void firePciEvent(EmbeddedEvent pe) {
      Iterator i = this.listeners.iterator();

      while (i.hasNext()) {
         ((EmbeddedListener)i.next()).statusEventOccurred(pe);
      }
   }

   public long getAdapterID() {
      return this.adapterID;
   }

   public native void nativeDisableAsyncCDComm();

   public native void nativeOpenDrawer(long var1);

   public native long nativeGetCDStatus();

   static {
      try {
         System.loadLibrary("aipposembedded");
      } catch (UnsatisfiedLinkError var1) {
      }
   }
}
