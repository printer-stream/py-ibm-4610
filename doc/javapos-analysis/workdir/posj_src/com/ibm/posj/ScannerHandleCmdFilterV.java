package com.ibm.posj;

import com.ibm.posj.util.DefaultHandleCmdV;

public class ScannerHandleCmdFilterV extends DefaultHandleCmdV {
   private boolean isScannerCmd = false;
   private boolean isSystemCmd = false;
   private boolean isInvalidCmd = false;

   public boolean isSystemCmd() {
      return this.isSystemCmd;
   }

   public boolean isScannerCmd() {
      return this.isScannerCmd;
   }

   public boolean isInvalidCmd() {
      return this.isInvalidCmd;
   }

   public void reset() {
      this.isInvalidCmd = false;
      this.isSystemCmd = false;
      this.isScannerCmd = false;
   }

   protected void visitHandleCmd(HandleCmd cmd) {
      this.isInvalidCmd = true;
   }

   public void visitSystemCmd(SystemCmd cmd) {
      this.isSystemCmd = true;
   }

   public void visitScannerCmd(ScannerCmd cmd) {
      this.isScannerCmd = true;
   }
}
