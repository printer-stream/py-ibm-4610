package com.ibm.posj.flash;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;
import com.ibm.posj.util.DevCat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class Rs485FlashFile implements FlashFile {
   private boolean bootSectorAvailable = false;
   private String fileName = null;
   private FlashFormat flashFormat = null;
   private int fileVersion;
   private int bootLevel;
   private int deviceType;
   private String ecLevelLine;
   private String bootLevelLine;
   private String devTypeLine;
   private Hashtable fileNameHashtable = new Hashtable();
   private DevCat devcat;
   private Tracer tracer = TracerFactory.getInstance().createTracer("FLASH", "Rs485FlashFile");

   public Rs485FlashFile(String fileName) throws FlashException {
      this.fileVersion = 0;
      this.bootLevel = 0;
      this.deviceType = 0;
      this.fileName = fileName;
      fileName = fileName.toLowerCase();
      File f = new File(fileName);
      this.devcat = FlashDevCatsTable.getInstance().getDevCat(f.getName());
      this.tracer.println("Constructor - fileName= " + fileName);
      if (!fileName.endsWith(".dat") && !fileName.endsWith(".hex") && !fileName.endsWith(".bt")) {
         this.tracer.println("Unable to load header");
      } else {
         this.loadHeader();
      }
   }

   public Rs485FlashFile(String fileName, boolean x) throws FlashException {
      this.fileVersion = 0;
      this.bootLevel = 0;
      this.deviceType = 0;
      this.fileName = fileName;
      fileName = fileName.toLowerCase();
      File f = new File(fileName);
      this.devcat = FlashDevCatsTable.getInstance().getDevCat(f.getName());
   }

   public void load() throws FlashException {
      try {
         BufferedReader in = new BufferedReader(new FileReader(this.fileName));
         if ((this.ecLevelLine = in.readLine()) != null && (this.bootLevelLine = in.readLine()) != null && (this.devTypeLine = in.readLine()) != null) {
         }

         this.flashFormat = new DefaultFlashFormat();

         String data;
         while ((data = in.readLine()) != null) {
            boolean isS1 = this.doBootSectorLoad(data);
            if (data.toUpperCase().startsWith("S2") || isS1) {
               String s1 = data.substring(2);
               byte[] record = this.convertRs485Record(s1, isS1);
               FlashRecord fr = new Rs485FlashRecord(record);
               this.flashFormat.add(fr);
            }
         }

         in.close();
      } catch (IOException var7) {
         throw new FlashException("USB load flashfile failed");
      }
   }

   public boolean equals(FlashFormat ff) {
      for (int i = 0; i < ff.size(); i++) {
         byte[] record1 = ff.get(i).getRecordData();
         byte[] record2 = this.flashFormat.get(i).getRecordData();
         if (record1.length != record2.length) {
            this.tracer.println("failed1:" + i);
            this.dumpHex(record1);
            this.dumpHex(record2);
            return false;
         }

         for (int j = 0; j < record1.length; j++) {
            if (record1[j] != record2[j]) {
               this.tracer.println("failed2:" + i);
               this.dumpHex(record1);
               this.dumpHex(record2);
               return false;
            }
         }
      }

      return true;
   }

   public void loadHeader() throws FlashException {
      try {
         this.readRs485Header(this.fileName);
         this.tracer.println(".loadHeader-ecLevelLine: " + this.ecLevelLine + " bootLevelLine: " + this.bootLevelLine + " devTypeLine: " + this.devTypeLine);
         if (this.ecLevelLine.startsWith("EcLevel")) {
            this.fileVersion = Integer.parseInt(this.getValueFromString(this.ecLevelLine), 16);
         }

         if (this.bootLevelLine.startsWith("BootLevel")) {
            this.bootLevel = Integer.parseInt(this.getValueFromString(this.bootLevelLine), 16);
         }

         if (this.devTypeLine != null && this.devTypeLine.startsWith("DevType")) {
            this.deviceType = Integer.parseInt(this.getValueFromString(this.devTypeLine), 16);
         } else {
            this.deviceType = 0;
         }
      } catch (IOException var2) {
         throw new FlashException("rs485 load header failed");
      } catch (Exception var3) {
         throw new FlashException(var3.toString());
      }
   }

   public String getFilename() {
      return this.fileName;
   }

   public FlashFormat getFlashFormat() {
      return this.flashFormat;
   }

   public boolean isFlashFileVersionNewer(int deviceVersion) {
      this.tracer
         .println(
            ".isFlashFileVersionNewer()-FileVersion: 0x" + Integer.toHexString(this.fileVersion) + " deviceVersion: 0x" + Integer.toHexString(deviceVersion)
         );
      return this.fileVersion > deviceVersion;
   }

   public boolean isProductIDMatched(int pID) {
      throw new RuntimeException("rs485 devices don't have product ID");
   }

   public boolean isUSBHardwareLevelMatched(int pHW) {
      throw new RuntimeException("rs485 devices don't have hardware level");
   }

   public boolean isDevBusMatched(DevBus devbus) {
      this.tracer.println(".isDevBusMatched()-return: " + (devbus == DevBuses.RS485_DEVBUS));
      return devbus == DevBuses.RS485_DEVBUS;
   }

   public boolean isDevCatMatched(DevCat devcat) {
      return this.devcat == devcat;
   }

   public int getUsbPid() {
      throw new RuntimeException("rs485 devices do not have product ID");
   }

   public int getVersion() {
      return this.fileVersion;
   }

   public int getRs485BootLevel() {
      return this.bootLevel;
   }

   public int getRs485DeviceType() {
      return this.deviceType;
   }

   public DevBus getDevBus() {
      return DevBuses.RS485_DEVBUS;
   }

   public DevCat getDevCat() {
      return this.devcat;
   }

   public byte[] getCheckSum() {
      return new byte[0];
   }

   public boolean isBootSectorAvailable() {
      return this.bootSectorAvailable;
   }

   private void setBootSectorAvailable(boolean b) {
      this.bootSectorAvailable = b;
   }

   private boolean doBootSectorLoad(String data) {
      boolean r = data.toUpperCase().startsWith("S1") && FlashManager.getInstance().isBootSectorFlag();
      if (!this.isBootSectorAvailable()) {
         this.setBootSectorAvailable(r);
      }

      return r;
   }

   private byte[] convertRs485Record(String s, boolean isS1) {
      byte[] rArry = this.convertStringToByteArray(s, isS1);
      byte[] b = new byte[rArry.length + 3 + 7];
      b[0] = b[1] = b[2] = 0;
      System.arraycopy(rArry, 0, b, 10, rArry.length);
      return b;
   }

   private void readRs485Header(String fileName) throws IOException {
      BufferedReader in = new BufferedReader(new FileReader(fileName));
      if ((this.ecLevelLine = in.readLine()) != null && (this.bootLevelLine = in.readLine()) != null && (this.devTypeLine = in.readLine()) != null) {
         in.close();
      }
   }

   private String getValueFromString(String s) {
      int i = 0;
      StringTokenizer st = new StringTokenizer(s);
      if (st.hasMoreTokens()) {
         st.nextToken();
      }

      return st.hasMoreTokens() ? st.nextToken() : "0";
   }

   private byte[] convertStringToByteArray(String s, boolean isS1) {
      int len = s.length();
      int recLen = this.parseHex(s, 0);
      int j = 0;
      byte[] b;
      if (isS1 && recLen % 2 != 0) {
         b = new byte[++recLen + 1];
         b[1] = 0;
         j = 2;
      } else {
         b = new byte[recLen + 1];
         j = 1;
      }

      b[0] = (byte)recLen;

      for (int i = 2; j < b.length; i += 2) {
         b[j++] = (byte)this.parseHex(s, i);
      }

      return b;
   }

   private short parseHex(String s, int pos) {
      String t = s.substring(pos, pos + 2);
      return Short.parseShort(t, 16);
   }

   private void dumpHex(byte[] buf) {
      for (int i = 0; i < buf.length; i++) {
         int hex = buf[i];
         if (hex < 0) {
            hex += 256;
         }

         if (hex >= 16) {
            this.tracer.println(Integer.toHexString(hex) + " ");
         } else {
            this.tracer.println("0" + Integer.toHexString(hex) + " ");
         }
      }

      this.tracer.println("\n");
   }
}
