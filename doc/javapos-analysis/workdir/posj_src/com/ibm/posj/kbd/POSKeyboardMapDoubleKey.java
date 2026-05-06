package com.ibm.posj.kbd;

import com.ibm.jutil.Timer;
import com.ibm.jutil.Timerable;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;

public class POSKeyboardMapDoubleKey {
   private POSKeyboardMapDoubleKey.DoubleKey[] doubleKeys;
   private boolean capKeyUp;
   private Tracer tracer = TracerFactory.getInstance().createTracer("POSKeyboardMap", "POSKeyboardMapDoubleKeys");
   private static final int KEY_DOWN = 1;

   POSKeyboardMapDoubleKey(int[][] list) {
      int size = 0;
      int[] repetition = new int[list.length * 2];
      int repSize = 0;

      for (int i = 0; i < list.length; i++) {
         if (list[i].length < 3) {
            if (this.tracer.isOn()) {
               this.tracer.println("Dropping line: [" + i + "], not enough values to configure a double key.");
            }
         } else {
            boolean isNotRepeated = true;
            if (list[i][1] == list[i][2]) {
               isNotRepeated = false;
            } else {
               for (int j = 0; j < repSize; j++) {
                  if (repetition[j] == list[i][1] || repetition[j] == list[i][2]) {
                     isNotRepeated = false;
                  }
               }
            }

            if (isNotRepeated) {
               list[size++] = list[i];
               repetition[repSize++] = list[i][1];
               repetition[repSize++] = list[i][2];
               if (this.tracer.isOn()) {
                  this.tracer
                     .println(
                        "Added rule [0x"
                           + Integer.toHexString(list[i][0]).toUpperCase()
                           + " <- 0x"
                           + Integer.toHexString(list[i][1]).toUpperCase()
                           + " ,0x"
                           + Integer.toHexString(list[i][2]).toUpperCase()
                           + "] from line: ["
                           + i
                           + "]"
                     );
               }
            } else if (this.tracer.isOn()) {
               this.tracer.println("Dropping line: [" + i + "], repeated values.");
            }
         }
      }

      this.doubleKeys = new POSKeyboardMapDoubleKey.DoubleKey[size];

      for (int ix = 0; ix < size; ix++) {
         this.doubleKeys[ix] = new POSKeyboardMapDoubleKey.DoubleKey(list[ix][0], list[ix][1], list[ix][2]);
      }

      this.capKeyUp = true;
   }

   void setCapKeyUp(boolean cku) {
      if (this.tracer.isOn()) {
         this.tracer.println("CapKeyUp reported to be " + cku + ".");
      }

      this.capKeyUp = cku;
   }

   int getDoubleKeyPosition(int k) {
      int ret = -1;

      for (int i = 0; i < this.doubleKeys.length; i++) {
         if (k == this.doubleKeys[i].getFirstDoubleKey() || k == this.doubleKeys[i].getSecondDoubleKey()) {
            ret = i;
         }
      }

      return ret;
   }

   int[] processDoubleKey(int k, int t) {
      int[] ret = new int[0];
      int dkid = this.getDoubleKeyPosition(k);
      if (this.tracer.isOn()) {
         this.tracer.println("-->processDoubleKey()");
         this.tracer.println("RECIEVED: [0X" + Integer.toHexString(k).toUpperCase() + ", " + (t == 1 ? "make" : "break") + "]");
      }

      if (dkid != -1) {
         ret = this.doubleKeys[dkid].process(k, t);
      }

      if (this.tracer.isOn()) {
         this.tracer
            .println(
               "Double key processed RETURNED["
                  + (ret.length == 2 ? "0x" + Integer.toHexString(ret[0]).toUpperCase() + ", " + (ret[1] == 1 ? "make" : "break") : "nothing")
                  + "]."
            );
         this.tracer.println("<--processDoubleKey()");
      }

      return ret;
   }

   int[][] getDoubleKeyList() {
      int[][] ret = new int[this.doubleKeys.length][];

      for (int i = 0; i < this.doubleKeys.length; i++) {
         ret[i] = new int[]{this.doubleKeys[i].getExpectedScanCode(), this.doubleKeys[i].getFirstDoubleKey(), this.doubleKeys[i].getSecondDoubleKey()};
      }

      return ret;
   }

   private class DoubleKey implements Timerable {
      private final int doubleKeyThreshold = 100;
      private Timer timer;
      private int expectedScanCode;
      private int firstDoubleKey;
      private int secondDoubleKey;
      private int firstProcessed;
      private byte currentState;
      private static final byte ST_INIT = 0;
      private static final byte ST_MAKE = 1;
      private static final byte ST_IDLE = 2;
      private static final byte ST_TYPE = 3;
      private static final byte ST_BREK = 4;

