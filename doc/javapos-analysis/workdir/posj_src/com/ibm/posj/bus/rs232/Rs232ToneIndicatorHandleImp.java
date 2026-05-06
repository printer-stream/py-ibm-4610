package com.ibm.posj.bus.rs232;

import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.ToneIndicatorHandleImp;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.rs232.Rs232Port;

public abstract class Rs232ToneIndicatorHandleImp extends AbstractRs232HandleImp implements ToneIndicatorHandleImp {
   public Rs232ToneIndicatorHandleImp(HandleKey key, Rs232Port rs232Port) {
      super(key, rs232Port);
   }

   public DevCat getDevCat() {
      return DevCats.TONEINDICATOR_DEVCAT;
   }
}
