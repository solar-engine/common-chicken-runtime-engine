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

import org.junit.Test;

@SuppressWarnings("javadoc")
public class LoggingTargetTest {

    @Test
    public void test() {
        LoggingTarget.ignored.log(LogLevel.FINEST, "hello", (String) null);
        LoggingTarget.ignored.log(LogLevel.FINEST, "hello", "hi");
        LoggingTarget.ignored.log(LogLevel.FINEST, "hello", (Throwable) null);
        LoggingTarget.ignored.log(LogLevel.FINEST, "hello", new Exception());
    }

    @Test(expected = NullPointerException.class)
    public void testNullA() {
        LoggingTarget.ignored.log(null, "hello", "hi");
    }

    @Test(expected = NullPointerException.class)
    public void testNullB() {
        LoggingTarget.ignored.log(null, "hello", new Exception());
    }

    @Test(expected = NullPointerException.class)
    public void testNullC() {
        LoggingTarget.ignored.log(LogLevel.FINEST, null, "hi");
    }

    @Test(expected = NullPointerException.class)
    public void testNullD() {
        LoggingTarget.ignored.log(LogLevel.FINEST, null, new Exception());
    }

}
