package com.ibm.posj;

public interface MSRHandle extends Handle {
   int STATUS_LENGTH_NOT_VALID = -601;
   int TOO_MUCH_DATA_RECEIVED = -602;
   int TOO_LITTLE_DATA_RECEIVED = -603;
   int TIME_OUT_ERROR_CODE = -604;

   MSRCmd.Factory getMSRCmdFactory();

   void reset();
}
