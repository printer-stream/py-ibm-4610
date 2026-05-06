package com.ibm.posj.bus.rs232;

import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.CheckScannerHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.rs232.Rs232Port;

public abstract class Rs232CheckScannerHandleImp extends AbstractRs232HandleImp implements CheckScannerHandleImp {
   public Rs232CheckScannerHandleImp(HandleKey key, Rs232Port rs232Port) {
      super(key, rs232Port);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitCheckScanner(this);
   }

   public DevCat getDevCat() {
      return DevCats.CHECKSCANNER_DEVCAT;
   }

   public short getECLevel() {
      return -1;
   }

   public boolean isFlashable() {
      return false;
   }
}
