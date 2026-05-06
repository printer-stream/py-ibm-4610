package com.ibm.posj;

import com.ibm.posj.bus.HandleImp;

public class DefaultHandleFactory implements HandleFactory {
   public Handle createCashDrawerHandle(HandleImp handleImp) throws HandleFactoryException {
      CashDrawerHandle handle = new DefaultCashDrawerHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createCheckScannerHandle(HandleImp handleImp) throws HandleFactoryException {
      CheckScannerHandle handle = new DefaultCheckScannerHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createFiscalPrinterHandle(HandleImp handleImp) throws HandleFactoryException {
      FiscalPrinterHandle handle = new DefaultFiscalPrinterHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createHardTotalsHandle(HandleImp handleImp) throws HandleFactoryException {
      HardTotalsHandle handle = new DefaultHardTotalsHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createKeylockHandle(HandleImp handleImp) throws HandleFactoryException {
      KeylockHandle handle = new DefaultKeylockHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createLineDisplayHandle(HandleImp handleImp) throws HandleFactoryException {
      LineDisplayHandle handle = new DefaultLineDisplayHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createMICRHandle(HandleImp handleImp) throws HandleFactoryException {
      MICRHandle handle = new DefaultMICRHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle create4610MICRHandle(HandleImp handleImp) throws HandleFactoryException {
      MICRHandle handle = new DefaultMICRHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createMSRHandle(HandleImp handleImp) throws HandleFactoryException {
      MSRHandle handle = new DefaultMSRHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createMotionSensorHandle(HandleImp handleImp) throws HandleFactoryException {
      MotionSensorHandle handle = new DefaultMotionSensorHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createPOSKeyboardHandle(HandleImp handleImp) throws HandleFactoryException {
      POSKeyboardHandle handle = new DefaultPOSKeyboardHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createPOSPrinterHandle(HandleImp handleImp) throws HandleFactoryException {
      POSPrinterHandle handle = new DefaultPOSPrinterHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createScaleHandle(HandleImp handleImp) throws HandleFactoryException {
      ScaleHandle handle = new DefaultScaleHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createScannerHandle(HandleImp handleImp) throws HandleFactoryException {
      ScannerHandle handle = new DefaultScannerHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }

   public Handle createToneIndicatorHandle(HandleImp handleImp) throws HandleFactoryException {
      ToneIndicatorHandle handle = new DefaultToneIndicatorHandle();

      try {
         handle.setHandleImp(handleImp);
         return handle;
      } catch (HandleException var4) {
         throw new HandleFactoryException("Could not create handle", var4);
      }
   }
}
