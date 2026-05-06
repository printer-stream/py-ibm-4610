package com.ibm.posj.bus.printer.cmds;

import com.ibm.posj.DefaultPOSPrinterCmd;

public interface MICRCmdFactory {
   PrintCmd createEndRegisterSlipCmd(DefaultPOSPrinterCmd.EndRegisterSlipCmd var1);

   PrintCmd createReadSlipCmd(DefaultPOSPrinterCmd.ReadSlipCmd var1);

   PrintCmd createEjectSlipCmd(DefaultPOSPrinterCmd.EjectSlipCmd var1);

   PrintCmd createRegisterSlipCmd(DefaultPOSPrinterCmd.RegisterSlipCmd var1);
}
