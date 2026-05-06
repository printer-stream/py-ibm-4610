package com.ibm.posj.bus.embedded;

import com.ibm.embedded.EmbeddedException;
import com.ibm.embedded.GetNvramInfo;
import com.ibm.embedded.HardTotalsEmbeddedDriver;
import com.ibm.embedded.NvramInfo;
import com.ibm.jutil.Util;
import com.ibm.posj.DefaultHardTotalsCmd;
import com.ibm.posj.HandleCmd;
import com.ibm.posj.HandleException;
import com.ibm.posj.HandleKey;
import com.ibm.posj.HardTotalsCmd;
import com.ibm.posj.HardTotalsHandle;
import com.ibm.posj.SystemCmd;
import com.ibm.posj.bus.HandleImpVisitor;
import com.ibm.posj.bus.HardTotalsCmdSubmitter;
import com.ibm.posj.bus.HardTotalsHandleImp;
import com.ibm.posj.ram.NvRamDevice;
import com.ibm.posj.ram.RamDevice;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;

public class EmbeddedHardTotalsHandleImp extends AbstractEmbeddedHandleImp implements HardTotalsHandleImp {
   private HardTotalsEmbeddedDriver embeddedDriver = null;
   private static HardTotalsCmd.Factory cmdFactory = null;
   private static NvRamDevice nvRamDevice = null;

   public EmbeddedHardTotalsHandleImp(HandleKey key, HardTotalsEmbeddedDriver driver) {
      super(key);
      this.embeddedDriver = driver;
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitHardTotals(this);
   }

   public DevCat getDevCat() {
      return DevCats.HARDTOTALS_DEVCAT;
   }

   public void init() throws HandleException {
   }

   public short getECLevel() {
      return 0;
   }

   public boolean isFlashable() {
      return false;
   }

   public HardTotalsEmbeddedDriver getEmbeddedDriver() {
      return this.embeddedDriver;
   }

   public RamDevice getRamDevice() {
      if (nvRamDevice == null) {
         nvRamDevice = new NvRamDevice(this.getCmdFactory());
         nvRamDevice.setHardTotalsHandle((HardTotalsHandle)this.getHandle());
      }

      return nvRamDevice;
   }

   public HardTotalsCmd.Factory getCmdFactory() {
      if (cmdFactory == null) {
         cmdFactory = new DefaultHardTotalsCmd.Factory();
         cmdFactory.setHardTotalsCmdSubmitter(new EmbeddedHardTotalsHandleImp.NVRamCmdSubmitter());
      }

      return cmdFactory;
   }

