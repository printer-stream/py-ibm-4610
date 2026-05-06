package com.ibm.posj.printer.parser.ibm4689;

import com.ibm.jutil.ByteBuffer;
import com.ibm.jutil.Util;
import com.ibm.jutil.logging.DefaultLogHelper;
import com.ibm.jutil.logging.LogHelper;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import com.ibm.posj.POSPrinterCmd;
import com.ibm.posj.bus.printer.cmds.ibm4689.Cmd4689;
import com.ibm.posj.printer.parser.ParserLine;
import com.ibm.posj.printer.parser.PrinterParserData;
import com.ibm.posj.printer.parser.PrinterParserElement;
import com.ibm.posj.printer.parser.PrinterParserEscCmd;
import java.util.ArrayList;
import java.util.List;

public class Printer4689ParserData extends PrinterParserData {
   protected List linesToRotate = new ArrayList();
   private ByteBuffer mainCmdBuffer = ByteBuffer.getByteBufferFactory().createByteBuffer();
   private Printer4689ParserRotate parserRotate = null;
   private Cmd4689 cmdBytes = new Cmd4689();
   private ParserLine line = null;
   private int printedChars = 0;
   private boolean isActionCmd = false;
   private boolean dataTruncated = false;
   private boolean appendLF = false;
   private boolean dataRemaining = false;
   private boolean rotateMode = false;
   private static Tracer parserTracer = TracerFactory.getInstance().createTracer("POSPrinter", "4689ParserData");
   private static LogHelper logHelper = DefaultLogHelper.getInstance();

   public boolean parse() {
      byte elementType = 0;
      int state = 0;
      if (getTracer().isOn()) {
         this.trace("Printer4689ParserData.parse >> ");
      }

      this.reset();
      PrinterParserElement parserElement = null;
      this.getParserLine().reset();
      this.rotateMode = this.escCmdProc.getParserState().getRotation(this.getStation()) != 0;
      this.dataTruncated = false;
      this.isActionCmd = false;
      this.printedChars = 0;

      while (state != 9) {
         switch (state) {
            case 0:
               if (getTracer().isOn()) {
                  this.trace("Printer4689Parser.INIT_STATE");
               }

               PrinterParserElement previousElem = parserElement;
               if ((parserElement = this.getNextElement()) != null) {
                  if (getTracer().isOn()) {
                     this.trace("parserElement = " + parserElement.getElementType());
                  }

                  this.isActionCmd = parserElement.isAction();
                  state = this.processInitState(parserElement);
               } else {
                  if (getTracer().isOn()) {
                     this.trace("getParserLine().isLineCompleted() = " + this.getParserLine().isLineCompleted());
                  }

                  if (getTracer().isOn()) {
                     this.trace("getParserLine().dataTruncated = " + this.getParserLine().isDataTruncated());
                  }

                  if (!this.getParserLine().isLineCompleted()) {
                     if (this.getParserLine().isDataTruncated()) {
                        if (getTracer().isOn()) {
                           this.trace("!getParserLine().isLineCompleted() && dataTruncated");
                        }

                        parserElement = previousElem;
                        state = 2;
                     } else {
                        state = 6;
                     }
                  } else {
                     if (getTracer().isOn()) {
                        this.trace("no more elements && line incomplete");
                     }

                     state = 6;
                  }
               }
               break;
            case 1:
               if (getTracer().isOn()) {
                  this.trace("Printer4689Parser.ELEM_RECEIVED_STATE");
               }

               state = this.doElemReceived(parserElement);
               break;
            case 2:
               if (getTracer().isOn()) {
                  this.trace("Printer4689Parser.LINE_COMPLETE_STATE");
               }

               state = this.doLineComplete(parserElement);
               break;
            case 3:
               if (getTracer().isOn()) {
                  this.trace("Printer4689Parser.TRUNCATE_VERIFICATION_STATE");
               }

               state = this.doTruncateVerification(parserElement);
               break;
            case 4:
               if (getTracer().isOn()) {
                  this.trace("Printer4689Parser.DATA_TRUNCATED_STATE");
               }

               parserElement.incIndex(this.printedChars);
               this.getParserLine().resetText();
               state = 1;
               break;
            case 5:
               if (getTracer().isOn()) {
                  this.trace("Printer4689Parser.ELEMENTS_REMAINIG_STATE");
               }

               state = this.doElementsRemainig(parserElement);
               break;
            case 6:
               if (getTracer().isOn()) {
                  this.trace("Printer4689Parser.END_OF_DATA_STATE");
               }

               if (this.dataRemaining) {
                  state = 5;
               } else {
                  state = 7;
               }
               break;
            case 7:
               if (getTracer().isOn()) {
                  this.trace("Printer4689Parser.CMD_CREATION_STATE");
               }

               state = this.doCmdCreation(parserElement);
               break;
            case 8:
               if (getTracer().isOn()) {
                  this.trace("Printer4689Parser.NON_TXT_ATTR_CMD_STATE");
               }

               int commandCode = ((PrinterParserEscCmd)parserElement).getEscCmd().getCode();
               if (commandCode == 6 || commandCode == 7) {
                  if (getTracer().isOn()) {
                     this.trace("data has logos");
                  }

                  this.dataHasLogos = true;
                  this.mainCmdBuffer.reset();
                  return false;
               }

               state = this.doNonTxtAttr(parserElement);
               break;
            case 9:
               if (getTracer().isOn()) {
                  this.trace("Printer4689Parser.END_STATE.  parsing ERROR, should not be reached");
               }
            default:
               logHelper.addLogEntry(1013, "Bad state received", "POSPrinter", 4);
         }
      }

      return true;
   }

