package com.ibm.rs232;

import com.ibm.jutil.tasks.FifoScheduler;
import com.ibm.jutil.tasks.Task;
import com.ibm.jutil.tasks.TaskScheduler;
import com.ibm.rs232.event.Rs232ControlLineEvent;
import com.ibm.rs232.event.Rs232DataEvent;
import com.ibm.rs232.event.Rs232ErrorEvent;
import com.ibm.rs232.event.Rs232Event;
import com.ibm.rs232.event.Rs232OfflineEvent;
import com.ibm.rs232.event.Rs232OnlineEvent;
import java.util.ArrayList;
import java.util.List;

public class DefaultRs232PortEventHelper implements Rs232Port.EventHelper {
   private List listeners = new ArrayList();
   private Rs232Port rs232Port = null;
   private TaskScheduler taskScheduler = null;

   public DefaultRs232PortEventHelper(Rs232Port rs232Port) {
      this(rs232Port, new FifoScheduler());
   }

   public DefaultRs232PortEventHelper(Rs232Port rs232Port, TaskScheduler taskScheduler) {
      this.rs232Port = rs232Port;
      this.taskScheduler = taskScheduler;
   }

   public Rs232Port getRs232Port() {
      return this.rs232Port;
   }

   public void fireDataEvent(Rs232DataEvent event) {
      this.taskScheduler.post(new DefaultRs232PortEventHelper.Rs232DataEventTask(event));
   }

   public void fireErrorEvent(Rs232ErrorEvent event) {
      this.taskScheduler.post(new DefaultRs232PortEventHelper.Rs232ErrorEventTask(event));
   }

   public void fireOfflineEvent(Rs232OfflineEvent event) {
      this.taskScheduler.post(new DefaultRs232PortEventHelper.Rs232OfflineEventTask(event));
   }

   public void fireOnlineEvent(Rs232OnlineEvent event) {
      this.taskScheduler.post(new DefaultRs232PortEventHelper.Rs232OnlineEventTask(event));
   }

   public void fireControlLineEvent(Rs232ControlLineEvent event) {
      this.taskScheduler.post(new DefaultRs232PortEventHelper.Rs232ControlLineEventTask(event));
   }

   public synchronized void addRs232PortListener(Rs232Port.Listener listener) {
      if (null != listener && !this.listeners.contains(listener)) {
         this.listeners.add(listener);
      }
   }

   public synchronized void removeRs232PortListener(Rs232Port.Listener listener) {
      if (null != listener) {
         this.listeners.remove(listener);
      }
   }

   public synchronized int getListenerCount() {
      return this.listeners.size();
   }

   protected abstract class AbstractEventTask implements Task {
      private Rs232Event rs232Event = null;

      public AbstractEventTask(Rs232Event event) {
         this.rs232Event = event;
      }

      protected abstract void fireEvent(Rs232Event var1, Rs232Port.Listener var2);

      public void execute() {
         List listenersCopy = null;
         ArrayList var5;
         synchronized (DefaultRs232PortEventHelper.this.listeners) {
            var5 = new ArrayList(DefaultRs232PortEventHelper.this.listeners);
         }

         for (Rs232Port.Listener listener : var5) {
            this.fireEvent(this.rs232Event, listener);
         }
      }

      public Exception getException() {
         return null;
      }

      public boolean isExecuted() {
         return false;
      }

      public boolean isInException() {
         return false;
      }

      public String getName() {
         return "";
      }
   }

   protected class Rs232ControlLineEventTask extends DefaultRs232PortEventHelper.AbstractEventTask {
      public Rs232ControlLineEventTask(Rs232Event event) {
         super(event);
      }

      protected void fireEvent(Rs232Event event, Rs232Port.Listener listener) {
         listener.controlLineEventOccurred((Rs232ControlLineEvent)event);
      }
   }

   protected class Rs232DataEventTask extends DefaultRs232PortEventHelper.AbstractEventTask {
      public Rs232DataEventTask(Rs232DataEvent event) {
         super(event);
      }

      protected void fireEvent(Rs232Event event, Rs232Port.Listener listener) {
         listener.dataEventOccurred((Rs232DataEvent)event);
      }
   }

   protected class Rs232ErrorEventTask extends DefaultRs232PortEventHelper.AbstractEventTask {
      public Rs232ErrorEventTask(Rs232ErrorEvent event) {
         super(event);
      }

      protected void fireEvent(Rs232Event event, Rs232Port.Listener listener) {
         listener.errorEventOccurred((Rs232ErrorEvent)event);
      }
   }

   protected class Rs232OfflineEventTask extends DefaultRs232PortEventHelper.AbstractEventTask {
      public Rs232OfflineEventTask(Rs232OfflineEvent event) {
         super(event);
      }

      protected void fireEvent(Rs232Event event, Rs232Port.Listener listener) {
         listener.offlineEventOccurred((Rs232OfflineEvent)event);
      }
   }

   protected class Rs232OnlineEventTask extends DefaultRs232PortEventHelper.AbstractEventTask {
      public Rs232OnlineEventTask(Rs232OnlineEvent event) {
         super(event);
      }

      protected void fireEvent(Rs232Event event, Rs232Port.Listener listener) {
         listener.onlineEventOccurred((Rs232OnlineEvent)event);
      }
   }
}
