package com.ibm.posj.bus.embedded;

import com.ibm.embedded.CashDrawerEmbeddedDriver;
import com.ibm.embedded.HardTotalsEmbeddedDriver;
import com.ibm.embedded.KeylockEmbeddedDriver;
import com.ibm.embedded.MotionSensorEmbeddedDriver;
import com.ibm.posj.HandleFactory;
import com.ibm.posj.HandleFactoryException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.CashDrawerHandleImp;
import com.ibm.posj.bus.HardTotalsHandleImp;
import com.ibm.posj.bus.KeylockHandleImp;
import com.ibm.posj.bus.MotionSensorHandleImp;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import java.util.Hashtable;

class EmbeddedHandleImpFactory {
   private HandleFactory handleFactory = null;
   private Hashtable numberTable = new Hashtable();

   public EmbeddedHandleImpFactory(HandleFactory factory) {
      this.handleFactory = factory;
   }

   public CashDrawerHandleImp createCashDrawer(CashDrawerEmbeddedDriver driver, int cdNum) throws HandleFactoryException {
      DevCat devCat = DevCats.CASHDRAWER_DEVCAT;
      int cdDeviceNumber = 0;
      byte var7;
      if (cdNum == 1) {
         var7 = 0;
      } else {
         if (cdNum != 2) {
            throw new HandleFactoryException("CashDrawers in port different from 3A and 3B not supported");
         }

         var7 = 1;
      }

      HandleKey hk = new EmbeddedHandleKey(devCat, var7);
      EmbeddedCashDrawerHandleImp hi = new EmbeddedCashDrawerHandleImp(hk, driver, cdNum);
      hi.setHandle(this.handleFactory.createCashDrawerHandle(hi));
      return hi;
   }

   public HardTotalsHandleImp createHardTotals(HardTotalsEmbeddedDriver driver) throws HandleFactoryException {
      DevCat devCat = DevCats.HARDTOTALS_DEVCAT;
      HandleKey hk = new EmbeddedHandleKey(devCat, this.getDeviceNumber(devCat));
      EmbeddedHardTotalsHandleImp hi = new EmbeddedHardTotalsHandleImp(hk, driver);
      hi.setHandle(this.handleFactory.createHardTotalsHandle(hi));
      return hi;
   }

   public KeylockHandleImp createKeylock(KeylockEmbeddedDriver driver) throws HandleFactoryException {
      DevCat devCat = DevCats.KEYLOCK_DEVCAT;
      HandleKey hk = new EmbeddedHandleKey(devCat, this.getDeviceNumber(devCat));
      EmbeddedKeylockHandleImp hi = new EmbeddedKeylockHandleImp(hk, driver);
      hi.setHandle(this.handleFactory.createKeylockHandle(hi));
      return hi;
   }

   public MotionSensorHandleImp createMotionSensor(MotionSensorEmbeddedDriver driver) throws HandleFactoryException {
      DevCat devCat = DevCats.MOTIONSENSOR_DEVCAT;
      HandleKey hk = new EmbeddedHandleKey(devCat, this.getDeviceNumber(devCat));
      EmbeddedMotionSensorHandleImp hi = new EmbeddedMotionSensorHandleImp(hk, driver);
      hi.setHandle(this.handleFactory.createMotionSensorHandle(hi));
      return hi;
   }

   protected int getDeviceNumber(DevCat devCat) {
      String key = devCat.toString();
      int number = 0;
      if (this.numberTable.containsKey(key)) {
         number = (Integer)this.numberTable.get(key);
         number++;
      }

      this.numberTable.put(key, new Integer(number));
      return number;
   }
}
