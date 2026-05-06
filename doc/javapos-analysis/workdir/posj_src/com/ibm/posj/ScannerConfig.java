package com.ibm.posj;

public class ScannerConfig {
   private boolean bBarCodeProgramming = false;
   private boolean bBeepFreq = false;
   private boolean bBeepLength = false;
   private boolean bBeepState = false;
   private boolean bBeepVolume = false;
   private boolean bBlinkLength = false;
   private boolean bBlockReadMode = false;
   private boolean bBlock1Type = false;
   private boolean bBlock2Type = false;
   private boolean bBlock3Type = false;
   private boolean bBVolSwitchState = false;
   private boolean bCheckModulo = false;
   private boolean bDReadTimeout = false;
   private boolean bDTouchMode = false;
   private boolean bITFLengthType = false;
   private boolean bIANTwoLabelDecode = false;
   private boolean bLabelsQueued = false;
   private boolean bLaserSwitchState = false;
   private boolean bQueueAllLabels = false;
   private boolean bSupplementals = false;
   private boolean bTransmitCheckDigit = false;
   private boolean bUPCExpansion = false;
   private boolean bVerifyPriceChk = false;
   private boolean bEnableUPCAE_EANJAN813 = false;
   private boolean bEnableUPCD1D5 = false;
   private boolean bEnableCODE39 = false;
   private boolean bEnableInterleaved2of5 = false;
   private boolean bEnableStandard2of5 = false;
   private boolean bEnableCodabar = false;
   private boolean bEnableCode93 = false;
   private boolean bEnableCode128 = false;
   private boolean bEnableUCC_EAN128 = false;
   private boolean bEnablePDF417 = false;
   private boolean bEnable_2_DigitSupplementals = false;
   private boolean bEnable_5_DigitSupplementals = false;
   private boolean bEnableCode128Supplementals = false;
   private boolean bEnableUPC_A_CheckDigit = false;
   private boolean bEnableUPC_E_CheckDigit = false;
   private boolean bEnableCode39CheckDigit = false;
   private boolean bEnableITFCheckDIgit = false;
   private boolean bEnableEAN_JAN_TwoLabelDecoding = false;
   private boolean bEnableUPC_A_To_EAN13Expansion = false;
   private boolean bEnableUPC_E_To_EAN13Expansion = false;
   private boolean bEnableUPC_E_To_UPC_AExpansion = false;
   private boolean bEnableVerificationUPC_A_EAN13_fourDigit = false;
   private boolean bEnableVerificationUPC_A_EAN13_fiveDigit = false;
   private boolean bEnableGoodReadBeep = true;
   private boolean bITFLengthSpecifiedTwo = false;
   private boolean bITFLengths = false;
   private boolean bSTFLengths = false;
   private boolean bEnableProgramingViaBarcodes = false;
   private boolean bEnableLaserOnOffSwitch = false;
   private boolean bEnableSwitchControlledVolumeAdjust = false;
   private boolean bEnableRSS14 = false;
   private boolean bEnableRSS_Expanded = false;
   private byte beeperFrequency = 0;
   private byte beeperVolume = 0;
   private byte beeperDuration = 0;
   private byte motorTimeOut = 0;
   private byte laserTimeOut = 0;
   private byte securityLevelForInStore = 0;
   private byte doubleReadTimeOut = 0;
   private byte ITFLength1 = 0;
   private byte ITFLength2 = 0;
   private byte STFLength1 = 0;
   private byte STFLength2 = 0;
   private byte LED_goodRead_duration = 0;
   private byte code128ScansPerRead = 0;
   private byte code39ScansPerRead = 0;
   private byte EAN13ScansPerRead = 0;
   private byte EAN8ScansPerRead = 0;
   private byte ITFScansPerRead = 0;
   private byte scansPerRead = 0;
   private byte storeScansPerRead = 0;
   private byte UPCAScansPerRead = 0;
   private byte UPCDScansPerRead = 0;
   private byte UPCEScansPerRead = 0;
   private byte supplementalsSecurityLevel = 30;
   private byte decodeAlgorithm = 0;
   private byte barCodes1 = 0;
   private byte barCodes2 = 0;
   private byte barCodes3 = 0;
   private byte barCodes4 = 0;
   private byte[] twoLabelFlagPair1 = new byte[2];
   private byte[] twoLabelFlagPair2 = new byte[2];
   private byte[] twoLabelFlagPair3 = new byte[2];
   private byte[] twoLabelFlagPair4 = new byte[2];
   public static final byte VOLUME_MASK = -7;
   public static final byte VOLUME_RETURN_MASK = 6;
   public static final byte LOWEST_VOLUME_OLD = 0;
   public static final byte MEDIUM_VOLUME_OLD = 4;
   public static final byte HIGH_VOLUME_OLD = 6;
   public static final byte LOWEST_VOLUME = 0;
   public static final byte LOW_VOLUME = 1;
   public static final byte MEDIUM_VOLUME = 2;
   public static final byte HIGH_VOLUME = 3;
   public static final byte FREQUENCY_MASK = -25;
   public static final byte FREQUENCY_RETURN_MASK = 24;
   public static final byte LOWEST_FREQUENCY_OLD = 0;
   public static final byte LOW_FREQUENCY_OLD = 8;
   public static final byte MEDIUM_FREQUENCY_OLD = 16;
   public static final byte HIGH_FREQUENCY_OLD = 24;
   public static final byte LOWEST_FREQUENCY = 0;
   public static final byte LOW_FREQUENCY = 1;
   public static final byte MEDIUM_FREQUENCY = 2;
   public static final byte HIGH_FREQUENCY = 3;
   public static final byte DURATION_MASK = -97;
   public static final byte DURATION_RETURN_MASK = 96;
   public static final byte SHORT_DURATION_OLD = 0;
   public static final byte MEDIUM_DURATION_OLD = 32;
   public static final byte LONG_DURATION_OLD = 64;
   public static final byte LONGEST_DURATION_OLD = 96;
   public static final byte SHORT_DURATION = 0;
   public static final byte MEDIUM_DURATION = 1;
   public static final byte LONG_DURATION = 2;
   public static final byte LONGEST_DURATION = 3;
   public static final byte MOTOR_TIME_OUT_MASK = -8;
   public static final byte MOTOR_TIME_OUT_RETURN_MASK = 7;
   public static final byte MOTOR_TIME_OUT_ALWAYS_ON = 0;
   public static final byte MOTOR_TIME_OUT_5_MINUTES = 1;
   public static final byte MOTOR_TIME_OUT_10_MINUTES = 2;
   public static final byte MOTOR_TIME_OUT_15_MINUTES = 3;
   public static final byte MOTOR_TIME_OUT_30_MINUTES = 4;
   public static final byte MOTOR_TIME_OUT_60_MINUTES = 5;
   public static final byte LASSER_TIME_OUT_MASK = -25;
   public static final byte LASSER_TIME_OUT_RETURN_MASK = 24;
   public static final byte LASSER_TIME_OUT_ALWAYS_ON = 0;
   public static final byte LASSER_TIME_OUT_5_MINUTES = 8;
   public static final byte LASSER_TIME_OUT_10_MINUTES = 16;
   public static final byte LASSER_TIME_OUT_15_MINUTES = 24;
   public static final byte DOUBLE_READ_TIME_OUT_MASK = -97;
   public static final byte DOUBLE_READ_TIME_OUT_RETURN_MASK = 96;
   public static final byte DOUBLE_READ_TIME_OUT_SHORT = 0;
   public static final byte DOUBLE_READ_TIME_OUT_MEDIUM = 32;
   public static final byte DOUBLE_READ_TIME_OUT_LONG = 64;
   public static final byte SECURITY_LEVEL_FOR_IN_STORE_MASK = -4;
   public static final byte SECURITY_LEVEL_FOR_IN_STORE_RETURN_MASK = 3;
   public static final byte SECURITY_LEVEL_FOR_IN_STORE_LOW = 0;
   public static final byte SECURITY_LEVEL_FOR_IN_STORE_MEDIUM = 1;
   public static final byte SECURITY_LEVEL_FOR_IN_STORE_HIGH = 2;
   public static final byte SECURITY_LEVEL_FOR_IN_STORE_HIGHEST = 3;
   public static final byte ITF_LENGTH_MASK = -64;
   public static final byte ITF_LENGTH_RETURN_MASK = 63;
   public static final byte LED_GOOD_READ_DURATION_MASK = -4;
   public static final byte LED_GOOD_READ_DURATION_RETURN_MASK = 3;
   public static final byte LED_GOOD_READ_DURATION_SHORT = 0;
   public static final byte LED_GOOD_READ_DURATION_MEDIUM = 1;
   public static final byte LED_GOOD_READ_DURATION_LONG = 2;
   public static final byte LED_GOOD_READ_DURATION_LONGEST = 3;
   public static final byte ITFLENGTH1_DEFAULT = 60;
   public static final byte ITFLENGTH2_DEFAULT = 60;
   public static final byte GROUP_NONE = 0;
   public static final byte GROUP_UPC_EAN_ITF = 1;
   public static final byte GROUP_UPCAED = 2;
   public static final byte GROUP_UPC_EAN_CODE128 = 3;
   public static final byte GROUP_UPC_EAN_CODE93 = 4;
   public static final byte GROUP_UPC_EAN_CODE39 = 5;
   public static final byte GROUP_UPC_EAN_CODABAR = 6;
   public static final byte GROUP_UPC_EAN_2_5_CODABAR = 7;

