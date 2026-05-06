package com.ibm.posj.scale;

import com.ibm.jutil.Util;
import com.ibm.posj.HandleException;
import com.ibm.posj.ScaleConfig;
import com.ibm.posj.util.PosjUtil;

public class ScaleDataParser {
   protected byte[] scaleData = null;
   byte statusLen = 0;
   byte scaleType = 0;
   public static final int EXT_STATUS_LEN = 3;
   public static final int STATUS_LEN = 2;
   public static final byte FIRST_STATUS_BYTE = 0;
   public static final byte SECOND_STATUS_BYTE = 1;
   public static final byte THIRD_STATUS_BYTE = 2;
   public static final byte EC_LEVEL_RESPONSE = 0;
   public static final byte FLASH_IN_PROGRESS_POS = 0;
   public static final byte CONFIG_DATA_RESPONSE_POS = 1;
   public static final byte EXTENDED_STATUS_RESPONSE_POS = 2;
   public static final byte UNACCEPTABLE_COMMAND_POS = 6;
   public static final byte NOT_READY_TO_RECEIVE_CMDS_POS = 7;
   public static final byte WEIGHT_MODE_POS = 0;
   public static final byte WEIGHT_DATA_NOT_INCLUDED_POS = 2;
   public static final byte DATA_VALUE_ERROR_POS = 3;
   public static final byte READ_ERROR_POS = 4;
   public static final byte DISPLAY_REQUIRED_BUT_NOT_DETECTED_POS = 5;
   public static final byte SCALE_HW_ERROR_POS = 6;
   public static final byte COMMAND_REJECTED_POS = 7;
   public static final byte CONFIG_SUCCESSFUL_POS = 0;
   public static final byte SCALE_UNDER_ZERO_POS = 1;
   public static final byte SCALE_OVER_CAPACITY_POS = 2;
   public static final byte SCALE_CENTER_OF_ZERO_POS = 3;
   public static final byte SCALE_REQUIRES_ZEROING_POS = 4;
   public static final byte SCALE_WARMUP_IN_PROGRESS_POS = 5;
   public static final byte DUPLICATE_WEIGHT_POS = 6;

   public ScaleDataParser() {
   }

   public ScaleDataParser(int scaleType) {
      this.scaleType = (byte)scaleType;
   }

   public void parse(byte[] scaleData) throws HandleException {
      if (scaleData.length < 2) {
         throw new HandleException("status lenght not valid");
      } else {
         this.setScaleData(scaleData);
         if (this.extendedStatus()) {
            this.statusLen = 3;
         } else {
            this.statusLen = 2;
         }
      }
   }

   public byte[] getData() {
      int dataLenght = this.scaleData.length - this.statusLen;
      byte[] data = new byte[dataLenght];
      System.arraycopy(this.scaleData, this.statusLen, data, 0, dataLenght);
      return data;
   }

   public byte[] getStatus() {
      byte[] status = new byte[this.statusLen];
      System.arraycopy(this.scaleData, 0, status, 0, this.statusLen);
      return status;
   }

   public int getWeight() {
      int weight = 0;
      int factor = 1;
      int tmp = 0;

      for (int i = this.scaleData.length - 1; i >= this.statusLen; i--) {
         tmp = this.scaleData[i] * factor;
         weight += tmp;
         factor *= 10;
      }

      if (this.scaleData.length - this.statusLen == 4) {
         weight *= 10;
      }

      return weight;
   }

   public void setScaleData(byte[] ScaleData) throws HandleException {
      if (ScaleData == null) {
         throw new HandleException("null byte array received");
      } else {
         this.scaleData = ScaleData;
      }
   }

   public boolean flashInProgress() {
      return PosjUtil.isBitSelected(this.scaleData[0], 0);
   }

   public boolean isECLevelResponse() {
      return PosjUtil.isBitSelected(this.scaleData[0], 0);
   }

   public boolean isConfigData() {
      return PosjUtil.isBitSelected(this.scaleData[0], 1);
   }

   public boolean extendedStatus() {
      return PosjUtil.isBitSelected(this.scaleData[0], 2);
   }

   public boolean unacceptableCommand() {
      return PosjUtil.isBitSelected(this.scaleData[0], 6);
   }

   public boolean notReadyforWeighing() {
      return PosjUtil.isBitSelected(this.scaleData[0], 7);
   }

   public boolean englishMode() {
      return !PosjUtil.isBitSelected(this.scaleData[1], 0);
   }

   public boolean containsWeight() {
      return !PosjUtil.isBitSelected(this.scaleData[1], 2);
   }

   public boolean dataValueError() {
      return PosjUtil.isBitSelected(this.scaleData[1], 3);
   }

   public boolean readError() {
      return PosjUtil.isBitSelected(this.scaleData[1], 4);
   }

   public boolean displayRequiredAbsent() {
      return PosjUtil.isBitSelected(this.scaleData[1], 5);
   }

   public boolean hardwareError() {
      return PosjUtil.isBitSelected(this.scaleData[1], 6);
   }

   public boolean cmdRejected() {
      return PosjUtil.isBitSelected(this.scaleData[1], 7);
   }

   public boolean configSuccessful() {
      return !this.extendedStatus() && this.scaleData.length < 3 ? false : PosjUtil.isBitSelected(this.scaleData[2], 0);
   }

   public boolean underZero() {
      return !this.extendedStatus() ? false : PosjUtil.isBitSelected(this.scaleData[2], 1);
   }

   public boolean overCapacity() {
      return !this.extendedStatus() ? false : PosjUtil.isBitSelected(this.scaleData[2], 2);
   }

   public boolean centerOfZero() {
      return !this.extendedStatus() ? false : PosjUtil.isBitSelected(this.scaleData[2], 3);
   }

   public boolean zeroingRequired() {
      return !this.extendedStatus() ? false : PosjUtil.isBitSelected(this.scaleData[2], 4);
   }

   public boolean warmupInProgress() {
      return !this.extendedStatus() ? false : PosjUtil.isBitSelected(this.scaleData[2], 5);
   }

   public boolean duplicateWeight() {
      return !this.extendedStatus() ? false : PosjUtil.isBitSelected(this.scaleData[2], 6);
   }

   public ScaleConfig getConfig() {
      ScaleConfig config = null;
      if (this.isConfigData()) {
         config = new ScaleConfig(this.getData());
      }

      return config;
   }

   public String toString() {
      return "ScaleData = "
         + Util.toFormatedHexString(this.scaleData)
         + "status = "
         + Util.toFormatedHexString(this.getStatus())
         + "data = "
         + Util.toFormatedHexString(this.getData());
   }
}
