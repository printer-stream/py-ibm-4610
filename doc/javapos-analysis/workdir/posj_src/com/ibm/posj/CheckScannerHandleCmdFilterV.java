package com.ibm.posj;

import com.ibm.posj.util.DefaultHandleCmdV;

public class CheckScannerHandleCmdFilterV extends DefaultHandleCmdV {
   private boolean isCheckScannerCmd = false;
   private boolean isSystemCmd = false;
   private boolean isInvalidCmd = false;

   public boolean isSystemCmd() {
      return this.isSystemCmd;
   }

   public boolean isCheckScannerCmd() {
      return this.isCheckScannerCmd;
   }

   public boolean isInvalidCmd() {
      return this.isInvalidCmd;
   }

   public void reset() {
      this.isInvalidCmd = false;
      this.isSystemCmd = false;
      this.isCheckScannerCmd = false;
   }

   protected void visitHandleCmd(HandleCmd cmd) {
      this.isInvalidCmd = true;
   }

   public void visitSystemCmd(SystemCmd cmd) {
      this.isSystemCmd = true;
   }

   public void visitCheckScannerCmd(CheckScannerCmd cmd) {
      this.isCheckScannerCmd = true;
   }
}
