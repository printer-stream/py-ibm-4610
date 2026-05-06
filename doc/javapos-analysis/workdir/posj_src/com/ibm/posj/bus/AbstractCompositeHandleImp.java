package com.ibm.posj.bus;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevCat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public abstract class AbstractCompositeHandleImp implements CompositeHandleImp {
   private HandleImp mainHandleImp = null;
   private List secondaryHandleImpList = new LinkedList();
   private BooleanMonitor isLocked = new BooleanMonitor(false);
   private List handleImpGroup = new Vector();

   public AbstractCompositeHandleImp() {
      this.mainHandleImp = this;
   }

   public AbstractCompositeHandleImp(HandleImp handleImp) {
      this.mainHandleImp = handleImp;
   }

   public AbstractCompositeHandleImp(Iterator handleImps) {
      this.mainHandleImp = this;

      while (handleImps.hasNext()) {
         this.secondaryHandleImpList.add(handleImps.next());
      }
   }

   public AbstractCompositeHandleImp(HandleImp handleImp, Iterator handleImps) {
      this.mainHandleImp = handleImp;

      while (handleImps.hasNext()) {
         this.secondaryHandleImpList.add(handleImps.next());
      }
   }

   public HandleImp getMainHandleImp() {
      return this.mainHandleImp;
   }

   public Iterator getSecondaryHandleImps() {
      return this.secondaryHandleImpList.iterator();
   }

   public HandleKey getHandleKey() {
      return this.getMainHandleImp().getHandleKey();
   }

   public void accept(HandleImpVisitor visitor) {
      this.getMainHandleImp().accept(visitor);
   }

   public DevCat getDevCat() {
      return this.getMainHandleImp().getDevCat();
   }

   public DevBus getDevBus() {
      return this.getMainHandleImp().getDevBus();
   }

   public void init() throws HandleException {
      this.getMainHandleImp().init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.getMainHandleImp().submit(cmd);
   }

   public void submitCmd(String name, byte[] cmd) throws HandleException {
   }

   public void flash(FlashRequest flashRequest) throws FlashException {
      throw new FlashException("not implemented in composite - devices should have an indivdual flash method");
   }

   public FlashHandleImp getFlashHandleImp() throws FlashException {
      throw new FlashException("not implemented in composite - devices should have an indivdual flash method");
   }

   public short getECLevel() {
      return 32767;
   }

   public boolean isFlashable() {
      return this.isCompositeParent();
   }

   public boolean isComposite() {
      return true;
   }

   public boolean isCompositeParent() {
      return false;
   }

   public List getHandleImpGroup() {
      return this.handleImpGroup;
   }

   public void asyncSubmit(HandleCmd cmd) throws HandleException {
      this.getMainHandleImp().asyncSubmit(cmd);
   }

   public Handle getHandle() {
      return this.getMainHandleImp().getHandle();
   }

   public void lock() throws FlashException {
      this.isLocked.set(true);

      try {
         this.isLocked.waitForFalse(-1);
      } catch (Exception var2) {
         throw new FlashException("Error while waiting for device to unlock from flashing");
      }
   }

   public void unlock() {
      this.isLocked.set(false);
   }

   protected List getSecondaryHandleImpList() {
      return this.secondaryHandleImpList;
   }

   protected void lockSubDevices() throws FlashException {
      Iterator subDevices = this.getSecondaryHandleImps();

      while (subDevices.hasNext()) {
         HandleImp currDevice = (HandleImp)subDevices.next();
         currDevice.lock();
      }
   }

   protected void unlockSubDevices() throws FlashException {
      Iterator subDevices = this.getSecondaryHandleImps();

      while (subDevices.hasNext()) {
         HandleImp currDevice = (HandleImp)subDevices.next();
         currDevice.unlock();
      }
   }
}
