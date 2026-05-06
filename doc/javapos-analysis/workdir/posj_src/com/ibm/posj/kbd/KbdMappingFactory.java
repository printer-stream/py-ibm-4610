package com.ibm.posj.kbd;

import com.ibm.posj.POSKeyboardHandle;

public interface KbdMappingFactory {
   KbdMapping createPS2NANPOSKbdMapping(POSKeyboardHandle var1) throws KbdMappingFactoryException;

   KbdMapping createSioLegacyUsbPoskbdMapping() throws KbdMappingFactoryException;
}
