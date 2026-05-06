package com.ibm.posj.bus.hid.javaxusb;

import com.ibm.hid.DisconnectEvent;
import com.ibm.hid.HidExceptionEvent;
import com.ibm.hid.HidListener;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.BooleanMonitor;
import com.ibm.jutil.Util;

class UsbHandleImpFactory$2 implements HidListener {
   UsbHandleImpFactory$2(UsbHandleImpFactory this$0, BooleanMonitor val$flag) {
      this.this$0 = this$0;
      this.val$flag = val$flag;
   }

   public void hidExceptionEventOccurred(HidExceptionEvent heE) {
   }

   public void hidDeviceDisconnected(DisconnectEvent dE) {
   }

   public void reportEventOccurred(ReportEvent rE) {
      if (UsbHandleImpFactory.access$000(this.this$0).isOn()) {
         UsbHandleImpFactory.access$000(this.this$0).println(3, "MOD 4 Status " + Util.toFormatedHexString(rE.getData()));
      }

      this.val$flag.set(true);
   }
}
