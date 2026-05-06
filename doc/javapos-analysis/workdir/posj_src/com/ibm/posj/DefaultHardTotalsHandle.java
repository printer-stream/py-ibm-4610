package com.ibm.posj;

import com.ibm.posj.bus.HardTotalsHandleImp;
import com.ibm.posj.ram.RamDevice;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

class DefaultHardTotalsHandle extends AbstractHandle implements HardTotalsHandle {
   private static int count = 0;

   DefaultHardTotalsHandle() {
      this.handleCount = count++;
   }

   public DevCat getDevCat() {
      return DevCats.HARDTOTALS_DEVCAT;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitHardTotals(this);
   }

   public HardTotalsCmd.Factory getCmdFactory() {
      return ((HardTotalsHandleImp)this.getHandleImp()).getCmdFactory();
   }

   public RamDevice getRamDevice() {
      return ((HardTotalsHandleImp)this.getHandleImp()).getRamDevice();
   }
}
