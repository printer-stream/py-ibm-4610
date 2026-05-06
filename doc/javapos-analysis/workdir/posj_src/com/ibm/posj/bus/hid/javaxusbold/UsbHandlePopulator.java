package com.ibm.posj.bus.hid.javaxusbold;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleFactory;
import com.ibm.posj.HandleRegistry;
import com.ibm.posj.PosException;
import com.ibm.posj.PosSystem;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.bus.AbstractHandlePopulator;
import com.ibm.posj.bus.hid.HidHandleFactoryV;
import com.ibm.posj.bus.hid.HidHandleImp;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;
import javax.usb.event.UsbServicesEvent;
import javax.usb.event.UsbServicesListener;

public class UsbHandlePopulator extends AbstractHandlePopulator {
   private BooleanMonitor printerInitDelay = new BooleanMonitor(false);
   private List deferredPrinters = new LinkedList();
   private boolean[] connectedPrinter = new boolean[]{false};
   private boolean isIniting = true;
   private UsbServices usbServices = null;
   private UsbServicesListener usbServicesListener = new UsbHandlePopulator.UsbServicesL();
   private UsbHandleImpFactory usbHandleImpFactory = new UsbHandleImpFactory(this);
   private int newDeviceConnecting = 0;
   private long initDelay = 5000L;
   private long connectDelay = 5000L;
   private long printer_delay = 22500L;
   public static final String USB_HANDLE_POPULATOR_NAME_STRING = "USB/HID Handle Populator";
   public static final String INIT_DELAY_PROPERTY = "com.ibm.posj.bus.hid.javaxusb.UsbHandlePopulator.INIT_DELAY";
   public static final String CONNECT_DELAY_PROPERTY = "com.ibm.posj.bus.hid.javaxusb.UsbHandlePopulator.CONNECT_DELAY";
   public static final String PRINTER_INIT_DELAY_PROPERTY = "com.ibm.posj.bus.hid.javaxusb.UsbHandlePopulator.INIT_4610_DELAY";
   public static final short LEGACY_PRINTER_ADAPTER_LEVEL = 263;
   public static final short PRINTER_ADAPTER_LEVEL = 531;
   public static final int MEASURED_REINIT_DELAY = 22500;
   public static final long INIT_DELAY = 5000L;
   public static final long CONNECT_DELAY = 5000L;
   public static final long INIT_NEWDEVICE_DELAY = 1000L;

   public UsbHandlePopulator(HandleRegistry handleRegistry, HandleFactory handleFactory, PosSystem.EventHelper eventHelper) {
      super(handleRegistry, handleFactory, eventHelper);
      PosSystem.Properties p = PosSystemManager.getInstance().getProperties();
      if (p.isPropertyDefined("com.ibm.posj.bus.hid.javaxusb.UsbHandlePopulator.INIT_DELAY")) {
         try {
            this.initDelay = Long.decode(p.getPropertyString("com.ibm.posj.bus.hid.javaxusb.UsbHandlePopulator.INIT_DELAY"));
         } catch (Exception var8) {
         }
      }

      if (p.isPropertyDefined("com.ibm.posj.bus.hid.javaxusb.UsbHandlePopulator.CONNECT_DELAY")) {
         try {
            this.connectDelay = Long.decode(p.getPropertyString("com.ibm.posj.bus.hid.javaxusb.UsbHandlePopulator.CONNECT_DELAY"));
         } catch (Exception var7) {
         }
      }

      if (p.isPropertyDefined("com.ibm.posj.bus.hid.javaxusb.UsbHandlePopulator.INIT_4610_DELAY")) {
         try {
            this.printer_delay = Long.decode(p.getPropertyString("com.ibm.posj.bus.hid.javaxusb.UsbHandlePopulator.INIT_4610_DELAY"));
         } catch (Exception var6) {
         }
      }
   }

   void syncAddUsbDevice(UsbDevice device) throws PosException {
      HidHandleFactoryV hidHandleFactoryV = new HidHandleFactoryV(this.getHandleFactory());
      PosException posException = null;
      Iterator iterator = null;

      try {
         iterator = this.usbHandleImpFactory.createHandleImps(device).iterator();
      } catch (UsbException var6) {
         throw new PosException("Error while creating HandleImps", var6);
      }

      while (iterator.hasNext()) {
         HidHandleImp hhI = (HidHandleImp)iterator.next();
         if (null == hhI.getHandle()) {
            hidHandleFactoryV.clear();
            hhI.accept(hidHandleFactoryV);
            if (null == hidHandleFactoryV.getException()) {
               if (null != hidHandleFactoryV.getHandle()) {
                  this.getHandleRegistry().addHandle(hidHandleFactoryV.getHandle());
               }
            } else {
               posException = new PosException("Error while creating Handle", hidHandleFactoryV.getException());
            }
         }
      }

      if (null != posException) {
         this.lastException = posException;
         throw posException;
      }
   }

   void asyncAddUsbDevice(UsbDevice device) {
      UsbHandlePopulator.AddUsbDevice auD = new UsbHandlePopulator.AddUsbDevice(device);
      this.getRunnableManager().add(auD);
   }

   void delayedAsyncAddUsbDevice(UsbDevice device, boolean compoundWait) {
      UsbHandlePopulator.AddUsbDevice auD = new UsbHandlePopulator.DelayedAddUsbDevice(device, compoundWait);
      this.getRunnableManager().add(auD);
   }

