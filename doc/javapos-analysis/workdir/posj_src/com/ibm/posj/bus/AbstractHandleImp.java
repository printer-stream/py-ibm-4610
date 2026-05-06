package com.ibm.posj.bus;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.RunnableManager;
import com.ibm.jutil.logging.LogHelper;
import com.ibm.pattern.Recyclable;
import com.ibm.pattern.RecycleFactory;
import com.ibm.pattern.Recycler;
import com.ibm.posj.AbstractHandleCmd;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.bus.hid.HidFlashHandleImp;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevCat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public abstract class AbstractHandleImp implements HandleImp {
   protected Handle handle = null;
   protected HandleKey handleKey = null;
   protected RunnableManager asyncManager = null;
   protected Recycler runnableFactory = null;
   protected BooleanMonitor isLocked = new BooleanMonitor(false);
   private List handleImpGroup = new Vector();
   private LogHelper logHelper = null;

   public AbstractHandleImp(HandleKey key) {
      this.handleKey = key;
   }

   public HandleKey getHandleKey() {
      return this.handleKey;
   }

   public abstract void accept(HandleImpVisitor var1);

   public abstract DevCat getDevCat();

   public abstract DevBus getDevBus();

   public abstract void init() throws HandleException;

   public void flash(FlashRequest flashRequest) throws FlashException {
      System.out.println(flashRequest.getFlashFile().getFilename());
      throw new FlashException("Needs to be overridden by subclass for use");
   }

   public FlashHandleImp getFlashHandleImp() throws FlashException {
      throw new FlashException("Needs to be overridden by subclass for use");
   }

   public abstract short getECLevel();

   public boolean isComposite() {
      return false;
   }

   public boolean isCompositeParent() {
      return false;
   }

   public boolean isSubDevice() {
      return false;
   }

   public List getHandleImpGroup() {
      return this.handleImpGroup;
   }

   public void lock() throws FlashException {
      this.isLocked.set(true);
   }

   public void unlock() {
      this.isLocked.set(false);
   }

   public String toString() {
      return "HandleImp <DevCat = " + this.getDevCat() + ", HandleKey = " + this.getHandleKey() + ">";
   }

   protected void setHandle(Handle handle) {
      if (this.handle != null) {
         throw new RuntimeException("Should not set handle of handleImp more than once");
      } else {
         this.handle = handle;
      }
   }

   protected void setHandleOnline(boolean b) {
      this.getHandle().getState().setOnline(b);
   }

   protected void setHandleCmdResultInError(HandleCmd handleCmd, boolean inError) {
      handleCmd.getResult().setInError(inError);
   }

   protected RunnableManager getAsyncManager() {
      if (null == this.asyncManager) {
         this.asyncManager = new RunnableManager(true);
      }

      return this.asyncManager;
   }

   protected Recycler getAsyncRunnableFactory() {
      if (null == this.runnableFactory) {
         this.runnableFactory = new AbstractHandleImp.AsyncRunnableFactory();
      }

      return this.runnableFactory;
   }

   protected void lockSubDevices() throws FlashException {
      Iterator subDevices = this.getSubDevices();
      if (null != subDevices) {
         while (subDevices.hasNext()) {
            Object subDevice = subDevices.next();
            if (subDevice instanceof HandleImp) {
               HandleImp currDevice = (HandleImp)subDevice;
               currDevice.lock();
            }
         }
      }
   }

   protected void unlockSubDevices() throws FlashException {
      Iterator subDevices = this.getSubDevices();
      if (null != subDevices) {
         while (subDevices.hasNext()) {
            Object subDevice = subDevices.next();
            if (subDevice instanceof HandleImp) {
               HandleImp currDevice = (HandleImp)subDevice;
               currDevice.unlock();
            }
         }
      }
   }

   protected Iterator getSubDevices() throws FlashException {
      if (!this.isCompositeParent()) {
         return null;
      } else if (this instanceof POSPrinterHandleImp) {
         return ((POSPrinterHandleImp)this).getSecondaryHandleImps();
      } else if (this instanceof POSKeyboardHandleImp) {
         return this.getPOSKeyboardSecondaryHandleImps();
      } else {
         throw new FlashException("Composite Parent Device not appropriately registered to get sub devices");
      }
   }

   protected Iterator getPOSKeyboardSecondaryHandleImps() {
      List secondaryList = new ArrayList();

      for (HandleImp currHandleImp : this.getHandleImpGroup()) {
         if (!(currHandleImp instanceof POSKeyboardHandleImp) && !(currHandleImp instanceof HidFlashHandleImp)) {
            secondaryList.add(currHandleImp);
         }
      }

      return secondaryList.iterator();
   }

   protected LogHelper getLogHelper() {
      if (this.logHelper == null) {
         this.logHelper = PosSystemManager.getInstance().getLogHelper();
      }

      return this.logHelper;
   }

   public Handle getHandle() {
      return this.handle;
   }

   public void submit(HandleCmd cmd) throws HandleException {
      throw new RuntimeException("Must be overridden by subclass!!");
   }

   public void asyncSubmit(HandleCmd cmd) throws HandleException {
      AbstractHandleImp.AsyncRunnable runnable = (AbstractHandleImp.AsyncRunnable)this.getAsyncRunnableFactory().takeRecyclable();
      runnable.setHandleCmd(cmd);
      this.getAsyncManager().add(runnable);
   }

   protected class AsyncRunnable implements Runnable, Recyclable {
      private AbstractHandleImp.AsyncRunnableFactory factory = null;
      private HandleCmd handleCmd = null;

      public AsyncRunnable(AbstractHandleImp.AsyncRunnableFactory factory) {
         this.factory = factory;
      }

      public void setHandleCmd(HandleCmd cmd) {
         this.handleCmd = cmd;
      }

      public HandleCmd getHandleCmd() {
         return this.handleCmd;
      }

      public void run() {
         try {
            AbstractHandleImp.this.submit(this.getHandleCmd());
         } catch (HandleException var5) {
            AbstractHandleCmd.DefaultResult result = new AbstractHandleCmd.DefaultResult();
            result.setHandleException(var5);
            result.setInError(true);
            this.getHandleCmd().setResult(result);
            this.getHandleCmd().setCompleted(true);
         }

         try {
            this.getHandleCmd().getCallback().execute(this.getHandleCmd());
         } catch (Exception var3) {
         } catch (Error var4) {
         }

         this.recycle();
      }

      public void recycle() {
         this.factory.returnAsyncRunnable(this);
      }

      public void clean() {
         this.handleCmd = null;
      }
   }

   protected class AsyncRunnableFactory extends RecycleFactory {
      protected Recyclable createRecyclable() {
         return AbstractHandleImp.this.new AsyncRunnable(this);
      }

      public void returnAsyncRunnable(AbstractHandleImp.AsyncRunnable asyncRunnable) {
         this.returnRecyclable(asyncRunnable);
      }
   }
}
