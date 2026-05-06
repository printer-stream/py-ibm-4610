package com.ibm.posj.bus.printer.cmds.ibm4610;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.FontCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;

public class Font4610CmdFactory implements FontCmdFactory {
   PrintCmdFactory factory;
   private static final byte CMD_PARAM_OFF = 0;
   private static final byte CMD_PARAM_ON = 1;

   public Font4610CmdFactory(PrintCmdFactory f) {
      this.factory = f;
   }

   private Cmd4610 getEscBytes() {
      return (Cmd4610)this.factory.getCmdBytes();
   }

   private PrintCmd createCmd(DefaultPOSPrinterCmd cmd) {
      if (cmd != null && !cmd.isPrintCmdNull()) {
         PrintCmd ret = cmd.getPrintCmd();
         ret.setBytes(cmd.getOutboundPacket());
         return ret;
      } else {
         return this.factory.createCmd();
      }
   }

   public PrintCmd createRotatePrintCmd(POSPrinterCmd.RotatePrintCmd rpc) {
      PrintCmd ret = this.factory.createCmd(rpc);
      this.appendRotationData(ret, rpc.getStation(), rpc.getRotation());
      return ret;
   }

   public void appendRotationData(PrintCmd ret, byte station, int rotation) {
      switch (rotation) {
         case 90:
            if (2 == station) {
               ret.appendCmd(this.getEscBytes().append(this.getEscBytes().ROTATE_CHARS90, (byte)1));
            }

            if (4 == station) {
               ret.appendCmd(this.getEscBytes().DIL_COMM);
               ret.appendCmd(this.getEscBytes().DIL_SETTINGS);
            }
            break;
         case 180:
            ret.appendCmd(this.getEscBytes().append(this.getEscBytes().ROTATE_CHARS180, (byte)1));
            break;
         case 270:
            ret.appendCmd(this.getEscBytes().append(this.getEscBytes().ROTATE_CHARS90, (byte)1));
            ret.appendCmd(this.getEscBytes().append(this.getEscBytes().ROTATE_CHARS180, (byte)1));
            break;
         default:
            if (4 == station) {
               ret.appendCmd(this.getEscBytes().DIP_COMM);
               ret.appendCmd(this.getEscBytes().DIP_SETTINGS);
            } else {
               ret.appendCmd(this.getEscBytes().append(this.getEscBytes().ROTATE_CHARS90, (byte)0));
               ret.appendCmd(this.getEscBytes().append(this.getEscBytes().ROTATE_CHARS180, (byte)0));
            }
      }
   }

   public PrintCmd createSelectCodePageCmd(POSPrinterCmd.SelectCodePageCmd scpc) {
      PrintCmd ret = this.factory.createCmd(scpc);
      ret.appendCmd(this.getEscBytes().RESIDENT_CHAR_SET);
      ret.appendCmd(this.getEscBytes().SELECT_CODE_PAGE);
      ret.appendCmd(scpc.getSelectedCodePage());
      return ret;
   }

   public PrintCmd createLeftMarginCmd(POSPrinterCmd.LeftMarginCmd lmc) {
      return this.factory.createCmd(lmc);
   }

   public PrintCmd createTextAttribCmd(POSPrinterCmd.TextAttribCmd tac) {
      return this.factory.createCmd(tac);
   }

   public PrintCmd createSetTabStopsCmd(POSPrinterCmd.SetTabStopsCmd stsc) {
      PrintCmd ret = this.factory.createCmd(stsc);
      ret.appendCmd(this.getEscBytes().TAB_SET);
      int count = stsc.getTabSize();

      for (int i = 0; i < count && stsc.getTabStops()[i] != 0; i++) {
         ret.appendCmd((byte)(stsc.getTabStops()[i] >> 8));
         ret.appendCmd((byte)stsc.getTabStops()[i]);
      }

      ret.appendCmd((byte)0);
      ret.appendCmd((byte)0);
      return ret;
   }

   public PrintCmd createAlterWideHighCmd(POSPrinterCmd.AlterWideHighCmd alterWideHighCmd) {
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

   public PrintCmd createSetLeftMarginCmd(POSPrinterCmd.SetLeftMarginCmd slmc) {
      PrintCmd ret = this.factory.createCmd(slmc);
      ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN_POS);
      ret.appendCmd((byte)(slmc.getLeftMarginValue() >> 8));
      ret.appendCmd((byte)slmc.getLeftMarginValue());
      return ret;
   }

