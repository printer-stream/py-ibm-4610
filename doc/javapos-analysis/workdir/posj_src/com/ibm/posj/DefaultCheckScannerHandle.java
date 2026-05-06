package com.ibm.posj;

import com.ibm.jutil.Util;
import com.ibm.posj.util.DevCat;
import com.ibm.posj.util.DevCats;
import com.ibm.posj.util.PosjUtil;
import java.util.Hashtable;
import java.util.Iterator;

public class DefaultCheckScannerHandle extends AbstractHandle implements CheckScannerHandle {
   private static int count = 0;
   private static CheckScannerCmd.Factory factory = new DefaultCheckScannerCmd.Factory();
   private CheckScannerHandle.State csState = null;
   private CheckScannerHandle.ImageMemory csImageMemory = null;
   private static final byte STATUS_BYTE = 0;
   static final byte QUALITY_ERROR_BIT = 6;

   DefaultCheckScannerHandle() {
      this.handleCount = count++;
   }

   public void submit(DefaultCheckScannerCmd cmd) throws HandleException {
      this.getHandleImp().submit(cmd);
   }

   public boolean isInit() throws HandleException {
      return this.isInitialized();
   }

   public HandleCmd.Factory getHandleCmdFactory() {
      return this.getCheckScannerCmdFactory();
   }

   public SystemCmd.Factory getSystemCmdFactory() {
      return this.getCheckScannerCmdFactory();
   }

   public CheckScannerCmd.Factory getCheckScannerCmdFactory() {
      return factory;
   }

   public DevCat getDevCat() {
      return DevCats.CHECKSCANNER_DEVCAT;
   }

   public void accept(HandleVisitor visitor) {
      visitor.visitCheckScanner(this);
   }

   public Handle.State getState() {
      return this.getCheckScannerState();
   }

   public CheckScannerHandle.State getCheckScannerState() {
      if (this.csState == null) {
         this.csState = new DefaultCheckScannerHandle.ChkState();
      }

      return this.csState;
   }

   public CheckScannerHandle.ImageMemory getImageMemory() {
      if (this.csImageMemory == null) {
         this.csImageMemory = new DefaultCheckScannerHandle.ChkImageMemory();
      }

      return this.csImageMemory;
   }

   public CheckScannerHandle.ImageHeader createImageHeader(short il, byte[] d) {
      return new DefaultCheckScannerHandle.ChkImageHeader(il, d);
   }

   protected class ChkImageHeader implements CheckScannerHandle.ImageHeader {
      private String tagData = null;
      private short imgLocation = 0;
      private byte[] header = null;

      public ChkImageHeader(short il, byte[] h) {
         this.header = h;
         this.imgLocation = il;
      }

      public byte getCompression() {
         return this.header[1];
      }

      public short getImageHeight() {
         return Util.toShort(this.header[8], this.header[9]);
      }

      public short getImageLocation() {
         return this.imgLocation;
      }

      public int getUserImageLocation() {
         if (this.header.length >= 20) {
            try {
               return Integer.decode("0x" + new String(this.header, 12, 8));
            } catch (NumberFormatException var2) {
               return -2;
            }
         } else {
            return -1;
         }
      }

      public short getImageWidth() {
         return Util.toShort(this.header[6], this.header[7]);
      }

      public boolean getQualityDetectionBit() {
         return PosjUtil.isBitSelected(this.header[0], 6);
      }

      public int getSize() {
         return Util.toInt(this.header[2], this.header[3], this.header[4], this.header[5]);
      }

      public String getTagData() {
         if (this.tagData == null) {
            if (this.header.length >= 21) {
               this.tagData = new String(this.header, 20, this.header.length - 21);
            } else {
               this.tagData = "";
            }
         }

         return this.tagData;
      }

      public int getImageHeaderSize() {
         return this.header.length;
      }

