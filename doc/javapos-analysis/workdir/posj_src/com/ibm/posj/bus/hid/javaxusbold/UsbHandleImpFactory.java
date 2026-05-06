package com.ibm.posj.bus.hid.javaxusbold;

import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.HidListener;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.HandleException;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.hid.DefaultHidKeylockStrategy;
import com.ibm.posj.bus.hid.DefaultHidPOSKeyboardStrategy;
import com.ibm.posj.bus.hid.DefaultHidToneIndicatorStrategy;
import com.ibm.posj.bus.hid.Hid4610PrinterHandleImp;
import com.ibm.posj.bus.hid.Hid4689PrinterHandleImp;
import com.ibm.posj.bus.hid.HidAPALineDisplayHandleImp;
import com.ibm.posj.bus.hid.HidBootModePOSKeyboardHandleImp;
import com.ibm.posj.bus.hid.HidCashDrawerHandleImp;
import com.ibm.posj.bus.hid.HidFiscalPrinterHandleImp;
import com.ibm.posj.bus.hid.HidFlashHandleImp;
import com.ibm.posj.bus.hid.HidHandleImp;
import com.ibm.posj.bus.hid.HidHandleKey;
import com.ibm.posj.bus.hid.HidHardTotalsHandleImp;
import com.ibm.posj.bus.hid.HidKeylockHandleImp;
import com.ibm.posj.bus.hid.HidKeylockStrategy;
import com.ibm.posj.bus.hid.HidLineDisplayHandleImp;
import com.ibm.posj.bus.hid.HidMSRHandleImp;
import com.ibm.posj.bus.hid.HidPOSKeyboardHandleImp;
import com.ibm.posj.bus.hid.HidPOSKeyboardStrategy;
import com.ibm.posj.bus.hid.HidPOSPrinterCashDrawerHandleImp;
import com.ibm.posj.bus.hid.HidPOSPrinterCheckScannerHandleImp;
import com.ibm.posj.bus.hid.HidPOSPrinterMICRHandleImp;
import com.ibm.posj.bus.hid.HidPOSPrinterToneIndicatorHandleImp;
import com.ibm.posj.bus.hid.HidScaleHandleImp;
import com.ibm.posj.bus.hid.HidScannerHandleImp;
import com.ibm.posj.bus.hid.HidToneIndicatorHandleImp;
import com.ibm.posj.bus.hid.HidToneIndicatorStrategy;
import com.ibm.posj.printer.IBM4610SSTToneIndicatorImp;
import com.ibm.posj.printer.IBMPrinterToneIndicatorImp;
import com.ibm.posj.printer.ibm4610.IBM4610CashDrawerImp;
import com.ibm.posj.printer.ibm4610.IBM4610CheckScannerImp;
import com.ibm.posj.printer.ibm4610.IBM4610MICRImp;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.POSKeyboardUtil;
import com.ibm.posj.util.PosjUtil;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbNotActiveException;
import javax.usb.event.UsbDeviceDataEvent;
import javax.usb.event.UsbDeviceErrorEvent;
import javax.usb.event.UsbDeviceEvent;
import javax.usb.event.UsbDeviceListener;

public class UsbHandleImpFactory {
   private UsbInitializationManager initializer = null;
   private UsbHandlePopulator usbHandlePopulator = null;
   private HidDeviceUsbFactory factory = new HidDeviceUsbFactory();
   private Hashtable numberTable = new Hashtable();
   private List handleImpTmpList = new ArrayList();
   private UsbDeviceListener processedListener = new UsbHandleImpFactory.ProcessDeviceListener(null);
   private List processedDevices = new Vector();
   private Hashtable hidHandleImpTable = new Hashtable();
   private HidKeylockStrategy.Factory keylockStrategyFactory = null;
   private HidToneIndicatorStrategy.Factory toneStrategyFactory = null;
   private HidPOSKeyboardStrategy.Factory kbdStrategyFactory = null;
   private Tracer tracer;
   private boolean isCheckScannerPresent;
   private boolean isMicrPresent;
   private boolean isTIPresent;
   private int cashDrawer4610DevNum;
   private int printerID_microcodeLevel;
   private final List EMTPY_LIST;
   public static final byte TI4_EMULATION_BIT_POSITION = 1;

   public UsbHandleImpFactory(UsbHandlePopulator populator) {
      this.tracer = this.tracer = TracerFactory.getInstance().createTracer("HID", "UsbHandleImpFactory");
      this.isCheckScannerPresent = false;
      this.isMicrPresent = false;
      this.isTIPresent = false;
      this.cashDrawer4610DevNum = 4;
      this.printerID_microcodeLevel = -1;
      this.EMTPY_LIST = new ArrayList();
      this.usbHandlePopulator = populator;
      this.initializer = new UsbInitializationManager(populator);
   }

