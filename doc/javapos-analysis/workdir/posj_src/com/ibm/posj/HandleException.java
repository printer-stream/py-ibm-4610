package com.ibm.posj;

public class HandleException extends PosException {
   public HandleException(String s) {
      super(s);
   }

   public HandleException(String s, Exception e) {
      super(s, e);
   }
}
