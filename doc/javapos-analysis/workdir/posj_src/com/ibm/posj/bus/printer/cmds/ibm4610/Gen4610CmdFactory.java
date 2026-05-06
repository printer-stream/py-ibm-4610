package com.ibm.posj.bus.printer.cmds.ibm4610;

import com.ibm.jutil.tasks.Mapping;
import com.ibm.jutil.tracing.Tracing;
import com.ibm.posj.HandleException;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;
import java.util.HashMap;
import java.util.Map;

public class Gen4610CmdFactory implements General4610CmdFactory {
   private PrintCmdFactory factory = null;
   private static final byte CMD_PARAM_OFF = 0;
   private static final byte CMD_PARAM_ON = 1;
   private Mapping mapping = new Mapping();
   private Map statisticsMapping;
   public static final double NOTERATIO = 1.0595;
   public static final int TI_MIN_DURATION = 1;
   public static final int TI_MAX_DURATION = 254;
   public static final int TI_VOLUME_LOW = 1;
   public static final int TI_VOLUME_HIGH = 100;
   public static final int VOLUME_LOW_CMD = 128;
   public static final int VOLUME_HIGH_CMD = 0;
   public static final int YEN_SIGN = 171;
   public static final int CODE_PAGE_US = 437;
   public static final int CODE_PAGE_ML = 858;

   public Gen4610CmdFactory(PrintCmdFactory f) {
      this.factory = f;
   }

   private Cmd4610 getEscBytes() {
      return (Cmd4610)this.factory.getCmdBytes();
   }

   public void setLineCnt(boolean b) {
   }

   public static int countLines(String string) {
      int c = 0;
      int i = -1;

      while (-1 != (i = string.indexOf(10, i + 1))) {
         c++;
      }

      Tracing.println("Lines Counted " + c);
      return c;
   }

   public PrintCmd createStatisticCmd(POSPrinterCmd.StatisticCmd statisticCmd) {
      PrintCmd ret = this.factory.createCmd(statisticCmd);
      ret.appendCmd(this.getEscBytes().COMMON_STATISTIC_CMD);
      ret.appendCmd((byte[])this.getStatisticsMapping().get(statisticCmd.getStatisticType()));
      ret.setWaitToFinish(true);
      ret.setWaitToStart(true);
      return ret;
   }

