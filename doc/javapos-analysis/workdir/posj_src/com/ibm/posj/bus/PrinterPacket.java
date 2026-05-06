package com.ibm.posj.bus;

import com.ibm.jutil.ByteArrayCollector;
import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.ByteBuffer.ByteBufferFactory;
import com.ibm.jutil.patterns.factory.RecyclableObject;
import com.ibm.jutil.patterns.factory.RecycleFactory;
import com.ibm.jutil.patterns.factory.RecycleFactoryException;
import com.ibm.jutil.patterns.factory.RecyclableObject.CtorArg;
import com.ibm.jutil.patterns.factory.RecycleFactory.CreateMethod;
import com.ibm.jutil.tasks.FifoScheduler;
import com.ibm.posj.HandleException;
import java.util.LinkedList;
import java.util.List;

public class PrinterPacket {
   PrinterPacket.PacketByteBufferFactory packetFactory = null;
   FifoScheduler scheduler = new FifoScheduler();
   List l = new LinkedList();
   ByteBuffer packages;
   byte[] oHead = null;
   byte[] defaultHeader = null;
   int max;
   int headersize;
   int used;
   private boolean raw = false;
   PrinterPacket.Transport trans;

   public PrinterPacket(PrinterPacket.Transport transport, byte[] header, int max) {
      this.packetFactory = new PrinterPacket.PacketByteBufferFactory(new PrinterPacket.PacketBufferCreator(max));
      this.max = max;
      this.headersize = header.length;
      this.oHead = ByteArrayCollector.getCollector().getArray(this.headersize);
      this.defaultHeader = ByteArrayCollector.getCollector().getArray(this.headersize);
      System.arraycopy(header, 0, this.oHead, 0, this.headersize);
      System.arraycopy(header, 0, this.defaultHeader, 0, this.headersize);
      this.createPackage(this.oHead);
      this.trans = transport;
   }

   public ByteBuffer requestPacket() {
      return this.packages;
   }

   public void appendBytes(byte[] cmd, int len) throws ArrayIndexOutOfBoundsException {
      if (!this.isRaw()) {
         if (len > this.remaining()) {
            throw new ArrayIndexOutOfBoundsException();
         } else {
            this.packages.append(cmd, 0, len);
            this.used += len;
         }
      }
   }

   public void resetHeader() {
      this.max = this.max + this.headersize;
      this.headersize = this.defaultHeader.length;
      this.packages.setStartIndex(0);
      this.packages.setByteCount(0, true);
      this.packages.append(this.defaultHeader, 0, this.headersize);
      this.packages.setStartIndex(this.headersize);
      this.max = this.max - this.headersize;
   }

   public boolean isRaw() {
      return this.raw;
   }

   public void setRaw(boolean iraw) {
      this.raw = iraw;
   }

   public boolean isEmpty() {
      return this.getMaximum() == this.remaining();
   }

   public void send() throws HandleException {
      if (this.isRaw()) {
         this.trans.transport(this.packages, 0);
      } else {
         this.trans.transport(this.packages, this.packages.getByteCount());
      }

      this.packages.setByteCount(this.headersize, false);
      this.used = 0;
   }

   public void send(ByteBuffer b) throws HandleException {
      if (this.isRaw()) {
         this.trans.transport(b, 0);
      } else {
         this.trans.transport(b, b.getByteCount());
      }
   }

   public int remaining() {
      return this.max - this.used;
   }

   public int getMaximum() {
      return this.max;
   }

   public ByteBuffer getHeader() {
      ByteBuffer buff = this.packetFactory.createPacket();
      buff.append(this.packages.getBytesRef(), 0, this.headersize);
      return buff;
   }

   public void setHeader(ByteBuffer newHead) throws NullPointerException {
      if (newHead == null) {
         throw new NullPointerException("Tried to reset header with null string");
      } else {
         this.max = this.max + this.headersize;
         this.headersize = newHead.getByteCount();
         this.packages.setStartIndex(0);
         this.packages.setByteCount(0, true);
         this.packages.append(newHead.getBytesRef(), 0, this.headersize);
         this.packages.setStartIndex(this.headersize);
         this.max = this.max - this.headersize;
      }
   }

   public ByteBuffer format() {
      ByteBuffer copy = this.packetFactory.createPacket();
      if (null == copy) {
         return null;
      } else {
         copy.setStartIndex(0);
         copy.setByteCount(0, true);
         copy.append(this.defaultHeader, 0, this.defaultHeader.length);
         copy.setStartIndex(this.defaultHeader.length);
         return copy;
      }
   }

   private void createPackage(byte[] header) {
      this.packages = this.packetFactory.createPacket();
      this.packages.append(header, 0, this.headersize);
      this.packages.setStartIndex(this.headersize);
   }

   class PacketBufferCreator implements CreateMethod {
      int deflen = 16;

      protected PacketBufferCreator(int max) {
         this.deflen = max;
      }

      public RecyclableObject newRecyclableObject(RecycleFactory recycleFactory) {
         ((ByteBufferFactory)recycleFactory).setInitialCapacity(this.deflen);
         return new ByteBuffer(recycleFactory);
      }

      public RecyclableObject newRecyclableObject(CtorArg[] ctorArgs, RecycleFactory recycleFactory) throws RecycleFactoryException {
         throw new RecycleFactoryException("Parameters not supported");
      }
   }

   protected class PacketByteBufferFactory extends ByteBufferFactory {
      protected PacketByteBufferFactory(CreateMethod cm) {
         super(cm);
      }

      protected ByteBuffer createPacket() {
         return this.createByteBuffer();
      }
   }

   public interface Transport {
      void transport(ByteBuffer var1, int var2) throws HandleException;
   }
}
