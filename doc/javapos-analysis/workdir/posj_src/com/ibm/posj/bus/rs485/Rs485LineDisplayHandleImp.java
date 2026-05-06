package com.ibm.posj.bus.rs485;

import com.ibm.jsio.SioDevice;
import com.ibm.jsio.event.SioDeviceDataEvent;
import com.ibm.jutil.Util;
import com.ibm.posj.GenLineDisplayCmdV;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.LineDisplayCmd;
import com.ibm.posj.LineDisplayHandle;
import com.ibm.posj.PosSystem;
import com.ibm.posj.PosSystemManager;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.LineDisplayHandleImp;
import com.ibm.posj.util.DefaultHandleCmdV;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PosjUtil;

public class Rs485LineDisplayHandleImp extends AbstractRs485HandleImp implements LineDisplayHandleImp {
   private long cmdTimeout = 100L;
   protected Rs485LineDisplayHandleImp.LineDisplaySubmitV ldSubmitV = new Rs485LineDisplayHandleImp.LineDisplaySubmitV();
   private Rs485LineDisplayHandleImp.HandleCmdFilterV handleCmdFilterV = new Rs485LineDisplayHandleImp.HandleCmdFilterV();
   private SioDevice device = null;
   private int deviceId = -1;
   private int deviceType = -1;
   private Object lockObj = new Object();
   private Object waitDataLock = new Object();
   public static final byte[] CLEAR_TOP = new byte[]{-127, 0, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32};
   public static final byte[] CLEAR_BOTTOM = new byte[]{-126, 0, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32};
   public static final long DEFAULT_COMMAND_TIMEOUT = 100L;
   public static final String COMMAND_TIMEOUT_PROP_NAME = "com.ibm.posj.bus.rs485.Rs485LineDisplayHandleImp.COMMAND_TIMEOUT";

   public Rs485LineDisplayHandleImp(HandleKey key, SioDevice sioDevice) {
      super(key, sioDevice);
      this.device = sioDevice;
   }

