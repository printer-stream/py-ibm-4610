package com.ibm.posj.bus.poskbd;

import com.ibm.jutil.Util;
import com.ibm.jutil.tasks.Mapping;
import com.ibm.posj.AbstractHandleCmd;
import com.ibm.posj.Handle;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.ToneIndicatorCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.ToneIndicatorHandleImp;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.ToneIndicatorUtil;
import com.ibm.poskbd.Function;
import com.ibm.poskbd.StatusFunction;
import com.ibm.poskbd.ToneFunction;
import com.ibm.poskbd.event.StatusEvent;
import com.ibm.poskbd.event.StatusListener;
import com.ibm.poskbd.util.ToneCmdFactory;

class PosKbdToneIndicatorHandleImp extends AbstractPosKbdHandleImp implements ToneIndicatorHandleImp {
   private ToneFunction toneFunction = null;
   private ToneCmdFactory factory = new ToneCmdFactory();
   private StatusFunction statusFunction = null;
   private PosKbdToneIndicatorHandleImp.ToneIndicatorStatusListener toneStatusListener = new PosKbdToneIndicatorHandleImp.ToneIndicatorStatusListener();
   private byte lsbFromStatusByte = 0;
   private byte[] statusBytesArray = new byte[0];
   Object lockobj = new Object();
   private boolean waitExtra = true;
   private long extraWaitTime = 100L;
   private Mapping mapping = new Mapping();
   private int[][] ti_frequency_values = new int[][]{{875, 1300, 2000}, {3, 4, 5}};
   private int[][] ti_volume_values = new int[][]{{1, 100}, {1, 2}};
   private int[] ti_duration_values = new int[256];
   private int duration = 0;
   private boolean isToneActive = false;
   private int deviceId = -1;
   private int kbdType = 0;
   private static final byte STATUS_BYTE = 2;
   private static final byte BIT_MASKED = 1;
   private static final byte TONE_ON = 1;
   private static final byte TONE_OFF = 0;
   private static final int TI_VOLUME_LOW = 1;
   private static final int TI_VOLUME_HIGH = 100;
   private static final int TI_MIN_DURATION = 0;
   private static final int TI_MAX_DURATION = 256;
   private static final int TONEIND_SOUND_DURATION_UNITS = 100;

   public PosKbdToneIndicatorHandleImp(HandleKey key, Function tonefunction) {
      super(key);
      this.toneFunction = (ToneFunction)tonefunction;
      this.fillToneInd_duration();
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitToneIndicator(this);
   }

   public DevCat getDevCat() {
      return DevCats.TONEINDICATOR_DEVCAT;
   }

   public void setStatusFunction(StatusFunction status) {
      this.statusFunction = status;
   }

