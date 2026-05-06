package com.ibm.posj;

import com.ibm.posj.bus.printer.cmds.PrintCmd;
import com.ibm.posj.bus.printer.cmds.PrintCmdFactory;
import com.ibm.posj.bus.printer.cmds.sureone.FontSureoneCmdFactory;
import com.ibm.posj.bus.printer.cmds.sureone.GenSureoneCmdFactory;
import com.ibm.posj.bus.printer.cmds.sureone.GrapSureoneCmdFactory;
import com.ibm.posj.printer.StatusVisitor;

public abstract class IBMSureonePrinterCmd extends DefaultPOSPrinterCmd {
   public IBMSureonePrinterCmd(HandleCmd.Factory factory, String name, byte station) {
      super(factory, name, station);
   }

   public IBMSureonePrinterCmd(HandleCmd.Factory factory, String name) {
      super(factory, name);
   }

   public static class BeeperCmd extends IBM4610PrinterCmd.BeeperCmd {
      BeeperCmd(HandleCmd.Factory factory, String name) {
         super(factory, name, true, false, 0, 0, 0);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitBeeperCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitBeeperCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitBeeperCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createBeeperCmd(this);
      }
   }

   public static class BoldCmd extends DefaultPOSPrinterCmd.BoldCmd {
      BoldCmd(HandleCmd.Factory factory, String name) {
         super(factory, name, true);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitBoldCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitBoldCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitBoldCmd(this);
      }
   }

