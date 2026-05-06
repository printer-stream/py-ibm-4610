package com.ibm.posj;

public class RuntimePosException extends RuntimeException {
   public RuntimePosException(String msg) {
      super(msg);
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("<RuntimePosException.message = " + this.getMessage() + "/>\n");
      return sb.toString();
   }
}
