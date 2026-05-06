package com.ibm.posj.bus.rs232;

class StateSendGetVersionCmd extends Rs232FiscalPrinterState {
   public StateSendGetVersionCmd(Rs232FiscalPrinterProtocol fpp) {
      super(fpp);
      this.protocol.setAttemptsCounter(0);
      this.run();
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateSendGetVersionCmd " + System.currentTimeMillis() + " <--");
      }
   }

   public void run() {
      this.protocol
         .submitToAdapter(
            this.protocol
               .getFactory()
               .createPacket(
                  this.protocol.getCurrIFPacket(), Rs232FiscalPrinterProtocol.CMD_FISCALGETVERSION, Rs232FiscalPrinterProtocol.CMD_FISCALGETVERSION.length
               )
         );
      this.protocol.setWaiting_response(true);
      this.protocol.restartTimer(200);
      this.started = System.currentTimeMillis();
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateSGVC - run " + System.currentTimeMillis() + " <--");
      }
   }

   public void receivePacket(byte[] packet, int length) {
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateSGVC receivePacket " + System.currentTimeMillis() + " <--");
      }

      if (Rs232FiscalPrinterPacketFactory.getTypeOfPacket(packet) == this.protocol.getCurrIFPacket()) {
         this.protocol.changeCurrIFPacket();
         this.protocol.changeState(new StateWaitIPLEnd(this.protocol));
      }
   }

   public void timerExpired() {
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateSGVC timerexpired " + System.currentTimeMillis() + " <--");
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
