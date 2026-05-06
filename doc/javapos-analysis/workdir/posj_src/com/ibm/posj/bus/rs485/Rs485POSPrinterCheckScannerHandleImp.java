package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.printer.IBMPrinterCheckScannerLinker;

public class Rs485POSPrinterCheckScannerHandleImp extends Rs485CheckScannerHandleImp {
   private IBMPrinterCheckScannerLinker csLinker;
   private boolean lastMulti = false;

   public Rs485POSPrinterCheckScannerHandleImp(HandleKey key, SioDevice device, IBMPrinterCheckScannerLinker linker) {
      super(key, device);
      this.csLinker = linker;
   }

   public void init() throws HandleException {
      this.csLinker.init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.csLinker.submit(cmd);
   }

   public short getRetrieveMaxSize() {
      return 1000;
   }

   public boolean setAsLast(boolean l) {
      return this.lastMulti = l;
   }

   public boolean isLast() {
      return this.lastMulti;
   }

   public byte getPrintSide() {
      return this.csLinker.getPrintSide();
   }
}
