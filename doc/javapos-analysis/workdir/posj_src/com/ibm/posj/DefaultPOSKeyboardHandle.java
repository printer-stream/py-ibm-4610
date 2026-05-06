package com.ibm.posj;

import com.ibm.posj.bus.POSKeyboardHandleImp;
import com.ibm.posj.kbd.DefaultPOSIndicator;
import com.ibm.posj.kbd.KbdMapping;
import com.ibm.posj.kbd.MappingManager;
import com.ibm.posj.kbd.POSIndicator;
import com.ibm.posj.kbd.POSKeyboardMap;
import com.ibm.posj.kbd.PS2Indicator;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

class DefaultPOSKeyboardHandle extends AbstractHandle implements POSKeyboardHandle {
   private static int count = 0;
   private static POSKeyboardCmd.Factory factory = new DefaultPOSKeyboardCmd.Factory();
   protected POSIndicator indicatorSettings = new DefaultPOSIndicator();
   protected KbdMapping kbdMapping = null;
   protected POSKeyboardMap userMapping = null;

   DefaultPOSKeyboardHandle() {
      this.handleCount = count++;
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getPOSKeyboardCmdFactory();
   }

   public POSKeyboardCmd.Factory getPOSKeyboardCmdFactory() {
      return factory;
   }

   public DevCat getDevCat() {
      return DevCats.POSKEYBOARD_DEVCAT;
   }

   public SystemCmd.Factory getSystemCmdFactory() {
      return this.getPOSKeyboardCmdFactory();
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitPOSKeyboard(this);
   }

   public synchronized void setPOSIndicatorState(POSIndicator indicator) {
      this.indicatorSettings.setWait(indicator.getWait());
      this.indicatorSettings.setOffline(indicator.getOffline());
      this.indicatorSettings.setMsgPendOrSysMsg(indicator.getMsgPendOrSysMsg());
      this.indicatorSettings.setBlankOrReady(indicator.getBlankOrReady());
   }

   public synchronized POSIndicator getPOSIndicatorState() {
      return this.indicatorSettings;
   }

   public PS2Indicator getPS2IndicatorState() {
      return ((POSKeyboardHandleImp)this.getHandleImp()).getPS2IndicatorState();
   }

   public KbdMapping createKbdMapping() throws HandleException {
      if (this.kbdMapping == null) {
         this.kbdMapping = MappingManager.getInstance().createKbdMapping(this);
      }

      return this.kbdMapping;
   }

   public POSKeyboardMap createUserMapping(String filename) throws HandleException {
      if (this.userMapping == null) {
         this.userMapping = MappingManager.getInstance().createUserMapping(this, filename);
      }

      return this.userMapping;
   }

   public void reset() {
      this.kbdMapping = null;
      this.userMapping = null;
   }
}
