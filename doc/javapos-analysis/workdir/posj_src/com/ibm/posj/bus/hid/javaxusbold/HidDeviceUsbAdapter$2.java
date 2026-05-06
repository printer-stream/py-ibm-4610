package com.ibm.posj.bus.hid.javaxusbold;

import com.ibm.hid.HidListener;
import com.ibm.hid.ReportEvent;

class HidDeviceUsbAdapter$2 implements Runnable {
   HidDeviceUsbAdapter$2(HidDeviceUsbAdapter.HidListenerHelper this$1, HidListener val$hL, ReportEvent val$reportEvent) {
      this.this$1 = this$1;
      this.val$hL = val$hL;
      this.val$reportEvent = val$reportEvent;
   }

   public void run() {
      this.val$hL.reportEventOccurred(this.val$reportEvent);
   }
}
