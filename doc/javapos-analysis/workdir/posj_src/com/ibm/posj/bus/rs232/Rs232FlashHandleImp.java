package com.ibm.posj.bus.rs232;

import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.FlashHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.printer.cmds.ibm4610.Rs232Cmd4610;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashFile;
import com.ibm.posj.flash.FlashFormat;
import com.ibm.posj.flash.FlashRecord;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.flash.FlashUtil;
import com.ibm.posj.flash.Rs485FlashFile;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.rs232.Rs232Exception;
import com.ibm.rs232.Rs232Port;
import com.ibm.rs232.event.Rs232DataEvent;
import com.ibm.rs232.event.Rs232ErrorEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Rs232FlashHandleImp extends AbstractRs232HandleImp implements FlashHandleImp {
   FlashRequest flashRequest = null;
   Rs232Cmd4610 prCmds = new Rs232Cmd4610();
   private Handle handle = null;
   private HandleKey key = null;
   private Rs232Port port = null;
   private FlashFile flashFile;
   protected FlashFile prtFlashFile;
   protected byte[] eraseBytes = new byte[]{4, 0, 1, 0};
   protected byte[] resetBytes = new byte[]{4, 0, 0, 64};
   protected byte[] checkSumBytes = new byte[7];
   protected static boolean needReset = false;
   protected static int dlSequence = 0;
   private int deviceECLevel = -1;
   private int submitCnt = 0;
   protected BooleanMonitor cmdCompleted = new BooleanMonitor(false);
   private byte[] hidFlashData = new byte[528];
   private Rs232FlashPrinterHandleImp rs232FlashPrinterHandleImp = null;
   private int printerFlashCount = 0;
   private Tracer tracer = TracerFactory.getInstance().createTracer("FLASH", "Rs232FlashHandleImp");
   public static final int ERR_STATUS_BYTE_INDEX = 0;
   public static final int FLASH_TIMEOUT = 10000;
   public static final int MAX_PACKET_SIZE = 528;

   public Rs232FlashHandleImp(HandleKey key, Rs232Port port) {
      super(key, port);
      this.key = key;
      this.port = port;
   }

   public void accept(HandleImpVisitor visitor) {
   }

   public FlashFile getPrtFlashFile() {
      return this.prtFlashFile;
   }

   public void setPrtFlashFile(FlashFile prtFlashFile) {
      this.prtFlashFile = prtFlashFile;
   }

   public DevCat getDevCat() {
      return DevCats.UNKNOWN_DEVCAT;
   }

   public void setFlashRequest(FlashRequest flashRequest) {
      this.flashRequest = flashRequest;
   }

   public FlashRequest getFlashRequest() {
      return this.flashRequest;
   }

   public void flashPOSPrinter(Handle handle, Vector flashRequests, ArrayList rs485FlashFileList) throws FlashException {
      this.tracer.println("in flashPOSPRinter 1");
      this.rs232FlashPrinterHandleImp = (Rs232FlashPrinterHandleImp)handle.getHandleImp().getFlashHandleImp();
      this.tracer.println("in flashPOSPRinter 2");
      this.rs232FlashPrinterHandleImp.initPrinter(handle, flashRequests, rs485FlashFileList);
   }

   public void flashPOSPrinterSDICC(Handle handle, String usbFileName, String rs485FileName) throws FlashException {
      this.rs232FlashPrinterHandleImp = new Rs232FlashPrinterHandleImp(this.key, this.port);
      this.rs232FlashPrinterHandleImp.initPrinterSDICC(handle, usbFileName, rs485FileName);
   }

   public Rs232FlashPrinterHandleImp getRs232FlashPrinterHandleImp() throws FlashException {
      if (null == this.rs232FlashPrinterHandleImp) {
         throw new FlashException("No printer flash HandleImp created yet");
      } else {
         return this.rs232FlashPrinterHandleImp;
      }
   }

   public void flash() throws FlashException {
      if (null != this.getFlashRequest()) {
         this.flashFile = this.getFlashRequest().getFlashFile();
         this.deviceECLevel = this.getFlashRequest().getDeviceECLevel();
         this.handle = this.getFlashRequest().getHandle();
         if (this.handle.getDevCat().toString().equals(DevCats.POSPRINTER_DEVCAT.toString())) {
            this.tracer.println(".flash -> calling executeFlash for Printer with file: " + this.flashFile);
            if (this.flashFile instanceof Rs485FlashFile) {
               this.deviceECLevel = this.rs232FlashPrinterHandleImp.getPrinterEC();
            }

            if (null != this.flashFile) {
               if (null == this.rs232FlashPrinterHandleImp) {
                  this.rs232FlashPrinterHandleImp = new Rs232FlashPrinterHandleImp(this.key, this.port);
               }

               this.rs232FlashPrinterHandleImp.setPrinterHandle(this.handle);
               this.rs232FlashPrinterHandleImp.executeFlash(this.flashFile, this.deviceECLevel);
            }

            this.printerFlashCount++;
         } else {
            this.tracer.println(".flash -> calling executeFlash for " + this.handle);
            this.executeFlash(this.flashFile, this.deviceECLevel);
         }
      } else {
         throw new FlashException("No FlashRequest set before Flash called");
      }
   }

   protected void executeFlash(FlashFile flashFile, int deviceECLevel) throws FlashException {
      this.tracer.println("executeFlash");
      if (!flashFile.isFlashFileVersionNewer(deviceECLevel)) {
         this.tracer
            .println(
               "executeFlash - "
                  + flashFile.getDevCat().toString()
                  + " does not need flashing, with device EC: "
                  + deviceECLevel
                  + " file EC: "
                  + flashFile.getVersion()
            );
      } else {
         this.tracer.println("Flashing " + flashFile.getDevCat().toString() + " from EC level " + deviceECLevel + " to EC Level " + flashFile.getVersion());
         this.submitCnt = 0;
         this.tracer.println(" - execute() send erase bytes");
         this.cmdCompleted.set(false);
         this.sendCmd(this.eraseBytes);

         try {
            Thread.currentThread();
            Thread.sleep(2000L);
         } catch (InterruptedException var8) {
         }

         needReset = false;
         flashFile.load();
         FlashFormat ff = flashFile.getFlashFormat();

         for (int i = 0; i < ff.size(); i++) {
            FlashRecord fr = ff.get(i);
            byte[] flashRecordData = fr.getRecordData();
            this.tracer.println("executeFlash -> flashRecordData length = " + flashRecordData.length + " seq: " + dlSequence++);
            this.cmdCompleted.set(false);
            this.sendCmd(flashRecordData);
         }

         byte[] cs = flashFile.getCheckSum();
         this.checkSumBytes[0] = (byte)(this.checkSumBytes.length % 256);
         this.checkSumBytes[1] = (byte)(this.checkSumBytes.length / 256);
         this.checkSumBytes[2] = 2;
         this.checkSumBytes[3] = 0;
         this.checkSumBytes[4] = 0;
         this.checkSumBytes[5] = cs[0];
         this.checkSumBytes[6] = cs[1];
         this.tracer.println("executeFlash -> sending checkSum");
         this.cmdCompleted.set(false);
         this.sendCmd(this.checkSumBytes);
         needReset = true;

         try {
            Thread.currentThread();
            Thread.sleep(4000L);
         } catch (InterruptedException var7) {
         }
      }
   }

   public void reset() throws FlashException {
      if (needReset) {
         this.cmdCompleted.set(false);
         this.tracer.println("send reset()");

         try {
            this.port.submit(this.prCmds.RESET);
         } catch (Rs232Exception var3) {
            throw new FlashException("Error while submitting flash reset cmd to device", var3);
         }

         this.tracer.println("send reset() - DONE!!!");

         try {
            this.cmdCompleted.waitForTrue(10000);
         } catch (Exception var2) {
            this.tracer.println("Flash Timed out waiting for response following reset");
            throw new FlashException("Flash Timed out waiting for response following reset");
         }
      }
   }

   public short getProductID() throws FlashException {
      throw new FlashException("Product ID not supported for rs232 connections");
   }

   protected void sendCmd(byte[] flashCmd) throws FlashException {
      this.tracer.println("sendCmd()");

      try {
         this.tracer.println("Submitting Flash Packet to device");
         this.port.submit(flashCmd);
      } catch (Rs232Exception var3) {
         throw new FlashException("Error while submitting flash cmd to device", var3);
      }

      try {
         if (this.submitCnt > 0) {
            this.cmdCompleted.waitForTrue(10000);
         }
      } catch (Exception var4) {
         this.tracer.println("Flash Timed out waiting for response");
         throw new FlashException("Flash Timed out waiting for response");
      }

      this.submitCnt++;
   }

   protected void sendCmd(byte[] flashCmd, int time) throws FlashException {
      this.tracer.println("sendCmd()");

      try {
         this.tracer.println("Submitting Flash Packet to device");
         this.port.submit(flashCmd);
      } catch (Rs232Exception var4) {
         throw new FlashException("Error while submitting flash cmd to device", var4);
      }

      try {
         if (this.submitCnt > 0) {
            this.cmdCompleted.waitForTrue(time);
         }
      } catch (Exception var5) {
         this.tracer.println("Flash Timed out waiting for response");
         throw new FlashException("Flash Timed out waiting for response");
      }

      this.submitCnt++;
   }

   protected void sendCmd(byte[] flashCmd, int offset, int len) throws FlashException {
      this.tracer.println("sendCmd()");

      try {
         this.tracer.println("Submitting Flash Packet to device");
         this.port.submit(flashCmd, offset, len);
      } catch (Rs232Exception var5) {
         throw new FlashException("Error while submitting flash cmd to device", var5);
      }

      try {
         if (this.submitCnt > 0) {
            this.cmdCompleted.waitForTrue(10000);
         }
      } catch (Exception var6) {
         this.tracer.println("Flash Timed out waiting for response");
         throw new FlashException("Flash Timed out waiting for response");
      }

      this.submitCnt++;
   }

   public void dataEventOccurred(Rs232DataEvent rs232DataEvent) {
   }

   public void errorEventOccurred(Rs232ErrorEvent rs232ErrorEvent) {
   }

   protected void checkForCmdRejectError(byte[] dataBytes) throws HandleException {
      byte statusByte = dataBytes[0];
      byte cmdRejectFlag = (byte)(statusByte & 128);
      this.tracer.println(".checkForCmdRejectError -status byte:" + statusByte);
      if (0 != cmdRejectFlag) {
         this.tracer.println("THROWING COMMAND REJECT ERROR IN RS232FLASHHANDLEIMP!!!!!");
         throw new HandleException("Command Rejected by Flash");
      }
   }

   public boolean isFlashable() {
      return true;
   }

   public short getECLevel() {
      return 0;
   }

   public String[] getFlashFileNames(Handle handle) {
      List rs232flashfilenames = new Vector();
      if (handle.getHandleImp().getDevCat() == DevCats.POSPRINTER_DEVCAT) {
         Rs232POSPrinterHandleImp rs232POSPrinterHandleImp = (Rs232POSPrinterHandleImp)handle.getHandleImp();
         int printerID = rs232POSPrinterHandleImp.getPrinterHandleState().getPrinterID();
         this.getPOSPrinterFlashFileNames(rs232flashfilenames);
      }

      return rs232flashfilenames.toArray(new String[rs232flashfilenames.size()]);
   }

   private void getPOSPrinterFlashFileNames(List flashFileNames) {
      flashFileNames.add(FlashUtil.T1_OR_T2_PRINTER_FILENAME);
      flashFileNames.add(FlashUtil.T3_OR_T4_PRINTER_FILENAME);
      flashFileNames.add(FlashUtil.TI5_PRINTER_FILENAME);
      flashFileNames.add(FlashUtil.CRABTREE_PRINTER_FILENAME);
   }
}
