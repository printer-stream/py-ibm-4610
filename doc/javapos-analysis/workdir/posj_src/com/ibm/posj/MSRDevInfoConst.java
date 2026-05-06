package com.ibm.posj;

public class MSRDevInfoConst {
   public static final int ERR_STATUS_BYTE_INDEX = 0;
   public static final int ERR_MASK = 21;
   public static final int ERR_MASK_TRK_1_JIS_II = 1;
   public static final int ERR_MASK_TRK2 = 4;
   public static final int ERR_MASK_TRK3 = 16;
   public static final int MSR_STATUS_LENGTH = 2;
   public static final int BYTE_LENGTH = 8;
   public static final int TRK1_MAX_LENGTH = 98;
   public static final int TRK2_MAX_LENGTH = 46;
   public static final int TRK3_MAX_LENGTH = 139;
   public static final int TRKJISII_MAX_LENGTH = 88;
   public static final int FIRST_STATUS_BYTE_INDEX = 0;
   public static final byte MSR_TRACK_JISII_BIT = 8;
   public static final byte MSR_TRACK_ISO3_BIT = 4;
   public static final byte MSR_TRACK_ISO2_BIT = 2;
   public static final byte MSR_TRACK_ISO1_BIT = 1;
   public static final int MSR_COMMAND_RESULT_BIT = 7;
   public static final int DEVICE_INFO_RESPONSE_BIT = 0;
   public static final byte[] MSR_TEST_REQ_CMD = new byte[]{0, 16};
   public static final byte[] MSR_STATUS_REQ_CMD = new byte[]{0, 32};
   public static final byte[] MSR_RESET_REQ_CMD = new byte[]{0, 64};
   public static final byte[] MSR_DEV_INFO_REQ_CMD = new byte[]{0, 0, 1};
   public static final byte[] WRITE_MSR_DATA_CMD = new byte[]{16, 0};
   public static final byte[] ENABLE_WRITE_MSR_DATA_CMD = new byte[]{1, -128, 0};
   public static final int DBCS_JISII_MAX_LENGTH = 69;
   public static final int DBCS_TRACK2_MAX_LENGTH = 37;
   public static final int MSR_THREE_TRACKS_MAX_LENGTH = 290;
   public static final int MSR_TWO_HEAD_MAX_LENGTH = 140;
   public static final int DBCS_MAX_LENGTH = 110;

   public boolean isISOMSRType(int type) {
      boolean isISO = false;
      if (type == 3300) {
         isISO = true;
      }

      return isISO;
   }

   public boolean isJUCCMSRType(int type) {
      boolean isJUCC = false;
      if (type == 3301) {
         isJUCC = true;
      }

      return isJUCC;
   }

   public boolean isWriteCapableMSRType(int type) {
      boolean isWriteCapable = false;
      if (type == 3302) {
         isWriteCapable = true;
      }

      return isWriteCapable;
   }
}
