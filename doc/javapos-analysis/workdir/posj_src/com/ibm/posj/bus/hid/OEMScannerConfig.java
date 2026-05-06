package com.ibm.posj.bus.hid;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.RuntimePosException;
import com.ibm.posj.ScannerConfig;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PosjUtil;

public class OEMScannerConfig {
   private byte cfg0 = 0;
   private byte cfg1 = 0;
   private byte cfg2 = 0;
   private byte cfg3 = 0;
   private byte cfg4 = 0;
   private byte cfg5 = 0;
   private byte cfg6 = 0;
   private byte cfg7 = 0;
   private byte cfg8 = 0;
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevCats.SCANNER_DEVCAT.toString(), "ScannerDevice");
   private final byte BIT0_ON_MASK = 1;
   private final byte BIT0_OFF_MASK = -2;
   private final byte BIT1_ON_MASK = 2;
   private final byte BIT1_OFF_MASK = -3;
   private final byte BIT2_ON_MASK = 4;
   private final byte BIT2_OFF_MASK = -5;
   private final byte BIT3_ON_MASK = 8;
   private final byte BIT3_OFF_MASK = -9;
   private final byte BIT4_ON_MASK = 16;
   private final byte BIT4_OFF_MASK = -17;
   private final byte BIT5_ON_MASK = 32;
   private final byte BIT5_OFF_MASK = -33;
   private final byte BIT6_ON_MASK = 64;
   private final byte BIT6_OFF_MASK = -65;
   private final byte BIT7_ON_MASK = -128;
   private final byte BIT7_OFF_MASK = 127;

   public byte[] getScannerConfigurationBytes(ScannerConfig scannerConfig) {
      if (scannerConfig.isEnabledUPCAE_EANJAN813()) {
         this.cfg0 = (byte)PosjUtil.setBit(this.cfg0, 0);
      }

      if (scannerConfig.isEnabledUPCD1D5()) {
         this.cfg0 = (byte)PosjUtil.setBit(this.cfg0, 1);
      }

      if (scannerConfig.isEnabledCODE39()) {
         this.cfg0 = (byte)PosjUtil.setBit(this.cfg0, 2);
      }

      if (scannerConfig.isEnabledInterleaved2of5()) {
         this.cfg0 = (byte)PosjUtil.setBit(this.cfg0, 3);
      }

      if (scannerConfig.isEnabledCodabar()) {
         this.cfg0 = (byte)PosjUtil.setBit(this.cfg0, 4);
      }

      if (scannerConfig.isEnabledCode93()) {
         this.cfg0 = (byte)PosjUtil.setBit(this.cfg0, 5);
      }

      if (scannerConfig.isEnabledCode128()) {
         this.cfg0 = (byte)PosjUtil.setBit(this.cfg0, 6);
      }

      if (scannerConfig.isEnabledUCC_EAN128()) {
         this.cfg0 = (byte)PosjUtil.setBit(this.cfg0, 7);
      }

      if (scannerConfig.isEnabled2digit_supplementals()) {
         this.cfg1 = (byte)PosjUtil.setBit(this.cfg1, 0);
      }

      if (scannerConfig.isEnabled5digit_supplementals()) {
         this.cfg1 = (byte)PosjUtil.setBit(this.cfg1, 1);
      }

      if (scannerConfig.isEnabledCode128_supplementals()) {
         this.cfg1 = (byte)PosjUtil.setBit(this.cfg1, 2);
      }

      if (scannerConfig.isEnabledUPCA_A_CheckDigit()) {
         this.cfg1 = (byte)PosjUtil.setBit(this.cfg1, 3);
      }

      if (scannerConfig.isEnabledUPC_E_CheckDigit()) {
         this.cfg1 = (byte)PosjUtil.setBit(this.cfg1, 4);
      }

      if (scannerConfig.isEnabledCode39_CheckDigit()) {
         this.cfg1 = (byte)PosjUtil.setBit(this.cfg1, 5);
      }

      if (scannerConfig.isEnabledITF_CheckDigit()) {
         this.cfg1 = (byte)PosjUtil.setBit(this.cfg1, 6);
      }

      if (scannerConfig.isEnabledEAN_JAN_TwoLabelDecoding()) {
         this.cfg2 = (byte)PosjUtil.setBit(this.cfg2, 0);
      }

      if (scannerConfig.isEnabledUPC_A_To_EAN13Expansion()) {
         this.cfg2 = (byte)PosjUtil.setBit(this.cfg2, 1);
      }

      if (scannerConfig.isEnabledUPC_E_To_EAN13Expansion()) {
         this.cfg2 = (byte)PosjUtil.setBit(this.cfg2, 2);
      }

      if (scannerConfig.isEnabledUPC_E_To_UPC_AExpansion()) {
         this.cfg2 = (byte)PosjUtil.setBit(this.cfg2, 3);
      }

      if (scannerConfig.isEnabledVerificationUPC_A_EAN13_fourDigit()) {
         this.cfg2 = (byte)PosjUtil.setBit(this.cfg2, 4);
      }

      if (scannerConfig.isEnabledVerificationUPC_A_EAN13_fiveDigit()) {
         this.cfg2 = (byte)PosjUtil.setBit(this.cfg2, 5);
      }

      if (scannerConfig.isEnabledGoodReadBeep()) {
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 0);
      }

      if (scannerConfig.getBeeperVolume() == 1) {
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 1);
      }

      if (scannerConfig.getBeeperVolume() == 4 || scannerConfig.getBeeperVolume() == 2) {
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 2);
      }

      if (scannerConfig.getBeeperVolume() == 6 || scannerConfig.getBeeperVolume() == 3) {
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 1);
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 2);
      }

      if (scannerConfig.getBeeperFrequency() == 8 || scannerConfig.getBeeperFrequency() == 1) {
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 3);
      }

      if (scannerConfig.getBeeperFrequency() == 16 || scannerConfig.getBeeperFrequency() == 2) {
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 4);
      }

      if (scannerConfig.getBeeperFrequency() == 24 || scannerConfig.getBeeperFrequency() == 3) {
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 3);
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 4);
      }

      if (scannerConfig.getBeeperDuration() == 32 || scannerConfig.getBeeperDuration() == 1) {
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 5);
      }

      if (scannerConfig.getBeeperDuration() == 64 || scannerConfig.getBeeperDuration() == 2) {
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 6);
      }

      if (scannerConfig.getBeeperDuration() == 96 || scannerConfig.getBeeperDuration() == 3) {
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 5);
         this.cfg3 = (byte)PosjUtil.setBit(this.cfg3, 6);
      }

      if (scannerConfig.getMotorTimeOut() == 1) {
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 0);
      }

      if (scannerConfig.getMotorTimeOut() == 2) {
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 1);
      }

      if (scannerConfig.getMotorTimeOut() == 3) {
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 0);
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 1);
      }

      if (scannerConfig.getMotorTimeOut() == 4) {
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 2);
      }

      if (scannerConfig.getMotorTimeOut() == 5) {
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 0);
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 2);
      }

      if (scannerConfig.getLaserTimeOut() == 8) {
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 3);
      }

      if (scannerConfig.getLaserTimeOut() == 16) {
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 4);
      }

      if (scannerConfig.getLaserTimeOut() == 24) {
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 3);
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 4);
      }

      if (scannerConfig.getDoubleReadTimeOut() == 32) {
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 5);
      }

      if (scannerConfig.getDoubleReadTimeOut() == 64) {
         this.cfg4 = (byte)PosjUtil.setBit(this.cfg4, 6);
      }

      if (scannerConfig.getSecurityLevelForInStore() == 1) {
         this.cfg5 = (byte)PosjUtil.setBit(this.cfg5, 0);
      }

      if (scannerConfig.getSecurityLevelForInStore() == 2) {
         this.cfg5 = (byte)PosjUtil.setBit(this.cfg5, 1);
      }

      if (scannerConfig.getSecurityLevelForInStore() == 3) {
         this.cfg5 = (byte)PosjUtil.setBit(this.cfg5, 0);
         this.cfg5 = (byte)PosjUtil.setBit(this.cfg5, 1);
      }

      this.cfg6 = (byte)(this.cfg6 | scannerConfig.getITFLength1());
      if (scannerConfig.isITFLengthSpecifiedTwo()) {
         this.cfg6 = (byte)PosjUtil.setBit(this.cfg6, 6);
      }

      if (scannerConfig.isITFLengths()) {
         this.cfg6 = (byte)PosjUtil.setBit(this.cfg6, 7);
      }

      this.cfg7 = (byte)(this.cfg7 | scannerConfig.getITFLength2());
      if (scannerConfig.getLED_goodRead_duration() == 1) {
         this.cfg8 = (byte)PosjUtil.setBit(this.cfg8, 0);
      }

      if (scannerConfig.getLED_goodRead_duration() == 2) {
         this.cfg8 = (byte)PosjUtil.setBit(this.cfg8, 1);
      }

      if (scannerConfig.getLED_goodRead_duration() == 3) {
         this.cfg8 = (byte)PosjUtil.setBit(this.cfg8, 0);
         this.cfg8 = (byte)PosjUtil.setBit(this.cfg8, 1);
      }

      if (scannerConfig.isEnabledBarCodeProgramming()) {
         this.cfg8 = (byte)PosjUtil.setBit(this.cfg8, 2);
      }

      if (scannerConfig.isEnabledLaserOnOffSwitch()) {
         this.cfg8 = (byte)PosjUtil.setBit(this.cfg8, 3);
      }

      if (scannerConfig.isEnabledSwitchControlledVolumeAdjust()) {
         this.cfg8 = (byte)PosjUtil.setBit(this.cfg8, 4);
      }

      return new byte[]{this.cfg0, this.cfg1, this.cfg2, this.cfg3, this.cfg4, this.cfg5, this.cfg6, this.cfg7, this.cfg8};
   }

   public ScannerConfig getScannerConfigObject(byte[] configBytes) {
      this.verifyConfigBytes(configBytes);
      ScannerConfig config = new ScannerConfig();
      if (PosjUtil.isBitSelected(configBytes[0], 0)) {
         config.setEnableUPCAE_EANJAN813(true);
      }

      if (PosjUtil.isBitSelected(configBytes[0], 1)) {
         config.setEnableUPCD1D5(true);
      }

      if (PosjUtil.isBitSelected(configBytes[0], 2)) {
         config.setEnableCODE39(true);
      }

      if (PosjUtil.isBitSelected(configBytes[0], 3)) {
         config.setEnableInterleaved2of5(true);
      }

      if (PosjUtil.isBitSelected(configBytes[0], 4)) {
         config.setEnableCodabar(true);
      }

      if (PosjUtil.isBitSelected(configBytes[0], 5)) {
         config.setEnableCode93(true);
      }

      if (PosjUtil.isBitSelected(configBytes[0], 6)) {
         config.setEnableCode128(true);
      }

      if (PosjUtil.isBitSelected(configBytes[0], 7)) {
         config.setEnableUCC_EAN128(true);
      }

      if (PosjUtil.isBitSelected(configBytes[1], 0)) {
         config.setEnable_2_DigitSupplementals(true);
      }

      if (PosjUtil.isBitSelected(configBytes[1], 1)) {
         config.setEnable_5_DigitSupplementals(true);
      }

      if (PosjUtil.isBitSelected(configBytes[1], 2)) {
         config.setEnableCode128Supplementals(true);
      }

      if (PosjUtil.isBitSelected(configBytes[1], 3)) {
         config.setEnableUPC_A_CheckDigit(true);
      }

      if (PosjUtil.isBitSelected(configBytes[1], 4)) {
         config.setEnableUPC_E_CheckDigit(true);
      }

      if (PosjUtil.isBitSelected(configBytes[1], 5)) {
         config.setEnableCode39CheckDigit(true);
      }

      if (PosjUtil.isBitSelected(configBytes[1], 6)) {
         config.setEnableITFCheckDigit(true);
      }

      if (PosjUtil.isBitSelected(configBytes[2], 0)) {
         config.setEnableEAN_JAN_TwoLabelDecoding(true);
      }

      if (PosjUtil.isBitSelected(configBytes[2], 1)) {
         config.setEnableUPC_A_To_EAN13Expansion(true);
      }

      if (PosjUtil.isBitSelected(configBytes[2], 2)) {
         config.setEnableUPC_E_To_EAN13Expansion(true);
      }

      if (PosjUtil.isBitSelected(configBytes[2], 3)) {
         config.setEnableUPC_E_To_UPC_AExpansion(true);
      }

      if (PosjUtil.isBitSelected(configBytes[2], 4)) {
         config.setEnableVerificationUPC_A_EAN13_fourDigit(true);
      }

      if (PosjUtil.isBitSelected(configBytes[2], 5)) {
         config.setEnableVerificationUPC_A_EAN13_fiveDigit(true);
      }

      if (PosjUtil.isBitSelected(configBytes[3], 0)) {
         config.setEnableGoodReadBeep(true);
      }

      if (PosjUtil.isBitSelected(configBytes[3], 1) && PosjUtil.isBitSelected(configBytes[3], 2)) {
         config.setBeeperVolume((byte)6);
      } else if (PosjUtil.isBitSelected(configBytes[3], 2)) {
         config.setBeeperVolume((byte)4);
      } else if (PosjUtil.isBitSelected(configBytes[3], 1)) {
         config.setBeeperVolume((byte)1);
      }

      if (PosjUtil.isBitSelected(configBytes[3], 3) && PosjUtil.isBitSelected(configBytes[3], 4)) {
         config.setBeeperFrequency((byte)24);
      } else if (PosjUtil.isBitSelected(configBytes[3], 3)) {
         config.setBeeperFrequency((byte)8);
      } else if (PosjUtil.isBitSelected(configBytes[3], 4)) {
         config.setBeeperFrequency((byte)16);
      }

      if (PosjUtil.isBitSelected(configBytes[3], 5) && PosjUtil.isBitSelected(configBytes[3], 6)) {
         config.setBeeperDuration((byte)96);
      } else if (PosjUtil.isBitSelected(configBytes[3], 5)) {
         config.setBeeperDuration((byte)32);
      } else if (PosjUtil.isBitSelected(configBytes[3], 6)) {
         config.setBeeperDuration((byte)64);
      }

      if (PosjUtil.isBitSelected(configBytes[4], 0) && PosjUtil.isBitSelected(configBytes[4], 1)) {
         config.setMotorTimeOut((byte)3);
      } else if (PosjUtil.isBitSelected(configBytes[4], 0) && PosjUtil.isBitSelected(configBytes[4], 2)) {
         config.setMotorTimeOut((byte)5);
      } else if (PosjUtil.isBitSelected(configBytes[4], 0)) {
         config.setMotorTimeOut((byte)1);
      } else if (PosjUtil.isBitSelected(configBytes[4], 1)) {
         config.setMotorTimeOut((byte)2);
      } else if (PosjUtil.isBitSelected(configBytes[4], 2)) {
         config.setMotorTimeOut((byte)4);
      }

      if (PosjUtil.isBitSelected(configBytes[4], 3) && PosjUtil.isBitSelected(configBytes[4], 4)) {
         config.setLaserTimeOut((byte)24);
      } else if (PosjUtil.isBitSelected(configBytes[4], 3)) {
         config.setLaserTimeOut((byte)8);
      } else if (PosjUtil.isBitSelected(configBytes[4], 4)) {
         config.setLaserTimeOut((byte)16);
      }

      if (PosjUtil.isBitSelected(configBytes[4], 5)) {
         config.setDoubleReadTimeOut((byte)32);
      }

      if (PosjUtil.isBitSelected(configBytes[4], 6)) {
         config.setDoubleReadTimeOut((byte)64);
      }

      if (PosjUtil.isBitSelected(configBytes[5], 0) && PosjUtil.isBitSelected(configBytes[5], 1)) {
         config.setSecurityLevelForInStore((byte)3);
      } else if (PosjUtil.isBitSelected(configBytes[5], 0)) {
         config.setSecurityLevelForInStore((byte)1);
      } else if (PosjUtil.isBitSelected(configBytes[5], 1)) {
         config.setSecurityLevelForInStore((byte)2);
      }

      if (PosjUtil.isBitSelected(configBytes[0], 3)) {
         byte itfLength1 = (byte)(configBytes[6] & 31);
         if (itfLength1 >= 4 && itfLength1 <= 32) {
            config.setITFLength1(itfLength1);
            if (PosjUtil.isBitSelected(configBytes[6], 6)) {
               config.setITFLengthSpecifiedTwo(true);
            }

            if (PosjUtil.isBitSelected(configBytes[6], 7)) {
               config.setITFLengths(true);
            }
         } else if (this.tracer.isOn()) {
            this.tracer.println("ITFLength1 should be an even value between 4 and 32 (inclusive)");
         }
      }

      if (PosjUtil.isBitSelected(configBytes[0], 3)) {
         byte itfLength2 = (byte)(configBytes[7] & 31);
         if (PosjUtil.isBitSelected(configBytes[6], 6)) {
            if (itfLength2 >= 4 && itfLength2 <= 32) {
               config.setITFLength2(itfLength2);
            } else if (this.tracer.isOn()) {
               this.tracer.println("ITFLength2 should be an even value between 4 and 32 (inclusive)");
            }
         } else {
            config.setITFLength2((byte)0);
         }
      }

      if (PosjUtil.isBitSelected(configBytes[8], 0) && PosjUtil.isBitSelected(configBytes[8], 1)) {
         config.setLED_goodRead_duration((byte)3);
      } else if (PosjUtil.isBitSelected(configBytes[8], 0)) {
         config.setLED_goodRead_duration((byte)1);
      } else if (PosjUtil.isBitSelected(configBytes[8], 1)) {
         config.setLED_goodRead_duration((byte)2);
      }

      if (PosjUtil.isBitSelected(configBytes[8], 2)) {
         config.setBarCodeProgramming(true);
      }

      if (PosjUtil.isBitSelected(configBytes[8], 3)) {
         config.EnableLaserOnOffSwitch(true);
      }

      if (PosjUtil.isBitSelected(configBytes[8], 4)) {
         config.EnableSwitchControlledVolumeAdjust(true);
      }

      return config;
   }

   protected void verifyConfigBytes(byte[] config) {
      if (config[0] == 0) {
         throw new RuntimePosException("Bad configuration... must enable any barcode type");
      } else {
         this.cfg0 = config[0];
         if ((config[1] & -128) == -128) {
            throw new RuntimePosException("Bad configuration... Bit 7 it's reserved");
         } else {
            this.cfg1 = config[1];
            if ((config[2] & 12) == 12 || (config[2] & 48) == 48) {
               throw new RuntimePosException("Bad configuration...");
            } else if ((config[2] & 64) != 64 && (config[2] & -128) != -128) {
               this.cfg2 = config[2];
               if ((config[3] & -128) == -128) {
                  throw new RuntimePosException("Bad configuration... Bit 7 it's reserved");
               } else {
                  this.cfg3 = config[3];
                  if ((config[4] & -128) == -128) {
                     throw new RuntimePosException("Bad configuration... Bit 7 it's reserved");
                  } else {
                     this.cfg4 = config[4];
                     if ((config[5] & -4) > 0) {
                        throw new RuntimePosException("Bad configuration... Bits reserved");
                     } else {
                        this.cfg5 = config[5];
                        if ((config[0] & 8) == 8) {
                           if ((config[6] & 63) < 4 || (config[6] & 63) >= 32 || (config[6] & 63) % 2 != 0) {
                              throw new RuntimePosException("Bad configuration... ITF length 1 must contain an even value");
                           }

                           this.cfg6 = config[6];
                        }

                        if ((config[0] & 8) == 8) {
                           if ((config[6] & 64) != 64) {
                              if ((config[7] & 63) != 0) {
                                 throw new RuntimePosException("Bad configuration... ITF length 2 must be 0 since Two lengths aren't been configured");
                              }
                           } else {
                              if ((config[7] & 63) < 4 || (config[7] & 63) >= 32 || (config[7] & 63) % 2 != 0) {
                                 throw new RuntimePosException("Bad configuration... ITF length 2 must contain an even value");
                              }

                              this.cfg7 = config[7];
                           }
                        }

                        if ((config[8] & -32) > 0) {
                           throw new RuntimePosException("Bad configuration... Bits reserved");
                        } else {
                           this.cfg8 = config[8];
                        }
                     }
                  }
               }
            } else {
               throw new RuntimePosException("Bad configuration... Bit reserved");
            }
         }
      }
   }
}
