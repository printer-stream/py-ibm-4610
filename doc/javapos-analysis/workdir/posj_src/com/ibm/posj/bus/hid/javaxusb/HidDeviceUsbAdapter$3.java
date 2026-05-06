package com.ibm.posj.bus.hid.javaxusb;

import com.ibm.hid.HidExceptionEvent;
import com.ibm.hid.HidListener;

class HidDeviceUsbAdapter$3 implements Runnable {
   HidDeviceUsbAdapter$3(HidDeviceUsbAdapter.HidListenerHelper this$1, HidListener val$hL, HidExceptionEvent val$event) {
      this.this$1 = this$1;
      this.val$hL = val$hL;
      this.val$event = val$event;
   }

   public void run() {
      this.val$hL.hidExceptionEventOccurred(this.val$event);
   }
}
