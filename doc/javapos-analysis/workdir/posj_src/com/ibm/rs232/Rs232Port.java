package com.ibm.rs232;

import com.ibm.rs232.event.Rs232ControlLineEvent;
import com.ibm.rs232.event.Rs232DataEvent;
import com.ibm.rs232.event.Rs232ErrorEvent;
import com.ibm.rs232.event.Rs232OfflineEvent;
import com.ibm.rs232.event.Rs232OnlineEvent;
import java.util.EventListener;
import java.util.Iterator;

public interface Rs232Port {
   void init() throws Rs232Exception;

   void open(String var1) throws Rs232Exception;

   void close(String var1);

   boolean isOpen();

   Iterator iterator();

   void submit(byte[] var1) throws Rs232Exception;

   void submit(byte[] var1, int var2, int var3) throws Rs232Exception;

   Rs232Config getRs232config();

   void addRs232PortListener(Rs232Port.Listener var1);

   void removeRs232PortListener(Rs232Port.Listener var1);

   Rs232Port.OnlineWatcher getOnlineWatcher();

   public interface EventHelper {
      Rs232Port getRs232Port();

      void fireDataEvent(Rs232DataEvent var1);

      void fireErrorEvent(Rs232ErrorEvent var1);

      void fireOnlineEvent(Rs232OnlineEvent var1);

      void fireOfflineEvent(Rs232OfflineEvent var1);

      void fireControlLineEvent(Rs232ControlLineEvent var1);

      void addRs232PortListener(Rs232Port.Listener var1);

      void removeRs232PortListener(Rs232Port.Listener var1);

      int getListenerCount();
   }

   public interface Listener extends EventListener {
      void dataEventOccurred(Rs232DataEvent var1);

      void errorEventOccurred(Rs232ErrorEvent var1);

      void onlineEventOccurred(Rs232OnlineEvent var1);

      void offlineEventOccurred(Rs232OfflineEvent var1);

      void controlLineEventOccurred(Rs232ControlLineEvent var1);
   }

   public interface OnlineWatcher {
      void setEnable(boolean var1, byte[] var2, long var3);

      boolean isEnable();
   }
}
