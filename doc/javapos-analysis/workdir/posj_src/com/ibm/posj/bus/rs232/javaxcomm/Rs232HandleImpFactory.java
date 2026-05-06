package com.ibm.posj.bus.rs232.javaxcomm;

import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.HandleFactory;
import com.ibm.posj.HandleFactoryException;
import com.ibm.posj.PosSystem;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.bus.HandleImp;
import com.ibm.posj.bus.IBM4610PrinterHandleImp;
import com.ibm.posj.bus.IBMPrinterComposite;
import com.ibm.posj.bus.rs232.Rs2324610PrinterHandleImp;
import com.ibm.posj.bus.rs232.Rs232APALineDisplayHandleImp;
import com.ibm.posj.bus.rs232.Rs232CashDrawerHandleImp;
import com.ibm.posj.bus.rs232.Rs232FiscalPrinterHandleImp;
import com.ibm.posj.bus.rs232.Rs232FiscalPrinterProtocol;
import com.ibm.posj.bus.rs232.Rs232HandleKey;
import com.ibm.posj.bus.rs232.Rs232IntegratedLineScannerHandleImp;
import com.ibm.posj.bus.rs232.Rs232IntegratedOmniScannerHandleImp;
import com.ibm.posj.bus.rs232.Rs232MICRHandleImp;
import com.ibm.posj.bus.rs232.Rs232MSRHandleImp;
import com.ibm.posj.bus.rs232.Rs232POSPrinterCashDrawerHandleImp;
import com.ibm.posj.bus.rs232.Rs232POSPrinterCheckScannerHandleImp;
import com.ibm.posj.bus.rs232.Rs232POSPrinterMICRHandleImp;
import com.ibm.posj.bus.rs232.Rs232POSPrinterToneIndicatorHandleImp;
import com.ibm.posj.bus.rs232.Rs232PrinterReader;
import com.ibm.posj.bus.rs232.Rs232SureonePrinterHandleImp;
import com.ibm.posj.bus.rs232.Rs232ToneIndicatorHandleImp;
import com.ibm.posj.bus.rs232.Rs232VFDLineDisplayHandleImp;
import com.ibm.posj.printer.IBM4610SSTToneIndicatorImp;
import com.ibm.posj.printer.IBMPrinterToneIndicatorImp;
import com.ibm.posj.printer.ibm4610.IBM4610CashDrawerImp;
import com.ibm.posj.printer.ibm4610.IBM4610CheckScannerImp;
import com.ibm.posj.printer.ibm4610.IBM4610MICRImp;
import com.ibm.posj.util.DefaultDevCatV;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCatVisitor;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.JclHelper;
import com.ibm.posj.util.PosjUtil;
import com.ibm.rs232.Rs232Config;
import com.ibm.rs232.Rs232Exception;
import com.ibm.rs232.Rs232Port;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import jpos.config.JposEntry;

