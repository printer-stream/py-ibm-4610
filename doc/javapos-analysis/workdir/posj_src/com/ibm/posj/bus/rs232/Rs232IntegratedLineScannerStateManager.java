package com.ibm.posj.bus.rs232;

public class Rs232IntegratedLineScannerStateManager {
   private Rs232IntegratedLineScannerStateManager.StateSynchronize stateSynchronize = null;
   private Rs232IntegratedLineScannerStateManager.StateIdle stateIdle = null;
   private Rs232IntegratedLineScannerStateManager.StateWaitingAck stateWaitingAck = null;
   private Rs232IntegratedLineScannerStateManager.StateWaitingHighLevelResponse stateWaitingHighLevelResponse = null;
   private Rs232IntegratedLineScannerStateManager.StateCommunicationError stateCommunicationError = null;
   private Rs232IntegratedLineScannerState currentState = null;

   public Rs232IntegratedLineScannerStateManager(Rs232IntegratedLineScannerProtocol protocol) {
      this.stateSynchronize = new Rs232IntegratedLineScannerStateManager.StateSynchronize(protocol);
      this.stateIdle = new Rs232IntegratedLineScannerStateManager.StateIdle(protocol);
      this.stateWaitingAck = new Rs232IntegratedLineScannerStateManager.StateWaitingAck(protocol);
      this.stateWaitingHighLevelResponse = new Rs232IntegratedLineScannerStateManager.StateWaitingHighLevelResponse(protocol);
      this.stateCommunicationError = new Rs232IntegratedLineScannerStateManager.StateCommunicationError(protocol);
      this.setIdleState();
   }

   public void setSynchronizeState() {
      this.currentState = this.stateSynchronize;
      this.currentState.set();
   }

   public void setIdleState() {
      this.currentState = this.stateIdle;
      this.currentState.set();
   }

   public void setWaitingForAckState() {
      this.currentState = this.stateWaitingAck;
      this.currentState.set();
   }

   public void setWaitingHighLevelResponseState() {
      this.currentState = this.stateWaitingHighLevelResponse;
      this.currentState.set();
   }

   public void setCommunicationErrorState() {
      this.currentState = this.stateCommunicationError;
      this.currentState.set();
   }

   protected Rs232IntegratedLineScannerState getCurrentState() {
      return this.currentState;
   }

   private class StateCommunicationError extends Rs232IntegratedLineScannerState {
      public StateCommunicationError(Rs232IntegratedLineScannerProtocol scannerProtocol) {
         super(scannerProtocol);
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println("-->stateCommunicationError<--");
         }

         if (this.protocol.isRecoveringEnabled()) {
            if (this.protocol.isRecovering()) {
               Rs232IntegratedLineScannerStateManager.this.setIdleState();
            } else {
               this.protocol.startRecovering();
            }
         }
      }

      public void receiveHighLevelFrame(byte[] highLevelFrame) {
         if (this.tracer.isOn()) {
            this.tracer.println("High level Frame ignored in stateCommunicationError");
         }
      }

      public void timerExpired() {
      }

