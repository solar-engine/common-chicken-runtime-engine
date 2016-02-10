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
 * An interface to read from a generic serial input. Usually RS232.
 *
 * WARNING: THIS INTERFACE IS SUBJECT TO NON-BACKWARDS-COMPATIBLE CHANGE.
 *
 * @author skeggsc
 */
public interface SerialInput {

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
     * @return the read bytes. this could be zero bytes, if non were promptly
     * available!
     * @throws IOException if an error occurs while reading the bytes.
     */
    public byte[] readNonblocking(int max) throws IOException;

    /**
     * Close the serial port. It may not be used after this is invoked.
     *
     * @throws IOException if an error occurred while the port was being closed.
     */
    public void close() throws IOException;
}
