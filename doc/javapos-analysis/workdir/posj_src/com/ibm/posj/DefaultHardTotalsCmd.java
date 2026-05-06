package com.ibm.posj;

import com.ibm.posj.bus.HardTotalsCmdSubmitter;

public abstract class DefaultHardTotalsCmd extends AbstractHandleCmd implements HardTotalsCmd {
   private String name = "";
   private int code = 0;
   private byte[] byteArray = new byte[0];
   private int completionCode = 0;
   private DefaultHardTotalsCmd.Factory factory = null;

   public DefaultHardTotalsCmd(HandleCmd.Factory factory, String name, int code) {
      super(factory);
      this.name = name;
      this.code = code;
      this.factory = (DefaultHardTotalsCmd.Factory)factory;
   }

   public String getName() {
      return this.name;
   }

   public int getCode() {
      return this.code;
   }

   public byte[] toBytes() {
      return this.byteArray;
   }

   public void accept(HandleCmdVisitor visitor) {
      visitor.visitHardTotalsCmd(this);
   }

   public HardTotalsCmd.Factory getFactory() {
      return this.factory;
   }

   public int getCompletionCode() {
      return this.completionCode;
   }

   public void setCompletionCode(int completionCode) {
      this.completionCode = completionCode;
   }

   public abstract void submit() throws HandleException;

   public static class Factory extends DefaultSystemCmd.Factory implements HardTotalsCmd.Factory {
      private HardTotalsCmdSubmitter submitter = null;

      public void setHardTotalsCmdSubmitter(HardTotalsCmdSubmitter submitter) {
         this.submitter = submitter;
      }

      public HardTotalsCmdSubmitter getHardTotalsCmdSubmitter() {
         return this.submitter;
      }

      public HardTotalsCmd.GetHardTotalsInfCmd createGetHardTotalsInfCmd() {
         return new DefaultHardTotalsCmd.GetHardTotalsInfCmd(this);
      }

      public HardTotalsCmd.ReadHardTotalsDataCmd createReadHardTotalsDataCmd(int offset, int length, byte[] buffer) {
         return new DefaultHardTotalsCmd.ReadHardTotalsDataCmd(this, offset, length, buffer);
      }

      public HardTotalsCmd.WriteHardTotalsDataCmd createWriteHardTotalsDataCmd(int offset, int length, byte[] buffer, int buffOffset) {
         return new DefaultHardTotalsCmd.WriteHardTotalsDataCmd(this, offset, length, buffer, buffOffset);
      }
   }

   static class GetHardTotalsInfCmd extends DefaultHardTotalsCmd implements HardTotalsCmd.GetHardTotalsInfCmd {
      private int totalSize = 0;
      private int userSize = 0;
      private int systemSize = 0;

      GetHardTotalsInfCmd(HandleCmd.Factory factory) {
         super(factory, "GetHardTotalsInfCmd", 201);
      }

      public int getTotalSize() {
         return this.totalSize;
      }

      public void setTotalSize(int size) {
         this.totalSize = size;
      }

      public int getUserSize() {
         return this.userSize;
      }

      public void setUserSize(int size) {
         this.userSize = size;
      }

      public int getSystemSize() {
         return this.systemSize;
      }

      public void setSystemSize(int size) {
         this.systemSize = size;
      }

      public void submit() throws HandleException {
         this.getFactory().getHardTotalsCmdSubmitter().submitGetHardTotalsInfCmd(this);
      }
   }

   abstract static class HardTotalsDataCmd extends DefaultHardTotalsCmd implements HardTotalsCmd.HardTotalsDataCmd {
      private int offset = 0;
      private int length = 0;
      private int bufferOffset = 0;
      private byte[] buffer = null;

      HardTotalsDataCmd(HandleCmd.Factory factory, String commandName, int commandCode, int offset, int length, byte[] buffer, int bufferOffset) {
         super(factory, commandName, commandCode);
         this.setOffset(offset);
         this.setLength(length);
         this.setBuffer(buffer);
         this.setBufferOffset(bufferOffset);
      }

      public int getOffset() {
         return this.offset;
      }

      public void setOffset(int offset) {
         this.offset = offset;
      }

      public int getLength() {
         return this.length;
      }

      public void setLength(int length) {
         this.length = length;
      }

      public byte[] getBuffer() {
         return this.buffer;
      }

      public void setBuffer(byte[] buffer) {
         this.buffer = buffer;
      }

      public int getBufferOffset() {
         return this.bufferOffset;
      }

      public void setBufferOffset(int bufferOffset) {
         this.bufferOffset = bufferOffset;
      }

      public abstract void submit() throws HandleException;
   }

   static class ReadHardTotalsDataCmd extends DefaultHardTotalsCmd.HardTotalsDataCmd implements HardTotalsCmd.ReadHardTotalsDataCmd {
      ReadHardTotalsDataCmd(HandleCmd.Factory factory, int offset, int length, byte[] buffer) {
         super(factory, "ReadHardTotalsDataCmd", 202, offset, length, buffer, 0);
      }

      public void submit() throws HandleException {
         this.getFactory().getHardTotalsCmdSubmitter().submitReadHardTotalsDataCmd(this);
      }
   }

   static class WriteHardTotalsDataCmd extends DefaultHardTotalsCmd.HardTotalsDataCmd implements HardTotalsCmd.WriteHardTotalsDataCmd {
      WriteHardTotalsDataCmd(HandleCmd.Factory factory, int offset, int length, byte[] buffer, int bufferOffset) {
         super(factory, "WriteHardTotalsDataCmd", 203, offset, length, buffer, bufferOffset);
      }

      public void submit() throws HandleException {
         this.getFactory().getHardTotalsCmdSubmitter().submitWriteHardTotalsDataCmd(this);
      }
   }
}
