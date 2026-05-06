package com.ibm.posj.bus.poskbd;

import com.ibm.posj.HandleKey;
import com.ibm.posj.HandleKeyVisitor;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCat;

public class PosKbdHandleKey implements HandleKey {
   private String name = null;
   private DevCat devCat = null;
   private int kbdNumber = 0;
   private int functionNumber = 0;

   public PosKbdHandleKey(DevCat category, int keyboardNumber, int functionNumber) {
      this.devCat = category;
      this.kbdNumber = keyboardNumber;
      this.functionNumber = functionNumber;
      this.name = "PosKbd key\n\tKeyboard Number : "
         + keyboardNumber
         + "\n"
         + "\tFunction Number : "
         + functionNumber
         + "\n"
         + "\tBus Category : "
         + this.getDevBus()
         + "\n"
         + "\tDevice Category : "
         + this.getDevCat()
         + "\n";
   }

   public void accept(HandleKeyVisitor visitor) {
      visitor.visitPosKbdHandleKey(this);
   }

   public String toString() {
      return this.name;
   }

   public int hashCode() {
      return this.toString().hashCode();
   }

   public boolean equals(Object obj) {
      try {
         return ((PosKbdHandleKey)obj).hashCode() == this.hashCode();
      } catch (NullPointerException var3) {
         return false;
      } catch (ClassCastException var4) {
         return false;
      }
   }

   public DevCat getDevCat() {
      return this.devCat;
   }

   public DevBus getDevBus() {
      return DevBuses.POSKBD_DEVBUS;
   }

   public int getKeyboardNumber() {
      return this.kbdNumber;
   }

   public int getFunctionNumber() {
      return this.functionNumber;
   }
}
