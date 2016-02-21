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
 */
package ccre.bus; // TODO: move Serial* classes to ccre.bus

import java.io.Closeable;
import java.io.IOException;

/**
 * A standard RS232 serial port.
 *
 * @author skeggsc
 */
public interface RS232IO extends Closeable {

    public void resetSerial() throws IOException;

    public boolean hasAvailableBytes() throws IOException;

    /**
     * Set the termination character - read operations will be ended early where
     * these characters are found.
     *
     * @param end the character to end read operations on, or null to not end
     * early.
     * @throws IOException if the termination cannot be set.
     */
    public void setTermination(Character end) throws IOException;

    /**
     * Read a byte array in a possibly-blocking manner, up to the given number
     * of bytes.
     *
     * @param max the maximum number of bytes to read.
     * @return the read bytes.
     * @throws IOException if an error occurs while reading the bytes.
     */
    public byte[] readBlocking(int max) throws IOException;

    /**
     * Read a byte array in a nonblocking manner, up to the given number of
     * bytes.
     *
     * @param max the maxmimum number of bytes to read.
     * @return the read bytes. this could be zero bytes, if none were promptly
     * available!
     * @throws IOException if an error occurs while reading the bytes.
     */
    public byte[] readNonblocking(int max) throws IOException;

    /**
     * Flush any remaining output as far down to the hardware as possible. This
     * will send any buffered data.
     *
     * @throws IOException if an error occurred while flushing.
     */
    public void flush() throws IOException;

    /**
     * Sets whether or not data should be flushed immediately when written as
     * opposed to waiting for a full buffer or a call to flush.
     *
     * @param flushOnWrite if data should be flushed immediately.
     * @throws IOException if an error occurred while changing the setting.
     */
    public void setFlushOnWrite(boolean flushOnWrite) throws IOException;

    /**
     * Write the entire byte section.
     *
     * @param bytes the byte array to write a section from.
     * @param from the start of the section.
     * @param to the end of the section.
     * @throws IOException if an error occurred while writing the data.
     */
    public void writeFully(byte[] bytes, int from, int to) throws IOException;

    /**
     * Write an appropriately-sized chunk of the byte section, and return the
     * number of bytes actually written.
     *
     * @param bytes the byte array to write a section from.
     * @param from the start of the section.
     * @param to the end of the section.
     * @return the number of bytes actually written.
     * @throws IOException if an error occurred while writing the data.
     */
    public int writePartial(byte[] bytes, int from, int to) throws IOException;

    /**
     * Close the serial port. It may not be used after this is invoked.
     *
     * @throws IOException if an error occurred while the port was being closed.
     */
    public void close() throws IOException;
}
