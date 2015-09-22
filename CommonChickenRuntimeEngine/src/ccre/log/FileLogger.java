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
package ccre.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;

import ccre.channel.EventOutput;
import ccre.storage.Storage;
import ccre.time.Time;
import ccre.timers.Ticker;

/**
 * A logging tool that stores logging message in a file on the current computer
 * or robot.
 *
 * @author skeggsc
 */
public class FileLogger implements LoggingTarget {

    /**
     * Register a new FileLogger writing to a unique file with the logging
     * manager.
     */
    public static void register() {
        try {
            int i = 0;
            while (true) {
                InputStream oi = Storage.openInput("log-" + i);
                if (oi == null) {
                    break;
                }
                oi.close();
                i++;
            }
            CCRELoggerFactory.getLogger().addTarget(new FileLogger("log-" + i));
        } catch (IOException ex) {
            CCRELoggerFactory.getLogger().warn("Could not set up File logging!", ex);
        }
    }

    /**
     * The PrintStream to log outputs to.
     */
    private final PrintStream pstream;

    /**
     * Create a new FileLogger writing to the specified output file.
     *
     * @param fname The filename to write to
     * @throws IOException If an IO Exception occurs.
     */
    public FileLogger(String fname) throws IOException {
        this(Storage.openOutput(fname));
    }

    /**
     * Create a new FileLogger writing to the specified output stream.
     *
     * @param out The output stream to write to.
     */
    public FileLogger(OutputStream out) {
        this(out instanceof PrintStream ? (PrintStream) out : new PrintStream(out));
    }

    /**
     * Create a new FileLogger writing to the specified PrintStream.
     *
     * @param pstream The stream to write to.
     */
    public FileLogger(final PrintStream pstream) {
        this.pstream = pstream;
        long now = Time.currentTimeMillis();
        pstream.println("Logging began at " + new Date(System.currentTimeMillis()) + " [" + now + "]");
        new Ticker(10000).send(new EventOutput() {
            public void event() {
                synchronized (FileLogger.this) {
                    pstream.println("Logging continues at " + new Date());
                    pstream.flush();
                }
            }
        });
    }

    public synchronized void log(LogLevel level, String message, Throwable throwable) {
        pstream.println("[" + (Time.currentTimeMillis()) + " " + level + "] " + message);
        if (throwable != null) {
            throwable.printStackTrace(pstream);
        }
        pstream.flush();
    }

    public synchronized void log(LogLevel level, String message, String extended) {
        pstream.println("[" + (Time.currentTimeMillis()) + " " + level + "] " + message);
        if (extended != null) {
            int i = extended.length();
            while (i != 0 && extended.charAt(i - 1) <= 32) {
                i -= 1;
            }
            if (i != 0) {
                pstream.println(extended);
            }
        }
        pstream.flush();
    }
}
