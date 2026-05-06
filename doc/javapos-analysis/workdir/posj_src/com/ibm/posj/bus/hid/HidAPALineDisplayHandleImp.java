package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.ReportEvent;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.LineDisplayCmd;
import java.util.Arrays;

public class HidAPALineDisplayHandleImp extends HidLineDisplayHandleImp {
   private HidAPALineDisplayHandleImp.APALineDisplaySubmitV APAldSubmitV = new HidAPALineDisplayHandleImp.APALineDisplaySubmitV();
   private byte init_mode_byte = -1;
   private static final byte[] WRITE_MODE_CMD = new byte[]{-95, 0};
   private static final byte[] READ_MODE_CMD = new byte[]{-79, 0};
   private static final byte UP_SET_SCREEN_MODE = -128;
   private static final byte NORMAL_SCREEN_MODE = 127;
   private static final int CS_ENGLISH = 0;
   private static final int CS_HUNGARY = 1;
   private static final int CS_CYRILLIC = 2;
   private static final int CS_TURKEY = 3;
   private static final int CS_MULTILINGUAL = 4;
   private static final int CS_ISRAEL = 5;
   private static final int CS_CANADIAN_FRENCH = 6;
   private static final int CS_ARABIC = 7;
   private static final int CS_NORDIC = 8;
   private static final int CS_CYRILLIC_RUSSIA = 9;
   private static final int CS_GREEK = 10;
   private static final int CS_KATAKANA = 11;
   private static final int CS_JAPAN = 12;
   private static final int CS_KOREAN = 13;
   private static final int SET_ENGLISH = 0;
   private static final int SET_HUNGARY = 3;
   private static final int SET_CYRILLIC = 4;
   private static final int SET_TURKEY = 5;
   private static final int SET_MULTILINGUAL = 2;
   private static final int SET_ISRAEL = 6;
   private static final int SET_CANADIAN_FRENCH = 7;
   private static final int SET_ARABIC = 8;
   private static final int SET_NORDIC = 9;
   private static final int SET_CYRILLIC_RUSSIA = 10;
   private static final int SET_GREEK = 11;
   private static final int SET_KATAKANA = 1;
   private static final int SET_JAPAN = 128;
   private static final int SET_KOREAN = 144;
   private Object lockObject = new Object();

   public HidAPALineDisplayHandleImp(HandleKey key, HidDevice device) {
      super(key, device);
   }

   public void init() throws HandleException {
      super.init();
      this.setDevIDPosition(3);
      this.setStatusByte(1);
      this.getLDSubmitV().submit(READ_MODE_CMD, "could not submit READ_MODE_CMD");
      if (this.isTracerOn()) {
         this.traceNormal("init() getInitModeByte -> " + this.getInitModeByte());
      }
   }

   protected HidLineDisplayHandleImp.LineDisplaySubmitV getLDSubmitV() throws HandleException {
      this.ldSubmitV = this.APAldSubmitV;
      return this.ldSubmitV;
   }

   protected void reportEventOccurred(ReportEvent rE) {
      super.reportEventOccurred(rE);
      if (this.isTracerOn()) {
         this.traceNormal("reportEventOccurred()-> " + Util.toFormatedHexString(rE.getData()));
      }

      if (this.getInitModeByte() == -1) {
         byte[] data = rE.getData();
         this.setInitModeByte(data[1]);
         if (this.isTracerOn()) {
            this.traceNormal("reportEventOccurred() getInitModeByte()-> " + this.getInitModeByte());
         }

         READ_MODE_CMD[1] = data[1];
         synchronized (this.lockObject) {
            this.lockObject.notifyAll();
         }
      }
   }

   protected void restoreGlyphs() throws HandleException {
      this.getLDSubmitV().submit(WRITE_MODE_CMD, "failed to submit WRITE_MODE_CMD");
   }

   private byte getInitModeByte() {
      return this.init_mode_byte;
   }

   private void setInitModeByte(byte b) {
      this.init_mode_byte = b;
   }

