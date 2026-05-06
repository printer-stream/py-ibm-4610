package com.ibm.posj.event;

import com.ibm.jutil.RunnableManager;
import java.util.EventListener;
import java.util.HashMap;

public class EventListenerImp implements EventListener {
   protected HashMap listeners = new HashMap();
   protected String name = this.getClass().toString();

   public void addEventListener(EventListener listener) {
      synchronized (this.listeners) {
         if (!this.listeners.containsKey(listener)) {
            EventListenerImp.EventListenerRunnableManager elrM = new EventListenerImp.EventListenerRunnableManager(listener);
            elrM.setName(this.getName() + " RunnableManager");
            elrM.setMaxSize(Long.MAX_VALUE);
            this.listeners.put(listener, elrM);
         }
      }
   }

   public void removeEventListener(EventListener listener) {
      synchronized (this.listeners) {
         if (this.listeners.containsKey(listener)) {
            RunnableManager rM = (RunnableManager)this.listeners.get(listener);
            this.listeners.remove(listener);
            rM.stop();
         }
      }
   }

   public void clear() {
      synchronized (this.listeners) {
         Object[] o = this.listeners.keySet().toArray();

         for (int i = 0; i < o.length; i++) {
            this.removeEventListener((EventListener)o[i]);
         }
      }
   }

   public boolean isEmpty() {
      return this.listeners.isEmpty();
   }

   public void setName(String n) {
      this.name = n;
   }

   public String getName() {
      return this.name;
   }

   protected class EventListenerRunnableManager extends RunnableManager {
      protected EventListener eventListener = null;

      public EventListenerRunnableManager(EventListener el) {
         this.eventListener = el;
      }

      public EventListener getEventListener() {
         return this.eventListener;
      }
   }
}
