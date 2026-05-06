package com.ibm.posj.util;

public class Counter {
   int cnt = 0;

   public Counter() {
   }

   public Counter(int x) {
      this.incrementCnt(x);
   }

   public void reset() {
      this.cnt = 0;
   }

   public void incrementCnt(int incrementBy) {
      this.cnt += incrementBy;
   }

   public void decrementCnt(int decrementBy) {
      this.cnt -= decrementBy;
   }

   public int getCnt() {
      return this.cnt;
   }
}