   public synchronized List createHandleImps(UsbInterface usbInterface) throws UsbException, HidException {
      if (this.tracer.isOn()) {
         this.tracer.println("Creating HandleImps for UsbInterface " + Util.unsignedInt(usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber()));
      }

      short pId = usbInterface.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idProduct();
      short vId = usbInterface.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idVendor();
      byte interfaceNumber = usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber();
      if (!this.ignoreUsbInterface(usbInterface) && !UsbUtil.getInstance().ignoreInterface(vId, pId, interfaceNumber)) {
         HidDevice hd = this.factory.createHidDevice(usbInterface);
         int usage = 0;

         try {
            hd.connect();
         } catch (HidException var10) {
            this.tracer.println("Could not connect HidDevice : " + var10.getMessage());
            this.tracer.print(var10);
            this.tracer.println("Disconnecting adapter!!");
            hd.disconnect();
            throw var10;
         }

         try {
            usage = this.getUsage(hd);
         } catch (HidException var9) {
            this.tracer.println("Could not get Usage : " + var9.getMessage());
            hd.disconnect();
            throw var9;
         }

         try {
            switch (this.getUsagePage(usage)) {
               case -187:
                  return this.createIbmHandleImps(usbInterface, hd, usage);
               case 1:
                  return this.createGenericDesktopHandleImps(usbInterface, hd, usage);
               default:
                  this.tracer.println("Unrecognized interface, ignoring");
                  hd.disconnect();
                  return new ArrayList();
            }
         } catch (HidException var8) {
            hd.disconnect();
            throw var8;
         }
      } else {
         if (this.tracer.isOn()) {
            this.tracer.println("Ignoring interface.");
         }

         return new ArrayList();
      }
   }

   public synchronized List createHandleImps(UsbDevice usbDevice) throws UsbException {
      if (usbDevice.isUsbHub()) {
         return this.createHandleImps((UsbHub)usbDevice);
      } else {
         if (this.tracer.isOn()) {
            String sn = "";
            short pId = usbDevice.getUsbDeviceDescriptor().idProduct();

            try {
               sn = usbDevice.getSerialNumberString();
            } catch (UnsupportedEncodingException var11) {
               this.tracer.print(var11);
            } catch (UsbDisconnectedException var12) {
               this.tracer.print(var12);
               this.tracer
                  .println(
                     "UsbDisconnectedException " + var12.toString() + " during usbDevice.getSerialNumberString() ignoring device:" + Util.toHexString(pId)
                  );
               return this.EMTPY_LIST;
            } catch (UsbException var13) {
               this.tracer.print(var13);
               this.tracer.println("UsbException " + var13.toString() + "during usbDevice.getSerialNumberString() ignoring device: " + Util.toHexString(pId));
               return this.EMTPY_LIST;
            }

            this.tracer.println("Creating HandleImps for UsbDevice 0x" + Util.toHexString(pId) + ", S/N '" + sn + "'");
         }

         if (!this.isKnownPosDevice(usbDevice)) {
            this.tracer
               .println(
                  "Skip unexpected UsbDevice : VendorID "
                     + Util.toHexString(usbDevice.getUsbDeviceDescriptor().idVendor())
                     + " ProductID "
                     + Util.toHexString(usbDevice.getUsbDeviceDescriptor().idProduct())
               );
            return this.EMTPY_LIST;
         } else if (this.processedDevices.contains(usbDevice)) {
            this.tracer.println("Device " + usbDevice.hashCode() + " already processed, ignoring");
            return this.EMTPY_LIST;
         } else {
            this.tracer.println("Processing device " + usbDevice.hashCode());
            this.processedDevices.add(usbDevice);
            usbDevice.addUsbDeviceListener(this.processedListener);
            if (!this.initializer.initializeUsbDevice(usbDevice)) {
               this.tracer.println("Device not initialized, deferring to initializer");
               return this.EMTPY_LIST;
            } else {
               List list = new LinkedList();

               Iterator uii;
               try {
                  List usbInterfaceList = usbDevice.getActiveUsbConfiguration().getUsbInterfaces();
                  uii = usbInterfaceList.iterator();
               } catch (UsbNotActiveException var10) {
                  return list;
               }

               try {
                  while (uii.hasNext()) {
                     list.addAll(this.createHandleImps((UsbInterface)uii.next()));
                  }
               } catch (HidException var14) {
                  this.tracer.print(var14);
                  this.tracer
                     .println(
                        "HidException during HandleImp creation, disconnecting " + list.size() + " HandleImps and ignoring device : " + var14.getMessage()
                     );

                  for (int i = 0; i < list.size(); i++) {
                     ((HidHandleImp)list.get(i)).getHidDevice().disconnect();
                  }

                  return this.EMTPY_LIST;
               } catch (UsbNotActiveException var15) {
                  this.tracer.print(var15);
                  this.tracer.println("UsbNotActiveException during HandleImp creation, disconnecting " + list.size() + " HandleImps and ignoring device.");

                  for (int i = 0; i < list.size(); i++) {
                     ((HidHandleImp)list.get(i)).getHidDevice().disconnect();
                  }

                  return this.EMTPY_LIST;
               } catch (UsbDisconnectedException var16) {
                  this.tracer.print(var16);
                  this.tracer.println("UsbDisconnectedException during HandleImp creation, disconnecting " + list.size() + " HandleImps and ignoring device.");

                  for (int i = 0; i < list.size(); i++) {
                     ((HidHandleImp)list.get(i)).getHidDevice().disconnect();
                  }
               } catch (UsbException var17) {
                  this.tracer.print(var17);
                  this.tracer
                     .println(
                        "UsbException during HandleImp creation, disconnecting " + list.size() + " HandleImps and ignoring device : " + var17.getMessage()
                     );

                  for (int i = 0; i < list.size(); i++) {
                     ((HidHandleImp)list.get(i)).getHidDevice().disconnect();
                  }

                  return this.EMTPY_LIST;
               }

               String sN = null;

               for (int i = 0; i < list.size(); i++) {
                  HidHandleImp hidHandleImp = (HidHandleImp)list.get(i);

                  try {
                     sN = usbDevice.getSerialNumberString();
                  } catch (Exception var9) {
                     sN = "";
                  }

                  hidHandleImp.setSerialNumber(sN);
                  hidHandleImp.setProductID(usbDevice.getUsbDeviceDescriptor().idProduct());
                  hidHandleImp.setVendorID(usbDevice.getUsbDeviceDescriptor().idVendor());
                  hidHandleImp.setBCDLevel(usbDevice.getUsbDeviceDescriptor().bcdDevice());
                  hidHandleImp.getHandleImpGroup().clear();
                  hidHandleImp.getHandleImpGroup().addAll(list);
                  this.addToTable(hidHandleImp);
               }

               if (this.tracer.isOn()) {
                  this.tracer
                     .println(
                        "HandleImp creation successful, "
                           + list.size()
                           + " HandleImps created for device 0x"
                           + Util.toHexString(usbDevice.getUsbDeviceDescriptor().idProduct())
                           + ", vendor 0x"
                           + Util.toHexString(usbDevice.getUsbDeviceDescriptor().idVendor())
                           + ", bcdLevel 0x"
                           + Util.toHexString(usbDevice.getUsbDeviceDescriptor().bcdDevice())
                           + ", serialNumber \""
                           + sN
                           + "\""
                     );
               }

               return list;
            }
         }
      }
   }

