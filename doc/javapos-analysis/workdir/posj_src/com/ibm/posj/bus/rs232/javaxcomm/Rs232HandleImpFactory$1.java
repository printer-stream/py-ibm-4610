package com.ibm.posj.bus.rs232.javaxcomm;

import com.ibm.jutil.ByteBuffer;
import com.ibm.rs232.DefaultRs232PortListener;
import com.ibm.rs232.event.Rs232DataEvent;
import com.ibm.rs232.event.Rs232ErrorEvent;

class Rs232HandleImpFactory$1 extends DefaultRs232PortListener {
   Rs232HandleImpFactory$1(Rs232HandleImpFactory this$0, ByteBuffer val$buffer) {
      this.this$0 = this$0;
      this.val$buffer = val$buffer;
   }

   public void dataEventOccurred(Rs232DataEvent rs232DataEvent) {
      this.val$buffer.append(rs232DataEvent.getData());
   }

   public void errorEventOccurred(Rs232ErrorEvent rs232ErrorEvent) {
      Rs232HandleImpFactory.access$000(this.this$0).println(rs232ErrorEvent.getRs232Exception().toString());
   }
}
