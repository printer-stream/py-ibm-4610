package com.ibm.posj.scanner;

import com.ibm.jutil.Util;
import com.ibm.posj.ScannerConfig;
import com.ibm.posj.util.PosjUtil;

public class Scanner4501CmdStrategy extends DefaultScannerCmdStrategy {
   public static final byte[] SCN_4501_REPORT_SCANNER_CMD = new byte[]{49, 0};
   public static final byte[] SCN_4501_CONFIG_JAN13_TWO_LABEL_SCANNER_CMD = null;
   public static final byte[] SCN_4501_REPORT_JAN13_TWO_LABEL_SCANNER_CMD = null;
   public static final byte[] SCN_4501_DIRECTIO = null;

   public int getStatusBytesLength() {
      return DEFAULT_STATUS_BYTES_LENGTH;
   }

   public int getCmdBytesLength() {
      return 2;
   }

   public boolean isDataInResponse(byte[] status) {
      return PosjUtil.isBitSelected(status[this.getDataInResponseBytePos()], this.getDataInResponseBitPos());
   }

   public boolean isConfigInResponse(byte[] status) {
      return PosjUtil.isBitSelected(status[this.getConfigInResponseBytePos()], this.getConfigInResponseBitPos());
   }

   public boolean isHwError(byte[] status) {
      return PosjUtil.isBitSelected(status[this.getHwErrorBytePos()], this.getHwErrorBitPos());
   }

   public boolean isCmdRejected(byte[] status) {
      return PosjUtil.isBitSelected(status[this.getCmdRejectedBytePos()], this.getCmdRejectedBitPos());
   }

   public boolean isConfigCoerced(byte[] status) {
      return false;
   }

   public boolean configSucceed(byte[] status) {
      return false;
   }

   public byte[] getScannerConfigurationBytes(ScannerConfig scannerConfig) {
      byte[] config = new byte[]{0, 0, 0, 0};
      if (scannerConfig.isEnabledCheckModulo()) {
         config[0] = (byte)PosjUtil.setBit(config[0], 2);
      }

      if (scannerConfig.isEnabledDTouchMode()) {
         config[0] = (byte)PosjUtil.setBit(config[0], 3);
      }

      if (this.configInTestMode(scannerConfig)) {
         this.tracer.println("HHBCR-1 Configuration in test mode - All labels are enabled");
      } else if (scannerConfig.isEnabledUPCAE_EANJAN813()) {
         if (scannerConfig.isEnabledCodabar()) {
            config[0] = (byte)PosjUtil.setBit(config[0], 4);
         } else if (scannerConfig.isEnabledUPCD1D5()) {
            config[0] = (byte)PosjUtil.setBit(config[0], 5);
         } else if (scannerConfig.isEnabledCODE39()) {
            config[0] = (byte)PosjUtil.setBit(config[0], 4);
            config[0] = (byte)PosjUtil.setBit(config[0], 5);
         } else if (scannerConfig.isEnabledInterleaved2of5()) {
            config[0] = (byte)PosjUtil.setBit(config[0], 6);
         } else if (scannerConfig.isEnabledCode128()) {
            config[0] = (byte)PosjUtil.setBit(config[0], 4);
            config[0] = (byte)PosjUtil.setBit(config[0], 6);
         } else if (scannerConfig.isEnabledCode93()) {
            config[0] = (byte)PosjUtil.setBit(config[0], 5);
            config[0] = (byte)PosjUtil.setBit(config[0], 6);
         } else if (scannerConfig.isEnabled2digit_supplementals() && scannerConfig.isEnabled5digit_supplementals()) {
            config[0] = (byte)PosjUtil.setBit(config[0], 4);
            config[0] = (byte)PosjUtil.setBit(config[0], 5);
            config[0] = (byte)PosjUtil.setBit(config[0], 6);
         }
      } else if (scannerConfig.isEnabledInterleaved2of5()) {
         config[0] = (byte)PosjUtil.setBit(config[0], 4);
         config[0] = (byte)PosjUtil.setBit(config[0], 7);
      } else if (scannerConfig.isEnabledCode128()) {
         config[0] = (byte)PosjUtil.setBit(config[0], 5);
         config[0] = (byte)PosjUtil.setBit(config[0], 7);
      } else if (scannerConfig.isEnabledCode93()) {
         config[0] = (byte)PosjUtil.setBit(config[0], 4);
         config[0] = (byte)PosjUtil.setBit(config[0], 5);
         config[0] = (byte)PosjUtil.setBit(config[0], 7);
      } else if (scannerConfig.isEnabled2digit_supplementals() && scannerConfig.isEnabled5digit_supplementals()) {
         config[0] = (byte)PosjUtil.setBit(config[0], 6);
         config[0] = (byte)PosjUtil.setBit(config[0], 7);
      }

      this.tracer.println("4501CmdStrategy:: config byte = " + config[0]);
      return config;
   }

