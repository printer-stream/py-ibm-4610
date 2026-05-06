package com.ibm.posj.bus.printer.cmds.ibm4689;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.IBM4689PrinterCmd;
import com.ibm.posj.bus.printer.cmds.GraphicCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;

public class Grap4689CmdFactory implements GraphicCmdFactory {
   PrintCmdFactory factory;

   public Grap4689CmdFactory(PrintCmdFactory factory) {
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

   private Cmd4689 getEscBytes() {
      return (Cmd4689)this.factory.getCmdBytes();
   }

   public PrintCmd createLoadUDCBufferCmd(IBM4689PrinterCmd.LoadUDCBufferCmd ludcbc) {
      PrintCmd ret = this.factory.createCmd(ludcbc);
      ret.appendCmd(this.getEscBytes().LOAD_UDC);
      ret.appendCmd(ludcbc.getBlock());
      byte id = this.factory.generateID();
      ludcbc.setID(id);
      ret.appendCmd(id);
      ret.appendCmd(ludcbc.getData());
      ret.setWaitToFinish(true);
      return ret;
   }

   public PrintCmd createPrintFullCutCmd(IBM4689PrinterCmd.PrintFullCutCmd pfc) {
      PrintCmd ret = this.factory.createCmd(pfc);
      ret.appendCmd(this.getEscBytes().FULL_CUT_LOGO_PRINT);
      byte id = this.factory.generateID();
      pfc.setID(id);
      ret.appendCmd(id);
      return ret;
   }

   public PrintCmd createSelectHSizeCmd(IBM4689PrinterCmd.SelectHSizeCmd shsc) {
      PrintCmd ret = this.factory.createCmd(shsc);
      ret.appendCmd(this.getEscBytes().BARCODE_HORIZ);
      ret.appendCmd(shsc.getSize());
      return ret;
   }

   public PrintCmd createSelectVSizeCmd(IBM4689PrinterCmd.SelectVSizeCmd svsc) {
      PrintCmd ret = this.factory.createCmd(svsc);
      ret.appendCmd(this.getEscBytes().BARCODE_VERT);
      int value = svsc.getSize() & 255;
      byte size = 1;
      size = (byte)Math.abs(value / 27);
      if (size >= 15) {
         size = 15;
      }

      if (size <= 0) {
         size = 1;
      }

      ret.appendCmd(size);
      return ret;
   }

   public PrintCmd createSelectHRIPositionCmd(IBM4689PrinterCmd.SelectHRIPositionCmd shripc) {
      PrintCmd ret = this.factory.createCmd(shripc);
      ret.appendCmd(this.getEscBytes().BARCODE_HRI_POSI);
      ret.appendCmd(shripc.getPos());
      return ret;
   }

   public PrintCmd createPrintBarCodeCmd(DefaultPOSPrinterCmd.PrintBarCodeCmd pbcc) {
      PrintCmd outCmd = this.createCmd(pbcc);
      outCmd.appendCmd(this.getEscBytes().BARCODE_PRINTBAR);
      outCmd.appendCmd((byte)pbcc.getSymbol());
      outCmd.appendCmd(pbcc.getData().getBytes());
      outCmd.appendCmd((byte)0);
      return outCmd;
   }

   public PrintCmd createSetLogoCmd(DefaultPOSPrinterCmd.SetLogoCmd slc) {
      PrintCmd ret = this.createCmd(slc);
      ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      byte id = this.factory.generateID();
      slc.setID(id);
      ret.appendCmd(id);
      return ret;
   }

   public PrintCmd createPrintSetLogoCmd(DefaultPOSPrinterCmd.PrintSetLogoCmd pslc) throws HandleException {
      PrintCmd ret = this.createCmd(pslc);
      ret.appendCmd(this.getEscBytes().STATUS_REQUEST);
      byte id = this.factory.generateID();
      pslc.setID(id);
      ret.appendCmd(id);
      return ret;
   }

   public PrintCmd createPrintBitmapCmd(DefaultPOSPrinterCmd.PrintBitmapCmd pbc) throws HandleException {
      PrintCmd ret = this.factory.createCmd(pbc);
      ret.appendCmd(this.getEscBytes().BITMAP_HD);
      byte id = this.factory.generateID();
      pbc.setID(id);
      ret.appendCmd(id);
      ret.appendCmd(this.getEscBytes().MULTI_LINE_MODE);
      ret.appendCmd(this.getEscBytes().CHAR_ATTRB_NORMAL);
      ret.appendCmd(this.getEscBytes().LINE_ATTRB_NORMAL);
      ret.appendCmd(this.getEscBytes().IMAGE_CHAR);
      ret.appendCmd(pbc.getWidth());
      ret.appendCmd(pbc.getHeight());
      ret.appendCmd(pbc.getBitmapStream());
      ret.appendCmd(this.getEscBytes().END_OF_DATA);
      return ret;
   }

   public PrintCmd createSetBitmapCmd(DefaultPOSPrinterCmd.SetBitmapCmd sbc) throws HandleException {
      throw new HandleException("Command not Supported");
   }

   public PrintCmd createPrintSetBitmapCmd(DefaultPOSPrinterCmd.PrintSetBitmapCmd psbc) throws HandleException {
      throw new HandleException("Command not Supported");
   }
}
