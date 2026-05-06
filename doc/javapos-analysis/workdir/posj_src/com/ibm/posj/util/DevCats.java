package com.ibm.posj.util;

import java.util.Hashtable;

public class DevCats {
   private static final Hashtable DEVCAT_TABLE = new Hashtable();
   public static final DevCat UNKNOWN_DEVCAT = DevCats.Unknown.getInstance();
   public static final DevCat BUMPBAR_DEVCAT = DevCats.BumpBar.getInstance();
   public static final DevCat CASHDRAWER_DEVCAT = DevCats.CashDrawer.getInstance();
   public static final DevCat CHECKSCANNER_DEVCAT = DevCats.CheckScanner.getInstance();
   public static final DevCat CAT_DEVCAT = DevCats.CAT.getInstance();
   public static final DevCat COINDISPENSER_DEVCAT = DevCats.CoinDispenser.getInstance();
   public static final DevCat FISCALPRINTER_DEVCAT = DevCats.FiscalPrinter.getInstance();
   public static final DevCat HARDTOTALS_DEVCAT = DevCats.HardTotals.getInstance();
   public static final DevCat KEYLOCK_DEVCAT = DevCats.Keylock.getInstance();
   public static final DevCat LINEDISPLAY_DEVCAT = DevCats.LineDisplay.getInstance();
   public static final DevCat MICR_DEVCAT = DevCats.MICR.getInstance();
   public static final DevCat MSR_DEVCAT = DevCats.MSR.getInstance();
   public static final DevCat MOTIONSENSOR_DEVCAT = DevCats.MotionSensor.getInstance();
   public static final DevCat PINPAD_DEVCAT = DevCats.Pinpad.getInstance();
   public static final DevCat POSKEYBOARD_DEVCAT = DevCats.POSKeyboard.getInstance();
   public static final DevCat POSPOWER_DEVCAT = DevCats.POSPower.getInstance();
   public static final DevCat POSPRINTER_DEVCAT = DevCats.POSPrinter.getInstance();
   public static final DevCat REMOTEORDERDISPLAY_DEVCAT = DevCats.RemoteOrderDisplay.getInstance();
   public static final DevCat SCALE_DEVCAT = DevCats.Scale.getInstance();
   public static final DevCat SCANNER_DEVCAT = DevCats.Scanner.getInstance();
   public static final DevCat SIGNATURECAPTURE_DEVCAT = DevCats.SignatureCapture.getInstance();
   public static final DevCat TONEINDICATOR_DEVCAT = DevCats.ToneIndicator.getInstance();
   public static final DevCat[] DEVCAT_ARRAY = new DevCat[]{
      BUMPBAR_DEVCAT,
      CASHDRAWER_DEVCAT,
      CHECKSCANNER_DEVCAT,
      CAT_DEVCAT,
      COINDISPENSER_DEVCAT,
      FISCALPRINTER_DEVCAT,
      HARDTOTALS_DEVCAT,
      KEYLOCK_DEVCAT,
      LINEDISPLAY_DEVCAT,
      MICR_DEVCAT,
      MSR_DEVCAT,
      MOTIONSENSOR_DEVCAT,
      PINPAD_DEVCAT,
      POSKEYBOARD_DEVCAT,
      POSPOWER_DEVCAT,
      POSPRINTER_DEVCAT,
      REMOTEORDERDISPLAY_DEVCAT,
      SCALE_DEVCAT,
      SCANNER_DEVCAT,
      SIGNATURECAPTURE_DEVCAT,
      TONEINDICATOR_DEVCAT
   };

   public static DevCat getDevCatForName(String devCatName) {
      return DEVCAT_TABLE.containsKey(devCatName) ? (DevCat)DEVCAT_TABLE.get(devCatName) : UNKNOWN_DEVCAT;
   }

