package com.ibm.posj.printer.parser.ibm4689;

import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.ByteEncoder;
import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.ibm4689.Cmd4689;
import com.ibm.posj.printer.parser.DefaultPrinterEscCmdV;
import com.ibm.posj.printer.parser.ParserLine;
import com.ibm.posj.printer.parser.PrinterEscCmd;
import com.ibm.posj.printer.parser.PrinterEscCmdProcessor;
import com.ibm.posj.printer.parser.PrinterParserElement;
import com.ibm.posj.printer.parser.PrinterParserEscCmd;
import com.ibm.posj.printer.parser.PrinterParserLine;
import java.util.ArrayList;
import java.util.List;

public class Printer4689ParserLine extends PrinterParserLine implements ParserLine {
   private Printer4689ParserLine.LeftText leftText = null;
   private Printer4689ParserLine.CenterText centerText = null;
   private Printer4689ParserLine.RightText rightText = null;
   private Cmd4689 cmdBytes = new Cmd4689();
   private boolean dataTruncated = false;
   private ByteBuffer highMagnificationChars = null;
   private static Tracer parserTracer = TracerFactory.getInstance().createTracer("POSPrinter", "4689ParserLine");
   Printer4689PropertiesManager propMngr = null;
   Printer4689ParserLine.AppendPropertyVisitor appendVisitor = new Printer4689ParserLine.AppendPropertyVisitor();
   private ByteBuffer lineMasterText = ByteBuffer.getByteBufferFactory().createByteBuffer();

   public Printer4689ParserLine(PrinterEscCmdProcessor escCmdProc) {
      super(escCmdProc);
   }

   public void buildLineMasterCmd(boolean addLineFeedAtTheEnd) {
      this.lineMasterText.reset();
      if (getTracer().isOn()) {
         this.trace("buildLineMasterCmd >>");
      }

      int leftTextLimit = this.getLeftText().isTextPresent() ? this.getLeftText().getRightLimit() : 0;
      int rightTextLimit = this.getRightText().isTextPresent() ? this.getRightText().getLeftLimit() : 0;
      int spaces = 0;
      if (this.getLeftText().isTextPresent()) {
         if (getTracer().isOn()) {
            this.trace("LeftText().isTextPresent()");
         }

         this.appendAlignedText((Printer4689ParserLine.IBM4689AlignedText)this.getLeftText());
      }

      if (this.getCenterText().isTextPresent()) {
         if (getTracer().isOn()) {
            this.trace("CenterText().isTextPresent()");
         }

         int leftChars = 0;
         if (this.getLeftText().isTextPresent()) {
            leftChars = this.getLeftText().getTextSize();
            if (leftTextLimit < this.getState().getLineChars(this.getStation()) / 2) {
               spaces = this.getCenterText().getLeftLimit() - leftChars;
            }
         } else {
            spaces = (this.getState().getLineChars(this.getStation()) - this.getCenterText().getTextSize()) / 2;
         }

         ((Printer4689ParserLine.LineElement)((Printer4689ParserLine.IBM4689AlignedText)this.getCenterText()).getElemList().get(0)).setAlignSpaces(spaces);
         this.appendAlignedText((Printer4689ParserLine.IBM4689AlignedText)this.getCenterText());
      }

      if (this.getRightText().isTextPresent()) {
         if (getTracer().isOn()) {
            this.trace("RightText().isTextPresent()");
         }

         spaces = this.getState().getLineChars(this.getStation()) - rightTextLimit - this.getRightText().getTextSize();
         ((Printer4689ParserLine.LineElement)((Printer4689ParserLine.IBM4689AlignedText)this.getRightText()).getElemList().get(0)).setAlignSpaces(spaces);
         this.appendAlignedText((Printer4689ParserLine.IBM4689AlignedText)this.getRightText());
      }

      this.lineMasterText.append(this.getHighMagnificationChars());
      this.highMagnificationChars.reset();
      if (!this.getPropMngr().isDoubleHigh()) {
         this.getPropMngr().setLineHighMagnification((byte)0);
      }

      if (getTracer().isOn()) {
         this.trace("buildLineMasterCmd <<");
      }
   }

