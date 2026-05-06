package com.ibm.posj.bus.rs232;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.DefaultHandleListener;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.IBM4610PrinterHandleImp;
import com.ibm.posj.bus.POSPrinterHandleImp;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.event.ErrorEvent;
import com.ibm.posj.event.OfflineEvent;
import com.ibm.posj.event.OnlineEvent;
import com.ibm.posj.event.OutputCompleteEvent;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashFile;
import com.ibm.posj.flash.FlashFormat;
import com.ibm.posj.flash.FlashHandleImpVisitable;
import com.ibm.posj.flash.FlashManager;
import com.ibm.posj.flash.FlashPrinterHandleImpVisitor;
import com.ibm.posj.flash.FlashRecord;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.flash.IBM4610FlashUtil;
import com.ibm.posj.flash.Rs485FlashFile;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCats;
import com.ibm.rs232.Rs232Port;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class Rs232FlashPrinterHandleImp extends Rs232FlashHandleImp {
   FlashRequest flashRequest = null;
   private BooleanMonitor waitStatus = new BooleanMonitor(false);
   private boolean wrapup = false;
   private boolean is_erasing = false;
   private boolean rejectRetry = false;
   private FlashPrinterHandleImpVisitor flashPrinterHandleImpVisitor = new FlashPrinterHandleImpVisitor();
   private Handle printerHandle = null;
   private FlashFile rs485FlashFile = null;
   private FlashRequest currRequest = null;
   private FlashRequest rs485PrinterFlashRequest = null;
   private FlashRequest rs485PrinterRequest = null;
   private Iterator flashRequestIterator = null;
   private Rs232FlashPrinterHandleImp.MCDownload microdeDwnld = null;
   private final byte[] statusRequest = new byte[]{27, 118};
   private final byte[] printerResetCmd = new byte[]{16, 5, 64};
   private int printerID = -1;
   private int printerEC = -1;
   private int printerType = -1;
   private byte printerFeatureByte1 = 0;
   private byte printerFeatureByte2 = 0;
   protected static final int T1_T2_PRINTERS = 0;
   protected static final int T3_T4_PRINTERS = 1;
   protected static final int T5_PRINTERS = 2;
   private String T1_OR_T2_PRINTER_FILENAME = "aip46mc.hex";
   private String T3_OR_T4_PRINTER_FILENAME = "aip46mch.hex";
   private String OTHER_PRINTER_FILENAME = "aip46mcd.hex";
   private String CRABTREE_PRINTER_FILENAME = "aip46ti8.hex";
   private static final int PRT_FLASH_TIMEOUT = 22000;
   private static final int ERASE_TIMEOUT = 90000;
   private byte[] cmd = new byte[260];
   private boolean retry = true;
   private int firmwareDlCnt = 0;
   private Rs232FlashPrinterHandleImp.PrinterFlashListener printerFlashListener = new Rs232FlashPrinterHandleImp.PrinterFlashListener();
   private byte[] currentEraseCmd = null;
   private byte[] currentLoadCmd = null;
   private byte[] eraseFirmwareCmd = new byte[]{1, 69, 82, 65};
   private byte[] loadFirmwareCmd = new byte[]{1, 68, 76};
   private final byte[] eraseBootSectorCmd = new byte[]{1, 69, 82, 66};
   private final int oldestEClevel = 11;
   private byte[] eraseFirmwareHSCmd = new byte[]{1, 69, 84, 65};
   private byte[] loadFirmwareHSCmd = new byte[]{1, 68, 78};
   private final byte[] eraseBootSectorHSCmd = new byte[]{1, 69, 84, 66};
   private final int oldestHSEClevel = 20;
   private byte[] eraseFirmwareTI8Cmd = new byte[]{1, 69, 85, 65};
   private byte[] loadFirmwareTI8Cmd = new byte[]{1, 68, 79};
   private final int oldestTI8EClevel = 20;
   private Tracer tracer = TracerFactory.getInstance().createTracer("FLASH", "Rs232FlashPrinterHandleImp");

   public Rs232FlashPrinterHandleImp(HandleKey key, Rs232Port device) {
      super(key, device);
   }

   public void flash(FlashRequest flashRequest) throws FlashException {
      try {
         this.lockSubDevices();
         this.tracer.println(".flash() - flashfilename:" + flashRequest.getFlashFile().getFilename());
         this.tracer.println(".flash() - deviceECLevel(): " + this.getECLevel());
         flashRequest.setDeviceECLevel(this.getECLevel());
         this.tracer.println(".flash() - preparing to flash " + this.getDevCat().toString());
         this.executeFlash(flashRequest.getFlashFile(), this.printerEC);
      } catch (FlashException var6) {
         throw var6;
      } finally {
         this.unlockSubDevices();
      }
   }

   protected void executeFlash(FlashFile flashFile, int deviceECLevel) throws FlashException {
      int realECLevel = deviceECLevel;
      this.tracer.println(".executeFlash - enter");
      if (null != flashFile) {
         if (this.printerHandle.getHandleImp().getClass().getName().indexOf("4610") != -1) {
            realECLevel = IBM4610FlashUtil.getRealECLevel(this.printerHandle);
         }

         if (!flashFile.isFlashFileVersionNewer(realECLevel)) {
            this.tracer
               .println(
                  ".executeFlash - "
                     + flashFile.getDevCat().toString()
                     + " does not need flashing, with device EC: 0x"
                     + Integer.toHexString(deviceECLevel)
                     + " Real EC: 0x"
                     + Integer.toHexString(realECLevel)
                     + " file EC: 0x"
                     + Integer.toHexString(flashFile.getVersion())
                     + " Flashfile="
                     + flashFile.getFilename()
               );
         } else {
            this.tracer
               .println(
                  "Flashing "
                     + flashFile.getDevCat().toString()
                     + " from EC level "
                     + deviceECLevel
                     + " Real EC level: "
                     + realECLevel
                     + " to EC Level "
                     + flashFile.getVersion()
               );
            if (flashFile.isDevBusMatched(DevBuses.RS485_DEVBUS)) {
               if (!this.validatePrinterEClevel(flashFile.getVersion(), this.printerEC)) {
                  this.tracer.println(".execute() - printer EC level is up to date");
               }

               this.rs485FlashFile = flashFile;
               this.tracer.println(".executeFlash() -send reset");
               this.wrapup = false;
               this.sendPrinterResetCmd(this.printerHandle);
               this.cmdCompleted.set(false);
               this.microdeDwnld.doFlash();
               this.wrapup = true;
               this.tracer.println(".executeFlash() -send reset");
               this.sendPrinterResetCmd(this.printerHandle);
               this.tracer.println(".execute Flash -> Done with Rs485 Printer Flash");
               this.printerHandle.removeHandleListener(this.printerFlashListener);
            }
         }
      }
   }

   public int getPrinterID() {
      return this.printerID;
   }

   public int getPrinterEC() {
      return this.printerEC;
   }

   public int getPrinterType() {
      return this.printerType;
   }

   public void setPrinterHandle(Handle pHandle) {
      this.printerHandle = pHandle;
   }

   private void initPrinterRequests(Handle printerHandle, Vector flashRequests) throws FlashException {
      boolean foundRs485 = false;
      this.flashRequestIterator = flashRequests.iterator();

      while (this.flashRequestIterator.hasNext()) {
         this.currRequest = (FlashRequest)this.flashRequestIterator.next();
         this.tracer.println(".initPrinterRequests -> checking request " + this.currRequest.getHandle().getDevCat().toString());
         if (this.currRequest.getHandle().getDevCat().toString().equals(DevCats.POSPRINTER_DEVCAT.toString())) {
            if (!foundRs485 && this.currRequest.getFlashFile() instanceof Rs485FlashFile) {
               this.rs485PrinterRequest = this.currRequest;
               foundRs485 = true;
            }

            if (foundRs485) {
               break;
            }
         }
      }

      if (!foundRs485) {
         this.tracer.println(".initPrinterRequests -> Exception");
      }
   }

   public void getPrinterInfo(Handle handle, FlashFile file) throws FlashException {
      if (handle.getDevCat().toString().equals(DevCats.POSPRINTER_DEVCAT.toString())) {
         this.tracer.println(".getPrinterInfo()");
         this.tracer.println(".initializing POSPrinter Handle");

         try {
            handle.init();
         } catch (HandleException var6) {
            throw new FlashException("Could not init POSPrinterHandle", var6);
         }

         this.tracer.println(".getPrinterInfo() - send status request");
         POSPrinterHandleImp POSPrinterHandleImp = (POSPrinterHandleImp)handle.getHandleImp();
         this.printerID = POSPrinterHandleImp.getPrinterHandleState().getPrinterID();
         this.printerEC = POSPrinterHandleImp.getPrinterHandleState().getPrinterEC();
         this.printerType = POSPrinterHandleImp.getPrinterHandleState().getPrinterType();
         IBM4610PrinterHandleImp hid4610PrinterHandleImp = (IBM4610PrinterHandleImp)handle.getHandleImp();
         this.printerFeatureByte1 = hid4610PrinterHandleImp.getFeatureByte1();
         this.printerFeatureByte2 = hid4610PrinterHandleImp.getFeatureByte2();
         this.tracer
            .println(
               ".getPrinterInfo() - printerID:0x"
                  + Integer.toHexString(this.printerID)
                  + " printerEC: 0x"
                  + Integer.toHexString(this.printerEC)
                  + " printerType: 0x"
                  + Integer.toHexString(this.printerType)
                  + " printerFeatureByte1: 0x"
                  + Integer.toHexString(this.printerFeatureByte1)
                  + " printerFeatureByte2: 0x"
                  + Integer.toHexString(this.printerFeatureByte2)
            );
         this.currentEraseCmd = this.eraseFirmwareHSCmd;
         this.currentLoadCmd = this.loadFirmwareHSCmd;
         if (this.printerType > 0) {
            boolean exCrabtree = 3801 == this.printerType && (this.printerFeatureByte2 & 2) > 0;
            if (3802 != this.printerType && !exCrabtree) {
               this.microdeDwnld = new Rs232FlashPrinterHandleImp.MCDownload4610();
            } else {
               this.microdeDwnld = new Rs232FlashPrinterHandleImp.MCDownloadCrabtree();
            }
         } else if (0 == this.printerID && 0 == this.printerEC && 0 == this.printerType) {
            String filename = file.getFilename();
            if (-1 != filename.indexOf(File.separator)) {
               filename = filename.substring(filename.lastIndexOf(File.separator) + 1, filename.length());
            }

            if (0 == filename.compareToIgnoreCase(this.T1_OR_T2_PRINTER_FILENAME)) {
               this.printerID = 0;
               this.microdeDwnld = new Rs232FlashPrinterHandleImp.MCDownload4610();
            } else if (0 == filename.compareToIgnoreCase(this.CRABTREE_PRINTER_FILENAME)) {
               this.printerType = 3802;
               this.microdeDwnld = new Rs232FlashPrinterHandleImp.MCDownloadCrabtree();
            } else {
               this.printerID = 1;
               this.microdeDwnld = new Rs232FlashPrinterHandleImp.MCDownload4610();
            }

            this.tracer.println(".getPrinterInfo() printerID:" + this.printerID + " printerEC:" + this.printerEC + " printerType:" + this.printerType);
         }
      }
   }

   public void setRs485PrinterInfo(int printerID, int printerEC, int printerType) {
      this.printerID = printerID;
      this.printerEC = printerEC;
      this.printerType = printerType;
   }

   private void sendPrinterResetCmd(Handle handle) throws FlashException {
      if (this.tracer.isOn()) {
         this.tracer.println(".sendPrinterResetCmd() -> reset()");
      }

      try {
         this.sendCmd(this.printerResetCmd);
      } catch (Exception var4) {
      }

      this.sleep(5000L);
      if (this.wrapup) {
         this.sendCmd(this.statusRequest);

         try {
            this.cmdCompleted.set(false);
            this.cmdCompleted.waitForTrue(22000);
         } catch (Exception var3) {
            this.getTracer().println("In Rs232FlashPrinterHandleImp -> Flash Timed out waiting for response");
         }
      }
   }

   private void formatDwnLdCmd(Handle handle, byte[] cmd) throws FlashException {
      if (handle.getHandleImp() instanceof Rs232POSPrinterHandleImp) {
         this.tracer.println(Util.toFormatedHexString(cmd));
      }

      this.waitStatus.set(false);
      super.sendCmd(cmd, 7, cmd.length - 7);

      try {
         this.waitStatus.waitForTrue(22000);
      } catch (Exception var5) {
         this.tracer.print(var5);
         Handle.EventHelper eventHelper = this.printerHandle.getEventHelper();
         this.firmwareDlCnt = Integer.MAX_VALUE;
         eventHelper.fireErrorEvent(new ErrorEvent(this, -101));
      }
   }

   private void sendCmd(Handle handle, byte[] cmd) throws FlashException {
      super.sendCmd(cmd);
   }

   private void sendCmd(Handle handle, byte[] cmd, int time) throws FlashException {
      super.sendCmd(cmd, time);
   }

   private FlashRequest getRs485PrinterFlashRequest() {
      return this.rs485PrinterRequest;
   }

   private boolean validatePrinterEClevel(int fileECLevel, int printerEC) {
      this.tracer.println(".validatePrinterEClevel() - printerEC: " + printerEC + " fileEC: " + fileECLevel);
      if (fileECLevel <= printerEC) {
         return false;
      } else {
         if (this.printerID == 0) {
            if (fileECLevel < 11) {
               return false;
            }
         } else if (fileECLevel < 20) {
            return false;
         }

         return true;
      }
   }

   private FlashFile getRs485FlashFile() {
      return null == this.rs485FlashFile ? this.getRs485PrinterFlashRequest().getFlashFile() : this.rs485FlashFile;
   }

   private int getFileEClevel() {
      return this.getRs485FlashFile().getVersion();
   }

   private void eraseBootSector() throws FlashException {
      this.tracer.println(".eraseBootSector() enter");
      if (this.printerID == 0) {
         this.tracer.println(".eraseBootSector() - T1,2");
         this.sendCmd(this.printerHandle, this.eraseBootSectorCmd, 90000);
      } else {
         this.tracer.println(".eraseBootSector() - T3,4,5");
         this.sendCmd(this.printerHandle, this.eraseBootSectorHSCmd, 90000);
      }

      try {
         this.tracer.println(".eraseBootSector() -> Waiting after EraseBootSector!!");
         this.waitStatus.waitForTrue(22000);
         this.tracer.println(".eraseCodeSector() exit");
      } catch (Exception var2) {
         this.tracer.print(var2);
      }
   }

   private void eraseCodeSector() throws FlashException {
      this.retry = true;
      this.is_erasing = true;

      while (this.retry) {
         this.cmdCompleted.set(false);
         this.waitStatus.set(false);

         try {
            this.sendCmd(this.currentEraseCmd, 90000);
         } catch (Exception var7) {
            this.tracer.print(var7);
         }

         try {
            this.waitStatus.waitForTrue(90000);
            this.retry = false;
            continue;
         } catch (Exception var8) {
            this.tracer.print(var8);
            Handle.EventHelper eventHelper = this.printerHandle.getEventHelper();
            eventHelper.fireErrorEvent(new ErrorEvent(this, -101));
         } finally {
            this.is_erasing = false;
         }

         return;
      }
   }

   private void downloadFirmware(FlashFormat ff) throws FlashException {
      dlSequence = 0;
      needReset = false;
      Iterator it = ff.getFRs();

      while (it.hasNext()) {
         try {
            this.tracer.println(".downloadFirmware() - seq: " + dlSequence++);
            FlashRecord fr = (FlashRecord)it.next();
            byte[] flashRecordData = fr.getRecordData();
            System.arraycopy(this.currentLoadCmd, 0, flashRecordData, 7, this.currentLoadCmd.length);
            this.cmdCompleted.set(false);
            this.rejectRetry = false;
            this.formatDwnLdCmd(this.printerHandle, flashRecordData);
            if (this.rejectRetry) {
               this.rejectRetry = false;
               this.formatDwnLdCmd(this.printerHandle, flashRecordData);
            }
         } catch (Exception var5) {
            if (this.tracer.isOn()) {
               this.tracer.print(var5);
            }
         }
      }

      this.sleep(2000L);
      needReset = true;
      this.firmwareDlCnt = 0;
   }

   public void flashPOSPrinterSDICC(Handle handle, String usbFileName, String rs485FileName) throws FlashException {
      this.initPrinterSDICC(handle, usbFileName, rs485FileName);
   }

   protected void initPrinter(Handle handle, Vector flashRequests, ArrayList rs485FlashFileList) throws FlashException {
      this.tracer.println(".initPrinter");
      this.printerHandle = handle;
      this.printerHandle.addHandleListener(this.printerFlashListener);
      this.initPrinterRequests(handle, flashRequests);
      this.tracer.println(".initPrinter() buildRs485FlashRequest() getPrinterInfo");
      this.flashPrinterHandleImpVisitor.setFlashFileList(rs485FlashFileList);
      POSPrinterHandleImp printerHandleImp = (POSPrinterHandleImp)handle.getHandleImp();
      FlashHandleImpVisitable handleImpVisitable = (FlashHandleImpVisitable)printerHandleImp;
      handleImpVisitable.accept(this.flashPrinterHandleImpVisitor);
      this.prtFlashFile = printerHandleImp.getFlashHandleImp().getPrtFlashFile();
      if (this.prtFlashFile != null) {
         this.getPrinterInfo(handle, this.prtFlashFile);
      } else {
         this.printerID = -1;
      }

      if (this.prtFlashFile != null) {
         this.rs485PrinterFlashRequest = new FlashRequest(handle, this.prtFlashFile);
         this.tracer.println("printer flash file: " + this.prtFlashFile.getFilename());
      } else {
         this.rs485PrinterFlashRequest = null;
      }

      if (null != this.rs485PrinterFlashRequest) {
         this.rs485PrinterFlashRequest.setRs485PrinterInfo(this.printerID, this.printerEC, this.printerType);
         handle.flash(this.rs485PrinterFlashRequest);
      } else {
         this.tracer.println(" no rs485 Printer Flash request");
      }

      this.tracer.println(" flash() printer's bridge - DONE!!!");
   }

   protected void initPrinterSDICC(Handle handle, String usbFileName, String rs485FileName) throws FlashException {
      this.printerHandle = handle;
      FlashFile rs485ff = new Rs485FlashFile(rs485FileName);
      boolean doUSBBridgeFlash = true;
      this.printerHandle.addHandleListener(this.printerFlashListener);
      this.tracer.println(".initPrinterSDICC() buildRs485FlashRequest() getPrinterInfo");
      this.getPrinterInfo(handle, rs485ff);
      this.rs485PrinterRequest = new FlashRequest(handle, rs485ff);
      this.tracer.println("flash() - printer flash file: " + rs485ff.getFilename());
      this.rs485PrinterRequest.setRs485PrinterInfo(this.printerID, this.printerEC, this.printerType);
      if (!this.rs485PrinterRequest.validate4610FlashFileDevType(handle)) {
         this.getTracer().println(".initPrinterSDICC-> Flash File device type field does not match printer");
         throw new FlashException("Device type does not match with flash file selected");
      } else {
         handle.flash(this.rs485PrinterRequest);
      }
   }

   protected void processDataEvent(PrintStatus status) {
      byte[] raw = status.getData();
      this.tracer.println(".processDataEvent() calling checkForCmdRejectError " + Util.toFormatedHexString(raw, 0, raw.length));
      this.cmdCompleted.set(true);
      if (this.is_erasing && raw[1] == 0 && raw[6] == 0) {
         this.waitStatus.set(true);
      } else {
         if (status.getStatus(896)) {
            this.waitStatus.set(true);
         }

         if (status.getStatus(12)) {
            this.rejectRetry = true;
            this.waitStatus.set(true);
         }
      }
   }

   public void sleep(long time) {
      try {
         Thread.currentThread();
         Thread.sleep(time);
      } catch (InterruptedException var4) {
      }
   }

   private byte getPrinterFeatureByte1() {
      return this.printerFeatureByte1;
   }

   private byte getPrinterFeatureByte2() {
      return this.printerFeatureByte2;
   }

   interface MCDownload {
      void doFlash() throws FlashException;
   }

   class MCDownload4610 implements Rs232FlashPrinterHandleImp.MCDownload {
      public void doFlash() throws FlashException {
         if (3811 == Rs232FlashPrinterHandleImp.this.printerID) {
            Rs232FlashPrinterHandleImp.this.currentEraseCmd = Rs232FlashPrinterHandleImp.this.eraseFirmwareCmd;
            Rs232FlashPrinterHandleImp.this.currentLoadCmd = Rs232FlashPrinterHandleImp.this.loadFirmwareCmd;
         }

         if (Rs232FlashPrinterHandleImp.this.printerType != 3801
            || 2 != (Rs232FlashPrinterHandleImp.this.getPrinterFeatureByte2() & 2)
            || !Rs232FlashPrinterHandleImp.this.getRs485FlashFile().getFilename().endsWith(Rs232FlashPrinterHandleImp.this.T3_OR_T4_PRINTER_FILENAME)
               && !Rs232FlashPrinterHandleImp.this.getRs485FlashFile().getFilename().endsWith(Rs232FlashPrinterHandleImp.this.OTHER_PRINTER_FILENAME)) {
            Rs232FlashPrinterHandleImp.this.getRs485FlashFile().load();
            FlashFormat ff = Rs232FlashPrinterHandleImp.this.getRs485FlashFile().getFlashFormat();
            if (FlashManager.getInstance().isBootSectorFlag()) {
               Rs485FlashFile rf = (Rs485FlashFile)Rs232FlashPrinterHandleImp.this.getRs485FlashFile();
               if (!rf.isBootSectorAvailable()) {
                  throw new FlashException("BootSector Data Not available");
               }

               Rs232FlashPrinterHandleImp.this.eraseBootSector();
            } else {
               Rs232FlashPrinterHandleImp.this.tracer.println("MCDownload4610 - doFlash() - eraseCodeSector()");
               Rs232FlashPrinterHandleImp.this.eraseCodeSector();
            }

            Rs232FlashPrinterHandleImp.this.tracer.println("MCDownload4610 - doFlash() - downloadFirmware()");
            Rs232FlashPrinterHandleImp.this.downloadFirmware(ff);
         } else {
            Rs232FlashPrinterHandleImp.this.tracer
               .println(" MCDownload4610 -> doFlash() -  Automatic flash for TI8 printer running as a TI3/4 is not implemented");
         }
      }
   }

   class MCDownloadCrabtree implements Rs232FlashPrinterHandleImp.MCDownload {
      public void doFlash() throws FlashException {
         if (!Rs232FlashPrinterHandleImp.this.getRs485FlashFile().getFilename().endsWith(Rs232FlashPrinterHandleImp.this.T3_OR_T4_PRINTER_FILENAME)
            && !Rs232FlashPrinterHandleImp.this.getRs485FlashFile().getFilename().endsWith(Rs232FlashPrinterHandleImp.this.OTHER_PRINTER_FILENAME)) {
            Rs232FlashPrinterHandleImp.this.getRs485FlashFile().load();
            FlashFormat ff = Rs232FlashPrinterHandleImp.this.getRs485FlashFile().getFlashFormat();
            Rs232FlashPrinterHandleImp.this.tracer.println(" MCDownloadCrabtree - doFlash() -downloadCrabtreeFirmware()");
            Rs232FlashPrinterHandleImp.this.currentEraseCmd = Rs232FlashPrinterHandleImp.this.eraseFirmwareTI8Cmd;
            Rs232FlashPrinterHandleImp.this.currentLoadCmd = Rs232FlashPrinterHandleImp.this.loadFirmwareHSCmd;
            Rs232FlashPrinterHandleImp.this.downloadFirmware(ff);
            Rs232FlashPrinterHandleImp.this.tracer.println(" MCDownloadCrabtree - doFlash() -eraseCodeSector()");
            Rs232FlashPrinterHandleImp.this.eraseCodeSector();
         } else {
            Rs232FlashPrinterHandleImp.this.tracer
               .println(
                  "MCDownloadCrabtree - doFlash() firmware: "
                     + Rs232FlashPrinterHandleImp.this.getRs485FlashFile().getFilename()
                     + " is not allowed for TI8 printers!"
               );
         }
      }
   }

   class PrinterFlashListener extends DefaultHandleListener {
      public void outputCompleteEventOccurred(OutputCompleteEvent event) {
      }

      public void dataEventOccurred(DataEvent event) {
      }

      public void statusEventOccurred(StatusEvent event) {
         Rs232FlashPrinterHandleImp.this.tracer.println(".statusEventOccurred() ->Got Flash Data BACK!!!");
         Rs232FlashPrinterHandleImp.this.processDataEvent((PrintStatus)event);
      }

      public void errorEventOccurred(ErrorEvent event) {
      }

      public void onlineEventOccurred(OnlineEvent event) {
      }

      public void offlineEventOccurred(OfflineEvent event) {
      }
   }
}
