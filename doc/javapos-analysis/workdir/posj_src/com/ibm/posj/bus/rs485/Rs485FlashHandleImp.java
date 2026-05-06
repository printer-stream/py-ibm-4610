package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.FlashHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashFile;
import com.ibm.posj.flash.FlashFormat;
import com.ibm.posj.flash.FlashRecord;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.flash.FlashUtil;
import com.ibm.posj.flash.Rs485FlashFile;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Rs485FlashHandleImp extends AbstractRs485HandleImp implements FlashHandleImp {
   FlashRequest flashRequest = null;
   private Handle handle = null;
   private HandleKey key = null;
   private SioDevice device = null;
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
   private Rs485FlashPrinterHandleImp rs485FlashPrinterHandleImp = null;
   private int printerFlashCount = 0;
   private Tracer tracer = TracerFactory.getInstance().createTracer("FLASH");
   public static final int ERR_STATUS_BYTE_INDEX = 0;
   public static final int FLASH_TIMEOUT = 10000;
   public static final int MAX_PACKET_SIZE = 528;

   public Rs485FlashHandleImp(HandleKey key, SioDevice device) {
      super(key, device);
      this.key = key;
      this.device = device;
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

   public void flashPOSPrinter(Handle handle, Vector flashRequests, ArrayList flashFileList) throws FlashException {
      this.tracer.println("IN Rs485FlashHandleImp -> in flashPOSPRinter 1");
      this.rs485FlashPrinterHandleImp = new Rs485FlashPrinterHandleImp(this.key, this.device);
      this.tracer.println("IN Rs485FlashHandleImp -> in flashPOSPRinter 2");
      this.rs485FlashPrinterHandleImp.initPrinter(handle, flashRequests, flashFileList);
   }

   public void flashPOSPrinterSDICC(Handle handle, String usbFileName, String rs485FileName) throws FlashException {
      this.rs485FlashPrinterHandleImp = new Rs485FlashPrinterHandleImp(this.key, this.device);
      this.rs485FlashPrinterHandleImp.initPrinterSDICC(handle, usbFileName, rs485FileName);
   }

   public Rs485FlashPrinterHandleImp getRs485FlashPrinterHandleImp() throws FlashException {
      if (null == this.rs485FlashPrinterHandleImp) {
         throw new FlashException("No printer flash HandleImp created yet");
      } else {
         return this.rs485FlashPrinterHandleImp;
      }
   }

   public void flash() throws FlashException {
      if (null != this.getFlashRequest()) {
         this.flashFile = this.getFlashRequest().getFlashFile();
         this.deviceECLevel = this.getFlashRequest().getDeviceECLevel();
         this.handle = this.getFlashRequest().getHandle();
         if (this.handle.getDevCat().toString().equals(DevCats.POSPRINTER_DEVCAT.toString())) {
            this.tracer.println("In HidFlashHandleImp -> flash -> calling executeFlash for Printer with file: " + this.flashFile.getFilename());
            if (this.flashFile instanceof Rs485FlashFile) {
               this.deviceECLevel = this.rs485FlashPrinterHandleImp.getPrinterEC();
            }

            if (null != this.flashFile) {
               if (null == this.rs485FlashPrinterHandleImp) {
                  this.rs485FlashPrinterHandleImp = new Rs485FlashPrinterHandleImp(this.key, this.device);
               }

               this.rs485FlashPrinterHandleImp.setPrinterHandle(this.handle);
               this.rs485FlashPrinterHandleImp.executeFlash(this.flashFile, this.deviceECLevel);
            }

            this.printerFlashCount++;
         } else {
            this.tracer.println("In HidFlashHandleImp -> flash -> calling executeFlash for " + this.handle);
            this.executeFlash(this.flashFile, this.deviceECLevel);
         }
      } else {
         this.tracer.println("Flash Request is NULL");
         throw new FlashException("No FlashRequest set before Flash called");
      }
   }

   protected void executeFlash(FlashFile flashFile, int deviceECLevel) throws FlashException {
      this.tracer.println("In HidFlashHandleImp -> executeFlash");
      if (!flashFile.isFlashFileVersionNewer(deviceECLevel)) {
         this.tracer
            .println(
               "In HidFlashHandleImp->executeFlash - "
                  + flashFile.getDevCat().toString()
                  + " does not need flashing, with device EC: "
                  + deviceECLevel
                  + " file EC: "
                  + flashFile.getVersion()
            );
      } else {
         this.tracer
            .println(
               "In HidFlashHandleImp-> Flashing "
                  + flashFile.getDevCat().toString()
                  + " from EC level "
                  + deviceECLevel
                  + " to EC Level "
                  + flashFile.getVersion()
            );
         this.submitCnt = 0;
         this.tracer.println("In HidFlashHandleImp - execute() send erase bytes");
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
            this.tracer.println("In HidFlashHandleImp -> executeFlash -> flashRecordData length = " + flashRecordData.length + " seq: " + dlSequence++);
            this.cmdCompleted.set(false);
            this.sendCmd(flashRecordData);
         }

         byte[] cs = flashFile.getCheckSum();
         this.checkSumBytes[0] = (byte)(this.checkSumBytes.length % 256);
         this.checkSumBytes[1] = (byte)(this.checkSumBytes.length / 256);
         this.checkSumBytes[2] = 2;
         this.checkSumBytes[3] = 0;
         this.checkSumBytes[4] = 0;
         this.checkSumBytes[5] = cs[1];
         this.checkSumBytes[6] = cs[0];
         this.tracer.println("In HidFlashHandleImp -> executeFlash -> sending checkSum");

         for (int i = 0; i < this.checkSumBytes.length; i++) {
            this.tracer.print(" " + this.checkSumBytes[i]);
         }

         this.tracer.println(" ");
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
         this.tracer.println("In HidFlashHandleImp - send reset()");
         this.tracer.println("in HidFlashHandleImp - send reset() - DONE!!!");

         try {
            this.cmdCompleted.waitForTrue(10000);
         } catch (Exception var2) {
            this.tracer.println("In HidFlashHandleImp -> Flash Timed out waiting for response following reset");
            throw new FlashException("Flash Timed out waiting for response following reset");
         }
      }
   }

   public short getProductID() throws FlashException {
      throw new FlashException("Product ID not supported for rs232 connections");
   }

   public String[] getFlashFileNames(Handle handle) {
      List rs485flashfilenames = new Vector();
      if (handle.getHandleImp().getDevCat() == DevCats.POSPRINTER_DEVCAT) {
         Rs485POSPrinterHandleImp rs485POSPrinterHandleImp = (Rs485POSPrinterHandleImp)handle.getHandleImp();
         int printerID = rs485POSPrinterHandleImp.getPrinterHandleState().getPrinterID();
         this.getPOSPrinterFlashFileNames(rs485flashfilenames);
      }

      return rs485flashfilenames.toArray(new String[rs485flashfilenames.size()]);
   }

   private void getPOSPrinterFlashFileNames(List flashFileNames) {
      flashFileNames.add(FlashUtil.T1_OR_T2_PRINTER_FILENAME);
      flashFileNames.add(FlashUtil.T3_OR_T4_PRINTER_FILENAME);
      flashFileNames.add(FlashUtil.TI5_PRINTER_FILENAME);
      flashFileNames.add(FlashUtil.CRABTREE_PRINTER_FILENAME);
   }

   protected void sendCmd(byte[] flashCmd) throws FlashException {
      this.tracer.println("in HidFlashHandleImp - sendCmd()");

      try {
         if (this.submitCnt > 0) {
            this.cmdCompleted.waitForTrue(10000);
         }
      } catch (Exception var3) {
         this.tracer.println("In HidFlashHandleImp -> Flash Timed out waiting for response");
         throw new FlashException("Flash Timed out waiting for response");
      }

      this.submitCnt++;
   }

   protected void checkForCmdRejectError(byte[] dataBytes) throws HandleException {
      byte statusByte = dataBytes[0];
      byte cmdRejectFlag = (byte)(statusByte & 128);
      this.tracer.println("in HidFlashHandleImp.checkForCmdRejectError - status byte:" + statusByte);
      if (0 != cmdRejectFlag) {
         this.tracer.println("THROWING COMMAND REJECT ERROR IN HIDFLASHHANDLEIMP!!!!!");
         throw new HandleException("Command Rejected by Flash");
      }
   }
}