   static {
      DEVCAT_TABLE.put(BUMPBAR_DEVCAT.toString(), BUMPBAR_DEVCAT);
      DEVCAT_TABLE.put(CASHDRAWER_DEVCAT.toString(), CASHDRAWER_DEVCAT);
      DEVCAT_TABLE.put(CHECKSCANNER_DEVCAT.toString(), CHECKSCANNER_DEVCAT);
      DEVCAT_TABLE.put(CAT_DEVCAT.toString(), CAT_DEVCAT);
      DEVCAT_TABLE.put(COINDISPENSER_DEVCAT.toString(), COINDISPENSER_DEVCAT);
      DEVCAT_TABLE.put(FISCALPRINTER_DEVCAT.toString(), FISCALPRINTER_DEVCAT);
      DEVCAT_TABLE.put(HARDTOTALS_DEVCAT.toString(), HARDTOTALS_DEVCAT);
      DEVCAT_TABLE.put(KEYLOCK_DEVCAT.toString(), KEYLOCK_DEVCAT);
      DEVCAT_TABLE.put(LINEDISPLAY_DEVCAT.toString(), LINEDISPLAY_DEVCAT);
      DEVCAT_TABLE.put(MICR_DEVCAT.toString(), MICR_DEVCAT);
      DEVCAT_TABLE.put(MSR_DEVCAT.toString(), MSR_DEVCAT);
      DEVCAT_TABLE.put(MOTIONSENSOR_DEVCAT.toString(), MOTIONSENSOR_DEVCAT);
      DEVCAT_TABLE.put(PINPAD_DEVCAT.toString(), PINPAD_DEVCAT);
      DEVCAT_TABLE.put(POSKEYBOARD_DEVCAT.toString(), POSKEYBOARD_DEVCAT);
      DEVCAT_TABLE.put(POSPOWER_DEVCAT.toString(), POSPOWER_DEVCAT);
      DEVCAT_TABLE.put(POSPRINTER_DEVCAT.toString(), POSPRINTER_DEVCAT);
      DEVCAT_TABLE.put(REMOTEORDERDISPLAY_DEVCAT.toString(), REMOTEORDERDISPLAY_DEVCAT);
      DEVCAT_TABLE.put(SCALE_DEVCAT.toString(), SCALE_DEVCAT);
      DEVCAT_TABLE.put(SCANNER_DEVCAT.toString(), SCANNER_DEVCAT);
      DEVCAT_TABLE.put(SIGNATURECAPTURE_DEVCAT.toString(), SIGNATURECAPTURE_DEVCAT);
      DEVCAT_TABLE.put(TONEINDICATOR_DEVCAT.toString(), TONEINDICATOR_DEVCAT);
   }

   public abstract static class AbstractDevCat implements DevCat {
      public abstract String toString();