   void setLastException(Exception exception) {
      this.lastException = exception;
   }

   protected long getInitDelay() {
      return this.initDelay;
   }

   protected long getConnectDelay() {
      return this.connectDelay;
   }

   protected long getPrinterInitDelay() {
      return this.printer_delay;
   }

   private void init() throws PosException {
      try {
         this.usbServices = UsbHostManager.getUsbServices();
      } catch (UsbException var7) {
         throw new PosException("Could not access UsbServices", var7);
      }

      UsbHub rootHub = null;

      try {
         rootHub = this.usbServices.getRootUsbHub();
      } catch (UsbException var6) {
         throw new PosException("Error while accessing root UsbHub", var6);
      }

      this.usbServices.addUsbServicesListener(this.usbServicesListener);
      this.syncAddUsbDevice(rootHub);

      try {
         if (this.isConfirmedPrinterConnected()) {
            try {
               this.printerInitDelay.waitForTrue((int)this.getPrinterInitDelay());
            } catch (Exception var4) {
            }
         }

         Thread.sleep(this.getInitDelay());
      } catch (InterruptedException var5) {
      }

      this.clearConfirmedPrinterTime();

      while (0 < this.newDeviceConnecting) {
         try {
            Thread.sleep(1000L);
         } catch (InterruptedException var3) {
         }
      }
   }

   private void removeHandleRegistry() {
      HandleRegistry registry = this.getHandleRegistry();
      Iterator iterator = registry.getHandles();
      Vector temp = new Vector();

      while (iterator.hasNext()) {
         temp.add(iterator.next());
      }

      for (int i = 0; i < temp.size(); i++) {
         registry.removeHandle((Handle)temp.elementAt(i));
      }
   }

   public String getName() {
      return "USB/HID Handle Populator";
   }

   public DevBus getDevBus() {
      return DevBuses.HID_DEVBUS;
   }

   public void start() throws PosException {
      if (!this.isStarted()) {
         super.start();

         try {
            this.init();
         } catch (PosException var5) {
            this.stop();
            throw var5;
         } finally {
            this.setIniting(false);
         }
      }
   }

   public void stop() {
      if (this.isStarted()) {
         try {
            this.usbServices.removeUsbServicesListener(this.usbServicesListener);
         } catch (NullPointerException var3) {
         }

         this.removeHandleRegistry();

         try {
            super.stop();
         } catch (PosException var2) {
         }
      }
   }

   public void setIniting(boolean isIniting) {
      this.isIniting = isIniting;
   }

   public boolean isIniting() {
      return this.isIniting;
   }

   public void confirmConnectedPrinter() {
      this.connectedPrinter[0] = true;
   }

   public boolean isConfirmedPrinterConnected() {
      return null == this.connectedPrinter ? false : this.connectedPrinter[0];
   }

   public void clearConfirmedPrinterTime() {
      this.connectedPrinter = null;
   }

   class AddUsbDevice implements Runnable {
      public UsbDevice usbDevice = null;

      public AddUsbDevice(UsbDevice device) {
         this.usbDevice = device;
      }

      public void run() {
         try {
            UsbHandlePopulator.this.syncAddUsbDevice(this.usbDevice);
         } catch (PosException var2) {
            UsbHandlePopulator.this.setLastException(var2);
         }
      }
   }

   class DelayedAddUsbDevice extends UsbHandlePopulator.AddUsbDevice implements Runnable {
      private boolean compoundWait = false;

      public DelayedAddUsbDevice(UsbDevice device, boolean wait) {
         super(device);
         this.compoundWait = wait;
      }

      public void run() {
         UsbHandlePopulator.this.newDeviceConnecting++;
         int totalWait = 0;
         int thisWait = 1;
         if (this.compoundWait) {
            thisWait = UsbHandlePopulator.this.newDeviceConnecting;
         }

         do {
            try {
               Thread.sleep(UsbHandlePopulator.this.getConnectDelay() * (long)thisWait);
            } catch (InterruptedException var4) {
               thisWait = 0;
            }

            totalWait += thisWait;
            thisWait = UsbHandlePopulator.this.newDeviceConnecting - totalWait;
         } while (this.compoundWait && 0 < thisWait);

         super.run();
         UsbHandlePopulator.this.newDeviceConnecting--;
      }
   }

   class UsbServicesL implements UsbServicesListener {
      public void usbDeviceAttached(UsbServicesEvent event) {
         UsbDevice device = event.getUsbDevice();
         UsbHandlePopulator.this.delayedAsyncAddUsbDevice(device, true);
         if (UsbHandlePopulator.this.isIniting()) {
            try {
               String serial = device.getSerialNumberString();
               if (UsbHandlePopulator.this.deferredPrinters.contains(serial)) {
                  UsbHandlePopulator.this.deferredPrinters.remove(serial);
                  if (UsbHandlePopulator.this.deferredPrinters.isEmpty()) {
                     UsbHandlePopulator.this.printerInitDelay.set(true);
                  }
               }
            } catch (Exception var4) {
            }
         }
      }

      public void usbDeviceDetached(UsbServicesEvent event) {
         UsbDevice device = event.getUsbDevice();

         try {
            if (UsbHandlePopulator.this.isIniting()) {
               UsbHandlePopulator.this.deferredPrinters.add(device.getSerialNumberString());
            }
         } catch (Exception var4) {
         }
      }
   }
}
