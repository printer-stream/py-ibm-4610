package com.ibm.posj.bus.rs232;

import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.printer.IBMPrinterCheckScannerLinker;
import com.ibm.rs232.Rs232Port;

public class Rs232POSPrinterCheckScannerHandleImp extends Rs232CheckScannerHandleImp {
   private IBMPrinterCheckScannerLinker csLinker;
   private boolean lastMulti = false;

   public Rs232POSPrinterCheckScannerHandleImp(HandleKey key, Rs232Port rs232Port, IBMPrinterCheckScannerLinker csLinker) {
      super(key, rs232Port);
      this.csLinker = csLinker;
   }

   public void init() throws HandleException {
      this.csLinker.init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.csLinker.submit(cmd);
   }

   public short getRetrieveMaxSize() {
      return 1024;
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
