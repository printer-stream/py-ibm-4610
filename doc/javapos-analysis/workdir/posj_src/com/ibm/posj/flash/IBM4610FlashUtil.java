package com.ibm.posj.flash;

import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.DefaultPOSPrinterCmd;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleException;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.IBM4610PrinterHandleImp;
import com.ibm.posj.util.PosjUtil;
import java.util.Iterator;
import java.util.List;

public class IBM4610FlashUtil {
   protected static final int CRABTREE_TYPE = 31;
   protected static final int NORMAL_TYPE = 30;
   protected static final int TI1_TI2 = 0;
   protected static final int TI3_TI4 = 1;
   protected static final int TI5_PRINTERS = 2;
   protected static final int SST_SB = 3;
   protected static final int TI3_TI4_8MB = 4;
   protected static final int SST_SB_8MB = 5;
   protected static final int SST_DB = 7;
   private static String T1_OR_T2_PRINTER_FILENAME = "aip46mc.hex";
   private static String T3_OR_T4_PRINTER_FILENAME = "aip46mch.hex";
   private static String TI5_PRINTER_FILENAME = "aip46mcd.hex";
   private static String CRABTREE_PRINTER_FILENAME = "aip46ti8.hex";
   private static Tracer tracer = TracerFactory.getInstance().createTracer("FLASH", "IBM4610FlashUtil");

   public static int getDeviceType(Handle printerHandle) {
      if (tracer.isOn()) {
         tracer.println(1, ">>IBM4610FlashUtil.getDeviceType");
      }

      IBM4610PrinterHandleImp handleImp = (IBM4610PrinterHandleImp)printerHandle.getHandleImp();
      byte printerFeatureByte1 = handleImp.getFeatureByte1();
      byte printerFeatureByte2 = handleImp.getFeatureByte2();
      int printerID = handleImp.getPrinterHandleState().getPrinterID();
      boolean is2MbFlashMemory = PosjUtil.isBitSelected(printerFeatureByte1, 2);
      if (tracer.isOn()) {
         tracer.println(3, "ID " + Util.toHexString(printerID));
      }

      if (isSBCrabtree(printerID)) {
         if (tracer.isOn()) {
            tracer.println(1, "  is SB Crabtree - DeviceType is 03");
         }

         return 3;
      } else if (is12Family(printerID)) {
         if (tracer.isOn()) {
            tracer.println(1, "  is TI1/2 - DeviceType is Unknown");
         }

         return -1;
      } else if (is34Family(printerID) && !is2MbFlashMemory) {
         if (tracer.isOn()) {
            tracer.println(1, "  is TI3/4  - DeviceType is 01");
         }

         tracer.println(".findFlashFile() - T3/T4 Is there 2Mb extra flash mem :" + is2MbFlashMemory);
         if (PosjUtil.isBitSelected(printerFeatureByte2, 1)) {
            tracer.println("TI8 printer in emulation mode is not supported");
            return -1;
         } else {
            return 1;
         }
      } else if (!isDBFamily(printerID) && !is2MbFlashMemory) {
         return -1;
      } else {
         if (tracer.isOn()) {
            tracer.println(1, "  is DB 4610 TI5- DeviceType is 2");
         }

         return 2;
      }
   }

   public static FlashFile findFlashFile(int printerType, int printerID, int printerFeatureByte1, int printerFeatureByte2, List rs485FlashFileList) throws FlashException {
      if (tracer.isOn()) {
         tracer.println(1, ">>IBM4610FlashUtil.findFlashFile");
      }

      if (null == rs485FlashFileList) {
         return null;
      } else {
         Iterator flashFiles = rs485FlashFileList.iterator();
         FlashFile currFlashFile = null;
         boolean is2MbFlashMemory = PosjUtil.isBitSelected(printerFeatureByte1, 2);
         if (tracer.isOn()) {
            tracer.println(3, "Type " + Util.toHexString(printerType));
            tracer.println(3, "ID " + Util.toHexString(printerID));
            tracer.println(3, "Feature2 " + Util.toHexString(printerFeatureByte2));
         }

         if (isSBCrabtree(printerID)) {
            if (tracer.isOn()) {
               tracer.println(1, "  is SB Crabtree");
            }

            while (flashFiles.hasNext()) {
               currFlashFile = (FlashFile)flashFiles.next();
               if (currFlashFile instanceof Rs485FlashFile) {
                  if (tracer.isOn()) {
                     tracer.println(3, "checking file " + currFlashFile.getFilename());
                  }

                  if (currFlashFile.getFilename().endsWith(CRABTREE_PRINTER_FILENAME) && currFlashFile.getRs485DeviceType() == 3) {
                     tracer.println(".findFlashFile()-find printer file(Crabtree type) " + currFlashFile.getFilename());
                     return currFlashFile;
                  }
               }
            }

            return null;
         } else {
            if (is12Family(printerID)) {
               if (tracer.isOn()) {
                  tracer.println(1, "  is TI1/2");
               }

               while (flashFiles.hasNext()) {
                  currFlashFile = (FlashFile)flashFiles.next();
                  if (currFlashFile instanceof Rs485FlashFile) {
                     if (tracer.isOn()) {
                        tracer.println(3, "checking file " + currFlashFile.getFilename());
                     }

                     if (currFlashFile.getFilename().endsWith(T1_OR_T2_PRINTER_FILENAME)) {
                        tracer.println(".findFlashFile()-find printer file(T1,2): " + currFlashFile.getFilename());
                        return currFlashFile;
                     }
                  }
               }
            } else if (is34Family(printerID) && !is2MbFlashMemory) {
               if (tracer.isOn()) {
                  tracer.println(1, "  is TI3/4");
               }

               tracer.println(".findFlashFile() - T3/T4 Is there 2Mb extra flash mem :" + is2MbFlashMemory);
               if (PosjUtil.isBitSelected(printerFeatureByte2, 1)) {
                  tracer.println("TI8 printer in emulation mode is not supported");
                  return null;
               }

               while (flashFiles.hasNext()) {
                  currFlashFile = (FlashFile)flashFiles.next();
                  if (currFlashFile instanceof Rs485FlashFile) {
                     if (tracer.isOn()) {
                        tracer.println(3, "checking file " + currFlashFile.getFilename());
                     }

                     if (currFlashFile.getFilename().endsWith(T3_OR_T4_PRINTER_FILENAME) && currFlashFile.getRs485DeviceType() == 1) {
                        tracer.println(".findFlashFile()-find printer file(T3,4): " + currFlashFile.getFilename());
                        return currFlashFile;
                     }
                  }
               }
            } else if (isDBFamily(printerID) || is2MbFlashMemory) {
               if (tracer.isOn()) {
                  tracer.println(1, "  is DB 4610");
               }

               while (flashFiles.hasNext()) {
                  currFlashFile = (FlashFile)flashFiles.next();
                  if (currFlashFile instanceof Rs485FlashFile) {
                     if (tracer.isOn()) {
                        tracer.println(3, "checking file " + currFlashFile.getFilename());
                     }

                     if (currFlashFile.getFilename().endsWith(TI5_PRINTER_FILENAME) && currFlashFile.getRs485DeviceType() == 2) {
                        tracer.println(".findFlashFile()-find printer file(T5): " + currFlashFile.getFilename());
                        return currFlashFile;
                     }
                  }
               }
            }

            return null;
         }
      }
   }

