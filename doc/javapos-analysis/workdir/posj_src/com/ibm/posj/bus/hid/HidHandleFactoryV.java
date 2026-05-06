package com.ibm.posj.bus.hid;

import com.ibm.posj.Handle;
import com.ibm.posj.HandleFactory;
import com.ibm.posj.HandleFactoryException;
import com.ibm.posj.bus.DefaultHandleImpV;
import com.ibm.posj.bus.HandleImp;

public class HidHandleFactoryV extends DefaultHandleImpV {
   private HandleFactory handleFactory = null;
   private Handle handle = null;
   private Exception exception = null;

   public HidHandleFactoryV(HandleFactory factory) {
      this.handleFactory = factory;
   }

   public void clear() {
      this.handle = null;
      this.exception = null;
   }

   public Exception getException() {
      return this.exception;
   }

   public Handle getHandle() {
      return this.handle;
   }

   public void visitCashDrawer(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createCashDrawerHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visitCheckScanner(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createCheckScannerHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visitHardTotals(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createHardTotalsHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visitKeylock(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createKeylockHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visitLineDisplay(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createLineDisplayHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visitMICR(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createMICRHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visit4610MICR(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.create4610MICRHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visitMSR(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createMSRHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visitPOSKeyboard(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createPOSKeyboardHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visitPOSPrinter(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createPOSPrinterHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visitScanner(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createScannerHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visitScale(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createScaleHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visitToneIndicator(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createToneIndicatorHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }

   public void visitFiscalPrinter(HandleImp handleImp) {
      try {
         this.handle = this.handleFactory.createFiscalPrinterHandle(handleImp);
         ((AbstractHidHandleImp)handleImp).setHandle(this.handle);
      } catch (HandleFactoryException var3) {
         this.exception = var3;
      }
   }
}
