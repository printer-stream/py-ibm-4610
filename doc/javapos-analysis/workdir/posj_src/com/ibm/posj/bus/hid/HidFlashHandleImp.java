package com.ibm.posj.bus.hid;

import com.ibm.hid.DisconnectEvent;
import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.HidExceptionEvent;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.FlashHandleImp;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.event.ErrorEvent;
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

public class HidFlashHandleImp extends AbstractHidHandleImp implements HidHandleImp, FlashHandleImp {
   FlashRequest flashRequest = null;
   private Handle handle = null;
   private HandleKey key = null;
   private HidDevice device = null;
   private FlashFile flashFile;
   protected FlashFile prtFlashFile;
   protected byte[] eraseBytes = new byte[]{4, 0, 1, 0, 0, 0};
   protected byte[] resetBytes = new byte[]{4, 0, 0, 64, 0, 0};
   protected byte[] checkSumBytes = new byte[7];
   protected static boolean needReset = false;
   protected static int dlSequence = 0;
   private int deviceECLevel = -1;
   private int submitCnt = 0;
   protected BooleanMonitor cmdCompleted = new BooleanMonitor(false);
   private byte[] hidFlashData = new byte[528];
   private HidFlashPrinterHandleImp hidFlashPrinterHandleImp = null;
   private int printerFlashCount = 0;
   private Tracer tracer = TracerFactory.getInstance().createTracer("FLASH");
   public static final int ERR_STATUS_BYTE_INDEX = 0;
   public static final int FLASH_TIMEOUT = 10000;
   public static final int MAX_PACKET_SIZE = 528;

   public HidFlashHandleImp(HandleKey key, HidDevice device) {
      super(key, device);
      this.key = key;
      this.device = device;
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidFlashHandleImp(this);
   }

   public void setPrtFlashFile(FlashFile prtFlashFile) {
      this.prtFlashFile = prtFlashFile;
   }

   public FlashFile getPrtFlashFile() {
      return this.prtFlashFile;
   }

   public void accept(HandleImpVisitor visitor) {
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
      this.tracer.println("IN HidFlashHandleImp -> in flashPOSPRinter 1");
      this.hidFlashPrinterHandleImp = new HidFlashPrinterHandleImp(this.key, this.device);
      this.tracer.println("IN HidFlashHandleImp -> in flashPOSPRinter 2");
      this.hidFlashPrinterHandleImp.initPrinter(handle, flashRequests, flashFileList);
   }

   public void flashPOSPrinterSDICC(Handle handle, String usbFileName, String rs485FileName) throws FlashException {
      this.hidFlashPrinterHandleImp = new HidFlashPrinterHandleImp(this.key, this.device);
      this.hidFlashPrinterHandleImp.initPrinterSDICC(handle, usbFileName, rs485FileName);
   }

   public HidFlashPrinterHandleImp getHidFlashPrinterHandleImp() throws FlashException {
      if (null == this.hidFlashPrinterHandleImp) {
         throw new FlashException("No printer flash HandleImp created yet");
      } else {
         return this.hidFlashPrinterHandleImp;
      }
   }

   public void flash() throws FlashException {
      needReset = false;
      if (null != this.getFlashRequest()) {
         this.flashFile = this.getFlashRequest().getFlashFile();
         this.deviceECLevel = this.getFlashRequest().getDeviceECLevel();
         this.handle = this.getFlashRequest().getHandle();
         if (this.handle.getDevCat().toString().equals(DevCats.POSPRINTER_DEVCAT.toString())) {
            this.tracer.println("In HidFlashHandleImp -> flash -> calling executeFlash for Printer with file: " + this.flashFile.getFilename());
            if (this.flashFile instanceof Rs485FlashFile) {
               this.deviceECLevel = this.hidFlashPrinterHandleImp.getPrinterEC();
            }

            if (null != this.flashFile) {
               if (null == this.hidFlashPrinterHandleImp) {
                  this.hidFlashPrinterHandleImp = new HidFlashPrinterHandleImp(this.key, this.device);
               }

               this.hidFlashPrinterHandleImp.setPrinterHandle(this.handle);
               this.hidFlashPrinterHandleImp.executeFlash(this.flashFile, this.deviceECLevel);
            }

            this.printerFlashCount++;
         } else {
            this.tracer.println("In HidFlashHandleImp -> flash -> calling executeFlash for " + this.handle);
            this.executeFlash(this.flashFile, this.deviceECLevel);
         }
      } else {
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

         try {
            this.getHidDevice().setReport((byte)2, (byte)0, (short)this.resetBytes.length, this.resetBytes);
         } catch (HidException var5) {
            this.tracer.println("Error while submitting flash reset cmd to device");

            try {
               Thread.sleep(10000L);
            } catch (InterruptedException var3) {
            }

            return;
         }

         this.tracer.println("in HidFlashHandleImp - send reset() - DONE!!!");

         try {
            this.cmdCompleted.waitForTrue(10000);
         } catch (Exception var4) {
            this.tracer.println("In HidFlashHandleImp -> Flash Timed out waiting for response following reset");
            throw new FlashException("Flash Timed out waiting for response following reset");
         }
      }
   }

