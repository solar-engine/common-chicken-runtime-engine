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
package ccre.bus;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A configured SPI port.
 *
 * @author skeggsc
 */
public interface SPIIO extends Closeable {

    /**
     * Performs a SPI transaction, with send and receive buffers, and a number
     * of bytes to exchange.
     *
     * @param send the bytes to send.
     * @param recv the bytes to receive.
     * @param length the number of bytes to exchange.
     * @return the number of bytes actually exchanged.
     * @throws IOException if the transaction could not be performed.
     */
    public int transact(ByteBuffer send, ByteBuffer recv, int length) throws IOException;

    /**
     * Performs a SPI write transaction, with a send buffer and a number of
     * bytes to write.
     *
     * @param send the bytes to send.
     * @param sendLen the number of bytes to write.
     * @return the number of bytes actually written.
     * @throws IOException if the transaction could not be performed.
     */
    public int write(ByteBuffer send, int sendLen) throws IOException;

    /**
     * Performs a SPI write transaction, with a send buffer and a number of
     * bytes to write. Fails if not enough bytes were written.
     *
     * @param send the bytes to send.
     * @param sendLen the number of bytes to write.
     * @throws IOException if the transaction could not be performed, or too few
     * bytes were written.
     */
    public default void writeExact(ByteBuffer send, int sendLen) throws IOException {
        if (write(send, sendLen) != sendLen) {
            throw new IOException("SPI write failure: not enough bytes written");
        }
    }

    /**
     * Performs a SPI read transaction, with a receive buffer and a number of
     * bytes to read.
     *
     * @param recv the buffer to hold the received bytes.
     * @param recvLen the number of bytes to read.
     * @return the number of bytes actually read.
     * @throws IOException if the transaction could not be performed.
     */
    public int read(ByteBuffer recv, int recvLen) throws IOException;

    /**
     * Performs a SPI read transaction, with a receive buffer and a number of
     * bytes to read. Fails if not enough bytes were written.
     *
     * @param recv the buffer to hold the received bytes.
     * @param recvLen the number of bytes to read.
     * @throws IOException if the transaction could not be performed, or too few
     * bytes were written.
     */
    public default void readExact(ByteBuffer recv, int recvLen) throws IOException {
        if (read(recv, recvLen) != recvLen) {
            throw new IOException("SPI read failure: not enough bytes read");
        }
    }

    /**
     * Performs a SPI read transaction, with a receive buffer and a number of
     * bytes to read, and provides zeroed bytes to initiate the reception of the
     * received bytes.
     *
     * @param recv the buffer to hold the received bytes.
     * @param recvLen the number of bytes to read.
     * @return the number of bytes actually read.
     * @throws IOException if the transaction could not be performed.
     */
    public int readInitiated(ByteBuffer recv, int recvLen) throws IOException;

    /**
     * Performs a SPI read transaction, with a receive buffer and a number of
     * bytes to read, and provides zeroed bytes to initiate the reception of the
     * received bytes. Fails if not enough bytes were written.
     *
     * @param recv the buffer to hold the received bytes.
     * @param recvLen the number of bytes to read.
     * @throws IOException if the transaction could not be performed, or too few
     * bytes were written.
     */
    public default void readInitiatedExact(ByteBuffer recv, int recvLen) throws IOException {
        if (readInitiated(recv, recvLen) != recvLen) {
            throw new IOException("SPI read failure: not enough bytes read");
        }
    }
}
