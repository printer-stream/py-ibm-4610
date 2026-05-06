package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.posj.HandleException;
import com.ibm.posj.bus.HandleImp;

public interface HidHandleImp extends HandleImp {
   void accept(HidHandleImpVisitor var1);

   HidDevice getHidDevice();

   void reattachHidDevice(HidDevice var1) throws HandleException;

   void setSerialNumber(String var1);

   void setVendorID(short var1);

   void setProductID(short var1);

   void setBCDLevel(short var1);

   String getSerialNumber();

   short getVendorID();

   short getProductID();

   short getBCDLevel();
}
