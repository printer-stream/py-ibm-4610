package com.ibm.posj.bus.hid;

import com.ibm.jutil.Util;
import com.ibm.posj.HandleKey;
import com.ibm.posj.HandleKeyVisitor;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class HidHandleKey implements HandleKey {
   private int usage = 0;
   private int number = 0;
   private DevCat devCat = DevCats.UNKNOWN_DEVCAT;
   private String cachedString = null;

   public HidHandleKey(short usagePage, short usageID, int number, DevCat dCat) {
      this(usagePage << 16 | usageID, number, dCat);
   }

   public HidHandleKey(int u, int n, DevCat dCat) {
      this.usage = u;
      this.number = n;
      this.devCat = dCat;
      StringBuffer sb = new StringBuffer();
      sb.append("<HidHandleKey:");
      sb.append("\n\t usagePage=0x" + Util.toHexString(this.getUsagePage()));
      sb.append("\n\t usageID=0x" + Util.toHexString(this.getUsageID()));
      sb.append("\n\t number=" + this.getNumber());
      sb.append("\n\t devCat=\"" + this.devCat.toString() + "\"/>");
      this.cachedString = sb.toString();
   }

   public void accept(HandleKeyVisitor visitor) {
      visitor.visitHidHandleKey(this);
   }

   public String toString() {
      return this.cachedString;
   }

   public int hashCode() {
      return this.toString().hashCode();
   }

   public boolean equals(Object obj) {
      try {
         return ((HidHandleKey)obj).toString().equals(this.toString());
      } catch (NullPointerException var3) {
         return false;
      } catch (ClassCastException var4) {
         return false;
      }
   }

   public int getUsage() {
      return this.usage;
   }

   public short getUsagePage() {
      return (short)(this.usage >> 16);
   }

   public short getUsageID() {
      return (short)(this.usage & 65535);
   }

   public int getNumber() {
      return this.number;
   }

   public DevCat getDevCat() {
      return this.devCat;
   }

   public DevBus getDevBus() {
      return DevBuses.HID_DEVBUS;
   }
}
