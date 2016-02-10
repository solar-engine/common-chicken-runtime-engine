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
 * Provides a way to do something when an input changes. Override
 * {@link #update()} for the action to perform, and pass the inputs to watch to
 * the constructor.
 *
 * Inputs are checked for changes with the
 * {@link UpdatingInput#onUpdate(EventOutput)} method.
 *
 * @author skeggsc
 */
public abstract class DerivedUpdate {

    /**
     * Create a new DerivedUpdate that calls {@link #update()} when any of the
     * specified inputs update.
     *
     * This is equivalent to calling {@link UpdatingInput#onUpdate(EventOutput)}
     * on all of these with an action that calls {@link #update()}.
     *
     * @param updates the inputs to watch.
     */
    public DerivedUpdate(UpdatingInput... updates) {
        onUpdates(updates, this::update);
    }

    /**
     * An extracted version of the constructor implementation. When any of the
     * specified UpdatingInputs are updated, then fire the EventOutput.
     *
     * @param output the output to fire
     * @param inputs the inputs to monitor
     */
    public static void onUpdates(UpdatingInput[] inputs, EventOutput output) {
        if (inputs.length == 0) {
            throw new IllegalArgumentException("Must be at least one update source!");
        }
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] == null) {
                throw new NullPointerException();
            }
            inputs[i].onUpdate(output);
        }
    }

    /**
     * Perform an action. This is called when any of the inputs specified in the
     * constructor are updated.
     */
    protected abstract void update();
}
