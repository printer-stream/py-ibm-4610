package com.ibm.posj;

import com.ibm.posj.scale.ScaleHandleState;

public interface ScaleHandle extends Handle {
   int ENGLISH_MODE = 0;
   int METRIC_MODE = 1;
   int NOT_READY_FOR_WEIGH_ERROR_CODE = -1401;
   int SCALE_IN_MOTION_ERROR_CODE = -1402;
   int DATA_ERROR_ERROR_CODE = -1403;
   int READ_ERROR_ERROR_CODE = -1404;
   int DISPLAY_REQUIRED_ERROR_CODE = -1405;
   int HARDWARE_ERROR_ERROR_CODE = -1406;
   int UNDER_ZERO_ERROR_CODE = -1407;
   int OVER_CAPACITY_ERROR_CODE = -1408;
   int CENTER_OF_ZERO_ERROR_CODE = -1409;
   int ZEROING_REQUIRED_ERROR_CODE = -1410;
   int WARMUP_IN_PROGRESS_ERROR_CODE = -1411;
   int DUPLICATE_WEIGH_ERROR_CODE = -1412;

   ScaleCmd.Factory getScaleCmdFactory();

   ScaleHandleState getHandleState();
}
