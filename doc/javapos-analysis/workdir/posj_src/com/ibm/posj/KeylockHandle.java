package com.ibm.posj;

public interface KeylockHandle extends Handle {
   int KEYLOCK_POSITION_INACTIVE = 1;
   int KEYLOCK_POSITION_OPERATOR = 2;
   int KEYLOCK_POSITION_MANAGER = 3;
   int KEYLOCK_POSITION_SYSTEM = 4;
   int KEYLOCK_POSITION_REGISTRATION_PCMODE = 5;
   int KEYLOCK_POSITION_SETTLEMENT = 6;

   KeylockCmd.Factory getKeylockCmdFactory();
}
