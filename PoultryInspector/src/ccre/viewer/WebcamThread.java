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
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.function.Consumer;

import ccre.concurrency.ReporterThread;
import ccre.log.Logger;

class WebcamThread {

    private volatile String address;
    private volatile boolean terminate = false;
    private final Consumer<BufferedImage> output;
    private final ReporterThread thread;
    private Consumer<String> error;

    public WebcamThread(Consumer<BufferedImage> output, Consumer<String> error) {
        this.output = output;
        this.error = error;
        this.thread = new ReporterThread("Webcam") {
            @Override
            protected void threadBody() throws Throwable {
                body();
            }
        };
        this.thread.start();
    }

    public synchronized void setAddress(String address) {
        this.address = address;
        this.notifyAll();
    }

    public void terminate() {
        Logger.finest("Terminating webcam connection to " + address + ".");
        terminate = true;
        this.thread.interrupt();
    }

    private void body() throws InterruptedException {
        output.accept(null);
        while (!terminate) {
            synchronized (this) {
                if (address == null && !terminate) {
                    error.accept("No address.");
                    this.wait();
                    continue;
                }
            }
            boolean long_sleep = false;
            try {
                try {
                    connectToWebcam();
                    error.accept("Stopped.");
                } catch (EOFException e) {
                    error.accept("Ended.");
                } catch (SocketTimeoutException e) {
                    error.accept("Timed out.");
                } catch (UnknownHostException e) {
                    error.accept("Could not resolve.");
                } catch (ConnectException e) {
                    error.accept(e.getMessage());
                } catch (Exception e) {
                    // this is the case that actually logs anything, so it
                    // should wait long enough to avoid log spam. other cases do
                    // not log, and so only benefit from shorter waits.
                    Logger.warning("Webcam connection to " + address + " failed.", e);
                    long_sleep = true;
                }
            } finally {
                output.accept(null);
            }
            Thread.sleep(long_sleep ? 500 : 50);
        }
    }

    private void connectToWebcam() throws IOException, EOFException {
        String addr = address;
        if (addr == null) {
            return;
        }
        error.accept("Connecting...");
        try (WebcamReader reader = new WebcamReader(addr, 500)) {
            error.accept("Beginning...");
            while (!terminate && addr.equals(this.address)) {
                output.accept(reader.readNext());
                error.accept(null);
            }
        }
    }
}
