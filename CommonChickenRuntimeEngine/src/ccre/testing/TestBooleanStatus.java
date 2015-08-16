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

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.ctrl.BooleanMixing;

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
        EventOutput unbind = status.sendR(b);
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
        unbind.event();
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

    private void testConsumerTracking() throws TestingException {
        BooleanStatus target = new BooleanStatus();
        assertFalse(target.hasConsumers(), "Target should not have consumers initially!");
        EventOutput unbind = target.sendR(BooleanMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        unbind.event();
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        EventOutput unbind1 = target.sendR(BooleanMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        EventOutput unbind2 = target.sendR(BooleanMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind2.event();
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind1.event();
        assertFalse(target.hasConsumers(), "Target should still not have consumers!");

        target = new BooleanStatus(BooleanMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind = target.sendR(BooleanMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind.event();
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        target.send(BooleanMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind = target.sendR(BooleanMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind.event();
        assertTrue(target.hasConsumers(), "Target should still have consumers!");

        target = new BooleanStatus(BooleanMixing.ignored, BooleanMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind = target.sendR(BooleanMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind.event();
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        target.send(BooleanMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind = target.sendR(BooleanMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind.event();
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
    }

    private void testIOConversions() throws TestingException {
        BooleanStatus test = new BooleanStatus();
        // Guaranteed by documentation to be exactly the same object.
        assertIdentityEqual(test, test.asInput(), "test should be its own input!");
        assertIdentityEqual(test, test.asOutput(), "test should be its own output!");

        BooleanInput inverted = test.asInvertedInput();

        assertTrue(inverted.get() == !test.get(), "Expected initial condition to be correct!");
        // check the opposite initialization
        assertFalse(new BooleanStatus(true).asInvertedInput().get(), "Expected initial condition to be correct!");

        boolean[] testBools = new boolean[] { false, true, true, true, false, false, true, true, false, true, false, false, false, false, true, true, false };

        for (boolean b : testBools) {
            test.set(b);
            assertTrue(inverted.get() == !b, "Expected new condition to match.");
        }

        BooleanOutput invout = test.asInvertedOutput();

        for (boolean b : testBools) {
            invout.set(b);
            assertTrue(test.get() == !b, "Expected new condition to match.");
            assertTrue(inverted.get() == b, "Expected new condition to match.");
        }
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
