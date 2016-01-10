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

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class VerifyingLoggerTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        VerifyingLogger.begin();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        VerifyingLogger.end();
    }

    @Test
    public void testString() {
        VerifyingLogger.configure(LogLevel.FINEST, "hello world");
        Logger.finest("hello world");
        VerifyingLogger.check();
    }

    @Test
    public void testThrowable() {
        Exception exp = new Exception();
        VerifyingLogger.configure(LogLevel.FINEST, "hello world", exp);
        Logger.finest("hello world", exp);
        VerifyingLogger.check();
    }
    
    @Test
    public void testStringFailure() {
        boolean pass = false;
        VerifyingLogger.configure(LogLevel.FINEST, "hello world");
        try {
            VerifyingLogger.check();
        } catch (AssertionError ex) {
            pass = true;
        }
        assertTrue(pass);
    }
    
    @Test
    public void testThrowableFailure() {
        boolean pass = false;
        VerifyingLogger.configure(LogLevel.FINEST, "hello world");
        try {
            VerifyingLogger.check();
        } catch (AssertionError ex) {
            pass = true;
        }
        assertTrue(pass);
    }
}