   private Map getStatisticsMapping() {
      if (this.statisticsMapping == null) {
         this.statisticsMapping = new HashMap();
         this.statisticsMapping.put("ManufactureDate", this.getEscBytes().MANUFACTURE_DATE);
         this.statisticsMapping.put("FormInsertionCount", this.getEscBytes().FORM_INSERTION_COUNT);
         this.statisticsMapping.put("FormInsertionCountRemainder", this.getEscBytes().FORM_INSERTION_COUNT_REMAINDER);
         this.statisticsMapping.put("HomeErrorCount", this.getEscBytes().HOME_ERROR_COUNT);
         this.statisticsMapping.put("PaperCutCount", this.getEscBytes().PAPER_CUT_COUNT);
         this.statisticsMapping.put("PaperCutCountRemainder", this.getEscBytes().PAPER_CUT_COUNT_REMAINDER);
         this.statisticsMapping.put("FailedPaperCutCount", this.getEscBytes().FAILED_PAPER_CUT_COUNT);
         this.statisticsMapping.put("ReceiptCoverOpenCount", this.getEscBytes().RECEIPT_COVER_OPEN_COUNT);
         this.statisticsMapping.put("SlipCharacterPrintedCount", this.getEscBytes().SLIP_CHARACTER_PRINTED_COUNT);
         this.statisticsMapping.put("SlipCharacterPrintedCountRemainder", this.getEscBytes().SLIP_CHARACTER_PRINTED_COUNT_REMAINDER);
         this.statisticsMapping.put("SlipCoverOpenCount", this.getEscBytes().SLIP_COVER_OPEN_COUNT);
         this.statisticsMapping.put("ReceiptCharacterPrintedCountRemainder", this.getEscBytes().RECEIPT_CHARACTER_PRINTED_COUNT_HIGH);
         this.statisticsMapping.put("ReceiptCharacterPrintedCount", this.getEscBytes().RECEIPT_CHARACTER_PRINTED_COUNT_LOW);
         this.statisticsMapping.put("PrintSideChangeCount", this.getEscBytes().PRINT_SIDE_CHANGE_COUNT);
         this.statisticsMapping.put("PrintSideChangeCountRemainder", this.getEscBytes().PRINT_SIDE_CHANGE_COUNT_REMAINDER);
         this.statisticsMapping.put("FailedPrintSideChangeCount", this.getEscBytes().FAILED_PRINT_SIDE_CHANGE_COUNT);
         this.statisticsMapping.put("FailedPrintSideChangeCountRemainder", this.getEscBytes().FAILED_PRINT_SIDE_CHANGE_COUNT_REMAINDER);
         this.statisticsMapping.put("ReceiptLineFeedCount", this.getEscBytes().RECEIPT_LINE_FEED_COUNT);
         this.statisticsMapping.put("ReceiptLineFeedCountRemainder", this.getEscBytes().RECEIPT_LINE_FEED_COUNT_REMAINDER);
         this.statisticsMapping.put("SlipLineFeedCount", this.getEscBytes().SLIP_LINE_FEED_COUNT);
         this.statisticsMapping.put("SlipLineFeedCountRemainder", this.getEscBytes().SLIP_LINE_FEED_COUNT_REMAINDER);
         this.statisticsMapping.put("BarcodePrintedCount", this.getEscBytes().BARCODE_PRINTED_COUNT);
         this.statisticsMapping.put("BarcodePrintedCountRemainder", this.getEscBytes().BARCODE_PRINTED_COUNT_REMAINDER);
         this.statisticsMapping.put("MaximumTempReachedCount", this.getEscBytes().MAXIMUM_TEMP_REACHED_COUNT);
         this.statisticsMapping.put("NVRAMWriteCount", this.getEscBytes().NVRAM_WRITE_COUNT);
         this.statisticsMapping.put("FailedReadCount", this.getEscBytes().FAILED_READ_COUNT);
         this.statisticsMapping.put("FailedReadCountRemainder", this.getEscBytes().FAILED_READ_COUNT_REMAINDER);
         this.statisticsMapping.put("TotalReadCount", this.getEscBytes().TOTAL_READ_COUNT);
         this.statisticsMapping.put("TotalReadCountRemainder", this.getEscBytes().TOTAL_READ_COUNT_REMAINDER);
         this.statisticsMapping.put("IBM_CheckScannedCount", this.getEscBytes().CHECK_SCANNED_COUNT);
         this.statisticsMapping.put("IBM_CheckScannedCountRemainder", this.getEscBytes().CHECK_SCANNED_COUNT_REMAINDER);
         this.statisticsMapping.put("IBM_ChecksFailedQualityCount", this.getEscBytes().CHECKS_FAILED_QUALITY_COUNT);
         this.statisticsMapping.put("IBM_CheckScannerBrightnessQuality", this.getEscBytes().CHECK_SCANNER_BRIGHTNESS_QUALITY);
         this.statisticsMapping.put("IBM_CheckScannerContrastQuality", this.getEscBytes().CHECK_SCANNER_CONTRAST_QUALITY);
         this.statisticsMapping.put("IBM_CheckScannerFocusQuality", this.getEscBytes().CHECK_SCANNER_FOCUS_QUALITY);
      }

      return this.statisticsMapping;
   }

   public PrintCmd createEnableLineCntCmd(IBM4610PrinterCmd.EnableLineCntCmd elc) {
      PrintCmd ret = this.factory.createCmd(elc);
      ret.appendCmd(this.getEscBytes().append(this.getEscBytes().DISABLE_LINECNT, (byte)(elc.isEnabled() ? 0 : 1)));
      return ret;
   }

   public PrintCmd createReinitPrinterCmd(IBM4610PrinterCmd.ReinitPrinterCmd rpc) {
      PrintCmd ret = this.factory.createCmd(rpc);
      ret.appendCmd(this.getEscBytes().REINIT);
      ret.setWaitToFinish(true);
      return ret;
   }

   public PrintCmd createResetLineCntCmd(IBM4610PrinterCmd.ResetLineCntCmd rlc) {
      PrintCmd ret = this.factory.createCmd(rlc);
      ret.appendCmd(this.getEscBytes().RESET_LINECNT);
      return ret;
   }

   public PrintCmd createEmptyCmd(int station) {
      return this.factory.createCmd(null);
   }