      public int hashCode() {
         return this.toString().hashCode();
      }

      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         } else {
            return !(obj instanceof DevCat) ? false : this.toString().equals(obj.toString());
         }
      }
   }

   public static class BumpBar extends DevCats.AbstractDevCat implements DevCat.BumpBar {
      private static DevCat instance = null;

      BumpBar() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.BumpBar();
         }

         return instance;
      }

      public String toString() {
         return "BumpBar";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitBumpBar(this);
      }
   }

   public static class CAT extends DevCats.AbstractDevCat implements DevCat.CAT {
      private static DevCat instance = null;

      CAT() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.CAT();
         }

         return instance;
      }

      public String toString() {
         return "CAT";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitCAT(this);
      }
   }

   public static class CashDrawer extends DevCats.AbstractDevCat implements DevCat.CashDrawer {
      private static DevCat instance = null;

      CashDrawer() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.CashDrawer();
         }

         return instance;
      }

      public String toString() {
         return "CashDrawer";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitCashDrawer(this);
      }
   }

   public static class CheckScanner extends DevCats.AbstractDevCat implements DevCat.CheckScanner {
      private static DevCat instance = null;

      CheckScanner() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.CheckScanner();
         }

         return instance;
      }

      public String toString() {
         return "CheckScanner";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitCheckScanner(this);
      }
   }

   public static class CoinDispenser extends DevCats.AbstractDevCat implements DevCat.CoinDispenser {
      private static DevCat instance = null;

      CoinDispenser() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.CoinDispenser();
         }

         return instance;
      }

      public String toString() {
         return "CoinDispenser";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitCoinDispenser(this);
      }
   }

   public static class Composite extends DevCats.AbstractDevCat implements DevCat {
      private static DevCat instance = null;

      Composite() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.Composite();
         }

         return instance;
      }

      public String toString() {
         return "Composite";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitComposite(this);
      }
   }

   public static class FiscalPrinter extends DevCats.AbstractDevCat implements DevCat.FiscalPrinter {
      private static DevCat instance = null;

      FiscalPrinter() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.FiscalPrinter();
         }

         return instance;
      }

      public String toString() {
         return "FiscalPrinter";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitFiscalPrinter(this);
      }
   }

   public static class HardTotals extends DevCats.AbstractDevCat implements DevCat.HardTotals {
      private static DevCat instance = null;

      HardTotals() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.HardTotals();
         }

         return instance;
      }

      public String toString() {
         return "HardTotals";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitHardTotals(this);
      }
   }

   public static class Keylock extends DevCats.AbstractDevCat implements DevCat.Keylock {
      private static DevCat instance = null;

      Keylock() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.Keylock();
         }

         return instance;
      }

      public String toString() {
         return "Keylock";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitKeylock(this);
      }
   }

   public static class LineDisplay extends DevCats.AbstractDevCat implements DevCat.LineDisplay {
      private static DevCat instance = null;

      LineDisplay() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.LineDisplay();
         }

         return instance;
      }

      public String toString() {
         return "LineDisplay";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitLineDisplay(this);
      }
   }

   public static class MICR extends DevCats.AbstractDevCat implements DevCat.MICR {
      private static DevCat instance = null;

      MICR() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.MICR();
         }

         return instance;
      }

      public String toString() {
         return "MICR";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitMICR(this);
      }
   }

   public static class MSR extends DevCats.AbstractDevCat implements DevCat.MSR {
      private static DevCat instance = null;

      MSR() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.MSR();
         }

         return instance;
      }

      public String toString() {
         return "MSR";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitMSR(this);
      }
   }

   public static class MotionSensor extends DevCats.AbstractDevCat implements DevCat.MotionSensor {
      private static DevCat instance = null;

      MotionSensor() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.MotionSensor();
         }

         return instance;
      }

      public String toString() {
         return "MotionSensor";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitMotionSensor(this);
      }
   }

   public static class POSKeyboard extends DevCats.AbstractDevCat implements DevCat.POSKeyboard {
      private static DevCat instance = null;

      POSKeyboard() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.POSKeyboard();
         }

         return instance;
      }

      public String toString() {
         return "POSKeyboard";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitPOSKeyboard(this);
      }
   }

   public static class POSPower extends DevCats.AbstractDevCat implements DevCat.POSPower {
      private static DevCat instance = null;

      POSPower() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.POSPower();
         }

         return instance;
      }

      public String toString() {
         return "POSPower";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitPOSPower(this);
      }
   }

   public static class POSPrinter extends DevCats.AbstractDevCat implements DevCat.POSPrinter {
      private static DevCat instance = null;

      POSPrinter() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.POSPrinter();
         }

         return instance;
      }

      public String toString() {
         return "POSPrinter";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitPOSPrinter(this);
      }
   }

   public static class Pinpad extends DevCats.AbstractDevCat implements DevCat.Pinpad {
      private static DevCat instance = null;

      Pinpad() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.Pinpad();
         }

         return instance;
      }

      public String toString() {
         return "Pinpad";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitPinpad(this);
      }
   }

   public static class RemoteOrderDisplay extends DevCats.AbstractDevCat implements DevCat.RemoteOrderDisplay {
      private static DevCat instance = null;

      RemoteOrderDisplay() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.RemoteOrderDisplay();
         }

         return instance;
      }

      public String toString() {
         return "RemoteOrderDisplay";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitRemoteOrderDisplay(this);
      }
   }

   public static class Scale extends DevCats.AbstractDevCat implements DevCat.Scale {
      private static DevCat instance = null;

      Scale() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.Scale();
         }

         return instance;
      }

      public String toString() {
         return "Scale";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitScale(this);
      }
   }

   public static class Scanner extends DevCats.AbstractDevCat implements DevCat.Scanner {
      private static DevCat instance = null;

      Scanner() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.Scanner();
         }

         return instance;
      }

      public String toString() {
         return "Scanner";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitScanner(this);
      }
   }

   public static class SignatureCapture extends DevCats.AbstractDevCat implements DevCat.SignatureCapture {
      private static DevCat instance = null;

      SignatureCapture() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.SignatureCapture();
         }

         return instance;
      }

      public String toString() {
         return "SignatureCapture";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitSignatureCapture(this);
      }
   }

   public static class ToneIndicator extends DevCats.AbstractDevCat implements DevCat.ToneIndicator {
      private static DevCat instance = null;

      ToneIndicator() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.ToneIndicator();
         }

         return instance;
      }

      public String toString() {
         return "ToneIndicator";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitToneIndicator(this);
      }
   }

   public static class Unknown extends DevCats.AbstractDevCat implements DevCat.Unknown {
      private static DevCat instance = null;

      Unknown() {
      }

      public static DevCat getInstance() {
         if (instance == null) {
            instance = new DevCats.Unknown();
         }

         return instance;
      }

      public String toString() {
         return "Unknown";
      }

      public void accept(DevCatVisitor visitor) {
         visitor.visitUnknown(this);
      }
   }
}
