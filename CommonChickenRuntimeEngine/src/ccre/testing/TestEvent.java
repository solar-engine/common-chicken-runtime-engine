/*
 * Copyright 2013 Colby Skeggs
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

import ccre.event.Event;
import ccre.event.EventConsumer;

/**
 * A test that tests some parts of the Event class.
 *
 * @author skeggsc
 */
public final class TestEvent extends BaseTest implements EventConsumer {

    @Override
    public String getName() {
        return "Event Testing";
    }
    private int eventCalled = -42;

    @Override
    protected void runTest() throws TestingException {
        Event event = new Event();
        eventCalled = 0;
        assertTrue(event.addListener(this), "Event did not add properly!");
        assertEqual(eventCalled, 0, "Event fired too soon!");
        event.produce();
        assertEqual(eventCalled, 1, "Event did not fire properly!");
        event.removeListener(this);
        event.produce();
        assertEqual(eventCalled, 1, "Event did not remove properly!");
    }

    public void eventFired() {
        eventCalled++;
    }
}
