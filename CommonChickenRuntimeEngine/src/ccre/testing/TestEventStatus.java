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
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.EventMixing;

/**
 * A test that tests some parts of the Event class.
 *
 * @author skeggsc
 */
public final class TestEventStatus extends BaseTest implements EventOutput {

    private final class EventNothing implements EventOutput {
        public void event() {
            // do nothing
        }
    }

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
        testDeprecatedFeatures();
    }

    private void basicTest() throws TestingException {
        EventStatus event = new EventStatus();
        eventCalled = 0;
        event.send(this);
        assertIntsEqual(eventCalled, 0, "Event fired too soon!");
        event.produce();
        assertIntsEqual(eventCalled, 1, "Event did not fire properly!");
        event.unsend(this);
        event.produce();
        assertIntsEqual(eventCalled, 1, "Event did not remove properly!");
    }

    private void testSingleParameter() throws TestingException {
        EventStatus event = new EventStatus(this);
        eventCalled = 0;
        assertIntsEqual(eventCalled, 0, "Event fired too soon!");
        event.produce();
        assertIntsEqual(eventCalled, 1, "Event did not fire properly!");
        event.unsend(this);
        event.produce();
        assertIntsEqual(eventCalled, 1, "Event did not remove properly!");
    }

    private void testConsumerTracking() throws TestingException {
        EventStatus target = new EventStatus();
        assertFalse(target.hasConsumers(), "Target should not have consumers initially!");
        target.send(EventMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        target.unsend(EventMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.send(EventMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        target.send(EventMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        target.unsend(EventMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.unsend(EventMixing.ignored); // should not fail
        assertFalse(target.hasConsumers(), "Target should still not have consumers!");

        target = new EventStatus(EventMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should have consumers initially!");
        target.unsend(EventMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.send(EventMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        target.unsend(EventMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.send(EventMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        target.send(EventMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        target.unsend(EventMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.unsend(EventMixing.ignored); // should not fail
        assertFalse(target.hasConsumers(), "Target should still not have consumers!");

        target = new EventStatus(EventMixing.ignored, EventMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should have consumers initially!");
        target.unsend(EventMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.send(EventMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        target.unsend(EventMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.send(EventMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should now have consumers!");
        target.send(EventMixing.ignored);
        assertTrue(target.hasConsumers(), "Target should still have consumers!");
        target.unsend(EventMixing.ignored);
        assertFalse(target.hasConsumers(), "Target should no longer have consumers!");
        target.unsend(EventMixing.ignored); // should not fail
        assertFalse(target.hasConsumers(), "Target should still not have consumers!");
    }

    @SuppressWarnings("deprecation")
    private void testDeprecatedFeatures() throws TestingException {
        EventStatus target = new EventStatus();
        EventOutput[] nothings = new EventOutput[] { new EventNothing(), new EventNothing(), new EventNothing(), new EventNothing() };
        for (int i = 0; i < nothings.length; i++) {
            assertIntsEqual(target.countConsumers(), 0, "Wrong initial number of consumers!");
            for (int j = 0; j < i; j++) {
                EventOutput n = nothings[j];
                assertIntsEqual(j, target.countConsumers(), "Wrong number of consumers!");
                target.send(n);
                assertIntsEqual(j + 1, target.countConsumers(), "Wrong number of consumers!");
                target.send(n);
                assertIntsEqual(j + 1, target.countConsumers(), "Wrong number of consumers!");
                target.unsend(n);
                assertIntsEqual(j, target.countConsumers(), "Wrong number of consumers!");
                target.send(n);
                assertIntsEqual(j + 1, target.countConsumers(), "Wrong number of consumers!");
            }
            assertIntsEqual(target.countConsumers(), i, "Wrong intermediate number of consumers!");
            for (int j = i; j > 0; j--) {
                EventOutput n = nothings[j - 1];
                assertIntsEqual(j, target.countConsumers(), "Wrong number of consumers!");
                target.unsend(n);
                assertIntsEqual(j - 1, target.countConsumers(), "Wrong number of consumers!");
            }
            assertIntsEqual(target.countConsumers(), 0, "Wrong final number of consumers!");
        }
    }

    public void event() {
        eventCalled++;
    }
}
