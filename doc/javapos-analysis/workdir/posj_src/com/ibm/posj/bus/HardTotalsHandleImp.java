package com.ibm.posj.bus;

import com.ibm.posj.HardTotalsCmd;
import com.ibm.posj.ram.RamDevice;

public interface HardTotalsHandleImp extends HandleImp {
   RamDevice getRamDevice();

   HardTotalsCmd.Factory getCmdFactory();
}
