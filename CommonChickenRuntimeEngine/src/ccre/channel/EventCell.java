/*
 * Copyright 2013-2015 Cel Skeggs
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
package ccre.channel;

import java.io.Serializable;

/**
 * An implementation of an EventInput. This can be fired using the .produce()
 * method or by firing this event as an EventOutput.
 *
 * @author skeggsc
 */
public class EventCell extends AbstractUpdatingInput implements EventIO, Serializable {

    private static final long serialVersionUID = -1536503261547524049L;

    /**
     * Create a new Event that fires the specified events when fired. This is
     * equivalent to adding the events as listeners.
     *
     * @param events the events to fire when this event is fired.
     * @see #send(ccre.channel.EventOutput)
     */
    public EventCell(EventOutput... events) {
        for (EventOutput event : events) {
            onUpdate(event);
        }
    }

    @Override
    public void event() {
        perform();
    }
}
