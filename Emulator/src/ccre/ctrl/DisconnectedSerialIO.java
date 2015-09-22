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
 */
package ccre.ctrl;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.SerialIO;

/**
 * An emulation of a completely disconnected serial port. Nothing comes in, and
 * everything going out is ignored.
 *
 * @author skeggsc
 */
public class DisconnectedSerialIO implements SerialIO {
    private static Logger logger = LoggerFactory.getLogger(DisconnectedSerialIO.class);

    private boolean closed = false;

    @Override
    public void setTermination(Character end) throws IOException {
        // don't care.
    }

    @Override
    public synchronized byte[] readBlocking(int max) throws IOException {
        logger.warn("Blocking read from DisconnectedSerialIO!");
        while (!closed) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new InterruptedIOException("interrupted in DisconnectedSerialIO blocking read");
            }
        }
        throw new IOException("DisconnectedSerialIO closed.");
    }

    @Override
    public byte[] readNonblocking(int max) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSerialIO closed.");
        }
        return new byte[0];
    }

    @Override
    public void flush() throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSerialIO closed.");
        }
        // do nothing.
    }

    @Override
    public synchronized void close() throws IOException {
        // do nothing.
        closed = true;
        this.notifyAll();
    }

    @Override
    public void setFlushOnWrite(boolean flushOnWrite) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSerialIO closed.");
        }
        // don't care.
    }

    @Override
    public void writeFully(byte[] bytes, int from, int to) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSerialIO closed.");
        }
        // do nothing.
    }

    @Override
    public int writePartial(byte[] bytes, int from, int to) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSerialIO closed.");
        }
        return to - from;
    }
}
