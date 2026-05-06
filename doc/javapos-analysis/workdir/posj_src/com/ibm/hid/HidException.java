package com.ibm.hid;

public class HidException extends Exception {
   private Exception origException = null;

   public HidException() {
   }

   public HidException(String s) {
      super(s);
   }

   public HidException(Exception origException) {
      this.origException = origException;
   }

   public HidException(String s, Exception origException) {
      super(s);
      this.origException = origException;
   }

   public Exception getOriginalException() {
      return this.origException;
   }
}
