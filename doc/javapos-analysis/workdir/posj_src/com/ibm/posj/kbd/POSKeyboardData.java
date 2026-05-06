package com.ibm.posj.kbd;

public class POSKeyboardData {
   private int data = 0;
   private int type = 0;

   public POSKeyboardData(int data, int type) {
      this.data = data;
      this.type = type;
   }

   public int getData() {
      return this.data;
   }

   public int getType() {
      return this.type;
   }
}
