package com.ibm.posj.event;

public class StatusEvent extends HandleEvent {
   private int status = 0;

   public StatusEvent(Object source, int status) {
      super(source);
      this.status = status;
   }

   public int getStatusCode() {
      return this.status;
   }
}
