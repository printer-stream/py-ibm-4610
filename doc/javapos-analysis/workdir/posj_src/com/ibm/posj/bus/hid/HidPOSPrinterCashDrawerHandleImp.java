package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.ReportEvent;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.CashDrawerHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.printer.IBMPrinterCashDrawerLinker;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class HidPOSPrinterCashDrawerHandleImp extends AbstractHidHandleImp implements HidHandleImp, CashDrawerHandleImp {
   private IBMPrinterCashDrawerLinker cdlinker;

   public HidPOSPrinterCashDrawerHandleImp(HandleKey key, HidDevice hidDevice, IBMPrinterCashDrawerLinker cdLinker) {
      super(key, hidDevice);
      this.cdlinker = cdLinker;
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitCashDrawer(this);
   }

   public DevCat getDevCat() {
      return DevCats.CASHDRAWER_DEVCAT;
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidCashDrawerHandleImp(this);
   }

   public void init() throws HandleException {
      this.cdlinker.init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (this.isLocked.isTrue()) {
         throw new HandleException("Attempting to submit to 4610CashDrawer while flashing");
      } else {
         this.cdlinker.submit(cmd);
      }
   }

   public boolean isComposite() {
      return true;
   }

   public boolean isSubDevice() {
      return true;
   }

   protected void reportEventOccurred(ReportEvent rE) {
   }
}
