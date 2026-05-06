package com.ibm.posj.flash;

import com.ibm.posj.PosException;

public class FlashException extends PosException {
   public FlashException(String s) {
      super(s);
   }

   public FlashException(String s, Exception e) {
      super(s, e);
   }
}
