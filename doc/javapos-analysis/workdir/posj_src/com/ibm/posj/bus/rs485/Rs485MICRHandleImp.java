package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.posj.HandleKey;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.MICRHandleImp;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public abstract class Rs485MICRHandleImp extends AbstractRs485HandleImp implements MICRHandleImp {
   public Rs485MICRHandleImp(HandleKey key, SioDevice device) {
      super(key, device);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitMICR(this);
   }

   public DevCat getDevCat() {
      return DevCats.MICR_DEVCAT;
   }
}
