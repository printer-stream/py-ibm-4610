package com.ibm.posj.printer.parser.ibm4689;

import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.bus.printer.cmds.ibm4689.Cmd4689;
import com.ibm.posj.printer.parser.PrinterParserLine;
import java.util.List;

public class Printer4689ParserRotate {
   private PrinterParserLine.AlignedText lfLine;
   private PrinterParserLine.AlignedText ctLine;
   private PrinterParserLine.AlignedText rtLine;
   private Printer4689PropertiesManager propMgr;
   private Cmd4689 cmdBytes = new Cmd4689();
   ByteBuffer lineMasterText = ByteBuffer.getByteBufferFactory().createByteBuffer();
   private boolean isbld = false;
   private boolean isurl = false;
   private boolean isDhi = false;
   private boolean isDwi = false;
   private boolean isDhw = false;
   private boolean isNml = false;
   private boolean isReV = false;
   private boolean dWide = false;
   private boolean dHigh = false;
   private boolean LFlag = false;
   private int Xval;
   private int Yval;
   boolean[] High;
   private static String bytemask = new String("-");
   private static String space = new String("20 ");
   private static Tracer tracer = TracerFactory.getInstance().createTracer("4689Rotate");

   public Printer4689ParserRotate(int X, int Y) {
      this.Xval = X;
      this.Yval = Y + 1;
   }

   public void add(Printer4689ParserLine line) {
      if (getTracer().isOn()) {
         getTracer().println(2, "--> add()");
      }

      this.lfLine = line.getLeftText();
      this.ctLine = line.getCenterText();
      this.rtLine = line.getRightText();
      if (this.lfLine.isTextPresent()) {
         List alignedElement = ((Printer4689ParserLine.IBM4689AlignedText)this.lfLine).getElemList();
         if (getTracer().isOn()) {
            getTracer().println(2, "Left has text and its size is -> " + alignedElement.size());
         }

         this.appendBuffer(alignedElement);
      }

      if (this.ctLine.isTextPresent()) {
         List alignedElement = ((Printer4689ParserLine.IBM4689AlignedText)this.ctLine).getElemList();
         if (getTracer().isOn()) {
            getTracer().println(2, "Center has text and its size is -> " + alignedElement.size());
         }

         this.appendBuffer(alignedElement);
      }

      if (this.rtLine.isTextPresent()) {
         List alignedElement = ((Printer4689ParserLine.IBM4689AlignedText)this.rtLine).getElemList();
         if (getTracer().isOn()) {
            getTracer().println(2, "Right has text and its size is -> " + alignedElement.size());
         }

         this.appendBuffer(alignedElement);
      }

      if (this.lineMasterText.getByteCount() != 0 && this.LFlag) {
         this.lineMasterText.append((byte)-1);
         this.LFlag = false;
      }
   }

   public ByteBuffer getBuffer() {
      return this.rotate();
   }

   public static Tracer getTracer() {
      return tracer;
   }

