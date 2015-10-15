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
package ccre.channel;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import ccre.util.Utils;

/**
 * An UpdatingInput that simplifies updating sending to simply calling
 * {@link #perform()}.
 *
 * @author skeggsc
 */
public abstract class AbstractUpdatingInput implements UpdatingInput {

    /**
     * The list of consumers that will be notified when this UpdatingInput
     * updates.
     */
    private final CopyOnWriteArrayList<EventOutput> consumers = new CopyOnWriteArrayList<>();

    /**
     * Tell all of the listeners that whatever this UpdatingInput represents has
     * updated.
     */
    protected final void perform() {
        for (Iterator<EventOutput> iterator = consumers.iterator(); iterator.hasNext();) {
            EventOutput output = iterator.next();
            try {
                output.event();
            } catch (Throwable e) {
                while (iterator.hasNext()) {
                    EventOutput out2 = iterator.next();
                    try {
                        out2.event();
                    } catch (Throwable ex) {
                        e.addSuppressed(ex);
                    }
                }
                throw e;
            }
        }
    }

    @Override
    public void onUpdate(EventOutput notify) {
        if (notify == null) {
            throw new NullPointerException();
        }
        consumers.add(notify);
    }

    @Override
    public EventOutput onUpdateR(EventOutput notify) {
        return Utils.addR(consumers, notify);
    }

    /**
     * Returns whether or not this has any listeners that will get fired. If
     * this returns false, the perform() method will do nothing.
     *
     * @return whether or not the perform method would do anything.
     * @see #perform()
     */
    public boolean hasListeners() {
        return !consumers.isEmpty();
    }

    /**
     * Clear all listeners on this DerivedUpdatingInput. Only do this if you
     * have a very good reason!
     */
    public void __UNSAFE_clearListeners() {
        consumers.clear();
    }
}
