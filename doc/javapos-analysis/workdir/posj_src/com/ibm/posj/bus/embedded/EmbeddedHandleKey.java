package com.ibm.posj.bus.embedded;

import com.ibm.posj.HandleKey;
import com.ibm.posj.HandleKeyVisitor;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCat;

public class EmbeddedHandleKey implements HandleKey {
   private DevCat devCat = null;
   private int number = 0;
   private String name = "";

   public EmbeddedHandleKey(DevCat devCat, int number) {
      this.devCat = devCat;
      this.number = number;
      StringBuffer sb = new StringBuffer();
      sb.append("<EmbeddedHandleKey:");
      sb.append("\n\t devCat=\"" + devCat.toString());
      sb.append("\n\t number = " + number);
      sb.append("\"/>\n");
      this.name = sb.toString();
   }

   public void accept(HandleKeyVisitor visitor) {
      visitor.visitEmbeddedHandleKey(this);
   }

   public DevCat getDevCat() {
      return this.devCat;
   }

   public int getNumber() {
      return this.number;
   }

   public DevBus getDevBus() {
      return DevBuses.EMBEDDED_DEVBUS;
   }

   public String toString() {
      return this.name;
   }

   public int hashCode() {
      return this.toString().hashCode();
   }

   public boolean equals(Object obj) {
      try {
         return ((EmbeddedHandleKey)obj).toString().equals(this.toString());
      } catch (NullPointerException var3) {
         return false;
      } catch (ClassCastException var4) {
         return false;
      }
   }
}
