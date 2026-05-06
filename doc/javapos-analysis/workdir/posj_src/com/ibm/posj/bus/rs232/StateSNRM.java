package com.ibm.posj.bus.rs232;

class StateSNRM extends Rs232FiscalPrinterState {
   public StateSNRM(Rs232FiscalPrinterProtocol fpp) {
      super(fpp);
      this.protocol.setAttemptsCounter(0);
      this.run();
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateSNRM<--");
      }
   }

   public void timerExpired() {
      this.protocol.setAttemptsCounter(this.protocol.getAttemptsCounter() + 1);
      if (this.protocol.getAttemptsCounter() >= 10) {
         this.protocol.changeState(new StateReset(this.protocol));
         this.protocol.fireErrorEvent(20);
      } else {
         this.run();
      }
   }

   public void run() {
      this.protocol.submitToAdapter(this.protocol.getFactory().createPacket(4, null, 0));
      this.protocol.setWaiting_response(true);
      this.protocol.restartTimer(200);
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateSNRM - run <--");
      }
   }

   public void receivePacket(byte[] packet, int length) {
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateSNRM - receivePacket " + System.currentTimeMillis() + " <--");
      }

      if (Rs232FiscalPrinterPacketFactory.getTypeOfPacket(packet) == 6) {
         super.receivePacket(packet, length);
         this.protocol.changeState(new StateWaitIPLEnd(this.protocol));
      }
   }
}
