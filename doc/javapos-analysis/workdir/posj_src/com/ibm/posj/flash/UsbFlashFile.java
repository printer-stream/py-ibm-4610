package com.ibm.posj.flash;

import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCat;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

public class UsbFlashFile implements FlashFile {
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

   public UsbFlashFile(String fileName) {
      this.fileName = fileName;
      File f = new File(fileName);
      this.devcat = FlashDevCatsTable.getInstance().getDevCat(f.getName());

      try {
         this.loadHeader();
      } catch (FlashException var4) {
      }
   }

   public void load() throws FlashException {
      try {
         this.readUsbFlashFile(this.fileName);
         this.flashFormat = new DefaultFlashFormat();
         int dataIndex = 8;

         for (int length = this.data[dataIndex] & 255; length != 0; length = this.data[dataIndex] & 255) {
            int sizeToCopy = length + 4;
            int recordSize = length + 8;
            byte[] record = new byte[recordSize];
            record[0] = (byte)(recordSize % 256);
            record[1] = (byte)(recordSize / 256);
            record[2] = 2;
            record[3] = 0;
            System.arraycopy(this.data, dataIndex, record, 4, sizeToCopy);
            FlashRecord fr = new UsbFlashRecord(record);
            this.flashFormat.add(fr);
            dataIndex += sizeToCopy;
         }

         this.checkSum[0] = this.data[this.data.length - 1];
         this.checkSum[1] = this.data[this.data.length - 2];
      } catch (IOException var7) {
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
         this.readUsbHeader(this.fileName);
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

   public DevBus getDevBus() {
      return DevBuses.USB_DEVBUS;
   }

   public String getFilename() {
      return this.fileName;
   }

   public DevCat getDevCat() {
      return this.devcat;
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
      if (pID == 18433
         || pID == 18434
         || pID == 18435
         || pID == 18436
         || pID == 18437
         || pID == 18438
         || pID == 18439
         || pID == 18449
         || pID == 18450
         || pID == 18454) {
         return this.productID + 512 == pID;
      } else if (pID == 18177
         || pID == 18178
         || pID == 18179
         || pID == 18180
         || pID == 18181
         || pID == 18182
         || pID == 18183
         || pID == 18193
         || pID == 18194
         || pID == 18198) {
         return this.productID + 256 == pID;
      } else if (pID == 18544 || pID == 18545 || pID == 18546 || pID == 18547 || pID == 18548) {
         return this.productID + 512 == pID;
      } else if (pID == 18288 || pID == 18289 || pID == 18290 || pID == 18291 || pID == 18292) {
         return this.productID + 256 == pID;
      } else {
         return pID == 17706 ? true : this.productID == pID;
      }
   }

   public boolean isUSBHardwareLevelMatched(int pHW) {
      return (this.fileVersion & 256) == (pHW & 256);
   }

   public boolean isDevBusMatched(DevBus devbus) {
      return devbus == DevBuses.USB_DEVBUS;
   }

   public boolean isDevCatMatched(DevCat devcat) {
      return this.devcat == devcat;
   }

   public int getUsbPid() {
      return this.productID;
   }

   public int getVersion() {
      return this.fileVersion;
   }

   public int getRs485BootLevel() {
      throw new RuntimeException("USB devices don't have boot level");
   }

   public int getRs485DeviceType() {
      throw new RuntimeException("USB devices don't have device type");
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
