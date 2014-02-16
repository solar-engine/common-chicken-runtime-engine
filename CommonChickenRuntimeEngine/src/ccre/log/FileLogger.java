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

import ccre.ctrl.Ticker;
import ccre.event.EventConsumer;
import ccre.saver.StorageProvider;
import ccre.workarounds.ThrowablePrinter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.logging.Level;

/**
 * A logging tool that stores logging message in a file on the current computer
 * or robot.
 *
 * @author skeggsc
 */
public class FileLogger implements LoggingTarget {

    public static void register() {
        try {
            Logger.addTarget(new FileLogger("log-" + System.currentTimeMillis()));
        } catch (IOException ex) {
            Logger.log(LogLevel.WARNING, "Could not set up File logging!", ex);
        }
    }

    private final PrintStream pstream;
    private final long start;

    public FileLogger(String fname) throws IOException {
        this(StorageProvider.openOutput(fname));
    }

    public FileLogger(OutputStream out) {
        this(out instanceof PrintStream ? (PrintStream) out : new PrintStream(out));
    }

    public FileLogger(PrintStream pstream) {
        this.pstream = pstream;
        start = System.currentTimeMillis();
        pstream.println("Logging began at " + new Date(start) + " [" + start + "]");
        new Ticker(10000).addListener(new EventConsumer() {
            public void eventFired() {
                log(LogLevel.FINE, "Logging continues at " + new Date(), (String) null);
            }
        });
    }

    public void log(LogLevel level, String message, Throwable throwable) {
        pstream.println("[" + (System.currentTimeMillis() - start) + " " + level + "] " + message);
        if (throwable == null) {
            return;
        }
        ThrowablePrinter.printThrowable(throwable, pstream);
        pstream.flush();
    }

    public void log(LogLevel level, String message, String extended) {
        pstream.println("[" + (System.currentTimeMillis() - start) + " " + level + "] " + message);
        if (extended == null) {
            return;
        }
        int i = extended.length();
        while (i != 0 && extended.charAt(i - 1) <= 32) {
            i -= 1;
        }
        if (i == 0) {
            return;
        }
        pstream.println(extended);
        pstream.flush();
    }
}
