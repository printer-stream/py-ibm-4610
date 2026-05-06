package com.ibm.posj.bus.hid;

public interface HidPOSKeyboardConst {
   byte[] POSKEYBOARD_TEST_REQUEST_CMD = new byte[]{0, 16};
   byte[] POSKEYBOARD_STATUS_REQUEST_CMD = new byte[]{0, 32};
   byte[] POSKEYBOARD_RESET_REQUEST_CMD = new byte[]{0, 64};
   byte[] POSKEYBOARD_DEV_INFO_REQUEST_CMD = new byte[]{0, 0, 1};
   byte[] POSKEYBOARD_CONFIG_REQUEST_CMD = new byte[]{33, 0, 0, 0};
   byte[] POSKEYBOARD_INDICATOR_REQUEST_CMD = new byte[]{28, 0};
   byte COMMON_OFFLINE_POSITION = 1;
   byte COMMON_SCROLL_LOCK_POSITION = 4;
   byte COMMON_NUM_LOCK_POSITION = 5;
   byte COMMON_CAPS_LOCK_POSITION = 6;
   byte LEGACY_SBCS_WAIT_POSITION = 0;
   byte LEGACY_SBCS_MSG_PEND_POSITION = 2;
   byte LEGACY_SBCS_BLANK_POSITION = 3;
   byte LEGACY_DBCS_WAIT_POSITION = 2;
   byte LEGACY_DBCS_SYS_MSG_POSITION = 0;
   byte LEGACY_DBCS_READY_POSITION = 3;
   byte ADMINISTRATIVE_WAIT_POSITION = 0;
   byte ADMINISTRATIVE_SBCS_BLANK_POSITION = 3;
   byte ADMINISTRATIVE_SBCS_MSG_PEND_POSITION = 2;
   byte ADMINISTRATIVE_DBCS_SYS_MSG_POSITION = 2;
   byte ADMINISTRATIVE_DBCS_READY_POSITION = 3;
   byte[][] TYPEMATIC_DELAY_VALUES = new byte[][]{{0, 0}, {1, 32}, {2, 64}, {3, 96}};
   byte[][] TYPEMATIC_RATE_VALUES = new byte[][]{
      {0, 31},
      {1, 30},
      {2, 29},
      {3, 28},
      {4, 27},
      {5, 26},
      {6, 25},
      {7, 24},
      {8, 23},
      {9, 22},
      {10, 21},
      {11, 20},
      {12, 19},
      {13, 18},
      {14, 17},
      {15, 16},
      {16, 15},
      {17, 14},
      {18, 13},
      {19, 12},
      {20, 11},
      {21, 10},
      {22, 9},
      {23, 8},
      {24, 7},
      {25, 6},
      {26, 5},
      {27, 4},
      {28, 3},
      {29, 2},
      {30, 1},
      {31, 0}
   };
   byte[][] FAT_FINGER_VALUES = new byte[][]{{0, 0}, {1, 1}, {2, 2}, {3, 3}, {4, 4}};
}
