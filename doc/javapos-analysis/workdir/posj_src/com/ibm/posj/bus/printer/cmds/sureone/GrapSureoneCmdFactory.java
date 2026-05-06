package com.ibm.posj.bus.printer.cmds.sureone;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.IBMSureonePrinterCmd;
import com.ibm.posj.bus.printer.cmds.GraphicCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;

public class GrapSureoneCmdFactory implements GraphicCmdFactory {
   PrintCmdFactory factory;
   private static byte[] EndPosition = new byte[]{0};

   public GrapSureoneCmdFactory(PrintCmdFactory factory) {
      this.factory = factory;
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

   private CmdSureone getEscBytes() {
      return (CmdSureone)this.factory.getCmdBytes();
   }

   public PrintCmd createNormalDensityCmd(IBMSureonePrinterCmd.NormalDensityCmd ndc) {
      PrintCmd ret = this.factory.createCmd(ndc);
      ret.appendCmd(this.getEscBytes().NORMAL_DENSITY_GRAPHS);
      ret.appendCmd(ndc.getDensity());
      ret.appendCmd(EndPosition);
      ret.appendCmd(ndc.getData());
      ret.appendCmd(this.getEscBytes().LINE_FEED);
      return ret;
   }

   public PrintCmd createHighDensityCmd(IBMSureonePrinterCmd.HighDensityCmd hdc) {
      PrintCmd ret = this.factory.createCmd(hdc);
      ret.appendCmd(this.getEscBytes().HIGH_DENSITY_GRAPHS);
      ret.appendCmd(hdc.getDensity1());
      ret.appendCmd(hdc.getDensity2());
      ret.appendCmd(hdc.getData());
      return ret;
   }

   public PrintCmd createFineDensityBitCmd(IBMSureonePrinterCmd.FineDensityBitCmd fdc) {
      PrintCmd ret = this.factory.createCmd(fdc);
      ret.appendCmd(this.getEscBytes().FINE_DENSITY_GRAPHS_1DOT);
      ret.appendCmd(fdc.getDensity());
      ret.appendCmd(EndPosition);
      ret.appendCmd(fdc.getData());
      return ret;
   }

   public PrintCmd createFineDensityCmd(IBMSureonePrinterCmd.FineDensityCmd fdc) {
      PrintCmd ret = this.factory.createCmd(fdc);
      ret.appendCmd(this.getEscBytes().FINE_DENSITY_GRAPHS_8DOTS);
      ret.appendCmd(EndPosition);
      ret.appendCmd(EndPosition);
      ret.appendCmd(fdc.getData());
      ret.appendCmd(this.getEscBytes().LINE_FEED);
      return ret;
   }

   public PrintCmd createEnableDownloadCharCmd(IBMSureonePrinterCmd.EnableDownloadCharCmd edcc) {
      PrintCmd ret = this.factory.createCmd(edcc);
      ret.appendCmd(this.getEscBytes().ENABLE_DOWNLOAD_CHAR);
      return ret;
   }

   public PrintCmd createDisableDownloadCharCmd(IBMSureonePrinterCmd.DisableDownloadCharCmd ddcc) {
      PrintCmd ret = this.factory.createCmd(ddcc);
      ret.appendCmd(this.getEscBytes().DISABLE_DOWNLOAD_CHAR);
      return ret;
   }

   public PrintCmd createDefineDownloadCharCmd(IBMSureonePrinterCmd.DefineDownloadCharCmd ddcc) {
      PrintCmd ret = this.factory.createCmd(ddcc);
      ret.appendCmd(this.getEscBytes().DOWNLOAD_FONT);
      ret.appendCmd(ddcc.getCodeC());
      ret.appendCmd(ddcc.getData());
      return ret;
   }

   public PrintCmd createDeleteDownloadCharCmd(IBMSureonePrinterCmd.DeleteDownloadCharCmd ddcc) {
      PrintCmd ret = this.factory.createCmd(ddcc);
      ret.appendCmd(this.getEscBytes().DELETE_DOWNLOAD_CHAR);
      ret.appendCmd(ddcc.getCodeC());
      return ret;
   }

   public PrintCmd createPrintBarCodeCmd(DefaultPOSPrinterCmd.PrintBarCodeCmd pbcc) {
      PrintCmd ret = this.factory.createCmd(pbcc);
      int factor = 0;
      int width = 0;
      switch (pbcc.getSymbol()) {
         case 0:
            if (pbcc.getWidth() <= 2) {
               width = 1;
            } else if (pbcc.getWidth() >= 3) {
               width = 3;
            }

            factor = (width + 1) * 4;
            if (pbcc.getAlignment() == 2) {
               ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
               ret.appendCmd((byte)21);
            }
            break;
         case 1:
            if (pbcc.getWidth() <= 2) {
               width = 1;
            } else if (pbcc.getWidth() >= 3) {
               width = 3;
            }

            factor = width * 3;
            if (pbcc.getAlignment() == 2) {
               ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
               ret.appendCmd((byte)16);
            }
            break;
         case 2:
            if (pbcc.getWidth() <= 2) {
               width = 1;
            } else if (pbcc.getWidth() >= 3) {
               width = 3;
            }

            factor = (width + 1) * 3;
            if (pbcc.getAlignment() == 2) {
               ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
               ret.appendCmd((byte)24);
            }
            break;
         case 3:
            if (pbcc.getWidth() <= 2) {
               width = 1;
            } else if (pbcc.getWidth() >= 3) {
               width = 3;
            }

            factor = width * 3;
            if (pbcc.getAlignment() == 2) {
               ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
               ret.appendCmd((byte)16);
            }
            break;
         case 4:
            if (pbcc.getWidth() <= 2) {
               width = 1;
            } else if (pbcc.getWidth() >= 3) {
               width = 3;
            }

            factor = width;
            width += 6;
            if (pbcc.getAlignment() == 2) {
               ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
               ret.appendCmd((byte)4);
            }
            break;
         case 5:
            if (pbcc.getWidth() <= 2) {
               width = 1;
            } else if (pbcc.getWidth() >= 3) {
               width = 3;
            }

            factor = (width + 1) * 2;
            width += 3;
            if (pbcc.getAlignment() == 2) {
               ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
               ret.appendCmd((byte)16);
            }
            break;
         case 6:
            if (pbcc.getWidth() <= 2) {
               width = 1;
            } else if (pbcc.getWidth() >= 3) {
               width = 3;
            }

            factor = (width + 1) * 3;
            if (pbcc.getAlignment() == 2) {
               ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
               ret.appendCmd((byte)20);
            }
            break;
         case 7:
            if (pbcc.getWidth() <= 2) {
               width = 1;
            } else if (pbcc.getWidth() >= 3) {
               width = 3;
            }

            factor = width * 2;
            if (pbcc.getAlignment() == 2) {
               ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
               ret.appendCmd((byte)11);
            }
            break;
         case 8:
            if (pbcc.getWidth() <= 2) {
               width = 1;
            } else if (pbcc.getWidth() >= 3) {
               width = 3;
            }

            factor = (width + 1) * 3;
            width += 6;
            if (pbcc.getAlignment() == 2) {
               ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
               ret.appendCmd((byte)21);
            }
      }

      if (pbcc.getAlignment() == 0) {
         ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
         ret.appendCmd((byte)0);
      } else if (pbcc.getAlignment() == 1) {
         ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
         ret.appendCmd((byte)factor);
      }

      ret.appendCmd(this.getEscBytes().SEL_BARCODE);
      ret.appendCmd((byte)pbcc.getSymbol());
      ret.appendCmd(pbcc.getTextPosition());
      ret.appendCmd((byte)width);
      ret.appendCmd(pbcc.getHeight());
      ret.appendCmd(pbcc.getData().getBytes());
      ret.appendCmd((byte)30);
      ret.appendCmd(this.getEscBytes().LINE_FEED);
      ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
      ret.appendCmd((byte)0);
      ret.appendCmd(this.getEscBytes().LINE_FEED);
      return ret;
   }

   public PrintCmd createPrintBitmapCmd(DefaultPOSPrinterCmd.PrintBitmapCmd ndc) {
      PrintCmd ret = this.factory.createCmd(ndc);
      byte factor = 0;
      int n1 = ndc.getHeight() & 255;
      int n2 = (ndc.getDensity() & 255) << 8;
      if (ndc.getAlignment() == -2) {
         ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
         factor = (byte)((560 - (n1 + n2)) / 24);
         ret.appendCmd(factor);
      } else if (ndc.getAlignment() == -3) {
         ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
         if (n1 + n2 > 250) {
            factor = (byte)((560 - (n1 + n2)) / 24 * 2);
         } else {
            factor = 24;
         }

         ret.appendCmd(factor);
      } else if (ndc.getAlignment() == -1) {
         ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
         ret.appendCmd((byte)0);
      }

      ret.appendCmd(this.getEscBytes().FINE_DENSITY_GRAPHS_8DOTS);
      ret.appendCmd((byte)(ndc.getHeight() & 255));
      ret.appendCmd((byte)(ndc.getDensity() & 255));
      ret.appendCmd(ndc.getBitmapStream());
      ret.appendCmd(this.getEscBytes().SET_LEFT_MARGIN);
      ret.appendCmd((byte)0);
      ret.appendCmd(this.getEscBytes().TIME_8MM_LINE_FEED);
      ret.appendCmd((byte)21);
      ndc.setOutputCmd();
      return ret;
   }

   public PrintCmd createPrintSetLogoCmd(DefaultPOSPrinterCmd.PrintSetLogoCmd pslc) throws HandleException {
      PrintCmd ret = this.createCmd(pslc);
      ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      return ret;
   }

   public PrintCmd createSetLogoCmd(DefaultPOSPrinterCmd.SetLogoCmd slc) throws HandleException {
      PrintCmd ret = this.createCmd(slc);
      ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      return ret;
   }

   public PrintCmd createSetBitmapCmd(DefaultPOSPrinterCmd.SetBitmapCmd sbc) throws HandleException {
      PrintCmd ret = this.createCmd(sbc);
      ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      return ret;
   }

   public PrintCmd createPrintSetBitmapCmd(DefaultPOSPrinterCmd.PrintSetBitmapCmd psbc) throws HandleException {
      PrintCmd ret = this.createCmd(psbc);
      ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      return ret;
   }
}
