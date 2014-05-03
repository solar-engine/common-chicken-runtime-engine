/*
 * Copyright 2014 Colby Skeggs
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A network provider that counts the bytes being sent across the network.
 *
 * @author skeggsc
 */
public class CountingNetworkProvider extends DefaultNetworkProvider {

    public static void register() {
        Network.setProvider(new CountingNetworkProvider());
    }
    
    public static AtomicLong totalBytesSent = new AtomicLong();
    public static AtomicLong totalBytesReceived = new AtomicLong();

    public static long getTotal() {
        return totalBytesReceived.get() + totalBytesSent.get();
    }

    @Override
    public ClientSocket openClient(String targetAddress, int port) throws IOException {
        return new CountingClientSocket(super.openClient(targetAddress, port));
    }

    public ServerSocket openServer(int port) throws IOException {
        return new CountingServerSocket(super.openServer(port));
    }

    private static class CountingClientSocket implements ClientSocket {

        private final ClientSocket base;

        public CountingClientSocket(ClientSocket base) {
            this.base = base;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new CountingInputStream(base.openInputStream());
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return new CountingOutputStream(base.openOutputStream());
        }

        @Override
        public DataInputStream openDataInputStream() throws IOException {
            return new DataInputStream(openInputStream());
        }

        @Override
        public DataOutputStream openDataOutputStream() throws IOException {
            return new DataOutputStream(openOutputStream());
        }

        @Override
        public void close() throws IOException {
            base.close();
        }
    }

    private static class CountingServerSocket implements ServerSocket {

        private final ServerSocket base;

        public CountingServerSocket(ServerSocket base) {
            this.base = base;
        }

        @Override
        public ClientSocket accept() throws IOException {
            return new CountingClientSocket(base.accept());
        }

        @Override
        public void close() throws IOException {
            base.close();
        }
    }

    private static class CountingInputStream extends InputStream {

        private final InputStream base;

        public CountingInputStream(InputStream base) {
            this.base = base;
        }

        @Override
        public int read() throws IOException {
            int out = base.read();
            if (out != -1) {
                totalBytesReceived.getAndIncrement();
            }
            return out;
        }

        public int read(byte[] b) throws IOException {
            int count = base.read(b);
            if (count > 0) {
                totalBytesReceived.getAndAdd(count);
            }
            return count;
        }

        public int read(byte b[], int off, int len) throws IOException {
            int count = base.read(b, off, len);
            if (count > 0) {
                totalBytesReceived.getAndAdd(count);
            }
            return count;
        }

        public long skip(long n) throws IOException {
            long count = base.skip(n);
            if (count > 0) {
                totalBytesReceived.getAndAdd(count);
            }
            return count;
        }

        public int available() throws IOException {
            return base.available();
        }

        public void close() throws IOException {
            base.close();
        }

        public void mark(int readlimit) {
            base.mark(readlimit);
        }

        public void reset() throws IOException {
            base.reset();
        }

        public boolean markSupported() {
            return base.markSupported();
        }
    }

    private static class CountingOutputStream extends OutputStream {

        private final OutputStream base;

        public CountingOutputStream(OutputStream base) {
            this.base = base;
        }

        @Override
        public void write(int b) throws IOException {
            base.write(b);
            totalBytesSent.getAndIncrement();
        }

        @Override
        public void write(byte[] b) throws IOException {
            base.write(b);
            totalBytesSent.getAndAdd(b.length);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            base.write(b, off, len);
            totalBytesSent.getAndAdd(b.length);
        }

        public void flush() throws IOException {
            base.flush();
        }

        public void close() throws IOException {
            base.close();
        }
    }
}