   public void submit(HandleCmd cmd) throws HandleException {
      if (cmd == null) {
         throw new HandleException("Attempted to submit a null command to handle");
      } else {
         try {
            if (cmd.getCode() != 103) {
               throw new HandleException("Invalid Hard Totals object submitted!");
            }

            this.submitDevInfoCmd((SystemCmd.DeviceInfoRequestCmd)cmd);
         } catch (HandleException var6) {
            this.setHandleCmdResultInError(cmd, true);
            throw var6;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   protected void submitDevInfoCmd(SystemCmd.DeviceInfoRequestCmd devInfoCmd) {
      if (this.embeddedDriver.getAdapterID() == 661L) {
         devInfoCmd.setDeviceId(2801);
      } else if (this.embeddedDriver.getAdapterID() == 6L) {
         devInfoCmd.setDeviceId(2802);
      } else if (this.embeddedDriver.getAdapterID() == 5L) {
         devInfoCmd.setDeviceId(2803);
      } else {
         devInfoCmd.setDeviceId(2800);
      }

      if (this.isTracerOn()) {
         this.traceNormal("-->submitDevInfoCmd() : devInfoCmd.getDeviceId() = " + devInfoCmd.getDeviceId() + "<--");
      }
   }

   public class NVRamCmdSubmitter implements HardTotalsCmdSubmitter {
      public static final String HT_ERROR_GETNFO = "Command GetHardTotalsInfCmd could not be submitted";
      public static final String HT_ERROR_READDATA = "Command ReadHardTotalsDataCmd could not be submitted";
      public static final String HT_ERROR_WRITEDATA = "Command WriteHardTotalsDataCmd could not be submitted";

      public void submitGetHardTotalsInfCmd(HardTotalsCmd.GetHardTotalsInfCmd cmd) throws HandleException {
         try {
            GetNvramInfo nvRamInfo = EmbeddedHardTotalsHandleImp.this.getEmbeddedDriver().getNVRAMInfo();
            if (EmbeddedHardTotalsHandleImp.this.isTracerOn()) {
               EmbeddedHardTotalsHandleImp.this.traceNormal(
                  "-->submitGetHardTotalsInfCmd() ->nvRamInfo.CompletionCode-> "
                     + nvRamInfo.getCompletionCode()
                     + "nvRamInfo.totalSize-> "
                     + nvRamInfo.getTotalSize()
                     + "nvRamInfo.getSize-> "
                     + nvRamInfo.getSize()[1]
                     + "ram system size-> "
                     + (cmd.getTotalSize() - cmd.getUserSize())
               );
            }

            cmd.setCompletionCode(nvRamInfo.getCompletionCode());
            cmd.setTotalSize(nvRamInfo.getTotalSize());
            cmd.setUserSize(nvRamInfo.getSize()[1]);
            cmd.setSystemSize(cmd.getTotalSize() - cmd.getUserSize());
         } catch (EmbeddedException var3) {
            throw new HandleException("Command GetHardTotalsInfCmd could not be submitted");
         }
      }

      public void submitReadHardTotalsDataCmd(HardTotalsCmd.ReadHardTotalsDataCmd cmd) throws HandleException {
         int offset = cmd.getOffset();
         int length = cmd.getLength();

         try {
            NvramInfo nvRamData = EmbeddedHardTotalsHandleImp.this.getEmbeddedDriver().readNVRAMData(offset, length);
            if (EmbeddedHardTotalsHandleImp.this.isTracerOn()) {
               EmbeddedHardTotalsHandleImp.this.traceNormal(
                  "-->submitReadHardTotalsDataCmd() ->nvRamData.CompletionCode-> "
                     + nvRamData.getCompletionCode()
                     + "offset-> "
                     + offset
                     + "length-> "
                     + length
               );
            }

            cmd.setCompletionCode(nvRamData.getCompletionCode());
            System.arraycopy(nvRamData.getBuffer(), 0, cmd.getBuffer(), 0, length);
         } catch (EmbeddedException var5) {
            throw new HandleException("Command ReadHardTotalsDataCmd could not be submitted");
         }
      }

      public void submitWriteHardTotalsDataCmd(HardTotalsCmd.WriteHardTotalsDataCmd cmd) throws HandleException {
         int offset = cmd.getOffset();
         int length = cmd.getLength();
         byte[] buffer = cmd.getBuffer();
         int bufferOffset = cmd.getBufferOffset();

         try {
            NvramInfo nvRamData = EmbeddedHardTotalsHandleImp.this.getEmbeddedDriver().writeNVRAMData(offset, length, buffer, bufferOffset);
            cmd.setCompletionCode(nvRamData.getCompletionCode());
            if (EmbeddedHardTotalsHandleImp.this.isTracerOn()) {
               EmbeddedHardTotalsHandleImp.this.traceNormal(
                  "-->submitWriteHardTotalsDataCmd() ->offset-> "
                     + offset
                     + "length-> "
                     + length
                     + "buffer-> "
                     + Util.toFormatedHexString(buffer)
                     + "bufferOffset-> "
                     + bufferOffset
                     + "nvRamData.CompletionCode-> "
                     + nvRamData.getCompletionCode()
               );
            }
         } catch (EmbeddedException var7) {
            throw new HandleException("Command WriteHardTotalsDataCmd could not be submitted");
         }
      }
   }
}
