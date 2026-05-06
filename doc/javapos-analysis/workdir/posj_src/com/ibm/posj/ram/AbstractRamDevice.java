package com.ibm.posj.ram;

import com.ibm.posj.HardTotalsHandle;
import com.ibm.posj.event.StatusEvent;
import com.ibm.posj.util.PosjUtil;

public abstract class AbstractRamDevice {
   private RamFileTable fileTable = null;
   private int userSize = 0;
   private int systemSize = 0;
   private int fileHandleConter = 0;
   private FileBlockTable fileBlockTable = null;
   private AbstractRamDevice.RamData ramData = new AbstractRamDevice.RamData();
   private boolean isRamDeviceClaimed = false;
   private String ramDeviceClaimOwner = "";
   private HardTotalsHandle posjHandle = null;
   private byte[] fileInfo = new byte[30];
   public static final int RAM_INITIAL_SIZE = 0;
   public static final int RAM_INITIAL_FILE_HANDLE_NUMBER = 0;
   public static final String RAM_ERROR_INVALID_SIZE = "Invalid file size value";
   public static final String RAM_ERROR_INVALID_NAME = "Invalid file name value";
   public static final String RAM_ERROR_INVALID_OFFSET = "Invalid file offset value";
   public static final String RAM_ERROR_INVALID_COUNT = "Invalid file count value";
   public static final String RAM_ERROR_INVALID_DATA = "Invalid file data value";
   public static final String RAM_ERROR_INSUFFICIENT_SPACE = "Insufficient room to create the file";
   private StatusEvent STATUS_DATA_CHANGED = new StatusEvent(this, 1);
   public static final int FILE_INFO_SIZE = 30;
   public static final int FILE_NAME_SIZE = 20;
   public static final int FILE_PRESENT_SIZE = 1;
   public static final int FILE_INT_SIZE = 4;
   public static final byte FILE_PRESENT_ON = 1;
   public static final byte FILE_PRESENT_OFF = 0;
   public static final int FILE_HANDLE_NOT_SET = -1;

   public void setHardTotalsHandle(HardTotalsHandle handle) {
      this.posjHandle = handle;
   }

   public int getUserSize() {
      return this.userSize;
   }

   public int getDataSize() {
      return this.getFileBlockTable().getAllocatedData();
   }

   public int getFreeDataSize() {
      return this.getUserSize() - this.getDataSize();
   }

   public int createFile(String name, int size, boolean errorDetection) throws IllegalArgumentException, IllegalAccessException, UnsupportedOperationException, IllegalStateException {
      return this.createFile(name, size, errorDetection, -1);
   }

   public int createFile(String name, int size, boolean errorDetection, int fileHandle) throws IllegalArgumentException, IllegalAccessException, UnsupportedOperationException, IllegalStateException {
      int position = 0;
      this.checkSizeArgument(size);
      this.checkFileSizeAllocation(size);
      this.checkNameArgument(name);
      this.checkUniqueNameArgument(name);
      if (size > 0) {
         position = this.getFileBlockTable().allocate(size);
      }

      new RamFile(name, size, position, errorDetection);
      int handle;
      if (fileHandle == -1) {
         handle = this.generateFileHandle();
      } else {
         handle = fileHandle++;
         this.setFileHandle(fileHandle);
      }

      this.getFileTable().add(handle, name, size, position, errorDetection);
      this.saveFileInfo(handle, name, size);
      if (this.posjHandle != null) {
         this.posjHandle.getEventHelper().fireStatusEvent(this.STATUS_DATA_CHANGED);
      }

      return handle;
   }

   public void deleteFile(int handle) throws IllegalArgumentException {
      RamFile ramFile = this.getFileTable().getFile(handle);
      int position = ramFile.getPosition();
      int size = ramFile.getSize();
      if (size > 0) {
         this.getFileBlockTable().free(position);
      }

      this.getFileTable().remove(handle);
      this.deleteFileInfo(handle);
      this.getFileBlockTable().defragment();
      if (this.posjHandle != null) {
         this.posjHandle.getEventHelper().fireStatusEvent(this.STATUS_DATA_CHANGED);
      }
   }

   public int getNumberOfFiles() {
      return this.getFileTable().getNumberOfFiles();
   }

   public int getNumberOfClaimedFiles() {
      return this.getFileTable().getClaimedFiles();
   }

   public int getHandleByName(String name) {
      return this.getFileTable().getHandleByName(name);
   }

   public int getHandleByIndex(int index) {
      return this.getFileTable().getHandleByIndex(index);
   }

   public RamFile getFile(int handle) throws IllegalArgumentException {
      return this.getFileTable().getFile(handle);
   }

   public void claimRamDevice(String owner) {
      this.isRamDeviceClaimed = true;
      this.ramDeviceClaimOwner = owner;
   }

   public String getClaimOwner() {
      return this.ramDeviceClaimOwner;
   }