   public synchronized List createHandleImps(UsbHub usbHub) throws UsbException {
      List list = new LinkedList();
      List usbDevices = usbHub.getAttachedUsbDevices();
      Iterator uii = usbDevices.iterator();

      while (uii.hasNext()) {
         list.addAll(this.createHandleImps((UsbDevice)uii.next()));
      }

      return list;
   }

   protected List createIbmHandleImps(UsbInterface usbInterface, HidDevice hd, int usage) throws HidException {
      List list = new LinkedList();
      HidHandleKey key = null;
      short productID = 0;
      switch ((short)usage) {
         case -24576:
            if (!this.reconnectHidDevice(list, hd, usbInterface, DevCats.UNKNOWN_DEVCAT)) {
               key = new HidHandleKey(usage, this.getNumber(usage), DevCats.UNKNOWN_DEVCAT);
               HidFlashHandleImp hidFlashHandleImp = new HidFlashHandleImp(key, hd);
               list.add(hidFlashHandleImp);
               this.checkNonBootKbd(hidFlashHandleImp);
            }
            break;
         case 5120:
            if (this.reconnectHidDevice(list, hd.getSynchronizedHidDevice(), usbInterface, DevCats.POSKEYBOARD_DEVCAT)) {
               this.reconnectHidDevice(list, hd.getSynchronizedHidDevice(), usbInterface, DevCats.KEYLOCK_DEVCAT);
               this.reconnectHidDevice(list, hd.getSynchronizedHidDevice(), usbInterface, DevCats.TONEINDICATOR_DEVCAT);
            } else {
               int number = this.getNumber(usage);
               productID = usbInterface.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idProduct();
               key = new HidHandleKey(usage, number, DevCats.POSKEYBOARD_DEVCAT);
               list.add(
                  new HidPOSKeyboardHandleImp(
                     key,
                     hd.getSynchronizedHidDevice(),
                     this.getPOSKeyboardStrategyFactory().createLegacyModePOSKeyboardStrategy(POSKeyboardUtil.isDBCS(productID))
                  )
               );
               key = new HidHandleKey(usage, number, DevCats.KEYLOCK_DEVCAT);
               list.add(new HidKeylockHandleImp(key, hd.getSynchronizedHidDevice(), this.getKeylockStrategyFactory().createLegacyModeKbdKeylockStrategy()));
               key = new HidHandleKey(usage, number, DevCats.TONEINDICATOR_DEVCAT);
               if (POSKeyboardUtil.isSurepointDevice(usbInterface.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idProduct())) {
                  list.add(
                     new HidToneIndicatorHandleImp(
                        key, hd.getSynchronizedHidDevice(), this.getToneIndicatorStrategyFactory().createLegacyModeSurepointToneStrategy()
                     )
                  );
               } else {
                  list.add(
                     new HidToneIndicatorHandleImp(key, hd.getSynchronizedHidDevice(), this.getToneIndicatorStrategyFactory().createLegacyModeKbdToneStrategy())
                  );
               }
            }
            break;
         case 5376:
            if (this.initializer.isSystemKeyboard(usbInterface.getUsbConfiguration().getUsbDevice())) {
               if (this.reconnectHidDevice(list, hd.getSynchronizedHidDevice(), usbInterface, DevCats.POSKEYBOARD_DEVCAT)) {
                  this.reconnectHidDevice(list, hd.getSynchronizedHidDevice(), usbInterface, DevCats.KEYLOCK_DEVCAT);
                  this.reconnectHidDevice(list, hd.getSynchronizedHidDevice(), usbInterface, DevCats.TONEINDICATOR_DEVCAT);
               } else {
                  int numberx = this.getNumber(usage);
                  productID = usbInterface.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idProduct();
                  if (POSKeyboardUtil.isBootMode(productID)) {
                     this.createBootModeHandles(productID, hd, usage, list, numberx);
                  } else {
                     this.createNonBootModeHandles(productID, hd, usage, list, numberx);
                  }
               }
               break;
            }
         case 4096:
         default:
            hd.disconnect();
            break;
         case 5632:
            if (!this.reconnectHidDevice(list, hd, usbInterface, DevCats.MSR_DEVCAT)) {
               key = new HidHandleKey(usage, this.getNumber(usage), DevCats.MSR_DEVCAT);
               list.add(new HidMSRHandleImp(key, hd, usbInterface.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idProduct()));
            }
            break;
         case 8704:
         case 9216:
         case 9472:
         case 9728:
            if (!this.reconnectHidDevice(list, hd, usbInterface, DevCats.LINEDISPLAY_DEVCAT)) {
               key = new HidHandleKey(usage, this.getNumber(usage), DevCats.LINEDISPLAY_DEVCAT);
               list.add(new HidLineDisplayHandleImp(key, hd));
            }
            break;
         case 10752:
            if (!this.reconnectHidDevice(list, hd, usbInterface, DevCats.LINEDISPLAY_DEVCAT)) {
               key = new HidHandleKey(usage, this.getNumber(usage), DevCats.LINEDISPLAY_DEVCAT);
               list.add(new HidAPALineDisplayHandleImp(key, hd));
            }
            break;
         case 13568:
            if (!this.reconnectPOSPrinter(list, hd.getSynchronizedHidDevice(), usbInterface)) {
               return this.createPOSPrinterHandleImps(usbInterface, hd);
            }
            break;
         case 14080:
            if (!this.reconnectHidDevice(list, hd, usbInterface, DevCats.POSPRINTER_DEVCAT)) {
               key = new HidHandleKey(usage, this.getNumber(usage), DevCats.POSPRINTER_DEVCAT);
               list.add(new Hid4689PrinterHandleImp(key, hd));
            }
            break;
         case 14336:
            if (!this.reconnectHidDevice(list, hd, usbInterface, DevCats.FISCALPRINTER_DEVCAT)) {
               key = new HidHandleKey(usage, this.getNumber(usage), DevCats.FISCALPRINTER_DEVCAT);
               list.add(new HidFiscalPrinterHandleImp(key, hd));
            }
            break;
         case 18944:
         case 19200:
            this.disableScanner(hd);
            if (!this.reconnectHidDevice(list, hd, usbInterface, DevCats.SCANNER_DEVCAT)) {
               key = new HidHandleKey(usage, this.getNumber(usage), DevCats.SCANNER_DEVCAT);
               list.add(new HidScannerHandleImp(key, hd));
            }
            break;
         case 20480:
            if (!this.reconnectHidDevice(list, hd, usbInterface, DevCats.HARDTOTALS_DEVCAT)) {
               key = new HidHandleKey(usage, this.getNumber(usage), DevCats.HARDTOTALS_DEVCAT);
               list.add(new HidHardTotalsHandleImp(key, hd));
            }
            break;
         case 21504:
            if (this.reconnectHidDevice(list, hd.getSynchronizedHidDevice(), usbInterface, DevCats.CASHDRAWER_DEVCAT)) {
               this.reconnectHidDevice(list, hd.getSynchronizedHidDevice(), usbInterface, DevCats.CASHDRAWER_DEVCAT);
            } else {
               this.createCashDrawerHandleImps(hd, usage, list);
            }
            break;
         case 28160:
            if (!this.reconnectHidDevice(list, hd, usbInterface, DevCats.SCALE_DEVCAT)) {
               key = new HidHandleKey(usage, this.getNumber(usage), DevCats.SCALE_DEVCAT);
               list.add(new HidScaleHandleImp(key, hd));
            }
      }

      return list;
   }

