/*
 * Copyright 2015-2016 Colby Skeggs
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
     * When the specified event occurs, toggle the io.
     *
     * @param event When to toggle the io.
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
     * @return this io, as an output.
     */
    public default BooleanOutput asOutput() {
        return this;
    }

    /**
     * Returns the input side of this BooleanIO. This is equivalent to upcasting
     * to BooleanInput.
     *
     * @return this io, as an input.
     */
    public default BooleanInput asInput() {
        return this;
    }

    /**
     * Compose a BooleanInput and a BooleanOutput into a single BooleanIO, which
     * dispatches to the two implementations.
     *
     * @param input the input to dispatch to.
     * @param output the output to dispatch to.
     * @return the composed BooleanIO.
     */
    public static BooleanIO compose(BooleanInput input, BooleanOutput output) {
        return new BooleanIO() {
            @Override
            public boolean get() {
                return input.get();
            }

            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                return input.onUpdate(notify);
            }

            @Override
            public void set(boolean value) {
                output.set(value);
            }
        };
    }

    @Override
    public default BooleanIO cell(boolean default_value) {
        this.set(default_value); // replicate behavior of superclass
        return this;
    }
}