   public PrintCmd createAlignmentCmd(POSPrinterCmd.AlignmentCmd ac) {
      return this.factory.createCmd(ac);
   }

   public PrintCmd createSetProportionalCharCmd(POSPrinterCmd.SetProportionalCharCmd spcc) {
      return this.factory.createCmd(spcc);
   }

   public PrintCmd createRelativePositionCmd(POSPrinterCmd.RelativePositionCmd rpc) {
      return this.factory.createCmd(rpc);
   }

   public PrintCmd createScaleFontCmd(POSPrinterCmd.ScaleFontCmd scaleFontCmd) {
      PrintCmd ret = this.factory.createCmd(scaleFontCmd);
      if (!((Print4610CmdFactory)this.factory).isOlderModel()) {
         ret.appendCmd(this.getEscBytes().FONT_SCALE);
         ret.appendCmd((byte)((byte)(scaleFontCmd.getWidth() << 4 & 240) + scaleFontCmd.getHeight()));
      } else if (scaleFontCmd.getWidth() == 1 && scaleFontCmd.getHeight() == 1) {
         this.setWideHighMode(ret, (byte)1);
      } else {
         if (scaleFontCmd.getWidth() == 1) {
            this.setWideMode(ret, (byte)1);
         } else {
            this.setWideMode(ret, (byte)0);
         }

         if (scaleFontCmd.getHeight() == 1) {
            this.setHighMode(ret, (byte)1);
         } else {
            this.setHighMode(ret, (byte)0);
         }
      }

      return ret;
   }

   public PrintCmd createSetUserDefinedCharCmd(POSPrinterCmd.SetUserDefinedCharCmd succ) {
      PrintCmd ret = this.factory.createCmd(succ);
      ret.appendCmd(this.getEscBytes().DOWNLOAD_FONT);
      ret.appendCmd(succ.getCodePage());
      ret.appendCmd(succ.getStart());
      ret.appendCmd(succ.getEnd());
      ret.appendCmd(succ.getCharData());
      if (this.getEscBytes() instanceof Rs232Cmd4610) {
         ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      }

      ret.setWaitToStart(true);
      ret.setWaitToFinish(true);
      return ret;
   }

   public PrintCmd createLineSpacingCmd(POSPrinterCmd.LineSpacingCmd lsc) {
      PrintCmd ret = this.factory.createCmd(lsc);
      ret.appendCmd(this.getEscBytes().append(this.getEscBytes().LINE_SPACING, lsc.getDots()));
      return ret;
   }

   public PrintCmd createNormalModeCmd(POSPrinterCmd.NormalModeCmd nmc) {
      PrintCmd printerCmd = this.factory.createCmd(nmc);
      int start = printerCmd.getDataSize();
      if (nmc.isBoldOn()) {
         printerCmd.appendCmd(this.getEscBytes().EMPHASIZE_MODE);
         printerCmd.appendCmd((byte)0);
      }

      if (nmc.isUnderlineOn()) {
         printerCmd.appendCmd(this.getEscBytes().UNDERLINE_MODE);
         printerCmd.appendCmd((byte)0);
      }

      if (nmc.isReverseVideoOn()) {
         printerCmd.appendCmd(this.getEscBytes().INVERT_MODE);
         printerCmd.appendCmd((byte)0);
      }

      if (nmc.isScaleOn()) {
         if (!((Print4610CmdFactory)this.factory).isOlderModel()) {
            printerCmd.appendCmd(this.getEscBytes().FONT_SCALE);
            printerCmd.appendCmd((byte)0);
         } else {
            this.setWideHighMode(printerCmd, (byte)0);
         }
      }

      if (nmc.isDoubleWideHighOn()) {
         this.setWideHighMode(printerCmd, (byte)0);
      }

      if (nmc.isAlignmentOn()) {
         printerCmd.appendCmd(this.getEscBytes().SET_ALIGNMENT);
         printerCmd.appendCmd((byte)0);
      }

      if (nmc.isColorOn()) {
         printerCmd.appendCmd(this.getEscBytes().FONT_COLOR);
         printerCmd.appendCmd((byte)0);
      }

      if (start == printerCmd.getDataSize()) {
         printerCmd.appendCmd(this.getEscBytes().UNDERLINE_MODE);
         printerCmd.appendCmd((byte)0);
      }

      return printerCmd;
   }

