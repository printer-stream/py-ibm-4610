package com.ibm.posj;

import com.ibm.posj.bus.HandleImp;

public interface HandleFactory {
   Handle createCashDrawerHandle(HandleImp var1) throws HandleFactoryException;

   Handle createCheckScannerHandle(HandleImp var1) throws HandleFactoryException;

   Handle createFiscalPrinterHandle(HandleImp var1) throws HandleFactoryException;

   Handle createHardTotalsHandle(HandleImp var1) throws HandleFactoryException;

   Handle createKeylockHandle(HandleImp var1) throws HandleFactoryException;

   Handle createLineDisplayHandle(HandleImp var1) throws HandleFactoryException;

   Handle createMICRHandle(HandleImp var1) throws HandleFactoryException;

   Handle create4610MICRHandle(HandleImp var1) throws HandleFactoryException;

   Handle createMSRHandle(HandleImp var1) throws HandleFactoryException;

   Handle createMotionSensorHandle(HandleImp var1) throws HandleFactoryException;

   Handle createPOSKeyboardHandle(HandleImp var1) throws HandleFactoryException;

   Handle createPOSPrinterHandle(HandleImp var1) throws HandleFactoryException;

   Handle createScaleHandle(HandleImp var1) throws HandleFactoryException;

   Handle createScannerHandle(HandleImp var1) throws HandleFactoryException;

   Handle createToneIndicatorHandle(HandleImp var1) throws HandleFactoryException;
}
