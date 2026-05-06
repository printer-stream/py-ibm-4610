package com.ibm.posj.bus;

import com.ibm.jutil.Util;

public abstract class CashDrawerStatusByteParser {
   private byte status = 0;

   public void init(byte status) {
      this.status = status;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("<CDStatus =\"0x" + Util.toHexString(this.status) + "\">\n");
      sb.append("\tcd1Opened=\"" + this.isCD1Opened() + "\"\n");
      sb.append("\tcd2Opened=\"" + this.isCD2Opened() + "\"\n");
      sb.append("\tcd1Connected=\"" + this.isCD1Connected() + "\"\n");
      sb.append("\tcd2Connected=\"" + this.isCD2Connected() + "\"\n");
      sb.append("</CDStatus>");
      return sb.toString();
   }

   public int getCashDrawerStatusCode() {
      int cdStatus = -100;
      byte var2;
      if (this.isCDOpened()) {
         var2 = 1;
      } else {
         var2 = 0;
      }

      return var2;
   }

   public byte getStatusByte() {
      return this.status;
   }

   public abstract boolean isCDOpened();

   public abstract boolean isCD1Opened();

   public abstract boolean isCD2Opened();

   public abstract boolean isCDConnected();

   public abstract boolean isCD1Connected();

   public abstract boolean isCD2Connected();

   public boolean getUnsolicitedStatus() {
      return true;
   }
}
