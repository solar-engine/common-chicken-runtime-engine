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
package ccre.frc;

import java.io.IOException;

import ccre.bus.RS232IO;
import ccre.bus.RS232Bus.Parity;
import ccre.bus.RS232Bus.StopBits;

final class RS232Direct implements RS232IO {
    private boolean closed;
    private final byte port;

    RS232Direct(byte port, int baudRate, Parity parity, StopBits stopBits, float timeout, int dataBits) {
        this.port = port;
        DirectRS232.init(port, baudRate, dataBits, parityToByte(parity), stopBitsToByte(stopBits), timeout);
    }

    private byte parityToByte(Parity parity) {
        switch (parity) {
        case PARITY_EVEN:
            return DirectRS232.PARITY_EVEN;
        case PARITY_MARK:
            return DirectRS232.PARITY_MARK;
        case PARITY_NONE:
            return DirectRS232.PARITY_NONE;
        case PARITY_ODD:
            return DirectRS232.PARITY_ODD;
        case PARITY_SPACE:
            return DirectRS232.PARITY_SPACE;
        default:
            throw new IllegalArgumentException("Invalid parity: " + parity);
        }
    }

    private byte stopBitsToByte(StopBits stopBits) {
        switch (stopBits) {
        case STOP_ONE:
            return DirectRS232.STOP_ONE;
        case STOP_ONE_POINT_FIVE:
            return DirectRS232.STOP_ONE_POINT_FIVE;
        case STOP_TWO:
            return DirectRS232.STOP_TWO;
        default:
            throw new IllegalArgumentException("Invalid stop bits: " + stopBits);
        }
    }

    public void setTermination(Character end) throws IOException {
        if (closed) {
            throw new IOException("SerialIO closed.");
        }
        DirectRS232.setTermination(port, end);
    }

    public byte[] readBlocking(int max) throws IOException {
        if (closed) {
            throw new IOException("SerialIO closed.");
        }
        if (max <= 0) {
            return new byte[0];
        }
        int ready = DirectRS232.getBytesReceived(port);
        if (ready <= 0) {
            byte[] gotten = DirectRS232.read(port, 1);
            while (gotten.length == 0) {
                // block for minimal amount of time if any data has been
                // received
                gotten = DirectRS232.read(port, 1);
            }
            ready = DirectRS232.getBytesReceived(port);
            if (max == 1 || ready <= 0) {
                return gotten;
            }
            byte[] rest = DirectRS232.read(port, Math.min(ready, max));
            byte[] out = new byte[rest.length + 1];
            out[0] = gotten[0];
            System.arraycopy(rest, 0, out, 1, rest.length);
            return out;
        } else {
            return DirectRS232.read(port, Math.min(ready, max));
        }
    }

    @Override
    public boolean hasAvailableBytes() throws IOException {
        return DirectRS232.getBytesReceived(port) > 0;
    }

    public byte[] readNonblocking(int max) throws IOException {
        if (closed) {
            throw new IOException("SerialIO closed.");
        }
        int count = Math.min(DirectRS232.getBytesReceived(port), max);
        return count <= 0 ? new byte[0] : DirectRS232.read(port, count);
    }

    public void flush() throws IOException {
        if (closed) {
            throw new IOException("SerialIO closed.");
        }
        DirectRS232.flush(port);
    }

    public void setFlushOnWrite(boolean flushOnWrite) throws IOException {
        if (closed) {
            throw new IOException("SerialIO closed.");
        }
        DirectRS232.setWriteBufferMode(port, flushOnWrite ? DirectRS232.FLUSH_ON_ACCESS : DirectRS232.FLUSH_WHEN_FULL);
    }

    public void writeFully(byte[] bytes, int from, int to) throws IOException {
        if (closed) {
            throw new IOException("SerialIO closed.");
        }
        if (from != 0) {
            System.arraycopy(bytes, from, bytes, 0, to - from);
        }
        int remaining = to - from;
        while (true) {
            int done = DirectRS232.write(port, bytes, remaining);
            if (done >= remaining) {
                break;
            }
            if (closed) {
                throw new IOException("SerialIO closed.");
            }
            remaining -= done;
            System.arraycopy(bytes, done, bytes, 0, remaining);
        }
    }

    public int writePartial(byte[] bytes, int from, int to) throws IOException {
        if (closed) {
            throw new IOException("SerialIO closed.");
        }
        if (from != 0) {
            System.arraycopy(bytes, from, bytes, 0, to - from);
        }
        return DirectRS232.write(port, bytes, to - from);
    }

    @Override
    public void resetSerial() throws IOException {
        if (closed) {
            throw new IOException("SerialIO closed.");
        }
        DirectRS232.clear(port);
    }

    public void close() throws IOException {
        if (!closed) {
            closed = true;
            flush();
            DirectRS232.close(port);
        }
        // TODO: Provide access to the other method of SerialPort?
    }
}