package com.ibm.posj;

import com.ibm.posj.util.DefaultHandleCmdV;

public class MICRHandleCmdFilterV extends DefaultHandleCmdV {
   private boolean isMICRCmd = false;
   private boolean isSystemCmd = false;
   private boolean isInvalidCmd = false;

   public boolean isSystemCmd() {
      return this.isSystemCmd;
   }

   public boolean isMICRCmd() {
      return this.isMICRCmd;
   }

   public boolean isInvalidCmd() {
      return this.isInvalidCmd;
   }

   public void reset() {
      this.isInvalidCmd = false;
      this.isSystemCmd = false;
      this.isMICRCmd = false;
   }

   protected void visitHandleCmd(HandleCmd cmd) {
      this.isInvalidCmd = true;
   }

   public void visitSystemCmd(SystemCmd cmd) {
      this.isSystemCmd = true;
   }

   public void visitMICRCmd(MICRCmd cmd) {
      this.isMICRCmd = true;
   }
}
