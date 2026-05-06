package com.ibm.posj.bus.hid;

import com.ibm.posj.bus.HandleImp;
import java.util.List;

public class HidFlashHandleImpFilterV implements HidHandleImpVisitor {
   public void visitHid4610PrinterHandleImp(Hid4610PrinterHandleImp handleImp) {
   }

   public void visitHid4689PrinterHandleImp(Hid4689PrinterHandleImp handleImp) {
   }

   public void visitHidCashDrawerHandleImp(HidHandleImp handleImp) {
   }

   public void visitHidCheckScannerHandleImp(HidHandleImp handleImp) {
   }

   public void visitHidFlashHandleImp(HidFlashHandleImp handleImp) {
   }

   public void visitHidHardTotalsHandleImp(HidHardTotalsHandleImp handleImp) {
   }

   public void visitHidKeylockHandleImp(HidKeylockHandleImp handleImp) {
   }

   public void visitHidLineDisplayHandleImp(HidLineDisplayHandleImp handleImp) {
   }

   public void visitHidMICRHandleImp(HidMICRHandleImp handleImp) {
   }

   public void visitHidMSRHandleImp(HidMSRHandleImp handleImp) {
   }

   public void visitHidPOSKeyboardHandleImp(HidPOSKeyboardHandleImp handleImp) {
      this.setAsNonFlashingDevice(handleImp);
   }

   public void visitHidPOSPrinterHandleImp(HidPOSPrinterHandleImp handleImp) {
   }

   public void visitHidPOSPrinterToneIndicatorHandleImp(HidHandleImp handleImp) {
   }

   public void visitHidScaleHandleImp(HidScaleHandleImp handleImp) {
   }

   public void visitHidScannerHandleImp(HidScannerHandleImp handleImp) {
   }

   public void visitHidToneIndicatorHandleImp(HidToneIndicatorHandleImp handleImp) {
   }

   public void visitHidFiscalPrinterImp(HidFiscalPrinterHandleImp handleImp) {
   }

   private void setAsNonFlashingDevice(HandleImp handleImp) {
      List handleImpGroup = handleImp.getHandleImpGroup();

      for (HandleImp currentHandleImp : handleImpGroup) {
         if (currentHandleImp instanceof HidFlashHandleImp) {
            handleImpGroup.remove(currentHandleImp);
            break;
         }
      }
   }
}