      public void update(short il, byte[] h) {
         this.imgLocation = il;
         this.header = h;
         this.tagData = null;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("<ImageHeader:");
         sb.append("\n\t getImageLocation() = " + this.getImageLocation());
         sb.append("\t\t getCompression() = " + this.getCompression());
         sb.append("\n\t getNumberBytes() = " + this.getSize() + " 0x" + Util.toHexString(this.getSize()));
         sb.append("\n\t getImageWidth()  = " + this.getImageWidth() + " 0x" + Util.toHexString(this.getImageWidth()));
         sb.append("\t getImageHeight() = " + this.getImageHeight() + " 0x" + Util.toHexString(this.getImageHeight()));
         sb.append("\n\t getUserImageLocation() = " + this.getUserImageLocation() + " 0x" + Util.toHexString(this.getUserImageLocation()));
         sb.append("\n\t getTagData() = \"" + this.getTagData());
         sb.append("\"\n\t getImageHeaderSize() = " + this.getImageHeaderSize());
         sb.append("\"\n\t getQualityDetectionBit() = " + this.getQualityDetectionBit());
         sb.append("/>\n");
         return sb.toString();
      }
   }

   protected class ChkImageMemory implements CheckScannerHandle.ImageMemory {
      private Hashtable imgMemory = new Hashtable();
      private long currentSize = 0L;
      private static final long maxSize = 1048576L;

      public void addImage(CheckScannerHandle.ImageHeader ih) {
         this.imgMemory.put(new Short(ih.getImageLocation()), ih);
      }

      public void clearMemory() {
         this.imgMemory.clear();
         this.currentSize = 0L;
      }

      public boolean existImageHeader(short loc) {
         return this.imgMemory.containsKey(new Short(loc));
      }

      public CheckScannerHandle.ImageHeader getImage(short loc) {
         return (CheckScannerHandle.ImageHeader)this.imgMemory.get(new Short(loc));
      }

      public boolean existImageHeaderByUserLoc(int loc) {
         Iterator i = this.imgMemory.values().iterator();
         CheckScannerHandle.ImageHeader ih = null;

         while (i.hasNext()) {
            ih = (CheckScannerHandle.ImageHeader)i.next();
            if (ih.getUserImageLocation() == loc) {
               return true;
            }
         }

         return false;
      }

      public CheckScannerHandle.ImageHeader getImageByUserLocation(int loc) {
         Iterator i = this.imgMemory.values().iterator();
         CheckScannerHandle.ImageHeader ih = null;

         while (i.hasNext()) {
            ih = (CheckScannerHandle.ImageHeader)i.next();
            if (ih.getUserImageLocation() == loc) {
               return ih;
            }
         }

         return null;
      }

      public synchronized void calculateSize() {
         this.currentSize = 0L;
         Iterator i = this.imgMemory.values().iterator();
         CheckScannerHandle.ImageHeader ih = null;

         while (i.hasNext()) {
            ih = (CheckScannerHandle.ImageHeader)i.next();
            this.currentSize = this.currentSize + (long)ih.getSize();
            this.currentSize = this.currentSize + (long)ih.getImageHeaderSize();
         }
      }

      public int getImagesNumber() {
         return this.imgMemory.size();
      }

      public long getRemainingSize() {
         return this.getTotalSize() - this.getSize();
      }

      public int getRemainingImagesEstimate() {
         return this.getImagesNumber() == 0 ? 0 : (int)(this.getRemainingSize() / (this.getSize() / (long)this.getImagesNumber()));
      }

      public long getSize() {
         return this.currentSize;
      }

      public long getTotalSize() {
         return 1048576L;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("<ImageMemory:");
         sb.append("\n\t getTotalSize() = " + this.getTotalSize());
         sb.append("\n\t getSize() = " + this.getSize());
         sb.append("\n\t getRemainingSize() = " + this.getRemainingSize());
         sb.append("\n\t getRemainingImagesEstimate() = " + this.getRemainingImagesEstimate());
         Iterator i = this.imgMemory.values().iterator();

         while (i.hasNext()) {
            sb.append("\n" + (CheckScannerHandle.ImageHeader)i.next());
         }

         sb.append("\"ImageMemory/>\n");
         return sb.toString();
      }
   }

   protected class ChkState extends AbstractHandle.State implements CheckScannerHandle.State {
      private boolean isDocInserted = false;
      private boolean isDocRegistered = false;
      private boolean isCoverOpen = false;

      public boolean isDocInserted() {
         return this.isDocInserted;
      }

      public void setDocInserted(boolean b) {
         this.isDocInserted = b;
      }

      public boolean isDocRegistered() {
         return this.isDocRegistered;
      }

      public void setDocRegistered(boolean b) {
         this.isDocRegistered = b;
      }

      public void setCoverOpen(boolean b) {
         this.isCoverOpen = b;
      }

      public boolean isCoverOpen() {
         return this.isCoverOpen;
      }
   }
}
