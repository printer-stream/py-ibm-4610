package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.CashDrawerHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.printer.IBMPrinterCashDrawerLinker;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class Rs485POSPrinterCashDrawerHandleImp extends AbstractRs485HandleImp implements CashDrawerHandleImp {
   private IBMPrinterCashDrawerLinker cdLinker;

   public Rs485POSPrinterCashDrawerHandleImp(HandleKey key, SioDevice device, IBMPrinterCashDrawerLinker linker) {
      super(key, device);
      this.cdLinker = linker;
   }

   public void init() throws HandleException {
      this.cdLinker.init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.cdLinker.submit(cmd);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitCashDrawer(this);
   }

   public boolean isSubDevice() {
      return true;
   }

   public DevCat getDevCat() {
      return DevCats.CASHDRAWER_DEVCAT;
   }
}
