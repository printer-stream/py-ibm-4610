package com.ibm.posj.kbd;

import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

public abstract class POSKeyboardMap {
   private Tracer tracer = TracerFactory.getInstance().createTracer("POSKeyboardMap");
   private static final String className = "POSKeyboardMap";
   private String inString;
   private int lineNumber = 0;
   private int[][] kbdMap = new int[0][0];
   private int kbdMapLength = 0;
   private int kbdMapDblKeyLength = 0;
   private int[][] vMap = new int[0][0];
   private int vMapLength = 0;
   private int[] s1Array = new int[0];
   private int[] s2Array = new int[0];
   private int[][] doubleKeyRuleSet = new int[0][];
   private POSKeyboardMapDoubleKey doubleKeyProcessor;
   private static Vector scancodeSeqVector = new Vector();
   private static Vector virScancodeVector = new Vector();
   private static final int FOUND = 1;
   private static final int IN_PROGRESS = 2;
   private static final int NOT_FOUND = 3;
   public static final int LESS = -1;
   public static final int EQUAL = 0;
   public static final int GREATER = 1;
   static final String START_VIRTUAL_SC_TOKEN = "[";
   static final String END_VIRTUAL_SC_TOKEN = "]";
   static final int END_SENTINAL = -1;
   private static int pHead = 0;
   private static int pTail = 0;
   private static int index = 0;
   private static int kbdMapMaxWidth = 0;
   private static int[][] inputBuf = new int[0][0];
   private static int[] virtualKey = new int[0];
   private static int flag = 3;

   protected POSKeyboardMap(String fileName) {
      try {
         FileReader fr = new FileReader(fileName);
         BufferedReader br = new BufferedReader(fr);

         while ((this.inString = br.readLine()) != null) {
            this.lineNumber++;
            if (this.inString.trim() != null && !this.inString.startsWith("#")) {
               this.processInputLine(this.inString);
            }
         }

         fr.close();
         this.doubleKeyProcessor = new POSKeyboardMapDoubleKey(this.doubleKeyRuleSet);
         this.showVector(virScancodeVector, "content of virScancodeVector:");
         this.showVector(scancodeSeqVector, "content of scancodeSeqVector:");
         POSKeyboardMap.makeArrayFromVector n = new POSKeyboardMap.makeArrayFromVector(virScancodeVector);
         this.vMap = n.getArray();
         this.vMapLength = n.getArrayLenght();
         POSKeyboardMap.makeArrayFromVector m = new POSKeyboardMap.makeArrayFromVector(scancodeSeqVector);
         this.kbdMap = m.getArray();
         this.kbdMapLength = m.getArrayLenght();
         this.kbdMapDblKeyLength = this.doubleKeyProcessor.getDoubleKeyList().length;
         pHead = 0;
         pTail = this.kbdMapLength;
         index = 0;
         kbdMapMaxWidth = m.getArrayMaxWidth();
         inputBuf = new int[kbdMapMaxWidth][2];
         this.showArray(this.kbdMap, this.kbdMapLength, "scancodeSequenceArray");
         this.showArray(this.vMap, this.vMapLength, "virtualScancodeArray");
      } catch (FileNotFoundException var6) {
         this.kbdMapLength = 0;
         this.kbdMapDblKeyLength = 0;
         this.doubleKeyProcessor = new POSKeyboardMapDoubleKey(new int[0][0]);
         if (this.tracer.isOn()) {
            this.tracer.print(var6);
            this.tracer.println("file not found!" + var6);
         }
      } catch (IOException var7) {
         this.kbdMapLength = 0;
         this.kbdMapDblKeyLength = 0;
         this.doubleKeyProcessor = new POSKeyboardMapDoubleKey(new int[0][0]);
         if (this.tracer.isOn()) {
            this.tracer.print(var7);
         }
      }
   }

