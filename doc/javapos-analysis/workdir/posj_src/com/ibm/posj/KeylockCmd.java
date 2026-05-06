package com.ibm.posj;

public interface KeylockCmd extends HandleCmd {
   String GET_KEY_POSITION_CMD_NAME = "GET_KEY_POSITION";
   int GET_KEY_POSITION_CMD_CODE = 300;

   public interface Factory extends HandleCmd.Factory {
      KeylockCmd createGetKeyPositionCmd();
   }

   public interface GetKeyPositionCmd extends KeylockCmd {
   }
}
