package com.ibm.posj.event;

public class DataEvent extends HandleEvent {
   private byte[] data = new byte[0];

   public DataEvent(Object source, byte[] byteArray) {
      super(source);
      this.data = byteArray;
   }

   public DataEvent(Object source) {
      super(source);
   }

   public byte[] getData() {
      return this.data;
   }
}
