package com.ibm.posj.bus.printer.cmds.ibm4689;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.FontCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;

public class Font4689CmdFactory implements FontCmdFactory {
   private static final byte CMD_PARAM_OFF = 0;
   private static final byte CMD_PARAM_ON = 1;
   private PrintCmdFactory factory = null;

   public Font4689CmdFactory(PrintCmdFactory f) {
      this.factory = f;
   }

   private Cmd4689 getEscBytes() {
      return (Cmd4689)this.factory.getCmdBytes();
   }

   private PrintCmd createCmd(DefaultPOSPrinterCmd cmd) {
      PrintCmd ret = null;
      if (cmd != null && !cmd.isPrintCmdNull()) {
         ret = cmd.getPrintCmd();
      } else {
         ret = this.factory.createCmd();
      }

      ret.setBytes(cmd.getOutboundPacket());
      return ret;
   }

   public PrintCmd createDownloadFontCmd(IBM4689PrinterCmd.DownloadFontCmd dfc) {
      PrintCmd ret = this.factory.createCmd(dfc);
      ret.appendCmd(this.getEscBytes().DOWNLOAD_FONT);
      ret.appendCmd(dfc.getSize());
      ret.appendCmd(dfc.getbAdd());
      ret.appendCmd(dfc.geteAdd());
      ret.appendCmd(dfc.getData());
      return ret;
   }

   public PrintCmd createDownloadDBCSFontCmd(IBM4689PrinterCmd.DownloadDBCSFontCmd dfc) {
      PrintCmd ret = this.factory.createCmd(dfc);
      ret.appendCmd(this.getEscBytes().DOWNLOAD_DBCS_FONT);
      ret.appendCmd(dfc.getSize());
      ret.appendCmd(dfc.getcNum());
      ret.appendCmd(dfc.getData());
      return ret;
   }

   public PrintCmd createRotatePrintCmd(POSPrinterCmd.RotatePrintCmd rpc) {
      PrintCmd ret = this.factory.createCmd(rpc);
      ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      byte id = this.factory.generateID();
      rpc.setID(id);
      ret.appendCmd(id);
      return ret;
   }

   public PrintCmd createTextAttribCmd(POSPrinterCmd.TextAttribCmd tac) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createAlignmentCmd(POSPrinterCmd.AlignmentCmd ac) throws HandleException {
      return this.factory.createCmd(ac);
   }

   public PrintCmd createScaleFontCmd(POSPrinterCmd.ScaleFontCmd sfc) throws HandleException {
      int height = sfc.getHeight();
      int width = sfc.getWidth();
      return this.factory.createCmd(sfc);
   }

   public PrintCmd createSetUserDefinedCharCmd(POSPrinterCmd.SetUserDefinedCharCmd succ) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createSetProportionalCharCmd(POSPrinterCmd.SetProportionalCharCmd spcc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createLineSpacingCmd(POSPrinterCmd.LineSpacingCmd lsc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createSetTabStopsCmd(POSPrinterCmd.SetTabStopsCmd stsc) throws HandleException {
      return this.factory.createCmd(stsc);
   }

   public PrintCmd createLeftMarginCmd(POSPrinterCmd.LeftMarginCmd lmc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createRelativePositionCmd(POSPrinterCmd.RelativePositionCmd rpc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createPrintQualityCmd(POSPrinterCmd.PrintQualityCmd pqc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createSelectCodePageCmd(POSPrinterCmd.SelectCodePageCmd scpc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createFontTypeCmd(POSPrinterCmd.FontTypeCmd ftc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createDotSpacingCmd(POSPrinterCmd.DotSpacingCmd dsc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createFontColorCmd(POSPrinterCmd.FontColorCmd fcc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createEnableColorModeCmd(POSPrinterCmd.EnableColorModeCmd ecm) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createBoldCmd(POSPrinterCmd.BoldCmd boldCmd) throws HandleException {
      return this.factory.createCmd(boldCmd);
   }

   public PrintCmd createUnderlineCmd(POSPrinterCmd.UnderlineCmd underlineCmd) throws HandleException {
      return this.factory.createCmd(underlineCmd);
   }

   public PrintCmd createReverseVideoCmd(POSPrinterCmd.ReverseVideoCmd reverseVideoCmd) throws HandleException {
      return this.factory.createCmd(reverseVideoCmd);
   }

   public PrintCmd createAlterWideHighCmd(POSPrinterCmd.AlterWideHighCmd alterWideHighCmd) throws HandleException {
      int option = alterWideHighCmd.getOption();
      return this.factory.createCmd(alterWideHighCmd);
   }

   public PrintCmd createNormalModeCmd(POSPrinterCmd.NormalModeCmd normalModeCmd) throws HandleException {
      return this.factory.createCmd(normalModeCmd);
   }

   public PrintCmd createAlignPositionCmd(POSPrinterCmd.AlignPositionCmd alignPositionCmd) throws HandleException {
      PrintCmd printerCmd = this.factory.createCmd(alignPositionCmd);
      printerCmd.appendCmd(this.getEscBytes().STATUS_REQUEST);
      byte id = this.factory.generateID();
      alignPositionCmd.setID(id);
      printerCmd.appendCmd(id);
      printerCmd.setWaitToStart(true);
      return printerCmd;
   }

   public PrintCmd createSetLeftMarginCmd(POSPrinterCmd.SetLeftMarginCmd slmc) throws HandleException {
      throw new HandleException("Command not Supported");
   }
}
