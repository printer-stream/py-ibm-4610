package com.ibm.posj;

import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultCompositeHandle extends AbstractHandle implements CompositeHandle {
   private Handle mainHandle = null;
   private List handleList = new ArrayList();
   private static int count = 0;

   DefaultCompositeHandle() {
      this.handleCount = count++;
   }

   public DevCat getDevCat() {
      return DevCats.UNKNOWN_DEVCAT;
   }

   public Handle getMainHandle() {
      return this.mainHandle;
   }

   public List getHandleList() {
      return this.handleList;
   }

   public HandleKey getHandleKey() {
      return this.getMainHandle().getHandleKey();
   }

   public void accept(HandleVisitor visitor) {
      Iterator iterator = this.handleList.iterator();

      while (iterator.hasNext()) {
         ((Handle)iterator.next()).accept(visitor);
      }
   }
}