   public PrintCmd createStatusRequestCmd(POSPrinterCmd.StatusRequestCmd src) {
      PrintCmd ret = this.factory.createCmd(src);
      ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      return ret;
   }

   public PrintCmd createOpenDrawerCmd(IBM4610PrinterCmd.OpenDrawerCmd odc) {
      PrintCmd ret = this.factory.createCmd(odc);
      ret.appendCmd(this.getEscBytes().PULSE_DRAWER);
      ret.appendCmd(odc.getNumber());
      ret.appendCmd(odc.getWidthOn());
      ret.appendCmd(odc.getWidthOff());
      ret.setWaitToFinish(true);
      return ret;
   }

   public PrintCmd createBeeperCmd(IBM4610PrinterCmd.BeeperCmd toneCmd) {
      PrintCmd ret = this.factory.createCmd(toneCmd);
      ret.appendCmd(this.getEscBytes().BEEPER);
      ret.appendCmd((byte)this.mapping.mapValue(toneCmd.getDuration() / 100, this.getDurationValues()));
      byte volume = (byte)this.mapping.mapValue(toneCmd.getVolume(), this.getVolumeValues());
      byte frequency = (byte)this.mapping.mapValue(toneCmd.getFrequency(), this.getFrequencyValues());
      int inc = 0;
      double tfreq = (double)toneCmd.getFrequency();
      long Octave = 0L;

      for (long Note = 0L; tfreq > 261.7; inc++) {
         tfreq /= 1.0595;
      }

      Octave = (long)(inc / 12) << 4;
      long var13 = (long)(inc % 12);
      ret.appendCmd((byte)((int)((long)volume | Octave | var13)));
      ret.setWaitToFinish(true);
      return ret;
   }

   public PrintCmd createPrintNormalCmd(POSPrinterCmd.PrintNormalCmd pnc) {
      PrintCmd ret = this.factory.createCmd(pnc);
      byte[] dataArray = pnc.getData();
      if (dataArray != null) {
         ret.appendCmd(dataArray, pnc.getIndex(), pnc.getCount());
      } else if (pnc.getCount() == 171) {
         this.printYenChar(pnc, ret);
      }

      if (pnc.appendByte() != 0) {
         ret.appendCmd(pnc.appendByte());
      }

      if (pnc.isImmediate()) {
         ret.setBuffered(false);
      }

      return ret;
   }

   private void printYenChar(POSPrinterCmd.PrintNormalCmd pnc, PrintCmd ret) {
      int codePage = pnc.getIndex();
      if (codePage == 437 || codePage == 858) {
         ret.appendCmd(this.getEscBytes().SELECT_CODE_PAGE);
         ret.appendCmd((byte)5);
         ret.appendCmd((byte)-85);
         byte restoreCP = (byte)(pnc.getIndex() == 437 ? 0 : 1);
         ret.appendCmd(this.getEscBytes().SELECT_CODE_PAGE);
         ret.appendCmd(restoreCP);
      }
   }

   public PrintCmd createTestCmd() {
      return null;
   }

   public PrintCmd createMCTCmd(POSPrinterCmd.MCTCmd mc) {
      PrintCmd ret = this.factory.createCmd(mc);
      ret.appendCmd(this.getEscBytes().append(this.getEscBytes().MCT_READ, mc.getMCT()));
      ret.setWaitToFinish(true);
      return ret;
   }

   public PrintCmd createMCTValueCmd(POSPrinterCmd.MCTValueCmd mvc) {
      PrintCmd ret = this.factory.createCmd(mvc);
      ret.appendCmd(this.getEscBytes().MCT_WRITE);
      ret.appendCmd(mvc.getMatrixValue());
      ret.appendCmd(mvc.getByteHighValue());
      ret.appendCmd(mvc.getByteLowValue());
      return ret;
   }

   public PrintCmd createContinuationCmd(IBM4610PrinterCmd.ContinuationCmd cc) {
      PrintCmd ret = this.factory.createCmd(cc);
      if (cc.isEscapeNeeded()) {
         ret.appendCmd(this.getEscBytes().CONTINUATION_CMD);
      }

      if (!cc.isSimple()) {
         if (Tracing.isOn()) {
            Tracing.println("--append length-- " + cc.getLength());
         }

         ret.appendCmd(cc.getData(), cc.getStart(), cc.getLength());
      }

      return ret;
   }

