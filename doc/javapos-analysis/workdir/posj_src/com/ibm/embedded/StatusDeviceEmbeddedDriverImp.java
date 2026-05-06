package com.ibm.embedded;

import com.ibm.embedded.event.EmbeddedEvent;
import com.ibm.embedded.event.EmbeddedListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class StatusDeviceEmbeddedDriverImp implements StatusDeviceEmbeddedDriver {
   private ArrayList listenerList = new ArrayList();
   private Thread listenerThread = new StatusDeviceEmbeddedDriverImp.ListenerThread();
   public static final int LISTENERPORT = 51717;

   public StatusDeviceEmbeddedDriverImp() {
      this.listenerThread.start();
   }

   public synchronized void addEmbeddedEventListener(EmbeddedListener listener) {
      this.listenerList.add(listener);
   }

   public synchronized void removeEmbeddedEventListener(EmbeddedListener listener) {
      this.listenerList.remove(listener);
   }

   public abstract byte getStatus() throws EmbeddedException;

   class ListenerThread extends Thread {
      public synchronized void fireEmbeddedEvent(EmbeddedEvent embeddedEvent) {
         Iterator listenerIterator = StatusDeviceEmbeddedDriverImp.this.listenerList.iterator();
         EmbeddedListener listener = null;

         while (listenerIterator.hasNext()) {
            listener = (EmbeddedListener)listenerIterator.next();
            listener.statusEventOccurred(embeddedEvent);
         }
      }

      public void run() {
         Socket serviceSocket = null;
         ServerSocket serverSocket = null;
         BufferedReader reader = null;
         byte status = 0;

         try {
            serverSocket = new ServerSocket(51717);

            while (true) {
               serviceSocket = serverSocket.accept();
               reader = new BufferedReader(new InputStreamReader(serviceSocket.getInputStream()));
               status = Byte.parseByte(reader.readLine().trim());
               this.fireEmbeddedEvent(new EmbeddedEvent(this, status));
            }
         } catch (IOException var15) {
         } catch (NumberFormatException var16) {
         } finally {
            try {
               serviceSocket.close();
            } catch (IOException var14) {
            }
         }
      }
   }
}
