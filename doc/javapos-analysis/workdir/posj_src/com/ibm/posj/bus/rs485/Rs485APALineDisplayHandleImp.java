package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jutil.Util;
import com.ibm.posj.DefaultLineDisplayCmdV;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.LineDisplayCmd;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.LineDisplayHandleImp;
import java.util.Arrays;

public class Rs485APALineDisplayHandleImp extends Rs485LineDisplayHandleImp implements LineDisplayHandleImp {
   protected Rs485APALineDisplayHandleImp.LDSubmitV ldSubmitV = new Rs485APALineDisplayHandleImp.LDSubmitV();
   private Rs485LineDisplayHandleImp.HandleCmdFilterV handleCmdFilterV = new Rs485LineDisplayHandleImp.HandleCmdFilterV();
   private SioDevice device = null;
   public HandleException handleException = null;
   private static final byte[] write_top_display_cmd = new byte[22];
   private static final byte[] write_bottom_display_cmd = new byte[22];
   private byte init_mode_byte = -1;
   private boolean isInternalCall = false;
   private byte getCharacterSetFromSystem;
   private Object lockObject = new Object();
   private static final byte[] CLEAR_TOPLINE_CMD = new byte[]{1, 0};
   private static final byte[] CLEAR_BOTTOMLINE_CMD = new byte[]{2, 0};
   private static final byte WRITE_TOP_LINE_CMD = 65;
   private static final byte WRITE_BOTTOM_LINE_CMD = 66;
   private static final byte[] INDICATORS_CMD = new byte[]{16, 0, 0, 0, 0, 0};
   private static final byte DBCS_CMD = 16;
   private static final byte SBCS_CMD = 0;
   private static final byte UP_SET_SCREEN_MODE_CMD = 64;
   private static final byte NORMAL_SCREEN_MODE_CMD = -65;
   private static final byte[] MODE_BYTE = new byte[]{-95, 0};
   private static final byte[] READ_MODE_BYTE = new byte[]{-79, 0};
   private static final int SET_MULTINATIONAL = 0;
   private static final int SET_JAPAN = 128;
   private static final int SET_KOREAN = 129;
   private static final int WRITE_LINE_DATA_LENGTH = 20;
   private static final int DISP_CS_ENGLISH = 0;
   private static final int DISP_CS_MULTINATIONAL = 1;
   private static final int DISP_CS_JAPAN = 2;
   private static final int DISP_CS_KOREAN = 3;
   private static final int DISP_CS_ASCII = 4;

   public Rs485APALineDisplayHandleImp(HandleKey key, SioDevice sioDevice) {
      super(key, sioDevice);
      this.device = sioDevice;
   }

