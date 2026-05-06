package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.printer.IBMPrinterMICRLinker;

public class HidPOSPrinterMICRHandleImp extends HidMICRHandleImp {
   private IBMPrinterMICRLinker micrLinker;

   public HidPOSPrinterMICRHandleImp(HandleKey key, HidDevice hidDevice, IBMPrinterMICRLinker micrLinker) {
      super(key, hidDevice);
      this.micrLinker = micrLinker;
   }

   public void init() throws HandleException {
      this.micrLinker.init();
   }

   public void submit(HandleCmd cmd) throws HandleException {
      this.micrLinker.submit(cmd);
   }
}
