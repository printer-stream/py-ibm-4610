package com.ibm.posj.bus.rs232;

import com.ibm.jutil.Util;
import com.ibm.posj.HandleException;
import com.ibm.rs232.event.Rs232ControlLineEvent;
import java.util.ArrayList;
import java.util.List;

public class Rs232IntegratedOmniScannerStateManager {
   private Rs232IntegratedOmniScannerStateManager.Idle idleState = null;
   private Rs232IntegratedOmniScannerStateManager.ReceivingMessage receivingMessageState = null;
   private Rs232IntegratedOmniScannerStateManager.WaitingForScannerReadyForAckReception waitingForScannerReadyForAckReceptionState = null;
   private Rs232IntegratedOmniScannerStateManager.WaitingForScannerEndOfAckReception waitingForScannerEndOfAckReceptionState = null;
   private Rs232IntegratedOmniScannerStateManager.WaitingForScannerReadyForCmdReception waitingForScannerReadyForCmdReceptionState = null;
   private Rs232IntegratedOmniScannerStateManager.WaitingForScannerEndOfCmdReception waitingForScannerEndOfCmdReceptionState = null;
   private Rs232IntegratedOmniScannerStateManager.WaitingForCmdResponse waitingForCmdResponseState = null;
   private Rs232IntegratedOmniScannerStateManager.CommunicationError communicationErrorState = null;
   private Rs232IntegratedOmniScannerState currentState = null;

   public Rs232IntegratedOmniScannerStateManager(Rs232IntegratedOmniScannerProtocol protocol) {
      this.idleState = new Rs232IntegratedOmniScannerStateManager.Idle(protocol);
      this.receivingMessageState = new Rs232IntegratedOmniScannerStateManager.ReceivingMessage(protocol);
      this.waitingForScannerReadyForAckReceptionState = new Rs232IntegratedOmniScannerStateManager.WaitingForScannerReadyForAckReception(protocol);
      this.waitingForScannerEndOfAckReceptionState = new Rs232IntegratedOmniScannerStateManager.WaitingForScannerEndOfAckReception(protocol);
      this.waitingForScannerReadyForCmdReceptionState = new Rs232IntegratedOmniScannerStateManager.WaitingForScannerReadyForCmdReception(protocol);
      this.waitingForScannerEndOfCmdReceptionState = new Rs232IntegratedOmniScannerStateManager.WaitingForScannerEndOfCmdReception(protocol);
      this.waitingForCmdResponseState = new Rs232IntegratedOmniScannerStateManager.WaitingForCmdResponse(protocol);
      this.communicationErrorState = new Rs232IntegratedOmniScannerStateManager.CommunicationError(protocol);
      this.setIdleState();
   }

   private void setCurrentState(Rs232IntegratedOmniScannerState state) {
      this.currentState = state;
      this.currentState.set();
   }

   public synchronized Rs232IntegratedOmniScannerState getCurrentState() {
      return this.currentState;
   }

   public synchronized void setIdleState() {
      this.setCurrentState(this.idleState);
   }

   public synchronized void setReceivingMessageState() {
      this.setCurrentState(this.receivingMessageState);
   }

   public synchronized void setWaitingForScannerReadyForAckReceptionState() {
      this.setCurrentState(this.waitingForScannerReadyForAckReceptionState);
   }

   public synchronized void setWaitingForScannerEndOfAckReceptionState() {
      this.setCurrentState(this.waitingForScannerEndOfAckReceptionState);
   }

   public synchronized void setWaitingForScannerReadyForCmdReceptionState() {
      this.setCurrentState(this.waitingForScannerReadyForCmdReceptionState);
   }

   public synchronized void setWaitingForScannerEndOfCmdReceptionState() {
      this.setCurrentState(this.waitingForScannerEndOfCmdReceptionState);
   }

   public synchronized void setWaitingForCmdResponseState() {
      this.setCurrentState(this.waitingForCmdResponseState);
   }