   public String toString() {
      return new String(
         "Scanner Configuration: \nisEnabledUPCAE_EANJAN813 "
            + this.isEnabledUPCAE_EANJAN813()
            + "\nisEnabledUPCD1D5 "
            + this.isEnabledUPCD1D5()
            + "\nisEnabledCODE39 "
            + this.isEnabledCODE39()
            + "\nisEnabledInterleaved2of5 "
            + this.isEnabledInterleaved2of5()
            + "\nisEnabledStandard2of5 "
            + this.isEnabledStandard2of5()
            + "\nisEnabledCodabar "
            + this.isEnabledCodabar()
            + "\nisEnabledCode93 "
            + this.isEnabledCode93()
            + "\nisEnabledCode128 "
            + this.isEnabledCode128()
            + "\nisEnabledUCC_EAN128 "
            + this.isEnabledUCC_EAN128()
            + "\nisEnabledPDF417"
            + this.isEnabledPDF417()
            + "\nisEnabled2digit_supplementals "
            + this.isEnabled2digit_supplementals()
            + "\nisEnabled5digit_supplementals "
            + this.isEnabled5digit_supplementals()
            + "\nisEnabledCode128_supplementals "
            + this.isEnabledCode128_supplementals()
            + "\nisEnabledUPCA_A_CheckDigit "
            + this.isEnabledUPCA_A_CheckDigit()
            + "\nisEnabledUPC_E_CheckDigit "
            + this.isEnabledUPC_E_CheckDigit()
            + "\nisEnabledCode39_CheckDigit "
            + this.isEnabledCode39_CheckDigit()
            + "\nisEnabledITF_CheckDigit "
            + this.isEnabledITF_CheckDigit()
            + "\nisEnabledUPC_A_To_EAN13Expansion "
            + this.isEnabledUPC_A_To_EAN13Expansion()
            + "\nisEnabledUPC_A_To_EAN13Expansion "
            + this.isEnabledUPC_A_To_EAN13Expansion()
            + "\nisEnabledUPC_E_To_EAN13Expansion "
            + this.isEnabledUPC_E_To_EAN13Expansion()
            + "\nisEnabledUPC_E_To_UPC_AExpansion "
            + this.isEnabledUPC_E_To_UPC_AExpansion()
            + "\nisEnabledVericationUPC_A_EAN13_fourDigit "
            + this.isEnabledVerificationUPC_A_EAN13_fourDigit()
            + "\nisEnabledVericationUPC_A_EAN13_fiveDigit "
            + this.isEnabledVerificationUPC_A_EAN13_fiveDigit()
            + "\nisEnabledGoodReadBeep "
            + this.isEnabledGoodReadBeep()
            + "\ngetBeeperVolume "
            + this.getBeeperVolume()
            + "\ngetBeeperFrequency "
            + this.getBeeperFrequency()
            + "\ngetBeeperDuration "
            + this.getBeeperDuration()
            + "\ngetMotorTimeOut "
            + this.getMotorTimeOut()
            + "\ngetLaserTimeOut "
            + this.getLaserTimeOut()
            + "\ngetDoubleReadTimeOut "
            + this.getDoubleReadTimeOut()
            + "\ngetSecurityLevelForInStore "
            + this.getSecurityLevelForInStore()
            + "\nisITFLengthSpeciedTwo "
            + this.isITFLengthSpecifiedTwo()
            + "\nisITFLengths "
            + this.isITFLengths()
            + "\ngetITFLength1 "
            + this.getITFLength1()
            + "\ngetITFLength2 "
            + this.getITFLength2()
            + "\ngetLED_goodRead_duration "
            + this.getLED_goodRead_duration()
            + "\nisEnabledProgramingViaBarcodes "
            + this.isEnabledProgramingViaBarcodes()
            + "\nisEnabledLaserOnOffSwitch "
            + this.isEnabledLaserOnOffSwitch()
            + "\nisEnabledSwitchControlledVolumeAdjust "
            + this.isEnabledSwitchControlledVolumeAdjust()
            + "\nisEnabledBlock1Type "
            + this.isEnabledBlock1Type()
            + "\nisEnabledBlock2Type "
            + this.isEnabledBlock2Type()
            + "\nisEnabledBlock3Type "
            + this.isEnabledBlock3Type()
            + "\nisEnabledCheckModulo "
            + this.isEnabledCheckModulo()
            + "\ngetCode128ScansPerRead "
            + this.getCode128ScansPerRead()
            + "\ngetCode39ScansPerRead "
            + this.getCode39ScansPerRead()
            + "\nisEnabledDTouchMode "
            + this.isEnabledDTouchMode()
            + "\ngetEAN13ScansPerRead "
            + this.getEAN13ScansPerRead()
            + "\ngetEAN8ScansPerRead "
            + this.getEAN8ScansPerRead()
            + "\ngetITFScansPerRead "
            + this.getITFScansPerRead()
      );
   }

