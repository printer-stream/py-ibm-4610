package com.ibm.posj.util;

public class WaitObject {
   private boolean wasNotified = false;
   private boolean waiting = false;
   private int timeleft = -1;
   private String tostring = null;

   public WaitObject(String x) {
      this.tostring = x;
   }

   WaitObject() {
   }

   public String toString() {
      return this.tostring != null ? this.tostring : super.toString();
   }

   public void reset() {
      this.wasNotified = false;
      this.timeleft = -1;
   }

   public void doWait(int time) throws InterruptedException {
      this.waiting = true;
      long startTime = System.currentTimeMillis();
      if (time != -1) {
         super.wait(time > 0 ? (long)time : 1L);
         this.timeleft = (int)(System.currentTimeMillis() - startTime);
      } else {
         super.wait();
         this.timeleft = time;
      }

      if (this.timeleft > time) {
         this.timeleft = 0;
      }
   }

   public int getTimeLeft() {
      return this.timeleft;
   }

   public boolean notified() {
      return this.wasNotified;
   }

   public boolean isWaiting() {
      return this.waiting;
   }

   public void doNotify() throws IllegalMonitorStateException {
      super.notifyAll();
      this.waiting = false;
      this.wasNotified = true;
   }
}
