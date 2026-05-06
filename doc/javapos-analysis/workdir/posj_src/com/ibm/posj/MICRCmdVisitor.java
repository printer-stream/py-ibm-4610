package com.ibm.posj;

public interface MICRCmdVisitor {
   void visitBeginInsertionCmd(MICRCmd var1) throws HandleException;

   void visitEndInsertionCmd(MICRCmd var1) throws HandleException;

   void visitBeginRemovalCmd(MICRCmd var1) throws HandleException;

   void visitEndRemovalCmd(MICRCmd var1);
}