   public int appendElement(PrinterParserElement parserElement) {
      if (getTracer().isOn()) {
         this.trace("appendElement >>");
      }

      int appendedChars = 0;
      Printer4689ParserLine.IBM4689AlignedText alignedText = (Printer4689ParserLine.IBM4689AlignedText)this.getAlignedText();
      Printer4689ParserLine.LineElement lineElem = alignedText.getCurrentLineElement();
      if (parserElement.getElementType() == 2) {
         PrinterParserEscCmd parserEscCmd = (PrinterParserEscCmd)parserElement;
         if (parserEscCmd.isProperty()) {
            PrinterEscCmd escCmd = ((PrinterParserEscCmd)parserElement).getEscCmd();

            try {
               alignedText.createNewElement();
               this.updateState((PrinterParserEscCmd)parserElement);
               escCmd.accept(this.getAppendVisitor());
            } catch (IllegalArgumentException var8) {
               if (getTracer().isOn()) {
                  this.trace(var8.toString());
               }
            } catch (IllegalAccessException var9) {
               if (getTracer().isOn()) {
                  this.trace(var9.toString());
               }
            }
         }
      } else if (parserElement.getElementType() == 1 || parserElement.getElementType() == 5) {
         if (getTracer().isOn()) {
            this.trace("element type is = " + parserElement.getElementType());
         }

         appendedChars = alignedText.appendText(parserElement);
         this.charsInLine = this.charsInLine + appendedChars * this.getWideMagnification();
         if (this.charsInLine >= this.getState().getLineChars(this.getStation())) {
            this.lineCompleted = true;
         }
      }

      if (getTracer().isOn()) {
         this.trace("Printer4689ParserLine.appendElement <<");
      }

      return appendedChars;
   }

   public void reset() {
      super.reset();
      this.getPropMngr().reset();
      this.resetText();
      this.getHighMagnificationChars().reset();
   }

   public void resetText() {
      if (getTracer().isOn()) {
         this.trace("resetText");
      }

      this.charsInLine = 0;
      this.lineMasterText.reset();
      this.lineCompleted = false;
      ((Printer4689ParserLine.IBM4689AlignedText)this.getLeftText()).init();
      ((Printer4689ParserLine.IBM4689AlignedText)this.getCenterText()).init();
      ((Printer4689ParserLine.IBM4689AlignedText)this.getRightText()).init();
   }

   public PrinterParserLine.AlignedText getCenterText() {
      if (this.centerText == null) {
         this.centerText = new Printer4689ParserLine.CenterText();
      }

      return this.centerText;
   }

   public PrinterParserLine.AlignedText getLeftText() {
      if (this.leftText == null) {
         this.leftText = new Printer4689ParserLine.LeftText();
      }

      return this.leftText;
   }

   public PrinterParserLine.AlignedText getRightText() {
      if (this.rightText == null) {
         this.rightText = new Printer4689ParserLine.RightText();
      }

      return this.rightText;
   }

   public ByteBuffer getLineMasterText() {
      return this.lineMasterText;
   }

   public Printer4689ParserLine.AppendPropertyVisitor getAppendVisitor() {
      return this.appendVisitor;
   }

   public boolean isDataTruncated() {
      return this.dataTruncated;
   }

   public void setDataTruncated(boolean dataTruncated) {
      this.dataTruncated = dataTruncated;
   }

   public ByteBuffer getHighMagnificationChars() {
      if (this.highMagnificationChars == null) {
         this.highMagnificationChars = ByteBuffer.getByteBufferFactory().createByteBuffer();
      }

      return this.highMagnificationChars;
   }

