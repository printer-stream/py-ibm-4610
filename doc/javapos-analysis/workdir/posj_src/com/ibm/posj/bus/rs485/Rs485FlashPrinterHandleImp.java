package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.ByteArrayCollector;
import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.DefaultHandleListener;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.POSPrinterHandleImp;
import com.ibm.posj.bus.hid.Hid4689PrinterHandleImp;
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
import com.ibm.posj.flash.UsbFlashFile;
import com.ibm.posj.printer.IBMPrinterState;
import com.ibm.posj.printer.event.PrintStatus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCats;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class Rs485FlashPrinterHandleImp extends Rs485FlashHandleImp {
   FlashRequest flashRequest = null;
   private BooleanMonitor waitStatus = new BooleanMonitor(false);
   private Handle printerHandle = null;
   private FlashFile rs485FlashFile = null;
   private FlashPrinterHandleImpVisitor flashPrinterHandleImpVisitor = new FlashPrinterHandleImpVisitor();
   private FlashRequest currRequest = null;
   private FlashRequest rs485PrinterFlashRequest = null;
   private FlashRequest usbPrinterRequest = null;
   private FlashRequest rs485PrinterRequest = null;
   private Iterator flashRequestIterator = null;
   private Rs485FlashPrinterHandleImp.MCDownload microdeDwnld = null;
   private final byte[] statusRequest = new byte[]{27, 0, 32, 0};
   private final byte[] printerResetCmd = new byte[]{0, 64, 0};
   protected static final boolean IS_RAW = true;
   protected static final boolean NOT_RAW = false;
   protected static final boolean IS_RS485_INTERFACE = true;
   private int printerID = -1;
   private int printerEC = -1;
   private int printerType = -1;
   private boolean wrapup = false;
   private byte printerFeatureByte1 = 0;
   private byte printerFeatureByte2 = 0;
   protected static final int T1_T2_PRINTERS = 0;
   protected static final int T3_T4_PRINTERS = 1;
   protected static final int T5_PRINTERS = 2;
   private String T1_OR_T2_PRINTER_FILENAME = "aip46mc.hex";
   private String T3_OR_T4_PRINTER_FILENAME = "aip46mch.hex";
   private String OTHER_PRINTER_FILENAME = "aip46mcd.hex";
   private String CRABTREE_PRINTER_FILENAME = "aip46ti8.hex";
   private static final String PRINTER_4689_FILENAME = "aip4689.hex";
   private static final int PRT_FLASH_TIMEOUT = 20000;
   byte[] cmd = new byte[260];
   private Rs485FlashPrinterHandleImp.PrinterFlashListener printerFlashListener = new Rs485FlashPrinterHandleImp.PrinterFlashListener();
   protected byte[] currentEraseCmd = null;
   protected byte[] currentLoadCmd = null;
   protected final byte[] eraseFirmwareCmd = new byte[]{1, 69, 82, 65};
   protected final byte[] eraseBootSectorCmd = new byte[]{1, 69, 82, 66};
   protected final byte[] loadFirmwareCmd = new byte[]{1, 68, 76};
   private final int oldestEClevel = 11;
   protected final byte[] eraseFirmwareHSCmd = new byte[]{1, 69, 84, 65};
   protected final byte[] eraseBootSectorHSCmd = new byte[]{1, 69, 84, 66};
   protected final byte[] loadFirmwareHSCmd = new byte[]{1, 68, 78};
   private final int oldestHSEClevel = 20;
   protected byte[] eraseFirmwareTI8Cmd = new byte[]{1, 69, 85, 65};
   protected byte[] loadFirmwareTI8Cmd = new byte[]{1, 68, 79};
   private final int oldestTI8EClevel = 20;
   private Tracer tracer = TracerFactory.getInstance().createTracer("FLASH", "HidFlashPrinterHandleImp");
   private int newEcLevel = -1;
   private boolean featureByte2Bit1 = false;
   public static final int ERR_STATUS_BYTE_INDEX = 0;
   public static final int MAX_PACKET_SIZE = 528;

   public Rs485FlashPrinterHandleImp(HandleKey key, SioDevice device) {
      super(key, device);
   }

   protected void executeFlash(FlashFile flashFile, int deviceECLevel) throws FlashException {
      int realECLevel = deviceECLevel;
      this.getTracer().println("executeFlash " + flashFile.getDevBus());
      if (null != flashFile) {
         if (this.printerHandle.getHandleImp().getClass().getName().indexOf("4610") != -1) {
            realECLevel = IBM4610FlashUtil.getRealECLevel(this.printerHandle);
         }

         if (!flashFile.isFlashFileVersionNewer(realECLevel)) {
            this.getTracer()
               .println(
                  "executeFlash - "
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
            return;
         }

         this.getTracer()
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
               this.getTracer().println("execute() - printer EC level is up to date");
            }

            this.rs485FlashFile = flashFile;
            this.getTracer().println("executeFlash() -send reset1");
            this.wrapup = false;
            this.sendPrinterResetCmd(this.printerHandle);
            this.cmdCompleted.set(false);
            this.waitStatus.set(false);
            this.getTracer().println("executeFlash() -doFlash()");
            this.getMCDownload().doFlash();
            this.getTracer().println("executeFlash() -send reset2");
            this.wrapup = true;
            this.sendPrinterResetCmd(this.printerHandle);
            this.getTracer().println("execute Flash -> Done with Rs485 Printer Flash");
            this.printerHandle.removeHandleListener(this.printerFlashListener);
         } else {
            this.getTracer().println("execute()  - beginning Bridge Flash -> send erase bytes");
            this.cmdCompleted.set(false);
            super.sendCmd(this.eraseBytes);
            this.sleep(2000L);
            needReset = false;
            flashFile.load();
            FlashFormat ff = flashFile.getFlashFormat();
            dlSequence = 0;

            for (int i = 0; i < ff.size(); i++) {
               FlashRecord fr = ff.get(i);
               byte[] flashRecordData = fr.getRecordData();
               this.getTracer().println("executeFlash -> flashRecordData length = " + flashRecordData.length + " seq: " + dlSequence++);
               this.cmdCompleted.set(false);
               super.sendCmd(flashRecordData);
            }

            byte[] cs = flashFile.getCheckSum();
            this.checkSumBytes[0] = (byte)(this.checkSumBytes.length % 256);
            this.checkSumBytes[1] = (byte)(this.checkSumBytes.length / 256);
            this.checkSumBytes[2] = 2;
            this.checkSumBytes[3] = 0;
            this.checkSumBytes[4] = 0;
            this.checkSumBytes[5] = cs[0];
            this.checkSumBytes[6] = cs[1];
            this.getTracer().println("executeFlash -> sending checkSum");
            this.cmdCompleted.set(false);
            super.sendCmd(this.checkSumBytes);
            needReset = true;
            this.sleep(2000L);
            this.getTracer().println("executeFlash() -send reset");
            this.sendPrinterResetCmd(this.printerHandle);
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

   protected void initPrinterRequests(Handle printerHandle, Vector flashRequests) throws FlashException {
      boolean foundUSB = false;
      boolean foundRs485 = false;
      printerHandle.addHandleListener(this.printerFlashListener);
      this.flashRequestIterator = flashRequests.iterator();

      while (this.flashRequestIterator.hasNext()) {
         this.currRequest = (FlashRequest)this.flashRequestIterator.next();
         this.getTracer().println("initPrinterRequests -> checking request " + this.currRequest.getHandle().getDevCat().toString());
         if (this.currRequest.getHandle().getDevCat().toString().equals(DevCats.POSPRINTER_DEVCAT.toString())) {
            if (!foundUSB && this.currRequest.getFlashFile() instanceof UsbFlashFile) {
               this.usbPrinterRequest = this.currRequest;
               foundUSB = true;
            }

            if (!foundRs485 && this.currRequest.getFlashFile() instanceof Rs485FlashFile) {
               this.rs485PrinterRequest = this.currRequest;
               foundRs485 = true;
            }

            if (foundRs485 && foundUSB) {
               break;
            }
         }
      }

      if (!foundRs485) {
         this.getTracer().println("initPrinterRequests -> Exception");
      }
   }

   public void getPrinterInfo(Handle handle, FlashFile file) throws FlashException {
      if (handle.getDevCat().toString().equals(DevCats.POSPRINTER_DEVCAT.toString())) {
         this.getTracer().println("Getting PrinterInfo()");
         this.getTracer().println("initializing POSPrinter Handle");

         try {
            handle.init();
         } catch (HandleException var5) {
            throw new FlashException("Could not init POSPrinterHandle", var5);
         }

         this.getTracer().println("Getting PrinterInfo() send status request");
         Rs485POSPrinterHandleImp rs485POSPrinterHandleImp = (Rs485POSPrinterHandleImp)handle.getHandleImp();
         this.printerID = rs485POSPrinterHandleImp.getPrinterHandleState().getPrinterID();
         this.printerEC = rs485POSPrinterHandleImp.getPrinterHandleState().getPrinterEC();
         this.printerType = rs485POSPrinterHandleImp.getPrinterHandleState().getPrinterType();
         this.getTracer()
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
         if (this.printerType > 0) {
            if (handle.getHandleImp() instanceof Hid4689PrinterHandleImp) {
               this.setMCDownload(new Rs485FlashPrinterHandleImp.MCDownload4689());
            } else {
               boolean exCrabtree = 3801 == this.printerType && (this.printerFeatureByte2 & 2) > 0;
               if (3802 != this.printerType && !exCrabtree) {
                  this.setMCDownload(new Rs485FlashPrinterHandleImp.MCDownload4610());
               } else {
                  this.setMCDownload(new Rs485FlashPrinterHandleImp.MCDownloadCrabtree());
               }
            }
         } else {
            if (handle.getHandleImp() instanceof Hid4689PrinterHandleImp) {
               this.setMCDownload(new Rs485FlashPrinterHandleImp.MCDownload4689());
            }

            if (0 == this.printerID && 0 == this.printerEC && 0 == this.printerType) {
               String filename = file.getFilename();
               if (-1 != filename.indexOf(File.separator)) {
                  filename = filename.substring(filename.lastIndexOf(File.separator) + 1, filename.length());
               }

               if (0 == filename.compareToIgnoreCase(this.T1_OR_T2_PRINTER_FILENAME)) {
                  this.printerID = 0;
                  this.setMCDownload(new Rs485FlashPrinterHandleImp.MCDownload4610());
               } else {
                  if (0 == filename.compareToIgnoreCase(this.CRABTREE_PRINTER_FILENAME)) {
                     this.printerType = 3802;
                     this.setMCDownload(new Rs485FlashPrinterHandleImp.MCDownloadCrabtree());
                  } else {
                     this.printerID = 1;
                     this.setMCDownload(new Rs485FlashPrinterHandleImp.MCDownload4610());
                  }

                  this.getTracer()
                     .println("getPrinterInfo() 2- printerID:" + this.printerID + " printerEC:" + this.printerEC + " printerType:" + this.printerType);
               }
            }
         }
      }
   }

   public void setRs485PrinterInfo(int printerID, int printerEC, int printerType) {
      this.printerID = printerID;
      this.printerEC = printerEC;
      this.printerType = printerType;
   }

   public void flash(FlashRequest flashRequest) throws FlashException {
      try {
         this.lockSubDevices();
         this.getTracer().println(".flash() - flashfilename:" + flashRequest.getFlashFile().getFilename());
         this.getTracer().println(".flash() - deviceECLevel(): " + this.getECLevel());
         flashRequest.setDeviceECLevel(this.getECLevel());
         this.getTracer().println(".flash() - preparing to flash " + this.getDevCat().toString());
         this.executeFlash(flashRequest.getFlashFile(), this.printerEC);
      } catch (FlashException var6) {
         throw var6;
      } finally {
         this.unlockSubDevices();
      }
   }

   private void sendPrinterResetCmd(Handle handle) throws FlashException {
      this.getTracer().println("sendPrinterResetCmd() -> reset()");

      try {
         this.makeAndSendCmd(handle, this.printerResetCmd, true);
      } catch (Exception var4) {
      }

      this.sleep(5000L);
      if (this.wrapup) {
         this.makeAndSendCmd(handle, this.statusRequest, true);

         try {
            this.cmdCompleted.set(false);
            this.cmdCompleted.waitForTrue(20000);
         } catch (Exception var3) {
            this.getTracer().println("In Rs485FlashPrinterHandleImp -> Flash Timed out waiting for response");
         }
      }
   }

   private void makeAndSendCmd(Handle handle, byte[] inCmd, boolean raw) throws FlashException {
      this.getTracer().println("makeAndSendCmd()");
      this.cmd[0] = 1;
      this.cmd[1] = (byte)((this.cmd.length - 3) % 256);
      this.cmd[2] = (byte)((this.cmd.length - 3) / 256);
      this.cmd[3] = 1;
      this.cmd[4] = 0;
      this.cmd[5] = 0;
      this.cmd[6] = 0;
      System.arraycopy(inCmd, 0, this.cmd, 0, inCmd.length);
      if (raw) {
         this.sendRawCmd(handle, inCmd);
      } else {
         this.sendCmd(handle, inCmd);
      }
   }

   protected void formatDwnLdCmd(Handle handle, byte[] cmd) throws FlashException {
      if (this.getTracer().isOn()) {
         this.getTracer().println("formatDwnLdCmd()");
      }

      this.sendCmd(handle, cmd);
   }

   private void sendCmd(Handle handle, byte[] cmd) throws FlashException {
      this.getTracer().println("sendCmd()");

      try {
         if (handle.getHandleImp() instanceof Rs485POSPrinterHandleImp) {
            this.getTracer().println(Util.toFormatedHexString(cmd));
         }

         ((Rs485POSPrinterHandleImp)handle.getHandleImp()).writeToBus(cmd, 0, cmd.length);
      } catch (HandleException var5) {
         throw new FlashException("-getPrinterInfo() Error while submitting flash cmd to device", var5);
      }

      try {
         this.cmdCompleted.waitForTrue(20000);
      } catch (Exception var4) {
         this.getTracer().println("Flash Timed out waiting for response");
         throw new FlashException("Flash Timed out waiting for response");
      }
   }

   private void sendRawCmd(Handle handle, byte[] cmd) throws FlashException {
      this.getTracer().println("sendRawCmd()");
      if (handle.getHandleImp() instanceof Rs485POSPrinterHandleImp) {
         this.getTracer().println(Util.toFormatedHexString(cmd));
      }

      try {
         ((Rs485POSPrinterHandleImp)handle.getHandleImp()).reset(cmd, 0, cmd.length);
      } catch (Exception var4) {
         throw new FlashException("Flash failed", var4);
      }

      this.getTracer().println("setReportDone!");
      this.getTracer().println("getReportDone!");
   }

   private FlashRequest getUsbPrinterFlashRequest() {
      return this.usbPrinterRequest;
   }

   protected FlashRequest getRs485PrinterFlashRequest() {
      return this.rs485PrinterRequest;
   }

   protected void setRs485PrinterFlashRequest(FlashRequest fr) {
      this.rs485PrinterFlashRequest = fr;
   }

   private boolean validatePrinterEClevel(int fileECLevel, int printerEC) {
      this.getTracer().println("validatePrinterEClevel() - printerEC: " + printerEC + " fileEC: " + fileECLevel);
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

   protected FlashFile getRs485FlashFile() {
      return null == this.rs485FlashFile ? this.getRs485PrinterFlashRequest().getFlashFile() : this.rs485FlashFile;
   }

   private int getFileEClevel() {
      return this.getRs485FlashFile().getVersion();
   }

   protected void eraseCodeSector() throws FlashException {
      this.getTracer().println(".eraseCodeSector() enter");

      try {
         this.makeAndSendCmd(this.printerHandle, this.currentEraseCmd, false);
      } catch (Exception var3) {
         this.getTracer().print(var3);
      }

      try {
         this.getTracer().println(".eraseCodeSector() -> Waiting after eraseCodeSector!!");
         this.waitStatus.waitForTrue(90000);
         this.getTracer().println(".eraseCodeSector() exit");
      } catch (Exception var2) {
         this.getTracer().print(var2);
      }
   }

   private void eraseBootSector() throws FlashException {
      this.getTracer().println(".eraseBootSector() enter");
      this.makeAndSendCmd(this.printerHandle, this.printerResetCmd, false);
      this.getTracer().println(".eraseBootSector() Reset -> Waiting !!");
      this.sleep(2100L);
      if (this.printerID == 0) {
         this.getTracer().println(".eraseBootSector() - T1,2");
         this.makeAndSendCmd(this.printerHandle, this.eraseBootSectorCmd, false);
      } else {
         this.getTracer().println(".eraseBootSector() - T3,4,5");
         this.makeAndSendCmd(this.printerHandle, this.eraseBootSectorHSCmd, false);
      }

      try {
         this.getTracer().println(".eraseBootSector() -> Waiting after EraseBootSector!!");
         this.waitStatus.waitForTrue(20000);
         this.getTracer().println(".eraseCodeSector() exit");
      } catch (Exception var2) {
         this.getTracer().print(var2);
      }
   }

   protected void downloadFirmware(FlashFormat ff) throws FlashException {
      dlSequence = 0;
      this.getTracer().println(".downloadFirmware() flashFile: " + this.getRs485FlashFile().getFilename());
      needReset = false;
      ByteArrayCollector bac = ByteArrayCollector.getCollector();
      byte[] cmd = null;

      for (int i = 0; i < ff.size(); i++) {
         this.getTracer().println("downloadFirmware() - seq: " + dlSequence++);
         FlashRecord fr = ff.get(i);
         byte[] flashRecordData = fr.getRecordData();
         if (this.printerID == 0) {
            this.getTracer().println("downloadFirmware() - T1, T2");
            System.arraycopy(this.currentLoadCmd, 0, flashRecordData, 0, this.currentLoadCmd.length);
         } else {
            bac = ByteArrayCollector.getCollector();
            cmd = bac.getArray(flashRecordData.length - 10 + this.currentLoadCmd.length);
            this.getTracer().println("downloadFirmware() - T3, T4, T5");
            System.arraycopy(this.currentLoadCmd, 0, cmd, 0, this.currentLoadCmd.length);
            System.arraycopy(flashRecordData, 10, cmd, this.currentLoadCmd.length, flashRecordData.length - 10);
         }

         this.cmdCompleted.set(false);
         this.formatDwnLdCmd(this.printerHandle, cmd);
      }

      this.sleep(2000L);
      needReset = true;
   }

   public void sleep(long time) {
      try {
         Thread.currentThread();
         Thread.sleep(time);
      } catch (InterruptedException var4) {
      }
   }

   protected void initPrinter(Handle handle, Vector flashRequests, ArrayList rs485FlashFileList) throws FlashException {
      this.getTracer().println("initPrinter");
      this.printerHandle = handle;
      this.initPrinterRequests(handle, flashRequests);
      this.getTracer().println("buildRs485FlashRequest() getPrinterInfo");
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
         this.getTracer().println("flash() - printer flash file: " + this.prtFlashFile);
      } else {
         this.rs485PrinterFlashRequest = null;
      }

      if (null != this.rs485PrinterFlashRequest) {
         this.rs485PrinterFlashRequest.setRs485PrinterInfo(this.printerID, this.printerEC, this.printerType);
         handle.flash(this.rs485PrinterFlashRequest);
      } else {
         this.getTracer().println("no rs485 Printer Flash request");
      }

      if (this.getUsbPrinterFlashRequest() != null) {
         handle.flash(this.getUsbPrinterFlashRequest());
      }

      this.getTracer().println("flash() printer's bridge - DONE!!!");
      if (null != this.getRs485PrinterFlashRequest() && null != this.getRs485PrinterFlashRequest().getFlashFile()) {
         this.getPrinterInfo(handle, this.getRs485PrinterFlashRequest().getFlashFile());
      } else {
         this.printerID = -1;
      }

      if (this.printerID != -1) {
         Rs485POSPrinterHandleImp rs485POSPrinterHandleImp = (Rs485POSPrinterHandleImp)handle.getHandleImp();
         IBMPrinterState ibmPrinterState = (IBMPrinterState)rs485POSPrinterHandleImp.getPrinterHandleState();
         ibmPrinterState.setECLevel(this.newEcLevel);
      }
   }

   protected void initPrinterSDICC(Handle handle, String usbFileName, String rs485FileName) throws FlashException {
      if (null != rs485FileName) {
         this.flashMicrocode(handle, rs485FileName);
      } else {
         this.getTracer().println("rs485FileName is NULL");
      }
   }

   public void flashPOSPrinterSDICC(Handle handle, String usbFileName, String rs485FileName) throws FlashException {
      this.initPrinterSDICC(handle, usbFileName, rs485FileName);
   }

   public void flashPOSPrinter(Handle handle, Vector flashRequests, ArrayList flashFileList) throws FlashException {
      this.getTracer().println("IN Rs485FlashHandleImp -> in flashPOSPRinter 2");
      this.initPrinter(handle, flashRequests, flashFileList);
   }

   protected void flashUsbBridge(Handle handle, String fName) throws FlashException {
      if (this.getTracer().isOn()) {
         this.getTracer().println("-->flashUsbBridge " + fName);
      }

      this.printerHandle = handle;
      FlashFile usbff = new UsbFlashFile(fName);
      boolean doUSBBridgeFlash = true;
      this.printerHandle.addHandleListener(this.printerFlashListener);
      File f = new File(fName);
      if (!f.exists()) {
         this.getTracer().println(".initPrinterSDICC-> usb file not exists");
         doUSBBridgeFlash = false;
      }

      this.usbPrinterRequest = new FlashRequest(handle, usbff);
      this.getTracer().println("flash() - printer flash file: " + usbff.getFilename());
      if (doUSBBridgeFlash) {
         handle.flash(this.usbPrinterRequest);
         this.getTracer().println("flash() printer's bridge - DONE!!!");
      }

      if (this.getTracer().isOn()) {
         this.getTracer().println("<--flashUsbBridge!");
      }
   }

   protected void flashMicrocode(Handle handle, String fName) throws FlashException {
      if (this.getTracer().isOn()) {
         this.getTracer().println("-->flashMicrocode " + fName);
      }

      this.printerHandle = handle;
      FlashFile rs485ff = new Rs485FlashFile(fName);
      this.printerHandle.addHandleListener(this.printerFlashListener);
      File f = new File(fName);
      if (!f.exists()) {
         this.getTracer().println(".initPrinterSDICC-> microcode file not exists");
         throw new FlashException("Microcode file not found");
      } else {
         this.getTracer().println("buildRs485FlashRequest() getPrinterInfo");
         this.getPrinterInfo(handle, rs485ff);
         this.rs485PrinterRequest = new FlashRequest(handle, rs485ff);
         this.getTracer().println("flash() - printer flash file: " + rs485ff.getFilename());
         this.rs485PrinterRequest.setRs485PrinterInfo(this.printerID, this.printerEC, this.printerType);
         if (!this.rs485PrinterRequest.validate4610FlashFileDevType(handle)) {
            this.getTracer().println(".initPrinterSDICC-> Flash File device type field does not match printer");
            throw new FlashException("Device type does not match with flash file selected");
         } else {
            handle.flash(this.rs485PrinterRequest);
            Rs485POSPrinterHandleImp rs485POSPrinterHandleImp = (Rs485POSPrinterHandleImp)handle.getHandleImp();
            IBMPrinterState ibmPrinterState = (IBMPrinterState)rs485POSPrinterHandleImp.getPrinterHandleState();
            ibmPrinterState.setECLevel(this.printerEC);
            if (this.getTracer().isOn()) {
               this.getTracer().println("<--flashMicrocode!");
            }
         }
      }
   }

   protected void checkForCmdRejectError(byte[] dataBytes) throws HandleException {
      byte statusByte = dataBytes[0];
      byte cmdRejectFlag = (byte)(statusByte & 128);
      this.getTracer().println(".checkForCmdRejectError() -statusByte:" + statusByte);
      if (0 != cmdRejectFlag) {
         throw new HandleException("Command Rejected by Flash");
      }
   }

   private byte getPrinterFeatureByte2() {
      return this.printerFeatureByte2;
   }

   protected void handleStatus(PrintStatus event) {
      this.getTracer().println("- ->Got Flash Data BACK!!!");
      this.newEcLevel = event.getECLevel();
      this.cmdCompleted.set(true);
      if (event.getStatus(896)) {
         this.waitStatus.set(true);
      }
   }

   protected void setMCDownload(Rs485FlashPrinterHandleImp.MCDownload microdeDwnld) {
      this.microdeDwnld = microdeDwnld;
   }

   protected Rs485FlashPrinterHandleImp.MCDownload getMCDownload() {
      return this.microdeDwnld;
   }

   protected void setTracer(Tracer tracer) {
      this.tracer = tracer;
   }

   protected Tracer getTracer() {
      return this.tracer;
   }

   interface MCDownload {
      void doFlash() throws FlashException;
   }

   class MCDownload4610 implements Rs485FlashPrinterHandleImp.MCDownload {
      public void doFlash() throws FlashException {
         Rs485FlashPrinterHandleImp.this.getTracer().println("MCDownload4610 - doFlash() - enter");
         if (Rs485FlashPrinterHandleImp.this.printerType != 3801
            || 2 != (Rs485FlashPrinterHandleImp.this.getPrinterFeatureByte2() & 2)
            || !Rs485FlashPrinterHandleImp.this.getRs485FlashFile().getFilename().endsWith(Rs485FlashPrinterHandleImp.this.T3_OR_T4_PRINTER_FILENAME)
               && !Rs485FlashPrinterHandleImp.this.getRs485FlashFile().getFilename().endsWith(Rs485FlashPrinterHandleImp.this.OTHER_PRINTER_FILENAME)) {
            Rs485FlashPrinterHandleImp.this.currentEraseCmd = Rs485FlashPrinterHandleImp.this.eraseFirmwareHSCmd;
            Rs485FlashPrinterHandleImp.this.currentLoadCmd = Rs485FlashPrinterHandleImp.this.loadFirmwareHSCmd;
            if (3819 == Rs485FlashPrinterHandleImp.this.getPrinterID()) {
               Rs485FlashPrinterHandleImp.this.currentEraseCmd = Rs485FlashPrinterHandleImp.this.eraseFirmwareCmd;
               Rs485FlashPrinterHandleImp.this.currentLoadCmd = Rs485FlashPrinterHandleImp.this.loadFirmwareCmd;
            }

            Rs485FlashPrinterHandleImp.this.getRs485FlashFile().load();
            FlashFormat ff = Rs485FlashPrinterHandleImp.this.getRs485FlashFile().getFlashFormat();
            if (FlashManager.getInstance().isBootSectorFlag()) {
               Rs485FlashFile rf = (Rs485FlashFile)Rs485FlashPrinterHandleImp.this.getRs485FlashFile();
               if (!rf.isBootSectorAvailable()) {
                  throw new FlashException("BootSector Data Not available");
               }

               Rs485FlashPrinterHandleImp.this.eraseBootSector();
            } else {
               Rs485FlashPrinterHandleImp.this.getTracer().println("MCDownload4610 - doFlash() - eraseCodeSector()");
               Rs485FlashPrinterHandleImp.this.eraseCodeSector();
            }

            Rs485FlashPrinterHandleImp.this.getTracer().println("MCDownload4610 - doFlash() - downloadFirmware()");
            Rs485FlashPrinterHandleImp.this.downloadFirmware(ff);
         } else {
            Rs485FlashPrinterHandleImp.this.getTracer()
               .println("MCDownload4610 - doFlash() -  Automatic flash for TI8 printer running as a TI3/4 is not implemented");
         }
      }
   }

   class MCDownload4689 implements Rs485FlashPrinterHandleImp.MCDownload {
      public void doFlash() throws FlashException {
         if (Rs485FlashPrinterHandleImp.this.getRs485FlashFile().getFilename().endsWith("aip4689.hex")) {
            Rs485FlashPrinterHandleImp.this.getTracer()
               .println(
                  "MCDownload4689 - doFlash() firmware: "
                     + Rs485FlashPrinterHandleImp.this.getRs485FlashFile().getFilename()
                     + " is not allowed for TI8 printers!"
               );
         } else {
            Rs485FlashPrinterHandleImp.this.getRs485FlashFile().load();
            Rs485FlashPrinterHandleImp.this.currentEraseCmd = Rs485FlashPrinterHandleImp.this.eraseFirmwareHSCmd;
            Rs485FlashPrinterHandleImp.this.currentLoadCmd = Rs485FlashPrinterHandleImp.this.loadFirmwareHSCmd;
            FlashFormat ff = Rs485FlashPrinterHandleImp.this.getRs485FlashFile().getFlashFormat();
            Rs485FlashPrinterHandleImp.this.getTracer().println("MCDownload4689 - doFlash() -download4689Firmware()");
            Rs485FlashPrinterHandleImp.this.downloadFirmware(ff);
            Rs485FlashPrinterHandleImp.this.getTracer().println("MCDownload4689 - doFlash() -eraseCodeSector()");
            Rs485FlashPrinterHandleImp.this.eraseCodeSector();
         }
      }
   }

   class MCDownloadCrabtree implements Rs485FlashPrinterHandleImp.MCDownload {
      public void doFlash() throws FlashException {
         if (!Rs485FlashPrinterHandleImp.this.getRs485FlashFile().getFilename().endsWith(Rs485FlashPrinterHandleImp.this.T3_OR_T4_PRINTER_FILENAME)
            && !Rs485FlashPrinterHandleImp.this.getRs485FlashFile().getFilename().endsWith(Rs485FlashPrinterHandleImp.this.OTHER_PRINTER_FILENAME)) {
            Rs485FlashPrinterHandleImp.this.getRs485FlashFile().load();
            Rs485FlashPrinterHandleImp.this.currentEraseCmd = Rs485FlashPrinterHandleImp.this.eraseFirmwareTI8Cmd;
            Rs485FlashPrinterHandleImp.this.currentLoadCmd = Rs485FlashPrinterHandleImp.this.loadFirmwareHSCmd;
            FlashFormat ff = Rs485FlashPrinterHandleImp.this.getRs485FlashFile().getFlashFormat();
            Rs485FlashPrinterHandleImp.this.getTracer().println("MCDownloadCrabtree - doFlash() -downloadCrabtreeFirmware()");
            Rs485FlashPrinterHandleImp.this.downloadFirmware(ff);
            Rs485FlashPrinterHandleImp.this.getTracer().println("MCDownloadCrabtree - doFlash() -eraseCodeSector()");
            Rs485FlashPrinterHandleImp.this.eraseCodeSector();
         } else {
            Rs485FlashPrinterHandleImp.this.getTracer()
               .println(
                  "MCDownloadCrabtree - doFlash() firmware: "
                     + Rs485FlashPrinterHandleImp.this.getRs485FlashFile().getFilename()
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
         Rs485FlashPrinterHandleImp.this.handleStatus((PrintStatus)event);
      }

      public void errorEventOccurred(ErrorEvent event) {
      }

      public void onlineEventOccurred(OnlineEvent event) {
      }

      public void offlineEventOccurred(OfflineEvent event) {
      }
   }
}
