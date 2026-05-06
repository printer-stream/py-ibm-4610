package com.ibm.posj;

import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

class DefaultMSRHandle extends AbstractHandle implements MSRHandle {
   private static int count = 0;
   private MSRDevInfoConst msrDevInfo = new MSRDevInfoConst();
   private static MSRCmd.Factory factory = new DefaultMSRCmd.Factory();
   private static SystemCmd.Factory systemfactory = new DefaultSystemCmd.Factory();

   DefaultMSRHandle() {
      this.handleCount = count++;
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getMSRCmdFactory();
   }

   public MSRCmd.Factory getMSRCmdFactory() {
      return factory;
   }

   public DevCat getDevCat() {
      return DevCats.MSR_DEVCAT;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitMSR(this);
   }

   public void reset() {
      try {
         this.configureMSR();
      } catch (HandleException var2) {
         this.getLogHelper().addLogEntry(1003, "Error while configuring MSR after reconnect", "AllDevices", 4);
      }
   }

   public void init() throws HandleException {
      super.init();
      this.configureMSR();
   }

   protected void configureMSR() throws HandleException {
      SystemCmd.DeviceInfoRequestCmd devInfoCmd = this.getSystemCmdFactory().createDeviceInfoRequestCmd();
      this.submit(devInfoCmd);
      int timeout = 0;
      if (!this.getDevInfoConst().isJUCCMSRType(devInfoCmd.getDeviceId()) && !this.getDevInfoConst().isWriteCapableMSRType(devInfoCmd.getDeviceId())) {
         this.submit(this.getMSRCmdFactory().createConfigMSRCmd(true, true, true, false, timeout));
      } else {
         this.submit(this.getMSRCmdFactory().createConfigMSRCmd(false, true, false, true, timeout));
      }
   }

   private MSRDevInfoConst getDevInfoConst() {
      return this.msrDevInfo;
   }
}
