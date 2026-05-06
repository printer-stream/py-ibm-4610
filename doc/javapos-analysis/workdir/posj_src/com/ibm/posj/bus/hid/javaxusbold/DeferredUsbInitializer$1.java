package com.ibm.posj.bus.hid.javaxusbold;

class DeferredUsbInitializer$1 implements Runnable {
   DeferredUsbInitializer$1(DeferredUsbInitializer this$0) {
      this.this$0 = this$0;
   }

   public void run() {
      this.this$0.deferredInitialize();
   }
}