   protected void appendAlignedText(Printer4689ParserLine.IBM4689AlignedText alignedText) {
      List elemList = alignedText.getElemList();
      if (getTracer().isOn()) {
         this.trace("appendAlignedText elemList.size() = " + elemList.size());
      }

      for (int i = 0; i < elemList.size(); i++) {
         int elemType = ((Printer4689ParserLine.LineElement)elemList.get(i)).getElementType();
         Printer4689ParserLine.LineElement e = (Printer4689ParserLine.LineElement)elemList.get(i);
         if (e.getAlignSpaces() > 0) {
            this.lineMasterText.append(this.createSpacesBuffer(e.getAlignSpaces()));
         }

         this.appendTextProperties(e);
         if (e.getPropMngr().isDoubleHigh()) {
            if (e.getPropMngr().isDoubleWide()) {
               this.addDoubleHighWideChars(e.getText().getByteCount());
            } else {
               this.addDoubleHighChars(e.getText().getByteCount());
            }
         } else if (!e.getPropMngr().isDoubleHigh() && this.getPropMngr().getLineHighMagnification() == 3 && e.getText().getByteCount() > 0) {
            this.getHighMagnificationChars().append(this.createSpacesBuffer(e.getText().getByteCount()));
         }

         if (e.getPropMngr().isDoubleWide()) {
            e.setText(this.addDoubleWideChars(e.getText()));
         }

         this.lineMasterText.append(e.getText());
      }
   }

   protected Printer4689PropertiesManager getPropMngr() {
      if (this.propMngr == null) {
         this.propMngr = new Printer4689PropertiesManager();
      }

      return this.propMngr;
   }

   protected int getWideMagnification() {
      int magnification = this.getState().getFontWideMagnification(this.getStation());
      if (magnification < 1) {
         throw new IllegalArgumentException("Invalid wide maginification");
      } else {
         return magnification;
      }
   }

   protected void trace(String s) {
      if (getTracer().isOn()) {
         getTracer().println(3, s);
      }
   }

   protected static Tracer getTracer() {
      return parserTracer;
   }

   private void appendTextProperties(Printer4689ParserLine.LineElement elem) {
      if (elem.getPropMngr().isNormalMode()) {
         if (elem.getText().getByteCount() > 0) {
            if (getTracer().isOn()) {
               this.trace("IS NORMAL MODE!");
            }

            this.lineMasterText.append(this.cmdBytes.NORMAL_MODE);
         }
      } else {
         if (elem.getPropMngr().isBold()) {
            this.lineMasterText.append(this.cmdBytes.EMPHASIZE_MODE);
         }

         if (elem.getPropMngr().isUnderline()) {
            this.lineMasterText.append(this.cmdBytes.UNDERLINE_MODE);
         }

         if (elem.getPropMngr().isReverseVideo()) {
            this.lineMasterText.append(this.cmdBytes.REVERSE_VIDEO);
         }

         if (elem.getPropMngr().isDoubleHigh()) {
            if (elem.getPropMngr().isDoubleWide()) {
               this.lineMasterText.append(this.cmdBytes.DOUBLE_HIGH_WIDE_MODE);
            } else {
               this.lineMasterText.append(this.cmdBytes.DOUBLE_HIGH_MODE);
            }
         } else if (elem.getPropMngr().isDoubleWide()) {
            this.lineMasterText.append(this.cmdBytes.DOUBLE_WIDE_MODE);
         }
      }
   }

   private ByteBuffer createSpacesBuffer(int spaces) {
      if (getTracer().isOn()) {
         this.trace("addSpaces >>");
      }

      if (getTracer().isOn()) {
         this.trace("spaces to add = " + spaces);
      }

      if (spaces <= 0) {
         throw new IllegalArgumentException("0 is an illegal argument in createSpacesBuffer");
      } else {
         ByteBuffer tmp = ByteBuffer.getByteBufferFactory().createByteBuffer();
         tmp.append(this.cmdBytes.CHAR_ATTRB_NORMAL);

         for (int i = 0; i < spaces; i++) {
            tmp.append(32);
         }

         return tmp;
      }
   }

