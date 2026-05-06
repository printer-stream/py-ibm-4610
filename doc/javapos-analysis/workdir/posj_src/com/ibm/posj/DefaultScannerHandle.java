package com.ibm.posj;

import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

class DefaultScannerHandle extends AbstractHandle implements ScannerHandle {
   private static int count = 0;
   private static ScannerCmd.Factory factory = new DefaultScannerCmd.Factory();

   DefaultScannerHandle() {
      this.handleCount = count++;
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getScannerCmdFactory();
   }

   public SystemCmd.Factory getSystemCmdFactory() {
      return this.getScannerCmdFactory();
   }

   public ScannerCmd.Factory getScannerCmdFactory() {
      return factory;
   }

   public DevCat getDevCat() {
      return DevCats.SCANNER_DEVCAT;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitScanner(this);
   }
}
