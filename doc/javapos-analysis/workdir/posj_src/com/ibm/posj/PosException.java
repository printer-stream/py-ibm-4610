package com.ibm.posj;

public class PosException extends Exception {
   private Exception origException = null;

   public PosException(String msg) {
      super(msg);
   }

   public PosException(String msg, Exception origException) {
      this(msg);
      this.origException = origException;
   }

   public Exception getOrigException() {
      return this.origException;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("PosException.message = " + this.getMessage() + "\n");
      if (this.getOrigException() == null) {
         return sb.toString();
      } else {
         if (this.getOrigException() instanceof PosException) {
            sb.append(this.getOrigException().toString());
         } else {
            sb.append("\torigException.message = " + this.getOrigException().getMessage() + "\n");
         }

         return sb.toString();
      }
   }
}