   public List getLinesToRotate() {
      return this.linesToRotate;
   }

   public Printer4689ParserRotate getParserRotate() {
      if (this.parserRotate == null) {
         int sideWaysMaxChars = this.escCmdProc.getHandleState().getSidewaysLineWidth((byte)2);
         int sideWaysMaxLines = 14;
         this.parserRotate = new Printer4689ParserRotate(sideWaysMaxLines, sideWaysMaxChars);
      }

      return this.parserRotate;
   }

   public void setParserRotate(Printer4689ParserRotate parserRotate) {
      this.parserRotate = parserRotate;
   }

   public void reset() {
      super.reset();
      this.dataRemaining = false;
      this.getParserLine().reset();
      this.mainCmdBuffer.reset();
      this.dataHasLogos = false;
      this.appendLF = false;
      this.rotateMode = false;
   }

   public void setData(ByteBuffer data) throws IllegalArgumentException {
      super.setData(data);
      this.dataMasterCmd = this.escCmdProc.getFactory().createSelectStationCmd(this.getStation());
   }

   public POSPrinterCmd getMasterCmd() {
      POSPrinterCmd cmd = this.dataMasterCmd;
      this.dataMasterCmd = null;
      return cmd;
   }

   protected int processInitState(PrinterParserElement elem) {
      int elemType = elem.getElementType();
      switch (elemType) {
         case -1:
         case 0:
            logHelper.addLogEntry(1013, "Bad parser element received", "POSPrinter", 4);
            return 0;
         case 1:
         case 3:
         case 5:
            return 1;
         case 2:
            int commandCode = ((PrinterParserEscCmd)elem).getEscCmd().getCode();
            if (elem.isProperty()) {
               return 1;
            } else {
               if (elem.isAction()) {
                  return 8;
               }

               this.getParserLine().updateState((PrinterParserEscCmd)elem);
               return 0;
            }
         case 4:
            return 0;
         default:
            if (getTracer().isOn()) {
               this.trace("default. Unknown elemType = " + elemType);
            }

            return 0;
      }
   }

   protected void updateMasterBuffer(boolean addLF, boolean isActionCmd) {
      if (getTracer().isOn()) {
         this.trace("Printer4689ParserData: updateMasterBuffer >>");
      }

      if (this.isNewMaster() && !isActionCmd) {
         this.mainCmdBuffer.append(this.cmdBytes.MULTI_LINE_MODE);
         this.mainCmdBuffer.append(this.cmdBytes.LINE_ATTRB_NORMAL);
         this.mainCmdBuffer.append(this.cmdBytes.ROTATE_NORMAL);
         this.mainCmdBuffer.append(this.cmdBytes.CHAR_ATTRB_NORMAL);
         this.setNewMaster(false);
      }

      if (this.rotateMode) {
         if (getTracer().isOn()) {
            this.trace("text in rotate mode");
         }

         this.getParserRotate().add(this.getParserLine());
      } else {
         if (getTracer().isOn()) {
            this.trace("rotation is PTR_RP_NORMAL");
         }

         this.getParserLine().buildLineMasterCmd();
         if (getTracer().isOn()) {
            this.trace("addLF = " + addLF + "        appendLF = " + this.appendLF);
         }

         if (this.appendLF) {
            this.mainCmdBuffer.append(this.cmdBytes.FEED_LINE_4689);
            this.appendLF = false;
         }

         this.mainCmdBuffer.append(this.getParserLine().getLineMasterText().getBytes());
         if (addLF) {
            this.mainCmdBuffer.append(this.cmdBytes.FEED_LINE_4689);
         }
      }

      this.dataRemaining = false;
      if (getTracer().isOn()) {
         this.trace("mainCmdBuffer byteCount = " + this.mainCmdBuffer.getByteCount());
      }

      if (getTracer().isOn()) {
         this.trace("mainCmdBuffer = " + Util.toFormatedHexString(this.mainCmdBuffer.getBytes()));
      }

      if (getTracer().isOn()) {
         this.trace("Printer4689ParserData: updateMasterBuffer <<");
      }
   }

   protected void appendToDataMasterCmd(POSPrinterCmd cmd) {
      if (this.dataMasterCmd == null) {
         this.dataMasterCmd = cmd;
      } else {
         this.dataMasterCmd.appendPOSPrinterCmd(cmd);
      }
   }

