package com.ibm.posj.printer.ibm4610;

import com.ibm.posj.GeneralPOSPrinterCmd;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.IBM4610PrinterCmdVisitor;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.AbstractCmdCompleteVisitor;
import com.ibm.posj.printer.IBMPrinterState;

public class CmdCompleteVisitor extends AbstractCmdCompleteVisitor implements IBM4610PrinterCmdVisitor {
   private IBMPrinterState state;

   public CmdCompleteVisitor(IBMPrinterState state) {
      this.state = state;
   }

   public void visitSelectStationCmd(POSPrinterCmd.SelectStationCmd ssc) {
      this.state.setCurrentStation(ssc.getStation());
   }

   public void visitDotSpacingCmd(POSPrinterCmd.DotSpacingCmd dsc) {
      int dots = dsc.getDotSpacing();
      this.state.setSpacingWidth(dsc.getStation(), dots);
      this.setStateChanged(true);
   }

   public void visitFontTypeCmd(POSPrinterCmd.FontTypeCmd ftc) {
      if (ftc.getStation() == 2) {
         this.setRecCharMatrix(ftc);
      } else {
         this.setSlpCharMatrix(ftc);
      }

      this.setStateChanged(true);
   }

   public void visitSetBitmapCmd(POSPrinterCmd.SetBitmapCmd setBmpCmd) {
      this.state.setSetBitmapCmd(setBmpCmd);
      this.setStateChanged(true);
   }

   public void visitSetLogoCmd(POSPrinterCmd.SetLogoCmd setLogoCmd) {
      this.state.setSetLogoCmd(setLogoCmd);
      this.setStateChanged(true);
   }

   public void visitAlignPositionCmd(POSPrinterCmd.AlignPositionCmd algPosCmd) {
      this.state.setAlignment(algPosCmd.getStation(), algPosCmd.getOption());
      this.setStateChanged(true);
   }

   public void visitContinuationCmd(IBM4610PrinterCmd.ContinuationCmd ccmd) {
      if (ccmd.getMasterCmd() != null) {
         if (ccmd.getMasterCmd().isCompleted()) {
            ccmd.getMasterCmd().accept(this);
         }
      }
   }

   protected void setSlpCharMatrix(POSPrinterCmd.FontTypeCmd ftc) {
      switch (ftc.getFontType()) {
         case 0:
         case 1:
            this.state.setCharWidth(ftc.getStation(), (byte)7);
            this.state.setCharHeight(ftc.getStation(), (byte)9);
      }
   }

   protected void setRecCharMatrix(POSPrinterCmd.FontTypeCmd ftc) {
      switch (ftc.getFontType()) {
         case 0:
            this.state.setCharWidth(ftc.getStation(), (byte)10);
            this.state.setCharHeight(ftc.getStation(), (byte)20);
            break;
         case 1:
            this.state.setCharWidth(ftc.getStation(), (byte)12);
            this.state.setCharWidth(ftc.getStation(), (byte)24);
            break;
         case 2:
            this.state.setCharWidth(ftc.getStation(), (byte)8);
            this.state.setCharWidth(ftc.getStation(), (byte)16);
      }
   }

   public void visitECLevelRequestCmd(IBM4610PrinterCmd.ECLevelRequestCmd elrc) {
   }

   public void visitReleasePrintBufferCmd(IBM4610PrinterCmd.ReleasePrintBufferCmd rpcd) {
   }

   public void visitDevInfoCmd(POSPrinterCmd.DevInfoCmd dic) {
      dic.setDeviceId(this.state.getPrinterID());
      ((GeneralPOSPrinterCmd.DevInfoCmd)dic).setDeviceType(this.state.getPrinterType());
      dic.setSerialNumber(this.state.getSerialNumber());
      dic.setFirmwareLevel(this.state.getPrinterEC());
   }

   public void visitPageModeCmd(IBM4610PrinterCmd.PageModeCmd pmc) {
   }

   public void visitStartScanCmd(IBM4610PrinterCmd.StartScanCmd ssc) {
   }

   public void visitChangePrintSideCmd(IBM4610PrinterCmd.ChangePrintSideCmd cmd) {
   }

   public void visitStoreScannedImageCmd(IBM4610PrinterCmd.StoreScannedImgCmd ssc) {
   }

   public void visitPageModeNormalCmd(IBM4610PrinterCmd.PMPageModeNormalCmd cmd) {
   }

   public void visitPrintPageModePageCmd(IBM4610PrinterCmd.PrintPageModePageCmd cmd) {
   }
}