   public boolean isClaimedRamDevice() {
      return this.isRamDeviceClaimed;
   }

   public void releaseRamDevice() {
      this.isRamDeviceClaimed = false;
   }

   public boolean claimFile(int fileHandle, long timeOut) throws IllegalArgumentException {
      return this.getFileTable().claim(fileHandle, timeOut);
   }

   public boolean isClaimed(int fileHandle) throws IllegalArgumentException {
      return this.getFile(fileHandle).isClaimed();
   }

   public void release(int fileHandle) throws IllegalArgumentException {
      if (!this.isClaimed(fileHandle)) {
         throw new IllegalArgumentException();
      } else {
         this.getFileTable().release(fileHandle);
      }
   }

   public void write(int handle, byte[] data, int offset, int count) throws IllegalArgumentException {
      this.ramData.set(data, offset, count);
      this.transfer(handle, this.ramData, true);
   }

   public void setAll(int handle, byte value) throws IllegalArgumentException {
      int size = this.getFile(handle).getSize();
      byte[] buffer = new byte[size];
      int i = 0;

      while (i < size) {
         buffer[i++] = value;
      }

      this.write(handle, buffer, 0, size);
   }

   public void read(int handle, byte[] data, int offset, int count) throws IllegalArgumentException {
      this.ramData.set(data, offset, count);
      this.transfer(handle, this.ramData, false);
   }

   public void rename(int handle, String name) throws IllegalArgumentException, UnsupportedOperationException {
      this.checkNameArgument(name);
      this.checkUniqueNameArgument(name);
      RamFile ramFile = this.getFileTable().getFile(handle);
      ramFile.setName(name);
   }

   public boolean restoreFileStructure() throws IllegalArgumentException, IllegalAccessException, UnsupportedOperationException, IllegalStateException {
      int fileInfoBytePos = this.getUserSize();
      int presentBytePos = 0;
      int nameBytePos = 1;
      int sizeBytePos = nameBytePos + 20;
      int handleBytePos = sizeBytePos + 4;
      boolean restored = false;
      this.ramData.set(this.fileInfo, 0, 30);
      this.readRamData(fileInfoBytePos, this.ramData);
      if (this.fileInfo[presentBytePos] == 1) {
         int i = 0;
         byte[] name = null;
         int realNameSize = 0;

         while (this.fileInfo[realNameSize + nameBytePos] != 0 && realNameSize < 20) {
            realNameSize++;
         }

         name = new byte[realNameSize];
         int fileSize = 0;

         for (int var12 = 0; var12 < 4; var12++) {
            fileSize = PosjUtil.setByte(fileSize, this.fileInfo[sizeBytePos + var12], var12);
         }

         int fileHandle = 0;

         for (int var13 = 0; var13 < 4; var13++) {
            fileHandle = PosjUtil.setByte(fileHandle, this.fileInfo[handleBytePos + var13], var13);
         }

         this.createFile(new String(name), fileSize, false, fileHandle);
         restored = true;
      }

      return restored;
   }

   protected FileBlockTable getFileBlockTable() {
      if (this.fileBlockTable == null) {
         this.fileBlockTable = new FileBlockTable();
         this.fileBlockTable.clear();
         this.fileBlockTable.add(0, this.getUserSize(), true, -1, true);
      }

      return this.fileBlockTable;
   }

   protected RamFileTable getFileTable() {
      if (this.fileTable == null) {
         this.fileTable = new RamFileTable();
      }

      return this.fileTable;
   }

   public int getSystemSize() {
      return this.systemSize;
   }

   protected void setUserSize(int size) {
      this.checkSizeArgument(size);
      this.userSize = size;
   }

   protected void setSystemSize(int size) {
      this.checkSizeArgument(size);
      this.systemSize = size;
   }

   protected abstract void writeRamData(int var1, AbstractRamDevice.RamData var2);

   protected abstract void readRamData(int var1, AbstractRamDevice.RamData var2);

   protected abstract void initRamSize();

   public abstract void checkStatus() throws UnsupportedOperationException;

   private void saveFileInfo(int fileHandle, String name, int fileSize) {
      int i = 0;
      byte[] fileName = name.getBytes();
      int fileInfoBytePos = this.getUserSize();
      int presentBytePos = 0;
      int nameBytePos = 1;
      int sizeBytePos = nameBytePos + 20;
      int handleBytePos = sizeBytePos + 4;
      this.fileInfo[presentBytePos] = 1;
      System.arraycopy(fileName, 0, this.fileInfo, nameBytePos, fileName.length);
      if (fileName.length < 20) {
         for (int var11 = fileName.length; var11 < 20; var11++) {
            this.fileInfo[nameBytePos + var11] = 0;
         }
      }

      for (int var12 = 0; var12 < 4; var12++) {
         this.fileInfo[sizeBytePos + var12] = (byte)(fileSize >> 8 * (3 - var12));
      }

      for (int var13 = 0; var13 < 4; var13++) {
         this.fileInfo[handleBytePos + var13] = (byte)(fileHandle >> 8 * (3 - var13));
      }

      this.ramData.set(this.fileInfo, 0, 30);
      this.writeRamData(fileInfoBytePos, this.ramData);
   }

