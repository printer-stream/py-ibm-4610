package com.ibm.posj.bus.printer.cmds.sureone;

import com.ibm.posj.bus.printer.cmds.AbstractByteCmd;
import com.ibm.posj.bus.printer.cmds.DefaultCmdList;
import com.ibm.posj.bus.printer.cmds.FontCmdFactory;
import com.ibm.posj.bus.printer.cmds.GeneralCmdFactory;
import com.ibm.posj.bus.printer.cmds.GraphicCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;
import com.ibm.posj.bus.printer.cmds.PrintCmdList;

public class PrintSureoneCmdFactory extends PrintCmdFactory {
   private GenSureoneCmdFactory general;
   private GraphicCmdFactory graphic;
   private FontCmdFactory font;
   private AbstractByteCmd cmdBytes = null;
   public static final int INITIAL_CMDCNT = 10;

   public PrintSureoneCmdFactory(int maxSize, AbstractByteCmd abCmd) {
      super(10, maxSize);
      this.general = new GenSureoneCmdFactory(this);
      this.graphic = new GrapSureoneCmdFactory(this);
      this.font = new FontSureoneCmdFactory(this);
      this.cmdBytes = abCmd;
   }

   public void setCmdsStation(PrintCmd ppiCmd, byte station) {
      switch (station) {
         case 2:
            ppiCmd.appendCmd(this.getCmdBytes().CR_COMM);
         case 8:
         case 32:
      }
   }

   public AbstractByteCmd getCmdBytes() {
      if (null == this.cmdBytes) {
         this.cmdBytes = new CmdSureone();
      }

      return this.cmdBytes;
   }

   public PrintCmdList createPrintCmdList() {
      return DefaultCmdList.getFactory().createPrintCmdList(this.getMaxCmdLen());
   }

   public GeneralCmdFactory getGeneralFactory() {
      return this.general;
   }

   public GraphicCmdFactory getGraphicFactory() {
      return this.graphic;
   }

   public FontCmdFactory getFontFactory() {
      return this.font;
   }
}
