package com.ibm.posj.scanner;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.ScannerConfig;
import com.ibm.posj.util.PosjUtil;

public class Commmon4696_4697Utility {
   protected Tracer tracer = TracerFactory.getInstance().createTracer("Scanner", "ScannerCmdStrategy");

   public ScannerConfig getScannerConfigObject(byte[] configBytes) {
      ScannerConfig scannerConfig = new ScannerConfig();
      if (PosjUtil.isBitSelected(configBytes[0], 0)) {
         scannerConfig.setEnableUPCAE_EANJAN813(true);
      } else if (PosjUtil.isBitSelected(configBytes[0], 1)) {
         scannerConfig.setEnableUPCAE_EANJAN813(true);
         scannerConfig.setEnableUPCD1D5(true);
      }

      if (PosjUtil.isBitSelected(configBytes[1], 2)) {
         scannerConfig.setEnableUPC_A_To_EAN13Expansion(true);
      }

      if (PosjUtil.isBitSelected(configBytes[1], 3)) {
         scannerConfig.setEnableUPC_E_To_EAN13Expansion(true);
      }

      if (PosjUtil.isBitSelected(configBytes[1], 4)) {
         scannerConfig.setEnableUPC_E_To_EAN13Expansion(true);
      }

      if (PosjUtil.isBitSelected(configBytes[1], 5)) {
         scannerConfig.setEnableVerificationUPC_A_EAN13_fourDigit(true);
      } else if (PosjUtil.isBitSelected(configBytes[1], 7)) {
         scannerConfig.setEnableVerificationUPC_A_EAN13_fiveDigit(true);
      }

      if (PosjUtil.isBitSelected(configBytes[1], 6)) {
         scannerConfig.EnableProgramingViaBarcodes(true);
      }

      if (PosjUtil.isBitSelected(configBytes[3], 0)) {
         scannerConfig.setEnableGoodReadBeep(true);
      }

      if (PosjUtil.isBitSelected(configBytes[3], 3)) {
         if (PosjUtil.isBitSelected(configBytes[3], 4)) {
            scannerConfig.setBeeperVolume((byte)3);
         } else {
            scannerConfig.setBeeperVolume((byte)1);
         }
      } else if (PosjUtil.isBitSelected(configBytes[3], 4)) {
         scannerConfig.setBeeperVolume((byte)2);
      } else {
         scannerConfig.setBeeperVolume((byte)1);
      }

      if (PosjUtil.isBitSelected(configBytes[3], 6)) {
         if (PosjUtil.isBitSelected(configBytes[3], 7)) {
            scannerConfig.setBeeperFrequency((byte)3);
         } else {
            scannerConfig.setBeeperFrequency((byte)1);
         }
      } else if (PosjUtil.isBitSelected(configBytes[3], 7)) {
         scannerConfig.setBeeperFrequency((byte)2);
      } else {
         scannerConfig.setBeeperFrequency((byte)1);
      }

      if (PosjUtil.isBitSelected(configBytes[5], 0)) {
         if (PosjUtil.isBitSelected(configBytes[5], 1)) {
            scannerConfig.setUPCAScansPerRead((byte)4);
         } else {
            scannerConfig.setUPCAScansPerRead((byte)2);
         }
      } else if (PosjUtil.isBitSelected(configBytes[5], 1)) {
         scannerConfig.setUPCAScansPerRead((byte)3);
      } else {
         scannerConfig.setUPCAScansPerRead((byte)1);
      }

      if (PosjUtil.isBitSelected(configBytes[5], 2)) {
         if (PosjUtil.isBitSelected(configBytes[5], 3)) {
            scannerConfig.setUPCEScansPerRead((byte)4);
         } else {
            scannerConfig.setUPCEScansPerRead((byte)1);
         }
      } else if (PosjUtil.isBitSelected(configBytes[5], 3)) {
         scannerConfig.setUPCEScansPerRead((byte)3);
      } else {
         scannerConfig.setUPCEScansPerRead((byte)2);
      }

      if (PosjUtil.isBitSelected(configBytes[5], 4)) {
         if (PosjUtil.isBitSelected(configBytes[5], 5)) {
            scannerConfig.setUPCDScansPerRead((byte)4);
         } else {
            scannerConfig.setUPCDScansPerRead((byte)2);
         }
      } else if (PosjUtil.isBitSelected(configBytes[5], 5)) {
         scannerConfig.setUPCDScansPerRead((byte)3);
      } else {
         scannerConfig.setUPCDScansPerRead((byte)1);
      }

      if (PosjUtil.isBitSelected(configBytes[5], 6)) {
         if (PosjUtil.isBitSelected(configBytes[5], 7)) {
            scannerConfig.setEAN13ScansPerRead((byte)4);
         } else {
            scannerConfig.setEAN13ScansPerRead((byte)2);
         }
      } else if (PosjUtil.isBitSelected(configBytes[5], 7)) {
         scannerConfig.setEAN13ScansPerRead((byte)3);
      } else {
         scannerConfig.setEAN13ScansPerRead((byte)1);
      }

      if (PosjUtil.isBitSelected(configBytes[6], 0)) {
         if (PosjUtil.isBitSelected(configBytes[6], 1)) {
            scannerConfig.setEAN8ScansPerRead((byte)4);
         } else {
            scannerConfig.setEAN8ScansPerRead((byte)1);
         }
      } else if (PosjUtil.isBitSelected(configBytes[6], 1)) {
         scannerConfig.setEAN8ScansPerRead((byte)3);
      } else {
         scannerConfig.setEAN8ScansPerRead((byte)2);
      }

      if (PosjUtil.isBitSelected(configBytes[6], 2)) {
         if (PosjUtil.isBitSelected(configBytes[6], 3)) {
            scannerConfig.setStoreScansPerRead((byte)4);
         } else {
            scannerConfig.setStoreScansPerRead((byte)1);
         }
      } else if (PosjUtil.isBitSelected(configBytes[6], 3)) {
         scannerConfig.setStoreScansPerRead((byte)3);
      } else {
         scannerConfig.setStoreScansPerRead((byte)2);
      }

      if (PosjUtil.isBitSelected(configBytes[6], 6)) {
         scannerConfig.setDecodeAlgorithm((byte)1);
      }

      if (PosjUtil.isBitSelected(configBytes[7], 0)) {
         if (PosjUtil.isBitSelected(configBytes[7], 1)) {
            scannerConfig.setMotorTimeOut((byte)15);
         } else if (PosjUtil.isBitSelected(configBytes[7], 2)) {
            scannerConfig.setMotorTimeOut((byte)60);
         } else {
            scannerConfig.setMotorTimeOut((byte)5);
         }
      } else if (PosjUtil.isBitSelected(configBytes[7], 1)) {
         scannerConfig.setMotorTimeOut((byte)10);
      } else if (PosjUtil.isBitSelected(configBytes[7], 2)) {
         scannerConfig.setMotorTimeOut((byte)30);
      }

      if (PosjUtil.isBitSelected(configBytes[7], 3)) {
         if (PosjUtil.isBitSelected(configBytes[7], 4)) {
            scannerConfig.setLaserTimeOut((byte)15);
         } else {
            scannerConfig.setLaserTimeOut((byte)5);
         }
      } else if (PosjUtil.isBitSelected(configBytes[7], 4)) {
         scannerConfig.setLaserTimeOut((byte)10);
      } else {
         scannerConfig.setLaserTimeOut((byte)15);
      }

      if (PosjUtil.isBitSelected(configBytes[7], 5)) {
         scannerConfig.setDoubleReadTimeOut((byte)70);
      } else if (PosjUtil.isBitSelected(configBytes[7], 6)) {
         scannerConfig.setDoubleReadTimeOut((byte)90);
      } else {
         scannerConfig.setDoubleReadTimeOut((byte)50);
      }

      if (PosjUtil.isBitSelected(configBytes[8], 5)) {
         scannerConfig.EnableSwitchControlledVolumeAdjust(true);
      }

      return scannerConfig;
   }

