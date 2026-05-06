package com.ibm.posj;

import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

class DefaultMotionSensorHandle extends AbstractHandle implements MotionSensorHandle {
   private static int count = 0;
   private static MotionSensorCmd.Factory factory = new DefaultMotionSensorCmd.Factory();

   DefaultMotionSensorHandle() {
      this.handleCount = count++;
   }

   public boolean isInit() throws HandleException {
      return this.isInitialized();
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getMotionSensorCmdFactory();
   }

   public SystemCmd.Factory getSystemCmdFactory() {
      return this.getMotionSensorCmdFactory();
   }

   public MotionSensorCmd.Factory getMotionSensorCmdFactory() {
      return factory;
   }

   public DevCat getDevCat() {
      return DevCats.MOTIONSENSOR_DEVCAT;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitMotionSensor(this);
   }
}