   public void setEnableUPCAE_EANJAN813(boolean value) {
      this.bEnableUPCAE_EANJAN813 = value;
   }

   public void setEnableUPCD1D5(boolean value) {
      this.bEnableUPCD1D5 = value;
   }

   public void setEnableCODE39(boolean value) {
      this.bEnableCODE39 = value;
   }

   public void setEnableInterleaved2of5(boolean value) {
      this.bEnableInterleaved2of5 = value;
   }

   public void setEnableStandard2of5(boolean value) {
      this.bEnableStandard2of5 = value;
   }

   public void setEnableCodabar(boolean value) {
      this.bEnableCodabar = value;
   }

   public void setEnableCode93(boolean value) {
      this.bEnableCode93 = value;
   }

   public void setEnablePDF417(boolean value) {
      this.bEnablePDF417 = value;
   }

   public void setEnableRSS14(boolean value) {
      this.bEnableRSS14 = value;
   }

   public void setEnableRSS_Expanded(boolean value) {
      this.bEnableRSS_Expanded = value;
   }

   public void setEnableCode128(boolean value) {
      this.bEnableCode128 = value;
   }

   public void setEnableUCC_EAN128(boolean value) {
      this.bEnableUCC_EAN128 = value;
   }

   public void setEnable_2_DigitSupplementals(boolean value) {
      this.bEnable_2_DigitSupplementals = value;
   }

