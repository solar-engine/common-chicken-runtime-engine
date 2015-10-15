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

import ccre.testing.CountingEventOutput;

public class EventStatusTest {

    private EventStatus status;

    @Before
    public void setUp() throws Exception {
        status = new EventStatus();
    }

    @After
    public void tearDown() throws Exception {
        status = null;
    }

    @Test
    public void testEventStatus() {
        new EventStatus().event();
    }

    @Test
    public void testEventStatusEventOutput() {
        CountingEventOutput ceo = new CountingEventOutput();
        status = new EventStatus(ceo);
        ceo.ifExpected = true;
        status.event();
        ceo.check();
    }

    @Test(expected = NullPointerException.class)
    public void testEventStatusEventOutputNull() {
        new EventStatus((EventOutput) null);
    }

    @Test
    public void testEventStatusEventOutputArray() {
        CountingEventOutput c1 = new CountingEventOutput();
        CountingEventOutput c2 = new CountingEventOutput();
        status = new EventStatus(c1, c2);
        c1.ifExpected = true;
        c2.ifExpected = true;
        status.event();
        c1.check();
        c2.check();
    }

    @Test(expected = NullPointerException.class)
    public void testEventStatusEventOutputArrayNull() {
        new EventStatus((EventOutput[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testEventStatusEventOutputArrayNulls() {
        new EventStatus(EventOutput.ignored, null);
    }

    @Test
    public void testHasConsumersInitSingle() {
        assertTrue(new EventStatus(EventOutput.ignored).hasListeners());
    }

    @Test
    public void testHasConsumersInitMany() {
        assertTrue(new EventStatus(EventOutput.ignored, EventOutput.ignored).hasListeners());
    }

    @Test
    public void testHasConsumersLingering() {
        assertFalse(status.hasListeners());
        status.onUpdate(EventOutput.ignored);
        assertTrue(status.hasListeners());
        EventOutput unbind = status.onUpdateR(EventOutput.ignored);
        assertTrue(status.hasListeners());
        unbind.event();
        assertTrue(status.hasListeners());// should STILL have one left
    }

    @Test
    public void testHasConsumers() {
        assertFalse(status.hasListeners());
        EventOutput unbind1 = status.onUpdateR(EventOutput.ignored);
        assertTrue(status.hasListeners());
        EventOutput unbind2 = status.onUpdateR(EventOutput.ignored);
        assertTrue(status.hasListeners());
        unbind1.event();
        assertTrue(status.hasListeners());// should STILL have one left
        unbind2.event();
        assertFalse(status.hasListeners());
    }

    @Test
    public void testOnUpdate() {
        CountingEventOutput ceo = new CountingEventOutput();
        status.onUpdate(ceo);
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = true;
            status.event();
            ceo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateNull() {
        status.onUpdate(null);
    }

    @Test
    public void testOnUpdateR() {
        CountingEventOutput ceo = new CountingEventOutput();
        for (int i = 0; i < 4; i++) {
            EventOutput unbind = status.onUpdateR(ceo);
            for (int j = 0; j < 10; j++) {
                ceo.ifExpected = true;
                status.event();
                ceo.check();
            }
            unbind.event();
            for (int j = 0; j < 10; j++) {
                status.event();// expect no events after unbinding
            }
            ceo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateRNull() {
        status.onUpdateR(null);
    }

    @Test
    public void testEvent() {
        CountingEventOutput[] cs = new CountingEventOutput[10];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = new CountingEventOutput();
        }
        EventOutput[] unbinds = new EventOutput[cs.length];
        for (int i = 0; i < cs.length; i++) {
            unbinds[i] = status.onUpdateR(cs[i]);
        }
        for (int r = 0; r < 5; r++) {
            for (int i = 0; i < cs.length; i++) {
                cs[i].ifExpected = true;
            }
            status.event();
            for (int i = 0; i < cs.length; i++) {
                cs[i].check();
            }
        }
        for (int i = 0; i < cs.length; i += 2) {
            unbinds[i].event();
            unbinds[i] = null;
        }
        for (int r = 0; r < 5; r++) {
            for (int i = 0; i < cs.length; i++) {
                cs[i].ifExpected = (unbinds[i] != null);
            }
            status.event();
            for (int i = 0; i < cs.length; i++) {
                cs[i].check();
            }
        }
        for (int i = 1; i < cs.length; i += 2) {
            unbinds[i].event();
            unbinds[i] = null;
        }
        status.event();
        for (int i = 0; i < cs.length; i++) {
            cs[i].check();
        }
    }

    @Test
    public void testClearListeners() {
        CountingEventOutput[] cs = new CountingEventOutput[10];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = new CountingEventOutput();
            status.onUpdate(cs[i]);
        }
        for (int r = 0; r < 5; r++) {
            for (int i = 0; i < cs.length; i++) {
                cs[i].ifExpected = true;
            }
            status.event();
            for (int i = 0; i < cs.length; i++) {
                cs[i].check();
            }
        }
        status.__UNSAFE_clearListeners();
        for (int r = 0; r < 5; r++) {
            status.event();
            for (int i = 0; i < cs.length; i++) {
                cs[i].check();
            }
        }
        CountingEventOutput tempc = new CountingEventOutput();
        status.onUpdate(tempc);
        tempc.ifExpected = true;
        status.event();
        tempc.check();
    }

    @Test
    public void testAsOutput() {
        assertEquals(status, status.asOutput());
    }

    @Test
    public void testAsInput() {
        assertEquals(status, status.asInput());
    }
}
