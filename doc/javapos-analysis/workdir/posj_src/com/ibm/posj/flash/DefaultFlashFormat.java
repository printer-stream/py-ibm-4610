package com.ibm.posj.flash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultFlashFormat implements FlashFormat {
   private List recordList = new ArrayList();

   public void add(FlashRecord fr) {
      this.recordList.add(fr);
   }

   public FlashRecord get(int index) {
      return (FlashRecord)this.recordList.get(index);
   }

   public int size() {
      return this.recordList.size();
   }

   public void remove(FlashRecord fr) {
      this.recordList.remove(fr);
   }

   public Iterator getFRs() {
      return this.recordList.iterator();
   }
}
