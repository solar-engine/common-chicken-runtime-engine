/*
 * Copyright 2016 Colby Skeggs
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.util.Values;

@SuppressWarnings("javadoc")
public class LoggerTest {
    @Before
    public void setUp() throws Exception {
        VerifyingLogger.begin();
    }

    @After
    public void tearDown() throws Exception {
        VerifyingLogger.checkAndEnd();
    }

    @Test
    public void testLogLogLevelStringThrowable() {
        for (LogLevel level : LogLevel.allLevels) {
            for (String message : Values.getRandomStrings(20)) {
                Throwable thr = Values.getRandomBoolean() ? null : new Exception();
                VerifyingLogger.configure(level, message, thr);
                Logger.log(level, message, thr);
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testLogLogLevelStringThrowableNullA() {
        Logger.log(null, "test", null);
    }

    @Test(expected = NullPointerException.class)
    public void testLogLogLevelStringThrowableNullB() {
        Logger.log(LogLevel.SEVERE, null, null);
    }

    @Test
    public void testLogLogLevelString() {
        for (LogLevel level : LogLevel.allLevels) {
            for (String message : Values.getRandomStrings(10)) {
                VerifyingLogger.configure(level, message);
                Logger.log(level, message);
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testLogLogLevelStringNullA() {
        Logger.log(null, "test");
    }

    @Test(expected = NullPointerException.class)
    public void testLogLogLevelStringNullB() {
        Logger.log(LogLevel.SEVERE, null, null);
    }

    @Test
    public void testLogExt() {
        for (LogLevel level : LogLevel.allLevels) {
            for (String message : Values.getRandomStrings(10)) {
                String ext = Values.getRandomString();
                VerifyingLogger.configureExt(level, message, ext);
                Logger.logExt(level, message, ext);
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testLogExtNullA() {
        Logger.logExt(null, "test", null);
    }

    @Test(expected = NullPointerException.class)
    public void testLogExtNullB() {
        Logger.logExt(LogLevel.SEVERE, null, null);
    }

    @Test
    public void testSevereString() {
        for (String message : Values.getRandomStrings(10)) {
            VerifyingLogger.configure(LogLevel.SEVERE, message);
            Logger.severe(message);
        }
    }

    @Test
    public void testWarningString() {
        for (String message : Values.getRandomStrings(10)) {
            VerifyingLogger.configure(LogLevel.WARNING, message);
            Logger.warning(message);
        }
    }

    @Test
    public void testInfoString() {
        for (String message : Values.getRandomStrings(10)) {
            VerifyingLogger.configure(LogLevel.INFO, message);
            Logger.info(message);
        }
    }

    @Test
    public void testConfigString() {
        for (String message : Values.getRandomStrings(10)) {
            VerifyingLogger.configure(LogLevel.CONFIG, message);
            Logger.config(message);
        }
    }

    @Test
    public void testFineString() {
        for (String message : Values.getRandomStrings(10)) {
            VerifyingLogger.configure(LogLevel.FINE, message);
            Logger.fine(message);
        }
    }

    @Test
    public void testFinerString() {
        for (String message : Values.getRandomStrings(10)) {
            VerifyingLogger.configure(LogLevel.FINER, message);
            Logger.finer(message);
        }
    }

    @Test
    public void testFinestString() {
        for (String message : Values.getRandomStrings(10)) {
            VerifyingLogger.configure(LogLevel.FINEST, message);
            Logger.finest(message);
        }
    }

    @Test
    public void testSevereStringThrowable() {
        for (String message : Values.getRandomStrings(20)) {
            Exception exc = Values.getRandomBoolean() ? null : new Exception();
            VerifyingLogger.configure(LogLevel.SEVERE, message, exc);
            Logger.severe(message, exc);
        }
    }

    @Test
    public void testWarningStringThrowable() {
        for (String message : Values.getRandomStrings(20)) {
            Exception exc = Values.getRandomBoolean() ? null : new Exception();
            VerifyingLogger.configure(LogLevel.WARNING, message, exc);
            Logger.warning(message, exc);
        }
    }

    @Test
    public void testInfoStringThrowable() {
        for (String message : Values.getRandomStrings(20)) {
            Exception exc = Values.getRandomBoolean() ? null : new Exception();
            VerifyingLogger.configure(LogLevel.INFO, message, exc);
            Logger.info(message, exc);
        }
    }

    @Test
    public void testConfigStringThrowable() {
        for (String message : Values.getRandomStrings(20)) {
            Exception exc = Values.getRandomBoolean() ? null : new Exception();
            VerifyingLogger.configure(LogLevel.CONFIG, message, exc);
            Logger.config(message, exc);
        }
    }

    @Test
    public void testFineStringThrowable() {
        for (String message : Values.getRandomStrings(20)) {
            Exception exc = Values.getRandomBoolean() ? null : new Exception();
            VerifyingLogger.configure(LogLevel.FINE, message, exc);
            Logger.fine(message, exc);
        }
    }

    @Test
    public void testFinerStringThrowable() {
        for (String message : Values.getRandomStrings(20)) {
            Exception exc = Values.getRandomBoolean() ? null : new Exception();
            VerifyingLogger.configure(LogLevel.FINER, message, exc);
            Logger.finer(message, exc);
        }
    }

    @Test
    public void testFinestStringThrowable() {
        for (String message : Values.getRandomStrings(20)) {
            Exception exc = Values.getRandomBoolean() ? null : new Exception();
            VerifyingLogger.configure(LogLevel.FINEST, message, exc);
            Logger.finest(message, exc);
        }
    }

    // NULLS

    @Test(expected = NullPointerException.class)
    public void testSevereStringNull() {
        Logger.severe(null);
    }

    @Test(expected = NullPointerException.class)
    public void testWarningStringNull() {
        Logger.warning(null);
    }

    @Test(expected = NullPointerException.class)
    public void testInfoStringNull() {
        Logger.info(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConfigStringNull() {
        Logger.config(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFineStringNull() {
        Logger.fine(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFinerStringNull() {
        Logger.finer(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFinestStringNull() {
        Logger.finest(null);
    }

    @Test(expected = NullPointerException.class)
    public void testSevereStringThrowableNull() {
        Logger.severe(null, new Exception());
    }

    @Test(expected = NullPointerException.class)
    public void testWarningStringThrowableNull() {
        Logger.warning(null, new Exception());
    }

    @Test(expected = NullPointerException.class)
    public void testInfoStringThrowableNull() {
        Logger.info(null, new Exception());
    }

    @Test(expected = NullPointerException.class)
    public void testConfigStringThrowableNull() {
        Logger.config(null, new Exception());
    }

    @Test(expected = NullPointerException.class)
    public void testFineStringThrowableNull() {
        Logger.fine(null, new Exception());
    }

    @Test(expected = NullPointerException.class)
    public void testFinerStringThrowableNull() {
        Logger.finer(null, new Exception());
    }

    @Test(expected = NullPointerException.class)
    public void testFinestStringThrowableNull() {
        Logger.finest(null, new Exception());
    }
}