   public void setEnable_5_DigitSupplementals(boolean value) {
      this.bEnable_5_DigitSupplementals = value;
   }

   public void setEnableCode128Supplementals(boolean value) {
      this.bEnableCode128Supplementals = value;
   }

   /** @deprecated */
   public void setEnable_2_DigitSuplementals(boolean value) {
      this.bEnable_2_DigitSupplementals = value;
   }

   /** @deprecated */
   public void setEnable_5_DigitSuplementals(boolean value) {
      this.bEnable_5_DigitSupplementals = value;
   }

   /** @deprecated */
   public void setEnableCode128Suplementals(boolean value) {
      this.bEnableCode128Supplementals = value;
   }

   public void setEnableUPC_A_CheckDigit(boolean value) {
      this.bEnableUPC_A_CheckDigit = value;
   }

   public void setEnableUPC_E_CheckDigit(boolean value) {
      this.bEnableUPC_E_CheckDigit = value;
   }

   public void setEnableCode39CheckDigit(boolean value) {
      this.bEnableCode39CheckDigit = value;
   }

   public void setEnableITFCheckDigit(boolean value) {
      this.bEnableITFCheckDIgit = value;
   }

   public void setEnableEAN_JAN_TwoLabelDecoding(boolean value) {
      this.bEnableEAN_JAN_TwoLabelDecoding = value;
   }

