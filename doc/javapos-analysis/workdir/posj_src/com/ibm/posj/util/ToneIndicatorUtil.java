package com.ibm.posj.util;

public class ToneIndicatorUtil {
   public static final int RS485_DEV_ID_POSITION = 5;
   public static final int RS485_FEATURES_BYTE = 6;
   public static final int USB_ADMIN_DEV_ID_POSITION = 4;
   public static final int USB_ADMIN_FEATURES_POSITION = 5;
   public static final int USB_LEGACY_DEV_ID_POSITION = 5;
   public static final int USB_LEGACY_FEATURES_POSITION = 6;

   public static int getRs485ToneIndicatorID(byte[] response) {
      if (response.length <= 5) {
         return 4401;
      } else {
         switch (response[5]) {
            case 1:
               if (response[6] == 17) {
                  return 4444;
               }
            case 2:
               return 4434;
            case 3:
               return 4436;
            case 4:
               return 4435;
            case 5:
               return 4438;
            case 6:
               if (response[6] == 0) {
                  return 4441;
               }

               return 4439;
            case 7:
               return 4442;
            case 8:
            case 9:
            case 10:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            default:
               return 4401;
            case 11:
               return 4443;
            case 12:
               return 4437;
            case 28:
               return 4440;
         }
      }
   }

   public static int getPs2ToneIndicatorID(short response) {
      int value = 4401;
      if (response == 1) {
         value = 4429;
      } else if (response == 3) {
         value = 4430;
      } else if (response == 4) {
         value = 4431;
      } else if (response == 2) {
         value = 4432;
      }

      return value;
   }

   public static int getUsbBootToneIndicatorID(byte[] response) {
      if (response.length <= 5) {
         return 4401;
      } else {
         byte devId = response[4];
         byte features = response[5];
         switch (devId) {
            case 4:
               return 4404;
            case 5:
               return 4405;
            default:
               return 4401;
         }
      }
   }

   public static int getUsbNonBootToneIndicatorID(byte[] response) {
      if (response.length <= 5) {
         return 4401;
      } else {
         byte devId = response[4];
         byte features = response[5];
         switch (devId) {
            case 4:
               return 4413;
            case 5:
               return 4414;
            default:
               return 4401;
         }
      }
   }

   public static int getUsbLegacyToneIndicatorID(byte[] response) {
      if (response.length <= 6) {
         return 4401;
      } else {
         switch (response[5]) {
            case 1:
               if (response[6] == 17) {
                  return 4428;
               }
            case 2:
               return 4420;
            case 3:
               return 4421;
            case 4:
               return 4422;
            case 5:
               return 4423;
            case 6:
               if (response[6] == 11) {
                  return 4425;
               }

               return 4426;
            case 7:
               return 4427;
            case 8:
            case 9:
            case 10:
            default:
               return 4401;
            case 11:
               return 4450;
            case 12:
               return 4424;
         }
      }
   }
}
