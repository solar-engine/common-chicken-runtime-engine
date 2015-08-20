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

import ccre.channel.EventOutput;
import ccre.channel.EventStatus;

/**
 * A test that tests some parts of the Event class.
 *
 * @author skeggsc
 */
public final class TestEventStatus extends BaseTest implements EventOutput {

    private int eventCalled = -42;

    @Override
    public String getName() {
        return "Event Testing";
    }

    @Override
    protected void runTest() throws TestingException {
        basicTest();
        testSingleParameter();
        testConsumerTracking();
    }

    private void basicTest() throws TestingException {
        EventStatus event = new EventStatus();
        eventCalled = 0;
        EventOutput unbind = event.sendR(this);
        assertIntsEqual(eventCalled, 0, "Event fired too soon!");
        event.produce();
        assertIntsEqual(eventCalled, 1, "Event did not fire properly!");
        unbind.event();
        event.produce();
        assertIntsEqual(eventCalled, 1, "Event did not remove properly!");
    }

    private void testSingleParameter() throws TestingException {
        EventStatus event = new EventStatus(this);
        eventCalled = 0;
        assertIntsEqual(eventCalled, 0, "Event fired too soon!");
        event.produce();
        assertIntsEqual(eventCalled, 1, "Event did not fire properly!");
    }

    private void testConsumerTracking() throws TestingException {
        EventStatus target = new EventStatus();
        assertFalse(target.hasConsumers(), "Target should not have consumers initially!");
        EventOutput unbind = target.sendR(EventOutput.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        unbind.event();
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        EventOutput unbind1 = target.sendR(EventOutput.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        EventOutput unbind2 = target.sendR(EventOutput.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind2.event();
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind1.event();
        assertFalse(target.hasConsumers(), "Target should not have consumers!");

        target = new EventStatus(EventOutput.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind = target.sendR(EventOutput.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind.event();
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind1 = target.sendR(EventOutput.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind2 = target.sendR(EventOutput.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind2.event();
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind1.event();
        assertTrue(target.hasConsumers(), "Target should still have consumers!");

        target = new EventStatus(EventOutput.ignored, EventOutput.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind = target.sendR(EventOutput.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind.event();
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind1 = target.sendR(EventOutput.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind2 = target.sendR(EventOutput.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind2.event();
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        unbind1.event(); // should not fail
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
    }

    public void event() {
        eventCalled++;
    }
}
