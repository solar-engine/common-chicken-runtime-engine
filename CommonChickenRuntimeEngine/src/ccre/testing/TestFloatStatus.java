/*
 * Copyright 2013-2015 Colby Skeggs
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

import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.ctrl.FloatMixing;

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
        assertObjectEqual(status.get(), 0.0f, "Bad default value!");
        status.set(1.7f);
        assertObjectEqual(status.get(), 1.7f, "Bad value!");
        status.set(1.7f);
        assertObjectEqual(status.get(), 1.7f, "Bad value!");
        status.set(-1.0f);
        assertObjectEqual(status.get(), -1.0f, "Bad value!");
        status.set(-1.0f);
        assertObjectEqual(status.get(), -1.0f, "Bad value!");
        status.set(3.6f);
        assertObjectEqual(status.get(), 3.6f, "Bad value!");
        status.set(-89.2f);
        assertObjectEqual(status.get(), -89.2f, "Bad value!");
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
            public void set(float value) {
                cur[0] = value;
                c2[0] = true;
            }
        };
        status.send(b);
        assertTrue(c2[0], "Current value not written!");
        assertObjectEqual(cur[0], 0.0f, "Initial value bad!");
        c2[0] = false;
        status.set(0.0f);
        assertFalse(c2[0], "Expected no write for the same value!");
        status.set(0.1f);
        assertTrue(c2[0], "Expected write when value modified!");
        assertObjectEqual(cur[0], 0.1f, "Expected write of 0.1f!");
        c2[0] = false;
        status.set(0.1f);
        assertFalse(c2[0], "Expected no write for the same value!");
        status.set(-4.6f);
        assertTrue(c2[0], "Expected write when value modified!");
        assertObjectEqual(cur[0], -4.6f, "Expected write of -4.6f!");
        c2[0] = false;
        status.unsend(b);
        status.set(1.8f);
        status.set(0.0f);
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
            public void set(float value) {
                c1[0] = true;
            }
        };
        FloatStatus status = new FloatStatus(b);
        assertTrue(c1[0], "Expected write when added!");
        c1[0] = false;
        status.set(1.8f);
        assertTrue(c1[0], "Expected write!");
        c1[0] = false;
        status.unsend(b);
        status.set(-3.2f);
        assertFalse(c1[0], "Expected no write once removed!");

        FloatOutput b2 = new FloatOutput() {
            public void set(float value) {
                c1[1] = true;
            }
        };
        status = new FloatStatus(b, b2);
        assertTrue(c1[0], "Expected write when added!");
        assertTrue(c1[1], "Expected write when added!");
        c1[0] = false;
        c1[1] = false;
        status.set(123.4f);
        assertTrue(c1[0], "Expected write!");
        assertTrue(c1[1], "Expected write!");
        c1[0] = false;
        c1[1] = false;
        status.unsend(b);
        status.set(-0.002f);
        assertFalse(c1[0], "Expected no write once removed!");
        assertTrue(c1[1], "Expected write!");
        c1[1] = false;
        status.unsend(b2);
        status.set(3.6f);
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
            public void set(float value) {
                cur[0] = value;
            }
        };
        final FloatStatus status = new FloatStatus(b);
        assertObjectEqual(cur[0], 0.0f, "Expected false default!");
        EventOutput st1_7f = status.getSetEvent(1.7f);
        assertObjectEqual(cur[0], 0.0f, "Expected no write when getting events!");
        st1_7f.event();
        assertObjectEqual(cur[0], 1.7f, "Expected write!");
        st1_7f.event();
        assertObjectEqual(cur[0], 1.7f, "Expected write!");
        EventStatus sp1_7f = new EventStatus();
        status.setWhen(-8.2f, sp1_7f);
        assertObjectEqual(cur[0], 1.7f, "Expected no write!");
        sp1_7f.event();
        assertObjectEqual(cur[0], -8.2f, "Expected write!");
    }

    private void testConsumerTracking() throws TestingException {
        FloatStatus target = new FloatStatus();
        assertFalse(target.hasConsumers(), "Target should not have consumers initially!");
        target.send(FloatMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        target.unsend(FloatMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.send(FloatMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        target.send(FloatMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        target.unsend(FloatMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.unsend(FloatMixing.ignored); // should not fail
        assertFalse(target.hasConsumers(), "Target should still not have consumers!");

        target = new FloatStatus(FloatMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should have consumers initially!");
        target.unsend(FloatMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.send(FloatMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        target.unsend(FloatMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.send(FloatMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        target.send(FloatMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        target.unsend(FloatMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.unsend(FloatMixing.ignored); // should not fail
        assertFalse(target.hasConsumers(), "Target should still not have consumers!");

        target = new FloatStatus(FloatMixing.ignored, FloatMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should have consumers initially!");
        target.unsend(FloatMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.send(FloatMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        target.unsend(FloatMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.send(FloatMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        target.send(FloatMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        target.unsend(FloatMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.unsend(FloatMixing.ignored); // should not fail
        assertFalse(target.hasConsumers(), "Target should still not have consumers!");
    }

    private void testIOConversions() throws TestingException {
        FloatStatus test = new FloatStatus();
        // Guaranteed by documentation to be exactly the same object.
        assertIdentityEqual(test, test.asInput(), "test should be its own input!");
        assertIdentityEqual(test, test.asOutput(), "test should be its own output!");
    }

    @Override
    protected void runTest() throws TestingException {
        testBasicReadWrite();
        testUpdateTargets();
        testCreationTargets();
        testSetEvents();
        testConsumerTracking();
        testIOConversions();
    }
}
