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

import ccre.chan.BooleanOutput;
import ccre.chan.BooleanStatus;
import ccre.event.Event;
import ccre.event.EventConsumer;

/**
 * Test BooleanStatus.
 *
 * @author skeggsc
 */
public class TestBooleanStatus extends BaseTest {

    @Override
    public String getName() {
        return "BooleanStatus tests";
    }

    /**
     * Test if basic reads and writes in various sequences work properly.
     *
     * @throws ccre.testing.TestingException If the test fails.
     */
    protected void testBasicReadWrite() throws TestingException {
        BooleanStatus status = new BooleanStatus();
        assertFalse(status.get(), "Bad default value!");
        status.set(true);
        assertTrue(status.get(), "Bad value!");
        status.set(true);
        assertTrue(status.get(), "Bad value!");
        status.set(false);
        assertFalse(status.get(), "Bad value!");
        status.set(false);
        assertFalse(status.get(), "Bad value!");
        status.set(true);
        assertTrue(status.get(), "Bad value!");
        status.set(false);
        assertFalse(status.get(), "Bad value!");
    }

    /**
     * Check if the BooleanStatus updates its targets properly.
     *
     * @throws TestingException If the test fails.
     */
    protected void testUpdateTargets() throws TestingException {
        BooleanStatus status = new BooleanStatus();
        final boolean[] cur = new boolean[2];
        BooleanOutput b = new BooleanOutput() {
            public void set(boolean value) {
                cur[0] = value;
                cur[1] = true;
            }
        };
        status.send(b);
        assertTrue(cur[1], "Current value not written!");
        assertFalse(cur[0], "Initial value bad!");
        cur[1] = false;
        status.set(false);
        assertFalse(cur[1], "Expected no write for the same value!");
        status.set(true);
        assertTrue(cur[1], "Expected write when value modified!");
        assertTrue(cur[0], "Expected write of true!");
        cur[1] = false;
        status.set(true);
        assertFalse(cur[1], "Expected no write for the same value!");
        status.set(false);
        assertTrue(cur[1], "Expected write when value modified!");
        assertFalse(cur[0], "Expected write of false!");
        cur[1] = false;
        assertTrue(status.unsend(b), "Expected existing subscription!");
        assertFalse(status.unsend(b), "Expected no subscription!");
        status.set(true);
        status.set(false);
        assertFalse(cur[1], "Expected no write after removal!");
    }

    /**
     * Check if targets added during creation work properly.
     *
     * @throws TestingException If the test fails.
     */
    protected void testCreationTargets() throws TestingException {
        final boolean[] cur = new boolean[2];
        BooleanOutput b = new BooleanOutput() {
            public void set(boolean value) {
                cur[0] = true;
            }
        };
        BooleanStatus status = new BooleanStatus(b);
        assertTrue(cur[0], "Expected write when added!");
        cur[0] = false;
        status.set(true);
        assertTrue(cur[0], "Expected write!");
        cur[0] = false;
        assertTrue(status.unsend(b), "Expected subscription!");
        status.set(false);
        assertFalse(cur[0], "Expected no write once removed!");

        BooleanOutput b2 = new BooleanOutput() {
            public void set(boolean value) {
                cur[1] = true;
            }
        };
        status = new BooleanStatus(b, b2);
        assertTrue(cur[0], "Expected write when added!");
        assertTrue(cur[1], "Expected write when added!");
        cur[0] = false;
        cur[1] = false;
        status.set(true);
        assertTrue(cur[0], "Expected write!");
        assertTrue(cur[1], "Expected write!");
        cur[0] = false;
        cur[1] = false;
        assertTrue(status.unsend(b), "Expected subscription!");
        assertFalse(status.unsend(b), "Expected no subscription!");
        status.set(false);
        assertFalse(cur[0], "Expected no write once removed!");
        assertTrue(cur[1], "Expected write!");
        cur[1] = false;
        assertTrue(status.unsend(b2), "Expected subscription!");
        assertFalse(status.unsend(b2), "Expected no subscription!");
        status.set(true);
        assertFalse(cur[0], "Expected no write once removed!");
        assertFalse(cur[1], "Expected no write once removed!");
    }

    /**
     * Test if setting events work properly.
     *
     * @throws TestingException If the test fails.
     */
    protected void testSetEvents() throws TestingException {
        final boolean[] cur = new boolean[1];
        BooleanOutput b = new BooleanOutput() {
            public void set(boolean value) {
                cur[0] = value;
            }
        };
        final BooleanStatus status = new BooleanStatus(b);
        assertFalse(cur[0], "Expected false default!");
        EventConsumer st = status.getSetTrueEvent();
        EventConsumer sf = status.getSetFalseEvent();
        EventConsumer tg = status.getToggleEvent();
        assertFalse(cur[0], "Expected no write when getting events!");
        st.eventFired();
        assertTrue(cur[0], "Expected write!");
        st.eventFired();
        assertTrue(cur[0], "Expected write!");
        sf.eventFired();
        assertFalse(cur[0], "Expected write!");
        sf.eventFired();
        assertFalse(cur[0], "Expected write!");
        tg.eventFired();
        assertTrue(cur[0], "Expected write!");
        tg.eventFired();
        assertFalse(cur[0], "Expected write!");
        Event st2 = new Event(), sf2 = new Event(), tg2 = new Event();
        status.setTrueWhen(st2);
        status.setFalseWhen(sf2);
        status.toggleWhen(tg2);
        assertFalse(cur[0], "Expected no write!");
        st2.eventFired();
        assertTrue(cur[0], "Expected write!");
        sf2.eventFired();
        assertFalse(cur[0], "Expected write!");
        tg2.eventFired();
        assertTrue(cur[0], "Expected write!");
        tg2.eventFired();
        assertFalse(cur[0], "Expected write!");
    }

    @Override
    protected void runTest() throws TestingException {
        testBasicReadWrite();
        testUpdateTargets();
        testCreationTargets();
        testSetEvents();
    }
}
