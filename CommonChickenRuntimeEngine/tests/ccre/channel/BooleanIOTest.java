/*
 * Copyright 2015-2016 Colby Skeggs
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingBooleanOutput;
import ccre.util.Values;

@SuppressWarnings("javadoc")
public class BooleanIOTest {

    private BooleanIO io;

    @Before
    public void setUp() throws Exception {
        this.io = new BooleanIO() {
            private boolean value;

            @Override
            public boolean get() {
                return value;
            }

            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                fail();
                return null;
            }

            @Override
            public void set(boolean value) {
                this.value = value;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        this.io = null;
    }

    @Test
    public void testAsOutput() {
        assertEquals(io, io.asOutput());
    }

    @Test
    public void testAsInput() {
        assertEquals(io, io.asInput());
    }

    @Test
    public void testToggleWhen() {
        EventCell toggleEvent = new EventCell();
        io.toggleWhen(toggleEvent);
        tryToggleEvent(toggleEvent);
    }

    @Test(expected = NullPointerException.class)
    public void testToggleWhenNull() {
        io.toggleWhen(null);
    }

    @Test
    public void testGetToggleEvent() {
        tryToggleEvent(io.eventToggle());
    }

    private void tryToggleEvent(EventOutput toggleEvent) {
        for (int i = 0; i < 20; i++) {
            boolean wanted = i % 2 == 0;
            io.set(wanted);
            assertEquals(wanted, io.get());
            for (int j = 0; j < 5; j++) {
                wanted = !wanted;
                toggleEvent.event();
                assertEquals(wanted, io.get());
            }
        }
    }

    @Test
    public void testCell() {
        BooleanIO c = new BooleanCell();
        CountingBooleanOutput cbo = new CountingBooleanOutput();
        cbo.ifExpected = true;
        cbo.valueExpected = false;
        c.send(cbo);
        cbo.check();
        for (boolean b : Values.interestingBooleans) {
            cbo.ifExpected = b != cbo.valueExpected;
            cbo.valueExpected = b;
            assertTrue(c.cell(b) == c);
            cbo.check();
        }
    }
}
