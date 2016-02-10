/*
 * Copyright 2015 Cel Skeggs
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
package ccre.channel;

import java.io.IOException;

/**
 * An interface to write to a generic serial output. Usually RS232.
 *
 * WARNING: THIS INTERFACE IS SUBJECT TO NON-BACKWARDS-COMPATIBLE CHANGE.
 *
 * @author skeggsc
 */
public interface SerialOutput {

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
