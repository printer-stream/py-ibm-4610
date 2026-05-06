package com.ibm.posj.printer.parser;

import com.ibm.posj.HandleException;
import com.ibm.posj.POSPrinterCmd;

public interface PrinterParser {
   String SPACE = " ";
   String CAP_PAPER_CUT = "Paper cut";
   String CAP_STAMP = "Fire stamp";
   String CAP_BITMAP = "Print bitmap";
   String CAP_FEED_REVERSE = "Feed reverse";
   String CAP_BOLD = "Bold";
   String CAP_UNDERLINE = "Underline";
   String CAP_ITALIC = "Italic";
   String CAP_2COLOR = "2 Color";
   String CAP_REVERSE_VIDEO = "Reverse video";
   String CAP_SHADING = "Shading";
   String CAP_DHIGH = "Double high";
   String CAP_DWIDE = "Double wide";
   String CAP_DHIGH_DWIDE = "Double high/Double wide";
   String CAP_HIGH_WIDE = "high/wide";
   String CAP_SCALE_H = "Scale horizontally";
   String CAP_SCALE_V = "Scale vertically";
   String CAP_SUBSCRIPT = "SubScript";
   String CAP_SUPERSCRIPT = "SuperScript";
   String CAP_FEED = "Feed";
   String CAP_FEED_UNITS = "Feed units";
   String CAP_FONT_TYPE = "Font type face";
   String CAP_RGB_COLOR = "RGB Color";
   String CAP_TOP_LOGO = "Top Logo";
   String CAP_NOT_SUPPORTED_ERROR = "capability not supported";
   String PAR_NOT_SUPPORTED_ERROR = "parameter not precisely supported.";
   String CAP_PAPER_CUT_NOT_SUPPORTED = "Paper cut capability not supported";
   String CAP_STAMP_NOT_SUPPORTED = "Fire stamp capability not supported";
   String CAP_BITMAP_NOT_SUPPORTED = "Print bitmap capability not supported";
   String CAP_BOLD_NOT_SUPPORTED = "Bold capability not supported";
   String CAP_UNDERLINE_NOT_SUPPORTED = "Underline capability not supported";
   String CAP_ITALIC_NOT_SUPPORTED = "Italic capability not supported";
   String CAP_2COLOR_NOT_SUPPORTED = "2 Color capability not supported";
   String CAP_REVERSE_VIDEO_NOT_SUPPORTED = "Reverse video capability not supported";
   String CAP_SHADING_NOT_SUPPORTED = "Shading capability not supported";
   String CAP_DHIGH_NOT_SUPPORTED = "Double high capability not supported";
   String CAP_DWIDE_NOT_SUPPORTED = "Double wide capability not supported";
   String CAP_DHIGH_DWIDE_NOT_SUPPORTED = "Double high/Double wide capability not supported";
   String CAP_HIGH_WIDE_NOT_SUPPORTED = "high/wide capability not supported";
   String CAP_SCALE_H_NOT_SUPPORTED = "Scale horizontally capability not supported";
   String CAP_SCALE_V_NOT_SUPPORTED = "Scale vertically capability not supported";
   String CAP_SUBSCRIPT_NOT_SUPPORTED = "SubScript capability not supported";
   String CAP_SUPERSCRIPT_NOT_SUPPORTED = "SuperScript capability not supported";
   String CAP_FEED_NOT_SUPPORTED = "Feed capability not supported";
   String CAP_FEED_UNITS_NOT_SUPPORTED = "Feed units capability not supported";
   String CAP_FEED_REVERSE_NOT_SUPPORTED = "Feed reverse capability not supported";
   String CAP_FONT_TYPE_NOT_SUPPORTED = "Font type face capability not supported";
   String CAP_RGB_COLOR_NOT_SUPPORTED = "RGB Color capability not supported";
   String PAR_TOP_LOGO_NOT_SUPPORTED = "Top Logo parameter not precisely supported.";
   String PAR_PAPER_CUT_NOT_SUPPORTED = "Paper cut parameter not precisely supported.";
   String PAR_STAMP_NOT_SUPPORTED = "Fire stamp parameter not precisely supported.";
   String PAR_BITMAP_NOT_SUPPORTED = "Print bitmap parameter not precisely supported.";
   String PAR_BOLD_NOT_SUPPORTED = "Bold parameter not precisely supported.";
   String PAR_UNDERLINE_NOT_SUPPORTED = "Underline parameter not precisely supported.";
   String PAR_ITALIC_NOT_SUPPORTED = "Italic parameter not precisely supported.";
   String PAR_2COLOR_NOT_SUPPORTED = "2 Color parameter not precisely supported.";
   String PAR_REVERSE_VIDEO_NOT_SUPPORTED = "Reverse video parameter not precisely supported.";
   String PAR_SHADING_NOT_SUPPORTED = "Shading parameter not precisely supported.";
   String PAR_DHIGH_NOT_SUPPORTED = "Double high parameter not precisely supported.";
   String PAR_DWIDE_NOT_SUPPORTED = "Double wide parameter not precisely supported.";
   String PAR_DHIGH_DWIDE_NOT_SUPPORTED = "Double high/Double wide parameter not precisely supported.";
   String PAR_SCALE_H_NOT_SUPPORTED = "Scale horizontally parameter not precisely supported.";
   String PAR_SCALE_V_NOT_SUPPORTED = "Scale vertically parameter not precisely supported.";
   String PAR_SUBSCRIPT_NOT_SUPPORTED = "SubScript parameter not precisely supported.";
   String PAR_FEED_NOT_SUPPORTED = "Feed parameter not precisely supported.";
   String PAR_FEED_UNITS_NOT_SUPPORTED = "Feed units parameter not precisely supported.";
   String PAR_FEED_REVERSE_NOT_SUPPORTED = "Feed reverse parameter not precisely supported.";
   String PAR_FONT_TYPE_NOT_SUPPORTED = "Font type face parameter not precisely supported.";
   String PAR_RGB_COLOR_NOT_SUPPORTED = "RGB Color parameter not precisely supported.";
   int PAR_CONVERSION_UNITS = 2540;

   boolean validateData(byte var1, String var2) throws IllegalAccessException, IllegalArgumentException;

   boolean validateData(byte var1, String var2, boolean var3) throws IllegalAccessException, IllegalArgumentException;

   boolean parseData(byte var1, String var2) throws HandleException;

   boolean parseData(byte var1, String var2, boolean var3) throws HandleException;

   POSPrinterCmd getMasterPrinterCmd();

   void reset();

   PrinterParserState getParserState();
}
