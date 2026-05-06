package com.ibm.posj;

import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

class DefaultLineDisplayHandle extends AbstractHandle implements LineDisplayHandle {
   private static int count = 0;
   private static LineDisplayCmd.Factory factory = new DefaultLineDisplayCmd.Factory();

   DefaultLineDisplayHandle() {
      this.handleCount = count++;
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getLineDisplayCmdFactory();
   }

   public SystemCmd.Factory getSystemCmdFactory() {
      return this.getLineDisplayCmdFactory();
   }

   public LineDisplayCmd.Factory getLineDisplayCmdFactory() {
      return factory;
   }

   public DevCat getDevCat() {
      return DevCats.LINEDISPLAY_DEVCAT;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitLineDisplay(this);
   }
}
