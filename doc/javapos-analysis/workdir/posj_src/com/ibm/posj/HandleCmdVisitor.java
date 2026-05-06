package com.ibm.posj;

public interface HandleCmdVisitor {
   void visitSystemCmd(SystemCmd var1);

   void visitCashDrawerCmd(CashDrawerCmd var1);

   void visitCheckScannerCmd(CheckScannerCmd var1);

   void visitFiscalPrinterCmd(FiscalPrinterCmd var1);

   void visitHardTotalsCmd(HardTotalsCmd var1);

   void visitKeylockCmd(KeylockCmd var1);

   void visitLineDisplayCmd(LineDisplayCmd var1);

   void visitMICRCmd(MICRCmd var1);

   void visitMSRCmd(MSRCmd var1);

   void visitMotionSensorCmd(MotionSensorCmd var1);

   void visitPOSKeyboardCmd(POSKeyboardCmd var1);

   void visitPOSPrinterCmd(POSPrinterCmd var1);

   void visitScaleCmd(ScaleCmd var1);

   void visitScannerCmd(ScannerCmd var1);

   void visitToneIndicatorCmd(ToneIndicatorCmd var1);
}
