package com.ibm.posj.util;

public class KeylockUtil {
   public static final int DEV_INFO_TYPE_BYTE = 0;
   public static final int DEV_INFO_ID_BYTE = 1;
   public static final int DEV_INFO_FEATURE_BYTE = 2;
   public static final int USB_ADMIN_DEV_ID_POSITION = 4;
   public static final int USB_ADMIN_FEATURES_POSITION = 5;
   public static final int USB_LEGACY_DEV_ID_POSITION = 5;
   public static final int USB_LEGACY_FEATURES_POSITION = 6;

   public static int getRs485KeylockID(byte[] response) {
      if (response.length < 5) {
         return 2910;
      } else {
         switch (response[1]) {
            case 1:
               if (response[2] == 17) {
                  return 2916;
               }
            case 2:
               return 2915;
            case 3:
               return 2918;
            case 4:
               return 2917;
            case 5:
               return 2920;
            case 6:
               if (response[2] == 0) {
                  return 2923;
               }

               return 2921;
            case 7:
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
               return 2910;
            case 11:
               return 2924;
            case 12:
               return 2919;
            case 28:
               return 2922;
         }
      }
   }

   public static int getPs2KeylockID(short response) {
      int value = 2910;
      if (response == 1) {
         value = 2912;
      } else if (response == 4) {
         value = 2913;
      } else if (response == 2) {
         value = 2914;
      }

      return value;
   }

   public static int getUsbBootKeylockID(byte[] response) {
      if (response.length <= 5) {
         return 2910;
      } else {
         byte devId = response[4];
         byte features = response[5];
         switch (devId) {
            case 4:
               return 2928;
            case 5:
               return 2929;
            case 11:
               return 2949;
            default:
               return 2910;
         }
      }
   }

   public static int getUsbNonBootKeylockID(byte[] response) {
      if (response.length <= 5) {
         return 2910;
      } else {
         byte devId = response[4];
         byte features = response[5];
         switch (devId) {
            case 4:
               return 2944;
            case 5:
               return 2945;
            default:
               return 2910;
         }
      }
   }

   public static int getUsbLegacyKeylockID(byte[] response) {
      if (response.length <= 6) {
         return 2910;
      } else {
         switch (response[5]) {
            case 1:
               if (response[6] == 17) {
                  return 2934;
               }
            case 2:
               return 2933;
            case 3:
               return 2935;
            case 4:
               return 2936;
            case 5:
               return 2937;
            case 6:
               if (response[6] == 11) {
                  return 2939;
               }

               return 2940;
            case 7:
            case 8:
            case 9:
            case 10:
            default:
               return 2910;
            case 11:
               return 2951;
            case 12:
               return 2938;
         }
      }
   }

   public static boolean isDBCS(int id) {
      switch (id) {
         case 2914:
         case 2916:
         case 2920:
         case 2921:
         case 2922:
         case 2923:
         case 2924:
         case 2926:
         case 2929:
         case 2931:
         case 2932:
         case 2934:
         case 2937:
         case 2939:
         case 2940:
         case 2945:
         case 2949:
         case 2950:
         case 2951:
            return true;
         case 2915:
         case 2917:
         case 2918:
         case 2919:
         case 2925:
         case 2927:
         case 2928:
         case 2930:
         case 2933:
         case 2935:
         case 2936:
         case 2938:
         case 2941:
         case 2942:
         case 2943:
         case 2944:
         case 2946:
         case 2947:
         case 2948:
         default:
            return false;
      }
   }
}