   private ByteBuffer addDoubleWideChars(ByteBuffer text) {
      int bufferPosition = 1;
      int count = text.getByteCount();
      ByteEncoder bEnc = null;

      try {
         bEnc = this.getEscCmdProcessor().getByteEncoder();
      } catch (IllegalAccessException var6) {
         if (getTracer().isOn()) {
            this.trace("IllegalAccessException caught");
         }
      }

      for (int i = 1; i < count; i++) {
         if (bEnc.isDBCSChar(text, bufferPosition - 1)) {
            if (++bufferPosition >= text.getByteCount()) {
               text.append(0);
               text.append(0);
            } else {
               i++;
               text.insert((byte)0, bufferPosition++);
               text.insert((byte)0, bufferPosition);
            }
         } else {
            text.insert((byte)0, bufferPosition);
         }

         bufferPosition += 2;
      }

      text.append(0);
      return text;
   }

   private void addDoubleHighChars(int count) {
      this.getHighMagnificationChars().append(this.cmdBytes.LINE_ATTRB_NORMAL);

      for (int i = 0; i < count; i++) {
         this.getHighMagnificationChars().append(this.cmdBytes.DOUBLE_HIGH_MODE);
         this.getHighMagnificationChars().append(0);
      }
   }

   private void addDoubleHighWideChars(int count) {
      this.getHighMagnificationChars().append(this.cmdBytes.LINE_ATTRB_NORMAL);

      for (int i = 0; i < count; i++) {
         this.getHighMagnificationChars().append(this.cmdBytes.DOUBLE_HIGH_WIDE_MODE);
         this.getHighMagnificationChars().append(0);
      }
   }

   class AppendPropertyVisitor extends DefaultPrinterEscCmdV {
      public POSPrinterCmd getPOSPrinterCmd() {
         return null;
      }

      public void reset() {
         Printer4689ParserLine.this.getPropMngr().reset();
      }

      public void visitBoldEscCmd(PrinterEscCmd cmd) throws IllegalAccessException, IllegalArgumentException {
         Printer4689ParserLine.this.getPropMngr().setBold(true);
      }

      public void visitCenterEscCmd(PrinterEscCmd cmd) throws IllegalAccessException, IllegalArgumentException {
         super.visitCenterEscCmd(cmd);
      }

      public void visitHighWideControlEscCmd(PrinterEscCmd cmd) throws IllegalAccessException, IllegalArgumentException {
         switch (cmd.getParameter()) {
            case 1:
               Printer4689ParserLine.this.getPropMngr().setDoubleHigh(false);
               Printer4689ParserLine.this.getPropMngr().setDoubleWide(false);
               break;
            case 2:
               Printer4689ParserLine.this.getPropMngr().setDoubleWide(true);
               break;
            case 3:
               Printer4689ParserLine.this.getPropMngr().setDoubleHigh(true);
               break;
            case 4:
               Printer4689ParserLine.this.getPropMngr().setDoubleHigh(true);
               Printer4689ParserLine.this.getPropMngr().setDoubleWide(true);
               break;
            default:
               throw new IllegalArgumentException("wrong HighWideControl argument");
         }
      }

      public void visitNormalEscCmd(PrinterEscCmd cmd) throws IllegalAccessException, IllegalArgumentException {
         if (Printer4689ParserLine.getTracer().isOn()) {
            Printer4689ParserLine.this.trace("visitNormalEscCmd");
         }

         Printer4689ParserLine.this.getPropMngr().reset();
      }

      public void visitNormalDataCmd(PrinterEscCmd cmd) throws IllegalAccessException, IllegalArgumentException {
         if (Printer4689ParserLine.getTracer().isOn()) {
            Printer4689ParserLine.this.trace("visitNormalData");
         }

         Printer4689ParserLine.this.getPropMngr().reset();
      }

      public void visitReverseVideoEscCmd(PrinterEscCmd cmd) throws IllegalAccessException, IllegalArgumentException {
         Printer4689ParserLine.this.getPropMngr().setReverseVideo(true);
      }

      public void visitRightJustifyEscCmd(PrinterEscCmd cmd) throws IllegalAccessException, IllegalArgumentException {
         super.visitRightJustifyEscCmd(cmd);
      }

      public void visitScaleHorizontallyEscCmd(PrinterEscCmd cmd) throws IllegalAccessException, IllegalArgumentException {
         if (cmd.getClosestParameter() > 1) {
            Printer4689ParserLine.this.getPropMngr().setDoubleWide(true);
         }
      }