   protected void sendCmd(byte[] flashCmd) throws FlashException {
      this.tracer.println("in HidFlashHandleImp - sendCmd()");

      try {
         this.tracer.println("in HidFlashHandleImp - Submitting Flash Packet to device");
         this.getHidDevice().setReport((byte)2, (byte)0, (short)flashCmd.length, flashCmd);
      } catch (HidException var3) {
         throw new FlashException("Error while submitting flash cmd to device", var3);
      }

      try {
         if (this.submitCnt > 0) {
            this.cmdCompleted.waitForTrue(10000);
         }
      } catch (Exception var4) {
         this.tracer.println("In HidFlashHandleImp -> Flash Timed out waiting for response");
         throw new FlashException("Flash Timed out waiting for response");
      }

      this.submitCnt++;
   }

   protected void reportEventOccurred(ReportEvent rE) {
      this.tracer.println("In HidFlashHandleImp ->Got Flash Data BACK!!!");
      this.cmdCompleted.set(true);
      this.processDataEvent(rE);
   }

   protected void hidExceptionEventOccurred(HidExceptionEvent heE) {
      this.tracer.println("IN HidFlashHandleImp -> got Flash ExceptionEvent!!!");
      this.cmdCompleted.set(true);
   }

   protected void hidDeviceDisconnected(DisconnectEvent dE) {
      this.cmdCompleted.set(true);
      this.tracer.println("In HidFlashHandleImp -> got Flash Disconnect Event!!!");
   }

   protected void processDataEvent(ReportEvent reportEvent) {
      byte[] dataBytes = reportEvent.getData();
      int dataLength = dataBytes.length;
      this.tracer.println("IN hidFalshHandleImp -> ProcessDataEvent");
      this.tracer.print("DATA = ");

      for (int i = 0; i < dataLength; i++) {
         this.tracer.print(dataBytes[i] + ",");
      }

      this.tracer.println("");
      Handle.EventHelper eventHelper = this.handle.getEventHelper();
      if (dataLength > 0) {
         try {
            this.checkForCmdRejectError(dataBytes);
         } catch (HandleException var6) {
            eventHelper.fireErrorEvent(new ErrorEvent(this, -101));
         }
      }
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

   public String[] getFlashFileNames(Handle handle) {
      List list = new Vector();
      String level = "";
      HidFlashHandleImp flashHandleImp = (HidFlashHandleImp)this.getFlashHandleImp();
      if (flashHandleImp.getVendorID() == 1203) {
         int intLevel = (flashHandleImp.getBCDLevel() & 3840) >>> 8;
         if (intLevel != 1) {
            level = String.valueOf(intLevel);
         }
      }

      String name = Integer.toHexString(this.getProductID());
      if (name.length() < 4) {
         name = "0000".substring(name.length()) + name;
      }

      if (handle.getDevCat().equals(DevCats.POSKEYBOARD_DEVCAT)) {
         HidPOSKeyboardHandleImp imp = (HidPOSKeyboardHandleImp)handle.getHandleImp();
         if (imp.isLegacyKbd() || imp.isNonBootModeOn()) {
            char[] values = new char[name.length()];

            for (int i = 0; i < values.length; i++) {
               if (i == 1) {
                  values[i] = '6';
               } else {
                  values[i] = name.charAt(i);
               }
            }

            name = new String(values);
         }
      }

      if (name.toLowerCase().equals(name.toUpperCase())) {
         list.add("aip" + name + level + ".dat");
      } else {
         list.add("aip" + name.toLowerCase() + level + ".dat");
         list.add("aip" + name.toUpperCase() + level + ".dat");
      }

      if (handle.getDevCat().equals(DevCats.POSPRINTER_DEVCAT)) {
         this.getPOSPrinterFlashFileNames(list);
      }

      return list.toArray(new String[list.size()]);
   }

   private void getPOSPrinterFlashFileNames(List flashFileNames) {
      flashFileNames.add(FlashUtil.T1_OR_T2_PRINTER_FILENAME);
      flashFileNames.add(FlashUtil.T3_OR_T4_PRINTER_FILENAME);
      flashFileNames.add(FlashUtil.TI5_PRINTER_FILENAME);
      flashFileNames.add(FlashUtil.CRABTREE_PRINTER_FILENAME);
      flashFileNames.add("aip4689.hex");
   }
}
