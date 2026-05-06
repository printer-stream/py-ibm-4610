package com.ibm.posj.bus.printer.cmds.ibm4610;

import com.ibm.posj.IBM4610PrinterCmd;
import com.ibm.posj.bus.printer.cmds.GeneralCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;

public interface General4610CmdFactory extends GeneralCmdFactory {
   PrintCmd createBeeperCmd(IBM4610PrinterCmd.BeeperCmd var1);

   PrintCmd createEnableFeedButtonCmd(IBM4610PrinterCmd.EnableFeedButtonCmd var1);

   PrintCmd createChangePrintSideCmd(IBM4610PrinterCmd.ChangePrintSideCmd var1);

   PrintCmd createContinuationCmd(IBM4610PrinterCmd.ContinuationCmd var1);

   PrintCmd createHeadMovementCmd(IBM4610PrinterCmd.HeadMovementCmd var1);
}
