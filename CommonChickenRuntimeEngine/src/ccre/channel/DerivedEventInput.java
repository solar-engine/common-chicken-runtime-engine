/*
 * Copyright 2015 Cel Skeggs
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
 * <code>EventInput input = new DerivedEventInput(... some other inputs to watch ...) {<br>&nbsp;&nbsp;&nbsp;&nbsp;protected boolean shouldProduce() {<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return // some condition<br>&nbsp;&nbsp;&nbsp;&nbsp;}<br>}</code>
 *
 * Any other inputs may be specified in the constructor, which should be the
 * inputs that, when updated, might cause this to also update (as in, fire.)
 * When one of these updates, <code>shouldProduce()</code> is evaluated and, if
 * the result has changed, the value of the BooleanInput is updated.
 *
 * @author skeggsc
 */
public abstract class DerivedEventInput extends AbstractUpdatingInput implements EventInput {

    /**
     * Creates a derived EventInput that may update when anything in
     * <code>updates</code> is changed.
     *
     * @param updates the UpdatingInputs to monitor.
     */
    public DerivedEventInput(UpdatingInput... updates) {
        DerivedUpdate.onUpdates(updates, () -> {
            if (shouldProduce()) {
                super.perform();
            }
        });
    }

    /**
     * Implement this to specify when the EventInput should be fired. This is
     * checked once for each time any input updates that this input is derived
     * from.
     *
     * @return if the EventInput should be fired/produced.
     */
    protected abstract boolean shouldProduce();
}
