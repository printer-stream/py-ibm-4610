package com.ibm.embedded;

public interface EmbeddedDriverVisitor {
   void visitCashDrawer(CashDrawerEmbeddedDriver var1);

   void visitKeylock(KeylockEmbeddedDriver var1);

   void visitHardTotals(HardTotalsEmbeddedDriver var1);

   void visitMotionSensor(MotionSensorEmbeddedDriver var1);
}
