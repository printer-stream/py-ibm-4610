package com.ibm.posj.bus.rs232;

import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.printer.IBMPrinterMICRLinker;
import com.ibm.rs232.Rs232Port;

public class Rs232POSPrinterMICRHandleImp extends Rs232MICRHandleImp {
   private IBMPrinterMICRLinker micrLinker;

   public Rs232POSPrinterMICRHandleImp(HandleKey key, Rs232Port rs232Port, IBMPrinterMICRLinker micrLinker) {
      super(key, rs232Port);
      this.micrLinker = micrLinker;
   }

   public void init() throws HandleException {
      this.micrLinker.init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.micrLinker.submit(cmd);
   }
}
