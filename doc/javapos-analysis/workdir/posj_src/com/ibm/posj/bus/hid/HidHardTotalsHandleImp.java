package com.ibm.posj.bus.hid;

import com.ibm.hid.HidDevice;
import com.ibm.hid.HidException;
import com.ibm.hid.ReportEvent;
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

public class HidHardTotalsHandleImp extends AbstractHidHandleImp implements HidHandleImp, HardTotalsHandleImp {
   private static HardTotalsCmd.Factory cmdFactory = null;
   private static NvRamDevice nvRamDevice = null;
   private HandleException handleException = null;
   public static final byte[] HT_READ_WRITE_UNIVERSAL_CMD = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};

   public HidHardTotalsHandleImp(HandleKey key, HidDevice hidDevice) {
      super(key, hidDevice);
   }

   public void accept(HidHandleImpVisitor visitor) {
      visitor.visitHidHardTotalsHandleImp(this);
   }

   public void accept(HandleImpVisitor visitor) {
      visitor.visitHardTotals(this);
   }

   public DevCat getDevCat() {
      return DevCats.HARDTOTALS_DEVCAT;
   }

   public boolean isFlashable() {
      return false;
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
         cmdFactory.setHardTotalsCmdSubmitter(new HidHardTotalsHandleImp.NVRamCmdSubmitter());
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

            ((SystemCmd.DeviceInfoRequestCmd)cmd).setDeviceId(2804);
            if (this.isTracerOn()) {
               this.traceNormal("-->submit() : devInfoCmd.getDeviceId() = " + ((SystemCmd.DeviceInfoRequestCmd)cmd).getDeviceId() + "<--");
            }

            ((SystemCmd.DeviceInfoRequestCmd)cmd).setFirmwareLevel(this.getBCDLevel());
            ((SystemCmd.DeviceInfoRequestCmd)cmd).setSerialNumber(this.getSerialNumber());
         } catch (HandleException var6) {
            this.setHandleCmdResultInError(cmd, true);
            throw var6;
         } finally {
            cmd.setCompleted(true);
         }
      }
   }

   protected void reportEventOccurred(ReportEvent rE) {
   }

   private void submitSetReportRequestCmd(byte[] cmd, String errorMsg) throws HandleException {
      try {
         if (this.isTracerOn()) {
            this.traceNormal("-->submitSetReportRequestCmd() : " + Util.toFormatedHexString(cmd));
         }

         this.getHidDevice().setReport((byte)2, (byte)0, (short)cmd.length, cmd);
      } catch (HidException var4) {
         throw new HandleException(errorMsg, var4);
      }
   }

   protected byte[] submitGetReportRequestCmd(String errorMsg) throws HandleException {
      byte[] report = null;

      try {
         report = this.getHidDevice().getReport((byte)3, (byte)0);
         if (this.isTracerOn()) {
            this.traceNormal("-->submitGetReportRequestCmd() <- " + Util.toFormatedHexString(report));
         }
      } catch (HidException var4) {
         throw new HandleException(errorMsg, var4);
      }

      if (report != null) {
         return report;
      } else {
         throw new HandleException("Error while receiving data from HidDevice");
      }
   }

   private void setDataAddress_DataLength(int offset, int dataLength, byte command) {
      int setAddress = 0;
      if (command == 1) {
         HT_READ_WRITE_UNIVERSAL_CMD[0] = 3;
      } else if (command == 2) {
         HT_READ_WRITE_UNIVERSAL_CMD[0] = 2;
      }

      setAddress = offset & 0xFF;
      HT_READ_WRITE_UNIVERSAL_CMD[1] = (byte)setAddress;
      setAddress = offset >> 8;
      setAddress &= 255;
      HT_READ_WRITE_UNIVERSAL_CMD[2] = (byte)setAddress;
      setAddress = offset >> 16;
      setAddress &= 255;
      HT_READ_WRITE_UNIVERSAL_CMD[3] = (byte)setAddress;
      HT_READ_WRITE_UNIVERSAL_CMD[4] = 0;
      setAddress = dataLength & 0xFF;
      HT_READ_WRITE_UNIVERSAL_CMD[5] = (byte)setAddress;
      setAddress = dataLength >> 8;
      setAddress &= 255;
      HT_READ_WRITE_UNIVERSAL_CMD[6] = (byte)setAddress;
   }

   public class NVRamCmdSubmitter implements HardTotalsCmdSubmitter {
      public static final String HT_ERROR_GETNFO = "Command GetHardTotalsInfCmd could not be submitted";
      public static final String HT_ERROR_READDATA = "Command ReadHardTotalsDataCmd could not be submitted";
      public static final String HT_ERROR_READCMD_RETRIEVE_DATA = "could not retrieve data for read command";
      public static final String HT_ERROR_WRITEDATA = "Command WriteHardTotalsDataCmd could not be submitted";
      public static final int HT_OFFSET = 19800;
      public static final int HT_USER_TOTAL_SIZE = 111271;
      public static final int HT_RAM_SYSTEM_SIZE = 1024;
      public static final int HT_MAX_WRITE_LONG = 500;

      public void submitGetHardTotalsInfCmd(HardTotalsCmd.GetHardTotalsInfCmd cmd) throws HandleException {
         cmd.setCompletionCode(0);
         cmd.setTotalSize(111271);
         cmd.setUserSize(110247);
         cmd.setSystemSize(1024);
      }

      public void submitReadHardTotalsDataCmd(HardTotalsCmd.ReadHardTotalsDataCmd cmd) throws HandleException {
         int offset = cmd.getOffset() + 19800;
         int outArrayLength = cmd.getLength();
         int initialAddress = offset;
         int arrayDataPointer = 0;
         int dataLeft = 0;

         while (arrayDataPointer < outArrayLength) {
            if (arrayDataPointer + 500 < outArrayLength) {
               HidHardTotalsHandleImp.this.setDataAddress_DataLength(initialAddress, 500, (byte)2);
               HidHardTotalsHandleImp.this.submitSetReportRequestCmd(
                  HidHardTotalsHandleImp.HT_READ_WRITE_UNIVERSAL_CMD, "Command ReadHardTotalsDataCmd could not be submitted"
               );
               byte[] tempReadArray = HidHardTotalsHandleImp.this.submitGetReportRequestCmd("could not retrieve data for read command");
               System.arraycopy(tempReadArray, 8, cmd.getBuffer(), arrayDataPointer, tempReadArray.length - 8);
               if (initialAddress == offset) {
                  initialAddress = initialAddress + 500 + 1;
               } else {
                  initialAddress += 500;
               }

               arrayDataPointer += 500;
            } else {
               dataLeft = outArrayLength - arrayDataPointer;
               HidHardTotalsHandleImp.this.setDataAddress_DataLength(initialAddress, dataLeft, (byte)2);
               HidHardTotalsHandleImp.this.submitSetReportRequestCmd(
                  HidHardTotalsHandleImp.HT_READ_WRITE_UNIVERSAL_CMD, "Command ReadHardTotalsDataCmd could not be submitted"
               );
               byte[] tempReadArray = HidHardTotalsHandleImp.this.submitGetReportRequestCmd("could not retrieve data for read command");
               System.arraycopy(tempReadArray, 8, cmd.getBuffer(), arrayDataPointer, tempReadArray.length - 8);
               initialAddress = initialAddress + 500 + 1;
               arrayDataPointer = arrayDataPointer + 500 + 1;
            }
         }
      }

      public void submitWriteHardTotalsDataCmd(HardTotalsCmd.WriteHardTotalsDataCmd cmd) throws HandleException {
         int offset = cmd.getOffset() + 19800;
         int length = cmd.getLength();
         byte[] dataArray = cmd.getBuffer();
         int initialAddress = offset;
         int arrayDataPointer = 0;
         int dataLeft = 0;

         while (arrayDataPointer < length) {
            if (arrayDataPointer + 500 < length) {
               HidHardTotalsHandleImp.this.setDataAddress_DataLength(initialAddress, 500, (byte)1);
               byte[] finalWriteCmd = new byte[HidHardTotalsHandleImp.HT_READ_WRITE_UNIVERSAL_CMD.length - 1 + 500];
               System.arraycopy(
                  HidHardTotalsHandleImp.HT_READ_WRITE_UNIVERSAL_CMD, 0, finalWriteCmd, 0, HidHardTotalsHandleImp.HT_READ_WRITE_UNIVERSAL_CMD.length - 1
               );
               System.arraycopy(dataArray, arrayDataPointer, finalWriteCmd, HidHardTotalsHandleImp.HT_READ_WRITE_UNIVERSAL_CMD.length - 1, 500);
               HidHardTotalsHandleImp.this.submitSetReportRequestCmd(finalWriteCmd, "Command WriteHardTotalsDataCmd could not be submitted");
               if (initialAddress == offset) {
                  initialAddress = initialAddress + 500 + 1;
               } else {
                  initialAddress += 500;
               }

               arrayDataPointer += 500;
            } else {
               dataLeft = length - arrayDataPointer;
               HidHardTotalsHandleImp.this.setDataAddress_DataLength(initialAddress, dataLeft, (byte)1);
               byte[] finalWriteCmd = new byte[HidHardTotalsHandleImp.HT_READ_WRITE_UNIVERSAL_CMD.length - 1 + dataLeft];
               System.arraycopy(
                  HidHardTotalsHandleImp.HT_READ_WRITE_UNIVERSAL_CMD, 0, finalWriteCmd, 0, HidHardTotalsHandleImp.HT_READ_WRITE_UNIVERSAL_CMD.length - 1
               );
               System.arraycopy(dataArray, arrayDataPointer, finalWriteCmd, HidHardTotalsHandleImp.HT_READ_WRITE_UNIVERSAL_CMD.length - 1, dataLeft);
               HidHardTotalsHandleImp.this.submitSetReportRequestCmd(finalWriteCmd, "Command WriteHardTotalsDataCmd could not be submitted");
               initialAddress = initialAddress + 500 + 1;
               arrayDataPointer = arrayDataPointer + 500 + 1;
            }
         }
      }
   }
}
