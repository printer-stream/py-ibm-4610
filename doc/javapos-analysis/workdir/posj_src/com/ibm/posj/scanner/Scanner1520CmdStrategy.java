package com.ibm.posj.scanner;

import com.ibm.jutil.Util;
import com.ibm.posj.ScannerCmd;
import com.ibm.posj.ScannerConfig;
import com.ibm.posj.util.PosjUtil;

public class Scanner1520CmdStrategy extends DefaultScannerCmdStrategy {
   public static int SCN_1520_STATUS_BYTES_LENGTH = 3;
   public static byte[] SCN_1520_CONFIG_SCANNER_CMD = new byte[]{49, 0};
   public static byte[] SCN_1520_CONFIG_JAN13_TWO_LABEL_SCANNER_CMD = null;
   public static byte[] SCN_1520_REPORT_SCANNER_CMD = null;
   public static byte[] SCN_1520_REPORT_JAN13_TWO_LABEL_SCANNER_CMD = null;

   public int getStatusBytesLength() {
      return SCN_1520_STATUS_BYTES_LENGTH;
   }

   public int getCmdBytesLength() {
      return 1;
   }

   public boolean isDataInResponse(byte[] status) {
      return status.length > this.getStatusBytesLength();
   }

   public boolean isConfigInResponse(byte[] status) {
      return false;
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
      byte itfLen = scannerConfig.getITFLength1();
      if (itfLen != 0) {
         if (itfLen < 4 || itfLen > 30) {
            this.tracer.println("ITFLength1 should have a value between 4 an 30 !!!!!current value is = " + itfLen);
         }

         itfLen = (byte)(itfLen / 2);
         this.tracer.println("ITFLen passed to the scanner(ITFLen / 2) = " + itfLen);
         config[0] = itfLen;
      }

      if (scannerConfig.isEnabledUPCAE_EANJAN813()) {
         config[0] = (byte)PosjUtil.setBit(config[0], 7);
      }

      if (scannerConfig.isEnabledUPCD1D5()) {
         config[0] = (byte)PosjUtil.setBit(config[0], 6);
      }

      if (scannerConfig.isEnabledCODE39()) {
         config[0] = (byte)PosjUtil.setBit(config[0], 5);
      }

      if (scannerConfig.isEnabledInterleaved2of5()) {
         if (itfLen == 0) {
            throw new RuntimeException("ITFLength1 should be diffrent from 0");
         }

         config[0] = (byte)PosjUtil.setBit(config[0], 4);
      }

      this.tracer.println("Scanner1520CmdStrategy:: config byte = " + Util.toHexString(config[0]));
      return config;
   }

   public ScannerConfig getScannerConfigObject(byte[] configBytes) {
      ScannerConfig scannerConfig = new ScannerConfig();
      if (PosjUtil.isBitSelected(configBytes[0], 7)) {
         scannerConfig.setEnableUPCAE_EANJAN813(true);
      }

      if (PosjUtil.isBitSelected(configBytes[0], 6)) {
         scannerConfig.setEnableUPCD1D5(true);
      }

      if (PosjUtil.isBitSelected(configBytes[0], 5)) {
         scannerConfig.setEnableCODE39(true);
      }

      if (PosjUtil.isBitSelected(configBytes[0], 4)) {
         byte itfLen = (byte)(configBytes[0] & 15);
         scannerConfig.setITFLength1(itfLen);
         scannerConfig.setEnableInterleaved2of5(true);
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

   public byte[] getConfigScannerCmdBytes(ScannerCmd.ConfigScannerCmd cmd) {
      byte[] byteCmd = SCN_1520_CONFIG_SCANNER_CMD;
      byte[] byteConfig = this.getScannerConfigurationBytes(cmd.getConfig());

      for (int i = 0; i < byteConfig.length; i++) {
         byteCmd[i + this.getCmdBytesLength()] = byteConfig[i];
      }

      this.tracer.println("Scanner1520CmdStrategy: Complete config command to return = " + Util.toFormatedHexString(byteCmd));
      return byteCmd;
   }

   public byte[] getReportScannerCmdBytes() {
      return SCN_1520_REPORT_SCANNER_CMD;
   }

   public byte[] getConfigJan13TwoLabelScannerCmdBytes() {
      return SCN_1520_CONFIG_JAN13_TWO_LABEL_SCANNER_CMD;
   }

   public byte[] getReportJan13TwoLabelScannerCmdBytes() {
      return SCN_1520_REPORT_JAN13_TWO_LABEL_SCANNER_CMD;
   }
}