public class Rs232HandleImpFactory {
   private DevCatVisitor devCatVisitor = null;
   private List list = new LinkedList();
   private HandleFactory handleFactory = null;
   private HandleFactoryException hfE = null;
   private JposEntry jposEntry = null;
   private Hashtable numberTable = new Hashtable();
   private Hashtable handleNumberTable = new Hashtable();
   private Rs232PortManager portManager = null;
   private Rs232Config config = null;
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevBuses.RS232_DEVBUS.toString(), "Rs232HandleImpFactory");
   private boolean isCheckScannerPresent = false;
   private boolean isMicrPresent = false;
   private boolean isTIPresent = false;
   private int printerID_microcodeLevel = -1;
   private int cdMaxHandles = 2;
   public static final int DEFAULT_MAX_HANDLES_PER_DEVICE = 1;
   public static final int DEFAULT_CD_MAX_HANDLES_PER_DEVICE = 2;
   public static final byte[] RESET_4610_PRINTER_CMD = new byte[]{16, 5, 64};
   public static final byte[] SUREONE_FIRMWARE_THERMAL_CMD = new byte[]{27, 35, 42, 10, 0};
   public static final byte[] REQUEST_PRINTER_ID_CMD = new byte[]{29, 73, 1};
   public static final byte[] PRINTER_IDENTIFIER_CMD = new byte[]{5};
   public static final byte MICR_PRESENT_BIT_POSITION = 0;
   public static final byte TI4_EMULATION_BIT_POSITION = 1;
   public static final int CD1 = 1;
   public static final int CD2 = 2;
   public static final int CD_1_PRESENT_BIT_POSITION = 4;
   public static final int CD_2_PRESENT_BIT_POSITION = 3;
   public static final int MSR_ISO = 1;
   public static final int MSR_JUCC = 2;
   public static final String OPEN_PORT_ID = "RS232";
   public static final String POLL_TIME_OUT_MSR = "com.ibm.posj.bus.rs232.onLineWatcherPollTime.MSR";
   public static final String POLL_TIME_OUT_SCANNER = "com.ibm.posj.bus.rs232.onLineWatcherPollTime.Scanner";

   public Rs232HandleImpFactory(HandleFactory factory) {
      this.handleFactory = factory;
   }

   public synchronized List createHandleImps(JposEntry jposEntry) {
      this.jposEntry = jposEntry;
      if (this.tracer.isOn()) {
         this.tracer.println("--->Creating HandleImps for " + jposEntry.getLogicalName());
      }

      this.clear();
      this.config = Rs232Util.createRs232Config(jposEntry);

      try {
         String portName = this.config.getPortName();
         boolean existPort = this.getPortManager().hasRs232Port(portName);
         Rs232Port rs232Port = null;
         if (existPort) {
            rs232Port = this.getPortManager().getRs232Port(portName);
            if (!this.config.equals(rs232Port.getRs232config()) && this.tracer.isOn()) {
               this.tracer.println("Rs232Port < " + portName + "> exists in portManager used previous one " + " this entry config :" + this.config);
            }
         } else {
            rs232Port = this.getPortManager().createRs232Port(this.config);
            rs232Port.open("RS232");
            if (!this.getPortManager().addRs232Port(rs232Port) && this.tracer.isOn()) {
               this.tracer.println("Rs232Port < " + portName + "> exists in portManager used previous one " + " this entry config :" + this.config);
            }
         }

         JclHelper.getDevCat(jposEntry).accept(this.getDevCatVisitor());
      } catch (Rs232Exception var5) {
         if (this.tracer.isOn()) {
            this.tracer.println("Error at Rs232Port < " + this.config.getPortName() + "> creation " + var5.toString());
         }
      }

      if (null != this.getException() && this.tracer.isOn()) {
         this.tracer.println(" An error occurred when creating Handle -> " + this.getException().toString());
      }

      if (this.tracer.isOn()) {
         this.tracer.println("<---HandleImp creation successful, " + this.list.size() + " HandleImps created for " + jposEntry.getLogicalName());
      }

      return this.list;
   }

   protected void visitCashDrawer(DevCat devCat) {
      try {
         String portName = this.config.getPortName();
         if (!portName.equalsIgnoreCase("COM4")) {
            throw new HandleFactoryException("Cash Drawers in a different port than \"COM4\" are not allowed for creation");
         }

         Rs232HandleKey key = null;
         Rs232CashDrawerHandleImp handleImp = null;
         String hKey = devCat.toString() + portName;
         this.checkHandleNumber(hKey, this.cdMaxHandles);
         if (this.isCDPresent(this.getRs232Port(portName), 1)) {
            key = new Rs232HandleKey(devCat, 0, this.config);
            handleImp = new Rs232CashDrawerHandleImp(key, this.getRs232Port(portName), 1);
            handleImp.setHandle(this.handleFactory.createCashDrawerHandle(handleImp));
            this.list.add(handleImp);
            this.addHandle(hKey);
         } else {
            this.cdMaxHandles = 1;
         }

         if (this.isCDPresent(this.getRs232Port(portName), 2)) {
            key = new Rs232HandleKey(devCat, 1, this.config);
            handleImp = new Rs232CashDrawerHandleImp(key, this.getRs232Port(portName), 2);
            handleImp.setHandle(this.handleFactory.createCashDrawerHandle(handleImp));
            this.list.add(handleImp);
            this.addHandle(hKey);
         } else {
            this.cdMaxHandles = 1;
         }
      } catch (HandleFactoryException var6) {
         this.hfE = var6;
      }
   }

   protected void visitFiscalPrinter(DevCat devCat) {
      try {
         String portName = this.config.getPortName();
         String hKey = devCat.toString() + portName;
         this.checkHandleNumber(hKey, 1);
         this.createFiscalPrinterHandle(devCat, portName);
         this.addHandle(hKey);
      } catch (HandleFactoryException var4) {
         this.hfE = var4;
      }
   }

   protected void visitLineDisplay(DevCat devCat) {
      try {
         String hKey = devCat.toString() + this.config.getPortName();
         this.checkHandleNumber(hKey, 1);
         Rs232HandleKey key = new Rs232HandleKey(devCat, this.getDeviceNumber(devCat), this.config);
         Object obj = this.jposEntry.getPropertyValue("com.ibm.posj.bus.rs232.lineDisplayId");
         String lineDisplayDeviceValue = null;
         if (obj == null) {
            throw new HandleFactoryException("can not decide what LineDisplay handleImp factory create");
         }

         lineDisplayDeviceValue = obj.toString();
         if (lineDisplayDeviceValue.equals("APA")) {
            Rs232APALineDisplayHandleImp handleImp = new Rs232APALineDisplayHandleImp(
               key, this.getRs232Port(key.getPortName()), this.lineDisplayRs232CursorState()
            );
            handleImp.setHandle(this.handleFactory.createLineDisplayHandle(handleImp));
            this.list.add(handleImp);
         } else {
            if (!lineDisplayDeviceValue.equals("VFD")) {
               throw new HandleFactoryException("Not supported LineDisplay handleImp factory type");
            }

            Rs232VFDLineDisplayHandleImp handleImp = new Rs232VFDLineDisplayHandleImp(
               key, this.getRs232Port(key.getPortName()), this.lineDisplayRs232CursorState()
            );
            handleImp.setHandle(this.handleFactory.createLineDisplayHandle(handleImp));
            this.list.add(handleImp);
         }

         this.addHandle(hKey);
      } catch (HandleFactoryException var7) {
         this.hfE = var7;
      }
   }

   protected void visitMSR(DevCat devCat) {
      try {
         String hKey = devCat.toString() + this.config.getPortName();
         this.checkHandleNumber(hKey, 1);
         Rs232HandleKey key = new Rs232HandleKey(devCat, this.getDeviceNumber(devCat), this.config);
         Object obj = this.jposEntry.getPropertyValue("com.ibm.posj.bus.rs232.msrId");
         int msrDeviceId = 1;
         if (obj != null) {
            String id = obj.toString();
            id = id.toUpperCase();
            if (id.equals("ISO") || id.equals("1")) {
               msrDeviceId = 1;
            } else if (id.equals("JUCC") || id.equals("2")) {
               msrDeviceId = 2;
            }
         }

         short var13;
         if (msrDeviceId == 1) {
            var13 = 3300;
         } else if (msrDeviceId == 2) {
            var13 = 3301;
         } else {
            var13 = 3303;
         }

         int timeout = 10000;
         boolean watcherEnabled = false;
         obj = this.jposEntry.getPropertyValue("com.ibm.posj.bus.rs232.enableOnlineWatcher");
         if (obj != null) {
            watcherEnabled = (Boolean)obj;
         }

         if (watcherEnabled) {
            PosSystem.Properties p = PosSystemManager.getInstance().getProperties();
            if (p.isPropertyDefined("com.ibm.posj.bus.rs232.onLineWatcherPollTime.MSR")) {
               try {
                  timeout = Integer.decode(p.getPropertyString("com.ibm.posj.bus.rs232.onLineWatcherPollTime.MSR"));
               } catch (Exception var10) {
                  this.tracer.print(var10);
               }
            }
         }

         Rs232MSRHandleImp handleImp = new Rs232MSRHandleImp(key, this.getRs232Port(key.getPortName()), var13, watcherEnabled, timeout);
         handleImp.setHandle(this.handleFactory.createMSRHandle(handleImp));
         this.list.add(handleImp);
         this.addHandle(hKey);
      } catch (HandleFactoryException var11) {
         this.hfE = var11;
      }
   }

   protected void visitPOSPrinter(DevCat devCat) {
      try {
         String portName = this.config.getPortName();
         String hKey = devCat.toString() + portName;
         this.checkHandleNumber(hKey, 1);
         if (portName.equalsIgnoreCase("COM3") && !this.is4610Printer(this.getRs232Port(portName))) {
            if (this.tracer.isOn()) {
               this.tracer.println("Creating sureone");
            }

            Rs232HandleKey key = new Rs232HandleKey(devCat, this.getDeviceNumber(devCat), this.config);
            Rs232SureonePrinterHandleImp sImp = new Rs232SureonePrinterHandleImp(key, this.getRs232Port(portName));
            sImp.setHandle(this.handleFactory.createPOSPrinterHandle(sImp));
            this.list.add(sImp);
         } else {
            this.create4610PrinterHandles(devCat, portName);
            if (this.tracer.isOn()) {
               this.tracer.println("creating 4610");
            }
         }

         this.addHandle(hKey);
      } catch (HandleFactoryException var6) {
         this.hfE = var6;
      }
   }

   protected void visitScanner(DevCat devCat) {
      try {
         String hKey = devCat.toString() + this.config.getPortName();
         this.checkHandleNumber(hKey, 1);
         Rs232HandleKey key = new Rs232HandleKey(devCat, this.getDeviceNumber(devCat), this.config);
         int timeout = 10000;
         boolean watcherEnabled = false;
         int pollInterval = 0;
         Object pollIntervalPropertyObj = this.jposEntry.getPropertyValue("com.ibm.posj.bus.rs232.scannerPollInterval");
         Object watcherEnabledPropertyObj = this.jposEntry.getPropertyValue("com.ibm.posj.bus.rs232.enableOnlineWatcher");
         if (pollIntervalPropertyObj != null) {
            pollInterval = ((Byte)pollIntervalPropertyObj).intValue();
         }

         if (watcherEnabledPropertyObj != null) {
            watcherEnabled = (Boolean)watcherEnabledPropertyObj;
            if (watcherEnabled && pollInterval > 0) {
               watcherEnabled = false;
               if (this.tracer.isOn()) {
                  this.tracer.println(1, "***OnlineWatcher disabled by DevicePoller***");
               }
            }
         }

         if (watcherEnabled) {
            PosSystem.Properties p = PosSystemManager.getInstance().getProperties();
            if (p.isPropertyDefined("com.ibm.posj.bus.rs232.onLineWatcherPollTime.Scanner")) {
               try {
                  timeout = Integer.decode(p.getPropertyString("com.ibm.posj.bus.rs232.onLineWatcherPollTime.Scanner"));
               } catch (Exception var12) {
                  this.tracer.print(var12);
               }
            }
         }

         if (this.tracer.isOn()) {
            this.tracer.println("com.ibm.posj.bus.rs232.scannerPollInterval = " + pollInterval);
            this.tracer
               .println(
                  "com.ibm.posj.bus.rs232.enableOnlineWatcher = "
                     + watcherEnabled
                     + "\n"
                     + "com.ibm.posj.bus.rs232.onLineWatcherPollTime.Scanner"
                     + " = "
                     + timeout
               );
         }

         Object scannerIdPropertyObj = this.jposEntry.getPropertyValue("com.ibm.posj.bus.rs232.scannerId");
         boolean isOmniScannerId = false;
         if (scannerIdPropertyObj != null) {
            String id = scannerIdPropertyObj.toString();
            if (id.toUpperCase().indexOf("OMNI") >= 0) {
               isOmniScannerId = true;
            }
         }

         if (isOmniScannerId) {
            Rs232IntegratedOmniScannerHandleImp handleImp = new Rs232IntegratedOmniScannerHandleImp(
               key, this.getRs232Port(key.getPortName()), watcherEnabled, timeout, pollInterval
            );
            handleImp.setHandle(this.handleFactory.createScannerHandle(handleImp));
            this.list.add(handleImp);
            this.addHandle(hKey);
         } else {
            Rs232IntegratedLineScannerHandleImp handleImp = new Rs232IntegratedLineScannerHandleImp(
               key, this.getRs232Port(key.getPortName()), watcherEnabled, timeout, pollInterval
            );
            handleImp.setHandle(this.handleFactory.createScannerHandle(handleImp));
            this.list.add(handleImp);
            this.addHandle(hKey);
         }
      } catch (HandleFactoryException var13) {
         this.hfE = var13;
      }
   }

   protected DevCatVisitor getDevCatVisitor() {
      if (this.devCatVisitor == null) {
         this.devCatVisitor = new Rs232HandleImpFactory.DevCatV();
      }

      return this.devCatVisitor;
   }

   protected HandleFactoryException getException() {
      return this.hfE;
   }

   protected void clear() {
      this.hfE = null;
      this.list.clear();
   }

   protected Rs232Port getRs232Port(String port) {
      return this.getPortManager().getRs232Port(port);
   }

   protected Rs232PortManager getPortManager() {
      if (this.portManager == null) {
         this.portManager = Rs232PortManager.getInstance();
      }

      return this.portManager;
   }

   protected int getDeviceNumber(DevCat devCat) {
      String key = devCat.toString();
      int number = 0;
      if (this.numberTable.containsKey(key)) {
         number = (Integer)this.numberTable.get(key);
         number++;
      }

      this.numberTable.put(key, new Integer(number));
      return number;
   }

   protected int getHandleNumber(String handleKey) {
      int number = 0;
      if (this.handleNumberTable.containsKey(handleKey)) {
         number = (Integer)this.handleNumberTable.get(handleKey);
      }

      return number;
   }

   protected void addHandle(String handleKey) {
      int number = 1;
      if (this.handleNumberTable.containsKey(handleKey)) {
         number = (Integer)this.handleNumberTable.get(handleKey);
         number++;
      }

      this.handleNumberTable.put(handleKey, new Integer(number));
   }

   protected void checkHandleNumber(String hKey, int maxNumber) throws HandleFactoryException {
      if (this.getHandleNumber(hKey) >= maxNumber) {
         throw new HandleFactoryException("The number of Handle Imps exceds the limit permited :" + maxNumber);
      }
   }

   private byte lineDisplayRs232CursorState() {
      String stringCursorState = null;
      byte cursorState = 1;
      Object obj = this.jposEntry.getPropertyValue("com.ibm.jpos.services.sdi.config.LineDisplay.CursorState");
      if (obj != null) {
         stringCursorState = obj.toString();
         if (stringCursorState.equals("OFF")) {
            cursorState = 0;
         }
      }

      return cursorState;
   }

   private void createFiscalPrinterHandle(DevCat devCat, String portName) throws HandleFactoryException {
      if (this.tracer.isOn()) {
         this.tracer.println("-->createFiscalPrinterHandle");
      }

      this.checkFiscalPrinterOnline(this.getRs232Port(portName));
      Rs232HandleKey key = new Rs232HandleKey(devCat, this.getDeviceNumber(devCat), this.config);
      Rs232FiscalPrinterHandleImp handleImp = new Rs232FiscalPrinterHandleImp(key, this.getRs232Port(portName));
      handleImp.setHandle(this.handleFactory.createFiscalPrinterHandle(handleImp));
      this.list.add(handleImp);
      if (this.tracer.isOn()) {
         this.tracer.println("<--createFiscalPrinterHandle");
      }
   }

   private byte checkFiscalPrinterOnline(Rs232Port port) throws HandleFactoryException {
      if (this.tracer.isOn()) {
         this.tracer.println("-->checkFiscalPrinterOnline");
         this.tracer.println("Check 4610 Fiscal Printer existance");
      }

      Rs232FiscalPrinterProtocol protocol = new Rs232FiscalPrinterProtocol();
      Rs232HandleImpFactory$1StateListener listener = new Rs232HandleImpFactory$1StateListener(this);
      byte type = 0;

      try {
         port.open("HandleImpFactory");
         listener.setPort((Rs232PortCommAdapter)port);
         ((Rs232PortCommAdapter)port).setReaderStrategy(protocol);
         int maxRetry = 0;
         long startTime = 0L;

         do {
            maxRetry++;
            if (this.tracer.isOn()) {
               this.tracer.println("retry: " + maxRetry);
            }

            protocol.stopTimer();
            startTime = System.currentTimeMillis();
            protocol.detectFiscalPrinter(listener);

            while (!listener.getDataReceived() && System.currentTimeMillis() - startTime < (long)(250 * 10)) {
            }
         } while (!listener.getDataReceived() && maxRetry < 6);

         protocol.stopTimer();
         if (maxRetry >= 6) {
            if (this.tracer.isOn()) {
               this.tracer.println("no more retries");
            }

            throw new HandleFactoryException("NO 4610 Fiscal Printer Hardware present");
         }
      } catch (Rs232Exception var11) {
         if (this.tracer.isOn()) {
            this.tracer.println("create exception");
         }

         throw new HandleFactoryException("Error checking Fiscal Printer existance" + var11);
      } finally {
         protocol.setHandleImp(null);
         Rs232FiscalPrinterProtocol var13 = null;
      }

      if (this.tracer.isOn()) {
         this.tracer.println("<--checkFiscalPrinterOnline");
      }

      return type;
   }

   private void create4610PrinterHandles(DevCat devCat, String portName) throws HandleFactoryException {
      IBM4610PrinterHandleImp.PrinterInfo type = this.areSubDevicesPresent(this.getRs232Port(portName));
      Rs232HandleKey key = new Rs232HandleKey(devCat, this.getDeviceNumber(devCat), this.config);
      Rs2324610PrinterHandleImp ptrHandleImp = new Rs2324610PrinterHandleImp(key, this.getRs232Port(portName), type);
      ptrHandleImp.setPrinterID_microcodeLevel(this.getPrinterID_microcodeLevel());
      ptrHandleImp.setHandle(this.handleFactory.createPOSPrinterHandle(ptrHandleImp));
      this.list.add(ptrHandleImp);
      if (this.isMicrPresent) {
         this.list.add(this.create4610MicrHandleImp(DevCats.MICR_DEVCAT, this.config, ptrHandleImp));
      }

      HandleImp handleI = this.create4610CDHandleImp(DevCats.CASHDRAWER_DEVCAT, this.config, 1, 0, ptrHandleImp);
      this.list.add(handleI);
      handleI = this.create4610CDHandleImp(DevCats.CASHDRAWER_DEVCAT, this.config, 2, 1, ptrHandleImp);
      this.list.add(handleI);
      if (this.isTIPresent) {
         HandleImp var8 = this.create4610TIHandleImp(DevCats.TONEINDICATOR_DEVCAT, this.config, ptrHandleImp);
         this.list.add(var8);
      }

      if (this.isCheckScannerPresent) {
         handleI = this.create4610CSHandleImp(DevCats.CHECKSCANNER_DEVCAT, this.config, ptrHandleImp);
         this.list.add(handleI);
      }
   }

   private int getPrinterID_microcodeLevel() {
      return this.printerID_microcodeLevel;
   }

   private boolean is4610Printer(Rs232Port port) throws HandleFactoryException {
      ByteBuffer buffer = new ByteBuffer();
      Rs232Port.Listener printerSubDevicesListener = new Rs232HandleImpFactory$1(this, buffer);
      port.addRs232PortListener(printerSubDevicesListener);

      try {
         port.submit(PRINTER_IDENTIFIER_CMD);
         Thread.currentThread();
         Thread.sleep(1200L);
      } catch (Exception var5) {
         throw new HandleFactoryException("Error checking printer existance" + var5);
      }

      return buffer.getByteCount() <= 0;
   }

   private IBM4610PrinterHandleImp.PrinterInfo areSubDevicesPresent(Rs232Port port) throws HandleFactoryException {
      IBM4610PrinterHandleImp.PrinterInfo level = new IBM4610PrinterHandleImp.PrinterInfo();
      this.isMicrPresent = false;
      this.isTIPresent = false;
      int printer_ID = 1;
      int printermicrocodeLevel = -1;
      Rs232Port.Listener printerSubDevicesListener = null;
      this.tracer.println("Check 4610 TI3/4/5 TM/F 6/7 Printer existance");
      Rs232PrinterReader treader = new Rs232PrinterReader(port);

      try {
         ByteBuffer buffer = new ByteBuffer();
         ((Rs232PortCommAdapter)port).setReaderStrategy(treader);
         printerSubDevicesListener = new Rs232HandleImpFactory$2(this, buffer);
         port.submit(RESET_4610_PRINTER_CMD);

         try {
            Thread.currentThread();
            Thread.sleep(3500L);
         } catch (InterruptedException var17) {
         }

         port.addRs232PortListener(printerSubDevicesListener);
         port.submit(REQUEST_PRINTER_ID_CMD);
         int maxRetry = 0;

         do {
            try {
               Thread.currentThread();
               Thread.sleep(300L);
            } catch (InterruptedException var16) {
            }
         } while (buffer.getByteCount() < 15 && maxRetry++ < 5);

         if (maxRetry >= 5) {
            throw new HandleFactoryException("NO 4610 TI3/4/5 TM/F 6/7 Printer Hardware present");
         }

         if (PosjUtil.isBitSelected(buffer.byteAt(12), 0) || buffer.byteAt(10) == 49) {
            this.isMicrPresent = true;
         }

         this.tracer.println(Util.toFormatedHexString(buffer.getBytes()));
         if (buffer.byteAt(11) == 3 || buffer.byteAt(11) == 5 || buffer.byteAt(11) == 7) {
            this.isTIPresent = true;
         }

         level.setType((byte)48);
         level.setId(buffer.byteAt(11));
         byte var20;
         if (buffer.byteAt(10) == 49 || buffer.byteAt(10) == 48 && PosjUtil.isBitSelected(buffer.byteAt(13), 1)) {
            level.setType((byte)49);
            if (PosjUtil.isBitSelected(buffer.byteAt(13), 2)) {
               if (PosjUtil.isBitSelected(buffer.byteAt(13), 4)) {
                  var20 = 11;
                  this.isCheckScannerPresent = false;
               } else {
                  var20 = 10;
                  this.isCheckScannerPresent = true;
               }
            } else {
               var20 = 9;
               this.isCheckScannerPresent = true;
            }

            level.setId((byte)var20);
         } else {
            var20 = buffer.byteAt(11);
         }

         this.printerID_microcodeLevel = -1;
         int var22 = buffer.byteAt(14);
         var20 <<= 8;
         this.printerID_microcodeLevel = var22 | var20;
         port.removeRs232PortListener(printerSubDevicesListener);
      } catch (Rs232Exception var18) {
         throw new HandleFactoryException("Error checking MICR existance" + var18);
      } finally {
         port.removeRs232PortListener(printerSubDevicesListener);
         ((Rs232PortCommAdapter)port).setReaderStrategy(null);
         treader.kill();
      }

      return level;
   }

   private boolean isCDPresent(Rs232Port port, int cdNumber) throws HandleFactoryException {
      boolean cdPresent = false;
      int presenceBitPosition = cdNumber == 1 ? 4 : 3;

      try {
         ByteBuffer status = new ByteBuffer();
         port.open("HandleImpFactory");
         Rs232Port.Listener cdListener = new Rs232HandleImpFactory$3(this, status);
         port.addRs232PortListener(cdListener);
         port.submit(Rs232CashDrawerHandleImp.STATUS_REQUEST_CMD);
         int retry = 0;
         int maxRetry = 6;

         do {
            try {
               Thread.currentThread();
               Thread.sleep(300L);
            } catch (InterruptedException var10) {
            }
         } while (status.getByteCount() == 0 && retry++ < maxRetry);

         if (retry >= maxRetry) {
            throw new HandleFactoryException("No Cash Drawer Hardware present");
         }

         if (PosjUtil.isBitSelected(status.byteAt(0), presenceBitPosition)) {
            cdPresent = true;
         }

         port.removeRs232PortListener(cdListener);
      } catch (Rs232Exception var11) {
         throw new HandleFactoryException("Error while verifying Cash Drawer presence" + var11);
      }

      this.tracer.println("Cash drawer " + cdNumber + " present ? " + cdPresent);
      return cdPresent;
   }

   private HandleImp create4610CDHandleImp(DevCat devCat, Rs232Config config, int cdNumber, int deviceNumber, IBMPrinterComposite printerComposite) throws HandleFactoryException {
      Rs232HandleKey k = new Rs232HandleKey(devCat, deviceNumber, config);
      IBM4610CashDrawerImp cdLinker = new IBM4610CashDrawerImp(k, printerComposite, cdNumber);
      Rs232POSPrinterCashDrawerHandleImp hi = new Rs232POSPrinterCashDrawerHandleImp(k, this.getRs232Port(k.getPortName()), cdLinker);
      cdLinker.setHandleImp(hi);
      hi.setHandle(this.handleFactory.createCashDrawerHandle(hi));
      printerComposite.addDevice(k, cdLinker);
      return hi;
   }

   private HandleImp create4610CSHandleImp(DevCat devCat, Rs232Config config, IBMPrinterComposite printerComposite) throws HandleFactoryException {
      Rs232HandleKey k = new Rs232HandleKey(devCat, this.getDeviceNumber(devCat), config);
      IBM4610CheckScannerImp csLinker = new IBM4610CheckScannerImp(k, printerComposite);
      Rs232POSPrinterCheckScannerHandleImp hi = new Rs232POSPrinterCheckScannerHandleImp(k, this.getRs232Port(k.getPortName()), csLinker);
      csLinker.setHandleImp(hi);
      hi.setHandle(this.handleFactory.createCheckScannerHandle(hi));
      printerComposite.addDevice(k, csLinker);
      return hi;
   }

   private HandleImp create4610MicrHandleImp(DevCat devCat, Rs232Config config, IBMPrinterComposite printerComposite) throws HandleFactoryException {
      Rs232HandleKey k = new Rs232HandleKey(devCat, this.getDeviceNumber(devCat), config);
      IBM4610MICRImp micrLinker = new IBM4610MICRImp(k, printerComposite);
      Rs232MICRHandleImp hi = new Rs232POSPrinterMICRHandleImp(k, this.getRs232Port(k.getPortName()), micrLinker);
      micrLinker.setHandleImp(hi);
      hi.setHandle(this.handleFactory.create4610MICRHandle(hi));
      printerComposite.addDevice(k, micrLinker);
      return hi;
   }

   private Rs232ToneIndicatorHandleImp create4610TIHandleImp(DevCat devCat, Rs232Config config, IBMPrinterComposite printerComposite) throws HandleFactoryException {
      Rs232HandleKey k = new Rs232HandleKey(devCat, this.getDeviceNumber(devCat), config);
      IBMPrinterToneIndicatorImp ti = new IBM4610SSTToneIndicatorImp(printerComposite);
      Rs232POSPrinterToneIndicatorHandleImp hi = new Rs232POSPrinterToneIndicatorHandleImp(k, this.getRs232Port(k.getPortName()), ti);
      hi.setHandle(this.handleFactory.createToneIndicatorHandle(hi));
      printerComposite.addDevice(k, hi);
      return hi;
   }

   class DevCatV extends DefaultDevCatV {
      public void visitCashDrawer(DevCat devCat) {
         Rs232HandleImpFactory.this.visitCashDrawer(devCat);
      }

      public void visitFiscalPrinter(DevCat devCat) {
         Rs232HandleImpFactory.this.visitFiscalPrinter(devCat);
      }

      public void visitMSR(DevCat devCat) {
         Rs232HandleImpFactory.this.visitMSR(devCat);
      }

      public void visitLineDisplay(DevCat devCat) {
         Rs232HandleImpFactory.this.visitLineDisplay(devCat);
      }

      public void visitPOSPrinter(DevCat devCat) {
         Rs232HandleImpFactory.this.visitPOSPrinter(devCat);
      }

      public void visitScanner(DevCat devCat) {
         Rs232HandleImpFactory.this.visitScanner(devCat);
      }
   }
}