   public void init() throws HandleException {
      super.init();
      this.getLDInstanceV().submit(READ_MODE_BYTE, "could not submit READ_MODE_BYTE");
      if (this.isTracerOn()) {
         this.traceNormal("init() getInitModeByte -> " + this.getInitModeByte());
      }

      if (this.getInitModeByte() == -1) {
         synchronized (this.lockObject) {
            try {
               this.lockObject.wait(5000L);
            } catch (InterruptedException var4) {
            }
         }
      }

      if (this.getInitModeByte() == -1) {
         if (this.isTracerOn()) {
            this.traceNormal("Unable to identify the init mode LineDisplay timeout 5000 ms -> " + this.getInitModeByte());
         }

         throw new HandleException("Unable to identify the init mode LineDisplay timeout 5000 ms");
      }
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            this.handleCmdFilterV.reset();
            cmd.accept(this.handleCmdFilterV);
            if (this.handleCmdFilterV.isSystemCmd()) {
               if (cmd.getCode() == 101) {
                  Rs485APALineDisplayHandleImp.LDSubmitV var10000 = this.getLDInstanceV();
                  this.getLDSubmitV();
                  var10000.submitSystemCmd(Rs485LineDisplayHandleImp.LineDisplaySubmitV.STATUS_REQUEST_CMD, "STATUS_REQUEST_CMD");
               } else if (cmd.getCode() == 100) {
                  Rs485APALineDisplayHandleImp.LDSubmitV var8 = this.getLDInstanceV();
                  this.getLDSubmitV();
                  var8.submitSystemCmd(Rs485LineDisplayHandleImp.LineDisplaySubmitV.TEST_REQUEST_CMD, "TEST_REQUEST_CMD");
               } else if (cmd.getCode() == 102) {
                  Rs485APALineDisplayHandleImp.LDSubmitV var9 = this.getLDInstanceV();
                  this.getLDSubmitV();
                  var9.submitSystemCmd(Rs485LineDisplayHandleImp.LineDisplaySubmitV.RESET_CMD, "RESET_CMD");
               } else {
                  if (cmd.getCode() != 103) {
                     throw new HandleException("Invalid SystemCmd submitted!");
                  }

                  this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)cmd);
               }
            } else {
               if (!this.handleCmdFilterV.isLineDisplayCmd()) {
                  throw new HandleException("Invalid LineDisplayCmd object submitted!");
               }

               LineDisplayCmd lineDisplayCmd = (LineDisplayCmd)cmd;
               this.getLDInstanceV().clearHandleException();
               lineDisplayCmd.accept(this.getLDInstanceV());
               if (null != this.getLDInstanceV().getHandleException()) {
                  throw this.getLDInstanceV().getHandleException();
               }
            }
         } catch (HandleException var6) {
            this.setHandleCmdResultInError(cmd, true);
            throw var6;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   protected void reinitialize() throws HandleException {
      if (this.isTracerOn()) {
         this.traceNormal("reintilize() has been called");
      }

      this.setIsInternalCall(true);
      this.getLDInstanceV().submit(CLEAR_TOPLINE_CMD, "failed to submit clearTopLineCmd");
      this.getLDInstanceV().submit(CLEAR_BOTTOMLINE_CMD, "failed to submit clearBottomLineCmd");
      this.getLDInstanceV().submit(MODE_BYTE, "failed to submit MODE_BYTE");
      this.getLDInstanceV().submit(write_top_display_cmd, "failed to submit write_top_display_cmd");
      this.getLDInstanceV().submit(write_bottom_display_cmd, "failed to submit write_bottom_display_cmd");
      this.setIsInternalCall(false);
   }

   private Rs485APALineDisplayHandleImp.LDSubmitV getLDInstanceV() throws HandleException {
      return this.ldSubmitV;
   }

   protected void dataEventOccurred(SioDeviceDataEvent event) {
      super.dataEventOccurred(event);
      if (this.isTracerOn()) {
         this.traceNormal("dataEventOccurred()-> " + Util.toFormatedHexString(event.getData()));
      }

      if (this.getInitModeByte() == -1) {
         byte[] data = event.getData();
         this.setInitModeByte(data[1]);
         if (this.isTracerOn()) {
            this.traceNormal("dataEventOccurred() getInitModeByte()-> " + this.getInitModeByte());
         }

         MODE_BYTE[1] = data[1];
         this.getCharacterSetFromSystem = data[2];
         synchronized (this.lockObject) {
            this.lockObject.notifyAll();
         }
      }
   }

   private int[][] getCharacterSetValues() {
      return new int[][]{{0, 1, 2, 3, 4}, {0, 0, 128, 129, 0}};
   }

   private boolean getIsInternalCall() {
      return this.isInternalCall;
   }

   private void setIsInternalCall(boolean b) {
      this.isInternalCall = b;
   }

   private byte getInitModeByte() {
      return this.init_mode_byte;
   }

   private void setInitModeByte(byte b) {
      this.init_mode_byte = b;
   }

   protected class LDSubmitV extends DefaultLineDisplayCmdV {
      public void clearHandleException() {
         Rs485APALineDisplayHandleImp.this.handleException = null;
      }

      public HandleException getHandleException() {
         return Rs485APALineDisplayHandleImp.this.handleException;
      }

      public void visitClearDisplayCmd(LineDisplayCmd cmd) {
         try {
            LineDisplayCmd.ClearDisplayCmd wdC = (LineDisplayCmd.ClearDisplayCmd)cmd;
            Rs485APALineDisplayHandleImp.this.getLDInstanceV().submit(Rs485APALineDisplayHandleImp.CLEAR_TOPLINE_CMD, "clear top cmd failed");
            Rs485APALineDisplayHandleImp.this.getLDInstanceV().submit(Rs485APALineDisplayHandleImp.CLEAR_BOTTOMLINE_CMD, "CLEAR_BOTTOM_LINE_CMD failed");
            if (!Rs485APALineDisplayHandleImp.this.getIsInternalCall()) {
               Arrays.fill(Rs485APALineDisplayHandleImp.write_top_display_cmd, 2, Rs485APALineDisplayHandleImp.write_top_display_cmd.length, (byte)32);
               Arrays.fill(Rs485APALineDisplayHandleImp.write_bottom_display_cmd, 2, Rs485APALineDisplayHandleImp.write_bottom_display_cmd.length, (byte)32);
            }
         } catch (HandleException var3) {
            Rs485APALineDisplayHandleImp.this.handleException = new HandleException("could not clear the Display", var3);
            if (Rs485APALineDisplayHandleImp.this.getTracer().isOn()) {
               Rs485APALineDisplayHandleImp.this.getTracer().print(var3);
            }
         }
      }

      public void visitWriteDisplayCmd(LineDisplayCmd cmd) {
         try {
            LineDisplayCmd.WriteDisplayCmd wdC = (LineDisplayCmd.WriteDisplayCmd)cmd;
            if (wdC.getRowPosition() == 0) {
               Rs485APALineDisplayHandleImp.write_top_display_cmd[0] = 65;
               Rs485APALineDisplayHandleImp.write_top_display_cmd[1] = 0;
               System.arraycopy(wdC.getData(), 0, Rs485APALineDisplayHandleImp.write_top_display_cmd, 2, 20);
               Rs485APALineDisplayHandleImp.this.getLDInstanceV().submit(Rs485APALineDisplayHandleImp.write_top_display_cmd, "write_top_display_cmd failed");
            } else if (wdC.getRowPosition() == 1) {
               Rs485APALineDisplayHandleImp.write_bottom_display_cmd[0] = 66;
               Rs485APALineDisplayHandleImp.write_bottom_display_cmd[1] = 0;
               System.arraycopy(wdC.getData(), 0, Rs485APALineDisplayHandleImp.write_bottom_display_cmd, 2, 20);
               Rs485APALineDisplayHandleImp.this.getLDInstanceV()
                  .submit(Rs485APALineDisplayHandleImp.write_bottom_display_cmd, "write_bottom_display_cmd failed");
            }
         } catch (HandleException var3) {
            Rs485APALineDisplayHandleImp.this.handleException = new HandleException("could not write to Display", var3);
         }
      }

      public void visitSetCharacterSetCmd(LineDisplayCmd cmd) {
         try {
            LineDisplayCmd.SetCharacterSetCmd cS = (LineDisplayCmd.SetCharacterSetCmd)cmd;
            boolean charSetfound = false;
            int charSetCounter = 0;

            int correspondingCharSet;
            for (correspondingCharSet = -1;
               charSetCounter < Rs485APALineDisplayHandleImp.this.getCharacterSetValues()[0].length && !charSetfound;
               charSetCounter++
            ) {
               if (cS.getCharacterSet() == Rs485APALineDisplayHandleImp.this.getCharacterSetValues()[0][charSetCounter]) {
                  charSetfound = true;
                  correspondingCharSet = Rs485APALineDisplayHandleImp.this.getCharacterSetValues()[1][charSetCounter];
               }
            }

            if (correspondingCharSet != -1) {
               if (Rs485APALineDisplayHandleImp.this.getCharacterSetFromSystem != (byte)correspondingCharSet && 0 != (byte)correspondingCharSet) {
                  Rs485APALineDisplayHandleImp.this.handleException = new HandleException("VFDII/APA Hardware does not have the selected font set");
               }

               switch (correspondingCharSet) {
                  case 0:
                     Rs485APALineDisplayHandleImp.MODE_BYTE[1] = (byte)(0 | Rs485APALineDisplayHandleImp.this.getInitModeByte());
                     Rs485APALineDisplayHandleImp.this.setInitModeByte(Rs485APALineDisplayHandleImp.MODE_BYTE[1]);
                     break;
                  case 128:
                  case 129:
                     Rs485APALineDisplayHandleImp.MODE_BYTE[1] = (byte)(16 | Rs485APALineDisplayHandleImp.this.getInitModeByte());
                     Rs485APALineDisplayHandleImp.this.setInitModeByte(Rs485APALineDisplayHandleImp.MODE_BYTE[1]);
                     break;
                  default:
                     Rs485APALineDisplayHandleImp.this.handleException = new HandleException("Unsupported character set value to set");
               }

               Rs485APALineDisplayHandleImp.this.getLDInstanceV().submit(Rs485APALineDisplayHandleImp.MODE_BYTE, "Could not set characterSetCmd");
            } else {
               Rs485APALineDisplayHandleImp.this.handleException = new HandleException("Unsupported character set value to set");
            }
         } catch (HandleException var6) {
            Rs485APALineDisplayHandleImp.this.handleException = new HandleException("could not submit character set cmd", var6);
         }
      }

      public void visitWriteTriangleMarksCmd(LineDisplayCmd cmd) {
         try {
            LineDisplayCmd.WriteTriangleMarksCmd wtM = (LineDisplayCmd.WriteTriangleMarksCmd)cmd;
            System.arraycopy(wtM.getIndicators(), 0, Rs485APALineDisplayHandleImp.INDICATORS_CMD, 2, wtM.getIndicators().length);
            Rs485APALineDisplayHandleImp.this.getLDInstanceV().submit(Rs485APALineDisplayHandleImp.INDICATORS_CMD, "INDICATORS_CMD failed");
         } catch (HandleException var3) {
            Rs485APALineDisplayHandleImp.this.handleException = new HandleException("could not write indicators to Display", var3);
         }
      }

      public void visitTestLDRequestCmd(LineDisplayCmd cmd) {
      }

      public void visitBrightnessCmd(LineDisplayCmd cmd) {
         throw new RuntimeException("Invalid LineDisplayCmd submitted");
      }

      public void visitScreenModeCmd(LineDisplayCmd cmd) {
         throw new RuntimeException("Invalid LineDisplayCmd submitted");
      }

      public void visitDisplayModeCmd(LineDisplayCmd cmd) {
         throw new RuntimeException("Invalid LineDisplayCmd submitted");
      }

      public void visitCursorModeCmd(LineDisplayCmd cmd) {
         throw new RuntimeException("Invalid LineDisplayCmd submitted");
      }

      public void visitUserDefinableCharacterCmd(LineDisplayCmd cmd) {
         throw new RuntimeException("Invalid LineDisplayCmd submitted");
      }

      public void visitDisplayTextModeCmd(LineDisplayCmd cmd) {
         try {
            Rs485APALineDisplayHandleImp.this.setIsInternalCall(true);
            LineDisplayCmd.DisplayTextModeCmd dtM = (LineDisplayCmd.DisplayTextModeCmd)cmd;
            if (dtM.getDisplayTextMode() != 0 && dtM.getDisplayTextMode() != 1) {
               Rs485APALineDisplayHandleImp.this.handleException = new HandleException("Unsupported display mode value to set");
            }

            switch (dtM.getDisplayTextMode()) {
               case 0:
                  Rs485APALineDisplayHandleImp.MODE_BYTE[1] = (byte)(-65 & Rs485APALineDisplayHandleImp.this.getInitModeByte());
                  Rs485APALineDisplayHandleImp.this.setInitModeByte(Rs485APALineDisplayHandleImp.MODE_BYTE[1]);
                  break;
               case 1:
                  Rs485APALineDisplayHandleImp.MODE_BYTE[1] = (byte)(64 | Rs485APALineDisplayHandleImp.this.getInitModeByte());
                  Rs485APALineDisplayHandleImp.this.setInitModeByte(Rs485APALineDisplayHandleImp.MODE_BYTE[1]);
                  break;
               default:
                  Rs485APALineDisplayHandleImp.this.handleException = new HandleException("Unsupported display mode value to set to set");
            }

            Rs485APALineDisplayHandleImp.this.getLDInstanceV().submit(Rs485APALineDisplayHandleImp.MODE_BYTE, "MODE_BYTE failed");
            Rs485APALineDisplayHandleImp.this.getLDInstanceV().submit(Rs485APALineDisplayHandleImp.CLEAR_TOPLINE_CMD, "CLEAR_TOP_LINE_CMD failed");
            Rs485APALineDisplayHandleImp.this.getLDInstanceV().submit(Rs485APALineDisplayHandleImp.CLEAR_BOTTOMLINE_CMD, "CLEAR_BOTTOM_LINE_CMD failed");
            Rs485APALineDisplayHandleImp.this.getLDInstanceV().submit(Rs485APALineDisplayHandleImp.write_top_display_cmd, "write_top_display_cmd failed");
            Rs485APALineDisplayHandleImp.this.getLDInstanceV().submit(Rs485APALineDisplayHandleImp.write_bottom_display_cmd, "write_top_display_cmd failed");
            Rs485APALineDisplayHandleImp.this.setIsInternalCall(false);
         } catch (HandleException var3) {
            Rs485APALineDisplayHandleImp.this.handleException = new HandleException("could not submit displayModeTextCmd", var3);
         }
      }

      protected void submit(byte[] data, String errorMsg) throws HandleException {
         Rs485APALineDisplayHandleImp.this.submitSync(data);
      }

      protected void submitSystemCmd(byte[] data, String errorMsg) throws HandleException {
         this.submit(data, errorMsg);
      }
   }
}
