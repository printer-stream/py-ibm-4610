package com.ibm.posj.bus.usb;

import com.ibm.posj.HandleKey;
import com.ibm.posj.HandleKeyVisitor;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class UsbHandleKey implements HandleKey {
   private int vendorId = 0;
   private int productId = 0;
   private int deviceNumber = 0;
   private DevCat devCat = DevCats.UNKNOWN_DEVCAT;
   private String cachedString = null;

   public UsbHandleKey(int vId, int pId, DevCat dCat) {
      this.vendorId = vId;
      this.productId = pId;
      this.devCat = dCat;
   }

   public UsbHandleKey(int vId, int pId, int dNumber, DevCat dCat) {
      this.vendorId = vId;
      this.productId = pId;
      this.devCat = dCat;
      this.deviceNumber = dNumber;
   }

   public void accept(HandleKeyVisitor visitor) {
      visitor.visitUsbHandleKey(this);
   }

   public String toString() {
      if (this.cachedString == null) {
         StringBuffer sb = new StringBuffer();
         sb.append("<UsbHandleKey");
         sb.append(" vendorId=\"0x" + Integer.toHexString(this.vendorId).toUpperCase() + "\"");
         sb.append(" productId=\"0x" + Integer.toHexString(this.productId).toUpperCase() + "\"");
         if (this.deviceNumber != 0) {
            sb.append(" deviceNumber=\"0x" + Integer.toHexString(this.deviceNumber).toUpperCase() + "\"");
         }

         sb.append(" devCat=\"" + this.devCat.toString() + "\"/>");
         this.cachedString = sb.toString();
      }

      return this.cachedString;
   }

   public int hashCode() {
      return this.toString().hashCode();
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (!(obj instanceof UsbHandleKey)) {
         return false;
      } else {
         return obj == this ? true : this.toString().equals(obj.toString());
      }
   }

   public DevCat getDevCat() {
      return this.devCat;
   }

   public DevBus getDevBus() {
      return DevBuses.USB_DEVBUS;
   }

   public int getProductId() {
      return this.productId;
   }

   public int getVendorId() {
      return this.vendorId;
   }

   public int getDeviceNumber() {
      return this.deviceNumber;
   }
}
