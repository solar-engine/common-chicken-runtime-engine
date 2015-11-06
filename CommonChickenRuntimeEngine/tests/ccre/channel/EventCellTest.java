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

@SuppressWarnings("javadoc")
public class EventCellTest {

    private EventCell cell;

    @Before
    public void setUp() throws Exception {
        cell = new EventCell();
    }

    @After
    public void tearDown() throws Exception {
        cell = null;
    }

    @Test
    public void testEventStatus() {
        new EventCell().event();
    }

    @Test
    public void testEventStatusEventOutput() {
        CountingEventOutput ceo = new CountingEventOutput();
        cell = new EventCell(ceo);
        ceo.ifExpected = true;
        cell.event();
        ceo.check();
    }

    @Test(expected = NullPointerException.class)
    public void testEventStatusEventOutputNull() {
        new EventCell((EventOutput) null);
    }

    @Test
    public void testEventStatusEventOutputArray() {
        CountingEventOutput c1 = new CountingEventOutput();
        CountingEventOutput c2 = new CountingEventOutput();
        cell = new EventCell(c1, c2);
        c1.ifExpected = true;
        c2.ifExpected = true;
        cell.event();
        c1.check();
        c2.check();
    }

    @Test(expected = NullPointerException.class)
    public void testEventStatusEventOutputArrayNull() {
        new EventCell((EventOutput[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testEventStatusEventOutputArrayNulls() {
        new EventCell(EventOutput.ignored, null);
    }

    @Test
    public void testHasConsumersInitSingle() {
        assertTrue(new EventCell(EventOutput.ignored).hasListeners());
    }

    @Test
    public void testHasConsumersInitMany() {
        assertTrue(new EventCell(EventOutput.ignored, EventOutput.ignored).hasListeners());
    }

    @Test
    public void testHasConsumersLingering() {
        assertFalse(cell.hasListeners());
        cell.onUpdate(EventOutput.ignored);
        assertTrue(cell.hasListeners());
        EventOutput unbind = cell.onUpdate(EventOutput.ignored);
        assertTrue(cell.hasListeners());
        unbind.event();
        assertTrue(cell.hasListeners());// should STILL have one left
    }

    @Test
    public void testHasConsumers() {
        assertFalse(cell.hasListeners());
        EventOutput unbind1 = cell.onUpdate(EventOutput.ignored);
        assertTrue(cell.hasListeners());
        EventOutput unbind2 = cell.onUpdate(EventOutput.ignored);
        assertTrue(cell.hasListeners());
        unbind1.event();
        assertTrue(cell.hasListeners());// should STILL have one left
        unbind2.event();
        assertFalse(cell.hasListeners());
    }

    @Test
    public void testOnUpdate() {
        CountingEventOutput ceo = new CountingEventOutput();
        cell.onUpdate(ceo);
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = true;
            cell.event();
            ceo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateNull() {
        cell.onUpdate(null);
    }

    @Test
    public void testOnUpdateR() {
        CountingEventOutput ceo = new CountingEventOutput();
        for (int i = 0; i < 4; i++) {
            EventOutput unbind = cell.onUpdate(ceo);
            for (int j = 0; j < 10; j++) {
                ceo.ifExpected = true;
                cell.event();
                ceo.check();
            }
            unbind.event();
            for (int j = 0; j < 10; j++) {
                cell.event();// expect no events after unbinding
            }
            ceo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateRNull() {
        cell.onUpdate(null);
    }

    @Test
    public void testEvent() {
        CountingEventOutput[] cs = new CountingEventOutput[10];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = new CountingEventOutput();
        }
        EventOutput[] unbinds = new EventOutput[cs.length];
        for (int i = 0; i < cs.length; i++) {
            unbinds[i] = cell.onUpdate(cs[i]);
        }
        for (int r = 0; r < 5; r++) {
            for (int i = 0; i < cs.length; i++) {
                cs[i].ifExpected = true;
            }
            cell.event();
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
            cell.event();
            for (int i = 0; i < cs.length; i++) {
                cs[i].check();
            }
        }
        for (int i = 1; i < cs.length; i += 2) {
            unbinds[i].event();
            unbinds[i] = null;
        }
        cell.event();
        for (int i = 0; i < cs.length; i++) {
            cs[i].check();
        }
    }

    @Test
    public void testClearListeners() {
        CountingEventOutput[] cs = new CountingEventOutput[10];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = new CountingEventOutput();
            cell.onUpdate(cs[i]);
        }
        for (int r = 0; r < 5; r++) {
            for (int i = 0; i < cs.length; i++) {
                cs[i].ifExpected = true;
            }
            cell.event();
            for (int i = 0; i < cs.length; i++) {
                cs[i].check();
            }
        }
        cell.__UNSAFE_clearListeners();
        for (int r = 0; r < 5; r++) {
            cell.event();
            for (int i = 0; i < cs.length; i++) {
                cs[i].check();
            }
        }
        CountingEventOutput tempc = new CountingEventOutput();
        cell.onUpdate(tempc);
        tempc.ifExpected = true;
        cell.event();
        tempc.check();
    }
}
