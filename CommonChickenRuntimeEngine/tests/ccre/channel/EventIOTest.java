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
package ccre.channel;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EventIOTest {

    private EventIO io;

    @Before
    public void setUp() throws Exception {
        io = new EventIO() {
            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                fail();
                return null;
            }

            @Override
            public void event() {
                fail();
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        io = null;
    }

    @Test
    public void testAsOutput() {
        assertEquals(io, io.asOutput());
    }

    @Test
    public void testAsInput() {
        assertEquals(io, io.asInput());
    }
}
