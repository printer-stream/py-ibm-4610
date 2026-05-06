package com.ibm.posj.util;

public interface DevBusVisitor {
   void visitUnknown(DevBus var1);

   void visitPosKbd(DevBus var1);

   void visitEmbedded(DevBus var1);

   void visitUsb(DevBus var1);

   void visitHid(DevBus var1);

   void visitRS232(DevBus var1);

   void visitRS485(DevBus var1);
}
