package com.ibm.posj.bus.rs232;

import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.ScannerHandleImp;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.rs232.Rs232Port;

public class Rs232ScannerHandleImp extends AbstractRs232HandleImp implements ScannerHandleImp {
   public Rs232ScannerHandleImp(HandleKey key, Rs232Port rs232Port) {
      super(key, rs232Port);
   }

   public boolean isFlashable() {
      return false;
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitScanner(this);
   }

   public DevCat getDevCat() {
      return DevCats.SCANNER_DEVCAT;
   }

   public short getECLevel() {
      return -1;
   }
}
