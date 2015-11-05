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
package ccre.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CallerInfoTest {

    @Test
    public void testSimpleCallerInfo() {
        CallerInfo info = new CallerInfo("class", "method", "file", 10);
        assertEquals(info.getClassName(), "class");
        assertEquals(info.getMethodName(), "method");
        assertEquals(info.getFileName(), "file");
        assertEquals(info.getLineNum(), 10);
        assertEquals(info.toString(), "class.method(file:10)");
    }

    @Test
    public void testPartialCallerInfo() {
        CallerInfo info = new CallerInfo("class", null, null, -1);
        assertEquals(info.getClassName(), "class");
        assertEquals(info.getMethodName(), null);
        assertEquals(info.getFileName(), null);
        assertEquals(info.getLineNum(), -1);
    }

    @Test
    public void testToStringCallerInfo() {
        assertEquals(new CallerInfo("class", "method", "file", 10).toString(), "class.method(file:10)");
        assertEquals(new CallerInfo("class", null, null, -1).toString(), "class.<unknown>(<unknown>:?)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullClassCallerInfo() {
        new CallerInfo(null, "method", "file", 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCallerInfo() {
        new CallerInfo(null, null, null, -1);
    }
}
