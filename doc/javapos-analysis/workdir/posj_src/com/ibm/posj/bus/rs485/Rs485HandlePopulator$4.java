package com.ibm.posj.bus.rs485;

import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jsio.event.SioDeviceErrorEvent;
import com.ibm.jsio.event.SioDeviceListener;
import com.ibm.jsio.event.SioDeviceStatusEvent;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;

class Rs485HandlePopulator$4 implements SioDeviceListener {
   Rs485HandlePopulator$4(Rs485HandlePopulator this$0, ByteBuffer val$buffer, ScannerDetectState val$state) {
      this.this$0 = this$0;
      this.val$buffer = val$buffer;
      this.val$state = val$state;
   }

   public void errorEventOccurred(SioDeviceErrorEvent event) {
   }

   public void dataEventOccurred(SioDeviceDataEvent event) {
      this.this$0.println("populator: data Event occurred " + Util.toFormatedHexString(event.getData()));
      this.val$buffer.replace(event.getData());
   }

   public void statusEventOccurred(SioDeviceStatusEvent event) {
      this.this$0.println("status Event occurred = " + event.getStatus());
      if (event.getStatus() == 2) {
         this.this$0.println("device OFFLINE");
      } else if (event.getStatus() == 1) {
         this.this$0.println("device ONLINE");
         this.val$state.setState(3);
      }
   }
}
