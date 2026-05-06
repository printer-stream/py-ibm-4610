package com.ibm.posj.bus.printer.cmds.ibm4610;

import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevBuses;

public class Rs232Cmd4610 extends Cmd4610 {
   public byte[] SOLICIT_STATUS = new byte[]{16, 5};

   public Rs232Cmd4610() {
      this.RESET = new byte[]{16, 5, 64};
      this.CLEAR = new byte[]{16, 5, 50};
      this.RESUME = new byte[]{16, 5, 49};
      this.TEST_REQ = new byte[]{111, 107, 10};
      this.CONTINUATION_CMD = new byte[0];
      this.DEVICE_INFO = new byte[]{29, 73, 1};
      this.BUFFERED_DEV_INFO = new byte[]{27, 0, 0, 1};
      this.STATUS_REQUEST = new byte[]{27, 118};
      this.BUFFERED_EC = new byte[]{27, 0, -128, 0};
      this.IMMEDIATE_EC = new byte[]{16, 5, 52};
      this.XON_XOFF_MODE = new byte[]{16, 5, 67};
   }

   public DevBus getBusType() {
      return DevBuses.RS232_DEVBUS;
   }
}
