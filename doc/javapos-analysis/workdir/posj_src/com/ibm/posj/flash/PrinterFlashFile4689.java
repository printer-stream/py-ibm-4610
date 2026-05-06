package com.ibm.posj.flash;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.posj.bus.printer.cmds.ibm4689.Cmd4689;
import com.ibm.posj.util.DevCat;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

public class PrinterFlashFile4689 extends Rs485FlashFile {
   private String fileName = null;
   private FlashFormat flashFormat = null;
   private byte[] data = new byte[0];
   private int productID = 0;
   private int fileVersion = 0;
   private byte[] header = new byte[0];
   private byte[] checkSum = new byte[2];
   private int chunkSize = 8192;
   private int headerSize = 8;
   private DevCat devcat;
   private Hashtable fileNameHashtable = new Hashtable();

   public PrinterFlashFile4689(String fileName) throws FlashException {
      super(fileName, true);
      this.loadHeader();
   }

   public void load() throws FlashException {
      try {
         String fileName = this.getFilename();
         this.readUsbFlashFile(fileName);
         this.flashFormat = new DefaultFlashFormat();
         int dataIndex = 8;
         int length = this.data[dataIndex] & 255;

         for (Cmd4689 cmds = new Cmd4689(); length != 0; length = this.data[dataIndex] & 255) {
            int sizeToCopy = length + 4;
            int cmdLen = cmds.DOWNLOAD_FIRMWARE.length;
            byte[] record = new byte[sizeToCopy + cmdLen + 1];
            System.arraycopy(cmds.DOWNLOAD_FIRMWARE, 0, record, 0, cmdLen);
            System.arraycopy(this.data, dataIndex, record, cmdLen, length);
            FlashRecord fr = new UsbFlashRecord(record);
            this.flashFormat.add(fr);
            dataIndex += sizeToCopy;
         }

         this.checkSum[0] = this.data[this.data.length - 1];
         this.checkSum[1] = this.data[this.data.length - 2];
      } catch (Exception var9) {
         if (Tracer.getInstance().isOn()) {
            Tracer.getInstance().print(var9);
         }

         throw new FlashException("USB load flashfile failed");
      }
   }

   public byte[] getCheckSum() {
      return this.checkSum;
   }

   public boolean equals(FlashFormat ff) {
      for (int i = 0; i < ff.size(); i++) {
         byte[] record1 = ff.get(i).getRecordData();
         byte[] record2 = this.flashFormat.get(i).getRecordData();
         if (record1.length != record2.length) {
            return false;
         }

         for (int j = 0; j < record1.length; j++) {
            if (record1[j] != record2[j]) {
               return false;
            }
         }
      }

      return true;
   }

   public void loadHeader() throws FlashException {
      try {
         this.readUsbHeader(this.getFilename());
         byte[] fVersion = new byte[4];
         System.arraycopy(this.header, 4, fVersion, 0, 4);
         this.fileVersion = this.convertAsciiArrayToInt(fVersion);
         byte[] pID = new byte[4];
         System.arraycopy(this.header, 0, pID, 0, 4);
         this.productID = this.convertAsciiArrayToInt(pID);
      } catch (IOException var3) {
         throw new FlashException("USB load header failed");
      }
   }

   public FlashFormat getFlashFormat() {
      return this.flashFormat;
   }

   public boolean isFlashFileVersionNewer(int deviceVersion) {
      byte[] fileVersionArray = this.convertIntToByteArray(this.fileVersion);
      byte[] deviceVersionArray = this.convertIntToByteArray(deviceVersion);
      if (fileVersionArray[1] == 2 && deviceVersionArray[1] == 1) {
         return false;
      } else {
         return fileVersionArray[1] == 1 && deviceVersionArray[1] == 2 ? false : this.fileVersion > deviceVersion;
      }
   }

   public boolean isProductIDMatched(int pID) {
      return this.productID == pID;
   }

   public boolean isUSBHardwareLevelMatched(int pHW) {
      return (this.fileVersion & 256) == (pHW & 256);
   }

   public int getUsbPid() {
      return this.productID;
   }

   public int getVersion() {
      return this.fileVersion;
   }

   private int convertAsciiArrayToInt(byte[] a) {
      return (a[0] & 207) * 4096 + (a[1] & 207) * 256 + (a[2] & 207) * 16 + (a[3] & 207);
   }

   private byte[] convertIntToByteArray(int n) {
      byte[] bArray = new byte[4];
      int r = n;
      int cnt = 4096;

      for (int i = 0; i < bArray.length; i++) {
         bArray[i] = (byte)(r / cnt);
         r %= cnt;
         cnt /= 16;
      }

      return bArray;
   }

   private void readUsbFlashFile(String fileName) throws IOException {
      File f = new File(fileName);
      int fileSize = (int)f.length();
      this.data = new byte[fileSize];
      DataInputStream in = new DataInputStream(new FileInputStream(f));
      in.readFully(this.data);
      in.close();
   }

   private void readUsbHeader(String fileName) throws IOException {
      this.header = new byte[this.headerSize];
      FileInputStream in = new FileInputStream(fileName);
      in.read(this.header);
      in.close();
   }
}
