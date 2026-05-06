package com.ibm.posj.kbd;

import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Timer;
import com.ibm.jutil.Timerable;
import com.ibm.posj.POSKeyboardHandle;
import com.ibm.posj.event.DataEvent;
import com.ibm.posj.util.PosjUtil;

public class PS2NANPOSKbdMapping extends AbstractKbdMapping implements Timerable {
   private byte lastKey = 0;
   private boolean lastKeyProcessed = false;
   private boolean extendedKbdMapping = true;
   private boolean wasExtended = false;
   private boolean leftCtrlFlag = false;
   private ByteBuffer buffer = new ByteBuffer();
   private ByteBuffer extValueBuffer = new ByteBuffer();
   private int state = 0;
   private int timeout = 60;
   private Timer timer = new Timer(this, this.timeout);
   private boolean specialExtended = false;
   private boolean pause = false;
   private POSKeyboardHandle pkHandle = null;

   public PS2NANPOSKbdMapping(POSKeyboardHandle handle) {
      this.pkHandle = handle;
   }

   public synchronized POSKeyboardData[] translateKey(byte[] handleKey) {
      byte key = handleKey[0];
      if (handleKey.length == 2 && (handleKey[0] & 255) == 255 && (handleKey[1] & 255) == 255) {
         this.state = 10;
      } else if (!this.isValidKey(key)) {
         this.setLastKey(key, false);
         return EMPTY_ARRAY;
      }

      return this.process124_135Keys(key);
   }

   protected POSKeyboardData[] process124_135Keys(byte key) {
      POSKeyboardData[] returnData = new POSKeyboardData[1];
      switch (this.state) {
         case 0:
            if (key == -99) {
               this.buffer.append(key);
               this.state = 1;
               if (this.lastKey == -32 && this.getExtendedKbdMapping()) {
                  this.setExtended(true);
                  this.extValueBuffer.append(1);
               } else {
                  this.leftCtrlFlag = true;
                  this.extValueBuffer.append(0);
               }

               returnData = EMPTY_ARRAY;
               this.lastKey = 0;
            } else if (this.lastKey == -32 && this.isValidKey(key) && this.getExtendedKbdMapping()) {
               this.setExtended(true);
               returnData[0] = this.toPOSKeyboardData(this.extendData(key));
               this.lastKey = 0;
               this.setExtended(false);
            } else {
               if (this.isPause()) {
                  returnData[0] = this.toPOSKeyboardData(this.extendData(key));
               } else {
                  returnData[0] = this.toPOSKeyboardData(key);
               }

               this.lastKey = 0;
               this.setExtended(false);
               this.setPause(false);
            }
            break;
         case 1:
            if (this.lastKey == -32 && this.getExtendedKbdMapping()) {
               this.setExtended(true);
               this.extValueBuffer.append(1);
            } else {
               this.extValueBuffer.append(0);
            }

            this.buffer.append(key);
            if (key == 28 || key == 1) {
               this.state = 2;
               returnData = EMPTY_ARRAY;
            } else if (key == -99) {
               this.state = 4;
               returnData = EMPTY_ARRAY;
            } else {
               this.state = 10;
            }
            break;
         case 2:
            this.buffer.append(key);
            this.extValueBuffer.append(0);
            if (key == 29) {
               this.setSpecialExtended(true);
               returnData[0] = this.toPOSKeyboardData(this.buffer.byteAt(1));
               this.setSpecialExtended(false);
               this.setExtended(false);
               this.lastKey = 0;
               this.state = 0;
               this.buffer.reset();
               this.extValueBuffer.reset();
            } else {
               this.state = 10;
            }
         case 3:
         default:
            break;
         case 4:
            this.buffer.append(key);
            this.extValueBuffer.append(0);
            if (key != 28 && key != 1) {
               this.state = 10;
            } else {
               this.state = 5;
               returnData = EMPTY_ARRAY;
            }
            break;
         case 5:
            this.buffer.append(key);
            this.extValueBuffer.append(0);
            if (key == 29) {
               this.setSpecialExtended(true);
               returnData[0] = this.toPOSKeyboardData(this.buffer.byteAt(2));
               this.setSpecialExtended(false);
               this.state = 0;
               this.setExtended(false);
               this.lastKey = 0;
               this.buffer.reset();
               this.extValueBuffer.reset();
            } else {
               this.state = 10;
            }
      }

      if (this.state == 10) {
         returnData = new POSKeyboardData[this.buffer.getByteCount()];

         for (int i = 0; i < returnData.length; i++) {
            if (this.extValueBuffer.byteAt(i) == 1 && this.getExtendedKbdMapping()) {
               this.setExtended(true);
               returnData[i] = this.toPOSKeyboardData(this.extendData(this.buffer.byteAt(i)));
               this.setExtended(false);
            } else {
               this.setExtended(false);
               returnData[i] = this.toPOSKeyboardData(this.buffer.byteAt(i));
               this.leftCtrlFlag = false;
            }
         }

         this.state = 0;
         this.buffer.reset();
         this.extValueBuffer.reset();
         this.lastKey = 0;
         this.setExtended(false);
         this.leftCtrlFlag = false;
      }

      if (this.state == 0) {
         this.timer.reset();
      }

      if (this.state == 1) {
         this.timer.setTime(this.timeout);
         this.timer.start();
      }

      return returnData;
   }

