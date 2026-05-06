package com.ibm.posj.bus;

import com.ibm.posj.HandleException;
import com.ibm.posj.HardTotalsCmd;

public interface HardTotalsCmdSubmitter {
   void submitGetHardTotalsInfCmd(HardTotalsCmd.GetHardTotalsInfCmd var1) throws HandleException;

   void submitReadHardTotalsDataCmd(HardTotalsCmd.ReadHardTotalsDataCmd var1) throws HandleException;

   void submitWriteHardTotalsDataCmd(HardTotalsCmd.WriteHardTotalsDataCmd var1) throws HandleException;
}
