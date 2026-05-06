package com.ibm.posj.bus.hid.javaxusb;

import com.ibm.hid.DisconnectEvent;
import com.ibm.hid.HidListener;

class HidDeviceUsbAdapter$4 implements Runnable {
   HidDeviceUsbAdapter$4(HidDeviceUsbAdapter.HidListenerHelper this$1, HidListener val$hL, DisconnectEvent val$event) {
      this.this$1 = this$1;
      this.val$hL = val$hL;
      this.val$event = val$event;
   }

   public void run() {
      this.val$hL.hidDeviceDisconnected(this.val$event);
   }
}
