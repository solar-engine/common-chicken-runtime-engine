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

import ccre.chan.FloatOutput;
import ccre.chan.FloatStatus;
import ccre.event.Event;
import ccre.event.EventConsumer;

/**
 * Test FloatStatus.
 *
 * @author skeggsc
 */
public class TestFloatStatus extends BaseTest {

    @Override
    public String getName() {
        return "FloatStatus tests";
    }

    /**
     * Test if basic reads and writes work properly.
     *
     * @throws TestingException If the test fails.
     */
    protected void testBasicReadWrite() throws TestingException {
        FloatStatus status = new FloatStatus();
        assertEqual(status.readValue(), 0.0f, "Bad default value!");
        status.writeValue(1.7f);
        assertEqual(status.readValue(), 1.7f, "Bad value!");
        status.writeValue(1.7f);
        assertEqual(status.readValue(), 1.7f, "Bad value!");
        status.writeValue(-1.0f);
        assertEqual(status.readValue(), -1.0f, "Bad value!");
        status.writeValue(-1.0f);
        assertEqual(status.readValue(), -1.0f, "Bad value!");
        status.writeValue(3.6f);
        assertEqual(status.readValue(), 3.6f, "Bad value!");
        status.writeValue(-89.2f);
        assertEqual(status.readValue(), -89.2f, "Bad value!");
    }

    /**
     * Test if target updates work properly.
     *
     * @throws TestingException If the test fails.
     */
    protected void testUpdateTargets() throws TestingException {
        FloatStatus status = new FloatStatus();
        final float[] cur = new float[1];
        final boolean[] c2 = new boolean[1];
        FloatOutput b = new FloatOutput() {
            public void writeValue(float value) {
                cur[0] = value;
                c2[0] = true;
            }
        };
        status.addTarget(b);
        assertTrue(c2[0], "Current value not written!");
        assertEqual(cur[0], 0.0f, "Initial value bad!");
        c2[0] = false;
        status.writeValue(0.0f);
        assertFalse(c2[0], "Expected no write for the same value!");
        status.writeValue(0.1f);
        assertTrue(c2[0], "Expected write when value modified!");
        assertEqual(cur[0], 0.1f, "Expected write of 0.1f!");
        c2[0] = false;
        status.writeValue(0.1f);
        assertFalse(c2[0], "Expected no write for the same value!");
        status.writeValue(-4.6f);
        assertTrue(c2[0], "Expected write when value modified!");
        assertEqual(cur[0], -4.6f, "Expected write of -4.6f!");
        c2[0] = false;
        assertTrue(status.removeTarget(b), "Expected existing subscription!");
        assertFalse(status.removeTarget(b), "Expected no subscription!");
        status.writeValue(1.8f);
        status.writeValue(0.0f);
        assertFalse(c2[0], "Expected no write after removal!");
    }

    /**
     * Test if creation targets register and work properly.
     *
     * @throws TestingException If the test fails.
     */
    protected void testCreationTargets() throws TestingException {
        final boolean[] c1 = new boolean[2];
        FloatOutput b = new FloatOutput() {
            public void writeValue(float value) {
                c1[0] = true;
            }
        };
        FloatStatus status = new FloatStatus(b);
        assertTrue(c1[0], "Expected write when added!");
        c1[0] = false;
        status.writeValue(1.8f);
        assertTrue(c1[0], "Expected write!");
        c1[0] = false;
        assertTrue(status.removeTarget(b), "Expected subscription!");
        status.writeValue(-3.2f);
        assertFalse(c1[0], "Expected no write once removed!");

        FloatOutput b2 = new FloatOutput() {
            public void writeValue(float value) {
                c1[1] = true;
            }
        };
        status = new FloatStatus(b, b2);
        assertTrue(c1[0], "Expected write when added!");
        assertTrue(c1[1], "Expected write when added!");
        c1[0] = false;
        c1[1] = false;
        status.writeValue(123.4f);
        assertTrue(c1[0], "Expected write!");
        assertTrue(c1[1], "Expected write!");
        c1[0] = false;
        c1[1] = false;
        assertTrue(status.removeTarget(b), "Expected subscription!");
        assertFalse(status.removeTarget(b), "Expected no subscription!");
        status.writeValue(-0.002f);
        assertFalse(c1[0], "Expected no write once removed!");
        assertTrue(c1[1], "Expected write!");
        c1[1] = false;
        assertTrue(status.removeTarget(b2), "Expected subscription!");
        assertFalse(status.removeTarget(b2), "Expected no subscription!");
        status.writeValue(3.6f);
        assertFalse(c1[0], "Expected no write once removed!");
        assertFalse(c1[1], "Expected no write once removed!");
    }

    /**
     * Test if setting events work properly.
     *
     * @throws TestingException If the test fails.
     */
    protected void testSetEvents() throws TestingException {
        final float[] cur = new float[1];
        FloatOutput b = new FloatOutput() {
            public void writeValue(float value) {
                cur[0] = value;
            }
        };
        final FloatStatus status = new FloatStatus(b);
        assertEqual(cur[0], 0.0f, "Expected false default!");
        EventConsumer st1_7f = status.getSetEvent(1.7f);
        assertEqual(cur[0], 0.0f, "Expected no write when getting events!");
        st1_7f.eventFired();
        assertEqual(cur[0], 1.7f, "Expected write!");
        st1_7f.eventFired();
        assertEqual(cur[0], 1.7f, "Expected write!");
        Event sp1_7f = new Event();
        status.setWhen(-8.2f, sp1_7f);
        assertEqual(cur[0], 1.7f, "Expected no write!");
        sp1_7f.eventFired();
        assertEqual(cur[0], -8.2f, "Expected write!");
    }

    @Override
    protected void runTest() throws TestingException {
        testBasicReadWrite();
        testUpdateTargets();
        testCreationTargets();
        testSetEvents();
    }
}
