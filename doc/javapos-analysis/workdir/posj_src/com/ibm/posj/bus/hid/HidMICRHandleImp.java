package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.ReportEvent;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.MICRHandleImp;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

class HidMICRHandleImp extends AbstractHidHandleImp implements HidHandleImp, MICRHandleImp {
   public HidMICRHandleImp(HandleKey key, HidDevice hidDevice) {
      super(key, hidDevice);
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (this.isLocked.isTrue()) {
         throw new HandleException("Attempting to submit to MICR while flashing");
      } else {
         throw new HandleException("UsbMICR submission not yet implemented");
      }
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidMICRHandleImp(this);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitMICR(this);
   }

   public DevCat getDevCat() {
      return DevCats.MICR_DEVCAT;
   }

   public boolean isComposite() {
      return true;
   }

   protected void reportEventOccurred(ReportEvent rE) {
   }
}