   public ScannerConfig getScannerConfigObject(byte[] configBytes) {
      ScannerConfig config = new ScannerConfig();
      byte firstByte = configBytes[0];
      if (PosjUtil.isBitSelected(firstByte, 2)) {
         config.setCheckModulo(true);
      }

      if (PosjUtil.isBitSelected(firstByte, 3)) {
         config.setDTouchMode(true);
      }

      byte highNibble = (byte)(firstByte & 240);
      switch (highNibble) {
         case 0:
            config.setEnableUPCAE_EANJAN813(true);
            config.setEnableCodabar(true);
            config.setEnableUPCD1D5(true);
            config.setEnableCODE39(true);
            config.setEnableInterleaved2of5(true);
            config.setEnableCode128(true);
            config.setEnableCode93(true);
            config.setEnable_2_DigitSupplementals(true);
            config.setEnable_5_DigitSupplementals(true);
            config.setEnableInterleaved2of5(true);
            config.setEnableCode128Supplementals(true);
            return config;
         case 1:
            config.setEnableUPCAE_EANJAN813(true);
            config.setEnableCodabar(true);
         case 2:
            config.setEnableUPCAE_EANJAN813(true);
            config.setEnableUPCD1D5(true);
         case 3:
            config.setEnableUPCAE_EANJAN813(true);
            config.setEnableCODE39(true);
         case 4:
            config.setEnableUPCAE_EANJAN813(true);
            config.setEnableInterleaved2of5(true);
         case 5:
            config.setEnableUPCAE_EANJAN813(true);
            config.setEnableCode128(true);
         case 6:
            config.setEnableUPCAE_EANJAN813(true);
            config.setEnableCode93(true);
         case 7:
            config.setEnableUPCAE_EANJAN813(true);
            config.setEnable_2_DigitSupplementals(true);
            config.setEnable_5_DigitSupplementals(true);
         case 8:
            config.setEnableInterleaved2of5(true);
         case 9:
            config.setEnableInterleaved2of5(true);
         case 10:
            config.setEnableCode128(true);
         case 11:
            config.setEnableCode93(true);
         case 12:
            config.setEnable_2_DigitSupplementals(true);
            config.setEnable_5_DigitSupplementals(true);
         default:
            throw new RuntimeException("Invalid Configuration byte = " + Util.toHexString(firstByte));
      }
   }

   protected int getCmdRejectedBytePos() {
      return this.cmdRejectedBytePosition;
   }

   protected int getCmdRejectedBitPos() {
      return this.cmdRejectedBitPosition;
   }

   protected int getHwErrorBytePos() {
      return this.hwErrorBytePosition;
   }

   protected int getHwErrorBitPos() {
      return this.hwErrorBitPosition;
   }

   protected int getDataInResponseBytePos() {
      return this.dataInResponseBytePosition;
   }

   protected int getDataInResponseBitPos() {
      return this.dataInResponseBitPosition;
   }

   protected int getConfigInResponseBytePos() {
      return this.configInResponseBytePosition;
   }

   protected int getConfigInResponseBitPos() {
      return this.configInResponseBitPosition;
   }

   protected boolean configInTestMode(ScannerConfig scannerConfig) {
      return scannerConfig.isEnabledUPCAE_EANJAN813()
         && scannerConfig.isEnabledCodabar()
         && scannerConfig.isEnabledUPCD1D5()
         && scannerConfig.isEnabledCODE39()
         && scannerConfig.isEnabledInterleaved2of5()
         && scannerConfig.isEnabledCode128()
         && scannerConfig.isEnabledCode93()
         && scannerConfig.isEnabled2digit_supplementals()
         && scannerConfig.isEnabled5digit_supplementals();
   }

   public byte[] getReportScannerCmdBytes() {
      return SCN_4501_REPORT_SCANNER_CMD;
   }

   public byte[] getConfigJan13TwoLabelScannerCmdBytes(byte[] config) {
      return SCN_4501_CONFIG_JAN13_TWO_LABEL_SCANNER_CMD;
   }

   public byte[] getReportJan13TwoLabelScannerCmdBytes() {
      return SCN_4501_REPORT_JAN13_TWO_LABEL_SCANNER_CMD;
   }

   public byte[] getDirectIOScannerCmdBytes() {
      return SCN_4501_DIRECTIO;
   }
}
