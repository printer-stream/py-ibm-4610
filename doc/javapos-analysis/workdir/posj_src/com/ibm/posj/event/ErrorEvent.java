package com.ibm.posj.event;

public class ErrorEvent extends HandleEvent {
   private int errorCode = 0;
   private String errorMessage = "";

   public ErrorEvent(Object source, int i) {
      super(source);
      this.errorCode = i;
   }

   public ErrorEvent(Object source, int i, String msg) {
      super(source);
      this.errorCode = i;
      this.errorMessage = msg;
   }

   public int getErrorCode() {
      return this.errorCode;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }
}
