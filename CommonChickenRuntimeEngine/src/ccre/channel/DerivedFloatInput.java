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

/**
 * A utility class that lets users define derived dataflow channels based on
 * other channels.
 *
 * <code>FloatInput input = new DerivedFloatInput(... some other inputs to watch ...) {<br>&nbsp;&nbsp;&nbsp;&nbsp;protected float apply() {<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return // some value<br>&nbsp;&nbsp;&nbsp;&nbsp;}<br>}</code>
 *
 * Any other inputs may be specified in the constructor, which should be the
 * inputs that, when updated, might cause this to also update. When one of these
 * updates, <code>apply()</code> is evaluated and, if the result has changed,
 * the value of the FloatInput is updated.
 *
 * In the case that one is polling something, the updating input could be a
 * periodic timer, such that it checks at a consistent rate. In FRC, the default
 * such timer is called {@link ccre.frc.FRC#sensorPeriodic}, but any EventInput
 * could work.
 *
 * @author skeggsc
 */
public abstract class DerivedFloatInput extends AbstractUpdatingInput implements FloatInput {

    private float value = apply();

    /**
     * Creates a derived FloatInput that may update when anything in
     * <code>updates</code> is changed.
     *
     * @param updates the UpdatingInputs to monitor.
     */
    public DerivedFloatInput(UpdatingInput... updates) {
        DerivedUpdate.onUpdates(updates, () -> {
            float newvalue = apply();
            if (newvalue != value) {
                value = newvalue;
                super.perform();
            }
        });
    }

    public final float get() {
        return value;
    }

    /**
     * Implement this to specify the value held by the derived FloatInput.
     *
     * @return the present value for the FloatInput.
     */
    protected abstract float apply();
}
