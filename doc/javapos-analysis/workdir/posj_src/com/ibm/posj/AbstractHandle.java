package com.ibm.posj;

import com.ibm.jutil.logging.LogHelper;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.event.OutputCompleteEvent;
import com.ibm.posj.event.PosSystemEvent;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevCat;

public abstract class AbstractHandle implements Handle {
   private SystemCmd.Factory systemCmdFactory = new DefaultSystemCmd.Factory();
   private HandleCmd.Factory handleCmdFactory = new DefaultHandleCmdFactory();
   private Handle.EventHelper eventHelper = new DefaultHandleEventHelper(this);
   protected HandleImp handleImp = null;
   protected int handleCount = 0;
   protected boolean asyncMode = false;
   protected int submissionCount = 0;
   protected int pendingSubmissionCount = 0;
   private boolean initialized = false;
   private boolean directIOMode = false;
   private AbstractHandle.State state = new AbstractHandle.State();
   private LogHelper logHelper = null;
   private AbstractHandle.LogHandleListener logHandleListener = null;
   private boolean isFlashing = false;
   private HandleCmd pendingCmd;

   public AbstractHandle() {
      this.initLogging();
   }

   public abstract DevCat getDevCat();

   public abstract void accept(HandleVisitor var1);

   int getHandleCount() {
      return this.handleCount;
   }

   static String copyright() {
      return "\n\nLicensed Materials - Property of IBM\nPackage: com.ibm.posj\n(C) Copyright IBM Corp. 2002,2003. All Rights Reserved.\nUS Government Users Restricted Rights - Use, duplication or\ndisclosure restricted by GSA ADP Schedule Contract with IBM Corp.\n\n";
   }

   public HandleImp getHandleImp() {
      return this.handleImp;
   }

   public Handle.EventHelper getEventHelper() {
      return this.eventHelper;
   }

   public void setHandleImp(HandleImp handleImp) throws HandleException {
      if (handleImp == null) {
         throw new HandleException("Cannot set this handle HandleImp to null");
      } else if (!handleImp.getDevCat().equals(this.getDevCat())) {
         throw new HandleException("Cannot set HandleImp with different DevCat to this handle");
      } else {
         this.handleImp = handleImp;
      }
   }

   public void setOnline(boolean b) {
      this.getState().setOnline(b);
   }

   public Handle.State getState() {
      return this.state;
   }

   public String getName() {
      return this.getDevCat().toString() + this.getHandleCount();
   }

   public DevBus getDevBus() {
      return this.getHandleImp().getDevBus();
   }

   public HandleKey getHandleKey() {
      return this.getHandleImp().getHandleKey();
   }

   public void addHandleListener(Handle.Listener listener) {
      this.eventHelper.addHandleListener(listener);
   }

   public void removeHandleListener(Handle.Listener listener) {
      this.eventHelper.removeHandleListener(listener);
   }

   public boolean isOnline() {
      return this.getState().isOnline();
   }

   public boolean isInit() throws HandleException {
      if (this.isOnline()) {
         return this.initialized;
      } else {
         throw new HandleException("Handle is offline!");
      }
   }

   public void init() throws HandleException {
      if (!this.isInit()) {
         this.getHandleImp().init();
         this.initialized = true;
      }
   }

   public boolean isAsyncMode() {
      return this.asyncMode;
   }

   public void setAsyncMode(boolean b) throws HandleException {
      this.asyncMode = b;
   }

   public boolean isDirectIOMode() {
      return this.directIOMode;
   }

   public void setDirectIOMode(boolean b) {
      this.directIOMode = b;
   }

   public int getSubmissionCount() {
      return this.submissionCount;
   }

   public int getPendingSubmissionCount() {
      return this.pendingSubmissionCount;
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.pendingCmd = cmd;
      if (this.isFlashing) {
         throw new HandleException("Attempt to submit a command while flash update in progress");
      } else if (!this.initialized) {
         throw new HandleException("Attempt to submit a command to a non-initialized handle");
      } else {
         this.submissionCount++;

         try {
            if (this.isAsyncMode()) {
               this.getHandleImp().asyncSubmit(cmd);
            } else {
               this.getHandleImp().submit(cmd);
            }
         } catch (HandleException var3) {
            this.getLogHelper().addLogEntry(1008, "Recieved Command Reject from Device", "AllDevices", 4);
            throw var3;
         }
      }
   }

   public synchronized void flash(FlashRequest flashRequest) throws FlashException {
      this.isFlashing = true;
      if (this.submissionCount == 0) {
         try {
            this.getHandleImp().flash(flashRequest);
         } catch (FlashException var7) {
            this.isFlashing = false;
            throw var7;
         }
      } else {
         synchronized (this.pendingCmd) {
            if (!this.pendingCmd.isCompleted()) {
               this.isFlashing = false;
               throw new FlashException("Attempting to flash while a submitted command is pending");
            }

            try {
               this.getHandleImp().flash(flashRequest);
            } catch (FlashException var5) {
               this.isFlashing = false;
               throw var5;
            }
         }
      }

      this.isFlashing = false;
   }

   public boolean isFlashable() {
      return this.getHandleImp().isFlashable();
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.handleCmdFactory;
   }

   public SystemCmd.Factory getSystemCmdFactory() {
      return this.systemCmdFactory;
   }

   protected LogHelper getLogHelper() {
      if (this.logHelper == null) {
         this.logHelper = PosSystemManager.getInstance().getLogHelper();
      }

      return this.logHelper;
   }

   protected AbstractHandle.LogHandleListener getLogHandleListener() {
      if (this.logHandleListener == null) {
         this.logHandleListener = new AbstractHandle.LogHandleListener();
      }

      return this.logHandleListener;
   }

   protected void initLogging() {
      this.addHandleListener(this.getLogHandleListener());
   }

   protected boolean isInitialized() {
      return this.initialized;
   }

   public class LogHandleListener extends DefaultHandleListener {
      public void outputCompleteEventOccurred(OutputCompleteEvent event) {
      }

      public void dataEventOccurred(DataEvent event) {
      }

      public void statusEventOccurred(StatusEvent event) {
      }

      public void errorEventOccurred(ErrorEvent event) {
         this.addErrorEventToLog();
      }

      public void onlineEventOccurred(OnlineEvent event) {
         this.checkIfExpectedOnlineRequestForLogging();
      }

      public void offlineEventOccurred(OfflineEvent event) {
         this.addOfflineEventToLog();
      }

      protected void checkIfExpectedOnlineRequestForLogging() {
         if (AbstractHandle.this.isOnline()) {
            AbstractHandle.this.getLogHelper().addLogEntry(1004, "Received unexpected request-on-line", "AllDevices", 4);
         }
      }

      protected void addOfflineEventToLog() {
         AbstractHandle.this.getLogHelper().addLogEntry(1001, "Device Offline", "AllDevices", 4);
      }

      protected void addErrorEventToLog() {
         AbstractHandle.this.getLogHelper().addLogEntry(1000, "Device Hardware error/failure", "AllDevices", 4);
      }
   }

   protected class State implements Handle.State {
      private boolean online = true;

      public State() {
      }

      public boolean isOnline() {
         return this.online;
      }

      public void setOnline(boolean b) {
         if (this.isOnline() != b) {
            this.online = b;
            if (AbstractHandle.this.getState().isOnline()) {
               PosSystemManager.getInstance().getEventHelper().firePosDeviceAttached(new PosSystemEvent(this));
            } else {
               PosSystemManager.getInstance().getEventHelper().firePosDeviceDetached(new PosSystemEvent(this));
            }
         }
      }
   }
}