   public synchronized void setCommunicationErrorState() {
      this.setCurrentState(this.communicationErrorState);
   }

   private class CommunicationError extends Rs232IntegratedOmniScannerState {
      public CommunicationError(Rs232IntegratedOmniScannerProtocol protocol) {
         super(protocol);
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println(3, "-->Communication Error state<--");
         }

         if (this.protocol.isRecoveringEnabled()) {
            if (this.protocol.isRecovering()) {
               Rs232IntegratedOmniScannerStateManager.this.setIdleState();
            } else {
               this.protocol.startRecovering();
            }
         }
      }

      public synchronized int write(byte[] data) {
         return 3;
      }

      public synchronized int packetArrived() {
         return 1;
      }

      public void controlLineEventOccurred(Rs232ControlLineEvent event) {
      }

      public void timerExpired() {
      }
   }

   private class Idle extends Rs232IntegratedOmniScannerState {
      public Idle(Rs232IntegratedOmniScannerProtocol protocol) {
         super(protocol);
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println(3, "-->Idle state<--");
         }

         if (this.protocol.isTransmissionPending()) {
            if (this.tracer.isOn()) {
               this.tracer.println(3, "Starting pending transmission.");
            }

            this.protocol.setTransmissionPending(false);
            Rs232IntegratedOmniScannerStateManager.this.setWaitingForScannerReadyForCmdReceptionState();
         }
      }

      public synchronized int write(byte[] data) {
         this.protocol.setLastPacketWritten(data);
         this.protocol.setFirstPacketSendAttempt();
         Rs232IntegratedOmniScannerStateManager.this.setWaitingForScannerReadyForCmdReceptionState();
         return 1;
      }

      public synchronized int packetArrived() {
         this.protocol.setLastPacketAccepted(this.protocol.getLastPacketReceived());
         if (this.tracer.isOn()) {
            this.tracer.println(3, "Packet arrived to Idle State: " + Util.toFormatedHexString(this.protocol.getLastPacketAccepted()));
         }

         if (Rs232IntegratedOmniScannerPacketManager.getInstance().getOpcode(this.protocol.getLastPacketAccepted()) != -48) {
            Rs232IntegratedOmniScannerStateManager.this.setWaitingForScannerReadyForAckReceptionState();
         } else if (this.tracer.isOn()) {
            this.tracer.println(3, "ACK received during Idle state. Ignoring packet.");
         }

         return 1;
      }
   }

   private class ReceivingMessage extends Rs232IntegratedOmniScannerState {
      private byte[] lastPacketProcessed = null;
      private ArrayList packetList = null;
      private Rs232IntegratedOmniScannerPacketManager pktManager = null;

      public ReceivingMessage(Rs232IntegratedOmniScannerProtocol protocol) {
         super(protocol);
         this.setTimmer(new Rs232FiscalPrinterTimer(this), 2000);
         this.packetList = new ArrayList();
         this.pktManager = Rs232IntegratedOmniScannerPacketManager.getInstance();
      }

      private boolean isEqual(byte[] b1, byte[] b2) {
         if (b1 != null && b2 != null && b1.length == b2.length) {
            for (int i = 0; i < b1.length; i++) {
               if (b1[i] != b2[i]) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      }

      private byte[] concatenateDecodedData(List decodeDataList) {
         byte[] finalDataBytes = null;

         for (int i = 0; i < decodeDataList.size(); i++) {
            byte[] currentDataBytes = Rs232IntegratedOmniScannerPacketManager.getInstance().getPacketData((byte[])decodeDataList.get(i));
            if (i == 0) {
               finalDataBytes = currentDataBytes;
            } else {
               byte[] aux = new byte[finalDataBytes.length + currentDataBytes.length - 1];
               System.arraycopy(finalDataBytes, 0, aux, 0, finalDataBytes.length);
               System.arraycopy(currentDataBytes, 1, aux, finalDataBytes.length, currentDataBytes.length - 1);
               finalDataBytes = aux;
            }
         }

         return finalDataBytes;
      }

      private void fireData(byte[] data) {
         this.packetList.clear();
         if (this.tracer.isOn()) {
            this.tracer.println(3, "Fireing data event: " + Util.toFormatedHexString(data));
         }

         this.protocol.fireDataEvent(data);
      }

      private void processPacket(byte[] packet) {
         byte opCode = this.pktManager.getOpcode(packet);
         if (opCode != -48 && opCode != -47 && opCode != -42 && opCode != -44 && opCode != -58 && opCode != -92) {
            if (this.tracer.isOn() && this.pktManager.isARetransmission(this.protocol.getLastPacketAccepted())) {
               this.tracer.println(3, "Packet is a retransmission.");
            }

            this.protocol.fireDirectIOEvent(this.protocol.getLastPacketAccepted());
            if (this.pktManager.getOpcode(packet) != -13) {
               this.stopTimer();
               this.packetList.clear();
               Rs232IntegratedOmniScannerStateManager.this.setIdleState();
            } else if (this.pktManager.isARetransmission(packet)
               && this.isEqual(this.pktManager.getPacketData(this.lastPacketProcessed), this.pktManager.getPacketData(packet))) {
               if (this.pktManager.isIntermediatePacket(packet)) {
                  this.restartTimer();
               } else {
                  this.stopTimer();
                  this.packetList.clear();
                  Rs232IntegratedOmniScannerStateManager.this.setIdleState();
               }
            } else {
               this.packetList.add(packet);
               if (this.tracer.isOn()) {
                  this.tracer.println(3, "Packet added. PAcket list has " + this.packetList.size() + " packets.");
               }

               if (!this.pktManager.isIntermediatePacket(packet)) {
                  this.stopTimer();
                  this.fireData(this.concatenateDecodedData(this.packetList));
                  this.packetList.clear();
                  Rs232IntegratedOmniScannerStateManager.this.setIdleState();
               }

               this.lastPacketProcessed = packet;
            }
         } else {
            if (this.tracer.isOn()) {
               this.tracer
                  .println(1, "Packet with unexpected opcode " + Util.toHexString(this.pktManager.getOpcode(packet)) + " received. Resetting message buffer.");
            }

            this.stopTimer();
            this.packetList.clear();
            Rs232IntegratedOmniScannerStateManager.this.setCommunicationErrorState();
         }
      }

      public synchronized int packetArrived() {
         this.protocol.setLastPacketAccepted(this.protocol.getLastPacketReceived());
         if (this.tracer.isOn()) {
            this.tracer.println(3, "Packet arrived to Receiving Message State: " + Util.toFormatedHexString(this.protocol.getLastPacketAccepted()));
         }

         this.stopTimer();
         Rs232IntegratedOmniScannerStateManager.this.setWaitingForScannerReadyForAckReceptionState();
         return 1;
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println(3, "-->Receiving Message state<--");
         }

         this.restartTimer();
         this.processPacket(this.protocol.getLastPacketAccepted());
      }

      public void timerExpired() {
         if (this.tracer.isOn()) {
            this.tracer.println(3, "Timer expired while waiting for next packet.");
         }

         this.packetList.clear();
         Rs232IntegratedOmniScannerStateManager.this.setCommunicationErrorState();
      }
   }

   private class WaitingForCmdResponse extends Rs232IntegratedOmniScannerState {
      private Rs232IntegratedOmniScannerPacketManager pkgManager = Rs232IntegratedOmniScannerPacketManager.getInstance();

      public WaitingForCmdResponse(Rs232IntegratedOmniScannerProtocol protocol) {
         super(protocol);
         this.setTimmer(new Rs232FiscalPrinterTimer(this), 2000);
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println(3, "-->Waiting For Cmd Response state<--");
         }

         this.startTimer();
      }

      public synchronized int packetArrived() {
         this.protocol.setLastPacketAccepted(this.protocol.getLastPacketReceived());
         if (this.tracer.isOn()) {
            this.tracer.println(3, "Packet arrived to Waiting for Cmd Response State: " + Util.toFormatedHexString(this.protocol.getLastPacketAccepted()));
         }

         switch (this.pkgManager.getOpcode(this.protocol.getLastPacketAccepted())) {
            case -58:
               if (this.tracer.isOn()) {
                  this.tracer
                     .println(
                        3, "Params received from scanner: " + Util.toFormatedHexString(this.pkgManager.getPacketData(this.protocol.getLastPacketAccepted()))
                     );
               }

               this.protocol.fireDirectIOEvent(this.protocol.getLastPacketAccepted());
               if (this.pkgManager.isIntermediatePacket(this.protocol.getLastPacketAccepted())) {
                  this.restartTimer();
               } else {
                  this.stopTimer();
                  Rs232IntegratedOmniScannerStateManager.this.setIdleState();
               }
               break;
            case -48:
               this.stopTimer();
               if (this.tracer.isOn()) {
                  this.tracer.println(3, "ACK received.");
               }

               Rs232IntegratedOmniScannerStateManager.this.setIdleState();
               break;
            case -47:
               this.stopTimer();
               if (this.tracer.isOn()) {
                  this.tracer.println(1, "Nak packet received.");
               }

               if (!this.protocol.maxPacketSendAttemptsReached()) {
                  if (this.tracer.isOn()) {
                     this.tracer.println(3, "Resending command.");
                  }

                  this.protocol
                     .setLastPacketWritten(Rs232IntegratedOmniScannerPacketManager.getInstance().setRetransmitBit(this.protocol.getLastPacketWritten()));
                  Rs232IntegratedOmniScannerStateManager.this.setWaitingForScannerReadyForCmdReceptionState();
               } else {
                  if (this.tracer.isOn()) {
                     this.tracer.println(3, "Maximum command resends reached. Switching to communication error state.");
                  }

                  Rs232IntegratedOmniScannerStateManager.this.setCommunicationErrorState();
               }
               break;
            case -13:
            case -10:
               this.stopTimer();
               if (this.tracer.isOn()) {
                  this.tracer.println(3, "Decode/Event arrived while expecting ACK. Transmission pending.");
               }

               this.protocol.setTransmissionPending(true);
               Rs232IntegratedOmniScannerStateManager.this.setWaitingForScannerReadyForAckReceptionState();
               break;
            default:
               this.stopTimer();
               if (this.tracer.isOn()) {
                  this.tracer
                     .println(3, "Packet with opcode " + Util.toHexString(this.pkgManager.getOpcode(this.protocol.getLastPacketAccepted())) + " received.");
               }

               this.protocol.fireDirectIOEvent(this.protocol.getLastPacketAccepted());
               Rs232IntegratedOmniScannerStateManager.this.setIdleState();
         }

         return 1;
      }

      public void timerExpired() {
         if (!this.protocol.maxPacketSendAttemptsReached()) {
            if (this.tracer.isOn()) {
               this.tracer.println(3, "Timeout while waiting for a valid cmd response. Resending command.");
            }

            this.protocol.setLastPacketWritten(Rs232IntegratedOmniScannerPacketManager.getInstance().setRetransmitBit(this.protocol.getLastPacketWritten()));
            Rs232IntegratedOmniScannerStateManager.this.setWaitingForScannerReadyForCmdReceptionState();
         } else {
            if (this.tracer.isOn()) {
               this.tracer.println(3, "Maximum command resends reached. Switching to communication error state.");
            }

            Rs232IntegratedOmniScannerStateManager.this.setCommunicationErrorState();
         }
      }
   }

   private class WaitingForScannerEndOfAckReception extends Rs232IntegratedOmniScannerStateManager.WaitingForScannerEndOfReception {
      public WaitingForScannerEndOfAckReception(Rs232IntegratedOmniScannerProtocol protocol) {
         super(protocol);
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println(3, "-->Waiting for Scanner End of Ack Reception state<--");
         }

         super.set();
      }

      protected void switchToNextState() {
         Rs232IntegratedOmniScannerStateManager.this.setReceivingMessageState();
      }
   }

   private class WaitingForScannerEndOfCmdReception extends Rs232IntegratedOmniScannerStateManager.WaitingForScannerEndOfReception {
      public WaitingForScannerEndOfCmdReception(Rs232IntegratedOmniScannerProtocol protocol) {
         super(protocol);
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println(3, "-->Waiting for Scanner End of Cmd Reception state<--");
         }

         super.set();
      }

      protected void switchToNextState() {
         Rs232IntegratedOmniScannerStateManager.this.setWaitingForCmdResponseState();
      }
   }

   private abstract class WaitingForScannerEndOfReception extends Rs232IntegratedOmniScannerState {
      public WaitingForScannerEndOfReception(Rs232IntegratedOmniScannerProtocol protocol) {
         super(protocol);
      }

      protected abstract void switchToNextState();

      public void set() {
         this.protocol.flush();
         this.startTimer();
         this.protocol.setRTS(false);

         try {
            this.protocol.pollForCTS(false);
            if (this.tracer.isOn()) {
               this.tracer.println(3, "CTS is down.");
            }

            this.stopTimer();
            this.switchToNextState();
         } catch (HandleException var2) {
            if (this.tracer.isOn()) {
               this.tracer.println(3, var2.getMessage());
            }

            Rs232IntegratedOmniScannerStateManager.this.setCommunicationErrorState();
         }
      }
   }

   private class WaitingForScannerReadyForAckReception extends Rs232IntegratedOmniScannerStateManager.WaitingForScannerReadyForReception {
      private byte[] ackPacket = null;

      public WaitingForScannerReadyForAckReception(Rs232IntegratedOmniScannerProtocol protocol) {
         super(protocol);
         this.ackPacket = Rs232IntegratedOmniScannerPacketManager.getInstance().createPacket((byte)-48);
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println(3, "-->Waiting for Scanner Ready for Ack Reception state<--");
         }

         super.set();
      }

      protected void sendPacket() {
         this.protocol.submitToAdapter(this.ackPacket);
      }

      protected void switchToNextState() {
         Rs232IntegratedOmniScannerStateManager.this.setWaitingForScannerEndOfAckReceptionState();
      }
   }

   private class WaitingForScannerReadyForCmdReception extends Rs232IntegratedOmniScannerStateManager.WaitingForScannerReadyForReception {
      public WaitingForScannerReadyForCmdReception(Rs232IntegratedOmniScannerProtocol protocol) {
         super(protocol);
      }

      public void set() {
         if (this.tracer.isOn()) {
            this.tracer.println(3, "-->Waiting for Scanner Ready for Cmd Reception state<--");
         }

         super.set();
      }

      protected void sendPacket() {
         this.protocol.submitToAdapter(this.protocol.getLastPacketWritten());
      }

      protected void switchToNextState() {
         Rs232IntegratedOmniScannerStateManager.this.setWaitingForScannerEndOfCmdReceptionState();
      }
   }

   private abstract class WaitingForScannerReadyForReception extends Rs232IntegratedOmniScannerState {
      public WaitingForScannerReadyForReception(Rs232IntegratedOmniScannerProtocol protocol) {
         super(protocol);
      }

      protected abstract void switchToNextState();

      protected abstract void sendPacket();

      public void set() {
         this.protocol.setRTS(true);

         try {
            this.protocol.pollForCTS(true);
            if (this.tracer.isOn()) {
               this.tracer.println(3, "CTS is up.");
            }

            this.sendPacket();
            this.switchToNextState();
         } catch (HandleException var2) {
            if (this.tracer.isOn()) {
               this.tracer.println(3, var2.getMessage());
            }

            Rs232IntegratedOmniScannerStateManager.this.setCommunicationErrorState();
         }
      }
   }
}
