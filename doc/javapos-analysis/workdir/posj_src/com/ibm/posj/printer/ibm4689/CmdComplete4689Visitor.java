package com.ibm.posj.printer.ibm4689;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.GeneralPOSPrinterCmd;
import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.IBM4689PrinterCmdVisitor;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.printer.AbstractCmdCompleteVisitor;
import com.ibm.posj.printer.IBMPrinterState;

public class CmdComplete4689Visitor extends AbstractCmdCompleteVisitor implements IBM4689PrinterCmdVisitor {
   private IBMPrinterState state;

   public CmdComplete4689Visitor(IBMPrinterState state) {
      this.state = state;
   }

   public void visitDevInfoCmd(POSPrinterCmd.DevInfoCmd dic) {
      dic.setDeviceId(this.state.getPrinterID());
      ((GeneralPOSPrinterCmd.DevInfoCmd)dic).setDeviceType(this.state.getPrinterType());
      dic.setSerialNumber(this.state.getSerialNumber());
      dic.setFirmwareLevel(this.state.getPrinterEC());
   }

   public void visitSelectStationCmd(POSPrinterCmd.SelectStationCmd ssc) {
      this.state.setCurrentStation(ssc.getStation());
   }

   public void visitSetLogoCmd(DefaultPOSPrinterCmd.SetLogoCmd setLogoCmd) {
      this.state.setSetLogoCmd(setLogoCmd);
      this.setStateChanged(true);
   }

   public void visitPrintSetLogoCmd(DefaultPOSPrinterCmd.PrintSetLogoCmd printSetLogoCmd) {
   }

   public void visitLoadUDCBufferCmd(IBM4689PrinterCmd.LoadUDCBufferCmd ludcbc) {
   }

   public void visitInitPrinterCmd(IBM4689PrinterCmd.InitPrinterCmd ipc) {
   }

   public void visitFeedCmd(IBM4689PrinterCmd.FeedCmd fc) {
   }

   public void visitPartialCutCmd(IBM4689PrinterCmd.PartialCutCmd pc) {
   }

   public void visitPrintFullCutCmd(IBM4689PrinterCmd.PrintFullCutCmd pfc) {
   }

   public void visitFeedUnitsBackCmd(IBM4689PrinterCmd.FeedUnitsBackCmd fubc) {
   }

   public void visitDownloadFontCmd(IBM4689PrinterCmd.DownloadFontCmd dfc) {
   }

   public void visitDownloadDBCSFontCmd(IBM4689PrinterCmd.DownloadDBCSFontCmd d2fc) {
   }

   public void visitECLevelRequestCmd(IBM4689PrinterCmd.ECLevelRequestCmd eclrc) {
   }

   public void visitSelectHSizeCmd(IBM4689PrinterCmd.SelectHSizeCmd shsc) {
   }

   public void visitSelectVSizeCmd(IBM4689PrinterCmd.SelectVSizeCmd svsc) {
   }

   public void visitSelectHRIPositionCmd(IBM4689PrinterCmd.SelectHRIPositionCmd shripc) {
   }

   public void visitSendCheckSumCmd(IBM4689PrinterCmd.SendCheckSumCmd sckc) {
   }

   public void visitContinuation4689Cmd(IBM4689PrinterCmd.Continuation4689Cmd cc) {
   }
}
