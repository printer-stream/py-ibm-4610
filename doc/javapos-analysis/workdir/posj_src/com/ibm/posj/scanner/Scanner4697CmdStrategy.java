package com.ibm.posj.scanner;

import com.ibm.posj.ScannerConfig;
import com.ibm.posj.util.PosjUtil;

public class Scanner4697CmdStrategy extends DefaultScannerCmdStrategy {
   Commmon4696_4697Utility commonUtility = null;
   public static final int SCN_4697_CONFIG_IN_RESPONSE_BYTE_POS = 0;
   public static final int SCN_4697_CONFIG_IN_RESPONSE_BIT_POS = 1;
   public static final int SCN_4697_STATUS_BYTES_LENGTH = 3;

   public int getStatusBytesLength() {
      return 3;
   }

   public int getCmdBytesLength() {
      return 1;
   }

   public boolean isDataInResponse(byte[] status) {
      return status.length > this.getStatusBytesLength() && !this.isConfigInResponse(status);
   }

   public boolean isConfigInResponse(byte[] status) {
      return PosjUtil.isBitSelected(status[this.getConfigInResponseBytePos()], this.getConfigInResponseBitPos());
   }

   public boolean isHwError(byte[] status) {
      return false;
   }

   public boolean isCmdRejected(byte[] status) {
      return PosjUtil.isBitSelected(status[this.getCmdRejectedBytePos()], this.getCmdRejectedBitPos());
   }

   public boolean configSucceed(byte[] status) {
      return PosjUtil.isBitSelected(status[2], 0);
   }

   public boolean isConfigCoerced(byte[] status) {
      return false;
   }

   public byte[] getScannerConfigurationBytes(ScannerConfig scannerConfig) {
      return this.getCommonUtility().getScannerConfigBytes(scannerConfig);
   }

   public ScannerConfig getScannerConfigObject(byte[] configBytes) {
      return this.getCommonUtility().getScannerConfigObject(configBytes);
   }

   protected int getCmdRejectedBytePos() {
      return this.cmdRejectedBytePosition;
   }

   protected int getCmdRejectedBitPos() {
      return this.cmdRejectedBitPosition;
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
      return 0;
   }

   protected int getConfigInResponseBitPos() {
      return 1;
   }

   protected Commmon4696_4697Utility getCommonUtility() {
      if (this.commonUtility == null) {
         this.commonUtility = new Commmon4696_4697Utility();
      }

      return this.commonUtility;
   }
}
