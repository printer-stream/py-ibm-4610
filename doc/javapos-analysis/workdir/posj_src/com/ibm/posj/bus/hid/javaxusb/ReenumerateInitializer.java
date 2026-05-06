package com.ibm.posj.bus.hid.javaxusb;

import com.ibm.jutil.Util;
import com.ibm.posj.PosException;
import javax.usb.UsbControlIrp;
import javax.usb.UsbInterface;

public abstract class ReenumerateInitializer extends DeferredUsbInitializer implements UsbInitializer {
   protected abstract byte getUsbInterfaceNumber();

   protected abstract byte[] getReenumerateCommand();

   protected boolean shouldWaitForDevice() {
      return true;
   }

   protected short getVendorId() {
      return this.getUsbDevice().getUsbDeviceDescriptor().idVendor();
   }

   protected short getProductId() {
      return this.getUsbDevice().getUsbDeviceDescriptor().idProduct();
   }

   protected String getSerialNumber() {
      try {
         return this.getUsbDevice().getSerialNumberString();
      } catch (Exception var2) {
         if (this.tracer.isOn()) {
            this.tracer.println("Could not get SerialNumber : " + var2.getMessage());
            this.tracer.print(var2);
         }

         return "";
      }
   }

   protected void deferredInitialize() {
      byte bmRequestType = 33;
      byte bRequest = 9;
      short wValue = 512;
      short wIndex = (short)(255 & this.getUsbInterfaceNumber());
      UsbControlIrp irp = this.getUsbDevice().createUsbControlIrp(bmRequestType, bRequest, wValue, wIndex);
      irp.setData(this.getReenumerateCommand());
      UsbInterface iface = null;

      try {
         iface = this.getUsbDevice().getActiveUsbConfiguration().getUsbInterface(this.getUsbInterfaceNumber());
      } catch (Exception var15) {
         if (this.tracer.isOn()) {
            String errMsg = "Could not get UsbInterface " + Util.unsignedInt(this.getUsbInterfaceNumber()) + " : " + var15.getMessage();
            this.tracer.println(errMsg);
            this.tracer.print(var15);
         }

         this.getUsbHandlePopulator().setLastException(new PosException("Could not re-enumerate UsbDevice : " + this.getUsbDevice(), var15));
      }

      try {
         iface.claim(DefaultUsbInterfacePolicy.getInstance());
      } catch (Exception var14) {
         if (this.tracer.isOn()) {
            String errMsg = "Could not claim UsbInterface " + Util.unsignedInt(this.getUsbInterfaceNumber()) + " : " + var14.getMessage();
            this.tracer.println(errMsg);
            this.tracer.print(var14);
         }

         this.getUsbHandlePopulator().setLastException(new PosException("Could not re-enumerate UsbDevice : " + this.getUsbDevice(), var14));
      }

      short vId = 0;
      short pId = 0;
      String sN = "";
      boolean w = this.shouldWaitForDevice();
      if (w) {
         vId = this.getVendorId();
         pId = this.getProductId();
         sN = this.getSerialNumber();
         this.getUsbHandlePopulator().waitForDeviceToReenumerate(vId, pId, sN, this.getUsbDevice().hashCode());
      }

      try {
         this.getUsbDevice().syncSubmit(irp);
      } catch (Exception var13) {
         if (this.tracer.isOn()) {
            String errMsg = "Exception while submitting re-enumeration command (this may be ok) : " + var13.getMessage();
            this.tracer.println(errMsg);
            this.tracer.print(var13);
         }

         this.getUsbHandlePopulator().setLastException(new PosException("Could not re-enumerate UsbDevice : " + this.getUsbDevice(), var13));
      }
   }
}
