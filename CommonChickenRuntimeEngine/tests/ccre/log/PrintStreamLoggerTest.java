/*
 * Copyright 2016 Cel Skeggs
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

import static org.junit.Assert.assertTrue;

import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.channel.EventOutput;
import ccre.testing.CountingEventOutput;
import ccre.testing.CountingStringOutput;
import ccre.util.LineCollectorOutputStream;
import ccre.util.Values;

@SuppressWarnings("javadoc")
public class PrintStreamLoggerTest {

    private PrintStreamLogger logger;
    private CountingStringOutput cso;
    private PrintStream printStream;
    private EventOutput postrun;

    @Before
    public void setUp() throws Exception {
        printStream = new PrintStream(new LineCollectorOutputStream() {
            @Override
            protected void collect(String param) {
                cso.set(param);
                if (postrun != null) {
                    postrun.event();
                }
            }
        });
        logger = new PrintStreamLogger(printStream);
        cso = new CountingStringOutput();
    }

    @After
    public void tearDown() throws Exception {
        printStream = null;
        logger = null;
        cso = null;
        postrun = null;
    }

    @Test(expected = NullPointerException.class)
    public void testPrintStreamLoggerNull() {
        new PrintStreamLogger(null);
    }

    @Test
    public void testLogSimple() {
        for (int i = 0; i < 10; i++) {
            for (LogLevel level : LogLevel.allLevels) {
                String str = Values.getRandomString().replace('\n', '_');
                cso.valueExpected = "LOG[" + level + "] " + str;
                logger.log(level, str, (Throwable) null);
                cso.check();
                cso.valueExpected = "LOG[" + level + "] " + str;
                logger.log(level, str, (String) null);
                cso.check();
                cso.valueExpected = "LOG[" + level + "] " + str;
                logger.log(level, str, "");
                cso.check();
            }
        }
    }

    @Test
    public void testLogLogLevelStringThrowable() {
        CountingEventOutput ceo = new CountingEventOutput();
        Throwable thr = new Throwable() {
            // because warnings
            private static final long serialVersionUID = 1L;

            @Override
            public void printStackTrace(PrintStream s) {
                assertTrue(s == printStream);
                ceo.event();
            }
        };
        for (int i = 0; i < 10; i++) {
            for (LogLevel level : LogLevel.allLevels) {
                String str = Values.getRandomString().replace('\n', '_');
                cso.valueExpected = "LOG{" + level + "} " + str;
                ceo.ifExpected = true;
                logger.log(level, str, thr);
                cso.check();
                ceo.check();
            }
        }
    }

    @Test
    public void testLogLogLevelStringString() {
        for (int i = 0; i < 10; i++) {
            for (LogLevel level : LogLevel.allLevels) {
                String str = Values.getRandomString().replace('\n', '_');
                String str2 = Values.getRandomString().replace('\n', '_') + "!";
                cso.valueExpected = "LOG[" + level + "] " + str;
                postrun = () -> {
                    postrun = null;
                    cso.check();
                    cso.valueExpected = str2;
                };
                logger.log(level, str, str2);
                assertTrue(postrun == null);
                cso.check();
            }
        }
    }
}
