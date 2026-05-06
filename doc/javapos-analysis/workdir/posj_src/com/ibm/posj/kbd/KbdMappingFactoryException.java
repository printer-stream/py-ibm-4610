package com.ibm.posj.kbd;

import com.ibm.posj.HandleException;

public class KbdMappingFactoryException extends HandleException {
   public KbdMappingFactoryException(String s) {
      super(s);
   }

   public KbdMappingFactoryException(String s, Exception e) {
      super(s, e);
   }
}