   private void checkNonBootKbd(HidFlashHandleImp hidFlashHandleImp) {
      for (HidHandleImp current : this.handleImpTmpList) {
         if (current instanceof HidBootModePOSKeyboardHandleImp) {
            HidPOSKeyboardHandleImp kbd = (HidPOSKeyboardHandleImp)current;
            if (kbd.isNonBootModeOn()) {
               kbd.getHandleImpGroup().add(hidFlashHandleImp);
            }
         }
      }
   }

   protected void createNonBootModeHandles(short productId, HidDevice hd, int usage, List list, int number) {
      HidHandleKey key = new HidHandleKey(usage, number, DevCats.POSKEYBOARD_DEVCAT);
      HidBootModePOSKeyboardHandleImp bootModePOSKeyboardHandleImp = new HidBootModePOSKeyboardHandleImp(
         key, hd.getSynchronizedHidDevice(), this.getPOSKeyboardStrategyFactory().createNonBootModePOSKeyboardStrategy(POSKeyboardUtil.isDBCS(productId))
      );
      list.add(bootModePOSKeyboardHandleImp);
      this.handleImpTmpList.add(bootModePOSKeyboardHandleImp);
      key = new HidHandleKey(usage, number, DevCats.KEYLOCK_DEVCAT);
      list.add(new HidKeylockHandleImp(key, hd.getSynchronizedHidDevice(), this.getKeylockStrategyFactory().createNonBootModeKbdKeylockStrategy()));
      key = new HidHandleKey(usage, number, DevCats.TONEINDICATOR_DEVCAT);
      list.add(new HidToneIndicatorHandleImp(key, hd.getSynchronizedHidDevice(), this.getToneIndicatorStrategyFactory().createNonBootModeKbdToneStrategy()));
   }

