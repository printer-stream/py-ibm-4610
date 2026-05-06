package com.ibm.posj.scanner;

import com.ibm.jutil.Util;
import com.ibm.posj.ScannerConfig;
import com.ibm.posj.util.PosjUtil;

public class Scanner4500CmdStrategy extends DefaultScannerCmdStrategy {
   public static final byte[] SCN_4500_CONFIG_JAN13_TWO_LABEL_SCANNER_CMD = null;
   public static final byte[] SCN_4500_REPORT_SCANNER_CMD = null;
   public static final byte[] SCN_4500_REPORT_JAN13_TWO_LABEL_SCANNER_CMD = null;

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
      byte[] config = new byte[1];
      if (this.configInTestMode(scannerConfig)) {
         this.tracer.println("HHBCR-2 Configuration in test mode - All labels are enabled");
      } else if (scannerConfig.isEnabledUPCAE_EANJAN813()) {
         if (scannerConfig.isEnabledCODE39()) {
            config[0] = (byte)PosjUtil.setBit(config[0], 4);
            config[0] = (byte)PosjUtil.setBit(config[0], 5);
         } else if (scannerConfig.isEnabledInterleaved2of5()) {
            config[0] = (byte)PosjUtil.setBit(config[0], 6);
         } else {
            config[0] = (byte)PosjUtil.setBit(config[0], 4);
         }
      }

      this.tracer.println("4500CmdStrategy:: config byte = " + config[0]);
      return config;
   }

   public ScannerConfig getScannerConfigObject(byte[] configBytes) {
      ScannerConfig scannerConfig = new ScannerConfig();
      switch (configBytes[0]) {
         case 0:
            scannerConfig.setEnableUPCAE_EANJAN813(true);
            scannerConfig.setEnableInterleaved2of5(true);
            scannerConfig.setEnableCODE39(true);
            break;
         case 16:
            scannerConfig.setEnableUPCAE_EANJAN813(true);
            break;
         case 48:
            scannerConfig.setEnableUPCAE_EANJAN813(true);
            scannerConfig.setEnableCODE39(true);
            break;
         case 64:
            scannerConfig.setEnableUPCAE_EANJAN813(true);
            scannerConfig.setEnableInterleaved2of5(true);
            break;
         default:
            throw new RuntimeException("Invalid Configuration byte = " + Util.toHexString(configBytes[0]));
      }

      return scannerConfig;
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
      return scannerConfig.isEnabledUPCAE_EANJAN813() && scannerConfig.isEnabledCODE39() && scannerConfig.isEnabledInterleaved2of5();
   }

   public byte[] getReportScannerCmdBytes() {
      return SCN_4500_REPORT_SCANNER_CMD;
   }

   public byte[] getConfigJan13TwoLabelScannerCmdBytes() {
      return SCN_4500_CONFIG_JAN13_TWO_LABEL_SCANNER_CMD;
   }

   public byte[] getReportJan13TwoLabelScannerCmdBytes() {
      return SCN_4500_REPORT_JAN13_TWO_LABEL_SCANNER_CMD;
   }
}
