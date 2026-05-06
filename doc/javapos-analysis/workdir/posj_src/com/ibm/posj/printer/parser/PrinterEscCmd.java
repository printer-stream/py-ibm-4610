package com.ibm.posj.printer.parser;

import com.ibm.posj.POSPrinterCmd;

public interface PrinterEscCmd {
   int ESC_CMD_CODE = 0;
   int ESC_CMD_CODE_PAPER_CUT = 1;
   int ESC_CMD_CODE_FEED_PAPER_CUT = 2;
   int ESC_CMD_CODE_FEED_PAPER_CUT_STAMP = 3;
   int ESC_CMD_CODE_FIRE_STAMP = 4;
   int ESC_CMD_CODE_PRINT_BITMAP = 5;
   int ESC_CMD_CODE_PRINT_TOP_LOGO = 6;
   int ESC_CMD_CODE_PRINT_BOTTOM_LOGO = 7;
   int ESC_CMD_CODE_FEED_LINES = 8;
   int ESC_CMD_CODE_FEED_UNITS = 9;
   int ESC_CMD_CODE_FEED_REVERSE = 10;
   int ESC_CMD_CODE_FONT_TYPE_FACE = 11;
   int ESC_CMD_CODE_BOLD = 12;
   int ESC_CMD_CODE_UNDERLINE = 13;
   int ESC_CMD_CODE_ITALIC = 14;
   int ESC_CMD_CODE_ALTERNATE_COLOR = 15;
   int ESC_CMD_CODE_REVERSE_VIDEO = 16;
   int ESC_CMD_CODE_SHADING = 17;
   int ESC_CMD_CODE_HIGH_WIDE_CONTROL = 18;
   int ESC_CMD_CODE_SCALE_HORIZONTALLY = 19;
   int ESC_CMD_CODE_SCALE_VERTICALLY = 20;
   int ESC_CMD_CODE_RGB_COLOR = 21;
   int ESC_CMD_CODE_SUBSCRIPT = 22;
   int ESC_CMD_CODE_SUPERSCRIPT = 23;
   int ESC_CMD_CODE_CENTER = 24;
   int ESC_CMD_CODE_RIGHT_JUSTIFY = 25;
   int ESC_CMD_CODE_NORMAL = 26;
   int ESC_CMD_CODE_PASS_THRU = 27;
   String ESC_CMD_NAME_PAPER_CUT = "Paper cut";
   String ESC_CMD_NAME_FEED_PAPER_CUT = "Feed and paper cut";
   String ESC_CMD_NAME_FEED_PAPER_CUT_STAMP = "Feed, paper cut and stamp";
   String ESC_CMD_NAME_FIRE_STAMP = "Fire stamp";
   String ESC_CMD_NAME_PRINT_BITMAP = "Print bitmap";
   String ESC_CMD_NAME_PRINT_TOP_LOGO = "Print top logo";
   String ESC_CMD_NAME_PRINT_BOTTOM_LOGO = "Print bottom logo";
   String ESC_CMD_NAME_FEED_LINES = "Feed lines";
   String ESC_CMD_NAME_FEED_UNITS = "Feed units";
   String ESC_CMD_NAME_FEED_REVERSE = "Fedd reverse";
   String ESC_CMD_NAME_FONT_TYPE_FACE = "Font type face";
   String ESC_CMD_NAME_BOLD = "Bold";
   String ESC_CMD_NAME_UNDERLINE = "Underline";
   String ESC_CMD_NAME_ITALIC = "Italic";
   String ESC_CMD_NAME_ALTERNATE_COLOR = "Alternate color";
   String ESC_CMD_NAME_REVERSE_VIDEO = "Reverse video";
   String ESC_CMD_NAME_SHADING = "Shading";
   String ESC_CMD_NAME_HIGH_WIDE_CONTROL = "High/wide control";
   String ESC_CMD_NAME_SCALE_HORIZONTALLY = "Scale horizontally";
   String ESC_CMD_NAME_SCALE_VERTICALLY = "Scale vertically";
   String ESC_CMD_NAME_RGB_COLOR = "RGB color";
   String ESC_CMD_NAME_SUBSCRIPT = "Subscript";
   String ESC_CMD_NAME_SUPERSCRIPT = "Superscript";
   String ESC_CMD_NAME_CENTER = "Center justify";
   String ESC_CMD_NAME_RIGHT_JUSTIFY = "Right justify";
   String ESC_CMD_NAME_NORMAL = "Normal";
   String ESC_CMD_NAME_PASS_THRU = "Passthru";
   byte PTR_PARSER_COMMAND_PROPERTY_REMEMBERED = 0;
   byte PTR_PARSER_COMMAND_PROPERTY_RESET = 1;
   byte PTR_PARSER_COMMAND_ACTION = 2;
   byte PTR_PARSER_COMMAND_ACTION_LF = 3;
   byte PTR_PARSER_COMMAND_ALIGNMENT = 4;
   byte PTR_PARSER_COMMAND_ACTION_LF_TEXT = 5;

