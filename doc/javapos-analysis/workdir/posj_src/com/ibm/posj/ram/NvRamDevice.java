package com.ibm.posj.ram;

import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.HandleException;
import com.ibm.posj.HardTotalsCmd;
import com.ibm.posj.util.DevCats;

public class NvRamDevice extends AbstractRamDevice implements RamDevice {
   HardTotalsCmd.Factory cmdFactory = null;
   private Tracer tracer = TracerFactory.getInstance().createTracer(DevCats.HARDTOTALS_DEVCAT.toString(), "NvRamDevice");
   public static final int NVRAM_SYSTEM_SIZE = 1024;

   public NvRamDevice(HardTotalsCmd.Factory cmdFactory) {
      try {
         this.setCmdFactory(cmdFactory);
         this.initRamSize();
         this.restoreFileStructure();
      } catch (IllegalArgumentException var3) {
      } catch (IllegalAccessException var4) {
      } catch (UnsupportedOperationException var5) {
      } catch (IllegalStateException var6) {
      }
   }

   protected void initRamSize() {
      try {
         HardTotalsCmd.GetHardTotalsInfCmd cmd = this.getCmdFactory().createGetHardTotalsInfCmd();
         cmd.submit();
         this.setUserSize(cmd.getTotalSize() - 1024);
         this.setSystemSize(1024);
         cmd.recycle();
      } catch (HandleException var2) {
      }
   }

   protected void writeRamData(int memoryPosition, AbstractRamDevice.RamData ramData) {
      int offset = ramData.getOffset();
      int count = ramData.getCount();
      byte[] buffer = ramData.getData();

      try {
         HardTotalsCmd.WriteHardTotalsDataCmd cmd = this.getCmdFactory().createWriteHardTotalsDataCmd(memoryPosition, count, buffer, 0);
         if (this.tracer.isOn()) {
            this.tracer.println("write memoryPosition-> " + memoryPosition + " count-> " + count + " buffer.size-> " + buffer.length);
            int temp = count;
            if (count > 300) {
               temp = 300;
            }

            this.tracer.println("<Contents:" + Util.toFormatedHexString(buffer, 0, temp) + "\\Contents>");
         }

         cmd.submit();
         cmd.recycle();
      } catch (HandleException var8) {
      }
   }

   protected void readRamData(int memoryPosition, AbstractRamDevice.RamData ramData) {
      int offset = ramData.getOffset();
      int count = ramData.getCount();
      byte[] buffer = ramData.getData();

      try {
         HardTotalsCmd.ReadHardTotalsDataCmd cmd = this.getCmdFactory().createReadHardTotalsDataCmd(memoryPosition, count, buffer);
         cmd.submit();
         if (this.tracer.isOn()) {
            int tcnt = count > 250 ? 250 : count;
            this.tracer.println("read memoryPosition-> " + memoryPosition + " count-> " + count + " buffer.size-> " + buffer.length);
            this.tracer.println("<Contents:" + Util.toFormatedHexString(buffer, 0, tcnt) + "\\Contents>");
         }

         cmd.recycle();
      } catch (HandleException var8) {
      }
   }

   public void checkStatus() throws UnsupportedOperationException {
      try {
         HardTotalsCmd.GetHardTotalsInfCmd cmd = this.getCmdFactory().createGetHardTotalsInfCmd();
         cmd.submit();
         cmd.recycle();
      } catch (HandleException var2) {
         throw new UnsupportedOperationException();
      }
   }

   private HardTotalsCmd.Factory getCmdFactory() {
      return this.cmdFactory;
   }

   private void setCmdFactory(HardTotalsCmd.Factory cmdFactory) {
      this.cmdFactory = cmdFactory;
   }
}
