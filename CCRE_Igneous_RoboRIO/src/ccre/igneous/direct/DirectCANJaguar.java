/*
 * Copyright 2015 Colby Skeggs
 * 
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 * 
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * This file contains code inspired by/based on code Copyright 2008-2014 FIRST.
 * To see the license terms of that code (modified BSD), see the root of the CCRE.
 */
package ccre.igneous.direct;

class DirectCANJaguar { // derived from WPILib CANJaguar code
//    public static final int JAGUAR_NUM = 63;
//    private static final boolean[] allocated = new boolean[JAGUAR_NUM];
//    private static final int kReceiveStatusAttempts = 50;
//    private static final int kSendMessagePeriod = 20;
//    private static final double kApproxBusVoltage = 12.0;
//
//    public static synchronized void init(int busID) {
//        if (busID < 0 || busID >= JAGUAR_NUM) {
//            throw new RuntimeException("Invalid CAN Jaguar bus ID: " + busID);
//        }
//        if (allocated[busID]) {
//            throw new RuntimeException("CAN Jaguar already allocated: " + busID);
//        }
//        allocated[busID] = true;
//
//        m_controlMode = ControlMode.PercentVbus;
//
//        boolean receivedFirmwareVersion = false;
//        byte[] data = new byte[8];
//
//        // Request firmware and hardware version only once
//        requestMessage(busID, CANJNI.CAN_IS_FRAME_REMOTE | CANJNI.CAN_MSGID_API_FIRMVER, CANJNI.CAN_SEND_PERIOD_NO_REPEAT);
//        requestMessage(busID, CANJNI.LM_API_HWVER, CANJNI.CAN_SEND_PERIOD_NO_REPEAT);
//
//        // Wait until we've gotten all of the status data at least once.
//        for (int i = 0; i < kReceiveStatusAttempts; i++) {
//            Timer.delay(0.001);
//
//            setupPeriodicStatus(busID);
//            updatePeriodicStatus(busID);
//
//            if (!receivedFirmwareVersion) {
//                try {
//                    getMessage(busID, CANJNI.CAN_MSGID_API_FIRMVER, CANJNI.CAN_MSGID_FULL_M, data);
//                    m_firmwareVersion = ByteFiddling.asInt32LE(data, 0);
//                    receivedFirmwareVersion = true;
//                } catch (CANMessageNotFoundException e) {
//                }
//            }
//
//            if (m_receivedStatusMessage0 && m_receivedStatusMessage1 && m_receivedStatusMessage2 && receivedFirmwareVersion) {
//                break;
//            }
//        }
//
//        if (!m_receivedStatusMessage0 || !m_receivedStatusMessage1 || !m_receivedStatusMessage2 || !receivedFirmwareVersion) {
//            /* Free the resource */
//            free(busID);
//            throw new CANMessageNotFoundException();
//        }
//
//        try {
//            getMessage(busID, CANJNI.LM_API_HWVER, CANJNI.CAN_MSGID_FULL_M, data);
//            m_hardwareVersion = data[0];
//        } catch (CANMessageNotFoundException e) {
//            // Not all Jaguar firmware reports a hardware version.
//            m_hardwareVersion = 0;
//        }
//
//        if (m_firmwareVersion >= 3330) {
//            throw new RuntimeException("CAN Jaguar " + busID + " firmware does not have FIRST approved firmware: " + m_firmwareVersion);
//        }
//        if (m_firmwareVersion < 108) {
//            throw new RuntimeException("CAN Jaguar " + busID + " firmware is too old (must be >= 108 of FIRST approved firmware): " + m_firmwareVersion);
//        }
//    }
//
//    public static void free(int busID) {
//        if (busID < 0 || busID >= JAGUAR_NUM) {
//            throw new RuntimeException("Invalid CAN Jaguar bus ID: " + busID);
//        }
//        if (!allocated[busID]) {
//            return;
//        }
//        allocated[busID] = false;
//
//        IntBuffer status = Common.allocateInt();
//
//        int messageID;
//
//        // Disable periodic setpoints
//        switch (m_controlMode) {
//        case PercentVbus:
//            messageID = busID | CANJNI.LM_API_VOLT_T_SET;
//            break;
//        case Speed:
//            messageID = busID | CANJNI.LM_API_SPD_T_SET;
//            break;
//        case Position:
//            messageID = busID | CANJNI.LM_API_POS_T_SET;
//            break;
//        case Current:
//            messageID = busID | CANJNI.LM_API_ICTRL_T_SET;
//            break;
//        case Voltage:
//            messageID = busID | CANJNI.LM_API_VCOMP_T_SET;
//            break;
//        default:
//            return;
//        }
//
//        CANJNI.FRCNetworkCommunicationCANSessionMuxSendMessage(messageID, null,
//                CANJNI.CAN_SEND_PERIOD_STOP_REPEATING, status); // TODO: Why is the result thrown away?
//
//        configMaxOutputVoltage(busID, kApproxBusVoltage);
//    }
//
//    public static void setPercentMode(int busID) {
//        changeControlMode(busID, ControlMode.PercentVbus);
//        setPositionReference(busID, CANJNI.LM_REF_NONE);
//        setSpeedReference(busID, CANJNI.LM_REF_NONE);
//    }
//    
//    private static void configMaxOutputVoltage(int busID, double voltage) {
//        short v = (short) (voltage * 256.0);
//        byte[] data = new byte[] {(byte) v, (byte) (v >> 8)};
//        sendMessage(busID, CANJNI.LM_API_CFG_MAX_VOUT, data, CANJNI.CAN_SEND_PERIOD_NO_REPEAT);
//
//        m_maxOutputVoltage = voltage;
//        m_maxOutputVoltageVerified = false;
//    }
//
//    private static void setupPeriodicStatus(int busID) {
//        // Message 0 returns bus voltage, output voltage, output current, and
//        // temperature.
//        final byte[] kMessage0Data = new byte[] {
//                CANJNI.LM_PSTAT_VOLTBUS_B0, CANJNI.LM_PSTAT_VOLTBUS_B1,
//                CANJNI.LM_PSTAT_VOLTOUT_B0, CANJNI.LM_PSTAT_VOLTOUT_B1,
//                CANJNI.LM_PSTAT_CURRENT_B0, CANJNI.LM_PSTAT_CURRENT_B1,
//                CANJNI.LM_PSTAT_TEMP_B0, CANJNI.LM_PSTAT_TEMP_B1
//        };
//
//        // Message 1 returns position and speed
//        final byte[] kMessage1Data = new byte[] {
//                CANJNI.LM_PSTAT_POS_B0, CANJNI.LM_PSTAT_POS_B1, CANJNI.LM_PSTAT_POS_B2, CANJNI.LM_PSTAT_POS_B3,
//                CANJNI.LM_PSTAT_SPD_B0, CANJNI.LM_PSTAT_SPD_B1, CANJNI.LM_PSTAT_SPD_B2, CANJNI.LM_PSTAT_SPD_B3
//        };
//
//        // Message 2 returns limits and faults
//        final byte[] kMessage2Data = new byte[] {
//                CANJNI.LM_PSTAT_LIMIT_CLR,
//                CANJNI.LM_PSTAT_FAULT,
//                CANJNI.LM_PSTAT_END,
//                (byte) 0,
//                (byte) 0,
//                (byte) 0,
//                (byte) 0,
//                (byte) 0,
//        };
//
//        byte[] data = new byte[] { (byte) kSendMessagePeriod, (byte) (kSendMessagePeriod >> 8) };
//        sendMessage(busID, CANJNI.LM_API_PSTAT_PER_EN_S0, data, CANJNI.CAN_SEND_PERIOD_NO_REPEAT);
//        sendMessage(busID, CANJNI.LM_API_PSTAT_PER_EN_S1, data, CANJNI.CAN_SEND_PERIOD_NO_REPEAT);
//        sendMessage(busID, CANJNI.LM_API_PSTAT_PER_EN_S2, data, CANJNI.CAN_SEND_PERIOD_NO_REPEAT);
//
//        sendMessage(busID, CANJNI.LM_API_PSTAT_CFG_S0, kMessage0Data, CANJNI.CAN_SEND_PERIOD_NO_REPEAT);
//        sendMessage(busID, CANJNI.LM_API_PSTAT_CFG_S1, kMessage1Data, CANJNI.CAN_SEND_PERIOD_NO_REPEAT);
//        sendMessage(busID, CANJNI.LM_API_PSTAT_CFG_S2, kMessage2Data, CANJNI.CAN_SEND_PERIOD_NO_REPEAT);
//    }
//
//    private static void updatePeriodicStatus(int busID) {
//        byte[] data = new byte[8];
//
//        // Check if a new bus voltage/output voltage/current/temperature message
//        // has arrived and unpack the values into the cached member variables
//        try {
//            getMessage(busID, CANJNI.LM_API_PSTAT_DATA_S0, CANJNI.CAN_MSGID_FULL_M, data);
//
//            m_busVoltage = unpackFXP8_8(new byte[] { data[0], data[1] });
//            m_outputVoltage = unpackPercentage(new byte[] { data[2], data[3] }) * m_busVoltage;
//            m_outputCurrent = unpackFXP8_8(new byte[] { data[4], data[5] });
//            m_temperature = unpackFXP8_8(new byte[] { data[6], data[7] });
//
//            m_receivedStatusMessage0 = true;
//        } catch (CANMessageNotFoundException e) {
//        }
//
//        // Check if a new position/speed message has arrived and do the same
//        try {
//            getMessage(busID, CANJNI.LM_API_PSTAT_DATA_S1, CANJNI.CAN_MSGID_FULL_M, data);
//
//            m_position = unpackFXP16_16(new byte[] { data[0], data[1], data[2], data[3] });
//            m_speed = unpackFXP16_16(new byte[] { data[4], data[5], data[6], data[7] });
//
//            m_receivedStatusMessage1 = true;
//        } catch (CANMessageNotFoundException e) {
//        }
//
//        // Check if a new limits/faults message has arrived and do the same
//        try {
//            getMessage(busID, CANJNI.LM_API_PSTAT_DATA_S2, CANJNI.CAN_MSGID_FULL_M, data);
//            m_limits = data[0];
//            m_faults = data[1];
//
//            m_receivedStatusMessage2 = true;
//        } catch (CANMessageNotFoundException e) {
//        }
//    }
//
//    private static void getMessage(int busID, int messageID, int messageMask, byte[] data) throws CANMessageNotFoundException {
//        messageID |= busID;
//        messageID &= CANJNI.CAN_MSGID_FULL_M;
//
//        IntBuffer targetedMessageID = Common.allocateInt();
//        targetedMessageID.put(0, messageID);
//
//        ByteBuffer timeStamp = ByteBuffer.allocateDirect(4);
//
//        IntBuffer status = Common.allocateInt();
//
//        // Get the data.
//        ByteBuffer dataBuffer = CANJNI.FRCNetworkCommunicationCANSessionMuxReceiveMessage(targetedMessageID, messageMask, timeStamp, status);
//
//        if (data != null) {
//            dataBuffer.get(data);
//            for (int i = 0; i < dataBuffer.capacity(); i++) {
//                data[i] = dataBuffer.get(i);
//            }
//        }
//
//        int statusCode = status.get(0);
//        if (statusCode < 0) {
//            CANExceptionFactory.checkStatus(statusCode, messageID);
//        }
//    }
//
//    private static void sendMessage(int busID, int messageID, byte[] data, int period) {
//        sendMessageHelper(messageID | busID, data, period);
//    }
//
//    private static void requestMessage(int busID, int messageID, int period) {
//        sendMessageHelper(messageID | busID, null, period);
//    }
//
//    private static final int[] TRUSTED_MESSAGES = {
//            CANJNI.LM_API_VOLT_T_EN, CANJNI.LM_API_VOLT_T_SET, CANJNI.LM_API_SPD_T_EN, CANJNI.LM_API_SPD_T_SET,
//            CANJNI.LM_API_VCOMP_T_EN, CANJNI.LM_API_VCOMP_T_SET, CANJNI.LM_API_POS_T_EN, CANJNI.LM_API_POS_T_SET,
//            CANJNI.LM_API_ICTRL_T_EN, CANJNI.LM_API_ICTRL_T_SET
//    };
//
//    private static final int MAX_MESSAGE_DATA_SIZE = 8;
//    private static final int FULL_MESSAGE_ID_MASK = CANJNI.CAN_MSGID_API_M | CANJNI.CAN_MSGID_MFR_M | CANJNI.CAN_MSGID_DTYPE_M;
//
//    private static void sendMessageHelper(int messageID, byte[] data, int period) {
//        boolean add_trust = false;
//        for (int trustedID : TRUSTED_MESSAGES) {
//            if ((messageID & FULL_MESSAGE_ID_MASK) == trustedID) {
//                add_trust = true;
//                break;
//            }
//        }
//
//        ByteBuffer buffer;
//        if (data == null) {
//            if (add_trust) {
//                buffer = ByteBuffer.allocateDirect(2); // initializes to zero
//            } else {
//                buffer = null;
//            }
//        } else {
//            buffer = ByteBuffer.allocateDirect(add_trust ? data.length + 2 : data.length); // initializes to zero
//            if (add_trust) {
//                buffer.position(2);
//            }
//            buffer.put(data, 0, data.length);
//            buffer.position(0);
//            if (buffer.capacity() > MAX_MESSAGE_DATA_SIZE) {
//                throw new RuntimeException("CAN message has too much data.");
//            }
//        }
//
//        IntBuffer status = Common.allocateInt();
//        CANJNI.FRCNetworkCommunicationCANSessionMuxSendMessage(messageID, buffer, period, status);
//        int statusCode = status.get(0);
//        if (statusCode < 0) {
//            CANExceptionFactory.checkStatus(statusCode, messageID);
//        }
//    }
}