      public void visitScaleVerticallyEscCmd(PrinterEscCmd cmd) throws IllegalAccessException, IllegalArgumentException {
         if (cmd.getClosestParameter() > 1) {
            Printer4689ParserLine.this.getPropMngr().setDoubleHigh(true);
         }
      }

      public void visitUnderlineEscCmd(PrinterEscCmd cmd) throws IllegalAccessException, IllegalArgumentException {
         if (cmd.getClosestParameter() > 0) {
            Printer4689ParserLine.this.getPropMngr().setUnderline(true);
         }
      }
   }

   class CenterText extends Printer4689ParserLine.IBM4689AlignedText {
      public CenterText() {
         this.setLeftLimit(this.getLeftLimit());
         this.setRightLimit(this.getRightLimit());
      }

      protected void updateLimits(int amount) {
         int lineWidth = Printer4689ParserLine.this.getState().getLineChars(Printer4689ParserLine.this.getStation());
         int centerLeftLimit = (lineWidth - amount) / 2;
         int centerRightLimit = (lineWidth + amount) / 2;
         this.setLeftLimit(centerLeftLimit);
         this.setRightLimit(centerRightLimit);
         this.setInitialPosition(centerLeftLimit);
         Printer4689ParserLine.this.getLeftText().setRightLimit(centerLeftLimit);
         Printer4689ParserLine.this.getRightText().setLeftLimit(centerRightLimit);
      }
   }

   public abstract class IBM4689AlignedText extends PrinterParserLine.AlignedText {
      List elemList = new ArrayList();
      private int listIndex = 0;

      public void init() {
         this.reset();
         this.setLeftLimit(0);
         this.setRightLimit(Printer4689ParserLine.this.getState().getLineChars(Printer4689ParserLine.this.getStation()));
         this.listIndex = 0;
         this.textSize = 0;
         this.elemList = new ArrayList();
      }

      public List getElemList() {
         return this.elemList;
      }

      public Printer4689ParserLine.LineElement getCurrentLineElement() {
         try {
            if (this.elemList.isEmpty() || this.listIndex >= this.elemList.size()) {
               this.elemList.add(this.listIndex, Printer4689ParserLine.this.new LineElement(Printer4689ParserLine.this.getEscCmdProcessor()));
            }
         } catch (IllegalAccessException var2) {
            if (Printer4689ParserLine.getTracer().isOn()) {
               Printer4689ParserLine.getTracer().println("null escCmdProcessor");
            }
         }

         ((Printer4689ParserLine.LineElement)this.getElemList().get(this.listIndex)).setPropMngr(Printer4689ParserLine.this.getPropMngr());
         return (Printer4689ParserLine.LineElement)this.getElemList().get(this.listIndex);
      }

      public void createNewElement() {
         this.listIndex++;

         try {
            Printer4689ParserLine.LineElement newElem = Printer4689ParserLine.this.new LineElement(Printer4689ParserLine.this.getEscCmdProcessor());
            this.elemList.add(this.listIndex, newElem);
         } catch (IllegalAccessException var2) {
            if (Printer4689ParserLine.getTracer().isOn()) {
               Printer4689ParserLine.getTracer().println("null escCmdProcessor");
            }
         }

         this.getCurrentLineElement();
      }

      public int getTextSize() {
         return super.getTextSize() * Printer4689ParserLine.this.getPropMngr().getWideMagnification();
      }

      protected void appendCmd(PrinterParserEscCmd escCmd) {
         Printer4689ParserLine.LineElement elem = Printer4689ParserLine.this.new LineElement(escCmd);
         this.listIndex++;
         this.getElemList().add(this.listIndex++, elem);
      }

