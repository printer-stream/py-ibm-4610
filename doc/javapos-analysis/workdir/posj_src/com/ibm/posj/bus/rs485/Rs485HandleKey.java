package com.ibm.posj.bus.rs485;

import com.ibm.posj.HandleKey;
import com.ibm.posj.HandleKeyVisitor;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCat;

public class Rs485HandleKey implements HandleKey {
   private DevCat devCat = null;
   private int number = 0;
   private byte slotNumber = 0;
   private byte portNumber = 0;
   private byte deviceNumber = 0;
   private String name = "";

   public Rs485HandleKey(DevCat deviceCat, int num, byte sNumber, byte pNumber, byte dNumber) {
      this.devCat = deviceCat;
      this.number = num;
      this.slotNumber = sNumber;
      this.portNumber = pNumber;
      this.deviceNumber = dNumber;
      StringBuffer sb = new StringBuffer();
      sb.append("<RS485HandleKey:");
      sb.append("\n\t devCat=\"" + this.devCat.toString());
      sb.append("\n\t number = " + this.number);
      sb.append("\n\t SioSlotNumber = 0x" + Integer.toHexString(this.slotNumber));
      sb.append("\n\t SioPortNumber = 0x" + Integer.toHexString(this.portNumber));
      sb.append("\n\t SioDeviceNumber = 0x" + Integer.toHexString(this.deviceNumber));
      sb.append("\"/>\n");
      this.name = sb.toString();
   }

   public void accept(HandleKeyVisitor visitor) {
      visitor.visitRs485HandleKey(this);
   }

   public DevCat getDevCat() {
      return this.devCat;
   }

   public int getSioSlotNumber() {
      return this.slotNumber;
   }

   public int getSioPortNumber() {
      return this.portNumber;
   }

   public int getSioDeviceNumber() {
      return this.deviceNumber;
   }

   public int getNumber() {
      return this.number;
   }

   public DevBus getDevBus() {
      return DevBuses.RS485_DEVBUS;
   }

   public String toString() {
      return this.name;
   }

   public int hashCode() {
      return this.toString().hashCode();
   }

   public boolean equals(Object obj) {
      try {
         return ((Rs485HandleKey)obj).toString().equals(this.toString());
      } catch (NullPointerException var3) {
         return false;
      } catch (ClassCastException var4) {
         return false;
      }
   }
}