   public void processInputLine(String s) {
      if (s.length() >= 3) {
         String virtualScancodeLine = "";
         String scancodeSequenceLine = "";
         char[] tmp = s.toCharArray();

         for (int cnt = 0; cnt < tmp.length; cnt++) {
            if (tmp[cnt] == '\t') {
               tmp[cnt] = ' ';
            }
         }

         s = new String(tmp);
         if (this.tracer.isOn()) {
            this.tracer.println("processInputLine s:" + s);
         }

         int i;
         if (s.startsWith("[") && (i = s.indexOf("]")) != -1) {
            virtualScancodeLine = s.substring(1, i).trim();
            scancodeSequenceLine = s.substring(i + 1).trim();
         } else if ((i = s.indexOf(" ")) != -1) {
            virtualScancodeLine = s.substring(0, i).trim();
            scancodeSequenceLine = s.substring(i + 1).trim();
         }

         int[] virtualScancodeLineArray = this.processString(virtualScancodeLine);
         int[] scancodeSequenceLineArray = this.processString(scancodeSequenceLine);
         if (virtualScancodeLineArray.length != 0 && virtualScancodeLineArray[0] == 255) {
            int[][] doubleKeyRuleSetTmp = new int[this.doubleKeyRuleSet.length + 1][];

            for (int k = 0; k < this.doubleKeyRuleSet.length; k++) {
               doubleKeyRuleSetTmp[k] = this.doubleKeyRuleSet[k];
            }

            doubleKeyRuleSetTmp[this.doubleKeyRuleSet.length] = scancodeSequenceLineArray;
            this.doubleKeyRuleSet = doubleKeyRuleSetTmp;
            virtualScancodeLineArray = new int[0];
            scancodeSequenceLineArray = new int[0];
         }

         if (this.tracer.isOn()) {
            this.tracer.println("input line:" + s);
         }

         if (virtualScancodeLineArray.length != 0
            && scancodeSequenceLineArray.length != 0
            && this.validateArrayValue(virtualScancodeLineArray, Integer.MAX_VALUE)) {
            if (this.tracer.isOn()) {
               this.tracer.println("-->OK");
            }

            this.insertElement(virtualScancodeLineArray, scancodeSequenceLineArray);
         } else if (this.tracer.isOn()) {
            this.tracer.println("-->Ignore");
         }
      }
   }

   private int[] processString(String s) {
      int[] scanCodes = new int[0];
      StringTokenizer st = new StringTokenizer(s);
      if (this.tracer.isOn()) {
         this.tracer.println("processString s:" + s);
      }

      while (st.hasMoreTokens()) {
         String s1 = st.nextToken();
         if (s1.startsWith("#")) {
            break;
         }

         int n = this.decodeString(s1);
         if (-1 == n) {
            return new int[0];
         }

         int[] temp = new int[scanCodes.length + 1];

         for (int i = 0; i < scanCodes.length; i++) {
            temp[i] = scanCodes[i];
         }

         temp[scanCodes.length] = n;
         scanCodes = temp;
      }

      return scanCodes;
   }

   private boolean validateArrayValue(int[] array, int max) {
      for (int i = 1; i < array.length; i++) {
         if (array[i] > max) {
            return false;
         }
      }

      return true;
   }

   private int decodeString(String s) {
      int n = 0;

      try {
         switch (s.length()) {
            case 0:
               return -1;
            case 1:
               return this.convertToScancode(s.charAt(0));
            case 2:
               if (s.charAt(0) == '^') {
                  char c = s.charAt(1);
                  if (Character.isLetter(c)) {
                     c = Character.toUpperCase(c);
                  }

                  n = c - '@';
                  if (n < 0 || n > 31) {
                     if (this.tracer.isOn()) {
                        this.tracer.println("invalid character following ^: " + s.charAt(1));
                     }

                     return -1;
                  }
               }
            default:
               if (s.charAt(0) == '+') {
                  s = s.substring(1, s.length());
               }

               n = Integer.decode(s);
               if ((n & -65536) != 0) {
                  throw new NumberFormatException();
               }
         }
      } catch (NumberFormatException var4) {
         if (this.tracer.isOn()) {
            this.tracer.println("Numeric error");
         }

         n = -1;
      }

      return n;
   }

   protected abstract int convertToScancode(char var1);

   private void insertElement(int[] virtualScancodes, int[] scancodes) {
      synchronized (scancodeSeqVector) {
         boolean found = false;
         int index = 0;

         for (int var9 = 0; var9 < scancodeSeqVector.size(); var9++) {
            int r = this.doCompaire((int[])scancodeSeqVector.elementAt(var9), scancodes);
            if (r > 0) {
               scancodeSeqVector.insertElementAt(scancodes, var9);
               virScancodeVector.insertElementAt(virtualScancodes, var9);
               found = true;
               break;
            }

            if (r >= 0) {
               found = true;
               break;
            }
         }

         if (!found) {
            scancodeSeqVector.addElement(scancodes);
            virScancodeVector.addElement(virtualScancodes);
         }
      }
   }

   private int doCompaire(int[] first, int[] second) {
      int rc = 0;
      int nSize = first.length;
      if (second.length < first.length) {
         nSize = second.length;
      }

      for (int i = 0; i < nSize; i++) {
         if (first[i] > second[i]) {
            return 1;
         }

         if (first[i] < second[i]) {
            return -1;
         }
      }

      if (first.length > second.length) {
         return 1;
      } else {
         return first.length < second.length ? -1 : rc;
      }
   }

