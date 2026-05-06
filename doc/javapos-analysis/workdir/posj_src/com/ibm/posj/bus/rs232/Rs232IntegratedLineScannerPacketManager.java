package com.ibm.posj.bus.rs232;

public abstract class Rs232IntegratedLineScannerPacketManager {
   static byte[] ackFrame = new byte[]{2, 0, 6, 0, 3};
   static byte[] resendFrame = new byte[]{2, 0, 5, 0, 3};
   static byte[] autoSyncFrame = new byte[]{2, 0, 22, 0, 3};

   public static byte[] encodeDLE(byte[] buffer) {
      byte[] newBuffer = new byte[buffer.length * 2];
      int i = 0;

      int j;
      for (j = 0; i < buffer.length; i++) {
         if (buffer[i] != 2 && buffer[i] != 16 && buffer[i] != 3) {
            newBuffer[j++] = buffer[i];
         } else {
            newBuffer[j++] = 16;
            newBuffer[j++] = (byte)(buffer[i] | 64);
         }
      }

      byte[] retBuffer = new byte[j + 2];
      System.arraycopy(newBuffer, 0, retBuffer, 1, j);
      retBuffer[0] = 2;
      retBuffer[j + 1] = 3;
      return retBuffer;
   }

   public static byte[] decodeDLE(byte[] buffer, int length) {
      byte[] newBuffer = new byte[length - 2];
      int i = 1;

      int j;
      for (j = 0; i < length - 1; j++) {
         if (buffer[i] == 16) {
            newBuffer[j] = (byte)(buffer[++i] & 191);
         } else {
            newBuffer[j] = buffer[i];
         }

         i++;
      }

      byte[] retBuffer = new byte[j];
      System.arraycopy(newBuffer, 0, retBuffer, 0, j);
      return retBuffer;
   }

   public static byte[] createHighLevelFrame(byte sequenceNumber, byte[] dataBuffer, byte frameManagement) {
      byte[] buff_noDLE = new byte[dataBuffer.length + 4];
      buff_noDLE[0] = sequenceNumber;
      System.arraycopy(dataBuffer, 0, buff_noDLE, 1, dataBuffer.length);
      buff_noDLE[dataBuffer.length + 1] = frameManagement;
      int checkSum = calculateCheckSum(buff_noDLE, dataBuffer.length + 2);
      buff_noDLE[dataBuffer.length + 2] = (byte)((checkSum & 0xFF00) >> 8);
      buff_noDLE[dataBuffer.length + 3] = (byte)(checkSum & 0xFF);
      return encodeDLE(buff_noDLE);
   }

   public static int calculateCheckSum(byte[] pcData, int length) {
      int Temp = 0;
      int Sum = 0;

      for (int i = 0; i < length; i++) {
         Temp += pcData[i] & 255;
         Sum += Temp;
      }

      return Sum & 65535;
   }

   public static boolean checkCheckSum(byte[] buff, int receivedCheckSum) {
      int calculatedCheckSum = calculateCheckSum(buff, buff.length);
      return calculatedCheckSum == receivedCheckSum;
   }

   public static byte getFrameNumber(byte[] highLevelFrame) {
      return (byte)(highLevelFrame[highLevelFrame.length - 1] & 7);
   }

   public static boolean isRestartFrame(byte[] highLevelFrame) {
      return (highLevelFrame[highLevelFrame.length - 1] & 8) == 8;
   }

   public static byte getFrameType(byte[] frame) {
      return frame[1];
   }
}