   private int[][] getCharacterSetValues() {
      return new int[][]{{0, 11, 4, 1, 2, 3, 5, 6, 7, 8, 9, 10, 12, 13}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 128, 144}};
   }

   private class APALineDisplaySubmitV extends HidLineDisplayHandleImp.LineDisplaySubmitV {
      private ByteBuffer buffer = new ByteBuffer();

      private APALineDisplaySubmitV() {
      }

      public void visitSetCharacterSetCmd(LineDisplayCmd cmd) {
         LineDisplayCmd.SetCharacterSetCmd scsC = (LineDisplayCmd.SetCharacterSetCmd)cmd;
         boolean charSetfound = false;
         int charSetCounter = 0;
         int correspondingCharSet = -1;

         for (this.characterSetCmd[0] = 65;
            charSetCounter < HidAPALineDisplayHandleImp.this.getCharacterSetValues()[0].length && !charSetfound;
            charSetCounter++
         ) {
            if (scsC.getCharacterSet() == HidAPALineDisplayHandleImp.this.getCharacterSetValues()[0][charSetCounter]) {
               charSetfound = true;
               correspondingCharSet = HidAPALineDisplayHandleImp.this.getCharacterSetValues()[1][charSetCounter];
            }
         }

         if (correspondingCharSet != -1) {
            this.characterSetCmd[1] = (byte)correspondingCharSet;
            this.submit(this.characterSetCmd, "Could not submit characterSet");
            this.submit(DEVICE_INFO_REQUEST, "could not submit deviceInfoRequest");
         } else {
            this.handleException = new HandleException("Unsupported character set value to set");
         }
      }

      public void visitClearDisplayCmd(LineDisplayCmd cmd) {
         LineDisplayCmd.ClearDisplayCmd cdC = (LineDisplayCmd.ClearDisplayCmd)cmd;
         this.shortCmd[0] = 1;
         this.shortCmd[1] = cdC.getCursorLocation();
         this.submit(this.shortCmd, "Could not Clear Top Line");
         this.shortCmd[0] = 2;
         this.shortCmd[1] = cdC.getCursorLocation();
         this.submit(this.shortCmd, "Could not Clear Botton Line");
         this.setIsClearLastCmd(true);
         Arrays.fill(this.writeTopLineCmd, 2, this.writeTopLineCmd.length, (byte)32);
         Arrays.fill(this.writeBottomLineCmd, 2, this.writeBottomLineCmd.length, (byte)32);
      }

      public void visitDisplayTextModeCmd(LineDisplayCmd cmd) {
         LineDisplayCmd.DisplayTextModeCmd dtM = (LineDisplayCmd.DisplayTextModeCmd)cmd;
         if (dtM.getDisplayTextMode() != 0 && dtM.getDisplayTextMode() != 1) {
            this.handleException = new HandleException("Unsupported display mode value to set");
         }

         switch (dtM.getDisplayTextMode()) {
            case 0:
               HidAPALineDisplayHandleImp.WRITE_MODE_CMD[1] = (byte)(127 & HidAPALineDisplayHandleImp.this.getInitModeByte());
               HidAPALineDisplayHandleImp.this.setInitModeByte(HidAPALineDisplayHandleImp.WRITE_MODE_CMD[1]);
               break;
            case 1:
               HidAPALineDisplayHandleImp.WRITE_MODE_CMD[1] = (byte)(-128 | HidAPALineDisplayHandleImp.this.getInitModeByte());
               HidAPALineDisplayHandleImp.this.setInitModeByte(HidAPALineDisplayHandleImp.WRITE_MODE_CMD[1]);
               break;
            default:
               this.handleException = new HandleException("Unsupported display mode value");
         }

         this.submit(HidAPALineDisplayHandleImp.WRITE_MODE_CMD, "MODE_BYTE failed");
         this.shortCmd[0] = 1;
         this.shortCmd[1] = 0;
         this.submit(this.shortCmd, "could not submit clearCmd");
         this.shortCmd[0] = 2;
         this.shortCmd[1] = 0;
         this.submit(this.shortCmd, "could not submit clearCmd");
         this.submit(this.writeTopLineCmd, "writeTopLineCmd failed");
         this.submit(this.writeBottomLineCmd, "writeBottomLineCmd failed");
      }

      protected void submit(byte[] data, String errorMsg) {
         this.buffer.clean();
         this.buffer.append(1);
         this.buffer.append(data);
         super.submit(this.buffer.getBytes(), errorMsg);
      }
   }
}