   public PrintCmd createBoldCmd(POSPrinterCmd.BoldCmd boldCmd) {
      byte active = (byte)(boldCmd.isActive() ? 1 : 0);
      PrintCmd printerCmd = this.factory.createCmd(boldCmd);
      printerCmd.appendCmd(this.getEscBytes().EMPHASIZE_MODE);
      printerCmd.appendCmd(active);
      return printerCmd;
   }

   public PrintCmd createReverseVideoCmd(POSPrinterCmd.ReverseVideoCmd reverseVideoCmd) {
      byte active = (byte)(reverseVideoCmd.isActive() ? 1 : 0);
      PrintCmd printerCmd = this.factory.createCmd(reverseVideoCmd);
      printerCmd.appendCmd(this.getEscBytes().INVERT_MODE);
      printerCmd.appendCmd(active);
      return printerCmd;
   }

   public PrintCmd createUnderlineCmd(POSPrinterCmd.UnderlineCmd underlineCmd) {
      byte thickness = (byte)(underlineCmd.getThickness() == 0 ? 0 : 1);
      PrintCmd printerCmd = this.factory.createCmd(underlineCmd);
      printerCmd.appendCmd(this.getEscBytes().UNDERLINE_MODE);
      printerCmd.appendCmd(thickness);
      return printerCmd;
   }

   public PrintCmd createUseUserDefinedCodePageCmd(IBM4610PrinterCmd.UseUserDefinedCodePageCmd uuc) {
      PrintCmd ret = this.factory.createCmd(uuc);
      ret.appendCmd(this.getEscBytes().SELECT_USER_CHARSET);
      ret.appendCmd(uuc.getValue());
      return ret;
   }

   public PrintCmd createSelectCharSetCmd(IBM4610PrinterCmd.SelectCharSetCmd scsc) {
      PrintCmd ret = this.factory.createCmd(scsc);
      if (scsc.getSelectedCharSet() == 0) {
         ret.appendCmd(this.getEscBytes().RESIDENT_CHAR_SET);
      } else {
         ret.appendCmd(this.getEscBytes().USER_CHAR_SET);
      }

      return ret;
   }

   public PrintCmd createPrintQualityCmd(POSPrinterCmd.PrintQualityCmd pqc) {
      PrintCmd ret = this.factory.createCmd(pqc);
      byte quat = (byte)(pqc.getQuality() ? 1 : 0);
      if (pqc.getStation() == 2) {
         ret.appendCmd(this.getEscBytes().append(this.getEscBytes().PRINT_QUALITY, quat));
      } else {
         ret.appendCmd(this.getEscBytes().append(this.getEscBytes().UNI_DIRECTIONAL, quat));
      }

      return ret;
   }

   public PrintCmd createFontTypeCmd(POSPrinterCmd.FontTypeCmd ftc) {
      PrintCmd ret = this.factory.createCmd(ftc);
      if (ftc.getStation() != 4 && ftc.getStation() != 20) {
         switch (ftc.getFontType()) {
            case 0:
            default:
               ret.appendCmd(this.getEscBytes().append(this.getEscBytes().SELECT_FONT_FACE, (byte)0));
               break;
            case 1:
               ret.appendCmd(this.getEscBytes().append(this.getEscBytes().SELECT_FONT_FACE, (byte)1));
               break;
            case 2:
               ret.appendCmd(this.getEscBytes().append(this.getEscBytes().SELECT_FONT_FACE, (byte)2));
         }

         return ret;
      } else {
         return this.createSlipFontTypeCmd(ftc, ret);
      }
   }

   protected PrintCmd createSlipFontTypeCmd(POSPrinterCmd.FontTypeCmd ftc, PrintCmd ret) {
      switch (ftc.getFontType()) {
         case 0:
         default:
            ret.appendCmd(this.getEscBytes().append(this.getEscBytes().SELECT_FONT_FACE, (byte)0));
            break;
         case 1:
            ret.appendCmd(this.getEscBytes().append(this.getEscBytes().SELECT_FONT_FACE, (byte)1));
      }

      return ret;
   }

