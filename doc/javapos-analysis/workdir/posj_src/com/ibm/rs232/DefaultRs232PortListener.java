package com.ibm.rs232;

import com.ibm.rs232.event.Rs232ControlLineEvent;
import com.ibm.rs232.event.Rs232DataEvent;
import com.ibm.rs232.event.Rs232ErrorEvent;
import com.ibm.rs232.event.Rs232OfflineEvent;
import com.ibm.rs232.event.Rs232OnlineEvent;

public class DefaultRs232PortListener implements Rs232Port.Listener {
   public void dataEventOccurred(Rs232DataEvent rs232DataEvent) {
   }

   public void errorEventOccurred(Rs232ErrorEvent rs232ErrorEvent) {
   }

   public void onlineEventOccurred(Rs232OnlineEvent rs232OEvent) {
   }

   public void offlineEventOccurred(Rs232OfflineEvent rs232OEvent) {
   }

   public void controlLineEventOccurred(Rs232ControlLineEvent rs232ControlLineEvent) {
   }
}
