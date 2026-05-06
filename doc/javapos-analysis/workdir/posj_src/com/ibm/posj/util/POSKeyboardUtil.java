package com.ibm.posj.util;

public class POSKeyboardUtil {
   public static final int DEV_INFO_TYPE_BYTE = 0;
   public static final int DEV_INFO_ID_BYTE = 1;
   public static final int DEV_INFO_ID_BYTE_LEGACY_MODE = 5;
   public static final int DEV_INFO_ID_BYTE_NON_BOOT_MODE = 4;
   public static final int DEV_INFO_ID_BYTE_BOOT_MODE = 4;
   public static final int DEV_INFO_FEATURE_BYTE = 2;
   public static final int DEV_INFO_FEATURE_BYTE_LEGACY_MODE = 6;
   public static final int DEV_INFO_FEATURE_BYTE_NON_BOOT_MODE = 5;
   public static final int DEV_INFO_FEATURE_BYTE_BOOT_MODE = 5;

   public static int getRs485POSKeyboardID(byte[] response) {
      if (response != null && response.length >= 5) {
         switch (response[1]) {
            case 1:
               if (response[2] == 17) {
                  return 3644;
               }
            case 2:
               return 3634;
            case 3:
               return 3636;
            case 4:
               return 3635;
            case 5:
               return 3638;
            case 6:
               if (response[2] == 0) {
                  return 3641;
               }

               return 3639;
            case 7:
               return 3642;
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
               return 3601;
            case 11:
               return 3643;
            case 12:
               return 3637;
            case 28:
               return 3640;
         }
      } else {
         return 3601;
      }
   }

   public static int getPs2POSKeyboardID(short response) {
      int value = 3601;
      if (response == 1) {
         value = 3629;
      } else if (response == 3) {
         value = 3630;
      } else if (response == 4) {
         value = 3631;
      } else if (response == 2) {
         value = 3632;
      } else if (response == 5) {
         value = 3633;
      }

      return value;
   }

   public static int getUsbPOSKeyboardIDNonBootMode(byte[] response) {
      if (response != null && response.length >= 6) {
         switch (response[4]) {
            case 1:
               if (response[5] == 1) {
                  return 3611;
               }

               return 3619;
            case 2:
               return 3611;
            case 3:
               return 3612;
            case 4:
               return 3613;
            case 5:
               return 3614;
            case 6:
               if (response[5] == 11) {
                  return 3616;
               }

               return 3617;
            case 7:
               return 3618;
            case 8:
            case 9:
            case 10:
            default:
               return 3601;
            case 11:
               return 3646;
            case 12:
               return 3615;
         }
      } else {
         return 3601;
      }
   }

   public static int getUsbPOSKeyboardIDBootMode(byte[] response) {
      if (response != null && response.length >= 6) {
         switch (response[4]) {
            case 1:
               if (response[5] == 1) {
                  return 3602;
               }

               return 3610;
            case 2:
               return 3602;
            case 3:
               return 3603;
            case 4:
               return 3604;
            case 5:
               return 3605;
            case 6:
               if (response[5] == 11) {
                  return 3607;
               }

               return 3608;
            case 7:
               return 3609;
            case 8:
            case 9:
            case 10:
            default:
               return 3601;
            case 11:
               return 3645;
            case 12:
               return 3606;
         }
      } else {
         return 3601;
      }
   }

   public static int getUsbPOSKeyboardIDLegacyMode(byte[] response) {
      if (response != null && response.length >= 7) {
         switch (response[5]) {
            case 1:
               if (response[6] == 1) {
                  return 3620;
               }

               return 3628;
            case 2:
               return 3620;
            case 3:
               return 3621;
            case 4:
               return 3622;
            case 5:
               return 3623;
            case 6:
               if (response[6] == 11) {
                  return 3625;
               }

               return 3626;
            case 7:
               return 3627;
            case 8:
            case 9:
            case 10:
            default:
               return 3601;
            case 11:
               return 3647;
            case 12:
               return 3624;
         }
      } else {
         return 3601;
      }
   }

   public static boolean isDBCS(int id) {
      switch (id) {
         case 3602:
         case 3605:
         case 3606:
         case 3607:
         case 3608:
         case 3609:
         case 3611:
         case 3614:
         case 3615:
         case 3616:
         case 3617:
         case 3618:
         case 3620:
         case 3623:
         case 3624:
         case 3625:
         case 3626:
         case 3627:
         case 3632:
         case 3638:
         case 3639:
         case 3640:
         case 3641:
         case 3642:
         case 3643:
         case 3645:
         case 3646:
         case 3647:
            return true;
         case 3603:
         case 3604:
         case 3610:
         case 3612:
         case 3613:
         case 3619:
         case 3621:
         case 3622:
         case 3628:
         case 3629:
         case 3630:
         case 3631:
         case 3633:
         case 3634:
         case 3635:
         case 3636:
         case 3637:
         case 3644:
         default:
            return false;
      }
   }

   public static boolean isDBCS(short productId) {
      switch (productId) {
         case 17925:
         case 17937:
         case 17938:
         case 17942:
         case 18181:
         case 18193:
         case 18194:
         case 18198:
         case 18437:
         case 18449:
         case 18450:
         case 18454:
            return true;
         default:
            return false;
      }
   }

   public static boolean isBootMode(short pId) {
      return 17920 == ('\uff00' & pId);
   }

   public static boolean isSurepointDevice(short productId) {
      switch (productId) {
         case 18544:
         case 18545:
         case 18546:
         case 18547:
         case 18548:
            return true;
         default:
            return false;
      }
   }
}
