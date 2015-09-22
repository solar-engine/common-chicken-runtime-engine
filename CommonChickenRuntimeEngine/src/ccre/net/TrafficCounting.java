/*
 * Copyright 2014-2015 Colby Skeggs
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
package ccre.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import ccre.channel.EventOutput;
import ccre.timers.Ticker;

/**
 * You can enable Traffic Counting and query traffic analytics via this class.
 * 
 * @author skeggsc
 */
public class TrafficCounting {

    /**
     * Calculates the number of bytes sent or received in a recent one-second
     * interval.
     *
     * @return the rate of data transfer, in bytes per second.
     */
    public static long getRateBytesPerSecond() {
        return TrafficCounting.lastDelta;
    }

    /**
     * Calculates the total number of bytes sent or received.
     *
     * @return the total number of bytes.
     */
    public static long getTotalBytes() {
        return TrafficCounting.totalBytesReceived.get() + TrafficCounting.totalBytesSent.get();
    }

    public static synchronized void setCountingEnabled(boolean enabled) {
        TrafficCounting.countingEnabled = enabled;
        if (enabled && !TrafficCounting.countingEverEnabled) {
            TrafficCounting.countingEverEnabled = true;
            new Ticker(1000).send(new EventOutput() {// TODO: get rid of this
                                                     // ticker
                @Override
                public void event() {
                    long newTotal = getTotalBytes();
                    TrafficCounting.lastDelta = newTotal - TrafficCounting.lastTotal;
                    TrafficCounting.lastTotal = newTotal;
                }
            });
        }
    }

    private static boolean countingEnabled = false;
    private static boolean countingEverEnabled = false;
    /**
     * The total number of bytes sent through the CountingNetworkProvider.
     */
    private static final AtomicLong totalBytesSent = new AtomicLong();
    /**
     * The total number of bytes received through the CountingNetworkProvider.
     */
    private static final AtomicLong totalBytesReceived = new AtomicLong();
    private static long lastDelta;
    private static long lastTotal;

    static InputStream wrap(InputStream inputStream) {
        return countingEnabled ? new CountingInputStream(inputStream) : inputStream;
    }

    static OutputStream wrap(OutputStream outputStream) {
        return countingEnabled ? new CountingOutputStream(outputStream) : outputStream;
    }

    private static class CountingInputStream extends InputStream {

        private final InputStream base;

        CountingInputStream(InputStream base) {
            this.base = base;
        }

        @Override
        public int read() throws IOException {
            int out = base.read();
            if (out != -1) {
                TrafficCounting.totalBytesReceived.getAndIncrement();
            }
            return out;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int count = base.read(b);
            if (count > 0) {
                TrafficCounting.totalBytesReceived.getAndAdd(count);
            }
            return count;
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            int count = base.read(b, off, len);
            if (count > 0) {
                TrafficCounting.totalBytesReceived.getAndAdd(count);
            }
            return count;
        }

        @Override
        public long skip(long n) throws IOException {
            long count = base.skip(n);
            if (count > 0) {
                TrafficCounting.totalBytesReceived.getAndAdd(count);
            }
            return count;
        }

        @Override
        public int available() throws IOException {
            return base.available();
        }

        @Override
        public void close() throws IOException {
            base.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            base.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            base.reset();
        }

        @Override
        public boolean markSupported() {
            return base.markSupported();
        }
    }

    private static class CountingOutputStream extends OutputStream {

        private final OutputStream base;

        CountingOutputStream(OutputStream base) {
            this.base = base;
        }

        @Override
        public void write(int b) throws IOException {
            base.write(b);
            TrafficCounting.totalBytesSent.getAndIncrement();
        }

        @Override
        public void write(byte[] b) throws IOException {
            base.write(b);
            TrafficCounting.totalBytesSent.getAndAdd(b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            base.write(b, off, len);
            TrafficCounting.totalBytesSent.getAndAdd(b.length);
        }

        @Override
        public void flush() throws IOException {
            base.flush();
        }

        @Override
        public void close() throws IOException {
            base.close();
        }
    }
}
