package com.ibm.posj.bus.rs232;

public interface Rs232FiscalPrinterProtocolListener {
   void writeToPort(byte[] var1, int var2);

   void fireErrorEvent(int var1);

   void fireDataEvent(byte[] var1);

   void fireStatusEvent(int var1);

   void fireDirectIOEvent(byte[] var1);

   boolean isDirectIOMode();
}
