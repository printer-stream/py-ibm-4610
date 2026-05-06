package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.ReportEvent;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.printer.IBMPrinterCheckScannerLinker;

public class HidPOSPrinterCheckScannerHandleImp extends HidCheckScannerHandleImp {
   private IBMPrinterCheckScannerLinker csLinker;
   private boolean lastMulti = false;

   public HidPOSPrinterCheckScannerHandleImp(HandleKey key, HidDevice hidDevice, IBMPrinterCheckScannerLinker csLinker) {
      super(key, hidDevice);
      this.csLinker = csLinker;
   }

   public void init() throws HandleException {
      this.csLinker.init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (this.isLocked.isTrue()) {
         throw new HandleException("Attempting to submit to 4610CheckScanner while flashing");
      } else {
         this.csLinker.submit(cmd);
      }
   }

   public short getRetrieveMaxSize() {
      return 240;
   }

   public boolean isComposite() {
      return true;
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

   protected void reportEventOccurred(ReportEvent rE) {
   }
}
