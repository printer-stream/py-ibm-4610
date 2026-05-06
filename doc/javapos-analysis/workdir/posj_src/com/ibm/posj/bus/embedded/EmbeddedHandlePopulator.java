package com.ibm.posj.bus.embedded;

import com.ibm.embedded.CashDrawerEmbeddedDriver;
import com.ibm.embedded.EmbeddedDriver;
import com.ibm.embedded.EmbeddedDriverVisitor;
import com.ibm.embedded.EmbeddedException;
import com.ibm.embedded.EmbeddedManager;
import com.ibm.embedded.HardTotalsEmbeddedDriver;
import com.ibm.embedded.KeylockEmbeddedDriver;
import com.ibm.embedded.MotionSensorEmbeddedDriver;
import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.HandleFactory;
import com.ibm.posj.HandleFactoryException;
import com.ibm.posj.HandleRegistry;
import com.ibm.posj.PosException;
import com.ibm.posj.PosSystem;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.bus.AbstractHandlePopulator;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.PosjUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class EmbeddedHandlePopulator extends AbstractHandlePopulator {
   private List deviceHandleList = new LinkedList();
   private HandleFactoryException hfE = null;
   private EmbeddedHandleImpFactory hiFactory = null;
   private EmbeddedDriverVisitor visitor = null;
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevBuses.EMBEDDED_DEVBUS.getName(), "EmbeddedHandlePopulator");
   public static final int CD1 = 1;
   public static final int CD2 = 2;
   public static final int CD_1_PRESENT_BIT_POSITION = 6;
   public static final int CD_2_PRESENT_BIT_POSITION = 4;
   public static final String EMBEDDED_HANDLE_POPULATOR_NAME_STRING = "Embedded Handle Populator";
   public static final String ERROR_MSG_1 = "Error while getting number of Drawers";

   public EmbeddedHandlePopulator(HandleRegistry registry, HandleFactory factory, PosSystem.EventHelper eventHelper) {
      super(registry, factory, eventHelper);
   }

   public void start() throws PosException {
      if (!this.isStarted()) {
         super.start();
         this.init();
      }
   }

   public void stop() throws PosException {
      if (this.isStarted()) {
         super.stop();
      }
   }

   public String getName() {
      return "Embedded Handle Populator";
   }

   public DevBus getDevBus() {
      return DevBuses.EMBEDDED_DEVBUS;
   }

   protected void init() {
      if (this.tracer.isOn()) {
         this.trace("-->init()");
      }

      Iterator iterator = this.getDrivers();
      EmbeddedDriver embeddedDriver = null;
      if (this.tracer.isOn() && !iterator.hasNext()) {
         this.trace("None EmbeddedDrivers found");
      }

      while (iterator.hasNext()) {
         this.clear();
         embeddedDriver = (EmbeddedDriver)iterator.next();
         embeddedDriver.accept(this.getDriverVisitor());
         if (null == this.getException()) {
            for (int i = 0; i < this.deviceHandleList.size(); i++) {
               if (this.tracer.isOn()) {
                  this.trace(((HandleImp)this.deviceHandleList.get(i)).getHandle().getName() + " Handle Created.");
               }

               this.getHandleRegistry().addHandle(((HandleImp)this.deviceHandleList.get(i)).getHandle());
            }
         } else if (this.tracer.isOn()) {
            this.trace(" An error occurred when creating Handle -> " + this.getException().toString());
            this.tracer.print(this.getException());
         }
      }

      if (this.tracer.isOn()) {
         this.trace("<--init()");
      }
   }

   protected Iterator getDrivers() {
      Iterator iterator = new ArrayList().iterator();

      try {
         iterator = EmbeddedManager.getInstance().getDrivers();
      } catch (Throwable var3) {
         if (this.tracer.isOn()) {
            this.trace("-->getDrivers():Error loading JNI drivers: " + var3.toString() + "<--");
         }
      }

      return iterator;
   }

   protected EmbeddedHandleImpFactory getEmbeddedHandleImpFactory() {
      if (this.hiFactory == null) {
         this.hiFactory = new EmbeddedHandleImpFactory(this.getHandleFactory());
      }

      return this.hiFactory;
   }

   protected EmbeddedDriverVisitor getDriverVisitor() {
      if (this.visitor == null) {
         this.visitor = new EmbeddedHandlePopulator.EmbeddedDriverV();
      }

      return this.visitor;
   }

   protected void visitCashDrawer(CashDrawerEmbeddedDriver driver) {
      if (this.tracer.isOn()) {
         this.trace("-->visitCashDrawer()");
      }

      int numberOfDrawers = 0;

      try {
         numberOfDrawers = driver.getNumberOfDrawers();
      } catch (EmbeddedException var5) {
         PosSystemManager.getInstance().getLogHelper().addLogEntry(1000, "Error while getting number of Drawers", "CashDrawer", 4);
      }

      if (numberOfDrawers > 0) {
         if (this.tracer.isOn()) {
            this.trace("number of drawers attached = " + numberOfDrawers);
         }

         try {
            if (this.isCDPresent(driver, 1)) {
               if (this.tracer.isOn()) {
                  this.trace("Adding CD1 to deviceHandleList");
               }

               this.deviceHandleList.add(this.getEmbeddedHandleImpFactory().createCashDrawer(driver, 1));
            }

            if (this.isCDPresent(driver, 2)) {
               if (this.tracer.isOn()) {
                  this.trace("Adding CD2 to deviceHandleList");
               }

               this.deviceHandleList.add(this.getEmbeddedHandleImpFactory().createCashDrawer(driver, 2));
            }
         } catch (HandleFactoryException var4) {
            this.hfE = var4;
         }

         if (this.tracer.isOn()) {
            this.trace("deviceHandleList size = " + this.deviceHandleList.size());
         }
      }

      if (this.tracer.isOn()) {
         this.trace("<--visitCashDrawer()");
      }
   }

   public void visitKeylock(KeylockEmbeddedDriver driver) {
      try {
         this.deviceHandleList.add(this.getEmbeddedHandleImpFactory().createKeylock(driver));
      } catch (HandleFactoryException var3) {
         this.hfE = var3;
      }
   }

   protected void visitHardTotals(HardTotalsEmbeddedDriver driver) {
      try {
         this.deviceHandleList.add(this.getEmbeddedHandleImpFactory().createHardTotals(driver));
      } catch (HandleFactoryException var3) {
         this.hfE = var3;
      }
   }

   public void visitMotionSensor(MotionSensorEmbeddedDriver driver) {
      try {
         this.deviceHandleList.add(this.getEmbeddedHandleImpFactory().createMotionSensor(driver));
      } catch (HandleFactoryException var3) {
         this.hfE = var3;
      }
   }

   protected void clear() {
      this.hfE = null;
      this.deviceHandleList.clear();
   }

   protected HandleFactoryException getException() {
      return this.hfE;
   }

   private boolean isCDPresent(EmbeddedDriver driver, int cdNumber) throws HandleFactoryException {
      boolean cdPresent = false;
      int presenceBitPosition = cdNumber == 1 ? 6 : 4;

      try {
         byte status = 0;
         CashDrawerEmbeddedDriver cdDriver = (CashDrawerEmbeddedDriver)driver;
         status = cdDriver.getCDStatus();
         if (this.tracer.isOn()) {
            this.trace("after send request for status.. status is = 0x" + Util.toHexString(status));
         }

         if (status == 0) {
            throw new HandleFactoryException("No Cash Drawer Hardware present");
         }

         if (!PosjUtil.isBitSelected(status, presenceBitPosition)) {
            cdPresent = true;
         }
      } catch (EmbeddedException var7) {
         throw new HandleFactoryException("Error while verifying Cash Drawer presence" + var7);
      }

      if (this.tracer.isOn()) {
         this.trace("Cash drawer " + cdNumber + " present ? " + cdPresent);
      }

      return cdPresent;
   }

   private void trace(String msg) {
      this.tracer.println(2, msg);
   }

   class EmbeddedDriverV implements EmbeddedDriverVisitor {
      public void visitCashDrawer(CashDrawerEmbeddedDriver driver) {
         EmbeddedHandlePopulator.this.visitCashDrawer(driver);
      }

      public void visitHardTotals(HardTotalsEmbeddedDriver driver) {
         EmbeddedHandlePopulator.this.visitHardTotals(driver);
      }

      public void visitKeylock(KeylockEmbeddedDriver driver) {
         EmbeddedHandlePopulator.this.visitKeylock(driver);
      }

      public void visitMotionSensor(MotionSensorEmbeddedDriver driver) {
         EmbeddedHandlePopulator.this.visitMotionSensor(driver);
      }
   }
}
