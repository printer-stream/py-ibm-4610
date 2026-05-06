package com.ibm.posj.kbd;

import com.ibm.posj.HandleException;
import com.ibm.posj.POSKeyboardHandle;
import com.ibm.posj.SystemCmd;

public class MappingManager {
   private static MappingManager instance = null;
   private KbdMappingFactory factory = new DefaultKbdMappingFactory();

   protected MappingManager() {
   }

   public static synchronized MappingManager getInstance() {
      if (instance == null) {
         instance = new MappingManager();
      }

      return instance;
   }

   public KbdMapping createKbdMapping(POSKeyboardHandle handle) throws HandleException {
      int kbdType = this.getDeviceId(handle);
      KbdMapping kbdMapping = null;
      switch (kbdType) {
         case 3604:
         case 3605:
         case 3613:
         case 3614:
         case 3629:
         case 3630:
         case 3631:
         case 3632:
         case 3633:
            kbdMapping = this.factory.createPS2NANPOSKbdMapping(handle);
            break;
         case 3606:
         case 3607:
         case 3608:
         case 3609:
         case 3610:
         case 3611:
         case 3612:
         case 3615:
         case 3616:
         case 3617:
         case 3618:
         case 3619:
         case 3645:
         case 3646:
         default:
            throw new HandleException("Unable to create the KbdMapping object for devInfoCmd.getDeviceId() == " + kbdType);
         case 3620:
         case 3621:
         case 3622:
         case 3623:
         case 3624:
         case 3625:
         case 3626:
         case 3627:
         case 3628:
         case 3634:
         case 3635:
         case 3636:
         case 3637:
         case 3638:
         case 3639:
         case 3640:
         case 3641:
         case 3642:
         case 3643:
         case 3644:
         case 3647:
            kbdMapping = this.factory.createSioLegacyUsbPoskbdMapping();
      }

      return kbdMapping;
   }

   public POSKeyboardMap createUserMapping(POSKeyboardHandle handle, String fileName) throws HandleException {
      int kbdType = this.getDeviceId(handle);
      POSKeyboardMap userMap = null;
      switch (kbdType) {
         case 3604:
         case 3605:
         case 3613:
         case 3614:
         case 3629:
         case 3630:
         case 3631:
         case 3632:
         case 3633:
            userMap = new SystemPOSKeyboardMap(fileName);
            break;
         case 3606:
         case 3607:
         case 3608:
         case 3609:
         case 3610:
         case 3611:
         case 3612:
         case 3615:
         case 3616:
         case 3617:
         case 3618:
         case 3619:
         case 3645:
         case 3646:
         default:
            throw new HandleException("Unable to create the POSKeyboardMap object for devInfoCmd.getDeviceId() == " + kbdType);
         case 3620:
         case 3621:
         case 3622:
         case 3623:
         case 3624:
         case 3625:
         case 3626:
         case 3627:
         case 3628:
         case 3634:
         case 3635:
         case 3636:
         case 3637:
         case 3638:
         case 3639:
         case 3640:
         case 3641:
         case 3642:
         case 3643:
         case 3644:
         case 3647:
            userMap = new NonSystemPOSKeyboardMap(fileName);
      }

      return userMap;
   }

   protected int getDeviceId(POSKeyboardHandle handle) throws HandleException {
      SystemCmd.DeviceInfoRequestCmd devInfoCmd = handle.getSystemCmdFactory().createDeviceInfoRequestCmd();
      if (!handle.isInit()) {
         handle.init();
      }

      handle.submit(devInfoCmd);
      return devInfoCmd.getDeviceId();
   }
}
