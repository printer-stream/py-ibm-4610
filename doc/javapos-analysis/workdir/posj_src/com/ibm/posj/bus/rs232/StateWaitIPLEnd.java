package com.ibm.posj.bus.rs232;

class StateWaitIPLEnd extends Rs232FiscalPrinterState {
   public StateWaitIPLEnd(Rs232FiscalPrinterProtocol fpp) {
      super(fpp);
      this.protocol.setAttemptsCounter(0);
      this.run();
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateWIPLEnd " + System.currentTimeMillis() + " <--");
      }
   }

   public void run() {
      this.protocol.submitToAdapter(this.protocol.getFactory().createPacket(this.protocol.getCurrIFPacket(), null, 0));
      this.protocol.setWaiting_response(true);
      this.protocol.restartTimer(200);
      this.started = System.currentTimeMillis();
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateWIPLEnd run" + System.currentTimeMillis() + " <--");
      }
   }

   public void receivePacket(byte[] packet, int length) {
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateWIPLEnd receivePkt " + System.currentTimeMillis() + " <--");
      }

      if (Rs232FiscalPrinterPacketFactory.getTypeOfPacket(packet) == this.protocol.getCurrIFPacket()) {
         this.protocol.changeCurrIFPacket();
         this.protocol.setAttemptsCounter(0);
         if (Rs232FiscalPrinterPacketFactory.getDataLength(packet) >= 15 && (packet[11] & 4) == 0) {
            this.protocol.changeState(new StateWork(this.protocol));
            this.protocol.fireIPLEndEvent(packet, length);
            return;
         }
      }

      this.run();
   }

   public void timerExpired() {
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateWIPLEnd tmrExpired " + System.currentTimeMillis() + " <--");
      }

      this.protocol.setAttemptsCounter(this.protocol.getAttemptsCounter() + 1);
      if (this.protocol.getAttemptsCounter() >= 10) {
         this.protocol.changeState(new StateReset(this.protocol));
      } else if (this.started == System.currentTimeMillis()) {
         this.protocol.restartTimer(200);
      } else {
         this.run();
      }
   }
}
