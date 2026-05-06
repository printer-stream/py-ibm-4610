package com.ibm.posj;

public interface LineDisplayCmdVisitor {
   void visitClearDisplayCmd(LineDisplayCmd var1);

   void visitWriteDisplayCmd(LineDisplayCmd var1);

   void visitSetCharacterSetCmd(LineDisplayCmd var1);

   void visitWriteTriangleMarksCmd(LineDisplayCmd var1);

   void visitTestLDRequestCmd(LineDisplayCmd var1);

   void visitBrightnessCmd(LineDisplayCmd var1);

   void visitScreenModeCmd(LineDisplayCmd var1);

   void visitDisplayModeCmd(LineDisplayCmd var1);

   void visitCursorModeCmd(LineDisplayCmd var1);

   void visitUserDefinableCharacterCmd(LineDisplayCmd var1);

   void visitDisplayTextModeCmd(LineDisplayCmd var1);
}