   public byte[] getScannerConfigBytes(ScannerConfig scannerConfig) {
      byte[] configBytes = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
      if (scannerConfig.isEnabledUPCAE_EANJAN813()) {
         if (scannerConfig.isEnabledUPCD1D5()) {
            configBytes[0] = (byte)PosjUtil.setBit(configBytes[0], 1);
         } else {
            configBytes[0] = (byte)PosjUtil.setBit(configBytes[0], 0);
         }
      }

      if (scannerConfig.isEnabledUPC_A_To_EAN13Expansion()) {
         configBytes[1] = (byte)PosjUtil.setBit(configBytes[1], 2);
      }

      if (scannerConfig.isEnabledUPC_E_To_EAN13Expansion()) {
         configBytes[1] = (byte)PosjUtil.setBit(configBytes[1], 3);
         configBytes[1] = (byte)PosjUtil.setBit(configBytes[1], 4);
      }

      if (scannerConfig.isEnabledVerificationUPC_A_EAN13_fourDigit()) {
         configBytes[1] = (byte)PosjUtil.setBit(configBytes[1], 5);
      } else if (scannerConfig.isEnabledVerificationUPC_A_EAN13_fiveDigit()) {
         configBytes[1] = (byte)PosjUtil.setBit(configBytes[1], 7);
      }

      if (scannerConfig.isEnabledGoodReadBeep()) {
         configBytes[3] = (byte)PosjUtil.setBit(configBytes[3], 0);
      }

      if (scannerConfig.getBeeperDuration() != 0) {
         throw new RuntimeException("Invalid beeper duration - must be 0");
      } else {
         if (scannerConfig.getBeeperVolume() != 0) {
            switch (scannerConfig.getBeeperVolume()) {
               case 1:
                  configBytes[3] = (byte)PosjUtil.setBit(configBytes[3], 3);
                  break;
               case 2:
                  configBytes[3] = (byte)PosjUtil.setBit(configBytes[3], 4);
                  break;
               case 3:
                  configBytes[3] = (byte)PosjUtil.setBit(configBytes[3], 3);
                  configBytes[3] = (byte)PosjUtil.setBit(configBytes[3], 4);
                  break;
               default:
                  throw new RuntimeException("Invalid beeper volume");
            }
         }

         if (scannerConfig.getBeeperFrequency() != 0) {
            switch (scannerConfig.getBeeperFrequency()) {
               case 1:
                  configBytes[3] = (byte)PosjUtil.setBit(configBytes[3], 6);
                  break;
               case 2:
                  configBytes[3] = (byte)PosjUtil.setBit(configBytes[3], 7);
                  break;
               case 3:
                  configBytes[3] = (byte)PosjUtil.setBit(configBytes[3], 6);
                  configBytes[3] = (byte)PosjUtil.setBit(configBytes[3], 7);
                  break;
               default:
                  throw new RuntimeException("Invalid beeper frequency");
            }
         }

         if (scannerConfig.getUPCAScansPerRead() != 0) {
            switch (scannerConfig.getUPCAScansPerRead()) {
               case 1:
               default:
                  break;
               case 2:
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 0);
                  break;
               case 3:
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 1);
                  break;
               case 4:
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 0);
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 1);
            }
         }

         if (scannerConfig.getUPCEScansPerRead() != 0) {
            switch (scannerConfig.getUPCEScansPerRead()) {
               case 1:
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 2);
               case 2:
               default:
                  break;
               case 3:
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 3);
                  break;
               case 4:
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 2);
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 3);
            }
         }

         if (scannerConfig.getUPCDScansPerRead() != 0) {
            switch (scannerConfig.getUPCDScansPerRead()) {
               case 1:
               default:
                  break;
               case 2:
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 4);
                  break;
               case 3:
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 5);
                  break;
               case 4:
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 4);
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 5);
            }
         }

         if (scannerConfig.getEAN13ScansPerRead() != 0) {
            switch (scannerConfig.getEAN13ScansPerRead()) {
               case 1:
               default:
                  break;
               case 2:
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 6);
                  break;
               case 3:
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 7);
                  break;
               case 4:
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 6);
                  configBytes[5] = (byte)PosjUtil.setBit(configBytes[5], 7);
            }
         }

         if (scannerConfig.getEAN8ScansPerRead() != 0) {
            switch (scannerConfig.getEAN8ScansPerRead()) {
               case 1:
                  configBytes[6] = (byte)PosjUtil.setBit(configBytes[6], 0);
               case 2:
               default:
                  break;
               case 3:
                  configBytes[6] = (byte)PosjUtil.setBit(configBytes[6], 1);
                  break;
               case 4:
                  configBytes[6] = (byte)PosjUtil.setBit(configBytes[6], 0);
                  configBytes[6] = (byte)PosjUtil.setBit(configBytes[6], 1);
            }
         }

         if (scannerConfig.getStoreScansPerRead() != 0) {
            switch (scannerConfig.getStoreScansPerRead()) {
               case 1:
                  configBytes[6] = (byte)PosjUtil.setBit(configBytes[6], 2);
               case 2:
               default:
                  break;
               case 3:
                  configBytes[6] = (byte)PosjUtil.setBit(configBytes[6], 3);
                  break;
               case 4:
                  configBytes[6] = (byte)PosjUtil.setBit(configBytes[6], 2);
                  configBytes[6] = (byte)PosjUtil.setBit(configBytes[6], 3);
            }
         }

         if (scannerConfig.getDecodeAlgorithm() == 1) {
            configBytes[6] = (byte)PosjUtil.setBit(configBytes[6], 6);
         }

         if (scannerConfig.getMotorTimeOut() != 0) {
            switch (scannerConfig.getMotorTimeOut()) {
               case 5:
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 0);
                  break;
               case 10:
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 1);
                  break;
               case 15:
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 0);
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 1);
                  break;
               case 30:
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 2);
                  break;
               case 60:
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 0);
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 2);
            }
         }

         if (scannerConfig.getLaserTimeOut() != 0) {
            switch (scannerConfig.getLaserTimeOut()) {
               case 5:
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 3);
                  break;
               case 10:
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 4);
                  break;
               case 15:
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 3);
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 4);
            }
         }

         if (scannerConfig.getDoubleReadTimeOut() != 0) {
            switch (scannerConfig.getDoubleReadTimeOut()) {
               case 70:
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 5);
                  break;
               case 90:
                  configBytes[7] = (byte)PosjUtil.setBit(configBytes[7], 6);
            }
         }

         if (scannerConfig.isEnabledGoodReadBeep()) {
            configBytes[8] = (byte)PosjUtil.setBit(configBytes[8], 1);
         }

         if (scannerConfig.isEnabledSwitchControlledVolumeAdjust()) {
            configBytes[8] = (byte)PosjUtil.setBit(configBytes[8], 5);
         }

         this.tracer.println("4696_4697CmdStrategy:: config byte = " + configBytes[0]);
         return configBytes;
      }
   }
}
