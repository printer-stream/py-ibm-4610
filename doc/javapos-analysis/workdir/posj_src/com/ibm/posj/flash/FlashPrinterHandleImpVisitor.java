package com.ibm.posj.flash;

import com.ibm.posj.bus.FlashHandleImp;
import com.ibm.posj.bus.IBM4610PrinterHandleImp;
import com.ibm.posj.bus.POSPrinterHandleImp;
import com.ibm.posj.bus.hid.Hid4689PrinterHandleImp;
import java.util.List;

public class FlashPrinterHandleImpVisitor {
   private List flashFileList;

   public void setFlashFileList(List flashFileList) {
      this.flashFileList = flashFileList;
   }

   public void visit4610PosPrinter(POSPrinterHandleImp handleImp) {
      if (this.flashFileList != null) {
         try {
            IBM4610PrinterHandleImp hid4610PrinterHandleImp = (IBM4610PrinterHandleImp)handleImp;
            hid4610PrinterHandleImp.init();
            byte printerFeatureByte1 = hid4610PrinterHandleImp.getFeatureByte1();
            byte printerFeatureByte2 = hid4610PrinterHandleImp.getFeatureByte2();
            int printerID = hid4610PrinterHandleImp.getPrinterHandleState().getPrinterID();
            int printerEC = hid4610PrinterHandleImp.getPrinterHandleState().getPrinterEC();
            int printerType = hid4610PrinterHandleImp.getPrinterHandleState().getPrinterType();
            FlashHandleImp flashHandleImp = handleImp.getFlashHandleImp();
            flashHandleImp.setPrtFlashFile(IBM4610FlashUtil.findFlashFile(printerType, printerID, printerFeatureByte1, printerFeatureByte2, this.flashFileList));
         } catch (Exception var9) {
         }
      }
   }

   public void visit4689PosPrinter(POSPrinterHandleImp handleImp) {
      if (this.flashFileList != null) {
         try {
            Hid4689PrinterHandleImp hid4689PrinterHandleImp = (Hid4689PrinterHandleImp)handleImp;
            hid4689PrinterHandleImp.init();
            byte printerFeatureByte1 = hid4689PrinterHandleImp.getFeatureByte1();
            byte printerFeatureByte2 = hid4689PrinterHandleImp.getFeatureByte2();
            int printerID = hid4689PrinterHandleImp.getPrinterHandleState().getPrinterID();
            int printerEC = hid4689PrinterHandleImp.getPrinterHandleState().getPrinterEC();
            int printerType = hid4689PrinterHandleImp.getPrinterHandleState().getPrinterType();
            FlashHandleImp flashHandleImp = handleImp.getFlashHandleImp();
            flashHandleImp.setPrtFlashFile(IBM4689FlashUtil.findFlashFile(printerType, printerID, printerFeatureByte1, printerFeatureByte2, this.flashFileList));
         } catch (Exception var9) {
         }
      }
   }
}
