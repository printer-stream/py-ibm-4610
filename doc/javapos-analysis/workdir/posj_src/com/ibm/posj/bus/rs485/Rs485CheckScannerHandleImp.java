package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.CheckScannerHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public abstract class Rs485CheckScannerHandleImp extends AbstractRs485HandleImp implements CheckScannerHandleImp {
   public Rs485CheckScannerHandleImp(HandleKey key, SioDevice device) {
      super(key, device);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitCheckScanner(this);
   }

   public DevCat getDevCat() {
      return DevCats.CHECKSCANNER_DEVCAT;
   }
}
