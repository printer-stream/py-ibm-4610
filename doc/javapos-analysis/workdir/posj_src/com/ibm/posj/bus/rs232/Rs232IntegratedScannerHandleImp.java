package com.ibm.posj.bus.rs232;

import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.scanner.IntegratedScannerDataParser;
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232OfflineEvent;
import com.ibm.rs232.event.Rs232OnlineEvent;

public abstract class Rs232IntegratedScannerHandleImp extends Rs232ScannerHandleImp implements Rs232IntegratedScannerProtocolListener {
   private boolean isWatcherEnabled = false;
   private long pollTime = 0L;
   protected int devicePollTime = 0;
   protected Rs232IntegratedScannerProtocol protocol = null;
   protected IntegratedScannerDataParser dataParser = null;

   public Rs232IntegratedScannerHandleImp(HandleKey key, Rs232Port rs232Port, boolean enable, int time, int devicePollTime) {
      super(key, rs232Port);
      this.isWatcherEnabled = enable;
      this.pollTime = (long)time;
      this.devicePollTime = devicePollTime;
   }

   public void writeToPort(byte[] data) {
      if (this.isTracerOn()) {
         this.traceMaximum("-->writeToPort");
      }

      try {
         super.submit(data, 0, data.length);
      } catch (HandleException var3) {
         if (this.isTracerOn()) {
            this.traceMaximum("writeToPort Error: " + var3.toString());
         }
      }

      if (this.isTracerOn()) {
         this.traceMaximum("<--writeToPort");
      }
   }

   public void fireErrorEvent(int type) {
      if (this.isTracerOn()) {
         this.traceMaximum("-->fireErrorEvent");
      }

      this.getHandle().getEventHelper().fireErrorEvent(new ErrorEvent(this, type));
      if (this.isTracerOn()) {
         this.traceMaximum("<--fireErrorEvent");
      }
   }

   public void fireDataEvent(byte[] data) {
      if (this.isTracerOn()) {
         this.traceMaximum("--> fireDataEvent()");
      }

      this.getHandle().getEventHelper().fireDataEvent(this.dataParser.parseData(data));
      if (this.isTracerOn()) {
         this.traceMaximum("<-- fireDataEvent()");
      }
   }

   public void fireDirectIOEvent(byte[] data) {
      if (this.isTracerOn()) {
         this.traceMaximum("--> fireDirectIOEvent()");
      }

      if (this.getHandle().isDirectIOMode()) {
         this.getHandle().getEventHelper().fireDirectIOEvent(new DirectIOEvent(this, data));
         if (this.isTracerOn()) {
            this.traceMaximum("DirectIOEvent fired.");
         }
      } else if (this.isTracerOn()) {
         this.traceMaximum("DirectIOEvent wasn't fired. Handle is not in DirectIO mode");
      }

      if (this.isTracerOn()) {
         this.traceMaximum("<-- fireDirectIOEvent()");
      }
   }

   public void fireOnlineEvent() {
      if (this.isTracerOn()) {
         this.traceMaximum("--> fireOnlineEvent()");
      }

      this.getHandle().getEventHelper().fireOnlineEvent(new OnlineEvent(this, System.currentTimeMillis()));
      if (this.isTracerOn()) {
         this.traceMaximum("<-- fireOnlineEvent()");
      }
   }

   public void fireOfflineEvent() {
      if (this.isTracerOn()) {
         this.traceMaximum("--> fireDirectOfflineEvent()");
      }

      this.getHandle().getEventHelper().fireOfflineEvent(new OfflineEvent(this, System.currentTimeMillis()));
      if (this.isTracerOn()) {
         this.traceMaximum("<-- fireDirectOfflineEvent()");
      }
   }

   protected void onlineEventOccurred(Rs232OnlineEvent rs232OEvent) {
      if (rs232OEvent.getSource() != this.getRs232Port()) {
         if (this.isTracerOn()) {
            this.traceMaximum("Valid Online event occurred");
         }

         super.onlineEventOccurred(rs232OEvent);
      }
   }

   protected void offlineEventOccurred(Rs232OfflineEvent rs232OEvent) {
      if (rs232OEvent.getSource() != this.getRs232Port()) {
         if (this.isTracerOn()) {
            this.traceMaximum("Valid Offline event occurred");
         }

         super.offlineEventOccurred(rs232OEvent);
      }
   }

   public void init() throws HandleException {
      super.init();
      if (this.isWatcherEnabled) {
         this.getRs232Port().getOnlineWatcher().setEnable(this.isWatcherEnabled, new byte[0], this.pollTime);
      }
   }
}
