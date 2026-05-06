package com.ibm.posj.bus.rs232.javaxcomm;

import com.ibm.jutil.ByteBuffer;
import com.ibm.rs232.DefaultRs232PortListener;
import com.ibm.rs232.event.Rs232DataEvent;
import com.ibm.rs232.event.Rs232ErrorEvent;

class Rs232HandleImpFactory$3 extends DefaultRs232PortListener {
   Rs232HandleImpFactory$3(Rs232HandleImpFactory this$0, ByteBuffer val$status) {
      this.this$0 = this$0;
      this.val$status = val$status;
   }

   public void dataEventOccurred(Rs232DataEvent rs232DataEvent) {
      this.val$status.append(rs232DataEvent.getData()[0]);
      Rs232HandleImpFactory.access$000(this.this$0).println("Verifying if CD present. status = " + this.val$status.byteAt(0));
   }

   public void errorEventOccurred(Rs232ErrorEvent rs232ErrorEvent) {
      Rs232HandleImpFactory.access$000(this.this$0).println(rs232ErrorEvent.getRs232Exception().toString());
   }
}
