package com.ibm.posj.scanner;

import com.ibm.posj.ScannerCmd;
import com.ibm.posj.ScannerConfig;

public class Scanner4687CmdStrategy extends DefaultScannerCmdStrategy {
   public static final byte[] SCN_4687_CONFIG_SCANNER_CMD = null;
   public static final byte[] SCN_4687_CONFIG_JAN13_TWO_LABEL_SCANNER_CMD = null;
   public static final byte[] SCN_4687_REPORT_SCANNER_CMD = null;
   public static final byte[] SCN_4687_REPORT_JAN13_TWO_LABEL_SCANNER_CMD = null;
   public static final int SCN_4687_STATUS_BYTES_LENGTH = 3;

   public int getStatusBytesLength() {
      return 3;
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
      return false;
   }

   public boolean isCmdRejected(byte[] status) {
      return false;
   }

   public boolean isConfigCoerced(byte[] status) {
      return false;
   }

   public boolean configSucceed(byte[] status) {
      return false;
   }

   public byte[] getScannerConfigurationBytes(ScannerConfig scannerConfig) {
      this.tracer.println("getScannerConfigurationBytes shouldn't be called for model 4687!!");
      return new byte[0];
   }

   public ScannerConfig getScannerConfigObject(byte[] configBytes) {
      this.tracer.println("getScannerConfigObject shouldn't be called for model 4687!!");
      return null;
   }

   protected int getCmdRejectedBytePos() {
      return -1;
   }

   protected int getCmdRejectedBitPos() {
      return -1;
   }

   protected int getHwErrorBytePos() {
      return -1;
   }

   protected int getHwErrorBitPos() {
      return -1;
   }

   protected int getDataInResponseBytePos() {
      return -1;
   }

   protected int getDataInResponseBitPos() {
      return -1;
   }

   protected int getConfigInResponseBytePos() {
      return -1;
   }

   protected int getConfigInResponseBitPos() {
      return -1;
   }

   public byte[] getConfigScannerCmdBytes(ScannerCmd.ConfigScannerCmd cmd) {
      return SCN_4687_CONFIG_SCANNER_CMD;
   }

   public byte[] getReportScannerCmdBytes() {
      return SCN_4687_REPORT_SCANNER_CMD;
   }

   public byte[] getConfigJan13TwoLabelScannerCmdBytes() {
      return SCN_4687_CONFIG_JAN13_TWO_LABEL_SCANNER_CMD;
   }

   public byte[] getReportJan13TwoLabelScannerCmdBytes() {
      return SCN_4687_REPORT_JAN13_TWO_LABEL_SCANNER_CMD;
   }
}
