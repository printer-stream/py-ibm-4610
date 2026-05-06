package com.ibm.posj;

public interface MICRCmd extends HandleCmd {
   int MICR_DI_ERROR = 0;
   int MICR_DI_FEED_ERROR = 1;

   void accept(MICRCmdVisitor var1) throws HandleException;

   public interface BeginInsertionCmd extends MICRCmd {
   }

   public interface BeginRemovalCmd extends MICRCmd {
   }

   public interface EndInsertionCmd extends MICRCmd {
   }

   public interface EndRemovalCmd extends MICRCmd {
   }

   public interface Factory extends HandleCmd.Factory {
      MICRCmd createBeginInsertionCmd();

      MICRCmd createEndInsertionCmd();

      MICRCmd createBeginRemovalCmd();

      MICRCmd createEndRemovalCmd();
   }
}
