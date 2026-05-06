package com.ibm.posj.bus.rs232;

import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.CashDrawerHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.printer.IBMPrinterCashDrawerLinker;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.rs232.Rs232Port;

public class Rs232POSPrinterCashDrawerHandleImp extends AbstractRs232HandleImp implements CashDrawerHandleImp {
   private IBMPrinterCashDrawerLinker cdlinker;

   public Rs232POSPrinterCashDrawerHandleImp(HandleKey key, Rs232Port rs232Port, IBMPrinterCashDrawerLinker cdLinker) {
      super(key, rs232Port);
      this.cdlinker = cdLinker;
   }

   public void init() throws HandleException {
      this.cdlinker.init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.cdlinker.submit(cmd);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitCashDrawer(this);
   }

   public DevCat getDevCat() {
      return DevCats.CASHDRAWER_DEVCAT;
   }

   public short getECLevel() {
      return -1;
   }

   public boolean isFlashable() {
      return false;
   }

   public boolean isSubDevice() {
      return true;
   }
}
