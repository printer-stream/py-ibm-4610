package com.ibm.posj.bus.rs485;

import com.ibm.embedded.EmbeddedManager;
import com.ibm.embedded.GetSlotInfo;
import com.ibm.jsio.SioDevice;
import com.ibm.jsio.SioException;
import com.ibm.jsio.SioHostManager;
import com.ibm.jsio.SioServices;
import com.ibm.jsio.SioSlot;
import com.ibm.jsio.event.SioDeviceListener;
import com.ibm.jsio.event.SioServicesEvent;
import com.ibm.jsio.event.SioServicesListener;
import com.ibm.jsio.os.SioIrpRecyclable;
import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.SleepPolicy;
import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.HandleFactory;
import com.ibm.posj.HandleFactoryException;
import com.ibm.posj.HandleRegistry;
import com.ibm.posj.PosException;
import com.ibm.posj.PosSystem;
import com.ibm.posj.bus.AbstractHandlePopulator;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.IBM4610PrinterHandleImp;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.PosjUtil;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Rs485HandlePopulator extends AbstractHandlePopulator {
   private final HandleImp handleImp = null;
   private final HandleFactoryException hfE = null;
   private SioServices sioServices = null;
   private SioSlot sioSlot = null;
   private SioServicesListener sioServicesListener = new Rs485HandlePopulator.SioServicesL(null);
   private Rs485HandleImpFactory hiFactory = null;
   private boolean isTIPresent = false;
   private boolean isMicrPresent = false;
   private boolean isCheckScannerPresent = false;
   private int printerID_microcodeLevel = -1;
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevBuses.RS485_DEVBUS.getName(), "Rs485HandlePopulator");
   public static final String RS485_HANDLE_POPULATOR_NAME_STRING = "RS485 Handle Populator";
   public static final byte[] REINITIALIZE_PRINTER_CMD = new byte[]{0, 64, 0};
   public static final byte[] REQUEST_PRINTER_ID_CMD = new byte[]{0, 0, 1};
   public static final byte[] REQUEST_4689_PRINTER_ID_CMD = new byte[]{0, 0, 1, 0, 0, 0, 1};
   public static final byte IBM_4689_PRINTERID_BIT_POSITION = 9;
   public static final byte IBM_4689_PRINTERECLEVEL_BIT_POSITION = 12;
   public static final byte MICR_PRESENT_BIT_POSITION = 0;
   public static final byte TI4_EMULATION_BIT_POSITION = 1;
   public static final byte PRINTER_4610_DEVICE_TYPE = 8;
   public static final byte PRINTER_4610_DEVICE_ID = 9;
   public static final byte PRINTER_4610_DEVICE_FEATURE_BYTE_1 = 10;
   public static final byte PRINTER_4610_DEVICE_FEATURE_BYTE_2 = 11;
   public static final byte PRINTER_4610_EC_LEVEL = 12;
   public static final byte[] REQUEST_STATUS_CMD = new byte[]{0, 32};
   public static final byte CD1_DRIVER_SENSOR_BIT_POSITION = 4;
   public static final byte CD2_DRIVER_SENSOR_BIT_POSITION = 3;
   private final byte[] SCANNER_CONFIG_PERIODICALS_CMD = new byte[]{32, 8, 0, 0, 0, 0, 0, 0, 0, 0};
   private final byte[] SCANNER_CONFIG_CODE39_CMD = new byte[]{32, 34, 0, 0, 0, 0, 0, 0, 0, 0};
   private final byte[] SCANNER_CONFIG_LABEL_EXPANSION_CMD = new byte[]{32, 2, 4, 0, 0, 0, 0, 0, 0, 0};
   private final byte[] SCANNER_CONFIG_UPCEAN25_CMD = new byte[]{32, 10, 0, 0, 0, 0, 0, 0, 0, 0};
   private final byte[] SCANNER_REPORT_CONFIG_CMD = new byte[]{33};
   private final byte[] SCANNER_1520_CONFIG_CMD = new byte[]{49, -128};
   private final byte[] SCANNER_HHBCR2_CONFIG_CMD = new byte[]{32, 0, 112, 0, 0, 0};
   private final byte[] SCANNER_EC_LEVEL_REQ_CMD = new byte[]{0, -128};
   private final int SCANNER_CMD_REJECTED_BYTE = 1;
   private final int SCANNER_CMD_REJECTED_BIT = 7;
   private final int SCANNER_HW_ERROR_BYTE = 1;
   private final int SCANNER_HW_ERROR_BIT = 6;
   private final int SCANNER_READ_ERROR_BYTE = 1;
   private final int SCANNER_READ_ERROR_BIT = 4;
   private final int SCANNER_MODE_DATA_BYTE = 0;
   private final int SCANNER_MODE_DATA_BIT = 6;
   private final int SCANNER_FB_NOT_READY_BYTE = 0;
   private final int SCANNER_FB_NOT_READY_BIT = 7;
   private final int SCANNER_FB_CONFIG_SUCCESSFUL_BYTE = 2;
   private final int SCANNER_FB_CONFIG_SUCCESSFUL_BIT = 0;
   private final int SCANNER_FB_CONFIG_RESPONSE_FRAME_BYTE = 0;
   private final int SCANNER_FB_CONFIG_RESPONSE_FRAME_BIT = 1;
   private final int SCANNER_FB_CONFIG_COERCED_BYTE = 2;
   private final int SCANNER_FB_CONFIG_COERCED_BIT = 1;
   private final int SCANNER_EC_LEVEL_RESPONSE_BYTE = 0;
   private final int SCANNER_EC_LEVEL_RESPONSE_BIT = 0;
   public final byte[] SCALE_EXTENDED_STATUS_CMD = new byte[]{4};
   public final byte[] SCALE_EC_LEVEL_CMD = new byte[]{0, -128};
   public final byte[] SCALE_CONFIGURE_CMD = new byte[]{32, 0, 0, 0};
   public final byte SCALE_EXTENDED_STATUS_BIT = 2;
   public final byte SCALE_EC_LEVEL_BIT = 0;
   public final byte SCALE_HW_ERROR_BIT = 6;
   public final byte SCALE_CONFIG_SUCCESS_BIT = 0;
   public final byte SCALE_CMD_REJECT_BIT = 6;

   public Rs485HandlePopulator(HandleRegistry registry, HandleFactory factory, PosSystem.EventHelper eventHelper) {
      super(registry, factory, eventHelper);
   }

   public void start() throws PosException {
      if (!this.isStarted()) {
         super.start();
         this.init();
      }
   }

   public void stop() throws PosException {
      if (this.isStarted()) {
         super.stop();
      }
   }

   public String getName() {
      return "RS485 Handle Populator";
   }

   public DevBus getDevBus() {
      return DevBuses.RS485_DEVBUS;
   }

   protected void init() {
      if (this.tracer.isOn()) {
         this.tracer.println(2, "--> init()");
      }

      Iterator sioSlotIterator = null;

      try {
         this.sioServices = SioHostManager.getSioServices();
         sioSlotIterator = this.getSioSlots();
      } catch (Exception var3) {
         if (this.tracer.isOn()) {
            this.tracer.println(2, "Error while getting SioServices\\RootSlot : " + var3.toString());
            this.tracer.print(var3);
            this.tracer.println(2, "<-- init()");
         }

         return;
      }

      if (this.tracer.isOn()) {
         this.tracer
            .println(
               2,
               "ApiVersion : "
                  + this.sioServices.getApiVersion()
                  + ", ImpVersion : "
                  + this.sioServices.getImpVersion()
                  + ", ImpDescription : "
                  + this.sioServices.getImpDescription()
            );
         if (!sioSlotIterator.hasNext()) {
            this.tracer.println(2, "None JSIO devices found");
         }
      }

      this.sioServices.addSioServicesListener(this.sioServicesListener);

      while (sioSlotIterator.hasNext()) {
         this.sioSlot = (SioSlot)sioSlotIterator.next();
         this.addSioDevices(this.getAttachedSioDevices());
      }

      if (this.tracer.isOn()) {
         this.tracer.println(2, "<-- init()");
      }
   }

   protected Iterator getSioSlots() throws SioException, SecurityException {
      return this.sioServices.getRootSioSlot().getSioSlots();
   }

   protected Iterator getAttachedSioDevices() {
      return this.sioSlot.getAttachedSioDevices().iterator();
   }

   protected void addSioDevices(Iterator iterator) {
      while (iterator.hasNext()) {
         this.addSioDevice((SioDevice)iterator.next());
      }
   }

   protected void addSioDevice(SioDevice device) {
      new LinkedList();

      List deviceHandleList;
      try {
         deviceHandleList = this.createHandleImps(device);
      } catch (HandleFactoryException var4) {
         if (this.tracer.isOn()) {
            this.tracer
               .println(2, "-->addSioDevice() An error occurred when creating Handles for " + this.getSioDeviceString(device) + " : " + var4.toString() + "<--");
            this.tracer.print(var4);
         }

         return;
      }

      for (int i = 0; i < deviceHandleList.size(); i++) {
         this.getHandleRegistry().addHandle(((HandleImp)deviceHandleList.get(i)).getHandle());
      }
   }

   protected String getSioDeviceString(SioDevice sioDevice) {
      return "Slot: 0x"
         + Integer.toHexString(sioDevice.getSioSlot().getSioSlotNumber())
         + " Port: 0x"
         + Integer.toHexString(sioDevice.getPortNumber())
         + " Device: 0x"
         + Integer.toHexString(sioDevice.getDeviceNumber());
   }

   protected int getSioDeviceId(SioDevice sioDevice) {
      int sioDeviceID = 0;
      int tmp = Util.unsignedInt(sioDevice.getSioSlotNumber());
      sioDeviceID = tmp << 16;
      tmp = Util.unsignedInt(sioDevice.getPortNumber());
      tmp <<= 8;
      sioDeviceID |= tmp;
      tmp = Util.unsignedInt(sioDevice.getDeviceNumber());
      return sioDeviceID | tmp;
   }

   protected HandleFactoryException getException() {
      return this.hfE;
   }

   protected synchronized List createHandleImps(SioDevice sioDevice) throws HandleFactoryException {
      List list = new LinkedList();
      if (this.tracer.isOn()) {
         this.tracer.println(2, "--> createHandleImps() ");
         this.tracer.println(2, "Processing SioDevice <" + this.getSioDeviceString(sioDevice) + ">");
      }

      byte devNumber = sioDevice.getDeviceNumber();
      if (Rs485Util.hasCustomWaitTime(devNumber)) {
         sioDevice.setAddressWaitTime(Rs485Util.getCustomAdressWaitTime(devNumber));
         sioDevice.setInterByteWaitTime(Rs485Util.getCustomInterByteWaitTime(devNumber));
      }

      switch (this.getSioDeviceId(sioDevice)) {
         case 69904:
         case 69905:
         case 69916:
         case 69917:
            list.add(this.getRs485HandleImpFactory().createPOSKbd(sioDevice));
            list.add(this.getRs485HandleImpFactory().createKeylock(sioDevice));
            list.add(this.getRs485HandleImpFactory().createToneIndicator(sioDevice));
            break;
         case 69906:
         case 69907:
         case 69908:
         case 69909:
         case 69910:
         case 69911:
         case 69912:
         case 69914:
         case 69915:
         case 69918:
         case 69919:
         case 69920:
         case 69921:
         case 69926:
         case 69927:
         case 69928:
         case 69929:
         case 69934:
         case 69935:
         case 69936:
         case 69937:
         case 69939:
         case 69942:
         case 69943:
         case 69945:
         case 69946:
         case 69947:
         case 69948:
         case 69949:
         case 69950:
         case 69951:
         case 69952:
         case 69953:
         case 69954:
         case 69955:
         case 69956:
         case 69957:
         case 69958:
         case 69959:
         case 69964:
         case 69965:
         case 69966:
         case 69967:
         case 69968:
         case 69969:
         case 69970:
         case 69971:
         case 69973:
         case 69974:
         case 69975:
         case 69976:
         case 69977:
         case 69978:
         case 69979:
         case 69980:
         case 69981:
         case 69982:
         case 69983:
         case 69984:
         case 69985:
         case 69986:
         case 69987:
         case 69988:
         case 69989:
         case 69990:
         case 69991:
         case 69992:
         case 69993:
         case 69995:
         case 69996:
         case 69997:
         default:
            if (this.tracer.isOn()) {
               this.tracer.println(2, "Skipping Unknown Device");
            }
            break;
         case 69913:
            list.add(this.getRs485HandleImpFactory().createPOSKbd(sioDevice));
            list.add(this.getRs485HandleImpFactory().createToneIndicator(sioDevice));
            break;
         case 69922:
         case 69923:
         case 69924:
         case 69925:
            list.add(this.getRs485HandleImpFactory().createLineDisplay(sioDevice));
            break;
         case 69930:
         case 69931:
         case 69932:
         case 69933:
            list.add(this.getRs485HandleImpFactory().createAPALineDisplay(sioDevice));
            break;
         case 69938:
            if (this.tracer.isOn()) {
               this.tracer.println(2, "-->Creating a 4689 printer...");
            }

            this.get4689Status(sioDevice);
            Rs4854689PrinterHandleImp ptrHandleImp = this.getRs485HandleImpFactory().create4689POSPrinter(sioDevice);
            ptrHandleImp.setPrinterID_microcodeLevel(this.printerID_microcodeLevel);
            list.add(ptrHandleImp);
            if (this.tracer.isOn()) {
               this.tracer.println(2, "<--Created a 4689 printer...");
            }
         case 69940:
            break;
         case 69941:
            this.createPOSPrinter4610HandleImps(list, sioDevice);
            break;
         case 69944:
            list.add(this.getRs485HandleImpFactory().createFiscalPrinter(sioDevice));
            break;
         case 69960:
         case 69961:
            list.add(this.getRs485HandleImpFactory().createMSR(sioDevice));
            break;
         case 69962:
            list.add(this.getRs485HandleImpFactory().createScanner(sioDevice, this.getScanner4ADeviceId(sioDevice)));
            break;
         case 69963:
            list.add(this.getRs485HandleImpFactory().createScanner(sioDevice, this.getScanner4BDeviceId(sioDevice)));
            break;
         case 69972:
            this.createCashDrawerHandleImps(sioDevice, list);
            break;
         case 69994:
         case 69998:
            list.add(this.getRs485HandleImpFactory().createScale(sioDevice, this.getScaleType(sioDevice)));
      }

      if (this.tracer.isOn()) {
         this.tracer
            .println(2, "HandleImp creation successful, " + list.size() + " HandleImps created for SioDevice <" + this.getSioDeviceString(sioDevice) + ">");
         this.tracer.println(2, "<-- createHandleImps() ");
      }

      return list;
   }

   protected Rs485HandleImpFactory getRs485HandleImpFactory() {
      if (this.hiFactory == null) {
         this.hiFactory = new Rs485HandleImpFactory(this.getHandleFactory());
      }

      return this.hiFactory;
   }

   protected void createCashDrawerHandleImps(SioDevice device, List list) throws HandleFactoryException {
      boolean isCDAPresent = false;
      boolean isCDBPresent = false;
      SioDeviceListener listener = null;
      if (!this.ignoreCashDrawer()) {
         if (this.tracer.isOn()) {
            this.tracer.println(2, "-->Check Cash Drawer existance");
         }

         try {
            ByteBuffer buffer = new ByteBuffer();
            device.open();
            listener = new Rs485HandlePopulator$1(this, buffer);
            device.addSioDeviceListener(listener);
            device.syncSubmit(device.createSioIrp(REQUEST_STATUS_CMD, false));

            try {
               Thread.currentThread();
               Thread.sleep(300L);
            } catch (InterruptedException var19) {
            }

            if (buffer.getByteCount() >= 1) {
               this.tracer.println(Util.toFormatedHexString(buffer.getBytes()));
               if (!PosjUtil.isBitSelected(buffer.byteAt(0), 4)) {
                  isCDAPresent = true;
               }

               if (!PosjUtil.isBitSelected(buffer.byteAt(0), 3)) {
                  isCDBPresent = true;
               }
            }
         } catch (SioException var21) {
            if (this.tracer.isOn()) {
               this.tracer.print(var21);
            }
         } catch (Exception var22) {
            if (this.tracer.isOn()) {
               this.tracer.print(var22);
            }
         } finally {
            device.removeSioDeviceListener(listener);
            listener = null;

            try {
               if (device.isOpen()) {
                  device.close();
               }
            } catch (SioException var20) {
               if (this.tracer.isOn()) {
                  this.tracer.print(var20);
               }
            }
         }

         if (isCDAPresent) {
            list.add(this.getRs485HandleImpFactory().createCashDrawer(device, 0, 1));
         }

         if (isCDBPresent) {
            list.add(this.getRs485HandleImpFactory().createCashDrawer(device, 1, 2));
         }

         if (this.tracer.isOn()) {
            this.tracer.println(2, "<--Check Cash Drawer existance");
         }
      }
   }

   protected void createPOSPrinter4610HandleImps(List list, SioDevice device) throws HandleFactoryException {
      IBM4610PrinterHandleImp.PrinterInfo type = this.areSubDevicesPresent(device);
      Rs4854610PrinterHandleImp ptrHandleImp = this.getRs485HandleImpFactory().create4610POSPrinter(device, type);
      ptrHandleImp.setPrinterID_microcodeLevel(this.printerID_microcodeLevel);
      list.add(ptrHandleImp);
      if (this.isMicrPresent) {
         list.add(this.getRs485HandleImpFactory().create4610MICRHandleImp(device, ptrHandleImp));
      }

      list.add(this.getRs485HandleImpFactory().create4610CDHandleImp(device, ptrHandleImp, 1));
      list.add(this.getRs485HandleImpFactory().create4610CDHandleImp(device, ptrHandleImp, 2));
      if (this.isTIPresent) {
         list.add(this.getRs485HandleImpFactory().create4610TIHandleImp(device, ptrHandleImp));
      }

      if (this.isCheckScannerPresent) {
         list.add(this.getRs485HandleImpFactory().create4610CSHandleImp(device, ptrHandleImp));
      }
   }

   protected void get4689Status(SioDevice device) throws HandleFactoryException {
      int printer_ID = 1;
      int printermicrocodeLevel = -1;
      BooleanMonitor pOnline = new BooleanMonitor(false);
      SioDeviceListener listener = null;
      if (this.tracer.isOn()) {
         this.tracer.println(2, "-->Check 4689 Printer existance");
      }

      try {
         ByteBuffer buffer = new ByteBuffer();
         device.open();
         listener = new Rs485HandlePopulator$2(this, buffer, pOnline);
         device.addSioDeviceListener(listener);
         SioIrpRecyclable irp = (SioIrpRecyclable)device.createSioIrp(REQUEST_4689_PRINTER_ID_CMD, true);
         device.syncSubmit(irp);
         irp.recycle();
         int maxRetry = 0;

         do {
            SleepPolicy.sleep(1000L);
         } while (buffer.getByteCount() < 12 && maxRetry++ < 5);

         this.tracer.println(Util.toFormatedHexString(buffer.getBytes()));
         if (maxRetry >= 5) {
            throw new HandleFactoryException("NO 4689 Printer Hardware present");
         }

         int var19 = buffer.byteAt(9);
         int var21 = buffer.byteAt(12);
         var19 <<= 8;
         this.printerID_microcodeLevel = var21 | var19;
      } catch (Exception var17) {
         if (this.tracer.isOn()) {
            this.tracer.print(var17);
         }

         throw new HandleFactoryException("Error checking 4689 POSPrinter status byte information" + var17);
      } finally {
         device.removeSioDeviceListener(listener);
         listener = null;

         try {
            if (device.isOpen()) {
               device.close();
            }
         } catch (SioException var16) {
            if (this.tracer.isOn()) {
               this.tracer.print(var16);
            }
         }
      }

      if (this.tracer.isOn()) {
         this.tracer.println(2, "<--Check 4689 Printer existance");
      }
   }

   protected IBM4610PrinterHandleImp.PrinterInfo areSubDevicesPresent(SioDevice device) throws HandleFactoryException {
      IBM4610PrinterHandleImp.PrinterInfo level = new IBM4610PrinterHandleImp.PrinterInfo();
      this.isMicrPresent = false;
      this.isTIPresent = false;
      this.isCheckScannerPresent = false;
      int printer_ID = 1;
      int printermicrocodeLevel = -1;
      BooleanMonitor pOnline = new BooleanMonitor(false);
      SioDeviceListener listener = null;
      if (this.tracer.isOn()) {
         this.tracer.println(2, "-->Check 4610 TI3/4/5 TM/F 6/7 Printer existance");
      }

      try {
         ByteBuffer buffer = new ByteBuffer();
         device.open();
         listener = new Rs485HandlePopulator$3(this, buffer, pOnline);
         device.addSioDeviceListener(listener);
         SioIrpRecyclable irp = (SioIrpRecyclable)device.createSioIrp(REINITIALIZE_PRINTER_CMD, true);
         device.syncSubmit(irp);
         irp.recycle();

         try {
            pOnline.waitForTrue(8500);
         } catch (Exception var19) {
            this.println("Printer didn't not return after reset.");
            throw new HandleFactoryException("Printer didn't not return after reset.");
         }

         irp = (SioIrpRecyclable)device.createSioIrp(REQUEST_PRINTER_ID_CMD, true);
         device.syncSubmit(irp);
         int maxRetry = 0;

         do {
            SleepPolicy.sleep(1000L);
         } while (buffer.getByteCount() < 12 && maxRetry++ < 5);

         this.tracer.println(Util.toFormatedHexString(buffer.getBytes()));
         if (maxRetry >= 5) {
            throw new HandleFactoryException("NO 4610 TI3/4/5 TM/F 6/7 Printer Hardware present");
         }

         if (PosjUtil.isBitSelected(buffer.byteAt(10), 0) || buffer.byteAt(8) == 49) {
            this.isMicrPresent = true;
         }

         if (buffer.byteAt(9) == 3 || buffer.byteAt(9) == 5 || buffer.byteAt(9) == 7) {
            this.isTIPresent = true;
         }

         level.setType((byte)48);
         int var24 = buffer.byteAt(9);
         this.printerID_microcodeLevel = -1;
         if (buffer.byteAt(8) == 49 || buffer.byteAt(8) == 48 && buffer.byteAt(9) == 1 && (buffer.byteAt(11) & 2) == 2) {
            level.setType((byte)49);
            if (PosjUtil.isBitSelected(buffer.byteAt(11), 2)) {
               if (PosjUtil.isBitSelected(buffer.byteAt(11), 4)) {
                  var24 = 11;
                  this.isCheckScannerPresent = false;
               } else {
                  var24 = 10;
                  this.isCheckScannerPresent = true;
               }
            } else {
               var24 = 9;
               this.isCheckScannerPresent = true;
            }
         } else {
            this.isCheckScannerPresent = false;
         }

         level.setId((byte)var24);
         int var26 = buffer.byteAt(12);
         var24 <<= 8;
         this.printerID_microcodeLevel = var26 | var24;
      } catch (SioException var21) {
         throw new HandleFactoryException("Error checking 4610 POSPrinter subdevices existance" + var21);
      } catch (Exception var22) {
         throw new HandleFactoryException("Error checking 4610 POSPrinter subdevices existance" + var22);
      } finally {
         device.removeSioDeviceListener(listener);
         listener = null;

         try {
            if (device.isOpen()) {
               device.close();
            }
         } catch (SioException var20) {
            if (this.tracer.isOn()) {
               this.tracer.print(var20);
            }
         }
      }

      if (this.tracer.isOn()) {
         this.tracer.println(2, "<--Check 4610 TI3/4/5 TM/F 6/7 Printer existance");
      }

      return level;
   }

   protected void sioDeviceAttached(SioDevice device) {
      this.addSioDevice(device);
   }

   protected int getScanner4ADeviceId(SioDevice device) throws HandleFactoryException {
      ScannerDetectState state = new ScannerDetectState();
      SioDeviceListener listener = null;
      this.println("Obtainig TableTop/FlatBed scanner ID");

      try {
         ByteBuffer buffer = new ByteBuffer();
         device.open();
         listener = new Rs485HandlePopulator$4(this, buffer, state);
         device.addSioDeviceListener(listener);
         state.reset();

         while (state.getState() != 5) {
            switch (state.getNextStep()) {
               case 1:
                  if (state.getState() == 2) {
                     this.println("Config sent twice.. will send EC level req. ");
                     state.setNextStep(6);
                  } else {
                     try {
                        state.setState(state.getState() + 1);
                        this.submitScannerCmd(device, this.SCANNER_CONFIG_PERIODICALS_CMD, buffer);
                     } catch (HandleFactoryException var23) {
                        this.println("No response from scanner..  will try again");
                        state.setNextStep(1);
                        continue;
                     } catch (SioException var24) {
                        state.setNextStep(6);
                        continue;
                     }

                     if (this.commandRejected(buffer)) {
                        this.println("ScannerType found,  is 4687");
                        state.setDevType(4);
                     } else if (this.configUpdated(buffer)) {
                        this.println("step 1: Config updated");
                        if (PosjUtil.isBitSelected(buffer.byteAt(2), 1)) {
                           this.println("Config coerced, might be a 4698");
                        }

                        state.setNextStep(2);
                        state.setState(0);
                     } else if (state.getState() == 3) {
                        state.setNextStep(1);
                     } else if (this.deviceNotReady(buffer)) {
                        state.setNextStep(1);
                     } else {
                        state.setNextStep(2);
                        state.setState(0);
                     }
                  }
                  break;
               case 2:
                  if (state.getState() == 2) {
                     this.println("step 2: Could NOT determine Scanner Type..  will send EC level Req.");
                     state.setNextStep(6);
                  } else {
                     try {
                        this.submitScannerCmd(device, this.SCANNER_CONFIG_CODE39_CMD, buffer);
                     } catch (SioException var22) {
                        this.println("step 2: SioException caught!");
                        continue;
                     }

                     state.setState(state.getState() + 1);
                     if (this.deviceNotReady(buffer)) {
                        state.setNextStep(2);
                     } else if (this.configUpdated(buffer)) {
                        this.println("Step 2: Config Updated");
                        state.setNextStep(3);
                        state.setState(0);
                     } else if (!this.configUpdated(buffer)) {
                        state.setNextStep(4);
                        state.setState(0);
                     }
                  }
                  break;
               case 3:
                  if (state.getState() == 2) {
                     this.println("Could NOT determine Scanner Type ");
                     state.setDevType(-1);
                  } else {
                     try {
                        this.submitScannerCmd(device, this.SCANNER_CONFIG_LABEL_EXPANSION_CMD, buffer);
                     } catch (SioException var21) {
                        this.println("step 3: SioException caught!");
                        state.setNextStep(6);
                        continue;
                     }

                     state.setState(state.getState() + 1);
                     if (this.configUpdated(buffer)) {
                        this.println("Step 3: Config Updated");
                        state.setNextStep(5);
                        state.setState(0);
                     } else if (this.configInResponse(buffer)) {
                        this.println("Scanner 4686 not supported, returning UNDEFINED type");
                        state.setDevType(-1);
                     } else if (this.deviceNotReady(buffer)) {
                        state.setNextStep(3);
                     }
                  }
                  break;
               case 4:
                  if (state.getState() == 2) {
                     this.println("Could NOT determine Scanner Type ");
                     state.setDevType(-1);
                  } else {
                     this.submitScannerCmd(device, this.SCANNER_REPORT_CONFIG_CMD, buffer);
                     state.setState(state.getState() + 1);
                     if (this.deviceNotReady(buffer)) {
                        state.setNextStep(4);
                     } else if (this.configInResponse(buffer)) {
                        this.println("ScannerType found,  is 4696");
                        state.setDevType(5);
                     } else {
                        this.println("ScannerType found,  is 4687");
                        state.setDevType(4);
                     }
                  }
                  break;
               case 5:
                  if (state.getState() == 2) {
                     this.println("step 5: Could NOT determine Scanner Type ");
                     state.setDevType(-1);
                  } else {
                     try {
                        this.submitScannerCmd(device, this.SCANNER_CONFIG_UPCEAN25_CMD, buffer);
                     } catch (SioException var19) {
                        state.setNextStep(6);
                     }

                     state.setState(state.getState() + 1);
                     if (this.deviceNotReady(buffer)) {
                        state.setNextStep(5);
                     } else if (this.configUpdated(buffer)) {
                        this.println("Step 5: Config Updated");
                        this.println("ScannerType found,  is 4698");
                        state.setDevType(7);
                     } else {
                        this.println("ScannerType found,  is 4697");
                        state.setDevType(6);
                     }
                  }
                  break;
               case 6:
                  this.submitScannerCmd(device, this.SCANNER_EC_LEVEL_REQ_CMD, buffer);
                  if (this.ecLevelResponse(buffer)) {
                     this.println("ScannerType found,  is 4687");
                     state.setDevType(4);
                  } else {
                     this.println("step 6: Could NOT determine Scanner type!");
                     state.setDevType(-1);
                  }
                  break;
               default:
                  this.println("Inscorrect state");
            }
         }
      } catch (SioException var25) {
         throw new HandleFactoryException("SioException while checking scanner ID" + var25);
      } catch (Exception var26) {
         throw new HandleFactoryException("Exception while checking scanner ID" + var26);
      } finally {
         device.removeSioDeviceListener(listener);
         listener = null;

         try {
            if (device.isOpen()) {
               device.close();
            }
         } catch (SioException var20) {
            if (this.tracer.isOn()) {
               this.tracer.print(var20);
            }
         }
      }

      this.println("Table Top Scanner Type is  = " + state.getDevType());
      return state.getDevType();
   }

   protected int getScanner4BDeviceId(SioDevice device) throws HandleFactoryException {
      ScannerDetectState state = new ScannerDetectState();
      SioDeviceListener listener = null;
      this.println("Obtainig Hand Held scanner ID");

      try {
         ByteBuffer buffer = new ByteBuffer();
         device.open();
         listener = new Rs485HandlePopulator$5(this, buffer, state);
         device.addSioDeviceListener(listener);
         state.reset();

         while (state.getState() != 5) {
            switch (state.getNextStep()) {
               case 1:
                  if (state.getState() == 2) {
                     this.println("Could NOT determine Scanner Type ");
                     state.setDevType(-1);
                  } else {
                     this.submitScannerCmd(device, this.SCANNER_1520_CONFIG_CMD, buffer);
                     state.setState(state.getState() + 1);
                     if (!this.commandRejected(buffer) && !this.hwError(buffer) && !this.readError(buffer)) {
                        if ((!PosjUtil.isBitSelected(buffer.byteAt(0), 6) || buffer.getByteCount() <= 3) && buffer.getByteCount() >= 3) {
                           this.println("ScannerType found,  is 1520");
                           state.setDevType(0);
                        } else {
                           this.println("ScannerType is NOT a 1520, go to step 2");
                           state.setNextStep(2);
                        }
                     } else {
                        this.println("ScannerType is NOT a 1520, go to step 2");
                        state.setNextStep(2);
                     }
                  }
                  break;
               case 2:
                  this.submitScannerCmd(device, this.SCANNER_HHBCR2_CONFIG_CMD, buffer);
                  if (!this.commandRejected(buffer) && !this.hwError(buffer) && !this.readError(buffer)) {
                     this.println("ScannerType found,  is 4501");
                     state.setDevType(2);
                  } else {
                     this.println("ScannerType found,  is 4500");
                     state.setDevType(1);
                  }
            }
         }
      } catch (SioException var14) {
         throw new HandleFactoryException("SioException while checking scanner ID" + var14);
      } catch (Exception var15) {
         throw new HandleFactoryException("Exception while checking scanner ID" + var15);
      } finally {
         device.removeSioDeviceListener(listener);
         listener = null;

         try {
            if (device.isOpen()) {
               device.close();
            }
         } catch (SioException var13) {
            if (this.tracer.isOn()) {
               this.tracer.print(var13);
            }
         }
      }

      this.println("Hand Held Scanner Type is  = " + state.getDevType());
      return state.getDevType();
   }

   protected void submitScannerCmd(SioDevice device, byte[] cmd, ByteBuffer buffer) throws SioException, HandleFactoryException {
      buffer.clean();
      this.println("submitting ScannerCmd = " + Util.toFormatedHexString(cmd));
      SioIrpRecyclable irp = (SioIrpRecyclable)device.createSioIrp(cmd, true);
      device.syncSubmit(irp);
      irp.recycle();
      this.waitForStatusResponse(buffer, 1);
      if (buffer.getByteCount() < 2) {
         throw new HandleFactoryException("Bad scanner response, less than 2 bytes received");
      }
   }

   protected void println(String s) {
      if (this.tracer.isOn()) {
         this.tracer.println(2, s);
      }
   }

   protected boolean configInResponse(ByteBuffer buffer) {
      return PosjUtil.isBitSelected(buffer.byteAt(0), 1);
   }

   protected boolean configUpdated(ByteBuffer buffer) {
      return PosjUtil.isBitSelected(buffer.byteAt(2), 0);
   }

   protected boolean commandRejected(ByteBuffer buffer) {
      return PosjUtil.isBitSelected(buffer.byteAt(1), 7);
   }

   protected boolean deviceNotReady(ByteBuffer buffer) {
      return PosjUtil.isBitSelected(buffer.byteAt(0), 7);
   }

   protected boolean hwError(ByteBuffer buffer) {
      return PosjUtil.isBitSelected(buffer.byteAt(1), 6);
   }

   protected boolean readError(ByteBuffer buffer) {
      return PosjUtil.isBitSelected(buffer.byteAt(1), 4);
   }

   protected boolean ecLevelResponse(ByteBuffer buffer) {
      return PosjUtil.isBitSelected(buffer.byteAt(0), 0);
   }

   protected int getScaleType(SioDevice device) throws HandleFactoryException {
      int type = 0;
      SioDeviceListener listener = null;
      int step = 1;
      int tries = 0;
      this.println("-->getScaleType()");

      try {
         ByteBuffer buffer = new ByteBuffer();
         device.open();
         listener = new Rs485HandlePopulator$6(this, buffer);
         device.addSioDeviceListener(listener);

         while (type == 0) {
            switch (step) {
               case 1:
                  if (tries == 2) {
                     this.println("Extended status sent twice, now send an EC Level cmd");
                     step = 3;
                  } else {
                     buffer.clean();
                     SioIrpRecyclable irpxx = (SioIrpRecyclable)device.createSioIrp(this.SCALE_EXTENDED_STATUS_CMD, true);
                     device.syncSubmit(irpxx);
                     this.waitForStatusResponse(buffer, 1);
                     irpxx.recycle();
                     tries++;
                     if (buffer.getByteCount() == 0 || PosjUtil.isBitSelected(buffer.byteAt(1), 6)) {
                        this.println("No response received from Extended status it should be a 4687");
                        step = 1;
                     } else if (PosjUtil.isBitSelected(buffer.byteAt(0), 2) && buffer.getByteCount() == 3) {
                        step = 2;
                        this.println("Extended Status succesful should b a 4696/4698");
                     }
                  }
                  break;
               case 2:
                  buffer.clean();
                  SioIrpRecyclable irpx = (SioIrpRecyclable)device.createSioIrp(this.SCALE_CONFIGURE_CMD, true);
                  this.println("sendind a configuration exclusive for 4698");
                  device.syncSubmit(irpx);
                  this.waitForStatusResponse(buffer, 1);
                  irpx.recycle();
                  if (PosjUtil.isBitSelected(buffer.byteAt(2), 0)) {
                     type = 3;
                     this.println("configuration successful should be a 4698");
                  } else {
                     if (!PosjUtil.isBitSelected(buffer.byteAt(0), 6)) {
                        int var20 = -1;
                        this.println("scale undefined");
                        throw new HandleFactoryException("could not determine scale type ");
                     }

                     type = 2;
                     this.println("configuration unsuccessful should be a 4696");
                  }
                  break;
               case 3:
                  buffer.clean();
                  SioIrpRecyclable irp = (SioIrpRecyclable)device.createSioIrp(this.SCALE_EC_LEVEL_CMD, true);
                  device.syncSubmit(irp);
                  this.waitForStatusResponse(buffer, 1);
                  this.println("EC level sent");
                  irp.recycle();
                  if (PosjUtil.isBitSelected(buffer.byteAt(0), 0)) {
                     type = 1;
                     this.println("EC level response received it should be a 4687");
                  }
            }
         }
      } catch (SioException var17) {
         throw new HandleFactoryException("Error checking Scale type" + var17);
      } catch (Exception var18) {
         throw new HandleFactoryException("Error checking Scale type" + var18);
      } finally {
         device.removeSioDeviceListener(listener);
         listener = null;

         try {
            if (device.isOpen()) {
               device.close();
            }
         } catch (SioException var16) {
            if (this.tracer.isOn()) {
               this.tracer.print(var16);
            }
         }
      }

      if (this.tracer.isOn()) {
         this.tracer.println(2, "Scale type - " + type);
      }

      if (this.tracer.isOn()) {
         this.tracer.println(2, "<--getScaleType()");
      }

      return type;
   }

   protected boolean ignoreCashDrawer() {
      if (this.tracer.isOn()) {
         this.tracer.println("-->ignoreCashDrawer()");
      }

      try {
         GetSlotInfo slotInfo = EmbeddedManager.getInstance().getSlotInfo();
         if (this.tracer.isOn()) {
            this.tracer.println("GetSlotInfo.getAdapterId = " + Util.toHexString(slotInfo.getAdapterID()));
         }

         if (slotInfo.getAdapterID() == 661L) {
            if (this.tracer.isOn()) {
               this.tracer.println("<--ignoreCashDrawer() : returns True");
            }

            return true;
         }
      } catch (UnsatisfiedLinkError var3) {
         if (this.tracer.isOn()) {
            this.tracer.println("Error when detecting system " + var3.toString());
            this.tracer.print(var3);
         }
      } catch (Exception var4) {
         if (this.tracer.isOn()) {
            this.tracer.println("Exception when detecting system " + var4.toString());
            this.tracer.print(var4);
         }
      }

      if (this.tracer.isOn()) {
         this.tracer.println("<--ignoreCashDrawer() : returns False");
      }

      return false;
   }

   private void waitForStatusResponse(ByteBuffer buffer, int responseLen) {
      int maxRetry = 0;

      do {
         SleepPolicy.sleep(300L);
      } while (buffer.getByteCount() < responseLen && maxRetry++ < 34);

      this.println("Device response while detecting type - " + Util.toFormatedHexString(buffer.getBytes()));
   }

   private class SioServicesL implements SioServicesListener {
      private SioServicesL() {
      }

      public void sioDeviceAttached(SioServicesEvent event) {
         Rs485HandlePopulator.this.sioDeviceAttached(event.getSioDevice());
      }

      public void sioDeviceDetached(SioServicesEvent event) {
      }
   }
}
