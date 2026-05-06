package com.ibm.posj.printer;

import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.POSPrinterCmd;

public interface CmdAdjuster {
   POSPrinterCmd adjustPOSPrinterCmd(DefaultPOSPrinterCmd var1);
}
