package com.ibm.posj;

public interface CashDrawerCmd extends HandleCmd {
   int OPEN_DRAWER_CMD_CODE = 100;
   String OPEN_DRAWER_CMD_NAME = "OPEN_DRAWER_CMD";

   public interface Factory extends SystemCmd.Factory {
      CashDrawerCmd createOpenDrawerCmd();
   }
}
