package com.ibm.posj.bus.rs232;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.ScannerConfig;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Rs232IntegratedOmniScannerInfoHelper {
   private ArrayList listOfConfigPackets = new ArrayList();
   private byte[] allParams = null;
   private boolean configListAlreadyCreated = false;
   private Tracer tracer = TracerFactory.getInstance().createTracer("Scanner", "Rs232IntegratedOmniScannerInfoHelper");
   private static Rs232IntegratedOmniScannerInfoHelper instance = null;
   public static final String REFLECTION_IS_ENABLED_STRING = "isEnabled";
   public static final String REFLECTION_CONFIG_STRING = "CONFIG_";
   public static final byte BEEPER_NO_BEEP = -1;
   public static final byte OPCODE_DECODE_DATA = -13;
   public static final byte OPCODE_PARAM_SEND = -58;
   public static final byte OPCODE_PARAM_DEFAULTS = -56;
   public static final byte OPCODE_PARAM_REQUEST = -57;
   public static final byte OPCODE_EVENT = -10;
   public static final byte OPCODE_BATCH_DATA = -42;
   public static final byte OPCODE_CAPABILITIES_REPLY = -44;
   public static final byte OPCODE_REPLY_REVISION = -92;
   public static final byte OPCODE_ACK = -48;
   public static final byte OPCODE_NAK = -47;
   public static final byte OPCODE_START_SESSION = -28;
   public static final byte OPCODE_STOP_SESSION = -27;
   public static final byte NACK_RESEND = 1;
   public static final byte NACK_BAD_CONTEXT = 2;
   public static final byte NACK_DENIED = 6;
   public static final byte NACK_CANCEL = 10;
   public static final byte BARCODE_TYPE_UPCA = 8;
   public static final byte BARCODE_TYPE_UPCA_2 = 72;
   public static final byte BARCODE_TYPE_UPCA_5 = -120;
   public static final byte BARCODE_TYPE_UPCE = 9;
   public static final byte BARCODE_TYPE_UPCE_2 = 73;
   public static final byte BARCODE_TYPE_UPCE_5 = -119;
   public static final byte BARCODE_TYPE_EAN8 = 10;
   public static final byte BARCODE_TYPE_EAN8_2 = 74;
   public static final byte BARCODE_TYPE_EAN8_5 = -118;
   public static final byte BARCODE_TYPE_EAN13 = 11;
   public static final byte BARCODE_TYPE_EAN13_2 = 75;
   public static final byte BARCODE_TYPE_EAN13_5 = -117;
   public static final byte BARCODE_TYPE_EAN128 = 15;
   public static final byte BARCODE_TYPE_CODE39 = 1;
   public static final byte BARCODE_TYPE_CODABAR = 2;
   public static final byte BARCODE_TYPE_CODE93 = 7;
   public static final byte BARCODE_TYPE_CODE128 = 3;
   public static final byte BARCODE_TYPE_PDF417 = 17;
   public static final byte BARCODE_TYPE_RSS14 = 48;
   public static final byte BARCODE_TYPE_RSS_EXPANDED = 50;
   public static final byte BARCODE_TYPE_INTERLEAVED2OF5 = 6;
   public static final byte BARCODE_TYPE_STANDARD2OF5 = 4;
   public static final byte BARCODE_TYPE_MULTIPACKET_FORMAT = -103;
   public static final byte ENABLE_PARAMETER = 1;
   public static final byte DISABLE_PARAMETER = 0;
   public static final byte PARAM_BEEPER_TONE = -111;
   private static final byte CODE39_CHECKDIGIT_VERIFICATION = 48;
   private static final byte CODE39_TX_CHECKDIGIT = 43;
   private static final byte ITF_CHECKDIGIT_VERIFICATION = 49;
   private static final byte ITF_TX_CHECKDIGIT = 44;
   public static final byte CONFIG_CODABAR = 7;
   public static final byte CONFIG_CODE128 = 8;
   public static final byte[] CONFIG_CODE39 = new byte[]{48, 0, 43, 0, 0};
   public static final byte CONFIG_CODE93 = 9;
   public static final byte CONFIG_GOODREADBEEP = 56;
   public static final byte[] CONFIG_INTERLEAVED2OF5 = new byte[]{49, 0, 44, 0, 6};
   public static final byte CONFIG_ITF_CHECKDIGIT = 44;
   public static final byte CONFIG_PDF417 = 15;
   public static final byte[] CONFIG_RSS14 = new byte[]{-16, 82};
   public static final byte[] CONFIG_RSS_EXPANDED = new byte[]{-16, 84};
   public static final byte CONFIG_STANDARD2OF5 = 5;
   public static final byte CONFIG_UCC_EAN128 = 14;
   public static final byte CONFIG_UPC_E_CHECKDIGIT = 41;
   public static final byte CONFIG_UPC_E_TO_UPC_AEXPANSION = 37;
   public static final byte CONFIG_UPCA_A_CHECKDIGIT = 40;
   public static final byte CONFIG_UPCA = 1;
   public static final byte CONFIG_UPCE = 2;
   public static final byte CONFIG_EAN8 = 4;
   public static final byte CONFIG_EAN13 = 3;
   public static final byte SET_SCANNING_MODE = -115;
   public static final byte SCANNING_MODE_SMART_RASTER = 1;
   public static final byte SCANNING_MODE_OMNIDIRECTIONAL = 6;
   public static final byte SCANNING_MODE_SEMI_OMNIDIRECTIONAL = 7;
   public static final byte SET_TRIGGERING_MODE = -118;
   public static final byte TRIGGERING_MODE_LEVEL = 0;
   public static final byte TRIGGERING_MODE_PULSE = 2;
   public static final byte TRIGGERING_MODE_CONTINUOUS = 4;
   public static final byte TRIGGERING_MODE_HOST = 8;
   public static final byte SET_PACKETED_DECODE_DATA = -18;
   public static final byte SET_PROGRAMINGVIABARCODES = -20;
   public static final byte SET_STFL1 = 20;
   public static final byte SET_STFL2 = 21;
   public static final byte SET_ITFL1 = 22;
   public static final byte SET_ITFL2 = 23;
   public static final byte SET_UPC_EAN_SUPPLEMENTALS = 16;
   public static final byte SUP_IGNORE = 0;
   public static final byte SUP_AUTODISCRIMINATE = 2;
   public static final byte SET_UPC_EAN_SUPPLEMENTAL_SECURITY_LEVEL = 80;
   private static final byte SUPPLEMENTAL_SECURITY_LEVEL_DIVISOR = 5;
   private static final byte MAXIMUM_SUPPLEMENTAL_SECURITY_LEVEL = 20;
   private static final byte MINIMUM_SUPPLEMENTAL_SECURITY_LEVEL = 2;
   private static final byte BEEP_FREQUENCY_LOW_FREQUENCY = 2;
   private static final byte BEEP_FREQUENCY_MEDIUM_FREQUENCY = 1;
   private static final byte BEEP_FREQUENCY_HIGH_FREQUENCY = 0;
   private static final byte PARAM_REQUEST_REQUEST_ALL_PARAMS = -2;

   private Rs232IntegratedOmniScannerInfoHelper() {
   }

   private void generateBaseConfig(ScannerConfig config) {
      this.listOfConfigPackets.add(Rs232IntegratedOmniScannerPacketManager.getInstance().createParameterPacket((byte)-18, (byte)1));
      this.listOfConfigPackets.add(Rs232IntegratedOmniScannerPacketManager.getInstance().createParameterPacket((byte)-118, (byte)8));
      if (this.tracer.isOn()) {
         this.listOfConfigPackets.add(Rs232IntegratedOmniScannerPacketManager.getInstance().createPacket((byte)-57, (byte)-2));
      }
   }

   private void generateIsEnabledConfigs(ScannerConfig config) {
      Method[] methods = (class$com$ibm$posj$ScannerConfig == null
            ? (class$com$ibm$posj$ScannerConfig = class$("com.ibm.posj.ScannerConfig"))
            : class$com$ibm$posj$ScannerConfig)
         .getDeclaredMethods();
      String constantName = null;
      if (this.tracer.isOn()) {
         this.tracer.println(3, "--> generateIsEnabledConfigs");
      }

      for (int i = 0; i < methods.length; i++) {
         if (methods[i].getReturnType().equals(boolean.class) && methods[i].getName().startsWith("isEnabled")) {
            if (this.tracer.isOn()) {
               this.tracer.println(3, "Creating config packet for: " + methods[i].getName() + " property");
            }

            try {
               constantName = "CONFIG_" + methods[i].getName().substring("isEnabled".length()).toUpperCase();
               if (this.getClass().getField(constantName).getType() == (array$B == null ? (array$B = class$("[B")) : array$B)) {
                  this.addParam((byte[])this.getClass().getField(constantName).get(this), (byte)(methods[i].invoke(config, null) ? 1 : 0));
               } else if (this.getClass().getField(constantName).getType() == byte.class) {
                  this.addParam(this.getClass().getField(constantName).getByte(this), (byte)(methods[i].invoke(config, null) ? 1 : 0));
               }
            } catch (IllegalArgumentException var6) {
               if (this.tracer.isOn()) {
                  this.tracer.println(3, var6);
               }
            } catch (SecurityException var7) {
               if (this.tracer.isOn()) {
                  this.tracer.println(3, var7);
               }
            } catch (IllegalAccessException var8) {
               if (this.tracer.isOn()) {
                  this.tracer.println(3, var8);
               }
            } catch (NoSuchFieldException var9) {
               if (this.tracer.isOn()) {
                  this.tracer.println(3, var9);
               }
            } catch (InvocationTargetException var10) {
               if (this.tracer.isOn()) {
                  this.tracer.println(3, var10);
               }
            }
         }
      }

      if (this.tracer.isOn()) {
         this.tracer.println(3, "<-- generateIsEnabledConfigs");
      }
   }

   private void generateRemainingConfigs(ScannerConfig config) {
      this.setScanMode(config);
      this.setBeeperFrequency(config.getBeeperFrequency());
      this.setProgrammingViaBarCodes(config);
      this.setEnableUPC_EANBarcodeGroup(config.isEnabledUPCAE_EANJAN813());
      this.setUPC_EANSupplementals(config.isEnabledSupplementals());
      this.setITFLengths(config);
      this.setSTFLengths(config);
      this.setSupplementalsSecurityLevel(config.getSupplementalsSecurityLevel());
   }

   private void setBeeperFrequency(byte frequency) {
      switch (frequency) {
         case 0:
         case 1:
         case 8:
            this.addParam((byte)-111, (byte)2);
            break;
         case 2:
         case 16:
            this.addParam((byte)-111, (byte)1);
            break;
         case 3:
         case 24:
            this.addParam((byte)-111, (byte)0);
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

   private void setProgrammingViaBarCodes(ScannerConfig config) {
      byte enableByte = 0;
      if (config.isEnabledProgramingViaBarcodes() || config.isEnabledBarCodeProgramming()) {
         enableByte = 1;
      }

      this.addParam((byte)-20, enableByte);
   }

   private void setEnableUPC_EANBarcodeGroup(boolean enable) {
      byte enabledByte = (byte)(enable ? 1 : 0);
      this.addParam((byte)1, enabledByte);
      this.addParam((byte)2, enabledByte);
      this.addParam((byte)4, enabledByte);
      this.addParam((byte)3, enabledByte);
   }

   private void setITFLengths(ScannerConfig config) {
      byte res1;
      byte l1 = res1 = config.getITFLength1();
      byte res2;
      byte l2 = res2 = config.getITFLength2();
      if (config.isITFLengths()) {
         if (l1 > l2) {
            res1 = l2;
            res2 = l1;
         }
      } else if (l1 < l2) {
         res1 = l2;
         res2 = l1;
      }

      this.addParam((byte)22, res1);
      this.addParam((byte)23, res2);
   }

   private void setSTFLengths(ScannerConfig config) {
      byte res1;
      byte l1 = res1 = config.getSTFLength1();
      byte res2;
      byte l2 = res2 = config.getSTFLength2();
      if (config.isSTFLengths()) {
         if (l1 > l2) {
            res1 = l2;
            res2 = l1;
         }
      } else if (l1 < l2) {
         res1 = l2;
         res2 = l1;
      }

      this.addParam((byte)20, res1);
      this.addParam((byte)21, res2);
   }

   private void setUPC_EANSupplementals(boolean enable) {
      byte configByte = 0;
      if (enable) {
         configByte = 2;
      }

      this.addParam((byte)16, configByte);
   }

   private void setSupplementalsSecurityLevel(byte securityLevel) {
      securityLevel = (byte)(securityLevel / 5);
      if (securityLevel > 20) {
         securityLevel = 20;
      } else if (securityLevel < 2) {
         securityLevel = 2;
      }

      this.addParam((byte)80, securityLevel);
   }

   private void setScanMode(ScannerConfig config) {
      byte scanMode = 6;
      if (config.isEnabledPDF417()) {
         scanMode = 1;
      }

      this.addParam((byte)-115, scanMode);
   }

   private void addParam(byte param, byte value) {
      byte[] aux = new byte[]{param, value};
      this.addParam(aux);
   }

   private void addParam(byte[] param, byte value) {
      byte[] aux = new byte[param.length + 1];
      byte[] valueArray = new byte[]{value};
      System.arraycopy(param, 0, aux, 0, param.length);
      System.arraycopy(valueArray, 0, aux, param.length, valueArray.length);
      this.addParam(aux);
   }

   private void addParam(byte[] param) {
      byte[] aux = new byte[this.allParams.length + param.length];
      System.arraycopy(this.allParams, 0, aux, 0, this.allParams.length);
      System.arraycopy(param, 0, aux, this.allParams.length, param.length);
      this.allParams = aux;
   }

   private boolean isConfigListAlreadyCreated() {
      return this.configListAlreadyCreated;
   }

   private void setConfigListAlreadyCreated(boolean value) {
      this.configListAlreadyCreated = value;
   }

   private void initAllParams() {
      this.allParams = new byte[1];
      this.allParams[0] = -1;
   }

   public static Rs232IntegratedOmniScannerInfoHelper getInstance() {
      if (instance == null) {
         instance = new Rs232IntegratedOmniScannerInfoHelper();
      }

      return instance;
   }

   public List getConfigurationPackets(ScannerConfig config) {
      if (!this.isConfigListAlreadyCreated()) {
         this.initAllParams();
         this.generateBaseConfig(config);
         this.generateIsEnabledConfigs(config);
         this.generateRemainingConfigs(config);
         this.listOfConfigPackets.addAll(Rs232IntegratedOmniScannerPacketManager.getInstance().createMultipacketMessage((byte)-58, this.allParams));
         this.setConfigListAlreadyCreated(true);
      }

      return this.listOfConfigPackets;
   }

   public byte[] getStartSessionScannerCmdBytes() {
      return Rs232IntegratedOmniScannerPacketManager.getInstance().createParameterPacket((byte)-118, (byte)4);
   }

   public byte[] getStopSessionScannerCmdBytes() {
      return Rs232IntegratedOmniScannerPacketManager.getInstance().createParameterPacket((byte)-118, (byte)8);
   }

   public byte[] getEnableBeeperScannerCmdBytes() {
      return Rs232IntegratedOmniScannerPacketManager.getInstance().createParameterPacket((byte)56, (byte)1);
   }

   public byte[] getDisableBeeperScannerCmdBytes() {
      return Rs232IntegratedOmniScannerPacketManager.getInstance().createParameterPacket((byte)56, (byte)0);
   }

   public byte[] getDirectIOCmdBytes(int command, int[] data) {
      if (data != null && data.length > 0) {
         int value = data[0];
         switch (command) {
            case 10:
               byte mode;
               switch (value) {
                  case 11:
                     mode = 1;
                     break;
                  case 12:
                     mode = 6;
                     break;
                  case 13:
                     mode = 7;
                     break;
                  default:
                     return null;
               }

               return Rs232IntegratedOmniScannerPacketManager.getInstance().createParameterPacket((byte)-115, mode);
            default:
               return null;
         }
      } else {
         return null;
      }
   }
}
