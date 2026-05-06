package com.ibm.posj.bus.rs232;

import com.ibm.jutil.Timerable;
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232DataEvent;

class Rs232MSRReader$1 implements Timerable {
   Rs232MSRReader$1(Rs232MSRReader this$0, Rs232Port.EventHelper val$helper) {
      this.this$0 = this$0;
      this.val$helper = val$helper;
   }

   public void timerExpired() {
      if (Rs232MSRReader.access$000(this.this$0).isOn()) {
         Rs232MSRReader.access$000(this.this$0).println("timerExpired!");
      }

      Rs232MSRReader.access$102(this.this$0, true);
      this.val$helper.fireDataEvent(new Rs232DataEvent(this, Rs232MSRReader.access$200(this.this$0).getBytes()));
   }
}
