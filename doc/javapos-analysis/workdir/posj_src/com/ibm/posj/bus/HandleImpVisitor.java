package com.ibm.posj.bus;

public interface HandleImpVisitor {
   void visitCashDrawer(HandleImp var1);

   void visitCashDrawer4610(HandleImp var1);

   void visitCheckScanner(HandleImp var1);

   void visitFiscalPrinter(HandleImp var1);

   void visitHardTotals(HandleImp var1);

   void visitKeylock(HandleImp var1);

   void visitLineDisplay(HandleImp var1);

   void visitMICR(HandleImp var1);

   void visit4610MICR(HandleImp var1);

   void visitMSR(HandleImp var1);

   void visitMotionSensor(HandleImp var1);

   void visitPOSKeyboard(HandleImp var1);

   void visitPOSPrinter(HandleImp var1);

   void visitScale(HandleImp var1);

   void visitScanner(HandleImp var1);

   void visitToneIndicator(HandleImp var1);
}
