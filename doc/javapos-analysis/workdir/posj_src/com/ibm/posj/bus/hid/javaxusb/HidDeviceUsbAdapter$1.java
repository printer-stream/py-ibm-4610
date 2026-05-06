package com.ibm.posj.bus.hid.javaxusb;

import com.ibm.hid.HidAsync;

class HidDeviceUsbAdapter$1 implements Runnable {
   HidDeviceUsbAdapter$1(HidDeviceUsbAdapter this$0, HidAsync val$hidAsync) {
      this.this$0 = this$0;
      this.val$hidAsync = val$hidAsync;
   }

   public void run() {
      this.val$hidAsync.accept(HidDeviceUsbAdapter.access$000(this.this$0));
   }
}
