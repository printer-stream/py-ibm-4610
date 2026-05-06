package com.ibm.posj.bus.printer.cmds.sureone;

import com.ibm.posj.HandleException;
import com.ibm.posj.IBMSureonePrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.GeneralCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;

public class GenSureoneCmdFactory implements GeneralCmdFactory {
   private PrintCmdFactory factory = null;
   private static byte[] EndPosition = new byte[]{0};

   public GenSureoneCmdFactory(PrintCmdFactory f) {
      this.factory = f;
   }

   private CmdSureone getEscBytes() {
      return (CmdSureone)this.factory.getCmdBytes();
   }

   public PrintCmd createStatisticCmd(POSPrinterCmd.StatisticCmd statisticCmd) {
      return null;
   }

   public PrintCmd createPrintNormalCmd(POSPrinterCmd.PrintNormalCmd pnc) {
      PrintCmd ret = this.factory.createCmd(pnc);
      byte[] dataArray = pnc.getData();
      if (dataArray != null) {
         ret.appendCmd(dataArray, pnc.getIndex(), pnc.getCount());
      }

      if (pnc.appendByte() != 0) {
         ret.appendCmd(pnc.appendByte());
      }

      if (pnc.isImmediate()) {
         ret.setBuffered(false);
      }

      return ret;
   }

   public PrintCmd createSetXonOffCmd(IBMSureonePrinterCmd.SetXonOffCmd sxofc) {
      PrintCmd ret = this.factory.createCmd(sxofc);
      ret.appendCmd(this.getEscBytes().SEL_XON_XOFF);
      return ret;
   }

   public PrintCmd createFeedLinesCmd(POSPrinterCmd.FeedLinesCmd flc) {
      PrintCmd ret = this.factory.createCmd(flc);
      ret.appendCmd(this.getEscBytes().FEED_UNITS);
      ret.appendCmd((byte)1);
      return ret;
   }

   public PrintCmd createFormFeedLengthCmd(POSPrinterCmd.FormFeedLengthCmd flc) {
      PrintCmd ret = this.factory.createCmd(flc);
      ret.appendCmd(this.getEscBytes().FORMFEED_LENGTH);
      return ret;
   }

   public PrintCmd createSetPageLengthLinesCmd(IBMSureonePrinterCmd.SetPageLengthLinesCmd splc) {
      PrintCmd ret = this.factory.createCmd(splc);
      ret.appendCmd(this.getEscBytes().SET_PAGE_LENGTH_LINES);
      ret.appendCmd(splc.getLines());
      return ret;
   }

   public PrintCmd createSetPageLengthInchesCmd(IBMSureonePrinterCmd.SetPageLengthInchesCmd spic) {
      PrintCmd ret = this.factory.createCmd(spic);
      ret.appendCmd(this.getEscBytes().SET_PAGE_LENGTH_INCHES);
      ret.appendCmd(spic.getInches());
      return ret;
   }

   public PrintCmd createVerticalTabCmd(IBMSureonePrinterCmd.VerticalTabCmd vtc) {
      PrintCmd ret = this.factory.createCmd(vtc);
      ret.appendCmd(this.getEscBytes().VERTICAL_TAB);
      return ret;
   }

   public PrintCmd createVerticalTabPosCmd(IBMSureonePrinterCmd.VerticalTabPosCmd vtpc) {
      PrintCmd ret = this.factory.createCmd(vtpc);
      ret.appendCmd(this.getEscBytes().SET_VERTICAL_TAB_POSITION);
      ret.appendCmd(vtpc.getPosition());
      ret.appendCmd(EndPosition);
      return ret;
   }

   public PrintCmd createSetBottomMarginCmd(IBMSureonePrinterCmd.SetBottomMarginCmd bmc) {
      PrintCmd ret = this.factory.createCmd(bmc);
      ret.appendCmd(this.getEscBytes().SET_BOTTOM_MARGIN);
      ret.appendCmd(bmc.getMargin());
      return ret;
   }

   public PrintCmd createHorizontalTabCmd(IBMSureonePrinterCmd.HorizontalTabCmd htc) {
      PrintCmd ret = this.factory.createCmd(htc);
      ret.appendCmd(this.getEscBytes().HORIZONTAL_TAB);
      return ret;
   }

   public PrintCmd createHorizontalTabPosCmd(IBMSureonePrinterCmd.HorizontalTabPosCmd htpc) {
      PrintCmd ret = this.factory.createCmd(htpc);
      ret.appendCmd(this.getEscBytes().TAB_SET);
      ret.appendCmd(htpc.getPosition());
      ret.appendCmd(EndPosition);
      return ret;
   }

   public PrintCmd createCashDrawerPulseCmd(IBMSureonePrinterCmd.CashDrawerPulseCmd cdpc) {
      PrintCmd ret = this.factory.createCmd(cdpc);
      ret.appendCmd(this.getEscBytes().ADJUST_PULSE_4_CD);
      ret.appendCmd(cdpc.getEnergizing());
      ret.appendCmd(cdpc.getDelay());
      return ret;
   }

