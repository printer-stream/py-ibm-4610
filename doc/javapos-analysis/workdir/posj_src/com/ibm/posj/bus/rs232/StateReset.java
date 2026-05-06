package com.ibm.posj.bus.rs232;

class StateReset extends Rs232FiscalPrinterState {
   public StateReset(Rs232FiscalPrinterProtocol fpp) {
      super(fpp);
      this.protocol.submitToAdapter(this.protocol.getFactory().createPacket(7, null, 0));
      this.protocol.setWaiting_response(true);
      this.protocol.restartTimer(250);
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateReset " + System.currentTimeMillis() + " <--");
      }
   }

   public void timerExpired() {
      if (this.tracer.isOn()) {
         this.tracer.println("-->stateReset - timerExpred" + System.currentTimeMillis() + " <--");
      }

      this.protocol.changeState(new StateInit(this.protocol));
   }
}