   public PrintCmd createDotSpacingCmd(POSPrinterCmd.DotSpacingCmd dsc) {
      PrintCmd ret = this.factory.createCmd(dsc);
      int space = dsc.getDotSpacing();
      int MAX = 8;
      int MIN = 0;
      if (dsc.isDBCS()) {
         MAX = 32;
         MIN = 0;
      }

      if (space > MAX) {
         space = MAX;
      } else if (space < MIN) {
         space = MIN;
      }

      if (!dsc.isDBCS()) {
         ret.appendCmd(this.getEscBytes().append(this.getEscBytes().SET_DOT_SPACING, (byte)space));
      } else {
         ret.appendCmd(this.getEscBytes().append(this.getEscBytes().SET_DBCS_DOT_SPACING, (byte)space));
      }

      return ret;
   }

   public PrintCmd createFontColorCmd(POSPrinterCmd.FontColorCmd fontColorCmd) {
      PrintCmd printerCmd = this.factory.createCmd(fontColorCmd);
      printerCmd.appendCmd(this.getEscBytes().FONT_COLOR);
      printerCmd.appendCmd(fontColorCmd.getFontColor());
      return printerCmd;
   }

   public PrintCmd createDBCSDotSpacingCmd(POSPrinterCmd.DotSpacingCmd dsc, PrintCmd ret) {
      int space = dsc.getDotSpacing();
      if (space > 32) {
         space = 32;
      } else if (space < 0) {
         space = 0;
      }

      ret.appendCmd(this.getEscBytes().append(this.getEscBytes().SET_DBCS_DOT_SPACING, (byte)space));
      return ret;
   }

   public PrintCmd createDownloadFontCmd(IBM4610PrinterCmd.DownloadFontCmd dfc) {
      PrintCmd ret = this.factory.createCmd(dfc);
      ret.appendCmd(dfc.getData());
      if (this.getEscBytes() instanceof Rs232Cmd4610) {
         ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      }

      ret.setWaitToStart(true);
      ret.setWaitToFinish(true);
      return ret;
   }

   public PrintCmd createAlignPositionCmd(POSPrinterCmd.AlignPositionCmd alignPositionCmd) {
      PrintCmd printerCmd = this.factory.createCmd(alignPositionCmd);
      printerCmd.appendCmd(this.getEscBytes().SET_ALIGNMENT);
      printerCmd.appendCmd(alignPositionCmd.getOption());
      return printerCmd;
   }

   public PrintCmd createDownloadDBCSFontCmd(IBM4610PrinterCmd.DownloadDBCSFontCmd ddfc) {
      PrintCmd ret = this.factory.createCmd(ddfc);
      short numOfCharacters = ddfc.getNumOfChars();
      ret.appendCmd(this.getEscBytes().DOWNLOAD_DBCS_FONT);
      if (2 == ddfc.getStation()) {
         ret.appendCmd((byte)0);
      } else {
         ret.appendCmd((byte)1);
      }

      ret.appendCmd((byte)numOfCharacters);
      ret.appendCmd(ddfc.getData());
      if (this.getEscBytes() instanceof Rs232Cmd4610) {
         ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      }

      ret.setWaitToFinish(true);
      return ret;
   }

   public PrintCmd createSetPrintStationSettingCmd(IBM4610PrinterCmd.SetPrintStationSettingCmd spssc) {
      PrintCmd ret = this.factory.createCmd(spssc);
      ret.appendCmd(this.getEscBytes().PRINT_STATION_SETTING);
      ret.appendCmd(spssc.getStation());
      return ret;
   }

   public PrintCmd createEnableColorModeCmd(POSPrinterCmd.EnableColorModeCmd ecm) {
      PrintCmd printerCmd = this.factory.createCmd(ecm);
      byte active = (byte)(ecm.isActive() ? 1 : 0);
      printerCmd.appendCmd(this.getEscBytes().FONT_COLOR_MODE);
      printerCmd.appendCmd(active);
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