   public void init() throws HandleException {
      super.init();
      this.initCmdTimeOut();
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
                  Rs485LineDisplayHandleImp.LineDisplaySubmitV var10000 = this.getLDSubmitV();
                  this.getLDSubmitV();
                  var10000.submitSystemCmd(Rs485LineDisplayHandleImp.LineDisplaySubmitV.STATUS_REQUEST_CMD, "STATUS_REQUEST_CMD");
               } else if (cmd.getCode() == 100) {
                  Rs485LineDisplayHandleImp.LineDisplaySubmitV var8 = this.getLDSubmitV();
                  this.getLDSubmitV();
                  var8.submitSystemCmd(Rs485LineDisplayHandleImp.LineDisplaySubmitV.TEST_REQUEST_CMD, "TEST_REQUEST_CMD");
               } else if (cmd.getCode() == 102) {
                  Rs485LineDisplayHandleImp.LineDisplaySubmitV var9 = this.getLDSubmitV();
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
               this.getLDSubmitV().clearHandleException();
               lineDisplayCmd.accept(this.getLDSubmitV());
               if (null != this.getLDSubmitV().getHandleException()) {
                  throw this.getLDSubmitV().getHandleException();
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

   public void accept(HandleImpVisitor visitor) {
      visitor.visitLineDisplay(this);
   }

   public DevCat getDevCat() {
      return DevCats.LINEDISPLAY_DEVCAT;
   }

   protected void dataEventOccurred(SioDeviceDataEvent event) {
      if (this.isTracerOn()) {
         this.traceNormal("-->dataEventOccurred() " + Util.toFormatedHexString(event.getData()));
      }

      if (this.isDevInfoResult(event.getData()) && this.deviceId == -1) {
         this.deviceId = this.getDeviceId(event.getData());
         synchronized (this.lockObj) {
            this.lockObj.notifyAll();
         }

         if (this.isTracerOn()) {
            this.traceNormal("dataEventOccurred(): deviceId to use ->" + this.deviceId + ", deviceType to use ->" + this.deviceType);
         }
      }

      synchronized (this.waitDataLock) {
         this.waitDataLock.notifyAll();
      }

      if (this.isTracerOn()) {
         this.traceNormal("<--dataEventOccurred() ");
      }
   }

   protected boolean isDevInfoResult(byte[] response) {
      return response.length >= 1 ? PosjUtil.isBitSelected(response[0], 4) : false;
   }

   protected int getDeviceId(byte[] response) {
      byte devId = response[2];
      switch (devId) {
         case 1:
         case 3:
            this.deviceType = 3001;
            return 3008;
         case 2:
            this.deviceType = 3001;
            return 3009;
         case 4:
            this.deviceType = 3003;
            return 3011;
         case 5:
         default:
            this.deviceType = 2001;
            return 3004;
         case 6:
            this.deviceType = 3002;
            return 3005;
         case 7:
            this.deviceType = 3002;
            if (((Rs485HandleKey)this.getHandleKey()).getSioDeviceNumber() == 36) {
               return 3006;
            }

            return 3007;
         case 8:
            this.deviceType = 3003;
            return 3011;
      }
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) throws HandleException {
      if (this.deviceId == -1) {
         synchronized (this.lockObj) {
            this.submitSync(Rs485LineDisplayHandleImp.LineDisplaySubmitV.DEVICE_INFO_REQUEST);

            try {
               this.lockObj.wait(1000L);
            } catch (InterruptedException var8) {
            }
         }

         if (this.deviceId == -1) {
            synchronized (this.lockObj) {
               this.submitSync(Rs485LineDisplayHandleImp.LineDisplaySubmitV.DEVICE_INFO_REQUEST);

               try {
                  this.lockObj.wait(1000L);
               } catch (InterruptedException var6) {
               }
            }
         }
      }

      if (this.deviceId == -1) {
         devInfoCmd.setDeviceId(3004);
      } else {
         devInfoCmd.setDeviceId(this.deviceId);
         devInfoCmd.setDeviceType(this.deviceType);
      }
   }

   protected void reinitialize() throws HandleException {
      LineDisplayCmd.ClearDisplayCmd cmd = ((LineDisplayHandle)this.getHandle()).getLineDisplayCmdFactory().createClearDisplayCmd((byte)0);
      this.getLDSubmitV().visitClearDisplayCmd(cmd);
      this.getLDSubmitV().submit(this.getLDSubmitV().characterSetCmd, "could not submit characterSetCmd");

      for (int i = 0; i < this.getLDSubmitV().writeRamDisplayBKP.length; i++) {
         byte var10000 = this.getLDSubmitV().writeRamDisplayBKP[i][1];
         this.getLDSubmitV();
         if (var10000 == 64) {
            this.getLDSubmitV().submit(this.getLDSubmitV().writeRamDisplayBKP[i], "could not submit writeRamCmd");
         }
      }

      this.getLDSubmitV().submit(this.getLDSubmitV().writeTopLineCmd, "could not submit writeTopCmd");
      this.getLDSubmitV().submit(this.getLDSubmitV().writeBottomLineCmd, "could not submit writeBottomCmd");
   }

   protected Rs485LineDisplayHandleImp.LineDisplaySubmitV getLDSubmitV() throws HandleException {
      return this.ldSubmitV;
   }

   protected void submitSyncAndWaitDataStatus(byte[] data) throws HandleException {
      synchronized (this.waitDataLock) {
         this.submitSync(data);

         try {
            this.waitDataLock.wait(this.getCmdTimeout());
         } catch (InterruptedException var5) {
         }
      }
   }

   void initCmdTimeOut() {
      PosSystem.Properties prop = PosSystemManager.getInstance().getProperties();
      if (!prop.isLoaded()) {
         prop.loadProperties();
      }

      if (prop.isPropertyDefined("com.ibm.posj.bus.rs485.Rs485LineDisplayHandleImp.COMMAND_TIMEOUT")) {
         String value = prop.getPropertyString("com.ibm.posj.bus.rs485.Rs485LineDisplayHandleImp.COMMAND_TIMEOUT");

         try {
            this.setCmdTimeout(Long.decode(value));
         } catch (Exception var4) {
            this.traceNormal("Error reading : com.ibm.posj.bus.rs485.Rs485LineDisplayHandleImp.COMMAND_TIMEOUT");
            this.getTracer().print(var4);
            this.setCmdTimeout(100L);
         }
      } else {
         this.setCmdTimeout(100L);
      }

      if (this.isTracerOn()) {
         this.traceNormal("->initCmdTimeOut() Use value : " + this.getCmdTimeout() + "ms. <-");
      }
   }

   long getCmdTimeout() {
      return this.cmdTimeout;
   }

   void setCmdTimeout(long t) {
      this.cmdTimeout = t;
   }

   protected class HandleCmdFilterV extends DefaultHandleCmdV {
      private boolean isLineDisplayCmd = false;
      private boolean isSystemCmd = false;
      private boolean isInvalidCmd = false;

      public boolean isSystemCmd() {
         return this.isSystemCmd;
      }

      public boolean isLineDisplayCmd() {
         return this.isLineDisplayCmd;
      }

      public boolean isInvalidCmd() {
         return this.isInvalidCmd;
      }

      public void reset() {
         this.isInvalidCmd = false;
         this.isSystemCmd = false;
         this.isLineDisplayCmd = false;
      }

      protected void visitHandleCmd(HandleCmd cmd) {
         this.isInvalidCmd = true;
      }

      public void visitSystemCmd(SystemCmd cmd) {
         this.isSystemCmd = true;
      }

      public void visitLineDisplayCmd(LineDisplayCmd cmd) {
         this.isLineDisplayCmd = true;
      }
   }

   protected class LineDisplaySubmitV extends GenLineDisplayCmdV {
      protected void submit(byte[] data, String errorMsg) throws HandleException {
         if (Rs485LineDisplayHandleImp.this.deviceId == 3008) {
            Rs485LineDisplayHandleImp.this.submitSyncAndWaitDataStatus(data);
         } else {
            Rs485LineDisplayHandleImp.this.submitSync(data);
         }
      }

      protected void submitSystemCmd(byte[] data, String errorMsg) throws HandleException {
         this.submit(data, errorMsg);
      }

      public void visitClearDisplayCmd(LineDisplayCmd cmd) {
         try {
            byte cursorLoc = ((LineDisplayCmd.ClearDisplayCmd)cmd).getCursorLocation();
            Rs485LineDisplayHandleImp.CLEAR_TOP[1] = cursorLoc;
            this.submit(Rs485LineDisplayHandleImp.CLEAR_TOP, "Could not clear top line");
            Rs485LineDisplayHandleImp.CLEAR_BOTTOM[1] = cursorLoc;
            this.submit(Rs485LineDisplayHandleImp.CLEAR_BOTTOM, "Could not clear bottom line");
         } catch (HandleException var3) {
            this.handleException = new HandleException("could not write to Display", var3);
         }
      }
   }
}
