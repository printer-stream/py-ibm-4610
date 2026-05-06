package com.ibm.posj;

import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

class DefaultCashDrawerHandle extends AbstractHandle implements CashDrawerHandle {
   private static int count = 0;
   private static CashDrawerCmd.Factory factory = new DefaultCashDrawerCmd.Factory();

   DefaultCashDrawerHandle() {
      this.handleCount = count++;
   }

   public boolean isInit() throws HandleException {
      return this.isInitialized();
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getCashDrawerCmdFactory();
   }

   public SystemCmd.Factory getSystemCmdFactory() {
      return this.getCashDrawerCmdFactory();
   }

   public CashDrawerCmd.Factory getCashDrawerCmdFactory() {
      return factory;
   }

   public DevCat getDevCat() {
      return DevCats.CASHDRAWER_DEVCAT;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitCashDrawer(this);
   }
}