      public int write(byte[] buffer) {
         return 3;
      }
   }

   private class StateIdle extends Rs232IntegratedLineScannerState {
      public StateIdle(Rs232IntegratedLineScannerProtocol scannerProtocol) {
         super(scannerProtocol);
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println("-->stateIdle<--");
         }
      }

      public void timerExpired() {
      }

      public int write(byte[] buffer) {
         byte[] highLevelFrame = Rs232IntegratedLineScannerPacketManager.createHighLevelFrame(
            this.protocol.getSequenceNumber(), buffer, this.protocol.getFrameManagement()
         );
         this.protocol.setLastHighLevelFrame(highLevelFrame);
         this.protocol.getStateManager().setWaitingForAckState();
         this.protocol.submitToAdapter(highLevelFrame);
         return 1;
      }
   }

   private class StateSynchronize extends Rs232IntegratedLineScannerState {
      public StateSynchronize(Rs232IntegratedLineScannerProtocol scannerProtocol) {
         super(scannerProtocol);
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println("-->stateSynchronize<--");
         }

         this.protocol.setAttemptsCounter(1);
         this.protocol.submitToAdapter(Rs232IntegratedLineScannerPacketManager.autoSyncFrame);
         this.protocol.restartTimer(1000, Rs232IntegratedLineScannerStateManager.this.currentState);
      }

      public void timerExpired() {
         if (this.protocol.getAttemptsCounter() >= 10) {
            this.protocol.getStateManager().setCommunicationErrorState();
         } else {
            this.protocol.stopTimer();
            this.protocol.submitToAdapter(Rs232IntegratedLineScannerPacketManager.autoSyncFrame);
            this.protocol.setAttemptsCounter(this.protocol.getAttemptsCounter() + 1);
            this.protocol.restartTimer(1000, this);
         }
      }

      public void receiveLowLevelFrame(byte[] lowLevelFrame) {
         if (Rs232IntegratedLineScannerPacketManager.getFrameType(lowLevelFrame) == 12) {
            this.protocol.setAttemptsCounter(1);
            this.protocol.stopTimer();
            this.protocol.getStateManager().setIdleState();
         }
      }
   }

   private class StateWaitingAck extends Rs232IntegratedLineScannerState {
      private static final byte TYPE_BYTE = 1;
      private static final byte RESULT_TYPE = 81;
      private static final byte RESULT_ID_BYTE = 2;
      private static final byte RESULT_DONE = 0;

      public StateWaitingAck(Rs232IntegratedLineScannerProtocol scannerProtocol) {
         super(scannerProtocol);
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println("-->stateWaitingAck<--");
         }

         this.protocol.setAttemptsCounter(1);
         this.protocol.restartTimer(1000, this);
      }

      protected void resend() {
         if (this.protocol.getAttemptsCounter() >= 5) {
            this.protocol.getStateManager().setCommunicationErrorState();
         } else {
            this.protocol.stopTimer();
            this.protocol.submitToAdapter(this.protocol.getLastHighLevelFrame());
            this.protocol.setAttemptsCounter(this.protocol.getAttemptsCounter() + 1);
            this.protocol.restartTimer(1000, this);
         }
      }

      public void timerExpired() {
         if (this.tracer.isOn()) {
            this.tracer.println("-->timerExpired " + System.currentTimeMillis());
         }

         this.resend();
         if (this.tracer.isOn()) {
            this.tracer.println("<--timerExpired");
         }
      }

      public void receiveLowLevelFrame(byte[] lowLevelFrame) {
         switch (Rs232IntegratedLineScannerPacketManager.getFrameType(lowLevelFrame)) {
            case 6:
               this.protocol.clearRestart();
               this.protocol.incrementHostFrameNumber();
               this.protocol.stopTimer();
               this.protocol.getStateManager().setWaitingHighLevelResponseState();
               break;
            case 21:
               this.resend();
               break;
            case 27:
               try {
                  Thread.sleep(400L);
               } catch (InterruptedException var3) {
               }

               this.resend();
         }
      }

      public void receiveHighLevelFrame(byte[] highLevelFrame) {
         this.protocol.stopTimer();
         super.receiveHighLevelFrame(highLevelFrame);
         if (highLevelFrame[1] == 81 && highLevelFrame[2] == 0) {
            this.protocol.incrementHostFrameNumber();
            this.protocol.stopTimer();
            this.protocol.getStateManager().setIdleState();
         } else {
            this.protocol.restartTimer(1000, this);
         }
      }
   }

   private class StateWaitingHighLevelResponse extends Rs232IntegratedLineScannerState {
      public StateWaitingHighLevelResponse(Rs232IntegratedLineScannerProtocol scannerProtocol) {
         super(scannerProtocol);
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println("-->stateWaitingHighLevelResponse<--");
         }

         this.protocol.restartTimer(5000, this);
      }

      public void timerExpired() {
         this.protocol.getStateManager().setCommunicationErrorState();
      }

      public void receiveHighLevelFrame(byte[] highLevelFrame) {
         if (Rs232IntegratedLineScannerPacketManager.getFrameNumber(highLevelFrame) != this.protocol.getScannerFrameNumber()) {
            if (this.protocol.getScannerFrameNumber() != -1 && !Rs232IntegratedLineScannerPacketManager.isRestartFrame(highLevelFrame)) {
               this.protocol.getStateManager().setCommunicationErrorState();
               return;
            }

            this.protocol.setScannerFrameNumber(Rs232IntegratedLineScannerPacketManager.getFrameNumber(highLevelFrame));
         }

         this.protocol.incrementScannerFrameNumber();
         this.protocol.stopTimer();
         this.protocol.getStateManager().setIdleState();
         this.protocol.submitToAdapter(Rs232IntegratedLineScannerPacketManager.ackFrame);
         this.protocol.fireDataEvent(highLevelFrame);
      }
   }
}
