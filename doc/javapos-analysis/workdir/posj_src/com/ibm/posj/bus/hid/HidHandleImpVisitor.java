package com.ibm.posj.bus.hid;

public interface HidHandleImpVisitor {
   void visitHidPOSPrinterToneIndicatorHandleImp(HidHandleImp var1);

   void visitHid4610PrinterHandleImp(Hid4610PrinterHandleImp var1);

   void visitHid4689PrinterHandleImp(Hid4689PrinterHandleImp var1);

   void visitHidCashDrawerHandleImp(HidHandleImp var1);

   void visitHidCheckScannerHandleImp(HidHandleImp var1);

   void visitHidHardTotalsHandleImp(HidHardTotalsHandleImp var1);

   void visitHidKeylockHandleImp(HidKeylockHandleImp var1);

   void visitHidLineDisplayHandleImp(HidLineDisplayHandleImp var1);

   void visitHidMICRHandleImp(HidMICRHandleImp var1);

   void visitHidMSRHandleImp(HidMSRHandleImp var1);

   void visitHidPOSKeyboardHandleImp(HidPOSKeyboardHandleImp var1);

   void visitHidPOSPrinterHandleImp(HidPOSPrinterHandleImp var1);

   void visitHidScaleHandleImp(HidScaleHandleImp var1);

   void visitHidScannerHandleImp(HidScannerHandleImp var1);

   void visitHidToneIndicatorHandleImp(HidToneIndicatorHandleImp var1);

   void visitHidFlashHandleImp(HidFlashHandleImp var1);

   void visitHidFiscalPrinterImp(HidFiscalPrinterHandleImp var1);
}
