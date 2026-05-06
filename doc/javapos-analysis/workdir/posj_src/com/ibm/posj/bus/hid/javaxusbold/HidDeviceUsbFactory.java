package com.ibm.posj.bus.hid.javaxusbold;

import com.ibm.hid.HidDevice;
import javax.usb.UsbInterface;

public class HidDeviceUsbFactory {
   public HidDevice createHidDevice(UsbInterface usbInterface) {
      return new HidDeviceUsbAdapter(usbInterface);
   }
}