   protected void createBootModeHandles(short productId, HidDevice hd, int usage, List list, int number) {
      HidHandleKey key = new HidHandleKey(usage, number, DevCats.POSKEYBOARD_DEVCAT);
      list.add(
         new HidBootModePOSKeyboardHandleImp(
            key, hd.getSynchronizedHidDevice(), this.getPOSKeyboardStrategyFactory().createBootModePOSKeyboardStrategy(POSKeyboardUtil.isDBCS(productId))
         )
      );
      key = new HidHandleKey(usage, number, DevCats.KEYLOCK_DEVCAT);
      list.add(new HidKeylockHandleImp(key, hd.getSynchronizedHidDevice(), this.getKeylockStrategyFactory().createBootModeKbdKeylockStrategy()));
      key = new HidHandleKey(usage, number, DevCats.TONEINDICATOR_DEVCAT);
      list.add(new HidToneIndicatorHandleImp(key, hd.getSynchronizedHidDevice(), this.getToneIndicatorStrategyFactory().createBootModeKbdToneStrategy()));
   }

   protected void createCashDrawerHandleImps(HidDevice hd, int usage, List list) {
      if (this.tracer.isOn()) {
         this.tracer.println("->createCashDrawerHandleImps()");
      }

      byte[] statusCmd = new byte[]{32};
      List arrayList = new ArrayList();
      HidListener hL = new UsbHandleImpFactory$1(this, arrayList, statusCmd);
      hd.addHidListener(hL);

      try {
         hd.setReport((byte)2, (byte)0, (short)((byte)statusCmd.length), statusCmd);
         hd.setReport((byte)2, (byte)0, (short)((byte)statusCmd.length), statusCmd);
         synchronized (statusCmd) {
            try {
               statusCmd.wait(2000L);
            } catch (InterruptedException var15) {
            }
         }
      } catch (HidException var17) {
         this.tracer.print(var17);
      } finally {
         hd.removeHidListener(hL);
      }

      if (arrayList.isEmpty()) {
         if (this.tracer.isOn()) {
            this.tracer.println("No response for Cash Drawer, do not create Handles");
         }
      } else {
         byte[] status = (byte[])arrayList.get(0);
         if (!PosjUtil.isBitSelected(status[0], 4)) {
            HidHandleKey key = new HidHandleKey(usage, 0, DevCats.CASHDRAWER_DEVCAT);
            list.add(new HidCashDrawerHandleImp(key, hd, 1));
         }

         if (!PosjUtil.isBitSelected(status[0], 3)) {
            HidHandleKey key = new HidHandleKey(usage, 1, DevCats.CASHDRAWER_DEVCAT);
            list.add(new HidCashDrawerHandleImp(key, hd, 2));
         }

         if (this.tracer.isOn()) {
            this.tracer.println("<-createCashDrawerHandleImps()");
         }
      }
   }

