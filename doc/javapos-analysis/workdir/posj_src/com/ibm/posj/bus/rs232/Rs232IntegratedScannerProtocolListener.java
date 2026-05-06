package com.ibm.posj.bus.rs232;

public interface Rs232IntegratedScannerProtocolListener {
   void writeToPort(byte[] var1);

   void fireErrorEvent(int var1);

   void fireDataEvent(byte[] var1);

   void fireDirectIOEvent(byte[] var1);

   void fireOnlineEvent();

   void fireOfflineEvent();
}
