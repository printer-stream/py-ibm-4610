package com.ibm.posj.printer.event;

import com.ibm.jutil.ByteBuffer;
import com.ibm.posj.event.DataEvent;

public class PrintDataEvent extends DataEvent {
   private int type;
   private ByteBuffer data;

   public PrintDataEvent(Object src, int type, ByteBuffer data) {
      super(src, data.getBytesRef());
      this.data = data;
      this.type = type;
   }

   public int getType() {
      return this.type;
   }

   public byte byteAt(byte idx) {
      return this.data.byteAt(idx - 1);
   }

   public ByteBuffer getDataBuffer() {
      return this.data;
   }
}