   public int getDoubleKeyLen() {
      return this.kbdMapDblKeyLength;
   }

   public int getKeyboardMapLen() {
      return this.kbdMapLength;
   }

   public synchronized int[][] lookupDoubleKey(int inChar, int keyEventType) {
      int[][] ret;
      if (this.doubleKeyProcessor.getDoubleKeyPosition(inChar) != -1) {
         int[] lr = this.doubleKeyProcessor.processDoubleKey(inChar, keyEventType);
         if (lr.length == 0) {
            ret = new int[0][0];
         } else {
            ret = new int[][]{lr};
         }
      } else {
         ret = new int[][]{{inChar, keyEventType}};
      }

      return ret;
   }

   public synchronized int[][] lookupScancodes(int inChar, int keyEventType) {
      flag = 3;
      if (index >= kbdMapMaxWidth) {
         int[][] sendBuf = new int[kbdMapMaxWidth][2];
         System.arraycopy(inputBuf, 0, sendBuf, 0, kbdMapMaxWidth);
         pHead = 0;
         pTail = this.kbdMapLength;
         index = 0;
         return sendBuf;
      } else {
         inputBuf[index][0] = inChar;
         inputBuf[index][1] = keyEventType;

         for (int i = pHead; i < pTail; i++) {
            if (inChar == this.kbdMap[i][index]) {
               pHead = i;
               if (index == this.kbdMap[i].length - 1) {
                  flag = 1;
               } else {
                  for (int j = pHead + 1; j < pTail; j++) {
                     if (inChar != this.kbdMap[j][index]) {
                        pTail = j;
                        break;
                     }
                  }

                  flag = 2;
               }
               break;
            }
         }

         if (flag != 1) {
            if (flag == 3) {
               int[][] sendBuf = new int[index + 1][2];
               System.arraycopy(inputBuf, 0, sendBuf, 0, index + 1);
               pHead = 0;
               pTail = this.kbdMapLength;
               index = 0;
               return sendBuf;
            } else {
               index++;
               return new int[0][0];
            }
         } else {
            int cnt = this.vMap[pHead].length;
            int[][] sendBuf = new int[cnt][2];

            for (int ix = 0; ix < cnt; ix++) {
               sendBuf[ix][0] = this.vMap[pHead][ix];
               sendBuf[ix][1] = keyEventType;
            }

            if (cnt == 1) {
               sendBuf[0][1] = keyEventType;
            }

            pHead = 0;
            pTail = this.kbdMapLength;
            index = 0;
            return sendBuf;
         }
      }
   }

   public void showVector(Vector v, String msg) {
      if (this.tracer.isOn()) {
         this.tracer.println(" vector size: " + v.size());
      }

      synchronized (v) {
         Enumeration e = v.elements();

         while (e.hasMoreElements()) {
            this.showArray((int[])e.nextElement());
         }
      }
   }

   public void showArray(int[] a) {
      String sTmp = "";

      for (int j = 0; j < a.length; j++) {
         sTmp = sTmp + "0x" + Integer.toHexString(a[j]) + " ";
      }

      if (this.tracer.isOn()) {
         this.tracer.println(sTmp);
      }
   }

   public void showArray(int[][] a2, int length, String msg) {
      if (this.tracer.isOn()) {
         this.tracer.println(msg + " length=" + length + " contains:");
      }

      for (int i = 0; i < length; i++) {
         this.showArray(a2[i]);
      }
   }

   public void setCapKeyUp(boolean cku) {
      if (this.doubleKeyProcessor != null) {
         this.doubleKeyProcessor.setCapKeyUp(cku);
      }
   }

   private class makeArrayFromVector {
      private int length;
      private int maxWidth;
      private int[][] array2 = new int[0][0];

      makeArrayFromVector(Vector v) {
         this.maxWidth = 0;
         synchronized (v) {
            this.length = v.size();
            this.array2 = new int[this.length][];

            for (int i = 0; i < this.length; i++) {
               this.array2[i] = (int[])v.elementAt(i);
               if (this.array2[i].length > this.maxWidth) {
                  this.maxWidth = this.array2[i].length;
               }
            }
         }
      }

      public int getArrayMaxWidth() {
         return this.maxWidth;
      }

      public int getArrayLenght() {
         return this.length;
      }

      public int[][] getArray() {
         return this.array2;
      }
   }
}
