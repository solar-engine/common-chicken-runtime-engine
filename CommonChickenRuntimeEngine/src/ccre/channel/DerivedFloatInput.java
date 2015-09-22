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

import java.util.concurrent.CopyOnWriteArrayList;

import ccre.util.Utils;

public abstract class DerivedFloatInput extends DerivedUpdate implements FloatInput {

    private float value;
    private final CopyOnWriteArrayList<EventOutput> consumers = new CopyOnWriteArrayList<>();

    public DerivedFloatInput(UpdatingInput... updates) {
        super(updates);
        value = apply();
    }

    public DerivedFloatInput(UpdatingInput[] updates, UpdatingInput... moreUpdates) {
        super(updates, moreUpdates);
        value = apply();
    }

    @Override
    protected final void update() {
        float newvalue = apply();
        if (newvalue != value) {
            value = newvalue;
            for (EventOutput consumer : consumers) {
                consumer.event();
            }
        }
    }

    @Override
    protected final boolean updateWithRecovery() {
        float newvalue = apply();
        boolean recovered = false;
        if (newvalue != value) {
            value = newvalue;
            for (EventOutput consumer : consumers) {
                recovered |= consumer.eventWithRecovery();
            }
        }
        return recovered;
    }

    public final float get() {
        return value;
    }

    protected abstract float apply();

    @Override
    public void onUpdate(EventOutput notify) {
        consumers.add(notify);
    }

    @Override
    public EventOutput onUpdateR(EventOutput notify) {
        return Utils.addR(consumers, notify);
    }
}