   protected List createGenericDesktopHandleImps(UsbInterface usbInterface, HidDevice hd, int usage) throws HidException {
      List list = new LinkedList();
      HidHandleKey key = null;
      switch ((short)usage) {
         case 6:
            if (!this.reconnectHidDevice(list, hd, usbInterface, DevCats.POSKEYBOARD_DEVCAT)) {
               key = new HidHandleKey(this.getUsagePage(usage), this.getUsageID(usage), DevCats.POSKEYBOARD_DEVCAT);
               list.add(new HidPOSKeyboardHandleImp(key, hd, this.getPOSKeyboardStrategyFactory().createBootModePOSKeyboardStrategy(false)));
            }
            break;
         default:
            hd.disconnect();
      }

      return list;
   }

   protected int getUsage(HidDevice hd) throws HidException {
      byte[] report = null;
      HidException hidException = null;
      int u = 0;
      int i = 0;
      int attempts = 20;

      do {
         try {
            report = hd.getDescriptor((byte)34, (byte)0);
            hidException = null;
         } catch (HidException var10) {
            hidException = var10;

            try {
               Thread.sleep(1000L);
            } catch (InterruptedException var9) {
            }
         }
      } while ((null == report || 6 > report.length) && 0 < attempts--);

      if (null == report) {
         throw new HidException("Could not get usage : " + (null == hidException ? "" : hidException.getMessage()));
      } else if (6 > report.length) {
         throw new HidException("Usage too short (length " + report.length + ")");
      } else {
         switch (report[i++]) {
            case 5:
               u |= 0xFF0000 & report[i++] << 16;
               break;
            case 6:
               u |= 0xFF0000 & report[i++] << 16;
               u |= 0xFF000000 & report[i++] << 24;
               break;
            default:
               this.tracer.println("Invalid UsagePage prefix byte : " + report[i - 1] + "... Ignoring!!");
         }

         switch (report[i++]) {
            case 9:
               u |= 255 & report[i++];
               break;
            case 10:
               u |= 255 & report[i++];
               u |= 0xFF00 & report[i++] << 8;
               break;
            default:
               this.tracer.println("Invalid Usage ID prefix byte : " + report[i - 1] + "... Ignoring!!");
         }

         return u;
      }
   }

   protected short getUsagePage(int usage) {
      return (short)(usage >> 16);
   }

   protected short getUsageID(int usage) {
      return (short)(usage & 65535);
   }

   protected short getUsagePage(HidDevice hd) throws HidException {
      return this.getUsagePage(this.getUsage(hd));
   }

   protected short getUsageID(HidDevice hd) throws HidException {
      return this.getUsageID(this.getUsage(hd));
   }

   protected HidKeylockStrategy.Factory getKeylockStrategyFactory() {
      if (this.keylockStrategyFactory == null) {
         this.keylockStrategyFactory = new DefaultHidKeylockStrategy.Factory();
      }

      return this.keylockStrategyFactory;
   }

   protected HidToneIndicatorStrategy.Factory getToneIndicatorStrategyFactory() {
      if (this.toneStrategyFactory == null) {
         this.toneStrategyFactory = new DefaultHidToneIndicatorStrategy.Factory();
      }

      return this.toneStrategyFactory;
   }

   protected HidPOSKeyboardStrategy.Factory getPOSKeyboardStrategyFactory() {
      if (this.kbdStrategyFactory == null) {
         this.kbdStrategyFactory = new DefaultHidPOSKeyboardStrategy.Factory();
      }

      return this.kbdStrategyFactory;
   }

   protected boolean ignoreUsbInterface(UsbInterface usbInterface) {
      return 3 != usbInterface.getUsbInterfaceDescriptor().bInterfaceClass()
         ? true
         : this.initializer.isSystemKeyboard(usbInterface.getUsbConfiguration().getUsbDevice())
            && 0 == usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber();
   }

   private boolean reconnectPOSPrinter(List list, HidDevice hd, UsbInterface iface) {
      if (!this.reconnectHidDevice(list, hd, iface, DevCats.POSPRINTER_DEVCAT)) {
         return false;
      } else {
         for (int i = 0; i < list.size(); i++) {
            try {
               Hid4610PrinterHandleImp h4phI = (Hid4610PrinterHandleImp)list.get(i);
               Iterator iterator = h4phI.getSecondaryHandleImps();

               while (iterator.hasNext()) {
                  HidHandleImp hhI = (HidHandleImp)iterator.next();
                  this.tracer.println("Reusing HidHandleImp type " + hhI.getDevCat() + " from POSPrinter");
                  list.add(hhI);
               }
            } catch (ClassCastException var8) {
            }
         }

         return true;
      }
   }

