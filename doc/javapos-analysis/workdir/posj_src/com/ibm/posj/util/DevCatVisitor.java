package com.ibm.posj.util;

public interface DevCatVisitor {
   void visitUnknown(DevCat var1);

   void visitComposite(DevCat var1);

   void visitBumpBar(DevCat var1);

   void visitCashDrawer(DevCat var1);

   void visitCheckScanner(DevCat var1);

   void visitCAT(DevCat var1);

   void visitCoinDispenser(DevCat var1);

   void visitFiscalPrinter(DevCat var1);

   void visitHardTotals(DevCat var1);

   void visitKeylock(DevCat var1);

   void visitLineDisplay(DevCat var1);

   void visitMICR(DevCat var1);

   void visitMSR(DevCat var1);

   void visitMotionSensor(DevCat var1);

   void visitPinpad(DevCat var1);

   void visitPOSKeyboard(DevCat var1);

   void visitPOSPower(DevCat var1);

   void visitPOSPrinter(DevCat var1);

   void visitRemoteOrderDisplay(DevCat var1);

   void visitScale(DevCat var1);

   void visitScanner(DevCat var1);

   void visitSignatureCapture(DevCat var1);

   void visitToneIndicator(DevCat var1);
}
