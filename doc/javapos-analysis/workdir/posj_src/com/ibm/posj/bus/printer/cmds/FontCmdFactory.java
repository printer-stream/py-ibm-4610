package com.ibm.posj.bus.printer.cmds;

import com.ibm.posj.HandleException;
import com.ibm.posj.POSPrinterCmd;

public interface FontCmdFactory {
   PrintCmd createRotatePrintCmd(POSPrinterCmd.RotatePrintCmd var1) throws HandleException;

   PrintCmd createTextAttribCmd(POSPrinterCmd.TextAttribCmd var1) throws HandleException;

   PrintCmd createAlignmentCmd(POSPrinterCmd.AlignmentCmd var1) throws HandleException;

   PrintCmd createScaleFontCmd(POSPrinterCmd.ScaleFontCmd var1) throws HandleException;

   PrintCmd createSetUserDefinedCharCmd(POSPrinterCmd.SetUserDefinedCharCmd var1) throws HandleException;

   PrintCmd createSetProportionalCharCmd(POSPrinterCmd.SetProportionalCharCmd var1) throws HandleException;

   PrintCmd createLineSpacingCmd(POSPrinterCmd.LineSpacingCmd var1) throws HandleException;

   PrintCmd createSetTabStopsCmd(POSPrinterCmd.SetTabStopsCmd var1) throws HandleException;

   PrintCmd createLeftMarginCmd(POSPrinterCmd.LeftMarginCmd var1) throws HandleException;

   PrintCmd createRelativePositionCmd(POSPrinterCmd.RelativePositionCmd var1) throws HandleException;

   PrintCmd createPrintQualityCmd(POSPrinterCmd.PrintQualityCmd var1) throws HandleException;

   PrintCmd createSelectCodePageCmd(POSPrinterCmd.SelectCodePageCmd var1) throws HandleException;

   PrintCmd createFontTypeCmd(POSPrinterCmd.FontTypeCmd var1) throws HandleException;

   PrintCmd createDotSpacingCmd(POSPrinterCmd.DotSpacingCmd var1) throws HandleException;

   PrintCmd createFontColorCmd(POSPrinterCmd.FontColorCmd var1) throws HandleException;

   PrintCmd createEnableColorModeCmd(POSPrinterCmd.EnableColorModeCmd var1) throws HandleException;

   PrintCmd createBoldCmd(POSPrinterCmd.BoldCmd var1) throws HandleException;

   PrintCmd createUnderlineCmd(POSPrinterCmd.UnderlineCmd var1) throws HandleException;

   PrintCmd createReverseVideoCmd(POSPrinterCmd.ReverseVideoCmd var1) throws HandleException;

   PrintCmd createAlterWideHighCmd(POSPrinterCmd.AlterWideHighCmd var1) throws HandleException;

   PrintCmd createNormalModeCmd(POSPrinterCmd.NormalModeCmd var1) throws HandleException;

   PrintCmd createAlignPositionCmd(POSPrinterCmd.AlignPositionCmd var1) throws HandleException;

   PrintCmd createSetLeftMarginCmd(POSPrinterCmd.SetLeftMarginCmd var1) throws HandleException;
}