   public PrintCmd createImmediateDriveCmd(IBMSureonePrinterCmd.ImmediateDriveCmd idc) {
      PrintCmd ret = this.factory.createCmd(idc);
      ret.appendCmd(this.getEscBytes().IGNORE_CMD_4_CD);
      return ret;
   }

   public PrintCmd createBeeperCmd(IBMSureonePrinterCmd.BeeperCmd bc) {
      PrintCmd ret = this.factory.createCmd(bc);
      ret.appendCmd(this.getEscBytes().BEEPER);
      return ret;
   }

   public PrintCmd createCancelPrintCmd(IBMSureonePrinterCmd.CancelPrintCmd cpc) {
      PrintCmd ret = this.factory.createCmd(cpc);
      ret.appendCmd(this.getEscBytes().CANCEL_PRNT_DATA);
      return ret;
   }

   public PrintCmd createReinitPrinterCmd(IBMSureonePrinterCmd.ReinitPrinterCmd ipc) {
      PrintCmd ret = this.factory.createCmd(ipc);
      ret.appendCmd(this.getEscBytes().RESET);
      return ret;
   }

   public PrintCmd createStatusRequestCmd(POSPrinterCmd.StatusRequestCmd src) {
      PrintCmd ret = this.factory.createCmd(src);
      ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      return ret;
   }

   public PrintCmd createInitPrinterCmd(IBMSureonePrinterCmd.InitPrinterCmd ipc) {
      PrintCmd ret = this.factory.createCmd(ipc);
      ret.appendCmd(this.getEscBytes().IGNORE_REINIT_PRNTR);
      return ret;
   }

   public PrintCmd createVerticalColAlignCmd(IBMSureonePrinterCmd.VerticalColAlignCmd vcac) {
      PrintCmd ret = this.factory.createCmd(vcac);
      ret.appendCmd(this.getEscBytes().VERTICAL_COL_ALIGN);
      return ret;
   }

   public PrintCmd createPrintDensityCmd(IBMSureonePrinterCmd.PrintDensityCmd pdc) {
      PrintCmd ret = this.factory.createCmd(pdc);
      ret.appendCmd(this.getEscBytes().PRNT_DENSITY);
      return ret;
   }

   public PrintCmd createCutPaperCmd(POSPrinterCmd.CutPaperCmd cpc) {
      PrintCmd ret = this.factory.createCmd(cpc);
      short per = cpc.getPercentage();
      if (per >= 51) {
         ret.appendCmd(this.getEscBytes().CUT_PAPER);
      } else if (per <= 50 && per > 0) {
         ret.appendCmd(this.getEscBytes().PARTIAL_CUT);
      } else {
         ret.appendCmd(this.getEscBytes().LINE_FEED_CHAR);
      }

      return ret;
   }

   public PrintCmd createTestRequestCmd(POSPrinterCmd.TestReqCmd tq) {
      throw new RuntimeException("Command not Supported");
   }

   public PrintCmd createDevInfoCmd(POSPrinterCmd.DevInfoCmd dic) {
      PrintCmd ret = this.factory.createCmd(dic);
      ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      return ret;
   }

   public PrintCmd createResetCmd(POSPrinterCmd.ResetCmd rsc) {
      throw new RuntimeException("Command not Supported");
   }

   public PrintCmd createMarkFeedCmd(POSPrinterCmd.MarkFeedCmd mfc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createSelectStationCmd(POSPrinterCmd.SelectStationCmd ssc) {
      return this.factory.createCmd(ssc);
   }

   public PrintCmd createMCTCmd(POSPrinterCmd.MCTCmd mc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createEraseFlashSectorCmd(POSPrinterCmd.EraseFlashSectorCmd efsc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createFeedReverseCmd(POSPrinterCmd.FeedReverseCmd feedReverseCmd) {
      PrintCmd ret = this.factory.createCmd(feedReverseCmd);
      ret.appendCmd(this.getEscBytes().TIME_BACKFEED);
      ret.appendCmd((byte)feedReverseCmd.getLines());
      return ret;
   }

   public PrintCmd createFeedUnitsCmd(POSPrinterCmd.FeedUnitsCmd feedUnitsCmd) throws HandleException {
      PrintCmd ret = this.factory.createCmd(feedUnitsCmd);
      ret.appendCmd(this.getEscBytes().TIME_MICRO_LINE_FEED);
      ret.appendCmd((byte)feedUnitsCmd.getUnits());
      return ret;
   }

   public PrintCmd createMCTValueCmd(POSPrinterCmd.MCTValueCmd mcVCmd) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createPassThruCmd(POSPrinterCmd.PassThruCmd ptCmd) throws HandleException {
      return null;
   }
}
