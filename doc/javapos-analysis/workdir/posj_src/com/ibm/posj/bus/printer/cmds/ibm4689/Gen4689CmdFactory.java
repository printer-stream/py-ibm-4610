package com.ibm.posj.bus.printer.cmds.ibm4689;

import com.ibm.posj.HandleException;
import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.GeneralCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;
import java.util.HashMap;
import java.util.Map;

public class Gen4689CmdFactory implements GeneralCmdFactory {
   private PrintCmdFactory factory = null;
   private byte PTR_JOURNAL_STATION = 1;
   private byte PTR_RECEIPT_STATION = 0;
   private int increasedChars = 0;
   private Map statisticsMapping;
   int charsPerLine = 32;
   boolean addCharsRotate = false;

   public Gen4689CmdFactory(PrintCmdFactory f) {
      this.factory = f;
   }

   private Cmd4689 getEscBytes() {
      return (Cmd4689)this.factory.getCmdBytes();
   }

   public PrintCmd createPrintNormalCmd(POSPrinterCmd.PrintNormalCmd pnc) {
      PrintCmd ret = this.factory.createCmd(pnc);
      byte[] dataArray = pnc.getData();
      Print4689CmdFactory factory8 = (Print4689CmdFactory)this.factory;
      factory8.setCmdsStation(ret, pnc.getStation());
      ret.appendCmd((byte)0);
      byte id = factory8.generateID();
      ret.appendCmd(id);
      pnc.setID(id);
      ret.appendCmd(dataArray);
      return ret;
   }

   public PrintCmd createInitPrinterCmd(IBM4689PrinterCmd.InitPrinterCmd ipc) {
      PrintCmd ret = this.factory.createCmd(ipc);
      ret.appendCmd(this.getEscBytes().INITIALIZE);
      byte id = this.factory.generateID();
      ipc.setID(id);
      ret.appendCmd(id);
      return ret;
   }

   public PrintCmd createSendCheckSumCmd(IBM4689PrinterCmd.SendCheckSumCmd sckc) {
      PrintCmd ret = this.factory.createCmd(sckc);
      ret.appendCmd(this.getEscBytes().SEND_CHKSUM);
      byte area = sckc.getArea();
      ret.appendCmd(area);
      return ret;
   }

   public PrintCmd createFeedCmd(IBM4689PrinterCmd.FeedCmd fc) {
      PrintCmd ret = this.factory.createCmd(fc);
      byte station = fc.getStation();
      switch (station) {
         case 2:
            ret.appendCmd(this.getEscBytes().CR_EJECT);
            break;
         case 8:
            ret.appendCmd(this.getEscBytes().JR_EJECT);
            break;
         case 32:
            ret.appendCmd(this.getEscBytes().JR_CR_EJECT);
      }

      ret.appendCmd(fc.getLines());
      byte id = this.factory.generateID();
      fc.setID(id);
      ret.appendCmd(id);
      return ret;
   }

   public PrintCmd createPartialCutCmd(IBM4689PrinterCmd.PartialCutCmd pc) {
      PrintCmd ret = this.factory.createCmd(pc);
      ret.appendCmd(this.getEscBytes().CR_PARTIAL_CUT);
      byte id = this.factory.generateID();
      pc.setID(id);
      ret.appendCmd(id);
      return ret;
   }

   public PrintCmd createFeedUnitsCmd(POSPrinterCmd.FeedUnitsCmd feedUnitsCmd) {
      PrintCmd printerCmd = this.factory.createCmd(feedUnitsCmd);
      printerCmd.appendCmd(this.getEscBytes().FEED_UNITS);
      printerCmd.appendCmd((byte)feedUnitsCmd.getUnits());
      return printerCmd;
   }

   public PrintCmd createFeedUnitsBackCmd(IBM4689PrinterCmd.FeedUnitsBackCmd fubc) {
      PrintCmd printerCmd = this.factory.createCmd(fubc);
      printerCmd.appendCmd(this.getEscBytes().FEED_UNITS_BACK);
      printerCmd.appendCmd(fubc.getLines());
      return printerCmd;
   }

   public PrintCmd createMCTValueCmd(POSPrinterCmd.MCTValueCmd wmctc) {
      PrintCmd ret = this.factory.createCmd(wmctc);
      ret.appendCmd(this.getEscBytes().MCT_WRITE);
      ret.appendCmd(wmctc.getMatrixValue());
      ret.appendCmd(wmctc.getByteHighValue());
      ret.appendCmd(wmctc.getByteLowValue());
      return ret;
   }

   public PrintCmd createSelectStationCmd(POSPrinterCmd.SelectStationCmd ssc) {
      PrintCmd ret = this.factory.createCmd(ssc);
      ret.appendCmd(this.getEscBytes().STATION);
      if (ssc.getStation() == 8) {
         ret.appendCmd(this.PTR_JOURNAL_STATION);
      } else if (ssc.getStation() == 2) {
         ret.appendCmd(this.PTR_RECEIPT_STATION);
      } else {
         ret.appendCmd(this.PTR_RECEIPT_STATION);
      }

      return ret;
   }

   public PrintCmd createDevInfoCmd(POSPrinterCmd.DevInfoCmd dic) {
      PrintCmd ret = this.factory.createCmd(dic);
      ret.appendCmd(this.getEscBytes().DEVICE_INFO);
      byte id = this.factory.generateID();
      dic.setID(id);
      ret.appendCmd(id);
      ret.setWaitToStart(true);
      return ret;
   }

