package com.ibm.posj.bus.rs232;

import com.ibm.jutil.Util;
import com.ibm.jutil.tracing.Tracer;
import com.ibm.jutil.tracing.TracerFactory;
import java.util.ArrayList;
import jpos.JposException;

public class Rs232IntegratedOmniScannerPacketManager {
   private static Rs232IntegratedOmniScannerPacketManager instance = null;
   private Tracer tracer = TracerFactory.getInstance().createTracer("Scanner", "Rs232IntegratedOmniScannerPacketManager");
   public static final int NUMBER_OF_CHECKSUM_BYTES = 2;
   private static final int NUMBER_OF_HEADER_BYTES = 4;
   private static final int MINIMUM_PACKET_LENGTH = 6;
   public static final int MAXIMUM_PACKET_LENGTH = 257;
   private static final int MAXIMUM_DATA_LENGTH = 251;
   private static final byte DECODER_MESSAGE = 0;
   private static final byte HOST_MESSAGE = 4;
   private static final int LENGTH_BYTE_POS = 0;
   private static final int OPCODE_BYTE_POS = 1;
   private static final int MESSAGE_SOURCE_BYTE_POS = 2;
   private static final int STATUS_BYTE_POS = 3;
   private static final int FIRST_DATA_BYTE_POS = 4;
   private static final byte STATUS_RETRANSMISSION_MASK = 1;
   private static final byte STATUS_INTERMEDIATE_PACKET_MASK = 2;
   private static final byte STATUS_PERMANENT_CHANGE_MASK = 8;
   private static final byte STATUS_RESERVED_BIT_MASK = 4;
   private static final byte STATUS_UNUSED_BITS_MASK = -16;

   private Rs232IntegratedOmniScannerPacketManager() {
   }

   private void createHeader(byte opCode, byte[] packet) {
      packet[0] = (byte)(packet.length - 2);
      packet[1] = opCode;
      packet[2] = 4;
      packet[3] = 0;
   }

   private void addChecksumBytes(byte[] packet) {
      byte[] checksum = Util.toByteArray(this.calculateChecksum(packet));
      System.arraycopy(checksum, 2, packet, this.getPacketLength(packet), 2);
   }

   private int calculateChecksum(byte[] buffer, int startPos, int length) {
      int sum = 0;

      for (int i = 0; i < this.getPacketLength(buffer, startPos); i++) {
         sum += Util.unsignedInt(buffer[startPos + i]);
      }

      return -sum & 65535;
   }

   private int calculateChecksum(byte[] packet) {
      return this.calculateChecksum(packet, 0, packet.length);
   }

   private int getChecksum(byte[] buffer, int startPos, int length) {
      int res = 0;
      res = buffer[startPos + length - 1] & 255;
      return res | buffer[startPos + length - 2] << 8 & 0xFF00;
   }

   public static Rs232IntegratedOmniScannerPacketManager getInstance() {
      if (null == instance) {
         instance = new Rs232IntegratedOmniScannerPacketManager();
      }

      return instance;
   }

   public byte[] createParameterPacket(byte parameter, byte value) {
      byte[] aux = new byte[]{-1, parameter, value};
      return this.createPacket((byte)-58, aux);
   }

   public byte[] createPacket(byte opCode, byte data) {
      byte[] aux = new byte[]{data};
      return this.createPacket(opCode, aux);
   }

   public byte[] createPacket(byte opCode) {
      return this.createPacket(opCode, null);
   }

   public byte[] createPacket(byte opCode, byte[] data) {
      int dataLength = null != data ? data.length : 0;
      byte[] packetBytes = new byte[4 + dataLength + 2];
      if (this.tracer.isOn()) {
         this.tracer.println(3, "-->Creating packet...");
      }

      this.createHeader(opCode, packetBytes);
      if (null != data) {
         System.arraycopy(data, 0, packetBytes, 4, dataLength);
      }

      this.addChecksumBytes(packetBytes);
      if (this.tracer.isOn()) {
         this.tracer.println(3, "<--Packet created: " + Util.toFormatedHexString(packetBytes));
      }

      return packetBytes;
   }

