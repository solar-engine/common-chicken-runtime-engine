/*
 * Copyright 2013-2014 Andrew Merrill & Colby Skeggs
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
package intelligence;

import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Webcam extends Thread {

    private String label;
    private String address;
    private BufferedImage image;
    private boolean active;
    private boolean keepRunning;
    private int frameCount;
    private long startTime;
    private int rotate = 0;
    private static GraphicsConfiguration graphicsConfiguration;
    private volatile long lastReceived = 0;

    static {
        GraphicsEnvironment graphEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice graphDevice = graphEnv.getDefaultScreenDevice();
        graphicsConfiguration = graphDevice.getDefaultConfiguration();
    }
    private static final boolean retryConnections = true;

    public Webcam(String label, String address, boolean active, int rotate) {
        this.label = label;
        this.address = address;
        this.active = active;
        this.rotate = rotate;
        this.image = null;
        this.keepRunning = true;
        this.start();
    }

    private Socket curclose = null;

    public void reconnect() {
        System.out.println("DoInterrupt");
        if (curclose != null) {
            try {
                curclose.close();
            } catch (IOException ex) {
                System.out.println("Got error while closing socket!");
                ex.printStackTrace();
            }
        }
        this.interrupt();
    }

    private static class WrappedIOException extends RuntimeException {

        WrappedIOException(IOException io) {
            super(io);
        }
    }

    public void run() {
        while (keepRunning) {
            System.out.println("Try connect at " + System.currentTimeMillis());
            try {
                connectToWebcam(address);
                Thread.sleep(1000);
            } catch (EOFException eofe) {
                System.out.println("reached end of file");
            } catch (WrappedIOException wio) {
                System.out.println("IO Exception: " + wio.getCause());
            } catch (IOException ioe) {
                System.out.println("IO Exception: " + ioe);
            } catch (InterruptedException inte) {
                System.out.println("Interrupted: " + inte);
            }
            if (!retryConnections) {
                break;
            }
        }
        System.out.println("Finished thread.");
    }

    public void connectToWebcam(String address) throws IOException, EOFException {
        String requestString = "GET /mjpg/video.mjpg HTTP/1.1\n"
                + "User-Agent: HTTPStreamClient\n"
                + "Connection: Keep-Alive\n"
                + "Cache-Control: no-cache\n"
                + "Authorization: Basic RlJDOkZSQw==\n\n";

        Socket socket;
        BufferedInputStream socketInputStream;
        BufferedOutputStream socketOutputStream;
        String boundary = null;

        socket = new Socket(address, 80);
        curclose = socket;
        try {
            socketInputStream = new BufferedInputStream(socket.getInputStream());
            socketOutputStream = new BufferedOutputStream(socket.getOutputStream());
            socketOutputStream.write(requestString.getBytes());
            socketOutputStream.flush();
            String header;
            assertLine(socketInputStream, "HTTP/1.0 200 OK");
            for (String line : readLinesUntilEmptyLine(socketInputStream)) {
                if (line.startsWith("Content-Type:")) {
                    int i = line.indexOf("boundary=");
                    boundary = line.substring(i + 9);
                }
            }

            frameCount = 0;
            startTime = System.currentTimeMillis();

            while (keepRunning && !Thread.interrupted()) {
                header = nextNonemptyLine(socketInputStream);
                if (!header.endsWith(boundary)) {
                    throw new IOException("not a boundary: " + header);
                }

                int contentLength = -1;
                for (String line : readLinesUntilEmptyLine(socketInputStream)) {
                    if (line.startsWith("Content-Length:")) {
                        String contentLengthString = line.substring(line.indexOf(": ") + 2);
                        try {
                            contentLength = Integer.parseInt(contentLengthString);
                        } catch (NumberFormatException nfe) {
                            throw new IOException("invalid content length: |" + contentLengthString + "|");
                        }
                    }
                }

                if (contentLength < 1 || contentLength > 200000) {
                    throw new IOException("content length out of range: " + contentLength);
                }
                BufferedImage newImage = readImage(socketInputStream, contentLength);

                if (rotate != 0) {
                    AffineTransform rotateTransform = AffineTransform.getQuadrantRotateInstance(rotate);
                    AffineTransformOp rotateOp = new AffineTransformOp(rotateTransform, AffineTransformOp.TYPE_BILINEAR);
                    BufferedImage rotatedImage = graphicsConfiguration.createCompatibleImage(newImage.getHeight(), newImage.getWidth());
                    Graphics2D pen2 = rotatedImage.createGraphics();
                    pen2.drawImage(newImage, rotateOp, newImage.getWidth() / 2, newImage.getHeight() / 2);
                    newImage = rotatedImage;
                }

                synchronized (this) {
                    this.image = newImage;
                }
                lastReceived = System.nanoTime();

                if (active) {
                    // Here is where we'd send the image to the display panel.
                }

                frameCount++;
                if (frameCount % 360 == 0) {
                    long currentTime = System.currentTimeMillis();
                    double fps = frameCount / ((currentTime - startTime) / 1000.0);
                    System.out.println("FPS (camera " + label + "): " + fps);
                    frameCount = 0;
                    startTime = currentTime;
                }
            }
            System.out.println("Done!");
        } finally {
            curclose = null;
        }
    }

    private void assertLine(InputStream stream, String require) throws IOException {
        String line = readLine(stream);
        if (!line.equals(require)) {
            throw new IOException("Expected '" + require + "', got '" + line + "'");
        }
    }

    private String nextNonemptyLine(BufferedInputStream socketInputStream) throws IOException {
        while (true) {
            String line = readLine(socketInputStream);
            if (!line.isEmpty()) {
                return line;
            }
        }
    }

    private static Iterable<String> readLinesUntilEmptyLine(final InputStream socketInputStream) {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    public String next = null;

                    @Override
                    public boolean hasNext() {
                        if (next == null) {
                            try {
                                next = readLine(socketInputStream);
                            } catch (IOException ex) {
                                throw new WrappedIOException(ex);
                            }
                        }
                        return !next.isEmpty();
                    }

                    @Override
                    public String next() throws NoSuchElementException {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        String out = next;
                        next = null;
                        return out;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
        };
    }

    static String readLine(InputStream stream) throws IOException, EOFException {
        final int BUFSIZE = 1024;
        byte[] buffer = new byte[BUFSIZE];
        int b;
        int i = 0;
        while (true) {
            b = stream.read();
            if (b == -1) {
                throw new EOFException();
            }
            buffer[i] = (byte) b;
            if (i > 0 && b == 10 && buffer[i - 1] == 13) {
                return new String(buffer, 0, i - 1);
            } else {
                i++;
                if (i >= BUFSIZE) {
                    break;
                }
            }
        }
        return null;
    }

    BufferedImage readImage(InputStream stream, int contentLength) throws IOException, EOFException {
        byte[] imageBytes = new byte[contentLength];
        int bytesRemaining = contentLength;
        int offset = 0;
        while (bytesRemaining > 0) {
            int bytesRead = stream.read(imageBytes, offset, bytesRemaining);
            if (bytesRead == -1) {
                throw new EOFException();
            }
            bytesRemaining -= bytesRead;
            offset += bytesRead;
        }
        BufferedImage newImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        return newImage;
    }

    public synchronized BufferedImage getImage() {
        return image;
    }

    public boolean isUpToDate() {
        return (System.nanoTime() - lastReceived) < 1000 * 1000000L; // 1 second
    }
}
