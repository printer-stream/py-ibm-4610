package com.ibm.posj.event;

public class OfflineEvent extends HandleEvent {
   private long time = 0L;

   public OfflineEvent(Object source, long time) {
      super(source);
      this.time = time;
   }

   public long getTime() {
      return this.time;
   }
}