   public void setEnableUPC_A_To_EAN13Expansion(boolean value) {
      this.bEnableUPC_A_To_EAN13Expansion = value;
   }

   public void setEnableUPC_E_To_EAN13Expansion(boolean value) {
      this.bEnableUPC_E_To_EAN13Expansion = value;
   }

   public void setEnableUPC_E_To_UPC_AExpansion(boolean value) {
      this.bEnableUPC_E_To_UPC_AExpansion = value;
   }

   public void setEnableVerificationUPC_A_EAN13_fourDigit(boolean value) {
      this.bEnableVerificationUPC_A_EAN13_fourDigit = value;
   }

   public void setEnableVerificationUPC_A_EAN13_fiveDigit(boolean value) {
      this.bEnableVerificationUPC_A_EAN13_fiveDigit = value;
   }

   public void setEnableGoodReadBeep(boolean value) {
      this.bEnableGoodReadBeep = value;
   }

   public void setBeeperVolume(byte volume) {
      this.beeperVolume = volume;
   }

   public void setBeeperFrequency(byte frequency) {
      this.beeperFrequency = frequency;
   }

   public void setBeeperDuration(byte duration) {
      this.beeperDuration = duration;
   }

   public void setMotorTimeOut(byte value) {
      this.motorTimeOut = value;
   }

   /** @deprecated */
   public void setLasserTimeOut(byte value) {
      this.setLaserTimeOut(value);
   }

   public void setLaserTimeOut(byte value) {
      this.laserTimeOut = value;
   }

   public void setDoubleReadTimeOut(byte value) {
      this.doubleReadTimeOut = value;
   }

   public void setSecurityLevelForInStore(byte value) {
      this.securityLevelForInStore = value;
   }

   public void setITFLengthSpecifiedTwo(boolean value) {
      this.bITFLengthSpecifiedTwo = value;
   }

   public void setITFLengths(boolean value) {
      this.bITFLengths = value;
   }

   public void setSTFLengths(boolean value) {
      this.bSTFLengths = value;
   }

   public void setITFLength1(byte value) {
      this.ITFLength1 = value;
   }

   public void setITFLength2(byte value) {
      this.ITFLength2 = value;
   }

   public void setSTFLength1(byte value) {
      this.STFLength1 = value;
   }

   public void setSTFLength2(byte value) {
      this.STFLength2 = value;
   }

   public void setLED_goodRead_duration(byte value) {
      this.LED_goodRead_duration = value;
   }

   public void EnableProgramingViaBarcodes(boolean value) {
      this.bEnableProgramingViaBarcodes = value;
   }

   public void EnableProgrammingViaBarcodes(boolean value) {
      this.bEnableProgramingViaBarcodes = value;
   }

   public void EnableLaserOnOffSwitch(boolean value) {
      this.bEnableLaserOnOffSwitch = value;
   }

   public void EnableSwitchControlledVolumeAdjust(boolean value) {
      this.bEnableSwitchControlledVolumeAdjust = value;
   }

   public void setBarCodes1(byte value) {
      this.barCodes1 = value;
      this.setBarcodes(value);
   }

   public void setBarCodes2(byte value) {
      this.barCodes2 = value;
      this.setBarcodes(value);
   }

   public void setBarCodes3(byte value) {
      this.barCodes3 = value;
      this.setBarcodes(value);
   }

   public void setBarCodes4(byte value) {
      this.barCodes4 = value;
      this.setBarcodes(value);
   }

   public void setBarCodeProgramming(boolean value) {
      this.bBarCodeProgramming = value;
   }

   public void setCheckModulo(boolean value) {
      this.bCheckModulo = value;
   }

   public void setCode128ScansPerRead(byte value) {
      this.code128ScansPerRead = value;
   }

   public void setCode39ScansPerRead(byte value) {
      this.code39ScansPerRead = value;
   }

   public void setDecodeAlgorithm(byte value) {
      this.decodeAlgorithm = value;
   }

   public void setDReadTimeout(boolean value) {
      this.bDReadTimeout = value;
   }

   public void setDTouchMode(boolean value) {
      this.bDTouchMode = value;
   }

