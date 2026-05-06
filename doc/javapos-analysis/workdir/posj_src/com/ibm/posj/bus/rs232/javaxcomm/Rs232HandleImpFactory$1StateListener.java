package com.ibm.posj.bus.rs232.javaxcomm;

import com.ibm.posj.bus.rs232.Rs232FiscalPrinterProtocolListener;
import com.ibm.rs232.Rs232Exception;

class Rs232HandleImpFactory$1StateListener implements Rs232FiscalPrinterProtocolListener {
   private boolean dataReceived;
   private Rs232PortCommAdapter port;

   Rs232HandleImpFactory$1StateListener(Rs232HandleImpFactory this$0) {
      this.this$0 = this$0;
      this.dataReceived = false;
      this.port = null;
   }

   public void writeToPort(byte[] data, int length) {
      try {
         this.port.submit(data);
      } catch (Rs232Exception var4) {
      }
   }

   public void fireErrorEvent(int type) {
   }

   public void fireDataEvent(byte[] data) {
      this.dataReceived = true;
      Rs232HandleImpFactory.access$000(this.this$0).println("data received");
   }

   public void fireStatusEvent(int sts) {
   }

   public void fireDirectIOEvent(byte[] data) {
   }

   public boolean isDirectIOMode() {
      return false;
   }

   public boolean getDataReceived() {
      return this.dataReceived;
   }

   public void setPort(Rs232PortCommAdapter commAdapter) {
      this.port = commAdapter;
   }
}
