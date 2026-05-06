package com.ibm.posj.bus.hid.javaxusb;

import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.HidListener;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.SleepPolicy;
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
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;

public class UsbHandleImpFactory {
   private UsbInitializationManager initializer = null;
   private UsbHandlePopulator usbHandlePopulator = null;
   private HidDeviceUsbFactory factory = new HidDeviceUsbFactory();
   private Hashtable numberTable = new Hashtable();
   private List handleImpTmpList = new ArrayList();
   private List processedDevices = new Vector();
   private List switched = null;
   private Hashtable hidHandleImpTable = new Hashtable();
   private HidKeylockStrategy.Factory keylockStrategyFactory = null;
   private HidToneIndicatorStrategy.Factory toneStrategyFactory = null;
   private HidPOSKeyboardStrategy.Factory kbdStrategyFactory = null;
   private Tracer tracer = TracerFactory.getInstance().createTracer("HID", "UsbHandleImpFactory");
   private boolean isCheckScannerPresent = false;
   private boolean isMicrPresent = false;
   private boolean isTIPresent = false;
   private int cashDrawer4610DevNum = 4;
   private int printerID_microcodeLevel = -1;
   private final List EMPTY_LIST = new ArrayList();
   public static final byte TI4_EMULATION_BIT_POSITION = 1;
   public static boolean REMOVE_MOD4_EMU = false;
   public static byte[] MOD4_MCT = null;
   private static byte PRINTER_REPORT_ID = 53;
   private static byte MAX_RETRIES = 2;

   public UsbHandleImpFactory(UsbHandlePopulator populator) {
      this.usbHandlePopulator = populator;
      this.initializer = new UsbInitializationManager(populator);
   }

   public synchronized List createHandleImps(UsbInterface usbInterface) throws HidException {
      byte bInterfaceNumber = usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber();
      String bInterfaceNumberString = Integer.toString(Util.unsignedInt(bInterfaceNumber));
      if (this.tracer.isOn()) {
         this.tracer.println("Creating HandleImps for UsbInterface " + bInterfaceNumberString);
      }

      short pId = usbInterface.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idProduct();
      short vId = usbInterface.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idVendor();
      byte interfaceNumber = usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber();
      if (!this.ignoreUsbInterface(usbInterface) && !UsbIDUtil.getInstance().ignoreInterface(vId, pId, interfaceNumber)) {
         HidDevice hidDevice = this.factory.createHidDevice(usbInterface);

         try {
            hidDevice.connect();
         } catch (HidException var12) {
            if (this.tracer.isOn()) {
               this.tracer.println("Could not connect HidDevice : " + var12.getMessage());
               this.tracer.print(var12);
            }

            hidDevice.disconnect();
            throw var12;
         }

         int usage = 0;

         try {
            usage = this.getUsage(hidDevice);
         } catch (HidException var11) {
            if (this.tracer.isOn()) {
               this.tracer.println("Could not get Usage : " + var11.getMessage());
            }

            hidDevice.disconnect();
            throw var11;
         }

         try {
            short usagePage = this.getUsagePage(usage);
            if (this.tracer.isOn()) {
               this.tracer.println("UsagePage =" + Util.toHexString(usagePage) + ", Usage =" + Util.toHexString((short)usage));
            }

            switch (usagePage) {
               case -187:
                  return this.createIbmHandleImps(usbInterface, hidDevice, usage);
               case 1:
                  return this.createGenericDesktopHandleImps(usbInterface, hidDevice, usage);
               default:
                  if (this.tracer.isOn()) {
                     this.tracer.println("Unrecognized UsbInterface UsagePage 0x" + Util.toHexString(usagePage) + ", ignoring.");
                  }

                  hidDevice.disconnect();
                  return new ArrayList();
            }
         } catch (HidException var10) {
            hidDevice.disconnect();
            throw var10;
         }
      } else {
         if (this.tracer.isOn()) {
            this.tracer.println("Ignoring interface.");
         }

         return new ArrayList();
      }
   }

