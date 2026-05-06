package com.ibm.posj.bus.rs485;

import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jsio.event.SioDeviceErrorEvent;
import com.ibm.jsio.event.SioDeviceListener;
import com.ibm.jsio.event.SioDeviceStatusEvent;
import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.ByteBuffer;

class Rs485HandlePopulator$2 implements SioDeviceListener {
   Rs485HandlePopulator$2(Rs485HandlePopulator this$0, ByteBuffer val$buffer, BooleanMonitor val$pOnline) {
      this.this$0 = this$0;
      this.val$buffer = val$buffer;
      this.val$pOnline = val$pOnline;
   }

   public void errorEventOccurred(SioDeviceErrorEvent event) {
   }

   public void dataEventOccurred(SioDeviceDataEvent event) {
      if (event.getData().length >= 13) {
         this.val$buffer.append(event.getData());
      }
   }

   public void statusEventOccurred(SioDeviceStatusEvent event) {
      if (event.getStatus() == 2) {
         this.this$0.println("device OFFLINE");
      } else if (event.getStatus() == 1) {
         this.this$0.println("device ONLINE");
         this.val$pOnline.set(true);
      }
   }
}