   protected void trace(String s) {
      getTracer().println(3, s);
   }

   protected static Tracer getTracer() {
      return parserTracer;
   }

   protected int doElemReceived(PrinterParserElement parserElement) {
      int state = 1;
      this.dataRemaining = true;
      if (parserElement.getElementType() != 1 && !parserElement.isProperty()) {
         if (parserElement.getElementType() == 3) {
            state = 2;
         }
      } else {
         this.printedChars = this.getParserLine().appendElement(parserElement);
         if (parserElement.isLineFeedAtTheEnd()) {
            this.setAddLineFeedAtTheEnd(true);
         }

         if (!this.getParserLine().isLineCompleted() && !parserElement.isLineFeedAtTheEnd()) {
            state = 0;
         } else {
            state = 2;
         }
      }

      return state;
   }

   protected int doLineComplete(PrinterParserElement parserElement) {
      int state = 2;
      if (parserElement.getElementType() == 3) {
         for (int i = 0; i <= parserElement.getLength(); i++) {
            this.getParserLine().reset();
            if (!parserElement.isLineFeedAtTheEnd() && this.appendLF) {
               this.updateMasterBuffer(false, parserElement.isAction());
            } else {
               this.updateMasterBuffer(true, parserElement.isAction());
            }
         }
      } else if (!parserElement.isLineFeedAtTheEnd() && !this.getParserLine().isLineCompleted()) {
         this.updateMasterBuffer(true, parserElement.isAction());
      } else {
         this.updateMasterBuffer(false, parserElement.isAction());
         this.appendLF = true;
      }

      return 3;
   }

   protected int doTruncateVerification(PrinterParserElement parserElement) {
      int state = 3;
      this.dataTruncated = this.getParserLine().isDataTruncated();
      byte var3;
      if (this.dataTruncated) {
         var3 = 4;
      } else {
         if (parserElement != null && parserElement.isLineFeedAtTheEnd()) {
            this.getParserLine().resetText();
         } else {
            this.getParserLine().reset();
         }

         var3 = 0;
      }

      return var3;
   }

   protected int doElementsRemainig(PrinterParserElement parserElement) {
      int state = 5;
      boolean addLF = false;
      if (parserElement != null) {
         this.dataTruncated = parserElement.isDataTruncated();
      }

      if (!this.dataTruncated) {
         addLF = false;
      } else if (this.getParserLine().isLineCompleted()) {
         addLF = true;
      }

      if (getTracer().isOn()) {
         this.trace("doElementsRemainig addLF = " + addLF);
      }

      this.updateMasterBuffer(addLF, this.isActionCmd);
      return 3;
   }

   protected int doCmdCreation(PrinterParserElement parserElement) {
      int state = 7;
      if (this.rotateMode) {
         if (getTracer().isOn()) {
            this.trace("ROTATE mode active");
         }

         this.mainCmdBuffer = this.getParserRotate().getBuffer();
         if (getTracer().isOn()) {
            this.trace(" buffer obtained from rotate = " + Util.toFormatedHexString(this.mainCmdBuffer.getBytes()));
         }

         int var4 = 5;
      }

      if (this.mainCmdBuffer.getByteCount() > 0) {
         this.mainCmdBuffer.append(this.cmdBytes.END_OF_DATA);
         ByteBuffer buffer = new ByteBuffer(this.mainCmdBuffer.getBytes());
         this.appendToDataMasterCmd(this.escCmdProc.getFactory().createPrintNormalCmd(this.getStation(), buffer, 0, buffer.getByteCount(), (byte)0));
         this.mainCmdBuffer.reset();
      }

      byte var5;
      if (parserElement == null) {
         var5 = 9;
      } else if (parserElement.isAction()) {
         if (getTracer().isOn()) {
            this.trace("parserElement.isAction() ");
         }

         if (getTracer().isOn()) {
            this.trace("is LF ?" + (parserElement.getElementType() == 3));
         }

         if (getTracer().isOn()) {
            this.trace("parserElement.getPOSPrinterCmd() = " + parserElement.getPOSPrinterCmd());
         }

         if (!this.rotateMode) {
            this.appendToDataMasterCmd(parserElement.getPOSPrinterCmd());
         }

         var5 = 0;
      } else {
         if (getTracer().isOn()) {
            this.trace("parserElementType = " + parserElement.getElementType());
         }

         var5 = 9;
      }

      return var5;
   }

   protected int doNonTxtAttr(PrinterParserElement parserElement) {
      int state = 8;
      this.updateMasterBuffer(false, parserElement.isAction());
      this.getParserLine().resetText();
      return 7;
   }

   private Printer4689ParserLine getParserLine() {
      if (this.line == null) {
         this.line = new Printer4689ParserLine(this.escCmdProc);
         this.line.setStation(this.getStation());
      }

      return (Printer4689ParserLine)this.line;
   }
}
