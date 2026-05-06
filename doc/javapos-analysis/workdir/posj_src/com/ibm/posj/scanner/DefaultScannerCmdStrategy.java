package com.ibm.posj.scanner;

import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.ScannerCmd;
import com.ibm.posj.ScannerConfig;

public abstract class DefaultScannerCmdStrategy {
   public int cmdRejectedBytePosition = 1;
   public int cmdRejectedBitPosition = 7;
   public int hwErrorBytePosition = 1;
   public int hwErrorBitPosition = 6;
   public int dataInResponseBytePosition = 0;
   public int dataInResponseBitPosition = 5;
   public int configInResponseBytePosition = 0;
   public int configInResponseBitPosition = 6;
   protected Tracer tracer = TracerFactory.getInstance().createTracer("Scanner", "ScannerCmdStrategy");
   public static final byte[] SCANNER_TEST_REQUEST_CMD = new byte[]{0, 16};
   public static final byte[] SCANNER_STATUS_REQUEST_CMD = new byte[]{0, 32};
   public static final byte[] SCANNER_RESET_REQUEST_CMD = new byte[]{0, 64};
   public static final byte[] ENABLE_SCANNER_CMD = new byte[]{17, 0};
   public static final byte[] DISABLE_SCANNER_CMD = new byte[]{18, 0};
   public static final byte[] ENABLE_BEEPER_SCANNER_CMD = new byte[]{20, 0};
   public static final byte[] DISABLE_BEEPER_SCANNER_CMD = new byte[]{24, 0};
   public static final byte[] CONFIG_SCANNER_CMD = new byte[]{32, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] CONFIG_JAN13_TWO_LABEL_SCANNER_CMD = new byte[]{35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   public static final byte[] REPORT_SCANNER_CMD = new byte[]{33, 0};
   public static final byte[] REPORT_JAN13_TWO_LABEL_SCANNER_CMD = new byte[]{52, 0};
   public static final byte[] DIRECTIO_SCANNER_CMD = new byte[]{48, 0};
   public static int DEFAULT_STATUS_BYTES_LENGTH = 2;
   public static final int CONFIG_SUCCESS_BYTE_POS = 2;
   public static final int CONFIG_SUCCESS_BIT_POS = 0;

   public abstract int getStatusBytesLength();

   public abstract int getCmdBytesLength();

   public abstract boolean isDataInResponse(byte[] var1);

   public abstract boolean isConfigInResponse(byte[] var1);

   public abstract boolean isHwError(byte[] var1);

   public abstract boolean isCmdRejected(byte[] var1);

   public abstract boolean isConfigCoerced(byte[] var1);

   public abstract boolean configSucceed(byte[] var1);

   public abstract byte[] getScannerConfigurationBytes(ScannerConfig var1);

   public abstract ScannerConfig getScannerConfigObject(byte[] var1);

   protected abstract int getCmdRejectedBytePos();

   protected abstract int getCmdRejectedBitPos();

   protected abstract int getHwErrorBytePos();

   protected abstract int getHwErrorBitPos();

   protected abstract int getDataInResponseBytePos();

   protected abstract int getDataInResponseBitPos();

   protected abstract int getConfigInResponseBytePos();

   protected abstract int getConfigInResponseBitPos();

   public byte[] getTestRequestCmdBytes() {
      return SCANNER_TEST_REQUEST_CMD;
   }

   public byte[] getStatusRequestCmdBytes() {
      return SCANNER_STATUS_REQUEST_CMD;
   }

   public byte[] getResetRequestCmdBytes() {
      return SCANNER_RESET_REQUEST_CMD;
   }

   public byte[] getEnableScannerCmdBytes() {
      byte[] cmdBytes = new byte[this.getCmdBytesLength()];

      for (int i = 0; i < this.getCmdBytesLength(); i++) {
         cmdBytes[i] = ENABLE_SCANNER_CMD[i];
      }

      return cmdBytes;
   }

   public byte[] getDisableScannerCmdBytes() {
      byte[] cmdBytes = new byte[this.getCmdBytesLength()];

      for (int i = 0; i < this.getCmdBytesLength(); i++) {
         cmdBytes[i] = DISABLE_SCANNER_CMD[i];
      }

      return cmdBytes;
   }

   public byte[] getEnableBeeperScannerCmdBytes() {
      byte[] cmdBytes = new byte[this.getCmdBytesLength()];

      for (int i = 0; i < this.getCmdBytesLength(); i++) {
         cmdBytes[i] = ENABLE_BEEPER_SCANNER_CMD[i];
      }

      return cmdBytes;
   }

   public byte[] getDisableBeeperScannerCmdBytes() {
      byte[] cmdBytes = new byte[this.getCmdBytesLength()];

      for (int i = 0; i < this.getCmdBytesLength(); i++) {
         cmdBytes[i] = DISABLE_BEEPER_SCANNER_CMD[i];
      }

      return cmdBytes;
   }

   public byte[] getConfigScannerCmdBytes(ScannerCmd.ConfigScannerCmd cmd) {
      byte[] byteCmd = CONFIG_SCANNER_CMD;
      byte[] byteConfig = this.getScannerConfigurationBytes(cmd.getConfig());

      for (int i = this.getCmdBytesLength(); i < byteConfig.length; i++) {
         byteCmd[i] = byteConfig[i - this.getCmdBytesLength()];
      }

      if (this.tracer.isOn()) {
         this.tracer.println("DefaultScannerCmdStrategy:: Complete config command to return = " + Util.toFormatedHexString(byteCmd));
      }

      return byteCmd;
   }

   public byte[] getReportScannerCmdBytes() {
      byte[] cmdBytes = new byte[this.getCmdBytesLength()];

      for (int i = 0; i < this.getCmdBytesLength(); i++) {
         cmdBytes[i] = REPORT_SCANNER_CMD[i];
      }

      return cmdBytes;
   }

   public byte[] getConfigJan13TwoLabelScannerCmdBytes(byte[] config) {
      byte[] cmdBytes = CONFIG_JAN13_TWO_LABEL_SCANNER_CMD;

      for (int i = this.getCmdBytesLength(); i < config.length; i++) {
         cmdBytes[i] = config[i - this.getCmdBytesLength()];
      }

      return cmdBytes;
   }

   public byte[] getReportJan13TwoLabelScannerCmdBytes() {
      byte[] cmdBytes = new byte[this.getCmdBytesLength()];

      for (int i = 0; i < this.getCmdBytesLength(); i++) {
         cmdBytes[i] = REPORT_JAN13_TWO_LABEL_SCANNER_CMD[i];
      }

      return cmdBytes;
   }

   public byte[] getDirectIOScannerCmdBytes() {
      byte[] cmdBytes = new byte[this.getCmdBytesLength()];

      for (int i = 0; i < this.getCmdBytesLength(); i++) {
         cmdBytes[i] = DIRECTIO_SCANNER_CMD[i];
      }

      return cmdBytes;
   }
}
