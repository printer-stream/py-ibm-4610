package com.ibm.posj.bus.rs232;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.ScannerCmd;
import com.ibm.posj.ScannerConfig;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Rs232IntegratedLineScannerInfoHelper {
   private List configCommandsList = new ArrayList();
   private List previousConfigCommandsList = new ArrayList();
   private Tracer tracer = TracerFactory.getInstance().createTracer("Scanner", "Rs232IntegratedLineScannerInfoHelper");
   private static Rs232IntegratedLineScannerInfoHelper instance = null;
   public static final String IS_ENABLED_STRING = "isEnabled";
   public static final String CONFIG_STRING = "CONFIG_";
   public static final String ENABLED = "_ENABLED";
   public static final String DISABLED = "_DISABLED";
   public static final byte CODEMARK_UPCA = 65;
   public static final byte CODEMARK_UPCE = 69;
   public static final byte CODEMARK_EAN8 = -1;
   public static final byte CODEMARK_EAN13 = 70;
   public static final byte CODEMARK_CODE39 = 42;
   public static final byte CODEMARK_INTERLEAVED2OF5 = 73;
   public static final byte CODEMARK_STANDARD2OF5 = 72;
   public static final byte CODEMARK_CODABAR = 66;
   public static final byte CODEMARK_CODE93 = 67;
   public static final byte CODEMARK_CODE128 = 68;
   public static final byte CODEMARK_RSS14 = 82;
   public static final byte CODEMARK_RSS_EXPANDED = 83;
   public static final byte UPCA_LENGTH = 12;
   public static final byte UPCE_LENGTH = 8;
   public static final byte EAN8_LENGTH = 8;
   public static final byte EAN13_LENGTH = 13;
   public static final byte[] CHECK_UPCA_ENABLED = new byte[]{75, 64};
   public static final byte[] CONFIG_UPCA_ENABLED = new byte[]{75, 64, 1, 75, 72, 65};
   public static final byte[] CONFIG_UPCA_DISABLED = new byte[]{75, 64, 0};
   public static final byte[] CONFIG_UPCA_CODEMARK = new byte[]{75, 72, 65};
   public static final byte[] CONFIG_UPCE_ENABLED = new byte[]{75, 65, 1, 75, 73, 69};
   public static final byte[] CONFIG_UPCE_DISABLED = new byte[]{75, 65, 0};
   public static final byte[] CONFIG_UPCE_CODEMARK = new byte[]{75, 73, 69};
   public static final byte[] CONFIG_EAN8_ENABLED = new byte[]{75, 66, 1, 75, 74, -1};
   public static final byte[] CONFIG_EAN8_DISABLED = new byte[]{75, 66, 0};
   public static final byte[] CONFIG_EAN8_CODEMARK = new byte[]{75, 74, -1};
   public static final byte[] CONFIG_EAN13_ENABLED = new byte[]{75, 67, 1, 75, 75, 70};
   public static final byte[] CONFIG_EAN13_DISABLED = new byte[]{75, 67, 0};
   public static final byte[] CONFIG_EAN13_CODEMARK = new byte[]{75, 75, 70};
   public static final byte[] CONFIG_CODE39_ENABLED = new byte[]{66, 64, 1, 66, 72, 42, 66, 76, 0};
   public static final byte[] CONFIG_CODE39_DISABLED = new byte[]{66, 64, 0};
   public static final byte[] CONFIG_CODE39_CODEMARK = new byte[]{66, 72, 42};
   public static final byte[] CONFIG_INTERLEAVED2OF5_ENABLED = new byte[]{68, 64, 1, 68, 72, 73, 68, 76, 0};
   public static final byte[] CONFIG_INTERLEAVED2OF5_DISABLED = new byte[]{68, 64, 0};
   public static final byte[] CONFIG_INTERLEAVED2OF5_CODEMARK = new byte[]{68, 72, 73};
   public static final byte[] CONFIG_STANDARD2OF5_ENABLED = new byte[]{72, 64, 1, 72, 72, 72, 72, 83, 0, 72, 80, 0};
   public static final byte[] CONFIG_STANDARD2OF5_DISABLED = new byte[]{72, 64, 0};
   public static final byte[] CONFIG_STANDARD2OF5_CODEMARK = new byte[]{72, 72, 72};
   public static final byte[] CONFIG_CODABAR_ENABLED = new byte[]{64, 64, 1, 64, 72, 66, 64, 88, 1};
   public static final byte[] CONFIG_CODABAR_DISABLED = new byte[]{64, 64, 0};
   public static final byte[] CONFIG_CODABAR_CODEMARK = new byte[]{64, 72, 66};
   public static final byte[] CONFIG_CODE93_ENABLED = new byte[]{65, 64, 1, 65, 72, 67};
   public static final byte[] CONFIG_CODE93_DISABLED = new byte[]{65, 64, 0};
   public static final byte[] CONFIG_CODE93_CODEMARK = new byte[]{65, 72, 67};
   public static final byte[] CONFIG_RSS14_ENABLED = new byte[]{79, 64, 1, 79, 72, 82};
   public static final byte[] CONFIG_RSS14_DISABLED = new byte[]{79, 64, 0};
   public static final byte[] CONFIG_RSS14_CODEMARK = new byte[]{79, 72, 82};
   public static final byte[] CONFIG_RSS_EXPANDED_ENABLED = new byte[]{79, 66, 1, 79, 74, 83};
   public static final byte[] CONFIG_RSS_EXPANDED_DISABLED = new byte[]{79, 66, 0};
   public static final byte[] CONFIG_RSS_EXPANDED_CODEMARK = new byte[]{79, 74, 83};
   public static final byte[] _CONFIG_CODE128_ENABLED = new byte[]{67, 64, 1, 67, 72, 68, 67, 88, 1};
   public static final byte[] _CONFIG_CODE128_DISABLED = new byte[]{67, 64, 0};
   public static final byte[] CONFIG_CODE128_CODEMARK = new byte[]{67, 72, 68};
   public static final byte[] CONFIG_UPCA_A_CHECKDIGIT_ENABLED = new byte[]{75, 84, 1};
   public static final byte[] CONFIG_UPCA_A_CHECKDIGIT_DISABLED = new byte[]{75, 84, 0};
   public static final byte[] CONFIG_UPC_E_CHECKDIGIT_ENABLED = new byte[]{75, 85, 1};
   public static final byte[] CONFIG_UPC_E_CHECKDIGIT_DISABLED = new byte[]{75, 85, 0};
   public static final byte[] CONFIG_UPC_A_TO_EAN13EXPANSION_ENABLED = new byte[]{75, 90, 1};
   public static final byte[] CONFIG_UPC_A_TO_EAN13EXPANSION_DISABLED = new byte[]{75, 90, 0};
   public static final byte[] CONFIG_UPC_E_TO_UPC_AEXPANSION_ENABLED = new byte[]{75, 91, 1};
   public static final byte[] CONFIG_UPC_E_TO_UPC_AEXPANSION_DISABLE = new byte[]{75, 91, 0};
   public static final byte[] CONFIG_GOODREADBEEP_ENABLED = new byte[]{114, 65, 1};
   public static final byte[] CONFIG_GOODREADBEEP_DISABLED = new byte[]{114, 65, 0};
   public static final byte[] CONFIG_2DIGIT_SUPLEMENTALS_ENABLED = new byte[]{75, 69, 1, 75, 93, 0};
   public static final byte[] CONFIG_2DIGIT_SUPLEMENTALS_DISABLED = new byte[]{75, 69, 0};
   public static final byte[] CONFIG_5DIGIT_SUPLEMENTALS_ENABLED = new byte[]{75, 70, 1, 75, 93, 0};
   public static final byte[] CONFIG_5DIGIT_SUPLEMENTALS_DISABLED = new byte[]{75, 70, 0};
   public static final byte[] CONFIG_UPCAE_EANJAN813_ENABLED = new byte[]{
      75, 64, 1, 75, 72, 65, 75, 65, 1, 75, 73, 69, 75, 66, 1, 75, 74, -1, 75, 67, 1, 75, 75, 70
   };
   public static final byte[] CONFIG_UPCAE_EANJAN813_DISABLED = new byte[]{75, 64, 0, 75, 65, 0, 75, 66, 0, 75, 67, 0};
   public static final byte[] CONFIG_ITF_LENGTHS_RANGE = new byte[]{68, 83, 0};
   public static final byte[] CONFIG_ITF_LENGTHS_DISCRETE = new byte[]{68, 83, 1};
   public static final byte[] CONFIG_STF_LENGTHS_RANGE = new byte[]{72, 83, 0};
   public static final byte[] CONFIG_STF_LENGTHS_DISCRETE = new byte[]{72, 83, 1};
   public static final byte[] BEEP_LENGTH_SHORT_DURATION = new byte[]{114, -127, 0, 50};
   public static final byte[] BEEP_LENGTH_MEDIUM_DURATION = new byte[]{114, -127, 0, -56};
   public static final byte[] BEEP_LENGTH_LONG_DURATION = new byte[]{114, -127, 1, -12};
   public static final byte[] BEEP_LENGTH_LONGEST_DURATION = new byte[]{114, -127, 3, -24};
   public static final byte[] BEEP_FREQUENCY_LOWEST_FREQUENCY = new byte[]{114, -128, 3, -24};
   public static final byte[] BEEP_FREQUENCY_LOW_FREQUENCY = new byte[]{114, -128, 7, -16};
   public static final byte[] BEEP_FREQUENCY_MEDIUM_FREQUENCY = new byte[]{114, -128, 11, -9};
   public static final byte[] BEEP_FREQUENCY_HIGH_FREQUENCY = new byte[]{114, -128, 15, -1};
   public static byte[] CONFIG_SUPPLEMANTALS_SECURITY_LEVEL = new byte[]{75, 71, 0};
   public static byte[] CONFIG_ITF_LENGTH1 = new byte[]{68, 80, 0};
   public static byte[] CONFIG_ITF_LENGTH2 = new byte[]{68, 81, 0};
   public static byte[] CONFIG_STF_LENGTH1 = new byte[]{72, 80, 0};
   public static byte[] CONFIG_STF_LENGTH2 = new byte[]{72, 81, 0};
   public static final byte[] ENABLE_PROGRAMMING_VIA_BARCODES = new byte[]{116, 64, 0};
   public static final byte[] DISABLE_PROGRAMMING_VIA_BARCODES = new byte[]{116, 64, 2};
   private static final byte TYPE_BYTE = 0;
   public static final byte SR_BYTE = 64;
   public static final byte SW_BYTE = 65;
   public static final byte CCMD_BYTE = 66;
   public static final byte RESULT_TYPE = 81;
   public static final byte BARCODE_TYPE = 96;
   private static final byte RESULT_ID_BYTE = 1;
   private static final byte RESULT_DONE = 0;
   private static final byte[] TEST_REQUEST_CMD_BYTES = null;
   private static final byte[] STATUS_REQUEST_CMD_BYTES = null;
   private static final byte[] RESET_REQUEST_CMD_BYTES = new byte[]{66, 48, 2};
   private static final byte[] ENABLE_SCANNER_CMD_BYTES = new byte[]{66, 32, 64, 1};
   private static final byte[] DISABLE_SCANNER_CMD_BYTES = new byte[]{66, 32, 64, 0};
   private static final byte[] ENABLE_BEEPER_SCANNER_CMD_BYTES = new byte[]{65, 114, 65, 1};
   private static final byte[] DISABLE_BEEPER_SCANNER_CMD_BYTES = new byte[]{65, 114, 65, 0};
   private static final byte[] REPORT_SCANNER_CMD_BYTES = null;
   private static final byte[] CONFIG_HEADER = new byte[]{65};
   private static final byte[] INTEGRATED_LINE_SCANNER_SET_ISCP = new byte[]{97, 64, 1};
   private static final byte[] INTEGRATED_LINE_SCANNER_SET_ISCP_PACKET_FORMAT = new byte[]{115, 64, 1};
   private static final byte[] INTEGRATED_LINE_SCANNER_SET_POWER_HOLD = new byte[]{112, 71, 1};
   private static final byte[] INTEGRATED_LINE_SCANNER_SET_TRIGGER_CONTINUOUS = new byte[]{112, 64, 0};
   private static final byte[] INTEGRATED_LINE_SCANNER_SET_SYMBOLOGY_IDENTIFIER = new byte[]{96, 64, 1};
   private static final byte[] INTEGRATED_LINE_SCANNER_SET_NO_POSTAMBLE = new byte[]{96, -63, 0, 0};
   private static final byte MAXIMUM_SUPPLEMENTAL_SECURITY_LEVEL = 100;
   private static final byte MINIMUM_SUPPLEMENTAL_SECURITY_LEVEL = 0;

   private Rs232IntegratedLineScannerInfoHelper() {
   }

   public static Rs232IntegratedLineScannerInfoHelper getInstance() {
      if (instance == null) {
         instance = new Rs232IntegratedLineScannerInfoHelper();
      }

      return instance;
   }

   public boolean isResultOk(byte[] data) {
      return data[0] == 81 && data[1] == 0;
   }

   public boolean isBarcodeData(byte[] data) {
      return data[0] == 96;
   }

   public byte[] getTestRequestCmdBytes() {
      return TEST_REQUEST_CMD_BYTES;
   }

   public byte[] getStatusRequestCmdBytes() {
      return STATUS_REQUEST_CMD_BYTES;
   }

   public byte[] getResetRequestCmdBytes() {
      return RESET_REQUEST_CMD_BYTES;
   }

   public byte[] getEnableScannerCmdBytes() {
      return ENABLE_SCANNER_CMD_BYTES;
   }

   public byte[] getDisableScannerCmdBytes() {
      return DISABLE_SCANNER_CMD_BYTES;
   }

   public byte[] getEnableBeeperScannerCmdBytes() {
      return ENABLE_BEEPER_SCANNER_CMD_BYTES;
   }

   public byte[] getDisableBeeperScannerCmdBytes() {
      return DISABLE_BEEPER_SCANNER_CMD_BYTES;
   }

   public byte[] getConfigScannerCmdBytes(ScannerCmd.ConfigScannerCmd cmd) {
      return this.generateConfig(cmd.getConfig());
   }

   public byte[] getReportScannerCmdBytes() {
      return REPORT_SCANNER_CMD_BYTES;
   }

   public byte[] getConfigJan13TwoLabelScannerCmdBytes(byte[] config) {
      return null;
   }

   public byte[] getReportJan13TwoLabelScannerCmdBytes() {
      return null;
   }

   public byte[] getDirectIOScannerCmdBytes() {
      return null;
   }

   public List getConfigCommandsList() {
      return this.configCommandsList;
   }

   public boolean equalThatPreviousConfig() {
      Iterator it1 = this.previousConfigCommandsList.iterator();
      Iterator it2 = this.configCommandsList.iterator();

      while (it1.hasNext() && it2.hasNext()) {
         String s1 = new String((byte[])it1.next());
         String s2 = new String((byte[])it2.next());
         if (!s1.equals(s2)) {
            return false;
         }
      }

      if (it1.hasNext()) {
         return it1.next() == null;
      } else {
         return it2.hasNext() ? it2.next() == null : true;
      }
   }

   public void resetConfigCommandsList() {
      this.previousConfigCommandsList.clear();
      this.previousConfigCommandsList.addAll(this.configCommandsList);
      this.configCommandsList.clear();
   }

   private byte[] generateConfig(ScannerConfig config) {
      this.generateHeader();
      this.generateIsEnabledConfigs(config);
      this.generateRemainingConfigs(config);
      return null;
   }

   private void generateHeader() {
      this.addConfigCmd(INTEGRATED_LINE_SCANNER_SET_ISCP);
      this.addConfigCmd(INTEGRATED_LINE_SCANNER_SET_ISCP_PACKET_FORMAT);
      this.addConfigCmd(INTEGRATED_LINE_SCANNER_SET_SYMBOLOGY_IDENTIFIER);
      this.addConfigCmd(INTEGRATED_LINE_SCANNER_SET_NO_POSTAMBLE);
      this.addConfigCmd(INTEGRATED_LINE_SCANNER_SET_POWER_HOLD);
      this.addConfigCmd(INTEGRATED_LINE_SCANNER_SET_TRIGGER_CONTINUOUS);
   }

   private void generateRemainingConfigs(ScannerConfig config) {
      this.setCode128(config.isEnabledCode128() || config.isEnabledUCC_EAN128());
      this.setBeeperDuration(config.getBeeperDuration());
      this.setBeeperFrequency(config.getBeeperFrequency());
      this.setITFLengths(config.isITFLengths());
      this.setITFLength1(config.getITFLength1());
      this.setITFLength2(config.getITFLength2());
      this.setSTFLengths(config.isSTFLengths());
      this.setSTFLength1(config.getSTFLength1());
      this.setSTFLength2(config.getSTFLength2());
      this.setSupplementalsSecurityLevel(config.getSupplementalsSecurityLevel());
      this.setProgrammingViaBarCodes(config.isEnabledProgramingViaBarcodes() || config.isEnabledBarCodeProgramming());
   }

   private void setSupplementalsSecurityLevel(byte securityLevel) {
      if (securityLevel > 100) {
         securityLevel = 100;
      } else if (securityLevel < 0) {
         securityLevel = 0;
      }

      CONFIG_SUPPLEMANTALS_SECURITY_LEVEL[CONFIG_SUPPLEMANTALS_SECURITY_LEVEL.length - 1] = securityLevel;
      this.addConfigCmd(CONFIG_SUPPLEMANTALS_SECURITY_LEVEL);
   }

   private void setCode128(boolean value) {
      if (value) {
         this.addConfigCmd(_CONFIG_CODE128_ENABLED);
      } else {
         this.addConfigCmd(_CONFIG_CODE128_DISABLED);
      }
   }

   private void setProgrammingViaBarCodes(boolean value) {
      if (value) {
         this.addConfigCmd(ENABLE_PROGRAMMING_VIA_BARCODES);
      } else {
         this.addConfigCmd(DISABLE_PROGRAMMING_VIA_BARCODES);
      }
   }

   private void setITFLengths(boolean value) {
      if (value) {
         this.addConfigCmd(CONFIG_ITF_LENGTHS_RANGE);
      } else {
         this.addConfigCmd(CONFIG_ITF_LENGTHS_DISCRETE);
      }
   }

   private void setITFLength1(byte length1) {
      CONFIG_ITF_LENGTH1[CONFIG_ITF_LENGTH1.length - 1] = length1;
      this.addConfigCmd(CONFIG_ITF_LENGTH1);
   }

   private void setITFLength2(byte length2) {
      CONFIG_ITF_LENGTH2[CONFIG_ITF_LENGTH2.length - 1] = length2;
      this.addConfigCmd(CONFIG_ITF_LENGTH2);
   }

   private void setSTFLengths(boolean value) {
      if (value) {
         this.addConfigCmd(CONFIG_STF_LENGTHS_RANGE);
      } else {
         this.addConfigCmd(CONFIG_STF_LENGTHS_DISCRETE);
      }
   }

   private void setSTFLength1(byte length1) {
      CONFIG_STF_LENGTH1[CONFIG_STF_LENGTH1.length - 1] = length1;
      this.addConfigCmd(CONFIG_STF_LENGTH1);
   }

   private void setSTFLength2(byte length2) {
      CONFIG_STF_LENGTH2[CONFIG_STF_LENGTH2.length - 1] = length2;
      this.addConfigCmd(CONFIG_STF_LENGTH2);
   }

   private void setBeeperFrequency(byte beeperFrequency) {
      switch (beeperFrequency) {
         case 0:
            this.addConfigCmd(BEEP_FREQUENCY_LOWEST_FREQUENCY);
            break;
         case 1:
         case 8:
            this.addConfigCmd(BEEP_FREQUENCY_LOW_FREQUENCY);
            break;
         case 2:
         case 16:
            this.addConfigCmd(BEEP_FREQUENCY_MEDIUM_FREQUENCY);
            break;
         case 3:
         case 24:
            this.addConfigCmd(BEEP_FREQUENCY_HIGH_FREQUENCY);
         case 4:
         case 5:
         case 6:
         case 7:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
      }
   }

   private void setBeeperDuration(byte beeperDuration) {
      switch (beeperDuration) {
         case 0:
            this.addConfigCmd(BEEP_LENGTH_SHORT_DURATION);
            break;
         case 1:
         case 32:
            this.addConfigCmd(BEEP_LENGTH_MEDIUM_DURATION);
            break;
         case 2:
         case 64:
            this.addConfigCmd(BEEP_LENGTH_LONG_DURATION);
            break;
         case 3:
         case 96:
            this.addConfigCmd(BEEP_LENGTH_LONGEST_DURATION);
      }
   }

   private void generateIsEnabledConfigs(ScannerConfig config) {
      String methodName = null;
      String constantName = null;
      Class configClass = config.getClass();
      Method[] methods = configClass.getMethods();

      for (int i = 0; i < methods.length; i++) {
         if (methods[i].getReturnType().equals(boolean.class)) {
            methodName = methods[i].getName();
            if (methodName.startsWith("isEnabled")) {
               try {
                  if (methods[i].getParameterTypes().length == 0) {
                     constantName = "CONFIG_" + methodName.substring("isEnabled".length(), methodName.length()).toUpperCase();
                     if ((Boolean)methods[i].invoke(config, null)) {
                        constantName = constantName + "_ENABLED";
                     } else {
                        constantName = constantName + "_DISABLED";
                     }
                  }

                  try {
                     this.addConfigCmd((byte[])this.getClass().getField(constantName).get(null));
                  } catch (SecurityException var8) {
                     if (this.tracer.isOn()) {
                        this.tracer.println(3, var8);
                     }
                  } catch (NoSuchFieldException var9) {
                     if (this.tracer.isOn()) {
                        this.tracer.println(3, var9);
                     }
                  }
               } catch (IllegalArgumentException var10) {
                  if (this.tracer.isOn()) {
                     this.tracer.println(3, var10);
                  }
               } catch (IllegalAccessException var11) {
                  if (this.tracer.isOn()) {
                     this.tracer.println(3, var11);
                  }
               } catch (InvocationTargetException var12) {
                  if (this.tracer.isOn()) {
                     this.tracer.println(3, var12);
                  }
               }
            }
         }
      }
   }

   private void addCmd(byte[] cmd) {
      this.configCommandsList.add(cmd);
   }

   private void addConfigCmd(byte[] cmd) {
      byte[] temp = new byte[cmd.length + 1];
      temp[0] = 65;
      System.arraycopy(cmd, 0, temp, 1, cmd.length);
      this.addCmd(temp);
   }

   private void addControlCmd(byte[] cmd) {
      byte[] temp = new byte[cmd.length + 1];
      temp[0] = 66;
      System.arraycopy(cmd, 0, temp, 1, cmd.length);
      this.addCmd(temp);
   }
}
