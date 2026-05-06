package com.ibm.posj;

import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

class DefaultKeylockHandle extends AbstractHandle implements KeylockHandle {
   private static int count = 0;
   private static KeylockCmd.Factory factory = new DefaultKeylockCmd.Factory();

   DefaultKeylockHandle() {
      this.handleCount = count++;
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getKeylockCmdFactory();
   }

   public KeylockCmd.Factory getKeylockCmdFactory() {
      return factory;
   }

   public SystemCmd.Factory getSystemCmdFactory() {
      return (SystemCmd.Factory)this.getKeylockCmdFactory();
   }

   public DevCat getDevCat() {
      return DevCats.KEYLOCK_DEVCAT;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitKeylock(this);
   }
}
