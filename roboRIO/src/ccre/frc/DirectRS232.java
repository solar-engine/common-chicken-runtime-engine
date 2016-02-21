/*
 * Copyright 2015-2016 Cel Skeggs
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
package ccre.frc;

import java.nio.ByteBuffer;

import edu.wpi.first.wpilibj.hal.SerialPortJNI;

class DirectRS232 {
    public static final byte PORT_ONBOARD = 0, PORT_MXP = 1, PORT_USB = 2;
    public static final byte PARITY_NONE = 0, PARITY_ODD = 1, PARITY_EVEN = 2, PARITY_MARK = 3, PARITY_SPACE = 4;
    public static final byte STOP_ONE = 10, STOP_ONE_POINT_FIVE = 15, STOP_TWO = 20;
    public static final byte FLUSH_ON_ACCESS = 1, FLUSH_WHEN_FULL = 2;

    public static void init(byte port, int baudRate, int dataBits, byte parity, byte stopBits, float timeout) {
        if (port != PORT_ONBOARD && port != PORT_MXP && port != PORT_USB) {
            throw new IllegalArgumentException("Invalid port to DirectRS232.init");
        }
        SerialPortJNI.serialInitializePort(port);
        SerialPortJNI.serialSetBaudRate(port, baudRate);
        SerialPortJNI.serialSetDataBits(port, (byte) dataBits);
        SerialPortJNI.serialSetParity(port, parity);
        SerialPortJNI.serialSetStopBits(port, stopBits);

        // return data immediately
        SerialPortJNI.serialSetReadBufferSize(port, 1);
        SerialPortJNI.serialSetTimeout(port, timeout); // default: 5.0f
        SerialPortJNI.serialSetWriteMode(port, FLUSH_ON_ACCESS);
        SerialPortJNI.serialDisableTermination(port);
        SerialPortJNI.serialSetWriteBufferSize(port, 1);
    }

    public static void setTermination(byte port, Character end) {
        if (end == null) {
            SerialPortJNI.serialDisableTermination(port);
        } else {
            SerialPortJNI.serialEnableTermination(port, end);
        }
    }

    public static int getBytesReceived(byte port) {
        return SerialPortJNI.serialGetBytesRecieved(port);
    }

    public static byte[] read(byte port, int len) {
        ByteBuffer recv = ByteBuffer.allocateDirect(len);
        int actuallyReceived = SerialPortJNI.serialRead(port, recv, len);

        byte[] array = new byte[actuallyReceived];
        recv.get(array);
        return array;
    }

    public static void flush(byte port) {
        SerialPortJNI.serialFlush(port);
    }

    public static void clear(byte port) {
        // TODO: handle errors for SerialPortJNI in a way that converts them to
        // IOExceptions
        SerialPortJNI.serialClear(port);
    }

    public static void setWriteBufferMode(byte port, byte mode) {
        SerialPortJNI.serialSetWriteMode(port, mode);
    }

    public static int write(byte port, byte[] buffer, int count) {
        ByteBuffer dataToSendBuffer = ByteBuffer.allocateDirect(count);
        dataToSendBuffer.put(buffer, 0, count);
        return SerialPortJNI.serialWrite(port, dataToSendBuffer, count);
    }

    public static void close(byte port) {
        SerialPortJNI.serialClose(port);
    }
}
