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
package ccre.testing;

import ccre.channel.EventOutput;

/**
 * An EventOutput that can be used when a test needs to check that an event did
 * or did not happen, including the number of times that it did or did not
 * happen.
 *
 * If an unexpected event is received (dictated by {@link #ifExpected}), an
 * exception will be thrown. If an expected event is received, ifExpected will
 * be set to false.
 *
 * The other side of the equation is handled by {@link #check()}, which throws
 * an exception if ifExpected is true (i.e. an event hasn't happened.)
 *
 * @author skeggsc
 */
public class CountingEventOutput implements EventOutput {

    /**
     * If an event should be expected, this should be true.
     *
     * If an event is received
     */
    public boolean ifExpected;

    private boolean anyUnexpected;

    public synchronized void event() {
        if (!ifExpected) {
            anyUnexpected = true;
            throw new RuntimeException("Unexpected event");
        }
        ifExpected = false;
    }

    /**
     * Ensure than an event has happened since ifExpected was last set to true.
     *
     * @throws RuntimeException if an event did not occur.
     */
    public synchronized void check() throws RuntimeException {
        if (ifExpected) {
            throw new RuntimeException("Event did not occur");
        }
        if (anyUnexpected) {
            throw new RuntimeException("Unexpected event");
        }
    }
}