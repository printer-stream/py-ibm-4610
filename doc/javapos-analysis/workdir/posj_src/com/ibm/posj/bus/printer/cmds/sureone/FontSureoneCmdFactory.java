package com.ibm.posj.bus.printer.cmds.sureone;

import com.ibm.jutil.Util;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.IBMSureonePrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.FontCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;

public class FontSureoneCmdFactory implements FontCmdFactory {
   private PrintCmdFactory factory = null;
   private static final byte CMD_PARAM_OFF = 0;
   private static final byte CMD_PARAM_ON = 1;

   public FontSureoneCmdFactory(PrintCmdFactory f) {
      this.factory = f;
   }

   private CmdSureone getEscBytes() {
      return (CmdSureone)this.factory.getCmdBytes();
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

   public PrintCmd createSelectIntCharSetCmd(IBMSureonePrinterCmd.SelectIntCharSetCmd sicsc) {
      PrintCmd ret = this.factory.createCmd(sicsc);
      ret.appendCmd(this.getEscBytes().SEL_CHAR_TABLE);
      ret.appendCmd(sicsc.getChar());
      return ret;
   }

   public PrintCmd createSelectIBMChar1Cmd(IBMSureonePrinterCmd.SelectIBMChar1Cmd sibm1c) {
      PrintCmd ret = this.factory.createCmd(sibm1c);
      ret.appendCmd(this.getEscBytes().SEL_IBM_1CHAR_TBL);
      return ret;
   }

   public PrintCmd createSelectIBMChar2Cmd(IBMSureonePrinterCmd.SelectIBMChar2Cmd sibm2c) {
      PrintCmd ret = this.factory.createCmd(sibm2c);
      ret.appendCmd(this.getEscBytes().SEL_IBM_2CHAR_TBL);
      return ret;
   }

   public PrintCmd createSelectNormalCharSpaceCmd(IBMSureonePrinterCmd.SelectNormalCharSpaceCmd sncsc) {
      PrintCmd ret = this.factory.createCmd(sncsc);
      ret.appendCmd(this.getEscBytes().SEL_NORMAL_CHAR_SPACNG);
      return ret;
   }

   public PrintCmd createSelectMediumCharSpaceCmd(IBMSureonePrinterCmd.SelectMediumCharSpaceCmd smcsc) {
      PrintCmd ret = this.factory.createCmd(smcsc);
      ret.appendCmd(this.getEscBytes().SEL_MEDIUM_CHAR_SPACNG);
      return ret;
   }

   public PrintCmd createSelectWideCharSpaceCmd(IBMSureonePrinterCmd.SelectWideCharSpaceCmd swcsc) {
      PrintCmd ret = this.factory.createCmd(swcsc);
      ret.appendCmd(this.getEscBytes().SEL_WIDE_CHAR_SPACNG);
      return ret;
   }

   public PrintCmd createSelectXtraWideCharSpaceCmd(IBMSureonePrinterCmd.SelectXtraWideCharSpaceCmd sxwcsc) {
      PrintCmd ret = this.factory.createCmd(sxwcsc);
      ret.appendCmd(this.getEscBytes().SEL_EXTRA_WIDE_CHAR_SPACNG);
      return ret;
   }

   public PrintCmd createSelect2xCharWidthCmd(IBMSureonePrinterCmd.Select2xCharWidthCmd s2xwcsc) {
      PrintCmd ret = this.factory.createCmd(s2xwcsc);
      ret.appendCmd(this.getEscBytes().SEL_2XCHAR_WIDTH_MODE);
      return ret;
   }

   public PrintCmd createSetWideModeCmd(IBMSureonePrinterCmd.SetWideModeCmd swmc) {
      PrintCmd ret = this.factory.createCmd(swmc);
      ret.appendCmd(this.getEscBytes().DOUBLE_WIDE_MODE);
      ret.appendCmd(swmc.getCharCode());
      return ret;
   }

   public PrintCmd createSelect2xCharHeightCmd(IBMSureonePrinterCmd.Select2xCharHeightCmd s2hc) {
      PrintCmd ret = this.factory.createCmd(s2hc);
      ret.appendCmd(this.getEscBytes().SET_PRNT_MAGNFIED_DBL_CHARH);
      return ret;
   }

   public PrintCmd createSetHighModeCmd(IBMSureonePrinterCmd.SetHighModeCmd shc) {
      PrintCmd ret = this.factory.createCmd(shc);
      ret.appendCmd(this.getEscBytes().DOUBLE_HIGH_MODE);
      ret.appendCmd(shc.getCharCode());
      return ret;
   }

   public PrintCmd createSelectBoldModeCmd(IBMSureonePrinterCmd.SelectBoldModeCmd sbc) {
      PrintCmd ret = this.factory.createCmd(sbc);
      ret.appendCmd(this.getEscBytes().SEL_EMPHASZD_PRNT_MODE);
      return ret;
   }

   public PrintCmd createOverlineCmd(IBMSureonePrinterCmd.OverlineCmd olc) {
      PrintCmd ret = this.factory.createCmd(olc);
      ret.appendCmd(this.getEscBytes().SET_OVERLINE_MODE);
      return ret;
   }

   public PrintCmd createSelectHighligthCmd(IBMSureonePrinterCmd.SelectHighligthCmd shlc) {
      PrintCmd ret = this.factory.createCmd(shlc);
      ret.appendCmd(this.getEscBytes().SEL_HIGHLIGHTED_PRNT_MODE);
      return ret;
   }

   public PrintCmd createDotSpacingCmd(POSPrinterCmd.DotSpacingCmd dsc) {
      PrintCmd ret = this.factory.createCmd(dsc);
      ret.appendCmd(this.getEscBytes().SET_DOT_SPACING);
      ret.appendCmd(Util.toByteArray(dsc.getDotSpacing()));
      return ret;
   }

   public PrintCmd createBoldCmd(POSPrinterCmd.BoldCmd bc) {
      PrintCmd ret = this.factory.createCmd(bc);
      if (bc.isActive()) {
         ret.appendCmd(this.getEscBytes().EMPHASIZE_MODE);
      } else {
         ret.appendCmd(this.getEscBytes().CANCEL_EMPHASIZED_PRINTNG);
      }

      return ret;
   }

   public PrintCmd createUnderlineCmd(POSPrinterCmd.UnderlineCmd uc) {
      PrintCmd ret = this.factory.createCmd(uc);
      ret.appendCmd(this.getEscBytes().UNDERLINE_MODE);
      return ret;
   }

   public PrintCmd createReverseVideoCmd(POSPrinterCmd.ReverseVideoCmd rvc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createLineSpacingCmd(POSPrinterCmd.LineSpacingCmd lsc) {
      PrintCmd ret = this.factory.createCmd(lsc);
      ret.appendCmd(this.getEscBytes().LINE_SPACING);
      return ret;
   }

   public PrintCmd createSetLineSpaceCmd(IBMSureonePrinterCmd.SetLineSpaceCmd slsc) {
      PrintCmd ret = this.factory.createCmd(slsc);
      ret.appendCmd(this.getEscBytes().COMPACT_LINE_SPACNG);
      return ret;
   }

   public PrintCmd createSetTightSpaceCmd(IBMSureonePrinterCmd.SetTightSpaceCmd stsc) {
      PrintCmd ret = this.factory.createCmd(stsc);
      ret.appendCmd(this.getEscBytes().TIGHT_LINE_SPACNG);
      return ret;
   }

   public PrintCmd createMicroFeedCmd(IBMSureonePrinterCmd.MicroFeedCmd mfc) {
      PrintCmd ret = this.factory.createCmd(mfc);
      ret.appendCmd(this.getEscBytes().TIME_MICRO_LINE_FEED);
      ret.appendCmd(mfc.getLines());
      return ret;
   }

   public PrintCmd createMicroBackFeedCmd(IBMSureonePrinterCmd.MicroBackFeedCmd mbfc) {
      PrintCmd ret = this.factory.createCmd(mbfc);
      ret.appendCmd(this.getEscBytes().TIME_BACKFEED);
      ret.appendCmd(mbfc.getLines());
      return ret;
   }

   public PrintCmd create_8mmFeedCmd(IBMSureonePrinterCmd._8mmFeedCmd fc) {
      PrintCmd ret = this.factory.createCmd(fc);
      ret.appendCmd(this.getEscBytes().TIME_8MM_LINE_FEED);
      ret.appendCmd(fc.getLines());
      return ret;
   }

   public PrintCmd createSetCrowdedSpaceCmd(IBMSureonePrinterCmd.SetCrowdedSpaceCmd scsc) {
      PrintCmd ret = this.factory.createCmd(scsc);
      ret.appendCmd(this.getEscBytes().CROWDED_LINE_SPACNG);
      return ret;
   }

   public PrintCmd createSetLeftMarginCmd(POSPrinterCmd.SetLeftMarginCmd lmc) {
      PrintCmd ret = this.factory.createCmd(lmc);
      ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
      ret.appendCmd(Util.toByteArray(lmc.getLeftMarginValue()));
      return ret;
   }

   public PrintCmd createSetRightMarginCmd(IBMSureonePrinterCmd.SetRightMarginCmd rmc) {
      PrintCmd ret = this.factory.createCmd(rmc);
      ret.appendCmd(this.getEscBytes().SET_RIGHT_MARGIN);
      ret.appendCmd(rmc.getMargin());
      return ret;
   }

   public PrintCmd createRotatePrintCmd(POSPrinterCmd.RotatePrintCmd tac) {
      PrintCmd ret = this.factory.createCmd(tac);
      if (tac.getRotation() > 0) {
         ret.appendCmd(this.getEscBytes().INVERT_MODE);
      } else {
         ret.appendCmd(this.getEscBytes().CANCEL_INVERTED_PRNT_MODE);
      }

      return ret;
   }

   public PrintCmd createTextAttribCmd(POSPrinterCmd.TextAttribCmd tac) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createAlignmentCmd(POSPrinterCmd.AlignmentCmd ac) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createScaleFontCmd(POSPrinterCmd.ScaleFontCmd sfc) throws HandleException {
      PrintCmd ret = this.factory.createCmd(sfc);
      byte width = sfc.getWidth();
      byte height = sfc.getHeight();
      ret.appendCmd(this.getEscBytes().DOUBLE_WIDE_MODE);
      if (width > 0 && width <= 6) {
         ret.appendCmd(width);
      } else {
         ret.appendCmd((byte)0);
      }

      ret.appendCmd(this.getEscBytes().DOUBLE_HIGH_MODE);
      if (height > 0 && height <= 6) {
         ret.appendCmd(height);
      } else {
         ret.appendCmd((byte)0);
      }

      return ret;
   }

   public PrintCmd createSetUserDefinedCharCmd(POSPrinterCmd.SetUserDefinedCharCmd succ) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createSetProportionalCharCmd(POSPrinterCmd.SetProportionalCharCmd spcc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createSetTabStopsCmd(POSPrinterCmd.SetTabStopsCmd stsc) throws HandleException {
      PrintCmd ret = this.factory.createCmd(stsc);
      ret.appendCmd(new byte[]{27, 87, 0, 27, 104, 0});
      ret.appendCmd(this.getEscBytes().TAB_SET);
      int count = stsc.getTabSize();
      int colsValue = 0;
      int roundCols = 0;

      for (int i = 0; i < count && stsc.getTabStops()[i] != 0; i++) {
         if (stsc.getTabStops()[i] == 1) {
            colsValue = stsc.getTabStops()[i] - 1;
         } else {
            colsValue = stsc.getTabStops()[i];
         }

         ret.appendCmd((byte)colsValue);
      }

      ret.appendCmd((byte)0);
      return ret;
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

   public PrintCmd createFontColorCmd(POSPrinterCmd.FontColorCmd fcc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createEnableColorModeCmd(POSPrinterCmd.EnableColorModeCmd ecm) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createAlterWideHighCmd(POSPrinterCmd.AlterWideHighCmd alterWideHighCmd) throws HandleException {
      int option = alterWideHighCmd.getOption();
      PrintCmd printerCmd = this.factory.createCmd(alterWideHighCmd);
      if (option == 4) {
         this.setWideHighMode(printerCmd, (byte)1);
      } else if (option == 2) {
         this.setWideMode(printerCmd, (byte)1);
      } else if (option == 3) {
         this.setHighMode(printerCmd, (byte)1);
      } else {
         this.setWideHighMode(printerCmd, (byte)0);
      }

      return printerCmd;
   }

   public PrintCmd createNormalModeCmd(POSPrinterCmd.NormalModeCmd normalModeCmd) throws HandleException {
      PrintCmd printerCmd = this.factory.createCmd(normalModeCmd);
      printerCmd.appendCmd(new byte[]{27, 70});
      printerCmd.appendCmd(new byte[]{27, 45, 0});
      printerCmd.appendCmd(new byte[]{27, 95, 0});
      printerCmd.appendCmd(new byte[]{27, 104, 0});
      printerCmd.appendCmd(new byte[]{27, 87, 0});
      return printerCmd;
   }

   public PrintCmd createAlignPositionCmd(POSPrinterCmd.AlignPositionCmd alignPositionCmd) throws HandleException {
      PrintCmd printerCmd = this.factory.createCmd(alignPositionCmd);
      printerCmd.appendCmd(this.getEscBytes().STATUS_REQUEST);
      ((DefaultPOSPrinterCmd)alignPositionCmd).setOutputCmd();
      alignPositionCmd.setStation((byte)2);
      return printerCmd;
   }

   private void setWideMode(PrintCmd prtCmd, byte option) {
      prtCmd.appendCmd(this.getEscBytes().DOUBLE_WIDE_MODE);
      prtCmd.appendCmd(option);
   }

   private void setHighMode(PrintCmd prtCmd, byte option) {
      prtCmd.appendCmd(this.getEscBytes().DOUBLE_HIGH_MODE);
      prtCmd.appendCmd(option);
   }

   private void setWideHighMode(PrintCmd prtCmd, byte option) {
      this.setHighMode(prtCmd, option);
      this.setWideMode(prtCmd, option);
   }
}
