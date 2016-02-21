/*
 * Copyright 2016 Cel Skeggs
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
 */
package ccre.frc;

import java.io.IOException;
import java.nio.ByteBuffer;

import edu.wpi.first.wpilibj.hal.SPIJNI;

public class DirectSPI {
    public static final byte PORT_CS0 = 0, PORT_CS1 = 1, PORT_CS2 = 2, PORT_CS3 = 3, PORT_MXP = 4;

    // TODO: one source says the maximum is 7 bytes... (docs in HAL) - should
    // that be included here?
    static final int MAX_SPI_LENGTH = 256; // based on size of byte

    public static byte portForCS(int n) {
        if (n < PORT_CS0 || n > PORT_CS3) {
            throw new IllegalArgumentException("Invalid CS port - not in [0, 3]: " + n);
        }
        return (byte) n;
    }

    public static void checkPort(byte port) {
        if (port < PORT_CS0 || port > PORT_MXP) {
            throw new IllegalArgumentException("Invalid SPI port: " + port);
        }
    }

    public static void init(byte port) {
        checkPort(port);
        SPIJNI.spiInitialize(port);
    }

    public static void free(byte port) {
        checkPort(port);
        SPIJNI.spiClose(port);
    }

    // defaults: hertz = 500,000 Hz, LSB, data on rising, clock active high,
    // chip select UNKNOWN
    public static void configure(byte port, int hertz, boolean isMSB, boolean dataOnFalling, boolean clockActiveLow, boolean chipSelectActiveLow) {
        checkPort(port);
        SPIJNI.spiSetSpeed(port, hertz);
        SPIJNI.spiSetOpts(port, isMSB ? 1 : 0, dataOnFalling ? 1 : 0, clockActiveLow ? 1 : 0);
        if (chipSelectActiveLow) {
            SPIJNI.spiSetChipSelectActiveLow(port);
        } else {
            SPIJNI.spiSetChipSelectActiveHigh(port);
        }
    }

    public static int transact(byte port, ByteBuffer send, ByteBuffer recv, int len) throws IOException {
        if (!send.isDirect() || !recv.isDirect()) {
            throw new IllegalArgumentException("Buffer must be direct!");
        }
        if (len >= MAX_SPI_LENGTH) {
            throw new IllegalArgumentException("Length too long");
        }
        if (send.capacity() < len || recv.capacity() < len) {
            throw new IllegalArgumentException("Buffer too short");
        }
        int count = SPIJNI.spiTransaction(port, send, recv, (byte) len);
        if (count < 0) {
            throw new IOException("SPI write error on " + port);
        }
        return count;
    }

    public static int write(byte port, ByteBuffer data, int len) throws IOException {
        if (!data.isDirect()) {
            throw new IllegalArgumentException("Buffer must be direct!");
        }
        if (len >= MAX_SPI_LENGTH) {
            throw new IllegalArgumentException("Length too long");
        }
        if (data.capacity() < len) {
            throw new IllegalArgumentException("Buffer too short");
        }
        int count = SPIJNI.spiWrite(port, data, (byte) len);
        if (count < 0) {
            throw new IOException("SPI write error on " + port);
        }
        return count;
    }

    public static int read(byte port, ByteBuffer data, int len) throws IOException {
        if (!data.isDirect()) {
            throw new IllegalArgumentException("Buffer must be direct!");
        }
        if (len >= MAX_SPI_LENGTH) {
            throw new IllegalArgumentException("Length too long");
        }
        if (data.capacity() < len) {
            throw new IllegalArgumentException("Buffer too short");
        }
        int count = SPIJNI.spiRead(port, data, (byte) len);
        if (count < 0) {
            throw new IOException("SPI write error on " + port);
        }
        return count;
    }

    // TODO: handle SPI accumulator
}
