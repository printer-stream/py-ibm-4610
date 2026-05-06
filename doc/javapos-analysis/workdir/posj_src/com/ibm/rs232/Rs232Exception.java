package com.ibm.rs232;

public class Rs232Exception extends Exception {
   private Exception origException = null;

   public Rs232Exception(String s) {
      super(s);
   }

   public Rs232Exception(Exception e) {
      this.origException = e;
   }

   public Rs232Exception(String s, Exception e) {
      super(s);
      this.origException = e;
   }

   public Exception getOriginalException() {
      return this.origException;
   }
}
