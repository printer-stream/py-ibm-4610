package com.ibm.embedded;

import com.ibm.jutil.Util;

public class GetNvramInfo {
   private int completionCode;
   private int slotNumber;
   private int builtIn;
   private int numberAreas;
   private int totalSize;
   private int[] offset;
   private int[] size;
   private int[] inUseFlag;

   public GetNvramInfo(int completionCode, int slotNumber, int builtIn, int numberAreas, int totalSize, int[] offset, int[] size, int[] inUseFlag) {
      this.completionCode = completionCode;
      this.slotNumber = slotNumber;
      this.builtIn = builtIn;
      this.numberAreas = numberAreas;
      this.totalSize = totalSize;
      this.offset = offset;
      this.size = size;
      this.inUseFlag = inUseFlag;
   }

   public int getCompletionCode() {
      return this.completionCode;
   }

   public int getSlotNumber() {
      return this.slotNumber;
   }

   public int getBuiltIn() {
      return this.builtIn;
   }

   public int getNumberAreas() {
      return this.numberAreas;
   }

   public int getTotalSize() {
      return this.totalSize;
   }

   public int[] getOffset() {
      return this.offset;
   }

   public int[] getSize() {
      return this.size;
   }

   public int[] getInUseFlag() {
      return this.inUseFlag;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("<GetNvramInfo>");
      sb.append("\nslotNumber : " + Util.toHexString(this.slotNumber));
      sb.append("\nbuiltIn : " + Util.toHexString(this.builtIn));
      sb.append("\nnumberAreas : " + Util.toHexString(this.numberAreas));
      sb.append("\ntotalSize : " + Util.toHexString(this.totalSize));
      sb.append("\noffset[] : " + Util.toFormatedHexString(this.offset));
      sb.append("\nsize[] : " + Util.toFormatedHexString(this.size));
      sb.append("\ninUseFlag[] : " + Util.toFormatedHexString(this.inUseFlag));
      sb.append("\n<\\GetNvramInfo>");
      return sb.toString();
   }
}
