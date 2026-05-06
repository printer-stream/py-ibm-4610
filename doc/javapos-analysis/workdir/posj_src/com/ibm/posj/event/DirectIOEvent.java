package com.ibm.posj.event;

public class DirectIOEvent extends HandleEvent {
   private byte[] data = new byte[0];

   public DirectIOEvent(Object source) {
      super(source);
   }

   public DirectIOEvent(Object source, byte[] byteArray) {
      super(source);
      this.data = byteArray;
   }

   public byte[] getData() {
      return this.data;
   }
}
