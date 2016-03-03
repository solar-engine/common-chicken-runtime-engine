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
 * A standard I2C port, after selection of a device address.
 *
 * @author skeggsc
 */
public interface I2CIO extends Closeable {
    /**
     * Checks to see if the device on this port is accessible, by sending a
     * zero-byte transaction.
     *
     * This method cannot throw any exceptions - false is returned instead.
     *
     * @return true if the device is available, false otherwise.
     */
    public boolean query();

    /**
     * Executes an arbitrary transaction.
     *
     * @param send the buffer to send data from.
     * @param sendLen how many bytes to use from the buffer.
     * @param recv the buffer to receive data into.
     * @param recvLen the maximum number of bytes to receive.
     * @throws IOException if the transaction cannot be executed.
     */
    public void transact(ByteBuffer send, int sendLen, ByteBuffer recv, int recvLen) throws IOException;

    /**
     * Writes a chunk of data to the device, starting with the initial register
     * number.
     *
     * @param send the buffer to send data from.
     * @param sendLen how many bytes to use from the buffer.
     * @throws IOException if the transaction cannot be executed.
     */
    public void write(ByteBuffer send, int sendLen) throws IOException;

    /**
     * Writes a byte to a register. Equivalent to calling
     * {@link #write(ByteBuffer, int)} with a {@link ByteBuffer} containing only
     * the register byte and the data byte.
     *
     * @param register the register byte.
     * @param data the data byte.
     * @throws IOException if the transaction cannot be executed.
     */
    public void write(byte register, byte data) throws IOException;

    /**
     * Reads a chunk of data from the device, given the initial register number.
     *
     * @param register the first register to read from.
     * @param recv the buffer to receive data into.
     * @param recvLen the maximum number of bytes to receive.
     * @throws IOException if the transaction cannot be executed.
     */
    public void read(byte register, ByteBuffer recv, int recvLen) throws IOException;

    /**
     * Reads a single register. Equivalent to calling
     * {@link #read(byte, ByteBuffer, int)} with a one-byte buffer and
     * extracting the byte.
     *
     * @param register the register to read from.
     * @return the received byte.
     * @throws IOException if the transaction cannot be executed.
     */
    public byte read(byte register) throws IOException;

    /**
     * Reads a chunk of data from the device. Does not send anything to prompt
     * the data.
     *
     * @param recv the buffer to receive data into.
     * @param recvLen the maximum number of bytes to receive.
     * @throws IOException if the transaction cannot be executed.
     */
    public void readOnly(ByteBuffer recv, int recvLen) throws IOException;
}
