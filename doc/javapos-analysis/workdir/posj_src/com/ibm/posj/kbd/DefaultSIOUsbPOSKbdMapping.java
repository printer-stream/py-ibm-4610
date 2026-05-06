package com.ibm.posj.kbd;

import com.ibm.posj.util.PosjUtil;
import java.util.Vector;

public class DefaultSIOUsbPOSKbdMapping extends AbstractKbdMapping {
   private byte lastKey = 0;
   private Vector vector = new Vector();
   private POSKeyboardData[] posKeyboardData = new POSKeyboardData[0];

   public synchronized POSKeyboardData[] translateKey(byte[] handleKey) {
      this.vector.removeAllElements();

      for (int count = 0; count < handleKey.length; count++) {
         if (this.isValidKey(handleKey[count])) {
            this.processKey(handleKey[count]);
            this.setLastKey(handleKey[count]);
         }
      }

      this.posKeyboardData = new POSKeyboardData[this.vector.size()];

      for (int countx = 0; countx < this.posKeyboardData.length; countx++) {
         this.posKeyboardData[countx] = (POSKeyboardData)this.vector.elementAt(countx);
      }

      return this.posKeyboardData;
   }

   protected void processKey(byte key) {
      if (key != -16) {
         if (this.lastKey == -16) {
            this.vector.add(this.toPOSKeyboardData(key, 2));
         } else {
            this.vector.add(this.toPOSKeyboardData(key, 1));
         }
      }
   }

   protected boolean isValidKey(byte key) {
      return key != -1;
   }

   protected POSKeyboardData toPOSKeyboardData(byte key, int type) {
      int data = PosjUtil.toUnsignedInt(key);
      return new POSKeyboardData(data, type);
   }

   protected void setLastKey(byte key) {
      this.lastKey = key;
   }
}