   public PrintCmd createFormFeedLengthCmd(POSPrinterCmd.FormFeedLengthCmd flc) {
      PrintCmd ret = this.factory.createCmd(flc);
      ret.appendCmd(this.getEscBytes().append(this.getEscBytes().FORMFEED_LENGTH, flc.getFormFeedLength()));
      return ret;
   }

   public PrintCmd createResetCmd(POSPrinterCmd.ResetCmd rsc) {
      PrintCmd ret = this.factory.createCmd(rsc);
      ret.appendCmd(this.getEscBytes().RESET);
      return ret;
   }

   public PrintCmd createMarkFeedCmd(POSPrinterCmd.MarkFeedCmd mfc) {
      return null;
   }

   public PrintCmd createEnableFeedButtonCmd(IBM4610PrinterCmd.EnableFeedButtonCmd efbc) {
      PrintCmd ret = this.factory.createCmd(efbc);
      ret.appendCmd(this.getEscBytes().append(this.getEscBytes().BUTTON_ENABLE, (byte)(efbc.enabled() ? 1 : 0)));
      return ret;
   }

   public PrintCmd createChangePrintSideCmd(IBM4610PrinterCmd.ChangePrintSideCmd cpsc) {
      PrintCmd ret = this.factory.createCmd(cpsc);
      ret.appendCmd(this.getEscBytes().FLIP_CHECK);
      return ret;
   }

   public PrintCmd createSelectStationCmd(POSPrinterCmd.SelectStationCmd ssc) {
      PrintCmd ret = this.factory.createCmd(ssc);
      this.factory.setCmdsStation(ret, ssc.getStation());
      return ret;
   }

   public PrintCmd createErrorRecoveryCmd(IBM4610PrinterCmd.ErrorRecoveryCmd erc) {
      PrintCmd ret = this.factory.createCmd(erc);
      byte ans = 0;
      if (!erc.isPrintBufferRelease()) {
         ans = (byte)(ans | 4);
      }

      if (!erc.isAutoHomeErrorRetry()) {
         ans = (byte)(ans | 8);
      }

      if (erc.isHoldForDocument()) {
         ans = (byte)(ans | 16);
      }

      if (erc.isHoldForFlipError()) {
         ans = (byte)(ans | 32);
      }

      ret.appendCmd(this.getEscBytes().append(this.getEscBytes().ERROR_RECOVERY, ans));
      return ret;
   }

   public PrintCmd createDevInfoCmd(POSPrinterCmd.DevInfoCmd dic) {
      PrintCmd ret = this.factory.createCmd(dic);
      ret.appendCmd(this.getEscBytes().DEVICE_INFO);
      ret.setWaitToStart(true);
      return ret;
   }

   public PrintCmd createHeadMovementCmd(IBM4610PrinterCmd.HeadMovementCmd hmc) {
      PrintCmd ret = this.factory.createCmd(hmc);
      ret.appendCmd(this.getEscBytes().RETURN_HOME);
      ret.appendCmd(hmc.getPosition());
      return ret;
   }

   public PrintCmd createCutPaperCmd(POSPrinterCmd.CutPaperCmd cutPaperCmd) {
      PrintCmd printerCmd = this.factory.createCmd(cutPaperCmd);
      if (cutPaperCmd.getLinesToPaperCut() > 0) {
         this.insertFeedLineChar(printerCmd, cutPaperCmd.getLinesToPaperCut());
      }

      if (cutPaperCmd.getPercentage() != 0) {
         printerCmd.appendCmd(this.getEscBytes().CUT_PAPER);
      } else if (cutPaperCmd.getLinesToPaperCut() == 0) {
         this.insertFeedLineChar(printerCmd, (short)1);
      }

      return printerCmd;
   }

   public PrintCmd createFeedLinesCmd(POSPrinterCmd.FeedLinesCmd feedLinesCmd) {
      PrintCmd printerCmd = this.factory.createCmd(feedLinesCmd);
      this.insertFeedLineChar(printerCmd, feedLinesCmd.getLines());
      return printerCmd;
   }

