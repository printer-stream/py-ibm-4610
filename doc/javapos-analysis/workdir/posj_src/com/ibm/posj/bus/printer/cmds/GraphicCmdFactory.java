package com.ibm.posj.bus.printer.cmds;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.HandleException;

public interface GraphicCmdFactory {
   PrintCmd createPrintBitmapCmd(DefaultPOSPrinterCmd.PrintBitmapCmd var1) throws HandleException;

   PrintCmd createPrintBarCodeCmd(DefaultPOSPrinterCmd.PrintBarCodeCmd var1) throws HandleException;

   PrintCmd createPrintSetLogoCmd(DefaultPOSPrinterCmd.PrintSetLogoCmd var1) throws HandleException;

   PrintCmd createSetLogoCmd(DefaultPOSPrinterCmd.SetLogoCmd var1) throws HandleException;

   PrintCmd createSetBitmapCmd(DefaultPOSPrinterCmd.SetBitmapCmd var1) throws HandleException;

   PrintCmd createPrintSetBitmapCmd(DefaultPOSPrinterCmd.PrintSetBitmapCmd var1) throws HandleException;
}