   public PrintCmd createECLevelRequestCmd(IBM4689PrinterCmd.ECLevelRequestCmd elrCmd) {
      PrintCmd printerCmd = this.factory.createCmd(elrCmd);
      printerCmd.appendCmd(this.getEscBytes().EC_REQUEST);
      byte id = this.factory.generateID();
      elrCmd.setID(id);
      printerCmd.appendCmd(id);
      printerCmd.setWaitToStart(true);
      return printerCmd;
   }

   public PrintCmd createTestRequestCmd(POSPrinterCmd.TestReqCmd tq) {
      PrintCmd pCmd = this.factory.createCmd(tq);
      pCmd.appendCmd(this.getEscBytes().TEST_REQ);
      byte id = this.factory.generateID();
      tq.setID(id);
      pCmd.appendCmd(id);
      pCmd.setWaitToStart(true);
      return pCmd;
   }

   public PrintCmd createStatusRequestCmd(POSPrinterCmd.StatusRequestCmd src) {
      PrintCmd ret = this.factory.createCmd(src);
      ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      byte id = this.factory.generateID();
      src.setID(id);
      ret.appendCmd(id);
      return ret;
   }

   public PrintCmd createResetCmd(POSPrinterCmd.ResetCmd rsc) {
      PrintCmd ret = this.factory.createCmd(rsc);
      ret.appendCmd(this.getEscBytes().RESET);
      byte id = this.factory.generateID();
      rsc.setID(id);
      ret.appendCmd(id);
      ret.setWaitToStart(true);
      return ret;
   }

   public PrintCmd createCutPaperCmd(POSPrinterCmd.CutPaperCmd cutPaperCmd) {
      PrintCmd ret = this.factory.createCmd(cutPaperCmd);
      short perc = cutPaperCmd.getPercentage();
      if (perc > 50 && perc <= 100) {
         ret.appendCmd(this.getEscBytes().CUT_PAPER);
      } else if (perc == 0) {
         ret.appendCmd(this.getEscBytes().FEED_UNITS);
         ret.appendCmd((byte)21);
      } else {
         ret.appendCmd(this.getEscBytes().PARTIAL_CUT);
      }

      return ret;
   }

   public PrintCmd createMCTCmd(POSPrinterCmd.MCTCmd mc) {
      PrintCmd ret = this.factory.createCmd(mc);
      ret.appendCmd(this.getEscBytes().append(this.getEscBytes().MCT_READ, mc.getMCT()));
      ret.setWaitToFinish(true);
      return ret;
   }

   public PrintCmd createFormFeedLengthCmd(POSPrinterCmd.FormFeedLengthCmd flc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createMarkFeedCmd(POSPrinterCmd.MarkFeedCmd mfc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createEraseFlashSectorCmd(POSPrinterCmd.EraseFlashSectorCmd efsc) throws HandleException {
      PrintCmd ret = this.factory.createCmd(efsc);
      ret.appendCmd(this.getEscBytes().append(this.getEscBytes().ERASE_FLASH, efsc.getSector()));
      ret.setWaitToFinish(true);
      return ret;
   }

   public PrintCmd createFeedLinesCmd(POSPrinterCmd.FeedLinesCmd feedLinesCmd) throws HandleException {
      PrintCmd printerCmd = this.factory.createCmd(feedLinesCmd);
      switch (printerCmd.getStation()) {
         case 0:
            printerCmd.appendCmd(this.getEscBytes().FEED_LINES_RECEIPT);
            break;
         case 2:
            printerCmd.appendCmd(this.getEscBytes().FEED_LINES_RECEIPT);
            break;
         case 8:
            printerCmd.appendCmd(this.getEscBytes().FEED_LINES_JOURNAL);
      }

      printerCmd.appendCmd((byte)feedLinesCmd.getLines());
      return printerCmd;
   }

   public PrintCmd createFeedReverseCmd(POSPrinterCmd.FeedReverseCmd feedReverseCmd) {
      PrintCmd printerCmd = this.factory.createCmd(feedReverseCmd);
      printerCmd.appendCmd(this.getEscBytes().FEED_REVERSE);
      printerCmd.appendCmd((byte)feedReverseCmd.getLines());
      return printerCmd;
   }

   public PrintCmd createPassThruCmd(POSPrinterCmd.PassThruCmd ptCmd) throws HandleException {
      return null;
   }

   public PrintCmd createContinuation4689Cmd(IBM4689PrinterCmd.Continuation4689Cmd cc) {
      PrintCmd ret = this.factory.createCmd(cc);
      byte[] dataArray = cc.getData();
      ret.appendCmd(dataArray);
      return ret;
   }

   public PrintCmd createStatisticCmd(POSPrinterCmd.StatisticCmd statisticCmd) {
      PrintCmd ret = this.factory.createCmd(statisticCmd);
      String statisticType = statisticCmd.getStatisticType();
      if (!this.getStatisticsMapping().containsKey(statisticType)) {
      }

      ret.appendCmd((byte[])this.getStatisticsMapping().get(statisticType));
      return ret;
   }

   private Map getStatisticsMapping() {
      if (this.statisticsMapping == null) {
         this.statisticsMapping = new HashMap();
         this.statisticsMapping.put("JournalLinePrintedCount", this.getEscBytes().STATUS_REQUEST);
         this.statisticsMapping.put("PaperCutCount", this.getEscBytes().STATUS_REQUEST);
         this.statisticsMapping.put("ReceiptLinePrintedCount", this.getEscBytes().STATUS_REQUEST);
      }

      return this.statisticsMapping;
   }
}
