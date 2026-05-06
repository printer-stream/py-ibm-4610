package com.ibm.embedded;

public class VitalProductData {
   private long completionCode;
   private char[] vpd;

   public VitalProductData(long completionCode, char[] vpd) {
      this.completionCode = completionCode;
      this.vpd = vpd;
   }

   public long getCompletionCode() {
      return this.completionCode;
   }

   public char[] getVPD() {
      return this.vpd;
   }
}
