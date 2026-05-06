package com.ibm.posj;

import com.ibm.posj.bus.printer.cmds.MICRCmdFactory;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

class DefaultMICRHandle extends AbstractHandle implements MICRHandle {
   private Object queue;
   MICRCmdFactory factories;
   private static int count = 0;
   private static DefaultMICRCmd.Factory factory = new DefaultMICRCmd.Factory();

   DefaultMICRHandle() {
      this.handleCount = count++;
   }

   public void submit(DefaultMICRCmd cmd) throws HandleException {
      this.getHandleImp().submit(cmd);
   }

   public DevCat getDevCat() {
      return DevCats.MICR_DEVCAT;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitMICR(this);
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getMICRCmdFactory();
   }

   public SystemCmd.Factory getSystemCmdFactory() {
      return (SystemCmd.Factory)this.getMICRCmdFactory();
   }

   public MICRCmd.Factory getMICRCmdFactory() {
      return factory;
   }
}
