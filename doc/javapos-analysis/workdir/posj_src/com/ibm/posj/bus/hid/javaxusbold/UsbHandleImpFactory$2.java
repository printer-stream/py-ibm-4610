package com.ibm.posj.bus.hid.javaxusbold;

import com.ibm.hid.DisconnectEvent;
import com.ibm.hid.HidExceptionEvent;
import com.ibm.hid.HidListener;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.Util;
import java.util.List;

class UsbHandleImpFactory$2 implements HidListener {
   UsbHandleImpFactory$2(UsbHandleImpFactory this$0, List val$list) {
      this.this$0 = this$0;
      this.val$list = val$list;
   }

   public void hidExceptionEventOccurred(HidExceptionEvent heE) {
      UsbHandleImpFactory.access$000(this.this$0).println("hidExceptionEventOccurred while disabling Scanner" + heE);
   }

   public void hidDeviceDisconnected(DisconnectEvent dE) {
      UsbHandleImpFactory.access$000(this.this$0).println("hidDeviceDisconnected while disabling Scanner");
      UsbHandleImpFactory.access$000(this.this$0).println(dE.getHidException());
   }

   public void reportEventOccurred(ReportEvent rE) {
      this.val$list.add(rE);
      UsbHandleImpFactory.access$000(this.this$0).println("reportEventOccurred while disabling Scanner -- > " + Util.toFormatedHexString(rE.getData()));
   }
}
