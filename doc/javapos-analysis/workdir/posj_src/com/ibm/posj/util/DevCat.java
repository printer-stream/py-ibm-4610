package com.ibm.posj.util;

public interface DevCat {
   String JPOS_VERSION_STRING = "1.7.2";

   String toString();

   void accept(DevCatVisitor var1);

   public interface BumpBar extends DevCat {
   }

   public interface CAT extends DevCat {
   }

   public interface CashDrawer extends DevCat {
   }

   public interface CheckScanner extends DevCat {
   }

   public interface CoinDispenser extends DevCat {
   }

   public interface FiscalPrinter extends DevCat {
   }

   public interface HardTotals extends DevCat {
   }

   public interface Keylock extends DevCat {
   }

   public interface LineDisplay extends DevCat {
   }

   public interface MICR extends DevCat {
   }

   public interface MSR extends DevCat {
   }

   public interface MotionSensor extends DevCat {
   }

   public interface POSKeyboard extends DevCat {
   }

   public interface POSPower extends DevCat {
   }

   public interface POSPrinter extends DevCat {
   }

   public interface Pinpad extends DevCat {
   }

   public interface RemoteOrderDisplay extends DevCat {
   }

   public interface Scale extends DevCat {
   }

   public interface Scanner extends DevCat {
   }

   public interface SignatureCapture extends DevCat {
   }

   public interface ToneIndicator extends DevCat {
   }

   public interface Unknown extends DevCat {
   }
}
