package com.ibm.posj.printer.ibm4610;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.printer.IBMPrinterImp;
import com.ibm.posj.printer.PrinterWriter;
import com.ibm.posj.util.DefaultHandleCmdV;
import com.ibm.posj.util.IBM4610PrinterCmdV;

public class ChaseVisitors {
   public static final String LOOSE = "LineCount.LOOSE";

   public static final boolean isLooseCounter() {
      String l = PosSystemManager.getInstance().getProperties().getPropertyString("LineCount.LOOSE");
      return null != l;
   }

   public static class EcLevelChaseVisitor extends DefaultHandleCmdV {
      private ChaseVisitors.EcLevelChaseVisitor.ChaseVisitor2 doubleVisitor = new ChaseVisitors.EcLevelChaseVisitor.ChaseVisitor2();
      IBMPrinterImp imp = null;

      public EcLevelChaseVisitor(IBMPrinterImp impl) {
         this.imp = impl;
      }

      public void visitEraseFlashSectorCmd(POSPrinterCmd.EraseFlashSectorCmd efc) {
         IBM4610PrinterCmd.Factory factory = (IBM4610PrinterCmd.Factory)this.imp.getPrintCmdFactory();
         POSPrinterCmd pc = factory.createEnableLineCntCmd(false);
         efc.appendPOSPrinterCmd(pc);
      }

      public void visitPOSPrinterCmd(POSPrinterCmd cmd) {
         cmd.accept(this.doubleVisitor);
      }

      class ChaseVisitor2 extends IBM4610PrinterCmdV {
         public void visit(POSPrinterCmd cmd) {
            PrintCmd pc = ((DefaultPOSPrinterCmd)cmd).getPrintCmd();
            if (!pc.waitToFinish() && !pc.waitToStart() && !cmd.isSettingsCmd()) {
               IBM4610PrinterCmd.Factory factory = (IBM4610PrinterCmd.Factory)EcLevelChaseVisitor.this.imp.getPrintCmdFactory();
               cmd.appendPOSPrinterCmd(factory.createECLevelRequestCmd(true));
            }
         }

         public void visitChangePrintSideCmd(IBM4610PrinterCmd.ChangePrintSideCmd cmd) {
         }
      }
   }

   public static class LineCntChaseVisitor extends DefaultHandleCmdV {
      private ChaseVisitors.LineCntChaseVisitor.ChaseVisitor2 doubleVisitor;
      IBMPrinterImp imp = null;
      boolean loose = false;

      public LineCntChaseVisitor(IBMPrinterImp impt) {
         this.loose = ChaseVisitors.isLooseCounter();
         PrinterWriter.getTracer().println("Using loose line count? " + this.loose);
         this.doubleVisitor = new ChaseVisitors.LineCntChaseVisitor.ChaseVisitor2();
         this.imp = impt;
      }

      public void visitPOSPrinterCmd(POSPrinterCmd cmd) {
         cmd.accept(this.doubleVisitor);
      }

      class ChaseVisitor2 extends IBM4610PrinterCmdV {
         public void visitPageModeCmd(IBM4610PrinterCmd.PageModeCmd ppm) {
            IBM4610PrinterCmd.Factory factory = (IBM4610PrinterCmd.Factory)LineCntChaseVisitor.this.imp.getPrintCmdFactory();
            ppm.appendPOSPrinterCmd(factory.createECLevelRequestCmd(true));
         }

         public void visitPrintSetLogoCmd(POSPrinterCmd.PrintSetLogoCmd psl) {
            IBM4610PrinterCmd.Factory factory = (IBM4610PrinterCmd.Factory)LineCntChaseVisitor.this.imp.getPrintCmdFactory();
            psl.appendPOSPrinterCmd(factory.createECLevelRequestCmd(true));
         }

         public void visitSetLogoCmd(POSPrinterCmd.SetLogoCmd psl) {
            if (!LineCntChaseVisitor.this.loose) {
               IBM4610PrinterCmd.Factory factory = (IBM4610PrinterCmd.Factory)LineCntChaseVisitor.this.imp.getPrintCmdFactory();
               psl.appendPOSPrinterCmd(factory.createECLevelRequestCmd(true));
            }
         }

         public void visitCutPaperCmd(POSPrinterCmd.CutPaperCmd psl) {
         }

         public void visitPrintBarCodeCmd(POSPrinterCmd.PrintBarCodeCmd psl) {
            if (!LineCntChaseVisitor.this.loose) {
               IBM4610PrinterCmd.Factory factory = (IBM4610PrinterCmd.Factory)LineCntChaseVisitor.this.imp.getPrintCmdFactory();
               psl.appendPOSPrinterCmd(factory.createECLevelRequestCmd(true));
            }
         }

         public void visitChangePrintSideCmd(IBM4610PrinterCmd.ChangePrintSideCmd cmd) {
            if (!LineCntChaseVisitor.this.loose) {
               IBM4610PrinterCmd.Factory factory = (IBM4610PrinterCmd.Factory)LineCntChaseVisitor.this.imp.getPrintCmdFactory();
               cmd.appendPOSPrinterCmd(factory.createECLevelRequestCmd(true));
            }
         }
      }
   }

   public static class PmMultiVisitor extends IBM4610PrinterCmdV {
      private boolean flag = false;

      public void visitPrintNormalCmd(POSPrinterCmd.PrintNormalCmd pnc) {
         if (this.flag) {
            pnc.incompleteLine();
         }
      }

      public void clearOutput() {
         this.flag = false;
      }

      public void visitPageModeCmd(IBM4610PrinterCmd.PageModeCmd ppm) {
         this.flag = ppm.getBoolean();
      }

      public void visitPrintPageModePageCmd(IBM4610PrinterCmd.PrintPageModePageCmd cmd) {
         this.flag = false;
      }

      public void visitPageModeNormalCmd(IBM4610PrinterCmd.PMPageModeNormalCmd cmd) {
      }

      public void visit(POSPrinterCmd cmd) {
         if (this.flag) {
            cmd.setMemoryCmd();
         }
      }
   }
}
