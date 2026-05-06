package com.ibm.posj.bus.printer.cmds;

import com.ibm.posj.IBM4610PrinterCmd;

public interface CheckScannerCmdFactory {
   PrintCmd createStartScanCmd(IBM4610PrinterCmd.StartScanCmd var1);

   PrintCmd createPrintScannedImgCmd(IBM4610PrinterCmd.PrintScannedImgCmd var1);

   PrintCmd createStoreScannedImgCmd(IBM4610PrinterCmd.StoreScannedImgCmd var1);

   PrintCmd createGetScannedImgCmd(IBM4610PrinterCmd.GetScannedImgCmd var1);

   PrintCmd createScannerCalibCmd(IBM4610PrinterCmd.ScannerCalibCmd var1);

   PrintCmd createGetNextImgLocCmd(IBM4610PrinterCmd.GetNextImgLocCmd var1);

   PrintCmd createGetFirstUnreadImgLocCmd(IBM4610PrinterCmd.GetFirstUnreadImgLocCmd var1);

   PrintCmd createSelCompressionFormatCmd(IBM4610PrinterCmd.SelCompressionFormatCmd var1);
}
