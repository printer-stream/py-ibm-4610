package com.ibm.rs232.event;

public class Rs232ControlLineEvent extends Rs232Event {
   private int eventType = 0;
   private boolean value = false;
   public static final int CTS_EVENT_TYPE = 1;
   public static final int DSR_EVENT_TYPE = 2;
   public static final int OUTPUT_BUFFER_EMPTY_EVENT_TYPE = 3;

   public Rs232ControlLineEvent(Object source, int eventType, boolean value) {
      super(source);
      this.eventType = eventType;
      this.value = value;
   }

   public int getEventType() {
      return this.eventType;
   }

   public boolean getValue() {
      return this.value;
   }

   public boolean isCTSEvent() {
      return this.eventType == 1;
   }

   public boolean isDSREvent() {
      return this.eventType == 2;
   }

   public boolean isOutputBufferEmptyEvent() {
      return this.eventType == 3;
   }
}