   private void deleteFileInfo(int handle) {
      int fileInfoBytePos = this.getUserSize();
      this.fileInfo[0] = 0;
      this.ramData.set(this.fileInfo, 0, 1);
      this.writeRamData(fileInfoBytePos, this.ramData);
   }

   private void transfer(int handle, AbstractRamDevice.RamData ramData, boolean write) throws IllegalArgumentException {
      byte[] data = ramData.getData();
      int count = ramData.getCount();
      int offset = ramData.getOffset();
      RamFile file = this.getFile(handle);
      this.checkOffsetArgument(offset, file.getSize());
      this.checkCountArgument(count, offset, file.getSize());
      this.checkDataArgument(data);
      int blockPosition = file.getPosition();
      int iniBlockOffset = 0;
      int endBlockOffset = 0;
      int offsetOnThisBlock = 0;
      int bytesToTransferOnThisBlock = 0;
      int absoluteMemoryPosition = 0;
      FileBlock block = null;
      int bytesToTransfer = count;
      int transferedBytes = 0;

      while (bytesToTransfer > 0) {
         block = this.getFileBlockTable().get(blockPosition);
         endBlockOffset = iniBlockOffset + block.getSize() - 1;
         if (iniBlockOffset <= offset && offset <= endBlockOffset) {
            offsetOnThisBlock = offset - iniBlockOffset;
            bytesToTransferOnThisBlock = endBlockOffset - offset + 1;
            if (bytesToTransferOnThisBlock > bytesToTransfer) {
               bytesToTransferOnThisBlock = bytesToTransfer;
            }

            absoluteMemoryPosition = iniBlockOffset + offsetOnThisBlock;
            if (write) {
               this.writeRamData(absoluteMemoryPosition, ramData);
            } else {
               this.readRamData(absoluteMemoryPosition, ramData);
            }

            transferedBytes += bytesToTransferOnThisBlock;
            bytesToTransfer = count - transferedBytes;
            offset += transferedBytes;
         }

         iniBlockOffset = endBlockOffset + 1;
         blockPosition = block.getNextBlockPosition();
      }

      ramData.setTransferedBytes(transferedBytes);
   }

   private void checkFileSizeAllocation(int size) {
      if (size > this.getFreeDataSize()) {
         throw new IllegalStateException("Insufficient room to create the file");
      }
   }

   private void checkNameArgument(String name) throws UnsupportedOperationException {
      if (name == null) {
         throw new IllegalArgumentException("Invalid file name value");
      }
   }

   protected void checkUniqueNameArgument(String name) throws UnsupportedOperationException {
      this.getFileTable().checkUniqueNameArgument(name);
   }

   private void checkSizeArgument(int size) throws IllegalArgumentException {
      if (size < 0) {
         throw new IllegalArgumentException("Invalid file size value");
      }
   }

   private void checkOffsetArgument(int offset, int maxFileSize) throws IllegalArgumentException {
      if (offset < 0 || offset >= maxFileSize) {
         throw new IllegalArgumentException("Invalid file offset value");
      }
   }

   private void checkCountArgument(int count, int offset, int maxFileSize) throws IllegalArgumentException {
      if (count < 0 || offset + count > maxFileSize) {
         throw new IllegalArgumentException("Invalid file count value");
      }
   }

   private void checkDataArgument(byte[] data) {
      if (data == null) {
         throw new IllegalArgumentException("Invalid file data value");
      }
   }

   private synchronized int generateFileHandle() {
      return this.fileHandleConter++;
   }

   private synchronized void setFileHandle(int handle) {
      this.fileHandleConter = handle;
   }

   class RamData {
      private int offset = 0;
      private int count = 0;
      private int transferedBytes = 0;
      private byte[] data = null;

      RamData() {
      }

      RamData(byte[] data, int offset, int count) {
         this.setData(data);
         this.setOffset(offset);
         this.setCount(count);
      }

      protected void set(byte[] data, int offset, int count) {
         this.setData(data);
         this.setOffset(offset);
         this.setCount(count);
         this.setTransferedBytes(0);
      }

      protected int getCount() {
         return this.count;
      }

      protected int getOffset() {
         return this.offset;
      }

      protected int getTransferedBytes() {
         return this.transferedBytes;
      }

      protected byte[] getData() {
         return this.data;
      }

      protected void setCount(int count) {
         this.count = count;
      }

      protected void setOffset(int offset) {
         this.offset = offset;
      }

      protected void setTransferedBytes(int bytes) {
         this.transferedBytes = bytes;
      }

      protected void setData(byte[] data) {
         this.data = data;
      }
   }
}