   public ArrayList createMultipacketMessage(byte opCode, byte[] data) {
      ArrayList listOfPackets = new ArrayList();
      byte[] currentPacketData = null;
      int numberOfPackets;
      if (null == data) {
         numberOfPackets = 0;
      } else {
         numberOfPackets = data.length / 251 + (data.length % 251 > 0 ? 1 : 0);
      }

      for (int i = 0; i < numberOfPackets - 1; i++) {
         currentPacketData = new byte[251];
         System.arraycopy(data, i * 251, currentPacketData, 0, 251);
         listOfPackets.add(this.setIntermediatePacketBit(this.createPacket(opCode, currentPacketData)));
      }

      if (numberOfPackets != 0) {
         int lastPacketLength = data.length - (numberOfPackets - 1) * 251;
         currentPacketData = new byte[lastPacketLength];
         System.arraycopy(data, 251 * (numberOfPackets - 1), currentPacketData, 0, lastPacketLength);
         listOfPackets.add(this.createPacket(opCode, currentPacketData));
      } else {
         listOfPackets.add(this.createPacket(opCode));
      }

      return listOfPackets;
   }

   public byte[] createDirectPacket(byte[] data) throws JposException {
      if (null != data && data.length >= 3) {
         byte[] packet = new byte[data.length + 2 + 4 - 3];
         this.createHeader(data[0], packet);
         if (data.length > 3) {
            System.arraycopy(data, 3, packet, 4, data.length - 3);
         }

         packet[2] = (byte)(packet[2] | data[2] & 8);
         this.addChecksumBytes(packet);
         return packet;
      } else {
         throw new JposException(106, "Trying to submit an illegal byte array.");
      }
   }

   public byte[] setRetransmitBit(byte[] packet) {
      packet[3] = (byte)(packet[3] | 1);
      this.addChecksumBytes(packet);
      return packet;
   }

   public byte[] setIntermediatePacketBit(byte[] packet) {
      packet[3] = (byte)(packet[3] | 2);
      this.addChecksumBytes(packet);
      return packet;
   }

   public boolean isARetransmission(byte[] packet) {
      return (packet[3] & 1) != 0;
   }

   public boolean isIntermediatePacket(byte[] packet) {
      return (packet[3] & 2) != 0;
   }

   public int getPacketLength(byte[] buffer, int startPos) {
      return Util.unsignedInt(buffer[startPos + 0]);
   }

   public int getPacketLength(byte[] packet) {
      return this.getPacketLength(packet, 0);
   }

   public byte[] getPacketData(byte[] packet) {
      byte[] packetData = new byte[this.getPacketLength(packet) - 4];
      System.arraycopy(packet, 4, packetData, 0, this.getPacketLength(packet) - 4);
      return packetData;
   }

   public byte getOpcode(byte[] packet) {
      return packet[1];
   }

   public boolean isCompletePacket(byte[] buffer, int startPos, int length) {
      if (this.tracer.isOn()) {
         this.tracer.println(3, "--> isCompletePacket(): " + Util.toFormatedHexString(buffer, startPos, length));
      }

      if (buffer != null
         && length >= 6
         && length <= 257
         && startPos + length <= buffer.length
         && startPos >= 0
         && length >= 6
         && this.getPacketLength(buffer, startPos) == length - 2
         && this.isChecksumOK(buffer, startPos, length)) {
         if ((buffer[startPos + 3] & 4) != 0 && this.tracer.isOn()) {
            this.tracer.println(2, "Packet's reserved bit in status byte is not 0.");
         }

         if ((buffer[startPos + 3] & -16) != 0 && this.tracer.isOn()) {
            this.tracer.println(2, "Packet's unused bits in status byte are not 0.");
         }

         if (buffer[startPos + 2] != 0 && this.tracer.isOn()) {
            this.tracer.println(2, "Unknown packet source.");
         }

         if (this.tracer.isOn()) {
            this.tracer.println(3, "Submitted packet is a valid packet.");
         }

         if (this.tracer.isOn()) {
            this.tracer.println(3, "<-- isCompletePacket()");
         }

         return true;
      } else {
         if (this.tracer.isOn()) {
            this.tracer.println(1, "Packet is not complete.\n<-- isCompletePacket()");
         }

         return false;
      }
   }

   public boolean isChecksumOK(byte[] buffer, int startPos, int length) {
      return this.calculateChecksum(buffer, startPos, length) == this.getChecksum(buffer, startPos, length);
   }
}
