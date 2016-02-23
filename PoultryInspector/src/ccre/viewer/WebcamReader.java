/*
 * Copyright 2013-2016 Cel Skeggs, 2013 Andrew Merrill.
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
package ccre.viewer;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.imageio.ImageIO;

import ccre.log.Logger;
import ccre.net.ClientSocket;
import ccre.net.Network;

public class WebcamReader implements Closeable {
    // @formatter:off
    private static final String requestString =
            "GET /mjpg/video.mjpg HTTP/1.1\n" +
            "User-Agent: HTTPStreamClient\n" +
            "Connection: Keep-Alive\n" +
            "Cache-Control: no-cache\n" +
            "Authorization: Basic RlJDOkZSQw==\n\n";
    // @formatter:on
    private final ClientSocket socket;
    private final BufferedInputStream source;
    private final BufferedOutputStream sink;
    private final ByteArrayOutputStream lineBuf = new ByteArrayOutputStream();
    private final String boundary;
    private final int timeoutMillis;

    public WebcamReader(String address, int timeoutMillis) throws IOException {
        this.timeoutMillis = timeoutMillis;
        //this.socket = new Socket();
        this.socket = Network.connectDynPort(address, 80);
        try {
            socket.setSocketTimeout(timeoutMillis);
            source = new BufferedInputStream(socket.openInputStream());
            sink = new BufferedOutputStream(socket.openOutputStream());
            sink.write(requestString.getBytes("UTF-8"));
            sink.flush();
            assertLine("HTTP/1.0 200 OK");
            this.boundary = readHeaderFromHeaders("Content-Type").split("boundary=", 2)[1];
        } catch (Throwable thr) {
            try {
                this.socket.close();
            } catch (Throwable thr2) {
                thr.addSuppressed(thr2);
            }
            throw thr;
        }
    }

    private String readHeaderFromHeaders(String headerName) throws IOException {
        headerName = headerName + ": ";
        String value = null;
        while (true) {
            String line = readLine();
            if (line.isEmpty()) {
                break;
            }
            if (line.startsWith(headerName)) {
                if (value != null) {
                    throw new IOException("Multiple instances of header: " + headerName);
                }
                value = line.substring(headerName.length());
            }
        }
        if (value == null) {
            throw new IOException("Cannot find header: " + headerName);
        }
        return value;
    }

    private String readLine() throws IOException, EOFException {
        lineBuf.reset();
        int last = -1;
        while (true) {
            int b = source.read();
            if (b == -1) {
                throw new EOFException();
            }
            if (b == '\n' && last == '\r') {
                byte[] array = lineBuf.toByteArray();
                return new String(array, 0, array.length - 1, "UTF-8");
            }
            lineBuf.write(b);
            last = b;
        }
    }

    private void assertLine(String string) throws IOException {
        String other = readLine();
        if (!string.equals(other)) {
            throw new IOException("Line mismatch: expected " + string + " but got " + other);
        }
    }

    public BufferedImage readNext() throws IOException {
        readThroughBoundary();
        String contentLengthString = readHeaderFromHeaders("Content-Length");
        int contentLength;
        try {
            contentLength = Integer.parseInt(contentLengthString);
        } catch (NumberFormatException nfe) {
            throw new IOException("Invalid content length string: |" + contentLengthString + "|");
        }
        if (contentLength < 1 || contentLength > 200000) {
            throw new IOException("Invalid content length: " + contentLength);
        }
        byte[] bytes = readExact(contentLength);
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
    
    private byte[] readExact(int bytes) throws IOException {
        byte[] out = new byte[bytes];
        int index = 0;
        while (index < out.length) {
            int read = source.read(out, index, out.length - index);
            if (read == -1) {
                throw new EOFException();
            }
            index += read;
        }
        return out;
    }

    private void readThroughBoundary() throws IOException {
        String header;
        do {
            header = readLine();
        } while (header.isEmpty());
        if (!header.endsWith(boundary)) {
            throw new IOException("Boundary not found in: " + header);
        }
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }
}
