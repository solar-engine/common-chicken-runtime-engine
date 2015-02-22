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

import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;

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
        status.unsend(b);
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
        status.unsend(b);
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
        status.unsend(b);
        status.set(false);
        assertFalse(cur[0], "Expected no write once removed!");
        assertTrue(cur[1], "Expected write!");
        cur[1] = false;
        status.unsend(b2);
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
        EventOutput st = status.getSetTrueEvent();
        EventOutput sf = status.getSetFalseEvent();
        EventOutput tg = status.getToggleEvent();
        assertFalse(cur[0], "Expected no write when getting events!");
        st.event();
        assertTrue(cur[0], "Expected write!");
        st.event();
        assertTrue(cur[0], "Expected write!");
        sf.event();
        assertFalse(cur[0], "Expected write!");
        sf.event();
        assertFalse(cur[0], "Expected write!");
        tg.event();
        assertTrue(cur[0], "Expected write!");
        tg.event();
        assertFalse(cur[0], "Expected write!");
        EventStatus st2 = new EventStatus(), sf2 = new EventStatus(), tg2 = new EventStatus();
        status.setTrueWhen(st2);
        status.setFalseWhen(sf2);
        status.toggleWhen(tg2);
        assertFalse(cur[0], "Expected no write!");
        st2.event();
        assertTrue(cur[0], "Expected write!");
        sf2.event();
        assertFalse(cur[0], "Expected write!");
        tg2.event();
        assertTrue(cur[0], "Expected write!");
        tg2.event();
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
