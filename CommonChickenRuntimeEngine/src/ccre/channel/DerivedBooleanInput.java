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

public abstract class DerivedBooleanInput implements BooleanInput {

    private final BooleanStatus value = new BooleanStatus();
    
    public DerivedBooleanInput(UpdatingInput... updates) {
        DerivedEventInput.whenAny(updates, () -> {
            value.set(apply());
        });
    }

    public final boolean get() {
        return value.get();
    }

    protected abstract boolean apply();

    @Override
    public final EventInput onUpdate() {
        return value.onUpdate();
    }

    @Override
    public final void send(BooleanOutput output) {
        value.send(output);
    }

    @Override
    public final void unsend(BooleanOutput output) {
        value.send(output);
    }
}
