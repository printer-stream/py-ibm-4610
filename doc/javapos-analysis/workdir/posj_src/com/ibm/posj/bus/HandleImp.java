package com.ibm.posj.bus;

import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.flash.FlashException;
import com.ibm.posj.flash.FlashRequest;
import com.ibm.posj.util.DevBus;
import com.ibm.posj.util.DevCat;
import java.util.List;

public interface HandleImp {
   String getDeviceSerialNumber();

   HandleKey getHandleKey();

   void accept(HandleImpVisitor var1);

   DevCat getDevCat();

   DevBus getDevBus();

   void init() throws HandleException;

   void submit(HandleCmd var1) throws HandleException;

   void asyncSubmit(HandleCmd var1) throws HandleException;

   void flash(FlashRequest var1) throws FlashException;

   FlashHandleImp getFlashHandleImp() throws FlashException;

   boolean isFlashable();

   void lock() throws FlashException;

   void unlock();

   boolean isComposite();

   boolean isCompositeParent();

   boolean isSubDevice();

   List getHandleImpGroup();

   Handle getHandle();
}