      protected int appendText(PrinterParserElement parserElem) {
         if (Printer4689ParserLine.getTracer().isOn()) {
            Printer4689ParserLine.this.trace("appendText >>");
         }

         int charsToAppend = 0;
         if (this.getFreeSpace() > 0) {
            int freeSpace = this.getFreeSpace() / Printer4689ParserLine.this.getWideMagnification();
            if (Printer4689ParserLine.getTracer().isOn()) {
               Printer4689ParserLine.this.trace("getFreeSpace() = " + this.getFreeSpace());
            }

            if (parserElem.getLength() > freeSpace) {
               charsToAppend = freeSpace;
            } else {
               charsToAppend = parserElem.getLength();
            }

            if (Printer4689ParserLine.this.getCharsInLine() + charsToAppend
               > Printer4689ParserLine.this.getState().getMaxPrintChars(Printer4689ParserLine.this.getStation())) {
               charsToAppend = Printer4689ParserLine.this.getState().getMaxPrintChars(Printer4689ParserLine.this.getStation())
                  - Printer4689ParserLine.this.getCharsInLine();
            }

            if (charsToAppend < parserElem.getLength() && parserElem.isDBCSChar(charsToAppend)) {
               charsToAppend--;
            }

            parserElem.setCharsToPrint(charsToAppend);
            Printer4689ParserLine.this.dataTruncated = parserElem.isDataTruncated();
            if (Printer4689ParserLine.getTracer().isOn()) {
               Printer4689ParserLine.this.trace("parserElem.isDataTruncated? = " + parserElem.isDataTruncated());
            }

            if (charsToAppend < parserElem.getLength()) {
               Printer4689ParserLine.this.dataTruncated = true;
               if (Printer4689ParserLine.getTracer().isOn()) {
                  Printer4689ParserLine.this.trace("seting dataTruncated = " + Printer4689ParserLine.this.dataTruncated);
               }
            }

            if (charsToAppend > 0) {
               if (Printer4689ParserLine.getTracer().isOn()) {
                  Printer4689ParserLine.this.trace("charsToAppend = " + charsToAppend);
               }

               this.getCurrentLineElement().setText(parserElem.getBytesRef(), charsToAppend, parserElem.getIndex());
               this.increaseTextSize(charsToAppend);
               if (Printer4689ParserLine.getTracer().isOn()) {
                  Printer4689ParserLine.this.trace("getTextSize = " + this.getTextSize());
               }

               this.updateLimits(charsToAppend * Printer4689ParserLine.this.getWideMagnification());
            }
         } else if (Printer4689ParserLine.getTracer().isOn()) {
            Printer4689ParserLine.this.trace("freeSpace = 0 ");
         }

         if (Printer4689ParserLine.getTracer().isOn()) {
            Printer4689ParserLine.this.trace("appendText << ");
         }

         return charsToAppend;
      }

      protected void completeTextMasterCommand() {
      }

      protected int getFreeSpace() {
         int freeSpace = 0;
         byte align = 0;

         try {
            align = Printer4689ParserLine.this.getEscCmdProcessor().getParserState().getAlignment(Printer4689ParserLine.this.getStation());
         } catch (IllegalAccessException var4) {
            var4.printStackTrace();
         }

         switch (align) {
            case 0:
               freeSpace = super.getFreeSpace();
            case 1:
               freeSpace = ((Printer4689ParserLine.IBM4689AlignedText)Printer4689ParserLine.this.getCenterText()).getRightLimit()
                  - ((Printer4689ParserLine.IBM4689AlignedText)Printer4689ParserLine.this.getCenterText()).getLeftLimit()
                  - ((Printer4689ParserLine.IBM4689AlignedText)Printer4689ParserLine.this.getCenterText()).getTextSize();
            case 2:
               freeSpace = ((Printer4689ParserLine.IBM4689AlignedText)Printer4689ParserLine.this.getRightText()).getRightLimit()
                  - ((Printer4689ParserLine.IBM4689AlignedText)Printer4689ParserLine.this.getRightText()).getLeftLimit()
                  - ((Printer4689ParserLine.IBM4689AlignedText)Printer4689ParserLine.this.getRightText()).getTextSize();
            default:
               return freeSpace;
         }
      }
   }

