package com.ibm.embedded;

import com.ibm.jutil.Util;

public class GetSlotInfo {
   private long completionCode;
   private long adapterID;
   private long slotType;
   private long rc;
   private long slotDrawers;
   private long slotNVRAM;
   private long slotMotionSensor;
   public static final long ADAPTER_ID_4674 = 5L;
   public static final long ADAPTER_ID_4694 = 6L;
   public static final long ADAPTER_ID_SP300 = 665L;
   public static final long ADAPTER_ID_SP700 = 661L;
   public static final long ADAPTER_ID_SUREONE = 4614L;

   public GetSlotInfo(long completionCode, long adapterID, long slotType, long rc, long slotDrawers, long slotNVRAM, long slotMotionSensor) {
      this.completionCode = completionCode;
      this.adapterID = adapterID;
      this.slotType = slotType;
      this.rc = rc;
      this.slotDrawers = slotDrawers;
      this.slotNVRAM = slotNVRAM;
      this.slotMotionSensor = slotMotionSensor;
   }

   public long getCompletionCode() {
      return this.completionCode;
   }

   public long getAdapterID() {
      return this.adapterID;
   }

   public long getSlotType() {
      return this.slotType;
   }

   public long getReturnCode() {
      return this.rc;
   }

   public long getSlotDrawers() {
      return this.slotDrawers;
   }

   public long getSlotNVRAM() {
      return this.slotNVRAM;
   }

   public long getSlotMotionSensor() {
      return this.slotMotionSensor;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("<GetSlotInfo>");
      sb.append("\nadapterID : " + Util.toHexString(this.adapterID));
      sb.append("\nslotType : " + Util.toHexString(this.slotType));
      sb.append("\nslotDrawers : " + Util.toHexString(this.slotDrawers));
      sb.append("\nslotNVRAM : " + Util.toHexString(this.slotNVRAM));
      sb.append("\nslotMotionSensor : " + Util.toHexString(this.slotMotionSensor));
      sb.append("<\\GetSlotInfo>");
      return sb.toString();
   }
}
