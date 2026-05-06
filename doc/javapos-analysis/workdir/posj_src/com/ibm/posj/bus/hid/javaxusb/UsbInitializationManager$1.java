package com.ibm.posj.bus.hid.javaxusb;

import javax.usb.event.UsbDeviceDataEvent;
import javax.usb.event.UsbDeviceErrorEvent;
import javax.usb.event.UsbDeviceEvent;
import javax.usb.event.UsbDeviceListener;

class UsbInitializationManager$1 implements UsbDeviceListener {
   UsbInitializationManager$1(UsbInitializationManager this$0) {
      this.this$0 = this$0;
   }

   public void usbDeviceDetached(UsbDeviceEvent udE) {
      UsbInitializationManager.access$002(this.this$0, null);
      udE.getUsbDevice().removeUsbDeviceListener(this);
   }

   public void errorEventOccurred(UsbDeviceErrorEvent udeE) {
   }

   public void dataEventOccurred(UsbDeviceDataEvent uddE) {
   }
}
