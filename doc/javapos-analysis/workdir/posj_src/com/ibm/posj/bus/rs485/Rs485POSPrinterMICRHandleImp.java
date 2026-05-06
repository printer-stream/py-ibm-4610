package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.printer.IBMPrinterMICRLinker;

public class Rs485POSPrinterMICRHandleImp extends Rs485MICRHandleImp {
   private IBMPrinterMICRLinker mLinker;

   public Rs485POSPrinterMICRHandleImp(HandleKey key, SioDevice device, IBMPrinterMICRLinker linker) {
      super(key, device);
      this.mLinker = linker;
   }

   public void init() throws HandleException {
      this.mLinker.init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.mLinker.submit(cmd);
   }
}
