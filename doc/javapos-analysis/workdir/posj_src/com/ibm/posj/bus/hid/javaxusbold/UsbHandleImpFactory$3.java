package com.ibm.posj.bus.hid.javaxusbold;

import com.ibm.hid.DisconnectEvent;
import com.ibm.hid.HidExceptionEvent;
import com.ibm.hid.HidListener;
import com.ibm.hid.ReportEvent;
import java.util.List;

class UsbHandleImpFactory$3 implements HidListener {
   UsbHandleImpFactory$3(UsbHandleImpFactory this$0, List val$list) {
      this.this$0 = this$0;
      this.val$list = val$list;
   }

   public void hidExceptionEventOccurred(HidExceptionEvent heE) {
   }

   public void hidDeviceDisconnected(DisconnectEvent dE) {
   }

   public void reportEventOccurred(ReportEvent rE) {
      this.val$list.add(rE);
   }
}