   void accept(PrinterEscCmdVisitor var1) throws IllegalAccessException, IllegalArgumentException;

   int getCode();

   String getName();

   int getParameter();

   int getSecParameter();

   int getClosestParameter();

   boolean hasParameter();

   void validParameter(boolean var1);

   byte getStation();

   POSPrinterCmd getPOSPrinterCmd();

   void setPOSPrinterCmd(POSPrinterCmd var1);

   void setParameter(int var1);

   void setSecParameter(int var1);

   void setStation(byte var1);

   void setClosestParameter(int var1);

   void reset();

   byte getType();

   void setValidateOnly(boolean var1);

   boolean isValidateOnly();

   public interface AlternateColorEscCmd extends PrinterEscCmd {
   }

   public interface BoldEscCmd extends PrinterEscCmd {
   }

   public interface CenterEscCmd extends PrinterEscCmd {
   }

   public interface Factory {
      PrinterEscCmd.PaperCutEscCmd createPaperCutEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.FeedPaperCutEscCmd createFeedPaperCutEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.FeedPaperCutStampEscCmd createFeedPaperCutStampEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.FireStampEscCmd createFireStampEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.PrintBitmapEscCmd createPrintBitmapEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.PrintTopLogoEscCmd createPrintTopLogoEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.PrintBottomLogoEscCmd createPrintBottomLogoEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.FeedLinesEscCmd createFeedLinesEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.FeedUnitsEscCmd createFeedUnitsEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.FeedReverseEscCmd createFeedReverseEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.FontTypefaceEscCmd createFontTypefaceEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.BoldEscCmd createBoldEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.UnderlineEscCmd createUnderlineEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.ItalicEscCmd createItalicEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.AlternateColorEscCmd createAlternateColorEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.ReverseVideoEscCmd createReverseVideoEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.ShadingEscCmd createShadingEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.HighWideControlEscCmd createHighWideControlEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.ScaleHorizontallyEscCmd createScaleHorizontallyEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.ScaleVerticallyEscCmd createScaleVerticallyEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.RGBColorEscCmd createRGBColorEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.SubScriptEscCmd createSubScriptEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.SuperScriptEscCmd createSuperScriptEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.CenterEscCmd createCenterEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.RightJustifyEscCmd createRightJustifyEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.NormalEscCmd createNormalEscCmd(int var1, String var2, byte var3);

      PrinterEscCmd.PassThruEscCmd createPassThruEscCmd(int var1, String var2, byte var3);
   }

   public interface FeedLinesEscCmd extends PrinterEscCmd {
   }

   public interface FeedPaperCutEscCmd extends PrinterEscCmd {
   }

   public interface FeedPaperCutStampEscCmd extends PrinterEscCmd {
   }

   public interface FeedReverseEscCmd extends PrinterEscCmd {
   }

   public interface FeedUnitsEscCmd extends PrinterEscCmd {
   }

   public interface FireStampEscCmd extends PrinterEscCmd {
   }

   public interface FontTypefaceEscCmd extends PrinterEscCmd {
   }

   public interface HighWideControlEscCmd extends PrinterEscCmd {
   }

   public interface ItalicEscCmd extends PrinterEscCmd {
   }

   public interface NormalEscCmd extends PrinterEscCmd {
   }

   public interface PaperCutEscCmd extends PrinterEscCmd {
   }

   public interface PassThruEscCmd extends PrinterEscCmd {
   }

   public interface PrintBitmapEscCmd extends PrinterEscCmd {
   }

   public interface PrintBottomLogoEscCmd extends PrinterEscCmd {
   }

   public interface PrintTopLogoEscCmd extends PrinterEscCmd {
   }

   public interface RGBColorEscCmd extends PrinterEscCmd {
   }

   public interface ReverseVideoEscCmd extends PrinterEscCmd {
   }

   public interface RightJustifyEscCmd extends PrinterEscCmd {
   }

   public interface ScaleHorizontallyEscCmd extends PrinterEscCmd {
   }

   public interface ScaleVerticallyEscCmd extends PrinterEscCmd {
   }

   public interface ShadingEscCmd extends PrinterEscCmd {
   }

   public interface SubScriptEscCmd extends PrinterEscCmd {
   }

   public interface SuperScriptEscCmd extends PrinterEscCmd {
   }

   public interface UnderlineEscCmd extends PrinterEscCmd {
   }
}
