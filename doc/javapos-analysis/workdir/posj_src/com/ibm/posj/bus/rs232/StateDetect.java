package com.ibm.posj.bus.rs232;

class StateDetect extends Rs232FiscalPrinterState {
   public StateDetect(Rs232FiscalPrinterProtocol fpp) {
      super(fpp);
      this.protocol.setAttemptsCounter(0);
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateDetect " + System.currentTimeMillis() + " <--");
      }

      this.run();
   }

   public void run() {
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateDetect - run" + System.currentTimeMillis() + " <--");
      }

      this.protocol.submitToAdapter(this.protocol.getFactory().createPacket(this.protocol.getCurrIFPacket(), null, 0));
      this.protocol.setWaiting_response(true);
      this.protocol.restartTimer(300);
      this.started = System.currentTimeMillis();
   }

   public void timerExpired() {
      if (this.tracer.isOn()) {
         this.tracer.println("-->tmrExpired " + System.currentTimeMillis() + " <--");
      }

      this.protocol.setAttemptsCounter(this.protocol.getAttemptsCounter() + 1);
      if (this.protocol.getAttemptsCounter() > 10) {
         this.protocol.fireErrorEvent(17);
      } else {
         this.run();
      }
   }

   public void receivePacket(byte[] packet, int length) {
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateDetect - rcvPkt " + System.currentTimeMillis() + " <--");
      }

      if (Rs232FiscalPrinterPacketFactory.getTypeOfPacket(packet) == this.protocol.getCurrIFPacket()
         || Rs232FiscalPrinterPacketFactory.getTypeOfPacket(packet) == 5) {
         this.protocol.changeCurrIFPacket();
         this.protocol.setAttemptsCounter(0);
         this.protocol.fireDataEvent(packet, length);
      }

      this.protocol.restartTimer(300);
   }
}
