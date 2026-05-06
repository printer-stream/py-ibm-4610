package com.ibm.posj.flash;

import java.util.Iterator;

public interface FlashFormat {
   void add(FlashRecord var1);

   FlashRecord get(int var1);

   int size();

   void remove(FlashRecord var1);

   Iterator getFRs();
}
