package com.ibm.posj.bus.printer.cmds;

import com.ibm.posj.HandleException;
import com.ibm.posj.POSPrinterCmd;

public interface GeneralCmdFactory {
   PrintCmd createStatisticCmd(POSPrinterCmd.StatisticCmd var1);

   PrintCmd createTestRequestCmd(POSPrinterCmd.TestReqCmd var1);

   PrintCmd createStatusRequestCmd(POSPrinterCmd.StatusRequestCmd var1);

   PrintCmd createPrintNormalCmd(POSPrinterCmd.PrintNormalCmd var1);

   PrintCmd createDevInfoCmd(POSPrinterCmd.DevInfoCmd var1);

   PrintCmd createFormFeedLengthCmd(POSPrinterCmd.FormFeedLengthCmd var1) throws HandleException;

   PrintCmd createResetCmd(POSPrinterCmd.ResetCmd var1);

   PrintCmd createMarkFeedCmd(POSPrinterCmd.MarkFeedCmd var1) throws HandleException;

   PrintCmd createSelectStationCmd(POSPrinterCmd.SelectStationCmd var1);

   PrintCmd createMCTCmd(POSPrinterCmd.MCTCmd var1) throws HandleException;

   PrintCmd createEraseFlashSectorCmd(POSPrinterCmd.EraseFlashSectorCmd var1) throws HandleException;

   PrintCmd createCutPaperCmd(POSPrinterCmd.CutPaperCmd var1);

   PrintCmd createFeedLinesCmd(POSPrinterCmd.FeedLinesCmd var1) throws HandleException;

   PrintCmd createFeedReverseCmd(POSPrinterCmd.FeedReverseCmd var1);

   PrintCmd createFeedUnitsCmd(POSPrinterCmd.FeedUnitsCmd var1) throws HandleException;

   PrintCmd createMCTValueCmd(POSPrinterCmd.MCTValueCmd var1) throws HandleException;

   PrintCmd createPassThruCmd(POSPrinterCmd.PassThruCmd var1) throws HandleException;
}
