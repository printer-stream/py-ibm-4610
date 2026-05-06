package com.ibm.posj.bus.rs232;

class StateInit extends Rs232FiscalPrinterState {
   public StateInit(Rs232FiscalPrinterProtocol fpp) {
      super(fpp);
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateInit <--");
      }

      this.run();
   }

   public void timerExpired() {
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateInit - timerExpired <--");
      }

      this.run();
   }

   public void run() {
      this.protocol.setCurrIFPacket(0);
      this.protocol.submitToAdapter(this.protocol.getFactory().createPacket(this.protocol.getCurrIFPacket(), new byte[1], 0));
      this.protocol.setWaiting_response(true);
      this.protocol.restartTimer(200);
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateInit - run <--");
      }
   }

   public void receivePacket(byte[] packet, int length) {
      if (Rs232FiscalPrinterPacketFactory.getTypeOfPacket(packet) == 5) {
         this.protocol.changeState(new StateSNRM(this.protocol));
      } else {
         this.protocol.changeState(new StateReset(this.protocol));
      }

      if (this.tracer.isOn()) {
         this.tracer.println("-->stateInit - receivePacket <--");
      }
   }
}