   public static boolean isSBCrabtree(int id) {
      switch (id) {
         case 3814:
         case 3822:
         case 3829:
         case 3837:
         case 3838:
         case 3839:
            return true;
         default:
            return false;
      }
   }

   public static boolean is12Family(int id) {
      switch (id) {
         case 3811:
         case 3819:
            return true;
         default:
            return false;
      }
   }

   public static boolean is34Family(int id) {
      switch (id) {
         case 3812:
         case 3815:
         case 3816:
         case 3817:
         case 3820:
         case 3823:
         case 3824:
         case 3825:
         case 3827:
         case 3830:
         case 3831:
         case 3832:
            return true;
         case 3813:
         case 3814:
         case 3818:
         case 3819:
         case 3821:
         case 3822:
         case 3826:
         case 3828:
         case 3829:
         default:
            return false;
      }
   }

   public static boolean isDBFamily(int id) {
      switch (id) {
         case 3813:
         case 3818:
         case 3821:
         case 3826:
         case 3828:
         case 3833:
            return true;
         default:
            return false;
      }
   }

   public static int getRealECLevel(Handle printerHandle) throws FlashException {
      int realECLevel = -1;
      IBM4610PrinterHandleImp handleImp = (IBM4610PrinterHandleImp)printerHandle.getHandleImp();
      realECLevel = handleImp.getPrinterHandleState().getPrinterEC();
      if (realECLevel <= 103) {
         return realECLevel;
      } else {
         POSPrinterCmd cmdMCT7 = ((POSPrinterCmd.Factory)printerHandle.getHandleCmdFactory()).createMCTReadCmd((byte)7);
         POSPrinterCmd cmdMCT16 = ((POSPrinterCmd.Factory)printerHandle.getHandleCmdFactory()).createMCTReadCmd((byte)16);

         try {
            handleImp.submit(cmdMCT7);
         } catch (HandleException var10) {
            throw new FlashException("can't get MCT for device", var10);
         }

         cmdMCT7.waitUntilCompleted(10000L);
         if (cmdMCT7.isCompleted() && ((DefaultPOSPrinterCmd)cmdMCT7).getPrintCmd().isSucceeded()) {
            byte byteMCT7 = ((DefaultPOSPrinterCmd)cmdMCT7).getRequestData().byteAt(0);
            byteMCT7 = (byte)(byteMCT7 & 16);
            byteMCT7 = (byte)(byteMCT7 >> 4);
            if (byteMCT7 == 1) {
               return realECLevel;
            }
         }

         try {
            handleImp.submit(cmdMCT16);
         } catch (HandleException var9) {
            throw new FlashException("can't get MCT for device", var9);
         }

         cmdMCT16.waitUntilCompleted(10000L);
         if (cmdMCT16.isCompleted() && ((DefaultPOSPrinterCmd)cmdMCT16).getPrintCmd().isSucceeded()) {
            byte byteMCT16 = ((DefaultPOSPrinterCmd)cmdMCT16).getRequestData().byteAt(1);
            byteMCT16 = (byte)(byteMCT16 & 16);
            byteMCT16 = (byte)(byteMCT16 >> 4);
            if (byteMCT16 == 0) {
               realECLevel -= 16;
            }
         }

         return realECLevel;
      }
   }
}
