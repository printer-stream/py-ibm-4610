package com.ibm.posj.printer;

import com.ibm.posj.util.DefaultPOSPrinterCmdV;

public abstract class AbstractCmdCompleteVisitor extends DefaultPOSPrinterCmdV {
   private boolean changed = false;

   public boolean stateChanged() {
      return this.changed;
   }

   public void reset() {
      this.changed = false;
   }

   protected void setStateChanged(boolean c) {
      this.changed = c;
   }
}