   public void setEAN13ScansPerRead(byte value) {
      this.EAN13ScansPerRead = value;
   }

   public void setEAN8ScansPerRead(byte value) {
      this.EAN8ScansPerRead = value;
   }

   public void setITFScansPerRead(byte value) {
      this.ITFScansPerRead = value;
   }

   public void setIANTwoLabelDecode(boolean value) {
      this.bIANTwoLabelDecode = value;
   }

   public void setLaserSwitchState(boolean value) {
      this.bLaserSwitchState = value;
   }

   public void setScansPerRead(byte value) {
      this.scansPerRead = value;
   }

   public void setStoreScansPerRead(byte value) {
      this.storeScansPerRead = value;
   }

   public void setSupplementals(boolean value) {
      this.bSupplementals = value;
   }

   /** @deprecated */
   public void setSuplementals(boolean value) {
      this.bSupplementals = value;
   }

   public void setTransmitCheckDigit(boolean value) {
      this.bTransmitCheckDigit = value;
   }

   public void setTwoLabelFlagPair1_1stLabel(byte value) {
      this.twoLabelFlagPair1[0] = value;
   }

   public void setTwoLabelFlagPair2_1stLabel(byte value) {
      this.twoLabelFlagPair2[0] = value;
   }

   public void setTwoLabelFlagPair3_1stLabel(byte value) {
      this.twoLabelFlagPair3[0] = value;
   }

   public void setTwoLabelFlagPair4_1stLabel(byte value) {
      this.twoLabelFlagPair4[0] = value;
   }

   public void setTwoLabelFlagPair1_2ndLabel(byte value) {
      this.twoLabelFlagPair1[1] = value;
   }

   public void setTwoLabelFlagPair2_2ndLabel(byte value) {
      this.twoLabelFlagPair2[1] = value;
   }

   public void setTwoLabelFlagPair3_2ndLabel(byte value) {
      this.twoLabelFlagPair3[1] = value;
   }

   public void setTwoLabelFlagPair4_2ndLabel(byte value) {
      this.twoLabelFlagPair4[1] = value;
   }

   public void setUPCAScansPerRead(byte value) {
      this.UPCAScansPerRead = value;
   }

   public void setUPCDScansPerRead(byte value) {
      this.UPCDScansPerRead = value;
   }

   public void setUPCEScansPerRead(byte value) {
      this.UPCEScansPerRead = value;
   }

   public void setSupplementalsSecurityLevel(byte value) {
      this.supplementalsSecurityLevel = value;
   }

   /** @deprecated */
   public void setSuplementalsSecurityLevel(byte value) {
      this.supplementalsSecurityLevel = value;
   }

   public void setUPCExpansion(boolean value) {
      this.bUPCExpansion = value;
   }

   public void setVerifyPriceChk(boolean value) {
      this.bVerifyPriceChk = value;
   }

   public boolean isEnabledUPCAE_EANJAN813() {
      return this.bEnableUPCAE_EANJAN813;
   }

   public boolean isEnabledUPCD1D5() {
      return this.bEnableUPCD1D5;
   }

   public boolean isEnabledCODE39() {
      return this.bEnableCODE39;
   }

   public boolean isEnabledInterleaved2of5() {
      return this.bEnableInterleaved2of5;
   }

   public boolean isEnabledStandard2of5() {
      return this.bEnableStandard2of5;
   }

   public boolean isEnabledCodabar() {
      return this.bEnableCodabar;
   }

   public boolean isEnabledCode93() {
      return this.bEnableCode93;
   }

   public boolean isEnabledCode128() {
      return this.bEnableCode128;
   }

   public boolean isEnabledUCC_EAN128() {
      return this.bEnableUCC_EAN128;
   }

   public boolean isEnabledPDF417() {
      return this.bEnablePDF417;
   }

   public boolean isEnabledRSS14() {
      return this.bEnableRSS14;
   }

   public boolean isEnabledRSS_Expanded() {
      return this.bEnableRSS_Expanded;
   }

   public boolean isEnabled2digit_supplementals() {
      return this.bEnable_2_DigitSupplementals;
   }

   public boolean isEnabled5digit_supplementals() {
      return this.bEnable_5_DigitSupplementals;
   }

