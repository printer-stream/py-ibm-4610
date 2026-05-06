package com.ibm.posj.bus.printer.cmds.ibm4610;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.bus.printer.cmds.GraphicCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;
import com.ibm.posj.util.DevBuses;

public class Grap4610CmdFactory implements GraphicCmdFactory {
   PrintCmdFactory factory;
   private static final int BYTE_SIZE_FACTOR = 8;

   public Grap4610CmdFactory(PrintCmdFactory factory) {
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

   private Cmd4610 getEscBytes() {
      return (Cmd4610)this.factory.getCmdBytes();
   }

   public PrintCmd createPrintBarCodeCmd(DefaultPOSPrinterCmd.PrintBarCodeCmd pbcc) {
      PrintCmd outCmd = this.createCmd(pbcc);
      outCmd.appendCmd(this.getEscBytes().SET_ALIGNMENT);
      outCmd.appendCmd(pbcc.getAlignment());
      if (0 != pbcc.getRotation()) {
         ((Font4610CmdFactory)this.factory.getFontFactory()).appendRotationData(outCmd, pbcc.getStation(), pbcc.getRotation());
      }

      if (201 == pbcc.getSymbol()) {
         outCmd = this.handlePDF417(pbcc, outCmd);
      } else {
         outCmd.appendCmd(this.getEscBytes().BARCODE_HRI_POSI);
         outCmd.appendCmd(pbcc.getTextPosition());
         outCmd.appendCmd(this.getEscBytes().BARCODE_VERT);
         outCmd.appendCmd(pbcc.getHeight());
         outCmd.appendCmd(this.getEscBytes().BARCODE_HORIZ);
         outCmd.appendCmd(pbcc.getWidth());
         outCmd.appendCmd(this.getEscBytes().BARCODE_PRINTBAR);
         outCmd.appendCmd((byte)pbcc.getSymbol());
      }

      if (9 == pbcc.getSymbol()) {
         outCmd.appendCmd((byte)pbcc.getData().getBytes().length);
      }

      outCmd.appendCmd(pbcc.getData().getBytes());
      if (9 != pbcc.getSymbol()) {
         outCmd.appendCmd((byte)0);
      }

      if (0 != pbcc.getRotation()) {
         ((Font4610CmdFactory)this.factory.getFontFactory()).appendRotationData(outCmd, pbcc.getStation(), 0);
      }

      return outCmd;
   }

   protected PrintCmd handlePDF417(DefaultPOSPrinterCmd.PrintBarCodeCmd c, PrintCmd pcmd) {
      pcmd.appendCmd(this.getEscBytes().PRINT_PDF417);
      return pcmd;
   }

   public PrintCmd createPrintBitmapCmd(DefaultPOSPrinterCmd.PrintBitmapCmd pbc) {
      PrintCmd outCmd = this.createCmd(pbc);
      byte width = pbc.getWidth();
      byte height = pbc.getHeight();
      int blockSize = width * height * 8;
      outCmd.setStation(pbc.getStation());
      outCmd.appendCmd(this.getEscBytes().SET_ALIGNMENT);
      outCmd.appendCmd(pbc.getAlignment());
      outCmd.appendCmd(this.getEscBytes().PRINT_LOGOS);
      outCmd.appendCmd(pbc.getDensity());
      outCmd.appendCmd(width);
      outCmd.appendCmd(height);
      outCmd.appendCmd(pbc.getBitmapStream(), pbc.getBlockIndex(), blockSize);
      if ((pbc.getStation() & 4) > 0 && DevBuses.RS232_DEVBUS == this.getEscBytes().getBusType()) {
         outCmd.setWaitToFinish(true);
      }

      return outCmd;
   }

   public PrintCmd createSetBitmapCmd(DefaultPOSPrinterCmd.SetBitmapCmd sbc) {
      byte n1 = sbc.getWidth();
      byte n2 = sbc.getHeight();
      PrintCmd outCmd = this.createCmd(sbc);
      outCmd.appendCmd(this.getEscBytes().SET_BITMAP);
      outCmd.appendCmd(sbc.getBitmapNo());
      outCmd.appendCmd(n1);
      outCmd.appendCmd(n2);
      outCmd.appendCmd(sbc.getBitmapStream());
      if (this.getEscBytes() instanceof Rs232Cmd4610) {
         outCmd.appendCmd(this.getEscBytes().STATUS_REQUEST);
      }

      outCmd.setWaitToStart(true);
      return outCmd;
   }

   public PrintCmd createPrintSetLogoCmd(DefaultPOSPrinterCmd.PrintSetLogoCmd pslc) {
      PrintCmd ret = this.createCmd(pslc);
      ret.appendCmd(this.getEscBytes().PRINT_SET_LOGO);
      ret.appendCmd(pslc.getLocation());
      ret.appendCmd(this.getEscBytes().BUFFERED_EC);
      return ret;
   }

   public PrintCmd createSetLogoCmd(DefaultPOSPrinterCmd.SetLogoCmd slc) {
      PrintCmd ret = this.createCmd(slc);
      ret.appendCmd(this.getEscBytes().SET_LOGO);
      if (100 != slc.getLocation()) {
         if (DevBuses.RS232_DEVBUS == this.getEscBytes().getBusType()) {
            ret.setAutoLoad(true, true);
         }

         ret.appendCmd(slc.getLocation());
      } else {
         ret.setWaitToFinish(true);
         if (DevBuses.RS232_DEVBUS == this.getEscBytes().getBusType()) {
            ret.setAutoLoad(false, true);
            ret.appendCmd(this.getEscBytes().MCT_READ);
            ret.appendCmd((byte)2);
         }
      }

      return ret;
   }

   public PrintCmd createPrintSetBitmapCmd(DefaultPOSPrinterCmd.PrintSetBitmapCmd psbc) {
      byte bmpAlignment = psbc.getAlignment();
      int bmpMargin = psbc.getMargin();
      PrintCmd outCmd = this.createCmd(psbc);
      if (bmpMargin > 0) {
         outCmd.appendCmd(this.getEscBytes().SET_LEFT_MARGIN_POS);
         outCmd.appendCmd((byte)(bmpMargin >> 8));
         outCmd.appendCmd((byte)bmpMargin);
      } else if (bmpAlignment != 0) {
         outCmd.appendCmd(this.getEscBytes().SET_ALIGNMENT);
         outCmd.appendCmd(bmpAlignment);
      }

      outCmd.appendCmd(this.getEscBytes().PRINT_SET_BITMAP);
      outCmd.appendCmd(psbc.getDensity());
      outCmd.appendCmd(psbc.getBitmapNo());
      if (bmpMargin > 0) {
         outCmd.appendCmd(this.getEscBytes().SET_LEFT_MARGIN_POS);
         outCmd.appendCmd((byte)0);
         outCmd.appendCmd((byte)0);
      } else if (bmpAlignment != 0) {
         outCmd.appendCmd(this.getEscBytes().SET_ALIGNMENT);
         outCmd.appendCmd((byte)0);
      }

      return outCmd;
   }

   public PrintCmd createPageModeCmd(IBM4610PrinterCmd.PageModeCmd pmc) {
      PrintCmd ret = this.createCmd(pmc);
      if (pmc.enabled()) {
         ret.appendCmd(this.getEscBytes().ENABLE_PAGEMODE);
      } else {
         ret.appendCmd(this.getEscBytes().DISABLE_PAGEMODE);
      }

      return ret;
   }

   public PrintCmd createPageModeNormalCmd(IBM4610PrinterCmd.PMPageModeNormalCmd ppmc) {
      PrintCmd ret = this.createCmd(ppmc);
      ret.appendCmd(this.getEscBytes().PAGEMODE_NORMODE);
      return ret;
   }

   public PrintCmd createPMPositioningCmd(IBM4610PrinterCmd.PMPositioningCmd pmc) {
      PrintCmd ret = this.createCmd(pmc);
      ret.appendCmd(this.getEscBytes().PAGEMODE_POSITION);
      ret.appendCmd(pmc.getPosition());
      return ret;
   }

   public PrintCmd createPrintPageModePageCmd(IBM4610PrinterCmd.PrintPageModePageCmd ppc) {
      PrintCmd ret = this.createCmd(ppc);
      if (ppc.getEnable()) {
         ret.appendCmd(this.getEscBytes().ENABLE_PAGEMODE);
      }

      ret.appendCmd(this.getEscBytes().PAGEMODE_PRINT);
      if (ppc.getClear()) {
         ret.appendCmd(this.getEscBytes().PAGEMODE_CLEAR);
      }

      if (ppc.getEnable()) {
         ret.appendCmd(this.getEscBytes().DISABLE_PAGEMODE);
      }

      return ret;
   }

   public PrintCmd createPMPageDefineCmd(IBM4610PrinterCmd.PMPageDefineCmd pdc) {
      PrintCmd ret = this.createCmd(pdc);
      ret.appendCmd(this.getEscBytes().PAGEMODE_DEFINE);
      int num = pdc.getX();
      byte[] x = new byte[]{0, 0};
      this.intSplit(pdc.getX(), x);
      ret.appendCmd(x[0]);
      ret.appendCmd(x[1]);
      this.intSplit(pdc.getY(), x);
      ret.appendCmd(x[0]);
      ret.appendCmd(x[1]);
      this.intSplit(pdc.getDX(), x);
      ret.appendCmd(x[0]);
      ret.appendCmd(x[1]);
      this.intSplit(pdc.getDY(), x);
      ret.appendCmd(x[0]);
      ret.appendCmd(x[1]);
      return ret;
   }

   public PrintCmd createHorizontalPositionCmd(IBM4610PrinterCmd.HorizontalPositionCmd hpc) {
      PrintCmd ret = this.createCmd(hpc);
      ret.appendCmd(this.getEscBytes().HORIZONTAL_POSITION);
      byte[] split = new byte[]{0, 0};
      this.intSplit(hpc.getHorizontalPosition(), split);
      ret.appendCmd(split[0]);
      ret.appendCmd(split[1]);
      return ret;
   }

   public PrintCmd createVerticalPositionCmd(IBM4610PrinterCmd.VerticalPositionCmd vpc) {
      PrintCmd ret = this.createCmd(vpc);
      ret.appendCmd(this.getEscBytes().VERTICAL_POSITION);
      byte[] split = new byte[]{0, 0};
      this.intSplit(vpc.getVerticalPosition(), split);
      ret.appendCmd(split[0]);
      ret.appendCmd(split[1]);
      return ret;
   }

   public PrintCmd createClearPageModeCmd(IBM4610PrinterCmd.ClearPageModeCmd cpmc) {
      PrintCmd ret = this.createCmd(cpmc);
      ret.appendCmd(this.getEscBytes().PAGEMODE_CLEAR);
      return ret;
   }

   public void intSplit(int num, byte[] split) {
      if (split.length >= 2) {
         split[0] = (byte)(num >> 8);
         split[1] = (byte)(num & 0xFF);
      }
   }
}
