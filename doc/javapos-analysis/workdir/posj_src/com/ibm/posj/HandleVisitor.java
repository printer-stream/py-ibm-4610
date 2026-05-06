package com.ibm.posj;

public interface HandleVisitor {
   void visitCashDrawer(Handle var1);

   void visitCashDrawer4610(Handle var1);

   void visitCheckScanner(Handle var1);

   void visitFiscalPrinter(Handle var1);

   void visitHardTotals(Handle var1);

   void visitKeylock(Handle var1);

   void visitLineDisplay(Handle var1);

   void visitMICR(Handle var1);

   void visit4610MICR(Handle var1);

   void visitMSR(Handle var1);

   void visitMotionSensor(Handle var1);

   void visitPOSKeyboard(Handle var1);

   void visitPOSPrinter(Handle var1);

   void visitScale(Handle var1);

   void visitScanner(Handle var1);

   void visitToneIndicator(Handle var1);
}
