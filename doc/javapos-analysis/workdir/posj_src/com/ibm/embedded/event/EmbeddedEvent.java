package com.ibm.embedded.event;

import java.util.EventObject;

public class EmbeddedEvent extends EventObject {
   private byte status = 0;

   public EmbeddedEvent(Object source, byte status) {
      super(source);
      this.status = status;
   }

   public byte getStatus() {
      return this.status;
   }
}
