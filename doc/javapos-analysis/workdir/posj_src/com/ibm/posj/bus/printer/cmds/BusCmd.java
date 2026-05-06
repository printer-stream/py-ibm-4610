package com.ibm.posj.bus.printer.cmds;

import com.ibm.jutil.ByteBuffer;

public class BusCmd extends PrintCmd {
   private Object parent = null;
   private ByteBuffer bytes = null;

   public BusCmd(Object parent, ByteBuffer busCmdArray) {
      super(null);
      this.bytes = busCmdArray;
      this.parent = parent;
      super.setWaitToStart(true);
      super.setWaitToFinish(true);
      super.setBuffered(true);
   }

   public ByteBuffer getByteBuffer() {
      return this.bytes;
   }

   public Object getBusParent() {
      return this.parent;
   }
}
