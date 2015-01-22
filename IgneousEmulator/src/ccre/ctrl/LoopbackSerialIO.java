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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import ccre.channel.SerialIO;
import ccre.drivers.ByteFiddling;

/**
 * An emulated serial port that simply sends all data back to itself.
 * 
 * @author skeggsc
 */
public class LoopbackSerialIO implements SerialIO {

    /**
     * Create a new connection that reads and writes from the specified queues.
     * 
     * @param source the input for data to this port.
     * @param target the output of data from this port.
     */
    public LoopbackSerialIO(BlockingQueue<byte[]> source, BlockingQueue<byte[]> target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Create a new connection that reads and writes from the specified queue.
     * 
     * @param data the queue for both reading and writing data.
     */
    public LoopbackSerialIO(BlockingQueue<byte[]> data) {
        this.source = this.target = data;
    }

    /**
     * Create a new loopback connection for serial messages.
     */
    public LoopbackSerialIO() {
        this(new ArrayBlockingQueue<byte[]>(16));
    }

    private Character termination = null;
    private final BlockingQueue<byte[]> source, target;
    private byte[] reading;
    private int readingIndex;
    private final ByteBuffer writing = ByteBuffer.allocate(1024);
    private boolean flushOnWrite = true;
    private boolean closed = false;

    public void setTermination(Character end) throws IOException {
        termination = end;
    }

    public byte[] readBlocking(int max) throws IOException {
        return read(max, true);
    }

    public byte[] readNonblocking(int max) throws IOException {
        return read(max, false);
    }

    private synchronized byte[] read(int max, boolean canBlock) throws IOException {
        if (reading != null && readingIndex >= reading.length) {
            reading = null;
        }
        if (reading == null) {
            readingIndex = 0;
            try {
                reading = canBlock && !closed ? source.take() : source.poll();
            } catch (InterruptedException e) {
                throw new InterruptedIOException("interrupted during blocking LoopbackSerialIO read.");
            }
            if (reading == null) {
                if (closed) {
                    throw new IOException("LoopbackSerialIO closed.");
                }
                return new byte[0];
            }
        }
        byte[] out;
        if (readingIndex == 0 && reading.length <= max && termination == null) {
            out = reading;
            reading = null;
        } else {
            int actuallyReadable = reading.length;
            if (termination != null) {
                int pos = ByteFiddling.indexOf(reading, readingIndex, reading.length, (byte) termination.charValue());
                if (pos != -1) {
                    actuallyReadable = pos;
                }
            }
            out = Arrays.copyOfRange(reading, readingIndex, Math.min(max, actuallyReadable));
            readingIndex += out.length;
        }
        return out;
    }

    public synchronized void flush() throws IOException {
        if (closed) {
            throw new IOException("LoopbackSerialIO closed.");
        }
        if (writing.position() > 0) {
            byte[] toQueue = new byte[writing.position()];
            writing.get(toQueue);
            try {
                target.put(toQueue);
            } catch (InterruptedException e) {
                throw new InterruptedIOException("interrupted during blocking LoopbackSerialIO write.");
            }
            writing.clear();
        }
    }

    public void setFlushOnWrite(boolean flushOnWrite) throws IOException {
        this.flushOnWrite = flushOnWrite;
    }

    public void writeFully(byte[] bytes, int from, int to) throws IOException {
        write(bytes, from, to, false);
    }

    private int write(byte[] bytes, int from, int to, boolean partial) throws IOException {
        if (closed) {
            throw new IOException("LoopbackSerialIO closed.");
        }
        if (to <= from) {
            return 0;
        }
        int offset = from;
        while (writing.remaining() < to - offset) {
            if (writing.hasRemaining()) {
                int count = writing.remaining();
                writing.put(bytes, offset, count);
                offset += count;
            }
            flush();
            if (partial && offset > from) {
                return offset;
            }
        }
        if (offset < to) {
            writing.put(bytes, offset, to - offset);
        }
        if (flushOnWrite || !writing.hasRemaining()) {
            flush();
        }
        return to - from;
    }

    public int writePartial(byte[] bytes, int from, int to) throws IOException {
        return write(bytes, from, to, true);
    }

    public void close() throws IOException {
        flush();
        closed = true;
    }
}
