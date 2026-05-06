package com.ibm.posj.event;

public class OutputCompleteEvent extends HandleEvent {
   private int outputID = -1;

   public OutputCompleteEvent(Object source, int outputID) {
      super(source);
      this.outputID = outputID;
   }

   public int getOutputID() {
      return this.outputID;
   }
}
