package com.ibm.posj;

public class HandleFactoryException extends PosException {
   public HandleFactoryException(String s) {
      super(s);
   }

   public HandleFactoryException(String s, Exception e) {
      super(s, e);
   }
}
