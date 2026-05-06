package com.ibm.posj.bus.hid.javaxusbold;

import javax.usb.UsbInterface;
import javax.usb.UsbInterfacePolicy;

public class DefaultUsbInterfacePolicy implements UsbInterfacePolicy {
   private static UsbInterfacePolicy instance = null;

   private DefaultUsbInterfacePolicy() {
   }

   static UsbInterfacePolicy getInstance() {
      if (instance == null) {
         instance = new DefaultUsbInterfacePolicy();
      }

      return instance;
   }

   public boolean forceClaim(UsbInterface arg0) {
      return true;
   }
}
