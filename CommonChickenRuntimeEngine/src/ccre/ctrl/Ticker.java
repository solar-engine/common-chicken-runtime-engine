/*
 * Copyright 2013-2014 Colby Skeggs and Vincent Miller
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
package ccre.ctrl;

import ccre.event.Event;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An EventSource that will fire the event in all its consumers at a specified
 * interval.
 *
 * @author MillerV
 */
public final class Ticker implements EventSource {

    private final Event producer = new Event(); // TODO: Make this inherit from something?

    /**
     * Create a new Ticker with the specified interval. The timer will start
     * immediately, executing for the first time after the specified interval.
     *
     * This will not run at a fixed rate, as extra time taken for one cycle will
     * not be corrected for in the time between the cycles.
     *
     * @param interval The desired interval, in milliseconds.
     */
    public Ticker(int interval) {
        this(interval, false);
    }

    /**
     * Create a new Ticker with the specified interval and fixed rate option.
     * The timer will start immediately, executing for the first time after the
     * specified interval.
     *
     * If fixedRate is false, this will not run at a fixed rate, as extra time
     * taken for one cycle will not be corrected for in the time between the
     * cycles.
     *
     * If fixedRate is true, this will run at a fixed rate, as extra time taken
     * for one cycle will be removed from the time before the subsequent cycle.
     * This does mean that if a cycle takes too long, that produces of the event
     * can bunch up and execute a number of times back-to-back.
     *
     * @param interval The desired interval, in milliseconds.
     * @param fixedRate Should the rate be corrected?
     */
    public Ticker(int interval, boolean fixedRate) {
        Timer t = new Timer();
        TimerTask ttask = new TimerTask() {
            @Override
            public void run() {
                producer.produce();
            }
        };
        if (fixedRate) {
            t.scheduleAtFixedRate(ttask, interval, interval);
        } else {
            t.schedule(ttask, interval, interval);
        }
    }

    /**
     * Adds an EventConsumer to listen for the periodically fired events
     * produced by this EventSource.
     *
     * @param ec The EventConsumer to add.
     * @return Whether the operation was successful, which it always is.
     */
    public boolean addListener(EventConsumer ec) {
        return producer.addListener(ec);
    }

    /**
     * Removes the specified EventConsumer, so that its eventFired method will
     * no longer be called by this EventSource.
     *
     * @param ec The EventConsumer to remove.
     */
    public void removeListener(EventConsumer ec) {
        producer.removeListener(ec);
    }
}
