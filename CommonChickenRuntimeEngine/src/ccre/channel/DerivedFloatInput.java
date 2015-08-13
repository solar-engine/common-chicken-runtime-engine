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

public abstract class DerivedFloatInput implements FloatInput {

    private final FloatStatus value = new FloatStatus();
    
    public DerivedFloatInput(UpdatingInput... updates) {
        DerivedEventInput.whenAny(updates, () -> {
            value.set(apply());
        });
    }

    public float get() {
        return value.get();
    }

    protected abstract float apply();

    @Override
    public EventInput onUpdate() {
        return value.onUpdate();
    }

    @Override
    public void send(FloatOutput output) {
        value.send(output);
    }

    @Override
    public void unsend(FloatOutput output) {
        value.send(output);
    }
}