   public boolean isEnabledCode128_supplementals() {
      return this.bEnableCode128Supplementals;
   }

   /** @deprecated */
   public boolean isEnabled2digit_suplementals() {
      return this.bEnable_2_DigitSupplementals;
   }

   /** @deprecated */
   public boolean isEnabled5digit_suplementals() {
      return this.bEnable_5_DigitSupplementals;
   }

   /** @deprecated */
   public boolean isEnabledCode128_suplementals() {
      return this.bEnableCode128Supplementals;
   }

   public boolean isEnabledUPCA_A_CheckDigit() {
      return this.bEnableUPC_A_CheckDigit;
   }

   public boolean isEnabledUPC_E_CheckDigit() {
      return this.bEnableUPC_E_CheckDigit;
   }

   public boolean isEnabledCode39_CheckDigit() {
      return this.bEnableCode39CheckDigit;
   }

   public boolean isEnabledITF_CheckDigit() {
      return this.bEnableITFCheckDIgit;
   }

   public boolean isEnabledEAN_JAN_TwoLabelDecoding() {
      return this.bEnableEAN_JAN_TwoLabelDecoding;
   }

   public boolean isEnabledUPC_A_To_EAN13Expansion() {
      return this.bEnableUPC_A_To_EAN13Expansion;
   }

   public boolean isEnabledUPC_E_To_EAN13Expansion() {
      return this.bEnableUPC_E_To_EAN13Expansion;
   }

   public boolean isEnabledUPC_E_To_UPC_AExpansion() {
      return this.bEnableUPC_E_To_UPC_AExpansion;
   }

   public boolean isEnabledVerificationUPC_A_EAN13_fourDigit() {
      return this.bEnableVerificationUPC_A_EAN13_fourDigit;
   }

   public boolean isEnabledVerificationUPC_A_EAN13_fiveDigit() {
      return this.bEnableVerificationUPC_A_EAN13_fiveDigit;
   }

   public boolean isEnabledGoodReadBeep() {
      return this.bEnableGoodReadBeep;
   }

   public byte getBeeperVolume() {
      return this.beeperVolume;
   }

   public byte getBeeperFrequency() {
      return this.beeperFrequency;
   }

   public byte getBeeperDuration() {
      return this.beeperDuration;
   }

   public byte getMotorTimeOut() {
      return this.motorTimeOut;
   }

   public byte getLaserTimeOut() {
      return this.laserTimeOut;
   }

   /** @deprecated */
   public byte getLasserTimeOut() {
      return this.getLaserTimeOut();
   }

   public byte getDoubleReadTimeOut() {
      return this.doubleReadTimeOut;
   }

   public byte getSecurityLevelForInStore() {
      return this.securityLevelForInStore;
   }

   public boolean isITFLengthSpecifiedTwo() {
      return this.bITFLengthSpecifiedTwo;
   }

   public boolean isITFLengths() {
      return this.bITFLengths;
   }

   public boolean isSTFLengths() {
      return this.bSTFLengths;
   }

   public byte getITFLength1() {
      return this.ITFLength1;
   }

   public byte getITFLength2() {
      return this.ITFLength2;
   }

   public byte getSTFLength1() {
      return this.STFLength1;
   }

   public byte getSTFLength2() {
      return this.STFLength2;
   }

   public byte getLED_goodRead_duration() {
      return this.LED_goodRead_duration;
   }

   public boolean isEnabledProgramingViaBarcodes() {
      return this.bEnableProgramingViaBarcodes;
   }

   public boolean isEnabledLaserOnOffSwitch() {
      return this.bEnableLaserOnOffSwitch;
   }

   public boolean isEnabledSwitchControlledVolumeAdjust() {
      return this.bEnableSwitchControlledVolumeAdjust;
   }

   public byte getBarCodes1() {
      return this.barCodes1;
   }

   public byte getBarCodes2() {
      return this.barCodes2;
   }

   public byte getBarCodes3() {
      return this.barCodes3;
   }

   public byte getBarCodes4() {
      return this.barCodes3;
   }

   public boolean isEnabledBarCodeProgramming() {
      return this.bBarCodeProgramming;
   }

   public boolean isEnabledBlock1Type() {
      return this.bBlock1Type;
   }

   public boolean isEnabledBlock2Type() {
      return this.bBlock2Type;
   }