   private List createPOSPrinterHandleImps(UsbInterface usbInterface, HidDevice hidDevice) throws HidException {
      HidDevice hd = hidDevice.getUnsynchronizedHidDevice();
      byte type = this.areSubDevicesPresent(hd);
      List list = new LinkedList();
      int usage = this.getUsage(hd);
      int number = this.getNumber(usage);
      HidHandleKey key = new HidHandleKey(usage, number, DevCats.POSPRINTER_DEVCAT);
      Hid4610PrinterHandleImp h4phI = new Hid4610PrinterHandleImp(key, hd, type);
      h4phI.setPrinterID_microcodeLevel(this.getPrinterID_microcodeLevel());
      list.add(h4phI);
      if (this.isMicrPresent) {
         HidHandleKey micrKey = new HidHandleKey(usage, number, DevCats.MICR_DEVCAT);
         IBM4610MICRImp micrLinker = new IBM4610MICRImp(micrKey, h4phI);
         HandleImp h4mhI = new HidPOSPrinterMICRHandleImp(micrKey, hd, micrLinker);
         micrLinker.setHandleImp(h4mhI);
         h4phI.addDevice(micrKey, micrLinker);
         list.add(h4mhI);
      }

      HidHandleKey cd1Key = new HidHandleKey(usage, this.cashDrawer4610DevNum++, DevCats.CASHDRAWER_DEVCAT);
      IBM4610CashDrawerImp cd1Linker = new IBM4610CashDrawerImp(cd1Key, h4phI, 1);
      HandleImp h4cdhI1 = new HidPOSPrinterCashDrawerHandleImp(cd1Key, hd, cd1Linker);
      cd1Linker.setHandleImp(h4cdhI1);
      h4phI.addDevice(cd1Key, cd1Linker);
      list.add(h4cdhI1);
      HidHandleKey cd2Key = new HidHandleKey(usage, this.cashDrawer4610DevNum++, DevCats.CASHDRAWER_DEVCAT);
      IBM4610CashDrawerImp cd2Linker = new IBM4610CashDrawerImp(cd2Key, h4phI, 2);
      HandleImp h4cdhI2 = new HidPOSPrinterCashDrawerHandleImp(cd2Key, hd, cd2Linker);
      cd2Linker.setHandleImp(h4cdhI2);
      h4phI.addDevice(cd2Key, cd2Linker);
      list.add(h4cdhI2);
      if (this.isTIPresent) {
         HidHandleKey tiKey = new HidHandleKey(usage, number, DevCats.TONEINDICATOR_DEVCAT);
         IBMPrinterToneIndicatorImp ti = new IBM4610SSTToneIndicatorImp(h4phI);
         HidPOSPrinterToneIndicatorHandleImp hi = new HidPOSPrinterToneIndicatorHandleImp(tiKey, hd, ti);
         h4phI.addDevice(tiKey, hi);
         list.add(hi);
      }

      if (this.isCheckScannerPresent) {
         HidHandleKey csKey = new HidHandleKey(usage, number, DevCats.CHECKSCANNER_DEVCAT);
         IBM4610CheckScannerImp csLinker = new IBM4610CheckScannerImp(csKey, h4phI);
         HidPOSPrinterCheckScannerHandleImp hi = new HidPOSPrinterCheckScannerHandleImp(csKey, hd, csLinker);
         csLinker.setHandleImp(hi);
         h4phI.addDevice(csKey, csLinker);
         list.add(hi);
      }

      return list;
   }

   private void disableScanner(HidDevice hd) throws HidException {
      byte[] disableCmd = HidScannerHandleImp.DISABLE_SCANNER_CMD;
      List list = new Vector();
      HidListener hL = new UsbHandleImpFactory$2(this, list);
      hd.addHidListener(hL);
      int count = 0;

      do {
         hd.setReport((byte)2, (byte)0, (short)disableCmd.length, disableCmd);
         count++;

         try {
            if (list.size() == 0) {
               Thread.sleep(100L);
            }
         } catch (InterruptedException var7) {
         }
      } while (0 == list.size() && count <= 3);

      this.tracer.println("disableScanner Listsize" + list.size() + " count " + count);
      hd.removeHidListener(hL);
   }

