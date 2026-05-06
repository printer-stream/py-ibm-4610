package com.ibm.rs232.event;

public class Rs232DataEvent extends Rs232Event {
   private byte[] data = null;

   public Rs232DataEvent(Object source, byte[] data) {
      super(source);
      this.data = data;
   }

   public byte[] getData() {
      return this.data;
   }
}
