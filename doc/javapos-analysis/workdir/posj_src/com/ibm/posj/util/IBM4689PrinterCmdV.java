package com.ibm.posj.util;

import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.IBM4689PrinterCmdVisitor;

public class IBM4689PrinterCmdV extends DefaultPOSPrinterCmdV implements IBM4689PrinterCmdVisitor {
   public void visitECLevelRequestCmd(IBM4689PrinterCmd.ECLevelRequestCmd eclrc) {
      this.visit(eclrc);
   }

   public void visitDownloadDBCSFontCmd(IBM4689PrinterCmd.DownloadDBCSFontCmd d2fc) {
      this.visit(d2fc);
   }

   public void visitDownloadFontCmd(IBM4689PrinterCmd.DownloadFontCmd dfc) {
      this.visit(dfc);
   }

   public void visitFeedCmd(IBM4689PrinterCmd.FeedCmd fc) {
      this.visit(fc);
   }

   public void visitFeedUnitsBackCmd(IBM4689PrinterCmd.FeedUnitsBackCmd fubc) {
      this.visit(fubc);
   }

   public void visitInitPrinterCmd(IBM4689PrinterCmd.InitPrinterCmd ipc) {
      this.visit(ipc);
   }

   public void visitLoadUDCBufferCmd(IBM4689PrinterCmd.LoadUDCBufferCmd ludcbc) {
      this.visit(ludcbc);
   }

   public void visitPartialCutCmd(IBM4689PrinterCmd.PartialCutCmd pc) {
      this.visit(pc);
   }

   public void visitPrintFullCutCmd(IBM4689PrinterCmd.PrintFullCutCmd pfc) {
      this.visit(pfc);
   }

   public void visitSelectHSizeCmd(IBM4689PrinterCmd.SelectHSizeCmd shsc) {
      this.visit(shsc);
   }

   public void visitSelectVSizeCmd(IBM4689PrinterCmd.SelectVSizeCmd svsc) {
      this.visit(svsc);
   }

   public void visitSelectHRIPositionCmd(IBM4689PrinterCmd.SelectHRIPositionCmd shripc) {
      this.visit(shripc);
   }

   public void visitSendCheckSumCmd(IBM4689PrinterCmd.SendCheckSumCmd sckc) {
      this.visit(sckc);
   }

   public void visitContinuation4689Cmd(IBM4689PrinterCmd.Continuation4689Cmd cc) {
      this.visit(cc);
   }
}
