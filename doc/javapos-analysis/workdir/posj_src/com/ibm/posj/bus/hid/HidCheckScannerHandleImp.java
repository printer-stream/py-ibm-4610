package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.CheckScannerHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public abstract class HidCheckScannerHandleImp extends AbstractHidHandleImp implements HidHandleImp, CheckScannerHandleImp {
   public HidCheckScannerHandleImp(HandleKey key, HidDevice hidDevice) {
      super(key, hidDevice);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitCheckScanner(this);
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidCheckScannerHandleImp(this);
   }

   public DevCat getDevCat() {
      return DevCats.CHECKSCANNER_DEVCAT;
   }
}