   private byte areSubDevicesPresent(HidDevice hd) throws HidException {
      byte[] resetCmd = new byte[]{2, 7, 0, 1, 0, 0, 0, 0, 64, 0};
      byte[] idRequest = new byte[]{2, 7, 0, 1, 0, 0, 0, 0, 0, 1};
      List list = new Vector();
      int resetWaitTime = 5;
      int idWaitTime = 5;
      boolean gotStatus = false;
      byte type = 0;
      int printer_ID = -1;
      int printermicrocodeLevel = -1;
      HidListener hL = new UsbHandleImpFactory$3(this, list);
      hd.addHidListener(hL);
      hd.setReport((byte)2, (byte)0, (short)resetCmd.length, resetCmd);

      do {
         try {
            Thread.sleep(3000L);
         } catch (InterruptedException var15) {
         }
      } while (0 == list.size() && 0 < resetWaitTime--);

      hd.setReport((byte)2, (byte)0, (short)idRequest.length, idRequest);

      do {
         for (int i = 0; i < list.size(); i++) {
            byte[] data = ((ReportEvent)list.get(i)).getData();
            if (13 < data.length && 1 == (1 & data[9])) {
               data = hd.getReport((byte)3, (byte)0);
               if (this.tracer.isOn()) {
                  this.tracer.println(2, Util.toFormatedHexString(data));
               }

               gotStatus = true;
               if (0 == data[12]) {
                  throw new HidException("4610 TI1/2 not yet supported");
               }

               this.isMicrPresent = PosjUtil.isBitSelected(data[13], 0) || data[11] == 49;
               this.isTIPresent = data[12] == 3 || data[12] == 4 || data[12] == 7;
               type = data[11];
               if (data[11] == 49 || data[11] == 48 && PosjUtil.isBitSelected(data[14], 1) || data[11] == 8) {
                  this.isCheckScannerPresent = true;
               }

               this.printerID_microcodeLevel = -1;
               byte var16;
               if (data[11] != 49 && (data[11] != 48 || data[12] != 1 || (data[14] & 2) != 2)) {
                  var16 = data[12];
               } else {
                  var16 = 9;
               }

               int var18 = data[15];
               var16 <<= 8;
               this.printerID_microcodeLevel = var18 | var16;
               break;
            }
         }

         if (!gotStatus) {
            try {
               Thread.sleep(1000L);
            } catch (InterruptedException var14) {
            }
         }
      } while (!gotStatus && 0 < idWaitTime--);

      if (!gotStatus) {
         throw new HidException("Printer did not respond to ID request");
      } else {
         hd.removeHidListener(hL);
         return type;
      }
   }

   private int getPrinterID_microcodeLevel() {
      return this.printerID_microcodeLevel;
   }

   private int getNumber(int usage) {
      Integer key = new Integer(usage);
      int number = 0;
      if (this.numberTable.containsKey(key)) {
         number = (Integer)this.numberTable.get(key);
         number++;
      }

      this.numberTable.put(key, new Integer(number));
      return number;
   }

   private void addToTable(HidHandleImp hhi) {
      String key = hhi.getDevCat().toString();
      if (!this.hidHandleImpTable.containsKey(key)) {
         this.hidHandleImpTable.put(key, new LinkedList());
      }

      List list = (List)this.hidHandleImpTable.get(key);
      if (!list.contains(hhi)) {
         list.add(hhi);
      }
   }

   private boolean reconnectHidDevice(List reconnectList, HidDevice hd, UsbInterface iface, DevCat dc) {
      String key = dc.toString();
      if (!this.hidHandleImpTable.containsKey(key)) {
         return false;
      } else {
         List list = (List)this.hidHandleImpTable.get(key);

         for (int i = 0; i < list.size(); i++) {
            HidHandleImp hhi = (HidHandleImp)list.get(i);
            boolean online = false;

            try {
               online = hhi.getHandle().isOnline();
            } catch (NullPointerException var15) {
               continue;
            }

            String sn = hhi.getSerialNumber();
            if (null == sn) {
               sn = "";
            }

            try {
               String newSn = iface.getUsbConfiguration().getUsbDevice().getSerialNumberString();
               if (this.tracer.isOn()) {
                  this.tracer.println("Handle=" + hhi.getHandle().getName() + " (S/N:'" + sn + "', isOnline=" + online + ")");
                  this.tracer.println(3, "new UsbDevice S/N:'" + newSn + "'");
               }

               if (!online && sn.equals(newSn)) {
                  this.tracer.println("Reconnecting interface to existing " + hhi.getDevCat() + " type Handle");

                  try {
                     hhi.reattachHidDevice(hd);
                     reconnectList.add(hhi);
                     return true;
                  } catch (HandleException var13) {
                     this.tracer.println("Could not reconnect interface to existing Handle : " + var13.getMessage());
                     return false;
                  }
               }
            } catch (Exception var14) {
               this.tracer.println("Could not access serial number string" + var14.getMessage());
               return false;
            }
         }

         return false;
      }
   }

   private boolean isKnownPosDevice(UsbDevice device) {
      return UsbUtil.getInstance().isKnownPosDevice(device.getUsbDeviceDescriptor().idVendor(), device.getUsbDeviceDescriptor().idProduct());
   }

   private class ProcessDeviceListener implements UsbDeviceListener {
      private ProcessDeviceListener() {
      }

      public void dataEventOccurred(UsbDeviceDataEvent event) {
      }

      public void errorEventOccurred(UsbDeviceErrorEvent event) {
      }

      public void usbDeviceDetached(UsbDeviceEvent event) {
         UsbHandleImpFactory.this.processedDevices.remove(event.getUsbDevice());
      }
   }
}
