package com.ibm.posj.bus.rs485;

import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jsio.event.SioDeviceErrorEvent;
import com.ibm.jsio.event.SioDeviceListener;
import com.ibm.jsio.event.SioDeviceStatusEvent;
import com.ibm.jutil.ByteBuffer;

class Rs485HandlePopulator$1 implements SioDeviceListener {
   Rs485HandlePopulator$1(Rs485HandlePopulator this$0, ByteBuffer val$buffer) {
      this.this$0 = this$0;
      this.val$buffer = val$buffer;
   }

   public void errorEventOccurred(SioDeviceErrorEvent event) {
   }

   public void dataEventOccurred(SioDeviceDataEvent event) {
      this.val$buffer.append(event.getData());
   }

   public void statusEventOccurred(SioDeviceStatusEvent event) {
   }
}
