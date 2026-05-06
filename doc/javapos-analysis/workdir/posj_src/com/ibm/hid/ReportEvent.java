package com.ibm.hid;

import java.util.EventObject;

public class ReportEvent extends EventObject {
   private byte[] data = null;

   public ReportEvent(Object source, byte[] data) {
      super(source);
      this.data = data;
   }

   public byte[] getData() {
      return this.data;
   }
}
