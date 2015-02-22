/*
 * Copyright 2013-2014 Colby Skeggs
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
package ccre.testing;

import java.util.Iterator;

import ccre.concurrency.ConcurrentDispatchArray;

/**
 * A test that tests some parts of the ConcurrentDispatchArray class.
 *
 * @author skeggsc
 */
public class TestConcurrentDispatchArray extends BaseTest {

    @Override
    public String getName() {
        return "ConcurrentDispatchArray basic test";
    }

    @Override
    protected void runTest() throws TestingException {
        ConcurrentDispatchArray<Object> arr = new ConcurrentDispatchArray<Object>();
        Object test = new Object();
        assertFalse(arr.contains(test), "Bad contains!");
        arr.add(test);
        assertTrue(arr.contains(test), "Bad contains!");
        Iterator<Object> itr = arr.iterator();
        assertTrue(itr.hasNext(), "Bad iterator!");
        assertIdentityEqual(itr.next(), test, "Bad iterator!");
        assertFalse(itr.hasNext(), "Bad iterator!");
        arr.remove(test);
        assertFalse(arr.contains(test), "Bad remove!");
    }
}
