package com.ibm.posj.event;

import com.ibm.jutil.Util;

public class CheckScannerDataEvent extends DataEvent {
   private byte dataEventCode = 0;
   private short fLocation = 0;

   public CheckScannerDataEvent(Object source, byte[] data, byte code, short loc) {
      super(source, data);
      this.dataEventCode = code;
      this.fLocation = loc;
   }

   public byte getDataEventCode() {
      return this.dataEventCode;
   }

   public short getFileLocation() {
      return this.fLocation;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("<CheckScannerDataEvent:");
      sb.append("\n\t getData() =\"" + Util.toFormatedHexString(this.getData()));
      sb.append("\n\t getDataEventCode() = 0x" + Util.toHexString(this.getDataEventCode()));
      sb.append("\n\t getFileLocation() = 0x" + Util.toHexString(this.getFileLocation()));
      sb.append("\"/>\n");
      return sb.toString();
   }
}
