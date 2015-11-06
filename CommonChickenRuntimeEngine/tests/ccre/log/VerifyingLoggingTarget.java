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
package ccre.log;

import static org.junit.Assert.*;

@SuppressWarnings("javadoc")
public class VerifyingLoggingTarget implements LoggingTarget {

    public boolean ifExpected;
    public LogLevel levelExpected = LogLevel.SEVERE;
    public String messageExpected = null;
    public boolean isThrowableExpected = false;
    public Throwable throwableExpected = null;
    public String stringExpected = null;
    private boolean hasFailure = false;

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        try {
            assertNotNull(level);
            assertNotNull(message);
            assertTrue(ifExpected);
            assertTrue(isThrowableExpected);
            assertEquals(levelExpected, level);
            assertEquals(messageExpected, message);
            assertEquals(throwableExpected, throwable);
        } catch (Throwable thr) {
            hasFailure = true;
            throw thr;
        }
        ifExpected = false;
    }

    @Override
    public void log(LogLevel level, String message, String extended) {
        try {
            assertNotNull(level);
            assertNotNull(message);
            assertTrue(ifExpected);
            assertFalse(isThrowableExpected);
            assertEquals(levelExpected, level);
            assertEquals(messageExpected, message);
            assertEquals(stringExpected, extended);
        } catch (Throwable thr) {
            hasFailure = true;
            throw thr;
        }
        ifExpected = false;
    }

    public void check() {
        assertFalse("Did not receive expected log.", ifExpected);
        assertFalse("Failed during individual log reception.", hasFailure);
    }
}