   public static class CancelPrintCmd extends IBMSureonePrinterCmd {
      CancelPrintCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitCancelPrintCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitCancelPrintCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitCancelPrintCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createCancelPrintCmd(this);
      }
   }

   public static class CashDrawerPulseCmd extends IBMSureonePrinterCmd {
      private byte value;
      private byte delay;

      CashDrawerPulseCmd(HandleCmd.Factory factory, String name, byte ener, byte delay) {
         super(factory, name);
         this.setEnergizing(ener);
         this.setDelay(delay);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createCashDrawerPulseCmd(this);
      }

      protected void setEnergizing(byte b) {
         this.value = b;
      }

      public byte getEnergizing() {
         return this.value;
      }

      protected void setDelay(byte b) {
         this.delay = b;
      }

      public byte getDelay() {
         return this.delay;
      }
   }

   public static class DefineDownloadCharCmd extends IBMSureonePrinterCmd {
      private byte value;
      private byte[] data;

      DefineDownloadCharCmd(HandleCmd.Factory factory, String name, byte codec, byte[] data) {
         super(factory, name);
         this.setCodeC(codec);
         this.setData(data);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitDefineDownloadCharCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitDefineDownloadCharCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitDefineDownloadCharCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GrapSureoneCmdFactory)this.factory.getGraphicCmdFactory()).createDefineDownloadCharCmd(this);
      }

      protected void setCodeC(byte b) {
         this.value = b;
      }

      public byte getCodeC() {
         return this.value;
      }

      protected void setData(byte[] b) {
         this.data = b;
      }

      public byte[] getData() {
         return this.data;
      }
   }

   public static class DeleteDownloadCharCmd extends IBMSureonePrinterCmd {
      private byte value;

      DeleteDownloadCharCmd(HandleCmd.Factory factory, String name, byte codec) {
         super(factory, name);
         this.setCodeC(codec);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitDeleteDownloadCharCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitDeleteDownloadCharCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitDeleteDownloadCharCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GrapSureoneCmdFactory)this.factory.getGraphicCmdFactory()).createDeleteDownloadCharCmd(this);
      }

      protected void setCodeC(byte b) {
         this.value = b;
      }

      public byte getCodeC() {
         return this.value;
      }
   }

   public static class DisableDownloadCharCmd extends IBMSureonePrinterCmd {
      DisableDownloadCharCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitDisableDownloadCharCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitDisableDownloadCharCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitDisableDownloadCharCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GrapSureoneCmdFactory)this.factory.getGraphicCmdFactory()).createDisableDownloadCharCmd(this);
      }
   }

   public static class DotSpacingCmd extends DefaultPOSPrinterCmd.DotSpacingCmd {
      DotSpacingCmd(HandleCmd.Factory factory, String name, int dots) {
         super(factory, name, (byte)0, dots, false);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitDotSpacingCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitDotSpacingCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitDotSpacingCmd(this);
      }
   }

   public static class EnableDownloadCharCmd extends IBMSureonePrinterCmd {
      EnableDownloadCharCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitEnableDownloadCharCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitEnableDownloadCharCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitEnableDownloadCharCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GrapSureoneCmdFactory)this.factory.getGraphicCmdFactory()).createEnableDownloadCharCmd(this);
      }
   }

   public static class Factory extends DefaultPOSPrinterCmd.Factory {
      public Factory(PrintCmdFactory lowerFactory) {
         this.setPrintCmdFactory(lowerFactory);
      }

      public POSPrinterCmd createSelectIntCharSetCmd(byte[] value) {
         return new IBMSureonePrinterCmd.SelectIntCharSetCmd(this, "SelectIntCharSetCmd", value);
      }

      public POSPrinterCmd createSetXonOffCmd() {
         return new IBMSureonePrinterCmd.SetXonOffCmd(this, "SetXonOffCmd");
      }

      public POSPrinterCmd createUseUserDefinedCodePageCmd(byte value) {
         return new IBM4610PrinterCmd.UseUserDefinedCodePageCmd(this, "UseUserDefinedCodePageCmd", (byte)0, value);
      }

      public POSPrinterCmd createSelectIBMChar1Cmd() {
         return new IBMSureonePrinterCmd.SelectIBMChar1Cmd(this, "SelectIBMChar1Cmd");
      }

      public POSPrinterCmd createSelectIBMChar2Cmd() {
         return new IBMSureonePrinterCmd.SelectIBMChar2Cmd(this, "SelectIBMChar2Cmd");
      }

      public POSPrinterCmd createSelectNormalCharSpaceCmd() {
         return new IBMSureonePrinterCmd.SelectNormalCharSpaceCmd(this, "SelectNormalCharSpaceCmd");
      }

      public POSPrinterCmd createSelectMediumCharSpaceCmd() {
         return new IBMSureonePrinterCmd.SelectMediumCharSpaceCmd(this, "SelectMediumCharSpaceCmd");
      }

      public POSPrinterCmd createSelectWideCharSpaceCmd() {
         return new IBMSureonePrinterCmd.SelectWideCharSpaceCmd(this, "SelectWideCharSpaceCmd");
      }

      public POSPrinterCmd createSelectXtraWideCharSpaceCmd() {
         return new IBMSureonePrinterCmd.SelectXtraWideCharSpaceCmd(this, "SelectXtraWideCharSpaceCmd");
      }

      public POSPrinterCmd createDotSpacingCmd(int dots) {
         return new IBMSureonePrinterCmd.DotSpacingCmd(this, "DotSpacingCmd", dots);
      }

      public POSPrinterCmd createSelect2xCharWidthCmd() {
         return new IBMSureonePrinterCmd.Select2xCharWidthCmd(this, "Select2xCharWidthCmd");
      }

      public POSPrinterCmd createSetWideModeCmd(byte mode) {
         return new IBMSureonePrinterCmd.SetWideModeCmd(this, "SetWideModeCmd", mode);
      }

      public POSPrinterCmd createSelect2xCharHeightCmd() {
         return new IBMSureonePrinterCmd.Select2xCharHeightCmd(this, "Select2xCharHeightCmd");
      }

      public POSPrinterCmd createSetHighMode(byte mode) {
         return new IBMSureonePrinterCmd.SetHighModeCmd(this, "SetHighMode", mode);
      }

      public POSPrinterCmd createSelectBoldModeCmd() {
         return new IBMSureonePrinterCmd.SelectBoldModeCmd(this, "SelectBoldModeCmd");
      }

      public POSPrinterCmd createUnderlineCmd() {
         return new DefaultPOSPrinterCmd.UnderlineCmd(this, "UnderlineCmd", (short)0);
      }

      public POSPrinterCmd createOverlineCmd() {
         return new IBMSureonePrinterCmd.OverlineCmd(this, "OverlineCmd");
      }

      public POSPrinterCmd createSelectHighligthCmd() {
         return new IBMSureonePrinterCmd.SelectHighligthCmd(this, "SelectHighligthCmd");
      }

      public POSPrinterCmd createLineSpacingCmd() {
         return new DefaultPOSPrinterCmd.LineSpacingCmd(this, "LineSpacingCmd", (byte)0, (byte)0);
      }

      public POSPrinterCmd createSetLineSpaceCmd() {
         return new IBMSureonePrinterCmd.SetLineSpaceCmd(this, "SetLineSpaceCmd");
      }

      public POSPrinterCmd createFeedPaperCmd(short lines) {
         return new IBMSureonePrinterCmd.FeedLinesCmd(this, "FeedPaperCmd", lines);
      }

      public POSPrinterCmd createSetTightSpaceCmd() {
         return new IBMSureonePrinterCmd.SetTightSpaceCmd(this, "SetTightSpaceCmd");
      }

      public POSPrinterCmd createMicroFeedCmd(byte lines) {
         return new IBMSureonePrinterCmd.MicroFeedCmd(this, "MicroFeedCmd", lines);
      }

      public POSPrinterCmd createMicroBackFeedCmd(byte lines) {
         return new IBMSureonePrinterCmd.MicroBackFeedCmd(this, "MicroBackFeedCmd", lines);
      }

      public POSPrinterCmd create_8mmFeedCmd(byte lines) {
         return new IBMSureonePrinterCmd._8mmFeedCmd(this, "_8mmFeedCmd", lines);
      }

      public POSPrinterCmd createSetCrowdedSpaceCmd() {
         return new IBMSureonePrinterCmd.SetCrowdedSpaceCmd(this, "SetCrowdedSpaceCmd");
      }

      public POSPrinterCmd createFormFeedLengthCmd() {
         return new DefaultPOSPrinterCmd.FormFeedLengthCmd(this, "FormFeedLengthCmd", (byte)0, (byte)0);
      }

      public POSPrinterCmd createSetPageLengthLinesCmd(byte lines) {
         return new IBMSureonePrinterCmd.SetPageLengthLinesCmd(this, "SetPageLengthLinesCmd", lines);
      }

      public POSPrinterCmd createSetPageLengthInchesCmd(byte lines) {
         return new IBMSureonePrinterCmd.SetPageLengthInchesCmd(this, "SetPageLengthInchesCmd", lines);
      }

      public POSPrinterCmd createVerticalTabCmd() {
         return new IBMSureonePrinterCmd.VerticalTabCmd(this, "VerticalTabCmd");
      }

      public POSPrinterCmd createVerticalTabPosCmd(byte[] pos) {
         return new IBMSureonePrinterCmd.VerticalTabPosCmd(this, "VerticalTabPosCmd", pos);
      }

      public POSPrinterCmd createSetBottomMarginCmd(byte margin) {
         return new IBMSureonePrinterCmd.SetBottomMarginCmd(this, "SetBottomMarginCmd", margin);
      }

      public POSPrinterCmd createSetLeftMarginCmd(int margin) {
         return new DefaultPOSPrinterCmd.SetLeftMarginCmd(this, "SetLeftMarginCmd", (byte)0, margin);
      }

      public POSPrinterCmd createSetRightMarginCmd(byte margin) {
         return new IBMSureonePrinterCmd.SetRightMarginCmd(this, "SetRightMarginCmd", margin);
      }

      public POSPrinterCmd createHorizontalTabCmd() {
         return new IBMSureonePrinterCmd.HorizontalTabCmd(this, "HorizontalTabCmd");
      }

      public POSPrinterCmd createHorizontalTabPosCmd(byte[] pos) {
         return new IBMSureonePrinterCmd.HorizontalTabPosCmd(this, "HorizontalTabPosCmd", pos);
      }

      public POSPrinterCmd createNormalDensityCmd(byte density, byte[] data) {
         return new IBMSureonePrinterCmd.NormalDensityCmd(this, "NormalDensityCmd", density, data);
      }

      public POSPrinterCmd createHighDensityCmd(byte density1, byte density2, byte[] data) {
         return new IBMSureonePrinterCmd.HighDensityCmd(this, "HighDensityCmd", density1, density2, data);
      }

      public POSPrinterCmd createFineDensityBitCmd(byte density, byte[] data) {
         return new IBMSureonePrinterCmd.FineDensityBitCmd(this, "FineDensityBitCmd", density, data);
      }

      public POSPrinterCmd createFineDensityCmd(byte density1, byte density2, byte[] data) {
         return new IBMSureonePrinterCmd.FineDensityCmd(this, "FineDensityCmd", density1, density2, data);
      }

      public POSPrinterCmd createEnableDownloadCharCmd() {
         return new IBMSureonePrinterCmd.EnableDownloadCharCmd(this, "EnableDownloadCharCmd");
      }

      public POSPrinterCmd createDisableDownloadCharCmd() {
         return new IBMSureonePrinterCmd.DisableDownloadCharCmd(this, "DisableDownloadCharCmd");
      }

      public POSPrinterCmd createDefineDownloadCharCmd(byte code, byte[] data) {
         return new IBMSureonePrinterCmd.DefineDownloadCharCmd(this, "DefineDownloadCharCmd", code, data);
      }

      public POSPrinterCmd createDeleteDownloadCharCmd(byte code) {
         return new IBMSureonePrinterCmd.DeleteDownloadCharCmd(this, "DeleteDownloadCharCmd", code);
      }

      public POSPrinterCmd createCashDrawerPulseCmd(byte energize, byte delay) {
         return new IBMSureonePrinterCmd.CashDrawerPulseCmd(this, "CashDrawerPulseCmd", energize, delay);
      }

      public POSPrinterCmd createImmediateDriveCmd() {
         return new IBMSureonePrinterCmd.ImmediateDriveCmd(this, "ImmediateDriveCmd");
      }

      public POSPrinterCmd createBeeperCmd() {
         return new IBM4610PrinterCmd.BeeperCmd(this, "BeeperCmd", true, false, 0, 0, 0);
      }

      public POSPrinterCmd createCancelPrintCmd() {
         return new IBMSureonePrinterCmd.CancelPrintCmd(this, "CancelPrintCmd");
      }

      public POSPrinterCmd createReinitPrinterCmd() {
         return new IBM4610PrinterCmd.ReinitPrinterCmd(this, "ReinitPrinterCmd");
      }

      public POSPrinterCmd createStatusReqCmd() {
         return new IBMSureonePrinterCmd.StatusRequestCmd(this, "StatusRequestCmd");
      }

      public POSPrinterCmd createInitPrinterCmd() {
         return new IBMSureonePrinterCmd.InitPrinterCmd(this, "InitPrinterCmd");
      }

      public POSPrinterCmd createVerticalColAlignCmd() {
         return new IBMSureonePrinterCmd.VerticalColAlignCmd(this, "VerticalColAlignCmd");
      }

      public POSPrinterCmd createPrintDensityCmd() {
         return new IBMSureonePrinterCmd.PrintDensityCmd(this, "PrintDensityCmd");
      }
   }

   public static class FeedLinesCmd extends DefaultPOSPrinterCmd.FeedLinesCmd {
      FeedLinesCmd(HandleCmd.Factory factory, String name, short lines) {
         super(factory, name, lines);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitFeedLinesCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitFeedLinesCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitFeedLinesCmd(this);
      }
   }

   public static class FineDensityBitCmd extends IBMSureonePrinterCmd {
      private byte value;
      private byte[] data;

      FineDensityBitCmd(HandleCmd.Factory factory, String name, byte density, byte[] data) {
         super(factory, name);
         this.setDensity(density);
         this.setData(data);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitFineDensityBitCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitFineDensityBitCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitFineDensityBitCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GrapSureoneCmdFactory)this.factory.getGraphicCmdFactory()).createFineDensityBitCmd(this);
      }

      protected void setDensity(byte b) {
         this.value = b;
      }

      public byte getDensity() {
         return this.value;
      }

      protected void setData(byte[] b) {
         this.data = b;
      }

      public byte[] getData() {
         return this.data;
      }
   }

   public static class FineDensityCmd extends IBMSureonePrinterCmd {
      private byte value;
      private byte value2;
      private byte[] data;

      FineDensityCmd(HandleCmd.Factory factory, String name, byte density1, byte density2, byte[] data) {
         super(factory, name);
         this.setDensity1(density1);
         this.setDensity2(density2);
         this.setData(data);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitFineDensityCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitFineDensityCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitFineDensityCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GrapSureoneCmdFactory)this.factory.getGraphicCmdFactory()).createFineDensityCmd(this);
      }

      protected void setDensity1(byte b) {
         this.value = b;
      }

      public byte getDensity1() {
         return this.value;
      }

      protected void setDensity2(byte b) {
         this.value2 = b;
      }

      public byte getDensity2() {
         return this.value2;
      }

      protected void setData(byte[] b) {
         this.data = b;
      }

      public byte[] getData() {
         return this.data;
      }
   }

   public static class FormFeedLengthCmd extends DefaultPOSPrinterCmd.FormFeedLengthCmd {
      FormFeedLengthCmd(HandleCmd.Factory factory, String name) {
         super(factory, name, (byte)0, (byte)0);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitFormFeedLengthCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitFormFeedLengthCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitFormFeedLengthCmd(this);
      }
   }

   public static class HighDensityCmd extends IBMSureonePrinterCmd {
      private byte value;
      private byte value2;
      private byte[] data;

      HighDensityCmd(HandleCmd.Factory factory, String name, byte density1, byte density2, byte[] data) {
         super(factory, name);
         this.setDensity1(density1);
         this.setDensity2(density2);
         this.setData(data);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitHighDensityCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitHighDensityCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitHighDensityCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GrapSureoneCmdFactory)this.factory.getGraphicCmdFactory()).createHighDensityCmd(this);
      }

      protected void setDensity1(byte b) {
         this.value = b;
      }

      public byte getDensity1() {
         return this.value;
      }

      protected void setDensity2(byte b) {
         this.value2 = b;
      }

      public byte getDensity2() {
         return this.value2;
      }

      protected void setData(byte[] b) {
         this.data = b;
      }

      public byte[] getData() {
         return this.data;
      }
   }

   public static class HorizontalTabCmd extends IBMSureonePrinterCmd {
      HorizontalTabCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitHorizontalTabCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitHorizontalTabCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitHorizontalTabCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createHorizontalTabCmd(this);
      }
   }

   public static class HorizontalTabPosCmd extends IBMSureonePrinterCmd {
      private byte[] value;

      HorizontalTabPosCmd(HandleCmd.Factory factory, String name, byte[] pos) {
         super(factory, name);
         this.setPosition(pos);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitHorizontalTabPosCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitHorizontalTabPosCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitHorizontalTabPosCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createHorizontalTabPosCmd(this);
      }

      protected void setPosition(byte[] b) {
         this.value = b;
      }

      public byte[] getPosition() {
         return this.value;
      }
   }

   public static class ImmediateDriveCmd extends IBMSureonePrinterCmd {
      ImmediateDriveCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitImmediateDriveCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitImmediateDriveCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitImmediateDriveCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createImmediateDriveCmd(this);
      }
   }

   public static class InitPrinterCmd extends IBMSureonePrinterCmd {
      InitPrinterCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitInitPrinterCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitInitPrinterCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitInitPrinterCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createInitPrinterCmd(this);
      }
   }

   public static class LineSpacingCmd extends DefaultPOSPrinterCmd.LineSpacingCmd {
      LineSpacingCmd(HandleCmd.Factory factory, String name) {
         super(factory, name, (byte)0, (byte)0);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitLineSpacingCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitLineSpacingCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitLineSpacingCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createLineSpacingCmd(this);
      }
   }

   public static class MicroBackFeedCmd extends IBMSureonePrinterCmd {
      private byte lines;

      MicroBackFeedCmd(HandleCmd.Factory factory, String name, byte lines) {
         super(factory, name);
         this.setLines(lines);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitMicroBackFeedCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitMicroBackFeedCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitMicroBackFeedCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createMicroBackFeedCmd(this);
      }

      protected void setLines(byte b) {
         this.lines = b;
      }

      public byte getLines() {
         return this.lines;
      }
   }

   public static class MicroFeedCmd extends IBMSureonePrinterCmd {
      private byte lines;

      MicroFeedCmd(HandleCmd.Factory factory, String name, byte lines) {
         super(factory, name);
         this.setLines(lines);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitMicroFeedCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitMicroFeedCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitMicroFeedCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createMicroFeedCmd(this);
      }

      protected void setLines(byte b) {
         this.lines = b;
      }

      public byte getLines() {
         return this.lines;
      }
   }

   public static class NormalDensityCmd extends IBMSureonePrinterCmd {
      private byte value;
      private byte[] data;

      NormalDensityCmd(HandleCmd.Factory factory, String name, byte density, byte[] data) {
         super(factory, name);
         this.setDensity(density);
         this.setData(data);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitNormalDensityCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitNormalDensityCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitNormalDensityCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GrapSureoneCmdFactory)this.factory.getGraphicCmdFactory()).createNormalDensityCmd(this);
      }

      protected void setDensity(byte b) {
         this.value = b;
      }

      public byte getDensity() {
         return this.value;
      }

      protected void setData(byte[] b) {
         this.data = b;
      }

      public byte[] getData() {
         return this.data;
      }
   }

   public static class OverlineCmd extends IBMSureonePrinterCmd {
      OverlineCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitOverlineCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitOverlineCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitOverlineCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createOverlineCmd(this);
      }
   }

   public static class PrintDensityCmd extends IBMSureonePrinterCmd {
      PrintDensityCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitPrintDensityCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitPrintDensityCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitPrintDensityCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createPrintDensityCmd(this);
      }
   }

   public static class ReinitPrinterCmd extends IBM4610PrinterCmd.ReinitPrinterCmd {
      ReinitPrinterCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitReinitPrinterCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitReinitPrinterCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitReinitPrinterCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createReinitPrinterCmd(this);
      }
   }

   public static class Select2xCharHeightCmd extends IBMSureonePrinterCmd {
      Select2xCharHeightCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSelect2xCharHeightCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelect2xCharHeightCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelect2xCharHeightCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSelect2xCharHeightCmd(this);
      }
   }

   public static class Select2xCharWidthCmd extends IBMSureonePrinterCmd {
      Select2xCharWidthCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSelect2xCharWidthCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelect2xCharWidthCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelect2xCharWidthCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSelect2xCharWidthCmd(this);
      }
   }

   public static class SelectBoldModeCmd extends IBMSureonePrinterCmd {
      SelectBoldModeCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSelectBoldModeCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectBoldModeCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectBoldModeCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSelectBoldModeCmd(this);
      }
   }

   public static class SelectHighligthCmd extends IBMSureonePrinterCmd {
      SelectHighligthCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSelectHighligthCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectHighligthCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectHighligthCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSelectHighligthCmd(this);
      }
   }

   public static class SelectIBMChar1Cmd extends IBMSureonePrinterCmd {
      SelectIBMChar1Cmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSelectIBMChar1Cmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectIBMChar1Cmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectIBMChar1Cmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSelectIBMChar1Cmd(this);
      }
   }

   public static class SelectIBMChar2Cmd extends IBMSureonePrinterCmd {
      SelectIBMChar2Cmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSelectIBMChar2Cmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectIBMChar2Cmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectIBMChar2Cmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSelectIBMChar2Cmd(this);
      }
   }

   public static class SelectIntCharSetCmd extends IBMSureonePrinterCmd {
      private byte[] value;

      SelectIntCharSetCmd(HandleCmd.Factory factory, String name, byte[] value) {
         super(factory, name);
         this.setChar(value);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSelectIntCharSetCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectIntCharSetCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectIntCharSetCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSelectIntCharSetCmd(this);
      }

      protected void setChar(byte[] b) {
         this.value = b;
      }

      public byte[] getChar() {
         return this.value;
      }
   }

   public static class SelectMediumCharSpaceCmd extends IBMSureonePrinterCmd {
      SelectMediumCharSpaceCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSelectMediumCharSpaceCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectMediumCharSpaceCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectMediumCharSpaceCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSelectMediumCharSpaceCmd(this);
      }
   }

   public static class SelectNormalCharSpaceCmd extends IBMSureonePrinterCmd {
      SelectNormalCharSpaceCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSelectNormalCharSpaceCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectNormalCharSpaceCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectNormalCharSpaceCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSelectNormalCharSpaceCmd(this);
      }
   }

   public static class SelectWideCharSpaceCmd extends IBMSureonePrinterCmd {
      SelectWideCharSpaceCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSelectWideCharSpaceCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectWideCharSpaceCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectWideCharSpaceCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSelectWideCharSpaceCmd(this);
      }
   }

   public static class SelectXtraWideCharSpaceCmd extends IBMSureonePrinterCmd {
      SelectXtraWideCharSpaceCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSelectXtraWideCharSpaceCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectXtraWideCharSpaceCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSelectXtraWideCharSpaceCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSelectXtraWideCharSpaceCmd(this);
      }
   }

   public static class SetBottomMarginCmd extends IBMSureonePrinterCmd {
      private byte value;

      SetBottomMarginCmd(HandleCmd.Factory factory, String name, byte margin) {
         super(factory, name);
         this.setMargin(margin);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSetBottomMarginCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetBottomMarginCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetBottomMarginCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createSetBottomMarginCmd(this);
      }

      protected void setMargin(byte b) {
         this.value = b;
      }

      public byte getMargin() {
         return this.value;
      }
   }

   public static class SetCrowdedSpaceCmd extends IBMSureonePrinterCmd {
      SetCrowdedSpaceCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSetCrowdedSpaceCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetCrowdedSpaceCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetCrowdedSpaceCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSetCrowdedSpaceCmd(this);
      }
   }

   public static class SetHighModeCmd extends IBMSureonePrinterCmd {
      private byte value;

      SetHighModeCmd(HandleCmd.Factory factory, String name, byte mode) {
         super(factory, name);
         this.setCharCode(mode);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSetHighModeCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetHighModeCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetHighModeCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSetHighModeCmd(this);
      }

      protected void setCharCode(byte b) {
         this.value = b;
      }

      public byte getCharCode() {
         return this.value;
      }
   }

   public static class SetLeftMarginCmd extends DefaultPOSPrinterCmd.SetLeftMarginCmd {
      SetLeftMarginCmd(HandleCmd.Factory factory, String name, int value) {
         super(factory, name, (byte)0, value);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSetLeftMarginCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetLeftMarginCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetLeftMarginCmd(this);
      }
   }

   public static class SetLineSpaceCmd extends IBMSureonePrinterCmd {
      SetLineSpaceCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSetLineSpaceCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetLineSpaceCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetLineSpaceCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSetLineSpaceCmd(this);
      }
   }

   public static class SetPageLengthInchesCmd extends IBMSureonePrinterCmd {
      private byte value;

      SetPageLengthInchesCmd(HandleCmd.Factory factory, String name, byte inches) {
         super(factory, name);
         this.setInches(inches);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSetPageLengthInchesCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetPageLengthInchesCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetPageLengthInchesCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createSetPageLengthInchesCmd(this);
      }

      protected void setInches(byte b) {
         this.value = b;
      }

      public byte getInches() {
         return this.value;
      }
   }

   public static class SetPageLengthLinesCmd extends IBMSureonePrinterCmd {
      private byte value;

      SetPageLengthLinesCmd(HandleCmd.Factory factory, String name, byte lines) {
         super(factory, name);
         this.setLines(lines);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSetPageLengthLinesCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetPageLengthLinesCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetPageLengthLinesCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createSetPageLengthLinesCmd(this);
      }

      protected void setLines(byte b) {
         this.value = b;
      }

      public byte getLines() {
         return this.value;
      }
   }

   public static class SetRightMarginCmd extends IBMSureonePrinterCmd {
      private byte value;

      SetRightMarginCmd(HandleCmd.Factory factory, String name, byte margin) {
         super(factory, name);
         this.setMargin(margin);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSetRightMarginCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetRightMarginCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetRightMarginCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSetRightMarginCmd(this);
      }

      protected void setMargin(byte b) {
         this.value = b;
      }

      public byte getMargin() {
         return this.value;
      }
   }

   public static class SetTightSpaceCmd extends IBMSureonePrinterCmd {
      SetTightSpaceCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSetTightSpaceCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetTightSpaceCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetTightSpaceCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSetTightSpaceCmd(this);
      }
   }

   public static class SetWideModeCmd extends IBMSureonePrinterCmd {
      private byte value;

      SetWideModeCmd(HandleCmd.Factory factory, String name, byte value) {
         super(factory, name);
         this.setCharCode(value);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSetWideModeCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetWideModeCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetWideModeCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).createSetWideModeCmd(this);
      }

      protected void setCharCode(byte b) {
         this.value = b;
      }

      public byte getCharCode() {
         return this.value;
      }
   }

   public static class SetXonOffCmd extends IBMSureonePrinterCmd {
      SetXonOffCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitSetXonOffCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetXonOffCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitSetXonOffCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createSetXonOffCmd(this);
      }
   }

   public static class StatusRequestCmd extends GeneralPOSPrinterCmd.StatusRequestCmd implements POSPrinterCmd.StatusRequestCmd {
      StatusRequestCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitStatusRequestCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitStatusRequestCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitStatusRequestCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createStatusRequestCmd(this);
      }
   }

   public static class UnderlineCmd extends DefaultPOSPrinterCmd.UnderlineCmd {
      UnderlineCmd(HandleCmd.Factory factory, String name) {
         super(factory, name, (short)0);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitUnderlineCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitUnderlineCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitUnderlineCmd(this);
      }
   }

   public static class VerticalColAlignCmd extends IBMSureonePrinterCmd {
      VerticalColAlignCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitVerticalColAlignCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitVerticalColAlignCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitVerticalColAlignCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createVerticalColAlignCmd(this);
      }
   }

   public static class VerticalTabCmd extends IBMSureonePrinterCmd {
      VerticalTabCmd(HandleCmd.Factory factory, String name) {
         super(factory, name);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitVerticalTabCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitVerticalTabCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitVerticalTabCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createVerticalTabCmd(this);
      }
   }

   public static class VerticalTabPosCmd extends IBMSureonePrinterCmd {
      private byte[] value;

      VerticalTabPosCmd(HandleCmd.Factory factory, String name, byte[] pos) {
         super(factory, name);
         this.setPosition(pos);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visitVerticalTabPosCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitVerticalTabPosCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visitVerticalTabPosCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((GenSureoneCmdFactory)this.factory.getGeneralCmdFactory()).createVerticalTabPosCmd(this);
      }

      protected void setPosition(byte[] b) {
         this.value = b;
      }

      public byte[] getPosition() {
         return this.value;
      }
   }

   public static class _8mmFeedCmd extends IBMSureonePrinterCmd {
      private byte lines;

      _8mmFeedCmd(HandleCmd.Factory factory, String name, byte lines) {
         super(factory, name);
         this.setLines(lines);
      }

      public void accept(IBMSureonePrinterCmdVisitor visitor) {
         visitor.visit_8mmFeedCmd(this);
      }

      public void accept(POSPrinterCmdVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visit_8mmFeedCmd(this);
      }

      public void accept(StatusVisitor visitor) {
         ((IBMSureonePrinterCmdVisitor)visitor).visit_8mmFeedCmd(this);
      }

      protected PrintCmd buildPrintCmd() {
         return ((FontSureoneCmdFactory)this.factory.getFontCmdFactory()).create_8mmFeedCmd(this);
      }

      protected void setLines(byte b) {
         this.lines = b;
      }

      public byte getLines() {
         return this.lines;
      }
   }
}