   public PrintCmd createFeedUnitsCmd(POSPrinterCmd.FeedUnitsCmd feedUnitsCmd) {
      PrintCmd printerCmd = this.factory.createCmd(feedUnitsCmd);
      printerCmd.appendCmd(this.getEscBytes().FEED_UNITS);
      printerCmd.appendCmd((byte)feedUnitsCmd.getUnits());
      return printerCmd;
   }

   public PrintCmd createECLevelRequestCmd(IBM4610PrinterCmd.ECLevelRequestCmd elrCmd) {
      PrintCmd printerCmd = this.factory.createCmd(elrCmd);
      if (elrCmd.isBuffered()) {
         printerCmd.appendCmd(this.getEscBytes().BUFFERED_EC);
      } else {
         printerCmd.appendCmd(this.getEscBytes().IMMEDIATE_EC);
      }

      return printerCmd;
   }

   public PrintCmd createFeedReverseCmd(POSPrinterCmd.FeedReverseCmd feedReverseCmd) {
      PrintCmd printerCmd = this.factory.createCmd(feedReverseCmd);
      printerCmd.appendCmd(this.getEscBytes().FEED_REVERSE);
      printerCmd.appendCmd((byte)feedReverseCmd.getLines());
      return printerCmd;
   }

   public PrintCmd createReleasePrintBufferCmd(IBM4610PrinterCmd.ReleasePrintBufferCmd rpc) {
      PrintCmd ret = this.factory.createCmd(rpc);
      if (!rpc.isCancelCmd()) {
         ret.appendCmd(this.getEscBytes().RELEASE_BUFFER);
      } else {
         ret.appendCmd(this.getEscBytes().CANCEL_BUFFER);
      }

      ret.setWaitToFinish(true);
      return ret;
   }

   public PrintCmd createEraseFlashSectorCmd(POSPrinterCmd.EraseFlashSectorCmd efsc) {
      byte sector = efsc.getSector();
      byte n = 0;
      PrintCmd ret = this.factory.createCmd(efsc);
      switch (sector) {
         case 1:
            ret.appendCmd(this.getEscBytes().ERASE_FLASH_DL_GRAPHICS);
            break;
         case 2:
            ret.appendCmd(this.getEscBytes().ERASE_FLASH_PRE_MESSAGES);
            break;
         case 3:
            ret.appendCmd(this.getEscBytes().ERASE_FLASH_USR_DEF_IMPACT_CHAR_SETS);
            break;
         case 4:
            ret.appendCmd(this.getEscBytes().ERASE_FLASH_USR_DEF_THERMAL_CHAR_SETS);
            break;
         case 5:
            ret.appendCmd(this.getEscBytes().ERASE_FLASH_USR_FLA_STORAGE);
            break;
         case 6:
            ret.appendCmd(this.getEscBytes().ERASE_FLASH_ALL_DBL_BYTE_CHARS);
         case 7:
         default:
            break;
         case 8:
            ret.appendCmd(this.getEscBytes().ERASE_FLASH_CHECK_IMAGES);
      }

      ret.setWaitToFinish(true);
      ret.setWaitToStart(true);
      return ret;
   }

   public PrintCmd createSelectCodePageCmd(POSPrinterCmd.SelectCodePageCmd scpc) {
      PrintCmd ret = this.factory.createCmd(scpc);
      ret.appendCmd(this.getEscBytes().SELECT_CODE_PAGE);
      ret.appendCmd(scpc.getSelectedCodePage());
      return ret;
   }

   private void insertFeedLineChar(PrintCmd prtCmd, short lines) {
      if (lines < 0) {
         lines = 1;
      }

      for (int i = 0; i < lines; i++) {
         if (null != prtCmd.getByteBuffer()) {
            byte[] b = prtCmd.getBytes();
            if (0 == prtCmd.getByteBuffer().getByteCount()) {
               prtCmd.appendCmd((byte)32);
            } else if (10 == b[prtCmd.getByteBuffer().getByteCount() - 1]) {
               prtCmd.appendCmd((byte)32);
            }
         } else {
            prtCmd.appendCmd((byte)32);
         }

         prtCmd.appendCmd(this.getEscBytes().LINE_FEED_CHAR);
      }

      Tracing.println("Raul inserts Line Count " + lines);
   }

   public PrintCmd createTestRequestCmd(POSPrinterCmd.TestReqCmd tq) {
      PrintCmd ret = this.factory.createCmd(tq);
      ret.appendCmd(this.getEscBytes().TEST_REQ);
      ret.setWaitToFinish(true);
      return ret;
   }

