package com.ibm.posj;

import com.ibm.posj.bus.FiscalPrinterHandleImp;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class DefaultFiscalPrinterHandle extends AbstractHandle implements FiscalPrinterHandle {
   public static final int POWER_INTERRUPTED = 1;
   public static final int STATUS_IPL_END = 2;
   private static int count = 0;
   private static FiscalPrinterCmd.Factory factory = new DefaultFiscalPrinterCmd.Factory();

   DefaultFiscalPrinterHandle() {
      this.handleCount = count++;
   }

   public boolean isInit() throws HandleException {
      return this.isInitialized();
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getFiscalPrinterCmdFactory();
   }

   public SystemCmd.Factory getSystemCmdFactory() {
      return this.getFiscalPrinterCmdFactory();
   }

   public FiscalPrinterCmd.Factory getFiscalPrinterCmdFactory() {
      return factory;
   }

   public DevCat getDevCat() {
      return DevCats.FISCALPRINTER_DEVCAT;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitFiscalPrinter(this);
   }

   public FiscalPrinterInfoHelper getInfoHelper() {
      return ((FiscalPrinterHandleImp)this.getHandleImp()).getInfoHelper();
   }

   public void startProtocol() {
      ((FiscalPrinterHandleImp)this.getHandleImp()).startProtocol();
   }

   public void stopProtocol() {
      ((FiscalPrinterHandleImp)this.getHandleImp()).stopProtocol();
   }
}
