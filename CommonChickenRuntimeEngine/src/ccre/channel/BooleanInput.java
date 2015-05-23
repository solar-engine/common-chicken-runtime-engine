/*
 * Copyright 2013-2014 Colby Skeggs
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
 * A BooleanInput is a way to get the current state of a boolean input, and to
 * subscribe to notifications of changes in the boolean input's value.
 * BooleanInput is a subinterface of BooleanInputPoll.
 *
 * @see BooleanInputPoll
 * @author skeggsc
 */
public interface BooleanInput extends BooleanInputPoll {

    /**
     * Subscribe to changes in this boolean input's value. The boolean output
     * will be modified whenever the value of this input changes.
     *
     * If available, the current value of the input will be written at this
     * time.
     *
     * If the same listener is added multiple times, it has the same effect as
     * if it was added once.
     *
     * @param output The boolean output to notify when the value changes.
     * @see BooleanOutput#set(boolean)
     * @see #unsend(ccre.channel.BooleanOutput)
     */
    public void send(BooleanOutput output);

    /**
     * Unsubscribe from changes in this boolean input's value. This reverses the
     * actions of a previous send call.
     *
     * If the listener was not added previously (or had been removed), this call
     * will do nothing.
     *
     * After unsend is called, a listener can be reregistered with send.
     *
     * @param output The output to unsubscribe.
     * @see #send(ccre.channel.BooleanOutput)
     */
    public void unsend(BooleanOutput output);
}