   public synchronized List createHandleImps(UsbDevice usbDevice) {
      if (usbDevice.isUsbHub()) {
         return this.createHandleImps((UsbHub)usbDevice);
      } else {
         UsbDeviceDescriptor usbDeviceDescriptor = usbDevice.getUsbDeviceDescriptor();
         short idVendor = usbDeviceDescriptor.idVendor();
         short idProduct = usbDeviceDescriptor.idProduct();
         String idVendorString = "0x" + Util.toHexString(idVendor);
         String idProductString = "0x" + Util.toHexString(idProduct);
         String idString = "<" + idVendorString + "," + idProductString + ">";
         String serialNumberString = "";
         if (this.tracer.isOn()) {
            this.tracer.println("Creating HandleImps for UsbDevice " + idString + " S/N " + serialNumberString);
         }

         if (!this.isKnownPosDevice(usbDevice)) {
            if (this.tracer.isOn()) {
               this.tracer.println("UsbDevice " + idString + " is not known POS device, skipping.");
            }

            return this.EMPTY_LIST;
         } else if (this.processedDevices.contains(new Integer(usbDevice.hashCode()))) {
            if (this.tracer.isOn()) {
               this.tracer.println("Device (hashCode " + usbDevice.hashCode() + ") already processed, ignoring.");
            }

            return this.EMPTY_LIST;
         } else {
            if (this.tracer.isOn()) {
               this.tracer.println("Processing device (hashcode " + usbDevice.hashCode() + ")");
            }

            this.processedDevices.add(new Integer(usbDevice.hashCode()));

            try {
               serialNumberString = usbDevice.getSerialNumberString();
               if (this.tracer.isOn()) {
                  this.tracer.println("UsbDevice has SerialNumber '" + serialNumberString + "'");
               }
            } catch (UnsupportedEncodingException var16) {
               this.tracer.print(var16);
            } catch (UsbDisconnectedException var17) {
               if (this.tracer.isOn()) {
                  short pId = usbDevice.getUsbDeviceDescriptor().idProduct();
                  this.tracer.print(var17);
                  this.tracer
                     .println(
                        "UsbDisconnectedException " + var17.toString() + " during usbDevice.getSerialNumberString() ignoring device:" + Util.toHexString(pId)
                     );
               }

               return this.EMPTY_LIST;
            } catch (Exception var18) {
               if (this.tracer.isOn()) {
                  this.tracer.println("Could not get device SerialNumber : " + var18.getMessage());
                  this.tracer.print(var18);
               }
            }

            if (!this.initializer.initializeUsbDevice(usbDevice)) {
               if (this.tracer.isOn()) {
                  this.tracer.println("Device not initialized, deferring to initializer.");
               }

               return this.EMPTY_LIST;
            } else {
               List list = new LinkedList();

               Iterator usbInterfaces;
               try {
                  usbInterfaces = usbDevice.getActiveUsbConfiguration().getUsbInterfaces().iterator();
               } catch (NullPointerException var13) {
                  if (this.tracer.isOn()) {
                     this.tracer.println("Got NPE while getting UsbInterfaces : " + var13.getMessage());
                     this.tracer.print(var13);
                  }

                  return this.EMPTY_LIST;
               }

               try {
                  while (usbInterfaces.hasNext()) {
                     list.addAll(this.createHandleImps((UsbInterface)usbInterfaces.next()));
                  }
               } catch (UsbDisconnectedException var14) {
                  if (this.tracer.isOn()) {
                     this.tracer
                        .println(
                           "UsbDisconnectedException during HandleImp creation, disconnecting "
                              + list.size()
                              + " HandleImps and ignoring device : "
                              + var14.getMessage()
                        );
                  }

                  for (int i = 0; i < list.size(); i++) {
                     ((HidHandleImp)list.get(i)).getHidDevice().disconnect();
                  }

                  return this.EMPTY_LIST;
               } catch (Exception var15) {
                  if (this.tracer.isOn()) {
                     this.tracer
                        .println(
                           "Exception during HandleImp creation, disconnecting " + list.size() + " HandleImps and ignoring device : " + var15.getMessage()
                        );
                  }

                  for (int i = 0; i < list.size(); i++) {
                     ((HidHandleImp)list.get(i)).getHidDevice().disconnect();
                  }

                  return this.EMPTY_LIST;
               }

               for (int i = 0; i < list.size(); i++) {
                  HidHandleImp hidHandleImp = (HidHandleImp)list.get(i);
                  hidHandleImp.setSerialNumber(serialNumberString);
                  hidHandleImp.setVendorID(idVendor);
                  hidHandleImp.setProductID(idProduct);
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
                           + " HandleImps created for device "
                           + idString
                           + " bcdLevel 0x"
                           + Util.toHexString(usbDevice.getUsbDeviceDescriptor().bcdDevice())
                           + " S/N "
                           + serialNumberString
                     );
               }

               return list;
            }
         }
      }
   }

   public synchronized List createHandleImps(UsbHub usbHub) {
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
               list.add(new HidFlashHandleImp(key, hd));
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
               } else if (18454 == usbInterface.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().idProduct()) {
                  list.add(
                     new HidToneIndicatorHandleImp(
                        key, hd.getSynchronizedHidDevice(), this.getToneIndicatorStrategyFactory().createLegacyMode485K03ToneStrategy()
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
         case 13056:
            if (REMOVE_MOD4_EMU) {
               String sn = "";

               try {
                  sn = usbInterface.getUsbConfiguration().getUsbDevice().getSerialNumberString();
               } catch (Exception var10) {
                  if (this.tracer.isOn()) {
                     this.tracer.println("Failed getting serial number before mod4 switch");
                     this.tracer.print(var10);
                  }
               }

               this.switchMod4Emulation(hd, sn);
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
         for (int i = 0; i < MAX_RETRIES; i++) {
            synchronized (statusCmd) {
               hd.setReport((byte)2, (byte)0, (short)((byte)statusCmd.length), statusCmd);

               try {
                  statusCmd.wait(1000L);
               } catch (InterruptedException var16) {
               }
            }
         }
      } catch (HidException var18) {
         this.tracer.print(var18);
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
      int u = 0;
      int i = 0;
      report = hd.getDescriptor((byte)34, (byte)0);
      if (6 > report.length) {
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
                  if (this.tracer.isOn()) {
                     this.tracer.println("Reusing HidHandleImp type " + hhI.getDevCat() + " from POSPrinter");
                  }

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
      UsbIDUtil.getInstance();
      if (!UsbIDUtil.isPrinterAdapterResetNeeded(usbInterface.getUsbConfiguration().getUsbDevice().getUsbDeviceDescriptor().bcdDevice())) {
         if (this.tracer.isOn()) {
            this.tracer.println("Doing a Hid Read instead of reset the adapter");
         }

         hd.getReport((byte)3, PRINTER_REPORT_ID);
      }

      byte type = 99;

      try {
         type = this.areSubDevicesPresent(hd);
      } catch (Exception var20) {
      }

      List list = new LinkedList();
      int usage = this.getUsage(hd);
      int number = this.getNumber(usage);
      HidHandleKey key = new HidHandleKey(usage, number, DevCats.POSPRINTER_DEVCAT);
      Hid4610PrinterHandleImp h4phI = new Hid4610PrinterHandleImp(key, hd, type);
      String sn = "";

      try {
         sn = usbInterface.getUsbConfiguration().getUsbDevice().getSerialNumberString();
      } catch (Exception var21) {
         if (this.tracer.isOn()) {
            this.tracer.println("Failed getting serial number before mod4 switch check");
            this.tracer.print(var21);
         }
      }

      if (null != this.switched && this.switched.contains(sn)) {
         h4phI.setFormerMod4(true);
      }

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

   public void switchMod4Emulation(HidDevice hd, String sn) throws HidException {
      if (null == this.switched) {
         this.switched = new ArrayList(1);
      }

      if (this.switched.size() <= 0 || !this.switched.contains(sn)) {
         this.switched.add(sn);
         BooleanMonitor flag = new BooleanMonitor(false);
         HidListener hL = new UsbHandleImpFactory$2(this, flag);
         hd.addHidListener(hL);
         byte[] switch1 = new byte[]{2, 7, 0, 1, 0, 0, 0, 11, 32, 0};
         hd.setReport((byte)2, (byte)0, (short)switch1.length, switch1);
         byte[] data = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0};

         try {
            flag.waitForTrue(5000);
            data = hd.getReport((byte)3, (byte)0);
         } catch (Exception var11) {
            this.tracer.print(var11);
         } finally {
            hd.removeHidListener(hL);
         }

         if (data != null) {
            MOD4_MCT = new byte[]{data[7], data[8]};
            if (this.tracer.isOn()) {
               this.tracer.println(3, "MOD 4 MCT Bits " + Util.toFormatedHexString(MOD4_MCT));
            }
         }

         byte[] switch4 = new byte[]{2, 9, 0, 1, 0, 0, 0, 27, 77, 32, -1, -1};
         hd.setReport((byte)2, (byte)0, (short)switch4.length, switch4);
         SleepPolicy.sleep(7500L);
      }
   }

   private void disableScanner(HidDevice hd) throws HidException {
      byte[] disableCmd = HidScannerHandleImp.DISABLE_SCANNER_CMD;
      List list = new Vector();
      HidListener hL = new UsbHandleImpFactory$3(this, list);
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
      int printermicrocodeLevel = -1;
      HidListener hL = new UsbHandleImpFactory$4(this, list);
      hd.addHidListener(hL);
      hd.setReport((byte)2, (byte)0, (short)resetCmd.length, resetCmd);

      do {
         try {
            Thread.sleep(3000L);
         } catch (InterruptedException var15) {
         }
      } while (0 == list.size() && 0 < resetWaitTime--);

      list.clear();
      hd.setReport((byte)2, (byte)0, (short)idRequest.length, idRequest);
      int printer_ID = -1;

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
               byte var17;
               if (data[11] == 49 || data[11] == 48 && PosjUtil.isBitSelected(data[14], 1)) {
                  type = 49;
                  if (PosjUtil.isBitSelected(data[14], 2)) {
                     if (PosjUtil.isBitSelected(data[14], 4)) {
                        var17 = 11;
                        this.isCheckScannerPresent = false;
                     } else {
                        var17 = 10;
                        this.isCheckScannerPresent = true;
                     }
                  } else {
                     var17 = 9;
                     this.isCheckScannerPresent = true;
                  }
               } else {
                  type = 48;
                  var17 = data[12];
               }

               this.printerID_microcodeLevel = -1;
               int var16 = data[15];
               var17 <<= 8;
               this.printerID_microcodeLevel = var16 | var17;
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
      return UsbIDUtil.getInstance().isKnownPosDevice(device.getUsbDeviceDescriptor().idVendor(), device.getUsbDeviceDescriptor().idProduct());
   }
}
