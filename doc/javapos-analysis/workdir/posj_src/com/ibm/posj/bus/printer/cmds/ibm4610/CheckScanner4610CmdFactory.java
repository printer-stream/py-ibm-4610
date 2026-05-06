package com.ibm.posj.bus.printer.cmds.ibm4610;

import com.ibm.jutil.Util;
import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.bus.printer.cmds.CheckScannerCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;

public class CheckScanner4610CmdFactory implements CheckScannerCmdFactory {
   PrintCmdFactory factory;

   public CheckScanner4610CmdFactory(PrintCmdFactory base) {
      this.factory = base;
   }

   private PrintCmd createCmd(IBM4610PrinterCmd cmd) {
      if (cmd != null && !cmd.isPrintCmdNull()) {
         PrintCmd ret = cmd.getPrintCmd();
         ret.setBytes(cmd.getOutboundPacket());
         return ret;
      } else {
         return this.factory.createCmd();
      }
   }

   private Cmd4610 getEscBytes() {
      return (Cmd4610)this.factory.getCmdBytes();
   }

   public PrintCmd createStartScanCmd(IBM4610PrinterCmd.StartScanCmd scan) {
      PrintCmd ret = this.createCmd(scan);
      ret.appendCmd(this.getEscBytes().START_SCAN);
      ret.appendCmd(scan.getMode());
      return ret;
   }

   public PrintCmd createPrintScannedImgCmd(IBM4610PrinterCmd.PrintScannedImgCmd printImg) {
      PrintCmd ret = this.createCmd(printImg);
      ret.appendCmd(this.getEscBytes().PRINT_SCANNED_IMG);
      ret.appendCmd(Util.toByteArray(printImg.getLocation()));
      ret.appendCmd(Util.toByteArray(printImg.getCorners()));
      ret.appendCmd(Util.toByteArray(printImg.getOffset()));
      ret.appendCmd(Util.toByteArray(printImg.getScale()));
      if (printImg.rotateImage()) {
         ret.appendCmd(new byte[]{1});
      } else {
         ret.appendCmd(new byte[]{0});
      }

      return ret;
   }

   public PrintCmd createScannerCalibCmd(IBM4610PrinterCmd.ScannerCalibCmd calibCmd) {
      PrintCmd ret = this.createCmd(calibCmd);
      ret.appendCmd(this.getEscBytes().SCANNER_CALIB);
      return ret;
   }

   public PrintCmd createSelCompressionFormatCmd(IBM4610PrinterCmd.SelCompressionFormatCmd selComFmtCmd) {
      PrintCmd ret = this.createCmd(selComFmtCmd);
      ret.appendCmd(this.getEscBytes().MCT_WRITE);
      ret.appendCmd((byte)31);
      ret.appendCmd(selComFmtCmd.getCompressionFormat());
      ret.appendCmd(selComFmtCmd.getContrast());
      return ret;
   }

   public PrintCmd createGetScannedImgCmd(IBM4610PrinterCmd.GetScannedImgCmd getScannedImg) {
      PrintCmd ret = this.createCmd(getScannedImg);
      ret.appendCmd(this.getEscBytes().RETRIEVE_IMAGE);
      ret.appendCmd(Util.toByteArray(getScannedImg.getLocation()));
      ret.appendCmd(Util.toByteArray(getScannedImg.getOffset()));
      ret.appendCmd(Util.toByteArray(getScannedImg.getNumBytes()));
      return ret;
   }

   public PrintCmd createGetNextImgLocCmd(IBM4610PrinterCmd.GetNextImgLocCmd nextImgLocCmd) {
      PrintCmd ret = this.createCmd(nextImgLocCmd);
      ret.appendCmd(this.getEscBytes().GET_NEXT_IMG_LOC);
      return ret;
   }

   public PrintCmd createGetFirstUnreadImgLocCmd(IBM4610PrinterCmd.GetFirstUnreadImgLocCmd firstUnreadImgLocCmd) {
      PrintCmd ret = this.createCmd(firstUnreadImgLocCmd);
      ret.appendCmd(this.getEscBytes().GET_FIRST_UNREAD_IMG_LOC);
      return ret;
   }

   public PrintCmd createStoreScannedImgCmd(IBM4610PrinterCmd.StoreScannedImgCmd storeCmd) {
      PrintCmd ret = this.createCmd(storeCmd);
      ret.appendCmd(this.getEscBytes().STORE_SCANNED_IMG);
      byte method = storeCmd.getStorageMethod();
      ret.appendCmd(new byte[]{storeCmd.getStorageMethod()});
      ret.appendCmd(Util.toByteArray(storeCmd.getCorner()));
      ret.appendCmd(Util.toByteArray(storeCmd.getOffset()));
      if (method != 0) {
         int[] xi_yi = storeCmd.getCornerSub();
         int[] dxi_dyi = storeCmd.getOffsetSub();

         for (int i = 0; i < 3; i++) {
            ret.appendCmd(Util.toByteArray(xi_yi[i]));
         }

         for (int j = 0; j < 3; j++) {
            ret.appendCmd(Util.toByteArray(dxi_dyi[j]));
         }
      }

      if (storeCmd.getTagData().length() > 40) {
         ret.appendCmd(storeCmd.getTagData().substring(0, 39).getBytes());
      } else {
         ret.appendCmd(storeCmd.getTagData().getBytes());
      }

      ret.appendCmd((byte)0);
      ret.appendCmd(this.getEscBytes().BUFFERED_EC);
      ret.setWaitToFinish(true);
      return ret;
   }
}
