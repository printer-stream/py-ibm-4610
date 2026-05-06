package com.ibm.posj.bus.rs232.javaxcomm;

import com.ibm.rs232.Rs232Config;
import com.ibm.rs232.Rs232Exception;
import com.ibm.rs232.Rs232Port;

class Rs232PortCommFactory {
   public Rs232PortCommFactory() {
   }

   public Rs232Port createRs232Port(Rs232Config config) throws Rs232Exception {
      return new Rs232PortCommAdapter(config);
   }
}
