package com.ibm.hid;

public class DisconnectEvent extends HidExceptionEvent {
   public DisconnectEvent(Object source, HidException hidException) {
      super(source, hidException);
   }
}