   private void appendBuffer(List alignedElement) {
      for (int i = 0; i < alignedElement.size(); i++) {
         ByteBuffer tempBuffer = ByteBuffer.getByteBufferFactory().createByteBuffer();
         tempBuffer.replace(((Printer4689ParserLine.LineElement)alignedElement.get(i)).getText().getBytes());
         this.propMgr = ((Printer4689ParserLine.LineElement)alignedElement.get(i)).getPropMngr();
         if (((Printer4689ParserLine.LineElement)alignedElement.get(i)).getElementType() == 1) {
            int textLength = ((Printer4689ParserLine.LineElement)alignedElement.get(i)).getText().getByteCount();
            if (getTracer().isOn()) {
               getTracer().println(2, "Element is TEXT type and its size is -> " + textLength);
            }

            for (int x = 0; x < textLength; x++) {
               this.LFlag = true;
               if (this.propMgr.isBold()) {
                  this.lineMasterText.append(this.cmdBytes.EMPHASIZE_MODE);
               }

               if (this.propMgr.isUnderline()) {
                  this.lineMasterText.append(this.cmdBytes.UNDERLINE_MODE);
               }

               if (this.propMgr.isDoubleHigh() && !this.propMgr.isDoubleWide()) {
                  this.lineMasterText.append(this.cmdBytes.DOUBLE_HIGH_MODE);
               }

               if (this.propMgr.isDoubleWide()) {
                  this.dWide = true;
                  if (!this.propMgr.isDoubleHigh()) {
                     this.lineMasterText.append(this.cmdBytes.DOUBLE_WIDE_MODE);
                  }
               }

               if (this.propMgr.isDoubleHighWide()) {
                  this.lineMasterText.append(this.cmdBytes.DOUBLE_HIGH_WIDE_MODE);
               }

               if (this.propMgr.isNormalMode()) {
                  this.lineMasterText.append(this.cmdBytes.NORMAL_MODE);
               }

               if (this.propMgr.isReverseVideo()) {
                  this.lineMasterText.append(this.cmdBytes.REVERSE_VIDEO);
               }

               this.lineMasterText.append(tempBuffer.byteAt(x));
               if (this.dWide) {
                  this.lineMasterText.append(" ".getBytes());
                  this.dWide = false;
               }
            }
         }

         tempBuffer.recycle();
      }
   }

   private String getFilter(String cmd) {
      new String();
      return cmd.replaceAll(space, bytemask);
   }

   private String getMask(int Xval) {
      String mask = new String();
      mask = mask.concat("[14]");

      for (int x = 0; x < Xval; x++) {
         mask = mask.concat(bytemask);
      }

      return mask;
   }

