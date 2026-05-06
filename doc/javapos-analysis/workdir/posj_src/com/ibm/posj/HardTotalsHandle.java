package com.ibm.posj;

import com.ibm.posj.ram.RamDevice;

public interface HardTotalsHandle extends Handle {
   int DATA_CHANGED = 1;

   HardTotalsCmd.Factory getCmdFactory();

   RamDevice getRamDevice();
}