   class LeftText extends Printer4689ParserLine.IBM4689AlignedText {
      public LeftText() {
         this.setLeftLimit(this.getLeftLimit());
         this.setRightLimit(this.getRightLimit());
      }

      protected void updateLimits(int amount) {
         int lineWidth = Printer4689ParserLine.this.getState().getLineChars(Printer4689ParserLine.this.getStation());
         Printer4689ParserLine.this.getCenterText().setLeftLimit(amount);
         Printer4689ParserLine.this.getCenterText().setRightLimit(lineWidth - amount);
         if (!Printer4689ParserLine.this.getCenterText().isTextPresent()) {
            Printer4689ParserLine.this.getRightText().setLeftLimit(amount);
         }
      }
   }

   class LineElement extends PrinterParserElement {
      private int lineElemType = 0;
      private Printer4689PropertiesManager propMngr;
      private PrinterParserEscCmd cmd = null;
      private ByteBuffer text = null;
      private int alignSpaces = 0;

      public LineElement(PrinterEscCmdProcessor escCmdProc) {
         super(escCmdProc);
         this.lineElemType = 0;
      }

      public LineElement(PrinterParserEscCmd cmd) {
         this.lineElemType = 2;
         this.cmd = cmd;
      }

      public LineElement(ByteBuffer text) {
         this.lineElemType = 1;
         this.text = text;
      }

      public LineElement(ByteBuffer text, int type) {
         this.lineElemType = type;
         this.text = text;
      }

      public void setText(byte[] array, int length, int index) {
         if (Printer4689ParserLine.getTracer().isOn()) {
            Printer4689ParserLine.this.trace("LineElement setText >>");
         }

         this.lineElemType = 1;
         if (Printer4689ParserLine.getTracer().isOn()) {
            Printer4689ParserLine.this.trace("length = " + length + "   index = " + index);
         }

         byte[] tmp = new byte[length];
         System.arraycopy(array, index, tmp, 0, length);
         if (Printer4689ParserLine.getTracer().isOn()) {
            Printer4689ParserLine.this.trace("text = " + Util.toFormatedHexString(tmp));
         }

         this.getText().reset();
         this.getText().append(tmp);
         if (Printer4689ParserLine.getTracer().isOn()) {
            Printer4689ParserLine.this.trace("LineElement setText << ");
         }
      }

      public ByteBuffer getText() {
         if (this.text == null) {
            this.text = ByteBuffer.getByteBufferFactory().createByteBuffer();
         }

         return this.text;
      }

      public void setText(ByteBuffer text) {
         if (text != null) {
            this.text = text;
         } else {
            throw new IllegalArgumentException("argument should not be null");
         }
      }

      public Printer4689PropertiesManager getPropMngr() {
         if (this.propMngr == null) {
            if (Printer4689ParserLine.getTracer().isOn()) {
               Printer4689ParserLine.this.trace("propMngr == null");
            }

            this.propMngr = new Printer4689PropertiesManager();
            this.propMngr.setProperties(Printer4689ParserLine.this.getPropMngr());
         }

         return this.propMngr;
      }

      public void setPropMngr(Printer4689PropertiesManager propMngr) {
         this.getPropMngr().setProperties(propMngr);
      }

      public int getAlignSpaces() {
         return this.alignSpaces;
      }

      public void setAlignSpaces(int alignSpaces) {
         this.alignSpaces = alignSpaces;
      }
   }

   class RightText extends Printer4689ParserLine.IBM4689AlignedText {
      public RightText() {
         this.setLeftLimit(this.getLeftLimit());
         this.setRightLimit(this.getRightLimit());
      }

      protected void updateLimits(int amount) {
         int lineWidth = Printer4689ParserLine.this.getState().getLineChars(Printer4689ParserLine.this.getStation());
         int rightRightLimit = lineWidth - amount;
         this.setInitialPosition(rightRightLimit);
         Printer4689ParserLine.this.getCenterText().setRightLimit(rightRightLimit);
         if (!Printer4689ParserLine.this.getCenterText().isTextPresent()) {
            Printer4689ParserLine.this.getLeftText().setRightLimit(rightRightLimit);
         }
      }
   }
}
