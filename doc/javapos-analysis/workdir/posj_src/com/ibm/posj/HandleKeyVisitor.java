package com.ibm.posj;

import com.ibm.posj.bus.embedded.EmbeddedHandleKey;
import com.ibm.posj.bus.hid.HidHandleKey;
import com.ibm.posj.bus.poskbd.PosKbdHandleKey;
import com.ibm.posj.bus.rs232.Rs232HandleKey;
import com.ibm.posj.bus.rs485.Rs485HandleKey;
import com.ibm.posj.bus.usb.UsbHandleKey;

public interface HandleKeyVisitor {
   void visitHidHandleKey(HidHandleKey var1);

   void visitEmbeddedHandleKey(EmbeddedHandleKey var1);

   void visitPosKbdHandleKey(PosKbdHandleKey var1);

   void visitRs232HandleKey(Rs232HandleKey var1);

   void visitRs485HandleKey(Rs485HandleKey var1);

   void visitUsbHandleKey(UsbHandleKey var1);

   void visitUnknownHandleKey(HandleKey var1);
}
