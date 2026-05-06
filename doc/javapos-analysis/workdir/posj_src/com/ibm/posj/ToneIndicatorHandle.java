package com.ibm.posj;

public interface ToneIndicatorHandle extends Handle {
   int TONE_ACTIVE = 0;
   int TONE_INACTIVE = 1;

   ToneIndicatorCmd.Factory getToneIndicatorCmdFactory();
}
