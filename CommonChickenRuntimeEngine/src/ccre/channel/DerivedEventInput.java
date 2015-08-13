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


public abstract class DerivedEventInput implements EventInput {

    private final EventStatus value = new EventStatus();

    public DerivedEventInput(UpdatingInput... updates) {
        whenAny(updates, () -> {
            if (shouldProduce()) {
                value.event();
            }
        });
    }

    static void whenAny(UpdatingInput[] updates, EventOutput event) {
        if (updates.length == 0) {
            throw new IllegalArgumentException("Must be at least one update source!");
        }
        for (int i = 0; i < updates.length; i++) {
            if (updates[i] == null) {
                throw new NullPointerException();
            }
            updates[i].onUpdate().send(event);
        }
    }

    protected abstract boolean shouldProduce();

    @Override
    public EventInput onUpdate() {
        return value;
    }

    @Override
    public void send(EventOutput output) {
        value.send(output);
    }

    @Override
    public void unsend(EventOutput output) {
        value.send(output);
    }
}
