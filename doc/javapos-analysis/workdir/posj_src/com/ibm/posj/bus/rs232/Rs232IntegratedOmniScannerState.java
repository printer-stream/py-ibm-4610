package com.ibm.posj.bus.rs232;

import com.ibm.jutil.Timerable;

public abstract class Rs232IntegratedOmniScannerState extends Rs232IntegratedScannerState implements Timerable {
   protected Rs232IntegratedOmniScannerProtocol protocol = null;
   private Rs232FiscalPrinterTimer stateTimer = null;

   Rs232IntegratedOmniScannerState(Rs232IntegratedOmniScannerProtocol protocol) {
      this.protocol = protocol;
   }

   protected void setTimmer(Rs232FiscalPrinterTimer timer, int timeout) {
      this.stateTimer = timer;
      this.stateTimer.setTime(timeout);
   }

   protected void startTimer() {
      if (this.stateTimer != null) {
         this.stateTimer.start();
         if (this.tracer.isOn()) {
            this.tracer.println(3, "Timer Started. -->" + System.currentTimeMillis());
         }
      }
   }

   protected void stopTimer() {
      if (this.stateTimer != null) {
         this.stateTimer.stop();
         if (this.tracer.isOn()) {
            this.tracer.println(3, "Timer Stopped. -->" + System.currentTimeMillis());
         }
      }
   }

   protected void restartTimer() {
      if (this.stateTimer != null) {
         this.stateTimer.stop();
         this.stateTimer.start();
         if (this.tracer.isOn()) {
            this.tracer.println(3, "Timer Restarted. -->" + System.currentTimeMillis());
         }
      }
   }

   public void timerExpired() {
      if (this.tracer.isOn()) {
         this.tracer.println(3, "Timer expired while not in a timerable state!" + System.currentTimeMillis());
      }
   }

   public int packetArrived() {
      return 2;
   }
}
