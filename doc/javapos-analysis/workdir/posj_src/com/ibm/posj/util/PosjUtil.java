package com.ibm.posj.util;

import java.util.BitSet;

public class PosjUtil {
   public static int toUnsignedInt(byte b) {
      return b & 0xFF;
   }

   public static int toUnsignedInt(short s) {
      return s & 65535;
   }

   public static void setBitSet(BitSet bs, int pos, boolean value) {
      if (value) {
         bs.set(pos);
      } else {
         bs.clear(pos);
      }
   }

   public static boolean isBitSelected(int value, int pos) {
      int bit = 1;
      if (pos < 32) {
         bit <<= pos;
         return (value & bit) == bit;
      } else {
         throw new IllegalArgumentException("The pos argument must be less than 32");
      }
   }

   public static int clearBit(int value, int pos) {
      if (!isBitSelected(value, pos)) {
         return value;
      } else {
         int bit = 1;
         if (pos < 32) {
            bit <<= pos;
            return value ^ bit;
         } else {
            throw new IllegalArgumentException("The pos argument must be less than 32");
         }
      }
   }

   public static int setBit(int value, int pos) {
      int bit = 1;
      if (pos < 32) {
         bit <<= pos;
         return value | bit;
      } else {
         throw new IllegalArgumentException("The pos argument must be less than 32");
      }
   }

   public static int setByte(int dest, byte value, int pos) {
      if (pos >= 0 && pos <= 3) {
         int tmp = 0;
         switch (pos) {
            case 0:
               dest &= 16777215;
               tmp = toUnsignedInt(value) << 24;
               dest |= tmp;
               break;
            case 1:
               dest &= -16711681;
               tmp = toUnsignedInt(value) << 16;
               dest |= tmp;
               break;
            case 2:
               dest &= -65281;
               tmp = toUnsignedInt(value) << 8;
               dest |= tmp;
               break;
            case 3:
               dest &= -256;
               dest |= toUnsignedInt(value);
         }

         return dest;
      } else {
         throw new IllegalArgumentException("The pos argument must be 0,1,2 or 3");
      }
   }

   public static int convertTo(short highBytes, short lowBytes) {
      int integer = 0;
      integer = toUnsignedInt(highBytes) << 16;
      return integer | toUnsignedInt(lowBytes);
   }
}