   public void init() throws HandleException {
      this.statusFunction.addStatusListener(this.toneStatusListener);
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd instanceof SystemCmd) {
         this.submitSystemCmd((SystemCmd)cmd);
      } else {
         if (!(cmd instanceof ToneIndicatorCmd)) {
            throw new HandleException("Invalid ToneIndicatorCmd object submitted!");
         }

         ToneIndicatorCmd toneIndicatorCmd = (ToneIndicatorCmd)cmd;
         this.submitSyncToneCmd(toneIndicatorCmd);
      }
   }

   public void handleStatusEvent(StatusEvent event) {
      if (this.isTracerOn()) {
         this.traceMaximum("handleStatusEvent() <- " + Util.toFormatedHexString(event.getStatus()));
      }

      this.statusBytesArray = event.getStatus();
      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      this.lsbFromStatusByte = (byte)(this.statusBytesArray[2] & 1);
      if (this.lsbFromStatusByte == 1) {
         this.isToneActive = true;
         eventHelper.fireStatusEvent(new com.ibm.posj.event.StatusEvent(this, 0));
      } else {
         this.isToneActive = false;
         eventHelper.fireStatusEvent(new com.ibm.posj.event.StatusEvent(this, 1));
         synchronized (this.lockobj) {
            this.lockobj.notifyAll();
         }
      }
   }

   protected void submitSyncToneCmd(ToneIndicatorCmd toneIndicatorCmd) throws HandleException {
      Handle.EventHelper eventHelper = this.getHandle().getEventHelper();
      this.submitToneCmd(toneIndicatorCmd);

      try {
         this.lockUntilSoundCompleted(toneIndicatorCmd);
      } catch (InterruptedException var4) {
      }

      if (this.isToneActive) {
         this.isToneActive = false;
         eventHelper.fireStatusEvent(new com.ibm.posj.event.StatusEvent(this, 1));
      }
   }

   protected void lockUntilSoundCompleted(ToneIndicatorCmd toneIndicatorCmd) throws InterruptedException {
      ToneIndicatorCmd.ToneCmd sound = (ToneIndicatorCmd.ToneCmd)toneIndicatorCmd;
      synchronized (this.lockobj) {
         try {
            this.lockobj.wait((long)(sound.getDuration() + 500));
            if (this.waitExtra) {
               this.sleep(this.extraWaitTime);
            }
         } catch (InterruptedException var6) {
         }
      }
   }

   protected void sleep(long timeout) throws InterruptedException {
      Thread.currentThread();
      Thread.sleep(timeout);
   }

   protected void submitToneCmd(ToneIndicatorCmd toneIndicatorCmd) throws HandleException {
      ToneIndicatorCmd.ToneCmd sound = (ToneIndicatorCmd.ToneCmd)toneIndicatorCmd;
      if (this.isTracerOn()) {
         this.traceMaximum(
            "submitToneCmd() -> sound.getOn -> "
               + sound.getOn()
               + "sound.getTimed -> "
               + sound.getTimed()
               + "sound.mappingDuration -> "
               + this.mapping.mapValue(sound.getDuration() / 100, this.ti_duration_values)
               + "sound.mappingFrequency -> "
               + this.mapping.mapValue(sound.getFrequency(), this.ti_frequency_values)
               + "sound.mappingVolume -> "
               + this.mapping.mapValue(sound.getVolume(), this.ti_volume_values)
         );
      }

      this.toneFunction
         .setTone(
            this.factory
               .createToneCmd(
                  sound.getOn(),
                  sound.getTimed(),
                  this.mapping.mapValue(sound.getDuration() / 100, this.ti_duration_values),
                  this.mapping.mapValue(sound.getFrequency(), this.ti_frequency_values),
                  this.mapping.mapValue(sound.getVolume(), this.ti_volume_values)
               )
         );
   }

   protected void submitSystemCmd(SystemCmd systemCmd) throws HandleException {
      try {
         if (systemCmd.getCode() != 101 && systemCmd.getCode() != 100) {
            if (systemCmd.getCode() == 103) {
               this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)systemCmd);
            } else if (systemCmd.getCode() != 102) {
               throw new HandleException("Invalid SystemCmd.getCode() value!");
            }
         }
      } catch (HandleException var3) {
         ((AbstractHandleCmd.DefaultResult)systemCmd.getResult()).setInError(true);
         throw new HandleException("Error while sending SystemCmd to PosKbd ToneIndicator", var3);
      }
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) throws HandleException {
      if (this.deviceId == -1) {
         this.deviceId = this.getDeviceId((short)this.getKbdType());
         if (this.isTracerOn()) {
            this.traceNormal("-->deviceId == " + this.deviceId + "<--");
         }
      }

      devInfoCmd.setDeviceId(this.deviceId);
   }

   protected int getDeviceId(short response) {
      return ToneIndicatorUtil.getPs2ToneIndicatorID(response);
   }

   public int getKbdType() {
      return this.kbdType;
   }

   public void setKbdType(int type) {
      if (this.isTracerOn()) {
         this.traceNormal("-->setKbdType() KeyboardType == " + type + "<--");
      }

      this.kbdType = type;
   }

   private void fillToneInd_duration() {
      this.duration = 0;

      while (this.duration < 256) {
         this.ti_duration_values[this.duration] = this.duration++;
      }
   }

   public class ToneIndicatorStatusListener implements StatusListener {
      public void statusReceived(StatusEvent event) {
         PosKbdToneIndicatorHandleImp.this.handleStatusEvent(event);
      }
   }
}
