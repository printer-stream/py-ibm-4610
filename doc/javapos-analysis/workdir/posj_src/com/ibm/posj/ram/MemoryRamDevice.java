package com.ibm.posj.ram;

public class MemoryRamDevice extends AbstractRamDevice implements RamDevice {
   public MemoryRamDevice(int size) {
      this.setUserSize(size);
   }

   protected void initRamSize() {
   }

   protected void writeRamData(int memoryPosition, AbstractRamDevice.RamData ramData) {
   }

   protected void readRamData(int memoryPosition, AbstractRamDevice.RamData ramData) {
   }

   public void checkStatus() throws UnsupportedOperationException {
   }
}