   public PrintCmd createSolicitStatusCmd(IBM4610PrinterCmd.SolicitStatusCmd ssCmd) {
      PrintCmd ret = this.factory.createCmd(ssCmd);
      Rs232Cmd4610 esc = (Rs232Cmd4610)this.getEscBytes();
      ret.appendCmd(esc.SOLICIT_STATUS);
      ret.appendCmd((byte)(ssCmd.enable() ? 65 : 66));
      return ret;
   }

   public PrintCmd createStatusSentCmd(IBM4610PrinterCmd.StatusSentCmd ssCmd) {
      PrintCmd printerCmd = this.factory.createCmd(ssCmd);
      printerCmd.appendCmd(this.getEscBytes().STATUS_SENT);
      byte sbyte = (byte)(!ssCmd.isBuffEmptyOn() ? 1 : 0);
      sbyte = (byte)(sbyte + 0);
      sbyte = (byte)(sbyte + (byte)((!ssCmd.isFrontDIOn() ? 1 : 0) * 4));
      sbyte = (byte)(sbyte + (byte)((!ssCmd.isTopDIOn() ? 1 : 0) * 8));
      sbyte = (byte)(sbyte + (byte)((!ssCmd.isLineCntOn() ? 1 : 0) * 16));
      sbyte = (byte)(sbyte + (byte)((!ssCmd.isCDChangeOn() ? 1 : 0) * 32));
      sbyte = (byte)(sbyte + (byte)((!ssCmd.isKeyChangeOn() ? 1 : 0) * 64));
      sbyte = (byte)(sbyte + (byte)((!ssCmd.isCoverOpenOn() ? 1 : 0) * 128));
      printerCmd.appendCmd(sbyte);
      return printerCmd;
   }

   public PrintCmd createXonXoffModeCmd(IBM4610PrinterCmd.XonXoffModeCmd xo) {
      PrintCmd printerCmd = this.factory.createCmd(xo);
      if (null != this.getEscBytes().XON_XOFF_MODE && !xo.isTransparent()) {
         printerCmd.appendCmd(this.getEscBytes().XON_XOFF_MODE);
      }

      return printerCmd;
   }

   public PrintCmd createHoldBufferCmd(IBM4610PrinterCmd.HoldBufferCmd hbc) {
      PrintCmd ret = this.factory.createCmd(hbc);
      ret.appendCmd(this.getEscBytes().HOLD_BUFFER);
      return ret;
   }

   public int[] getDurationValues() {
      return this.createDurationValues();
   }

   private int[] createDurationValues() {
      int[] durations = new int[254];
      int duration = 1;

      while (duration < durations.length) {
         durations[duration] = duration++;
      }

      return durations;
   }

   public int[][] getVolumeValues() {
      return new int[][]{{1, 100}, {128, 0}};
   }

   public int[][] getFrequencyValues() {
      return new int[][]{
         {
               0,
               1,
               2,
               3,
               4,
               5,
               6,
               7,
               8,
               9,
               10,
               11,
               15,
               16,
               17,
               18,
               19,
               20,
               21,
               22,
               23,
               24,
               25,
               26,
               27,
               31,
               32,
               33,
               34,
               35,
               36,
               37,
               38,
               39,
               40,
               41,
               42,
               43,
               47,
               48,
               49,
               50,
               51,
               52,
               53,
               54,
               55,
               56,
               57,
               58,
               59,
               63
         },
         {
               0,
               1,
               2,
               3,
               4,
               5,
               6,
               7,
               8,
               9,
               10,
               11,
               15,
               16,
               17,
               18,
               19,
               20,
               21,
               22,
               23,
               24,
               25,
               26,
               27,
               31,
               32,
               33,
               34,
               35,
               36,
               37,
               38,
               39,
               40,
               41,
               42,
               43,
               47,
               48,
               49,
               50,
               51,
               52,
               53,
               54,
               55,
               56,
               57,
               58,
               59,
               63
         }
      };
   }

   public PrintCmd createPassThruCmd(POSPrinterCmd.PassThruCmd ptCmd) throws HandleException {
      PrintCmd ret = this.factory.createCmd(ptCmd);
      ret.appendCmd(ptCmd.toBytes());
      return ret;
   }
}
