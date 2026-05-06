package com.ibm.posj.bus.hid;

import java.util.List;

public class HidFlashHandleImpV extends DefaultHidHandleImpV implements HidHandleImpVisitor {
   private HidFlashHandleImp hidFlashHandleImp = null;

   public void filterHidHandleImps(List list) {
      if (null != list) {
         for (int i = 0; i < list.size(); i++) {
            try {
               HidHandleImp handleImp = (HidHandleImp)list.get(i);
               handleImp.accept(this);
            } catch (ClassCastException var4) {
            }
         }
      }
   }

   public HidFlashHandleImp getHidFlashHandleImp() {
      return this.hidFlashHandleImp;
   }

   public void visitHidFlashHandleImp(HidFlashHandleImp handleImp) {
      this.hidFlashHandleImp = handleImp;
   }
}
