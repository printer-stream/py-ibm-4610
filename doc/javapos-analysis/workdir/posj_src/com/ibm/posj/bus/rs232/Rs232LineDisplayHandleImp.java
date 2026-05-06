package com.ibm.posj.bus.rs232;

import com.ibm.posj.DefaultLineDisplayCmdV;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.LineDisplayCmd;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.LineDisplayHandleImp;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.rs232.Rs232Port;

public abstract class Rs232LineDisplayHandleImp extends AbstractRs232HandleImp implements LineDisplayHandleImp {
   protected HandleException handleException = null;
   private Rs232LineDisplayHandleImp.LineDisplaySubmitV ldSubmitV = new Rs232LineDisplayHandleImp.LineDisplaySubmitV();
   protected byte cursorStateValue = -1;
   public static final byte CURSOR_ON = 1;
   public static final byte CURSOR_OFF = 0;
   public static final int DISP_CS_ENGLISH = 0;

   public Rs232LineDisplayHandleImp(HandleKey key, Rs232Port rs232Port) {
      super(key, rs232Port);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitLineDisplay(this);
   }

   public DevCat getDevCat() {
      return DevCats.LINEDISPLAY_DEVCAT;
   }

   public short getECLevel() {
      return -1;
   }

   public boolean isFlashable() {
      return false;
   }

   public void init() throws HandleException {
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            if (cmd instanceof LineDisplayCmd) {
               LineDisplayCmd lineDisplayCmd = (LineDisplayCmd)cmd;
               this.ldSubmitV.clearHandleException();
               lineDisplayCmd.accept(this.ldSubmitV);
               if (null != this.ldSubmitV.getHandleException()) {
                  throw this.ldSubmitV.getHandleException();
               }
            } else if (cmd instanceof SystemCmd && cmd.getCode() == 103) {
               this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)cmd);
            }
         } catch (HandleException var6) {
            this.setHandleCmdResultInError(cmd, true);
            throw var6;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   protected abstract void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd var1);

   protected abstract void visitWriteDisplayCmd(LineDisplayCmd var1);

   protected abstract void visitClearDisplayCmd(LineDisplayCmd var1);

   protected abstract void visitWriteTriangleMarksCmd(LineDisplayCmd var1);

   protected abstract void visitBrightnessCmd(LineDisplayCmd var1);

   protected abstract void visitSetCharacterSetCmd(LineDisplayCmd var1);

   protected abstract void visitScreenModeCmd(LineDisplayCmd var1);

   protected abstract void visitDisplayModeCmd(LineDisplayCmd var1);

   protected abstract void visitCursorModeCmd(LineDisplayCmd var1);

   protected abstract void visitUserDefinableCharacterCmd(LineDisplayCmd var1);

   protected abstract void visitDisplayTextModeCmd(LineDisplayCmd var1);

   protected synchronized void submit(byte[] data, String errorMsg) {
      try {
         this.submit(data);
      } catch (HandleException var4) {
         this.handleException = new HandleException(errorMsg, var4);
      }
   }

   protected void setCursorStateValue(byte cursorState) {
      this.cursorStateValue = cursorState;
   }

   protected byte getCursorStateValue() {
      return this.cursorStateValue;
   }

   protected class LineDisplaySubmitV extends DefaultLineDisplayCmdV {
      public void clearHandleException() {
         Rs232LineDisplayHandleImp.this.handleException = null;
      }

      public HandleException getHandleException() {
         return Rs232LineDisplayHandleImp.this.handleException;
      }

      public void visitWriteDisplayCmd(LineDisplayCmd cmd) {
         Rs232LineDisplayHandleImp.this.visitWriteDisplayCmd(cmd);
      }

      public void visitClearDisplayCmd(LineDisplayCmd cmd) {
         Rs232LineDisplayHandleImp.this.visitClearDisplayCmd(cmd);
      }

      public void visitSetCharacterSetCmd(LineDisplayCmd cmd) {
         Rs232LineDisplayHandleImp.this.visitSetCharacterSetCmd(cmd);
      }

      public void visitWriteTriangleMarksCmd(LineDisplayCmd cmd) {
         Rs232LineDisplayHandleImp.this.visitWriteTriangleMarksCmd(cmd);
      }

      public void visitBrightnessCmd(LineDisplayCmd cmd) {
         Rs232LineDisplayHandleImp.this.visitBrightnessCmd(cmd);
      }

      public void visitScreenModeCmd(LineDisplayCmd cmd) {
         Rs232LineDisplayHandleImp.this.visitScreenModeCmd(cmd);
      }

      public void visitDisplayModeCmd(LineDisplayCmd cmd) {
         Rs232LineDisplayHandleImp.this.visitDisplayModeCmd(cmd);
      }

      public void visitCursorModeCmd(LineDisplayCmd cmd) {
         Rs232LineDisplayHandleImp.this.visitCursorModeCmd(cmd);
      }

      public void visitUserDefinableCharacterCmd(LineDisplayCmd cmd) {
         Rs232LineDisplayHandleImp.this.visitUserDefinableCharacterCmd(cmd);
      }

      public void visitDisplayTextModeCmd(LineDisplayCmd cmd) {
         Rs232LineDisplayHandleImp.this.visitDisplayTextModeCmd(cmd);
      }

      protected byte checkSum(byte[] data, int length) {
         byte checkSum = data[0];

         for (int i = 1; i < length; i++) {
            checkSum ^= data[i];
         }

         return checkSum;
      }
   }
}
