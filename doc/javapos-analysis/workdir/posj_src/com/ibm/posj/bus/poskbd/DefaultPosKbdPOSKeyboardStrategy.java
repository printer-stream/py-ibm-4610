package com.ibm.posj.bus.poskbd;

import com.ibm.posj.kbd.POSKeyboardIndicator;
import com.ibm.posj.util.PosjUtil;
import java.util.BitSet;

public abstract class DefaultPosKbdPOSKeyboardStrategy implements PosKbdPOSKeyboardStrategy {
   protected BitSet convertToDBCSBitSet(POSKeyboardIndicator indicators) {
      BitSet bs = new BitSet(8);
      PosjUtil.setBitSet(bs, 3, indicators.getWait());
      PosjUtil.setBitSet(bs, 2, indicators.getOffline());
      PosjUtil.setBitSet(bs, 1, indicators.getMsgPendOrSysMsg());
      PosjUtil.setBitSet(bs, 4, indicators.getBlankOrReady());
      PosjUtil.setBitSet(bs, 5, indicators.getNumLock());
      PosjUtil.setBitSet(bs, 6, indicators.getCapsLock());
      PosjUtil.setBitSet(bs, 7, indicators.getScrollLock());
      return bs;
   }

   protected BitSet convertToSBCSBitSet(POSKeyboardIndicator indicators) {
      BitSet bs = new BitSet(8);
      PosjUtil.setBitSet(bs, 1, indicators.getWait());
      PosjUtil.setBitSet(bs, 2, indicators.getOffline());
      PosjUtil.setBitSet(bs, 3, indicators.getMsgPendOrSysMsg());
      PosjUtil.setBitSet(bs, 4, indicators.getBlankOrReady());
      PosjUtil.setBitSet(bs, 5, indicators.getNumLock());
      PosjUtil.setBitSet(bs, 6, indicators.getCapsLock());
      PosjUtil.setBitSet(bs, 7, indicators.getScrollLock());
      return bs;
   }

   static class DBCSPOSKeyboardStrategy extends DefaultPosKbdPOSKeyboardStrategy implements PosKbdPOSKeyboardStrategy.DBCSPOSKeyboardStrategy {
      public BitSet createIndicatorConfig(POSKeyboardIndicator indicators) {
         return this.convertToDBCSBitSet(indicators);
      }
   }

   public static class Factory implements PosKbdPOSKeyboardStrategy.Factory {
      public PosKbdPOSKeyboardStrategy createSBCSPOSKeyboardStrategy() {
         return new DefaultPosKbdPOSKeyboardStrategy.SBCSPOSKeyboardStrategy();
      }

      public PosKbdPOSKeyboardStrategy createDBCSPOSKeyboardStrategy() {
         return new DefaultPosKbdPOSKeyboardStrategy.DBCSPOSKeyboardStrategy();
      }
   }

   static class SBCSPOSKeyboardStrategy extends DefaultPosKbdPOSKeyboardStrategy implements PosKbdPOSKeyboardStrategy.SBCSPOSKeyboardStrategy {
      public BitSet createIndicatorConfig(POSKeyboardIndicator indicators) {
         return this.convertToSBCSBitSet(indicators);
      }
   }
}
