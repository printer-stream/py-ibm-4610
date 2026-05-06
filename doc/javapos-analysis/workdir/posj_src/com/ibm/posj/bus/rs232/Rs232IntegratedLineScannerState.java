package com.ibm.posj.bus.rs232;

import com.ibm.jutil.Timerable;

public abstract class Rs232IntegratedLineScannerState extends Rs232IntegratedScannerState implements Timerable {
   protected Rs232IntegratedLineScannerProtocol protocol;

   public Rs232IntegratedLineScannerState(Rs232IntegratedLineScannerProtocol scannerProtocol) {
      this.protocol = scannerProtocol;
   }

   public abstract void timerExpired();

   public void receiveHighLevelFrame(byte[] highLevelFrame) {
      this.protocol.submitToAdapter(Rs232IntegratedLineScannerPacketManager.ackFrame);
      if (Rs232IntegratedLineScannerPacketManager.getFrameNumber(highLevelFrame) != this.protocol.getScannerFrameNumber()) {
         if (this.protocol.getScannerFrameNumber() != -1 && !Rs232IntegratedLineScannerPacketManager.isRestartFrame(highLevelFrame)) {
            if (this.tracer.isOn()) {
               this.tracer.println("Frame Ignored!");
            }

            return;
         }

         this.protocol.setScannerFrameNumber(Rs232IntegratedLineScannerPacketManager.getFrameNumber(highLevelFrame));
      }

      this.protocol.incrementScannerFrameNumber();
      this.protocol.fireDataEvent(highLevelFrame);
   }

   public void receiveLowLevelFrame(byte[] lowLevelFrame) {
      if (this.tracer.isOn()) {
         this.tracer.println("Protocol Error - Low Level Frame unexpected in this state");
      }
   }
}