   public void timerExpired() {
      this.pkHandle.getEventHelper().fireDataEvent(new DataEvent(this.getClass(), new byte[]{-1, -1}));
   }

   protected POSKeyboardData toPOSKeyboardData(int key) {
      int type = 1;
      int data;
      if (this.getSpecialExtended() && this.getExtendedKbdMapping()) {
         data = key & 0xFF;
         data |= 512;
      } else if (this.getExtended() && this.getExtendedKbdMapping()) {
         data = key & 511;
      } else {
         data = key & 0xFF;
      }

      if (PosjUtil.isBitSelected(data, 7)) {
         data = PosjUtil.clearBit(data, 7);
         type = 2;
      }

      if (data == 119) {
         data = 82;
      }

      return new POSKeyboardData(data, type);
   }

   protected boolean isValidKey(byte key) {
      return !this.isFlag(key) && !this.isSpecialScanCodeFlag(key) && !this.isScanCode138Flag(key);
   }

   protected boolean isFlag(byte key) {
      return key == -32 || key == -31;
   }

   protected boolean isSpecialScanCodeFlag(byte key) {
      return key == -86 && this.lastKey == -32
         || key == 42 && this.lastKey == -32
         || key == -74 && this.lastKey == -32
         || key == 54 && this.lastKey == -32
         || this.isSpecialScanCodeFlag2(key);
   }

   protected boolean isSpecialScanCodeFlag2(byte key) {
      return key == -74 && this.lastKey == -86 && !this.lastKeyProcessed;
   }

   protected boolean isScanCode138Flag(byte key) {
      if ((key != 29 || this.lastKey != -31) && (key != -99 || this.lastKey != -31) && (key != -59 || this.lastKey != -99 || this.lastKeyProcessed)) {
         return key == 70 && this.lastKey == -32 || key == -58 && this.lastKey == -32;
      } else {
         if (key == 29) {
            this.setExtended(true);
            this.setPause(true);
         }

         return true;
      }
   }

   protected int extendData(byte key) {
      int data = 1;
      data <<= 8;
      data |= key;
      if (PosjUtil.isBitSelected(key, 7)) {
         data &= 511;
      } else {
         data &= 4095;
      }

      return data;
   }

   protected void setLastKey(byte key, boolean processed) {
      this.lastKey = key;
      this.lastKeyProcessed = processed;
   }

   public boolean getExtendedKbdMapping() {
      return this.extendedKbdMapping;
   }

   public void setExtendedKbdMapping(boolean ext) {
      this.extendedKbdMapping = ext;
   }

   public void setExtended(boolean value) {
      this.wasExtended = value;
   }

   public boolean getExtended() {
      return this.wasExtended;
   }

   public void setSpecialExtended(boolean value) {
      this.specialExtended = value;
   }

   public boolean getSpecialExtended() {
      return this.specialExtended;
   }

   public void setPause(boolean value) {
      this.pause = value;
   }

   public boolean isPause() {
      return this.pause;
   }
}
