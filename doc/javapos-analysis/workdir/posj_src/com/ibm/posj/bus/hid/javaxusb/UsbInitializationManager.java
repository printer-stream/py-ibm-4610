package com.ibm.posj.bus.hid.javaxusb;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import java.util.ArrayList;
import java.util.List;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.event.UsbDeviceListener;

public class UsbInitializationManager {
   private UsbHandlePopulator usbHandlePopulator = null;
   private UsbIDUtil usbIDUtil = UsbIDUtil.getInstance();
   private final Tracer tracer = TracerFactory.getInstance().createTracer("HID", "UsbInitializationManager");
   private List printers = null;
   private UsbDevice systemKeyboard = null;
   private final UsbDeviceListener sysKbdListener = new UsbInitializationManager$1(this);

   UsbInitializationManager(UsbHandlePopulator populator) {
      this.usbHandlePopulator = populator;
   }

   public boolean initializeUsbDevice(UsbDevice device) {
      boolean initialized = true;
      if (this.usbIDUtil.isKbdAsSystemKeyboard(device.getUsbDeviceDescriptor().idVendor(), device.getUsbDeviceDescriptor().idProduct())) {
         if (this.existSystemKeyboard() && !this.isSystemKeyboard(device)) {
            this.tracer.println("IBM keyboard : System keyboard already attached");
         } else {
            this.tracer.println("IBM keyboard : device will be used as system keyboard");
            if (!this.isSystemKeyboard(device)) {
               this.systemKeyboard = device;
               device.addUsbDeviceListener(this.sysKbdListener);
            }
         }
      }

      if (this.usbIDUtil.isKbdRenumerationRequired(device.getUsbDeviceDescriptor().idVendor(), device.getUsbDeviceDescriptor().idProduct())) {
         if (this.isSystemKeyboard(device)) {
            return true;
         } else {
            UsbInitializer initializer = new POSKeyboardInitializer();
            initializer.setUsbDevice(device);
            initializer.setUsbHandlePopulator(this.usbHandlePopulator);
            return initializer.initialize();
         }
      } else {
         if (this.usbIDUtil.isOEMScannerRenumerationRequired(device.getUsbDeviceDescriptor().idVendor(), device.getUsbDeviceDescriptor().idProduct())) {
            UsbInitializer initializer = new ProtocolConverterInitializer();
            initializer.setUsbDevice(device);
            initializer.setUsbHandlePopulator(this.usbHandlePopulator);
            initialized = initializer.initialize();
         }

         UsbDeviceDescriptor udd = device.getUsbDeviceDescriptor();
         if (UsbIDUtil.isIBM4610(udd.idVendor(), udd.idProduct()) && !this.usbHandlePopulator.isInitialized()) {
            try {
               String serial = device.getSerialNumberString();
               short pid = udd.idProduct();
               if (17717 == pid && null == this.printers) {
                  this.printers = new ArrayList(3);
               }

               if (!this.printers.contains(serial)) {
                  this.printers.add(serial);
                  if (UsbIDUtil.isPrinterAdapterResetNeeded(device.getUsbDeviceDescriptor().bcdDevice())) {
                     UsbInitializer initializer = new IBM4610PrinterInitializer();
                     initializer.setUsbDevice(device);
                     initializer.setUsbHandlePopulator(this.usbHandlePopulator);
                     return initializer.initialize();
                  }

                  return initialized;
               }
            } catch (Exception var8) {
               this.tracer.print(var8);
               return initialized;
            }
         }

         if (UsbIDUtil.isIBM4689(udd.idVendor(), udd.idProduct()) && !this.usbHandlePopulator.isInitialized()) {
            try {
               String serialx = device.getSerialNumberString();
               short pidx = udd.idProduct();
               if (17719 == pidx && null == this.printers) {
                  this.printers = new ArrayList(3);
               }

               if (!this.printers.contains(serialx)) {
                  this.printers.add(serialx);
                  UsbInitializer initializer = new IBM4689PrinterInitializer();
                  initializer.setUsbDevice(device);
                  initializer.setUsbHandlePopulator(this.usbHandlePopulator);
                  return initializer.initialize();
               }
            } catch (Exception var7) {
               this.tracer.print(var7);
               return initialized;
            }
         }

         return initialized;
      }
   }

   protected boolean existSystemKeyboard() {
      return null != this.systemKeyboard;
   }

   protected boolean isSystemKeyboard(UsbDevice device) {
      try {
         return this.systemKeyboard == null ? false : this.systemKeyboard.getSerialNumberString().equalsIgnoreCase(device.getSerialNumberString());
      } catch (Exception var3) {
         this.tracer.print(var3);
         return false;
      }
   }
}
