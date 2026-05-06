package com.ibm.posj.kbd;

import com.ibm.posj.POSKeyboardHandle;

class DefaultKbdMappingFactory implements KbdMappingFactory {
   public KbdMapping createPS2NANPOSKbdMapping(POSKeyboardHandle handle) throws KbdMappingFactoryException {
      return new PS2NANPOSKbdMapping(handle);
   }

   public KbdMapping createSioLegacyUsbPoskbdMapping() throws KbdMappingFactoryException {
      return new DefaultSIOUsbPOSKbdMapping();
   }
}
