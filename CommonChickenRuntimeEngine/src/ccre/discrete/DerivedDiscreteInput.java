/*
 * Copyright 2015-2016 Cel Skeggs
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
package ccre.discrete;

import java.util.Objects;

import ccre.channel.AbstractUpdatingInput;
import ccre.channel.DerivedUpdate;
import ccre.channel.UpdatingInput;

/**
 * A utility class that lets users define derived dataflow channels based on
 * other channels.
 *
 * <code>DiscreteInput&lt;E&gt; input = new DerivedDiscreteInput&lt;E&gt;(... some other inputs to watch ...) {<br>&nbsp;&nbsp;&nbsp;&nbsp;protected E apply() {<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return // some value<br>&nbsp;&nbsp;&nbsp;&nbsp;}<br>}</code>
 *
 * Any other inputs may be specified in the constructor, which should be the
 * inputs that, when updated, might cause this to also update. When one of these
 * updates, <code>apply()</code> is evaluated and, if the result has changed,
 * the value of the DiscreteInput is updated.
 *
 * In the case that one is polling something, the updating input could be a
 * periodic timer, such that it checks at a consistent rate. In FRC, the default
 * such timer is called {@link ccre.frc.FRC#sensorPeriodic}, but any EventInput
 * could work.
 *
 * @author skeggsc
 * @param <E> the type of the discrete data
 */
public abstract class DerivedDiscreteInput<E> extends AbstractUpdatingInput implements DiscreteInput<E> {

    private final DiscreteType<E> type;
    private E value = apply();

    /**
     * Creates a derived DiscreteInput that may update when anything in
     * <code>updates</code> is changed.
     *
     * @param type the discrete type to use
     * @param updates the UpdatingInputs to monitor.
     */
    public DerivedDiscreteInput(DiscreteType<E> type, UpdatingInput... updates) {
        this.type = type;
        DerivedUpdate.onUpdates(updates, () -> {
            E newvalue = apply();
            if (!Objects.equals(newvalue, value)) {
                value = newvalue;
                super.perform();
            }
        });
    }

    @Override
    public final DiscreteType<E> getType() {
        return type;
    }

    public final E get() {
        return value;
    }

    /**
     * Implement this to specify the value held by the derived DiscreteInput.
     *
     * @return the present value for the DiscreteInput.
     */
    protected abstract E apply();
}
