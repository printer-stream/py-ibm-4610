package com.ibm.posj.bus.hid.javaxusbold;

import com.ibm.hid.DisconnectEvent;
import com.ibm.hid.HidExceptionEvent;
import com.ibm.hid.HidListener;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.Util;
import java.util.List;

class UsbHandleImpFactory$1 implements HidListener {
   UsbHandleImpFactory$1(UsbHandleImpFactory this$0, List val$arrayList, byte[] val$statusCmd) {
      this.this$0 = this$0;
      this.val$arrayList = val$arrayList;
      this.val$statusCmd = val$statusCmd;
   }

   public void hidExceptionEventOccurred(HidExceptionEvent heE) {
   }

   public void hidDeviceDisconnected(DisconnectEvent dE) {
   }

   public void reportEventOccurred(ReportEvent rE) {
      if (UsbHandleImpFactory.access$000(this.this$0).isOn() && rE.getData() != null) {
         UsbHandleImpFactory.access$000(this.this$0).println(Util.toFormatedHexString(rE.getData()));
      }

      this.val$arrayList.add(rE.getData());
      synchronized (this.val$statusCmd) {
         this.val$statusCmd.notifyAll();
      }
   }
}
