package com.ibm.posj;

public interface CashDrawerHandle extends Handle {
   int CASHDRAWER_CLOSE_STATUS = 0;
   int CASHDRAWER_OPEN_STATUS = 1;
   int CASHDRAWER_ALARM_MODE_SET_STATUS = 4;
   int CD1 = 1;
   int CD2 = 2;

   CashDrawerCmd.Factory getCashDrawerCmdFactory();
}
