package com.ibm.embedded;

import java.io.PrintStream;
import java.io.PrintWriter;

public class EmbeddedException extends Exception {
   private Throwable cause = null;

   public EmbeddedException() {
   }

   public EmbeddedException(String message) {
      super(message);
   }

   public EmbeddedException(String message, Throwable cause) {
      super(message);
      this.cause = cause;
   }

   public Throwable getCause() {
      return this.cause;
   }

   public void printStackTrace() {
      super.printStackTrace();
      if (this.cause != null) {
         System.err.println("Caused by:");
         this.cause.printStackTrace();
      }
   }

   public void printStackTrace(PrintStream ps) {
      super.printStackTrace(ps);
      if (this.cause != null) {
         System.err.println("Caused by:");
         this.cause.printStackTrace(ps);
      }
   }

   public void printStackTrace(PrintWriter pw) {
      super.printStackTrace(pw);
      if (this.cause != null) {
         System.err.println("Caused by:");
         this.cause.printStackTrace(pw);
      }
   }
}
