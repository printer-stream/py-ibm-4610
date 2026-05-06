package com.ibm.posj.bus.rs232;

class StateWork extends Rs232FiscalPrinterState {
   private byte[] txBuffer = null;
   private boolean isIDLE = true;
   private boolean isWriting = false;
   private boolean isWaitingResponse = false;

   public StateWork(Rs232FiscalPrinterProtocol fpp) {
      super(fpp);
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateWork " + System.currentTimeMillis() + " <--");
      }

      this.protocol.setAttemptsCounter(0);
      this.run();
   }

   public int write(byte[] buffer, int length) {
      if (!this.isIDLE) {
         return 2;
      } else {
         this.isWriting = true;
         this.isIDLE = false;
         if (this.tracer.isOn()) {
            this.tracer.println("-->stateWork - write " + System.currentTimeMillis() + " <--");
         }

         this.protocol.stopTimer();
         this.txBuffer = new byte[length];
         System.arraycopy(buffer, 0, this.txBuffer, 0, length);
         this.protocol.submitToAdapter(this.protocol.getFactory().createPacket(this.protocol.getCurrIFPacket(), buffer, length));
         this.protocol.setWaiting_response(true);
         this.protocol.setAttemptsCounter(0);
         this.waitingForCompletion = true;
         this.isWriting = false;
         this.protocol.restartTimer(200);
         return 1;
      }
   }

   public void run() {
      if (!this.isWriting) {
         this.isIDLE = false;
         if (this.tracer.isOn()) {
            this.tracer.println("-->stateWork - run" + System.currentTimeMillis() + " <--");
         }

         if (this.txBuffer != null) {
            this.protocol.submitToAdapter(this.protocol.getFactory().createPacket(this.protocol.getCurrIFPacket(), this.txBuffer, this.txBuffer.length));
         } else {
            this.protocol.submitToAdapter(this.protocol.getFactory().createPacket(this.protocol.getCurrIFPacket(), null, 0));
         }

         this.isWaitingResponse = this.protocol.isWaiting_response();
         this.protocol.setWaiting_response(true);
         if (this.isWaitingResponse) {
            this.protocol.restartTimer(200);
         } else if (this.waitingForCompletion) {
            this.protocol.restartTimer(45);
         } else {
            this.protocol.restartTimer(200);
         }

         this.started = System.currentTimeMillis();
      }
   }

   public void timerExpired() {
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateWork - tmrExpired" + System.currentTimeMillis() + " <--");
      }

      this.protocol.setAttemptsCounter(this.protocol.getAttemptsCounter() + 1);
      if (this.protocol.getAttemptsCounter() >= 10) {
         this.protocol.changeState(new StateReset(this.protocol));
         this.protocol.fireErrorEvent(16);
      } else if (this.started == System.currentTimeMillis()) {
         if (this.waitingForCompletion) {
            this.protocol.restartTimer(45);
         } else {
            this.protocol.restartTimer(200);
         }
      } else {
         this.run();
      }
   }

   public void receivePacket(byte[] packet, int length) {
      int timeout = 100;
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateWork - rcvPkt" + System.currentTimeMillis() + " <--");
      }

      if (this.waitingForCompletion) {
         timeout = 45;
      }

      if (Rs232FiscalPrinterPacketFactory.getTypeOfPacket(packet) == this.protocol.getCurrIFPacket()) {
         this.protocol.changeCurrIFPacket();
         this.protocol.setAttemptsCounter(0);
         this.txBuffer = null;
         if (Rs232FiscalPrinterPacketFactory.getDataLength(packet) >= 15) {
            this.protocol.fireDataEvent(packet, length);
            timeout = 45;
            if ((packet[11] & 96) == 0) {
               this.waitingForCompletion = false;
            }
         }

         this.isIDLE = true;
         this.protocol.restartTimer(timeout);
      } else {
         if (Rs232FiscalPrinterPacketFactory.getTypeOfPacket(packet) == 5) {
            this.protocol.changeState(new StateSNRM(this.protocol));
            this.protocol.fireErrorEvent(16);
            return;
         }

         this.protocol.restartTimer(timeout);
      }
   }
}
