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

public abstract class DerivedEventInput extends DerivedUpdate implements EventInput {

    private final CopyOnWriteArrayList<EventOutput> consumers = new CopyOnWriteArrayList<>();

    public DerivedEventInput(UpdatingInput... updates) {
        super(updates);
    }

    public DerivedEventInput(UpdatingInput[] updates, UpdatingInput... moreUpdates) {
        super(updates, moreUpdates);
    }

    @Override
    protected final void update() {
        if (shouldProduce()) {
            for (EventOutput consumer : consumers) {
                consumer.event();
            }
        }
    }

    @Override
    protected final boolean updateWithRecovery() {
        boolean recovered = false;
        if (shouldProduce()) {
            for (EventOutput consumer : consumers) {
                recovered |= consumer.eventWithRecovery();
            }
        }
        return recovered;
    }

    protected abstract boolean shouldProduce();

    @Override
    public void onUpdate(EventOutput notify) {
        consumers.add(notify);
    }

    @Override
    public EventOutput onUpdateR(EventOutput notify) {
        return Utils.addR(consumers, notify);
    }
}
