package com.ibm.posj.bus.hid.javaxusbold;

import com.ibm.hid.HidExceptionEvent;
import com.ibm.hid.HidListener;

class HidDeviceUsbAdapter$3 implements Runnable {
   HidDeviceUsbAdapter$3(HidDeviceUsbAdapter.HidListenerHelper this$1, HidListener val$hL, HidExceptionEvent val$hidExceptionEvent) {
      this.this$1 = this$1;
      this.val$hL = val$hL;
      this.val$hidExceptionEvent = val$hidExceptionEvent;
   }

   public void run() {
      this.val$hL.hidExceptionEventOccurred(this.val$hidExceptionEvent);
   }
}
