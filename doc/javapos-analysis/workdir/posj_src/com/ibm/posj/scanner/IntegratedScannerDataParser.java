package com.ibm.posj.scanner;

import com.ibm.posj.event.ScannerDataEvent;

public interface IntegratedScannerDataParser {
   ScannerDataEvent parseData(byte[] var1);
}