   private ByteBuffer rotate() {
      int index = 0;
      ByteBuffer charRotated = ByteBuffer.getByteBufferFactory().createByteBuffer();
      ByteBuffer[][] matrix = new ByteBuffer[this.Xval][this.Yval];
      ByteBuffer cmdLine = ByteBuffer.getByteBufferFactory().createByteBuffer();
      this.High = new boolean[this.Yval];

      for (int x = 0; x < this.Xval; x++) {
         for (int y = 0; y < this.Yval; y++) {
            matrix[x][y] = ByteBuffer.getByteBufferFactory().createByteBuffer();
         }
      }

      if (getTracer().isOn()) {
         getTracer().println(2, "--> rotate()");
      }

      if (getTracer().isOn()) {
         getTracer().println(2, "createBuffer size: " + this.lineMasterText.getByteCount());
      }

      if (getTracer().isOn()) {
         getTracer().println(3, "buffer ->" + Util.toFormatedHexString(this.lineMasterText.getBytes()));
      }

      for (int x = this.Xval - 1; x >= 0; x--) {
         for (int y = 0; y < this.Yval; y++) {
            if (this.lineMasterText.getByteCount() == index) {
               matrix[x][y].append(" ".getBytes());
            } else {
               charRotated.reset();

               for (; this.lineMasterText.byteAt(index) == 27; index += 3) {
                  if (this.lineMasterText.byteAt(index + 2) == 16) {
                     this.isbld = true;
                  } else if (this.lineMasterText.byteAt(index + 2) == 2) {
                     this.isurl = true;
                  } else if (this.lineMasterText.byteAt(index + 2) == -128) {
                     this.isDhi = true;
                  } else if (this.lineMasterText.byteAt(index + 2) == 64) {
                     this.isDwi = true;
                  } else if (this.lineMasterText.byteAt(index + 2) == -64) {
                     this.isDhw = true;
                  } else if (this.lineMasterText.byteAt(index + 2) == 0) {
                     this.isNml = true;
                  } else if (this.lineMasterText.byteAt(index + 2) == 32) {
                     this.isReV = true;
                  }
               }

               if (this.isbld) {
                  charRotated.append(this.cmdBytes.EMPHASIZE_MODE);
                  this.isbld = false;
               } else if (this.isurl) {
                  charRotated.append(this.cmdBytes.UNDERLINE_MODE);
                  this.isurl = false;
               } else if (this.isDhi) {
                  charRotated.append(this.cmdBytes.DOUBLE_HIGH_MODE);
                  this.isDhi = false;
                  this.dHigh = true;
               } else if (this.isDwi) {
                  charRotated.append(this.cmdBytes.DOUBLE_WIDE_MODE);
                  this.isDwi = false;
               } else if (this.isDhw) {
                  charRotated.append(this.cmdBytes.DOUBLE_HIGH_WIDE_MODE);
                  this.isDhw = false;
                  this.dHigh = true;
               } else if (this.isNml) {
                  charRotated.append(this.cmdBytes.NORMAL_MODE);
                  this.isNml = false;
               } else if (this.isReV) {
                  charRotated.append(this.cmdBytes.REVERSE_VIDEO);
                  this.isReV = false;
               }

               if (this.lineMasterText.byteAt(index) != -1) {
                  charRotated.append(this.lineMasterText.byteAt(index));
                  if (this.dHigh) {
                     this.High[y] = this.dHigh;
                     this.dHigh = false;
                  } else {
                     charRotated.append(this.cmdBytes.NORMAL_MODE);
                  }

                  matrix[x][y].append(charRotated.getBytes());
                  index++;
               } else {
                  charRotated.append(" ".getBytes());

                  for (int z = y; z < this.Yval; z++) {
                     matrix[x][z].append(charRotated.getBytes());
                  }

                  index++;
                  if (y != 0) {
                     y = this.Yval;
                  } else {
                     y--;
                  }
               }
            }
         }
      }

      charRotated.recycle();
      new String();
      cmdLine.append(this.cmdBytes.MULTI_LINE_MODE);
      cmdLine.append(this.cmdBytes.ROTATE_CHARS90);
      cmdLine.append(this.cmdBytes.FEED_LINE_4689);

      for (int yx = 0; yx < this.Yval; yx++) {
         ByteBuffer cmd = ByteBuffer.getByteBufferFactory().createByteBuffer();

         for (int x = 0; x < this.Xval; x++) {
            cmd.append(matrix[x][yx].getBytes());
         }

         ByteBuffer tmpHigh = ByteBuffer.getByteBufferFactory().createByteBuffer();
         if (this.High[yx]) {
            for (int h = 0; h < cmd.getByteCount(); h++) {
               if (cmd.byteAt(h) == 27 && (cmd.byteAt(h + 2) == -128 || cmd.byteAt(h + 2) == -64)) {
                  for (int z = 1; z < h; z++) {
                     tmpHigh.append(cmd.byteAt(z));
                  }

                  for (int z = h; z < cmd.getByteCount(); z++) {
                     tmpHigh.append(cmd.byteAt(z));
                     if (z == h + 3) {
                        tmpHigh.append(" ".getBytes());
                     }
                  }
               }
            }

            cmd.replace(tmpHigh.getBytes());
            tmpHigh.recycle();
         }

         if (getTracer().isOn()) {
            getTracer().println(3, "Rotated Column -> " + Util.toFormatedHexString(cmd.getBytes()));
         }

         String var12 = this.getFilter(Util.toFormatedHexString(cmd.getBytes()));
         if (var12.compareTo(this.getMask(this.Xval)) != 0) {
            cmdLine.append(cmd.getBytes());
            cmdLine.append(this.cmdBytes.FEED_LINE_4689);
         }

         cmd.recycle();
      }

      for (int x = 0; x < this.Xval; x++) {
         for (int yx = 0; yx < this.Yval; yx++) {
            matrix[x][yx].recycle();
         }
      }

      this.lineMasterText.reset();
      if (getTracer().isOn()) {
         getTracer().println(3, "RotBuffer ->" + Util.toFormatedHexString(cmdLine.getBytes()));
      }

      if (getTracer().isOn()) {
         getTracer().println(2, "<-- rotate()");
      }

      return cmdLine;
   }
}
