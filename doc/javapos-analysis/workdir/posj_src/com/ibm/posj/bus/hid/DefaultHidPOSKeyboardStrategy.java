package com.ibm.posj.bus.hid;

import com.ibm.posj.kbd.POSKeyboardIndicator;
import com.ibm.posj.util.POSKeyboardUtil;
import com.ibm.posj.util.PosjUtil;
import java.util.BitSet;

public abstract class DefaultHidPOSKeyboardStrategy implements HidPOSKeyboardStrategy {
   abstract static class AbstractAdministrativeModePOSKeyboardStrategy extends DefaultHidPOSKeyboardStrategy {
      protected byte createSBCSIndicatorConfig(POSKeyboardIndicator indicators) {
         byte ppByte = 0;
         if (indicators.getWait()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 0);
         }

         if (indicators.getOffline()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 1);
         }

         if (indicators.getMsgPendOrSysMsg()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 2);
         }

         if (indicators.getBlankOrReady()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 3);
         }

         if (indicators.getNumLock()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 5);
         }

         if (indicators.getCapsLock()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 6);
         }

         if (indicators.getScrollLock()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 4);
         }

         return ppByte;
      }

      protected byte createDBCSIndicatorConfig(POSKeyboardIndicator indicators) {
         byte ppByte = 0;
         if (indicators.getWait()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 0);
         }

         if (indicators.getOffline()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 1);
         }

         if (indicators.getMsgPendOrSysMsg()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 2);
         }

         if (indicators.getBlankOrReady()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 3);
         }

         if (indicators.getNumLock()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 5);
         }

         if (indicators.getCapsLock()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 6);
         }

         if (indicators.getScrollLock()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 4);
         }

         return ppByte;
      }

      protected BitSet convertToDBCSBitSet(POSKeyboardIndicator indicators) {
         BitSet bs = new BitSet(8);
         PosjUtil.setBitSet(bs, 0, indicators.getWait());
         PosjUtil.setBitSet(bs, 1, indicators.getOffline());
         PosjUtil.setBitSet(bs, 2, indicators.getMsgPendOrSysMsg());
         PosjUtil.setBitSet(bs, 3, indicators.getBlankOrReady());
         PosjUtil.setBitSet(bs, 5, indicators.getNumLock());
         PosjUtil.setBitSet(bs, 6, indicators.getCapsLock());
         PosjUtil.setBitSet(bs, 4, indicators.getScrollLock());
         return bs;
      }

      protected BitSet convertToSBCSBitSet(POSKeyboardIndicator indicators) {
         BitSet bs = new BitSet(8);
         PosjUtil.setBitSet(bs, 0, indicators.getWait());
         PosjUtil.setBitSet(bs, 1, indicators.getOffline());
         PosjUtil.setBitSet(bs, 2, indicators.getMsgPendOrSysMsg());
         PosjUtil.setBitSet(bs, 3, indicators.getBlankOrReady());
         PosjUtil.setBitSet(bs, 5, indicators.getNumLock());
         PosjUtil.setBitSet(bs, 6, indicators.getCapsLock());
         PosjUtil.setBitSet(bs, 4, indicators.getScrollLock());
         return bs;
      }

      public int getStatusLength() {
         return 3;
      }

      public int getDevInfoPosition() {
         return 0;
      }

      public abstract int getDevID(byte[] var1);
   }

   abstract static class AbstractLegacyModePOSKeyboardStrategy extends DefaultHidPOSKeyboardStrategy {
      protected byte createSBCSIndicatorConfig(POSKeyboardIndicator indicators) {
         byte ppByte = 0;
         if (indicators.getWait()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 0);
         }

         if (indicators.getOffline()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 1);
         }

         if (indicators.getMsgPendOrSysMsg()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 2);
         }

         if (indicators.getBlankOrReady()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 3);
         }

         if (indicators.getNumLock()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 5);
         }

         if (indicators.getCapsLock()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 6);
         }

         if (indicators.getScrollLock()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 4);
         }

         return ppByte;
      }

      protected byte createDBCSIndicatorConfig(POSKeyboardIndicator indicators) {
         byte ppByte = 0;
         if (indicators.getWait()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 2);
         }

         if (indicators.getOffline()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 1);
         }

         if (indicators.getMsgPendOrSysMsg()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 0);
         }

         if (indicators.getBlankOrReady()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 3);
         }

         if (indicators.getNumLock()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 5);
         }

         if (indicators.getCapsLock()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 6);
         }

         if (indicators.getScrollLock()) {
            ppByte = (byte)PosjUtil.setBit(ppByte, 4);
         }

         return ppByte;
      }

      protected BitSet convertToDBCSBitSet(POSKeyboardIndicator indicators) {
         BitSet bs = new BitSet(8);
         PosjUtil.setBitSet(bs, 2, indicators.getWait());
         PosjUtil.setBitSet(bs, 1, indicators.getOffline());
         PosjUtil.setBitSet(bs, 0, indicators.getMsgPendOrSysMsg());
         PosjUtil.setBitSet(bs, 3, indicators.getBlankOrReady());
         PosjUtil.setBitSet(bs, 5, indicators.getNumLock());
         PosjUtil.setBitSet(bs, 6, indicators.getCapsLock());
         PosjUtil.setBitSet(bs, 4, indicators.getScrollLock());
         return bs;
      }

      protected BitSet convertToSBCSBitSet(POSKeyboardIndicator indicators) {
         BitSet bs = new BitSet(8);
         PosjUtil.setBitSet(bs, 0, indicators.getWait());
         PosjUtil.setBitSet(bs, 1, indicators.getOffline());
         PosjUtil.setBitSet(bs, 2, indicators.getMsgPendOrSysMsg());
         PosjUtil.setBitSet(bs, 3, indicators.getBlankOrReady());
         PosjUtil.setBitSet(bs, 5, indicators.getNumLock());
         PosjUtil.setBitSet(bs, 6, indicators.getCapsLock());
         PosjUtil.setBitSet(bs, 4, indicators.getScrollLock());
         return bs;
      }

      public int getStatusLength() {
         return 4;
      }

      public int getDevInfoPosition() {
         return 1;
      }

      public int getDevID(byte[] response) {
         return POSKeyboardUtil.getUsbPOSKeyboardIDLegacyMode(response);
      }
   }

   static class BootModeDBCSPOSKeyboardStrategy
      extends DefaultHidPOSKeyboardStrategy.AbstractAdministrativeModePOSKeyboardStrategy
      implements HidPOSKeyboardStrategy.BootModeDBCSPOSKeyboardStrategy {
      public byte createIndicatorConfig(POSKeyboardIndicator indicators) {
         return this.createDBCSIndicatorConfig(indicators);
      }

      public void createIndicatorConfig(POSKeyboardIndicator indicators, BitSet bitSet) {
         bitSet = this.convertToDBCSBitSet(indicators);
      }

      public int getDevID(byte[] response) {
         return POSKeyboardUtil.getUsbPOSKeyboardIDBootMode(response);
      }
   }

   static class BootModeSBCSPOSKeyboardStrategy
      extends DefaultHidPOSKeyboardStrategy.AbstractAdministrativeModePOSKeyboardStrategy
      implements HidPOSKeyboardStrategy.BootModeSBCSPOSKeyboardStrategy {
      public byte createIndicatorConfig(POSKeyboardIndicator indicators) {
         return this.createSBCSIndicatorConfig(indicators);
      }

      public void createIndicatorConfig(POSKeyboardIndicator indicators, BitSet bitSet) {
         bitSet = this.convertToSBCSBitSet(indicators);
      }

      public int getDevID(byte[] response) {
         return POSKeyboardUtil.getUsbPOSKeyboardIDBootMode(response);
      }
   }

   public static class Factory implements HidPOSKeyboardStrategy.Factory {
      public HidPOSKeyboardStrategy createNonBootModePOSKeyboardStrategy(boolean isDBCS) {
         return (HidPOSKeyboardStrategy)(isDBCS
            ? new DefaultHidPOSKeyboardStrategy.NonBootModeDBCSPOSKeyboardStrategy()
            : new DefaultHidPOSKeyboardStrategy.NonBootModeSBCSPOSKeyboardStrategy());
      }

      public HidPOSKeyboardStrategy createBootModePOSKeyboardStrategy(boolean isDBCS) {
         return (HidPOSKeyboardStrategy)(isDBCS
            ? new DefaultHidPOSKeyboardStrategy.BootModeDBCSPOSKeyboardStrategy()
            : new DefaultHidPOSKeyboardStrategy.BootModeSBCSPOSKeyboardStrategy());
      }

      public HidPOSKeyboardStrategy createLegacyModePOSKeyboardStrategy(boolean isDBCS) {
         return (HidPOSKeyboardStrategy)(isDBCS
            ? new DefaultHidPOSKeyboardStrategy.LegacyModeDBCSPOSKeyboardStrategy()
            : new DefaultHidPOSKeyboardStrategy.LegacyModeSBCSPOSKeyboardStrategy());
      }
   }

   static class LegacyModeDBCSPOSKeyboardStrategy
      extends DefaultHidPOSKeyboardStrategy.AbstractLegacyModePOSKeyboardStrategy
      implements HidPOSKeyboardStrategy.LegacyModeDBCSPOSKeyboardStrategy {
      public byte createIndicatorConfig(POSKeyboardIndicator indicators) {
         return this.createDBCSIndicatorConfig(indicators);
      }

      public void createIndicatorConfig(POSKeyboardIndicator indicators, BitSet bitSet) {
         bitSet = this.convertToDBCSBitSet(indicators);
      }
   }

   static class LegacyModeSBCSPOSKeyboardStrategy
      extends DefaultHidPOSKeyboardStrategy.AbstractLegacyModePOSKeyboardStrategy
      implements HidPOSKeyboardStrategy.LegacyModeSBCSPOSKeyboardStrategy {
      public byte createIndicatorConfig(POSKeyboardIndicator indicators) {
         return this.createSBCSIndicatorConfig(indicators);
      }

      public void createIndicatorConfig(POSKeyboardIndicator indicators, BitSet bitSet) {
         bitSet = this.convertToSBCSBitSet(indicators);
      }
   }

   static class NonBootModeDBCSPOSKeyboardStrategy
      extends DefaultHidPOSKeyboardStrategy.AbstractAdministrativeModePOSKeyboardStrategy
      implements HidPOSKeyboardStrategy.NonBootModeDBCSPOSKeyboardStrategy {
      public byte createIndicatorConfig(POSKeyboardIndicator indicators) {
         return this.createDBCSIndicatorConfig(indicators);
      }

      public void createIndicatorConfig(POSKeyboardIndicator indicators, BitSet bitSet) {
         bitSet = this.convertToDBCSBitSet(indicators);
      }

      public int getDevID(byte[] response) {
         return POSKeyboardUtil.getUsbPOSKeyboardIDNonBootMode(response);
      }
   }

   static class NonBootModeSBCSPOSKeyboardStrategy
      extends DefaultHidPOSKeyboardStrategy.AbstractAdministrativeModePOSKeyboardStrategy
      implements HidPOSKeyboardStrategy.NonBootModeSBCSPOSKeyboardStrategy {
      public byte createIndicatorConfig(POSKeyboardIndicator indicators) {
         return this.createSBCSIndicatorConfig(indicators);
      }

      public void createIndicatorConfig(POSKeyboardIndicator indicators, BitSet bitSet) {
         bitSet = this.convertToSBCSBitSet(indicators);
      }

      public int getDevID(byte[] response) {
         return POSKeyboardUtil.getUsbPOSKeyboardIDNonBootMode(response);
      }
   }
}
