/*
 * Copyright 2013 Colby Skeggs and Vincent Miller
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

import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.util.CLinkedList;
import ccre.util.CList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An EventSource that will fire the event in all its consumers at a specified
 * interval.
 * 
 * @author MillerV
 */
public final class Ticker extends TimerTask implements EventSource {
    private CList<EventConsumer> consumers;
    
    /**
     * Create a new Ticker with the specified interval. The timer will start
     * immediately, executing for the first time after the specified interval.
     * 
     * @param interval The desired interval, in milliseconds.
     */
    public Ticker(int interval) {
        consumers = new CLinkedList<EventConsumer>();
        Timer t = new Timer();
        t.schedule(this, interval, interval);
    }
    
    /**
     * Adds an EventConsumer to listen for the periodically fired events
     * produced by this EventSource.
     * 
     * @param ec The EventConsumer to add.
     * @return Whether the operation was successful, which it always is.
     */
    public boolean addListener(EventConsumer ec) {
        return consumers.add(ec);
    }
    
    /**
     * Removes the specified EventConsumer, so that its eventFired method will
     * no longer be called by this EventSource.
     * 
     * @param ec The EventConsumer to remove.
     */
    public void removeListener(EventConsumer ec) {
        consumers.remove(ec);
    }
    
    /**
     * This is called by the timer. Do not call this method yourself.
     */
    public void run() {
        for (EventConsumer ec : consumers) {
            ec.eventFired();
        }
    }
}
