package com.ibm.posj;

import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

class DefaultToneIndicatorHandle extends AbstractHandle implements ToneIndicatorHandle {
   private static int count = 0;
   private static ToneIndicatorCmd.Factory factory = new DefaultToneIndicatorCmd.Factory();

   DefaultToneIndicatorHandle() {
      this.handleCount = count++;
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getToneIndicatorCmdFactory();
   }

   public ToneIndicatorCmd.Factory getToneIndicatorCmdFactory() {
      return factory;
   }

   public DevCat getDevCat() {
      return DevCats.TONEINDICATOR_DEVCAT;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitToneIndicator(this);
   }
}
