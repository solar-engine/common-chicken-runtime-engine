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

    public void event() {
        eventCalled++;
    }
}
