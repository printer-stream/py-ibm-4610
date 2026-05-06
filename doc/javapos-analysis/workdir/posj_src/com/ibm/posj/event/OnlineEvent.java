package com.ibm.posj.event;

public class OnlineEvent extends HandleEvent {
   private long time = 0L;

   public OnlineEvent(Object source, long time) {
      super(source);
      this.time = time;
   }

   public long getTime() {
      return this.time;
   }
}