      DoubleKey(int vsc, int sc1, int sc2) {
         this.expectedScanCode = vsc;
         this.firstDoubleKey = sc1;
         this.secondDoubleKey = sc2;
         this.firstProcessed = 0;
         this.currentState = 0;
         this.timer = new Timer(this, 100);
         if (POSKeyboardMapDoubleKey.this.tracer.isOn()) {
            POSKeyboardMapDoubleKey.this.tracer
               .println(
                  "DoubleKey instance created! 0x"
                     + Integer.toHexString(vsc).toUpperCase()
                     + " <- [0x"
                     + Integer.toHexString(sc1).toUpperCase()
                     + " 0x"
                     + Integer.toHexString(sc2).toUpperCase()
                     + "]"
               );
         }
      }

      public void timerExpired() {
         if (POSKeyboardMapDoubleKey.this.tracer.isOn()) {
            POSKeyboardMapDoubleKey.this.tracer.println("Timer expired.");
         }
      }

      int[] process(int key, int type) {
         int[] ret = new int[0];
         if (POSKeyboardMapDoubleKey.this.tracer.isOn()) {
            POSKeyboardMapDoubleKey.this.tracer.println("-->DoubleKey.process()");
         }

         if (key == this.firstDoubleKey || key == this.secondDoubleKey) {
            if (!POSKeyboardMapDoubleKey.this.capKeyUp) {
               if (POSKeyboardMapDoubleKey.this.tracer.isOn()) {
                  POSKeyboardMapDoubleKey.this.tracer.println("CapKeyUp Capability is NOT available, using timer.");
               }

               if (!this.timer.getStarted()) {
                  ret = new int[]{this.expectedScanCode, type};
                  this.timer.setTime(100);
                  this.timer.start();
                  if (POSKeyboardMapDoubleKey.this.tracer.isOn()) {
                     POSKeyboardMapDoubleKey.this.tracer.println("Scancode processed and returned");
                     POSKeyboardMapDoubleKey.this.tracer.println("Timer started");
                  }
               } else if (POSKeyboardMapDoubleKey.this.tracer.isOn()) {
                  POSKeyboardMapDoubleKey.this.tracer.println("Scancode ignored, timer has not yet expired");
               }
            } else {
               if (POSKeyboardMapDoubleKey.this.tracer.isOn()) {
                  POSKeyboardMapDoubleKey.this.tracer.println("CapKeyUp Capability is available, using state machine.");
               }

               switch (this.currentState) {
                  case 0:
                     if (type == 1) {
                        this.currentState = 1;
                     } else {
                        this.currentState = 0;
                     }
                     break;
                  case 1:
                     if (type == 1) {
                        if (key == this.firstProcessed) {
                           this.currentState = 1;
                        } else {
                           this.currentState = 2;
                        }
                     } else {
                        this.currentState = 4;
                     }
                     break;
                  case 2:
                     if (type == 1) {
                        this.currentState = 3;
                     } else {
                        this.currentState = 4;
                     }
                     break;
                  case 3:
                     if (type == 1) {
                        this.currentState = 3;
                     } else {
                        this.currentState = 4;
                     }
                     break;
                  case 4:
                     if (type == 1) {
                        this.currentState = 1;
                     } else {
                        this.currentState = 0;
                     }
                     break;
                  default:
                     this.currentState = 0;
               }

               switch (this.currentState) {
                  case 0:
                  case 2:
                  default:
                     if (POSKeyboardMapDoubleKey.this.tracer.isOn()) {
                        POSKeyboardMapDoubleKey.this.tracer.println("Ignoring event");
                     }
                     break;
                  case 1:
                     this.firstProcessed = key;
                     if (POSKeyboardMapDoubleKey.this.tracer.isOn()) {
                        POSKeyboardMapDoubleKey.this.tracer.println("Returning virtual scancode");
                     }

                     ret = new int[]{this.getExpectedScanCode(), type};
                     break;
                  case 3:
                  case 4:
                     this.firstProcessed = 0;
                     if (POSKeyboardMapDoubleKey.this.tracer.isOn()) {
                        POSKeyboardMapDoubleKey.this.tracer.println("Returning virtual scancode");
                     }

                     ret = new int[]{this.getExpectedScanCode(), type};
               }
            }
         }

         if (POSKeyboardMapDoubleKey.this.tracer.isOn()) {
            POSKeyboardMapDoubleKey.this.tracer.println("<--DoubleKey.process()");
         }

         return ret;
      }

      int getExpectedScanCode() {
         return this.expectedScanCode;
      }

      int getFirstDoubleKey() {
         return this.firstDoubleKey;
      }

      int getSecondDoubleKey() {
         return this.secondDoubleKey;
      }
   }
}
