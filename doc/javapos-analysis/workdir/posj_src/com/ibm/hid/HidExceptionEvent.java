package com.ibm.hid;

import java.util.EventObject;

public class HidExceptionEvent extends EventObject {
   private HidException hidException = null;

   public HidExceptionEvent(Object source, HidException hidException) {
      super(source);
      this.hidException = hidException;
   }

   public HidException getHidException() {
      return this.hidException;
   }
}
