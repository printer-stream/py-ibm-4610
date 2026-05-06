package com.ibm.posj;

import com.ibm.jutil.tasks.FifoScheduler;
import com.ibm.jutil.tasks.Task;
import com.ibm.jutil.tasks.TaskScheduler;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.event.DirectIOEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.HandleEvent;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.event.OutputCompleteEvent;
import com.ibm.posj.event.StatusEvent;
import java.util.ArrayList;
import java.util.List;

class DefaultHandleEventHelper implements Handle.EventHelper {
   private List listeners = new ArrayList();
   private Handle handle = null;
   private TaskScheduler taskScheduler = null;
   private static Tracer tracer = TracerFactory.getInstance().createTracer("POSJ", "DefaultHandleEventHelper");

   public DefaultHandleEventHelper(Handle handle) {
      this(handle, new FifoScheduler());
   }

   public DefaultHandleEventHelper(Handle handle, TaskScheduler taskScheduler) {
      this.handle = handle;
      this.taskScheduler = taskScheduler;
   }

   public Handle getHandle() {
      return this.handle;
   }

   public void fireOutputCompleteEvent(OutputCompleteEvent event) {
      this.taskScheduler.post(new DefaultHandleEventHelper.OutputCompleteEventTask(event));
   }

   public void fireDataEvent(DataEvent event) {
      this.taskScheduler.post(new DefaultHandleEventHelper.DataEventTask(event));
   }

   public void fireDirectIOEvent(DirectIOEvent event) {
      this.taskScheduler.post(new DefaultHandleEventHelper.DirectIOEventTask(event));
   }

   public void fireErrorEvent(ErrorEvent event) {
      this.taskScheduler.post(new DefaultHandleEventHelper.ErrorEventTask(event));
   }

   public void fireStatusEvent(StatusEvent event) {
      this.taskScheduler.post(new DefaultHandleEventHelper.StatusEventTask(event));
   }

   public void fireOnlineEvent(OnlineEvent event) {
      this.taskScheduler.post(new DefaultHandleEventHelper.OnlineEventTask(event));
   }

   public void fireOfflineEvent(OfflineEvent event) {
      this.taskScheduler.post(new DefaultHandleEventHelper.OfflineEventTask(event));
   }

   public synchronized void addHandleListener(Handle.Listener listener) {
      if (null != listener && !this.listeners.contains(listener)) {
         this.listeners.add(listener);
      }
   }

   public synchronized void removeHandleListener(Handle.Listener listener) {
      if (null != listener) {
         this.listeners.remove(listener);
      }
   }

   public synchronized int getListenerCount() {
      return this.listeners.size();
   }

   protected abstract class AbstractEventTask implements Task {
      private HandleEvent handleEvent = null;

      public AbstractEventTask(HandleEvent event) {
         this.handleEvent = event;
      }

      protected abstract void fireEvent(HandleEvent var1, Handle.Listener var2);

      public void execute() {
         List listenersCopy = null;
         ArrayList var7;
         synchronized (DefaultHandleEventHelper.this.listeners) {
            var7 = new ArrayList(DefaultHandleEventHelper.this.listeners);
         }

         for (Handle.Listener listener : var7) {
            try {
               this.fireEvent(this.handleEvent, listener);
            } catch (Exception var6) {
               if (DefaultHandleEventHelper.tracer.isOn()) {
                  DefaultHandleEventHelper.tracer.print(var6);
               }
            }
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

   protected class DataEventTask extends DefaultHandleEventHelper.AbstractEventTask {
      public DataEventTask(DataEvent event) {
         super(event);
      }

      protected void fireEvent(HandleEvent event, Handle.Listener listener) {
         listener.dataEventOccurred((DataEvent)event);
      }
   }

   protected class DirectIOEventTask extends DefaultHandleEventHelper.AbstractEventTask {
      public DirectIOEventTask(DirectIOEvent event) {
         super(event);
      }

      protected void fireEvent(HandleEvent event, Handle.Listener listener) {
         listener.directIOEventOccurred((DirectIOEvent)event);
      }
   }

   protected class ErrorEventTask extends DefaultHandleEventHelper.AbstractEventTask {
      public ErrorEventTask(ErrorEvent event) {
         super(event);
      }

      protected void fireEvent(HandleEvent event, Handle.Listener listener) {
         listener.errorEventOccurred((ErrorEvent)event);
      }
   }

   protected class OfflineEventTask extends DefaultHandleEventHelper.AbstractEventTask {
      public OfflineEventTask(OfflineEvent event) {
         super(event);
      }

      protected void fireEvent(HandleEvent event, Handle.Listener listener) {
         listener.offlineEventOccurred((OfflineEvent)event);
      }
   }

   protected class OnlineEventTask extends DefaultHandleEventHelper.AbstractEventTask {
      public OnlineEventTask(OnlineEvent event) {
         super(event);
      }

      protected void fireEvent(HandleEvent event, Handle.Listener listener) {
         listener.onlineEventOccurred((OnlineEvent)event);
      }
   }

   protected class OutputCompleteEventTask extends DefaultHandleEventHelper.AbstractEventTask {
      public OutputCompleteEventTask(OutputCompleteEvent event) {
         super(event);
      }

      protected void fireEvent(HandleEvent event, Handle.Listener listener) {
         listener.outputCompleteEventOccurred((OutputCompleteEvent)event);
      }
   }

   protected class StatusEventTask extends DefaultHandleEventHelper.AbstractEventTask {
      public StatusEventTask(StatusEvent event) {
         super(event);
      }

      protected void fireEvent(HandleEvent event, Handle.Listener listener) {
         listener.statusEventOccurred((StatusEvent)event);
      }
   }
}
