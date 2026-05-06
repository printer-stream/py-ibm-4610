package com.ibm.posj.bus;

import com.ibm.jutil.RunnableManager;
import com.ibm.jutil.tasks.FifoScheduler;
import com.ibm.jutil.tasks.Task;
import com.ibm.jutil.tasks.TaskScheduler;
import com.ibm.posj.HandleFactory;
import com.ibm.posj.HandleRegistry;
import com.ibm.posj.PosException;
import com.ibm.posj.PosSystem;

public abstract class AbstractHandlePopulator implements HandlePopulator {
   private boolean started = false;
   private HandleRegistry handleRegistry = null;
   private HandleFactory handleFactory = null;
   private PosSystem.EventHelper eventHelper = null;
   protected TaskScheduler scheduler = new FifoScheduler();
   protected RunnableManager runnableManager = new RunnableManager();
   protected Exception lastException = null;

   public AbstractHandlePopulator(HandleRegistry registry, HandleFactory factory, PosSystem.EventHelper eventHelper) {
      this.handleRegistry = registry;
      this.handleFactory = factory;
      this.eventHelper = eventHelper;
   }

   protected HandleFactory getHandleFactory() {
      return this.handleFactory;
   }

   protected PosSystem.EventHelper getEventHelper() {
      return this.eventHelper;
   }

   protected void postTask(Task task) {
      this.scheduler.post(task);
   }

   protected RunnableManager getRunnableManager() {
      return this.runnableManager;
   }

   public HandleRegistry getHandleRegistry() {
      return this.handleRegistry;
   }

   public void stop() throws PosException {
      this.scheduler.stop();
      this.started = false;
   }

   public void start() throws PosException {
      this.started = true;
      this.scheduler.start();
   }

   public boolean isStarted() {
      return this.started;
   }

   public Exception getLastException() {
      return this.lastException;
   }
}
