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
 * A BooleanIO is both a BooleanInput and a BooleanOutput.
 * 
 * @author skeggsc
 */
public interface BooleanIO extends BooleanInput, BooleanOutput {

    /**
     * When the specified event occurs, toggle the status.
     *
     * @param event When to toggle the status.
     * @see #eventToggle()
     * @see #toggle()
     */
    public default void toggleWhen(EventInput event) {
        event.send(eventToggle());
    }

    /**
     * Get an EventOutput that, when fired, will toggle the state.
     *
     * @return the EventOutput.
     * @see #toggleWhen(ccre.channel.EventInput)
     * @see #toggle()
     */
    public default EventOutput eventToggle() {
        return this::toggle;
    }

    /**
     * Toggle the value. True to false, and false to true.
     *
     * @see #toggleWhen(EventInput)
     * @see #eventToggle()
     */
    public default void toggle() {
        set(!get());
    }

    /**
     * Returns the output side of this BooleanIO. This is equivalent to
     * upcasting to BooleanOutput.
     *
     * @return this status, as an output.
     */
    public default BooleanOutput asOutput() {
        return this;
    }

    /**
     * Returns the input side of this BooleanIO. This is equivalent to upcasting
     * to BooleanInput.
     *
     * @return this status, as an input.
     */
    public default BooleanInput asInput() {
        return this;
    }
}