package com.ibm.rs232.event;

import com.ibm.rs232.Rs232Exception;

public class Rs232ErrorEvent extends Rs232Event {
   private Rs232Exception rs232Exception = null;

   public Rs232ErrorEvent(Object source, Rs232Exception rs232Exception) {
      super(source);
      this.rs232Exception = rs232Exception;
   }

   public Rs232Exception getRs232Exception() {
      return this.rs232Exception;
   }
}