   public boolean isEnabledBlock3Type() {
      return this.bBlock3Type;
   }

   public boolean isEnabledCheckModulo() {
      return this.bCheckModulo;
   }

   public byte getCode128ScansPerRead() {
      return this.code128ScansPerRead;
   }

   public byte getCode39ScansPerRead() {
      return this.code39ScansPerRead;
   }

   public byte getDecodeAlgorithm() {
      return this.decodeAlgorithm;
   }

   public boolean isEnabledDReadTimeout() {
      return this.bDReadTimeout;
   }

   public boolean isEnabledDTouchMode() {
      return this.bDTouchMode;
   }

   public byte getEAN13ScansPerRead() {
      return this.EAN13ScansPerRead;
   }

   public byte getEAN8ScansPerRead() {
      return this.EAN8ScansPerRead;
   }

   public byte getITFScansPerRead() {
      return this.ITFScansPerRead;
   }

   public boolean isEnabledIANTwoLabelDecode() {
      return this.bIANTwoLabelDecode;
   }

   public boolean isEnabledLaserSwitchState() {
      return this.bLaserSwitchState;
   }

   public byte getScansPerRead() {
      return this.scansPerRead;
   }

   public byte getStoreScansPerRead() {
      return this.storeScansPerRead;
   }

   public boolean isEnabledSupplementals() {
      return this.bSupplementals;
   }

   /** @deprecated */
   public boolean isEnabledSuplementals() {
      return this.bSupplementals;
   }

   public boolean isEnabledTransmitCheckDigit() {
      return this.bTransmitCheckDigit;
   }

   public byte getTwoLabelFlagPair1_1stLabel() {
      return this.twoLabelFlagPair1[0];
   }

   public byte getTwoLabelFlagPair2_1stLabel() {
      return this.twoLabelFlagPair2[0];
   }

   public byte isEnabledTwoLabelFlagPair3_1stLabel() {
      return this.twoLabelFlagPair3[0];
   }

   public byte isEnabledTwoLabelFlagPair4_1stLabel() {
      return this.twoLabelFlagPair4[0];
   }

   public byte getTwoLabelFlagPair1_2ndLabel() {
      return this.twoLabelFlagPair1[1];
   }

   public byte getTwoLabelFlagPair2_2ndLabel() {
      return this.twoLabelFlagPair2[1];
   }

   public byte isEnabledTwoLabelFlagPair3_2ndLabel() {
      return this.twoLabelFlagPair3[1];
   }

   public byte isEnabledTwoLabelFlagPair4_2ndLabel() {
      return this.twoLabelFlagPair4[1];
   }

   public byte getUPCAScansPerRead() {
      return this.UPCAScansPerRead;
   }

   public byte getUPCDScansPerRead() {
      return this.UPCDScansPerRead;
   }

   public byte getUPCEScansPerRead() {
      return this.UPCEScansPerRead;
   }

   public byte getSupplementalsSecurityLevel() {
      return this.supplementalsSecurityLevel;
   }

   /** @deprecated */
   public byte getSuplementalsSecurityLevel() {
      return this.supplementalsSecurityLevel;
   }

   public boolean isEnabledUPCExpansion() {
      return this.bUPCExpansion;
   }

   public boolean isEnabledVerifyPriceChk() {
      return this.bVerifyPriceChk;
   }

   protected void setBarcodes(byte group) {
      switch (group) {
         case 1:
            this.setEnableUPCAE_EANJAN813(true);
            this.setEnableInterleaved2of5(true);
         case 2:
            this.setEnableUPCAE_EANJAN813(true);
            this.setEnableUPCD1D5(true);
         case 3:
            this.setEnableUPCAE_EANJAN813(true);
            this.setEnableCode128(true);
         case 4:
            this.setEnableUPCAE_EANJAN813(true);
            this.setEnableCode93(true);
         case 5:
            this.setEnableUPCAE_EANJAN813(true);
            this.setEnableCODE39(true);
         case 6:
            this.setEnableUPCAE_EANJAN813(true);
            this.setEnableCodabar(true);
         case 7:
            this.setEnableUPCAE_EANJAN813(true);
            this.setEnableCodabar(true);
            this.setEnable_2_DigitSupplementals(true);
            this.setEnable_5_DigitSupplementals(true);
      }
   }
}
