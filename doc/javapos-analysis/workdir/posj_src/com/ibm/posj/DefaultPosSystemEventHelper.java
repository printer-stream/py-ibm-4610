package com.ibm.posj;

import com.ibm.jutil.tasks.FifoScheduler;
import com.ibm.jutil.tasks.Task;
import com.ibm.jutil.tasks.TaskScheduler;
import com.ibm.posj.event.PosSystemEvent;
import com.ibm.posj.event.PosSystemListener;
import java.util.ArrayList;
import java.util.List;

public class DefaultPosSystemEventHelper implements PosSystem.EventHelper {
   private List listeners = new ArrayList();
   private TaskScheduler taskScheduler = null;

   public DefaultPosSystemEventHelper() {
      this(new FifoScheduler());
   }

   public DefaultPosSystemEventHelper(TaskScheduler taskScheduler) {
      this.taskScheduler = taskScheduler;
   }

   public void firePosDeviceAttached(PosSystemEvent event) {
      this.taskScheduler.post(new DefaultPosSystemEventHelper.PosDeviceAttachedEventTask(event));
   }

   public void firePosDeviceDetached(PosSystemEvent event) {
      this.taskScheduler.post(new DefaultPosSystemEventHelper.PosDeviceDetachedEventTask(event));
   }

   public synchronized void addPosSystemListener(PosSystemListener listener) {
      if (!this.listeners.contains(listener)) {
         this.listeners.add(listener);
      }
   }

   public synchronized void removePosSystemListener(PosSystemListener listener) {
      this.listeners.remove(listener);
   }

   public synchronized int getListenerCount() {
      return this.listeners.size();
   }

   protected abstract class AbstractEventTask implements Task {
      private PosSystemEvent posSystemEvent = null;

      public AbstractEventTask(PosSystemEvent event) {
         this.posSystemEvent = event;
      }

      protected abstract void fireEvent(PosSystemEvent var1, PosSystemListener var2);

      public void execute() {
         List listenersCopy = null;
         ArrayList var5;
         synchronized (DefaultPosSystemEventHelper.this.listeners) {
            var5 = new ArrayList(DefaultPosSystemEventHelper.this.listeners);
         }

         for (PosSystemListener listener : var5) {
            this.fireEvent(this.posSystemEvent, listener);
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

   protected class PosDeviceAttachedEventTask extends DefaultPosSystemEventHelper.AbstractEventTask {
      public PosDeviceAttachedEventTask(PosSystemEvent event) {
         super(event);
      }

      protected void fireEvent(PosSystemEvent event, PosSystemListener listener) {
         listener.posDeviceAttached(event);
      }
   }

   protected class PosDeviceDetachedEventTask extends DefaultPosSystemEventHelper.AbstractEventTask {
      public PosDeviceDetachedEventTask(PosSystemEvent event) {
         super(event);
      }

      protected void fireEvent(PosSystemEvent event, PosSystemListener listener) {
         listener.posDeviceDetached(event);
      }
   }
}
