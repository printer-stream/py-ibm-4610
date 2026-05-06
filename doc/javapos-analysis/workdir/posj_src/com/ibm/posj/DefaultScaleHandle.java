package com.ibm.posj;

import com.ibm.posj.bus.ScaleHandleImp;
import com.ibm.posj.scale.ScaleHandleState;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class DefaultScaleHandle extends AbstractHandle implements ScaleHandle {
   private static int count = 0;
   private static ScaleCmd.Factory factory = new DefaultScaleCmd.Factory();

   DefaultScaleHandle() {
      this.handleCount = count++;
   }

   public boolean isInit() throws HandleException {
      return this.isInitialized();
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getScaleCmdFactory();
   }

   public SystemCmd.Factory getSystemCmdFactory() {
      return this.getScaleCmdFactory();
   }

   public ScaleCmd.Factory getScaleCmdFactory() {
      return factory;
   }

   public DevCat getDevCat() {
      return DevCats.SCALE_DEVCAT;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitScale(this);
   }

   public ScaleHandleState getHandleState() {
      return ((ScaleHandleImp)this.getHandleImp()).getHandleState();
   }
}
