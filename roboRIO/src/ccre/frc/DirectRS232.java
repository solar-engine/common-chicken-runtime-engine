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
package ccre.frc;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import edu.wpi.first.wpilibj.hal.SerialPortJNI;

class DirectRS232 {
    public static final byte PORT_ONBOARD = 0, PORT_MXP = 1, PORT_USB = 2;
    public static final byte PARITY_NONE = 0, PARITY_ODD = 1, PARITY_EVEN = 2,
            PARITY_MARK = 3, PARITY_SPACE = 4;
    public static final byte STOP_ONE = 10, STOP_ONE_POINT_FIVE = 15,
            STOP_TWO = 20;
    public static final byte FLUSH_ON_ACCESS = 1, FLUSH_WHEN_FULL = 2;

    public static void init(byte port, int baudRate, int dataBits, byte parity, byte stopBits) {
        IntBuffer status = Common.getCheckBuffer();
        SerialPortJNI.serialInitializePort(port, status);
        Common.check(status);
        SerialPortJNI.serialSetBaudRate(port, baudRate, status);
        Common.check(status);
        SerialPortJNI.serialSetDataBits(port, (byte) dataBits, status);
        Common.check(status);
        SerialPortJNI.serialSetParity(port, parity, status);
        Common.check(status);
        SerialPortJNI.serialSetStopBits(port, stopBits, status);
        Common.check(status);

        // return data immediately
        SerialPortJNI.serialSetReadBufferSize(port, 1, status);
        Common.check(status);

        SerialPortJNI.serialSetTimeout(port, 5.0f, status);
        Common.check(status);

        SerialPortJNI.serialSetWriteMode(port, FLUSH_ON_ACCESS, status);
        Common.check(status);

        SerialPortJNI.serialDisableTermination(port, status);
        Common.check(status);

        SerialPortJNI.serialSetWriteBufferSize(port, 1, status);
        Common.check(status);
    }

    public static void setTermination(byte port, Character end) {
        IntBuffer status = Common.getCheckBuffer();
        if (end == null) {
            SerialPortJNI.serialDisableTermination(port, status);
        } else {
            SerialPortJNI.serialEnableTermination(port, end, status);
        }
        Common.check(status);
    }

    public static int getBytesReceived(byte port) {
        IntBuffer status = Common.getCheckBuffer();
        int bytes = SerialPortJNI.serialGetBytesRecieved(port, status);
        Common.check(status);
        return bytes;
    }

    public static byte[] read(byte port, int len) {
        IntBuffer status = Common.getCheckBuffer();
        ByteBuffer recv = ByteBuffer.allocateDirect(len);
        int actuallyReceived = SerialPortJNI.serialRead(port, recv, len, status);
        Common.check(status);
        byte[] array = new byte[actuallyReceived];
        recv.get(array);
        return array;
    }

    public static void flush(byte port) {
        IntBuffer status = Common.getCheckBuffer();
        SerialPortJNI.serialFlush(port, status);
        Common.check(status);
    }

    public static void setWriteBufferMode(byte port, byte mode) {
        IntBuffer status = Common.getCheckBuffer();
        SerialPortJNI.serialSetWriteMode(port, mode, status);
        Common.check(status);
    }

    public static int write(byte port, byte[] buffer, int count) {
        IntBuffer status = Common.getCheckBuffer();
        ByteBuffer dataToSendBuffer = ByteBuffer.allocateDirect(count);
        dataToSendBuffer.put(buffer, 0, count);
        int actual = SerialPortJNI.serialWrite(port, dataToSendBuffer, count, status);
        Common.check(status);
        return actual;
    }

    public static void close(byte port) {
        IntBuffer status = Common.getCheckBuffer();
        SerialPortJNI.serialClose(port, status);
        Common.check(status);
    }
}
