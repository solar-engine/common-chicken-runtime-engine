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
 * A FloatInput is a way to get the current state of a float input, and to
 * subscribe to notifications of changes in the float input's value. FloatInput
 * is a subinterface of FloatInputPoll.
 *
 * By convention, most float inputs and outputs have states that range from
 * -1.0f to 1.0f.
 *
 * @author skeggsc
 */
public interface FloatInput extends UpdatingInput {

    public float get();
    
    /**
     * Subscribe to changes in this float input's value. The float output will
     * be modified whenever the value of this input changes.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * If available, the current value of the input will be written at this
     * time.
     *
     * If the same listener is added multiple times, it has the same effect as
     * if it was added once.
     *
     * @param output The float output to notify when the value changes.
     * @see FloatOutput#set(float)
     * @see #unsend(ccre.channel.FloatOutput)
     */
    public void send(FloatOutput output);

    /**
     * Unsubscribe from changes in this float input's value. This reverses the
     * actions of a previous send call.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * If the listener was not added previously (or had been removed), this call
     * will do nothing.
     *
     * After this is called, a listener can be reregistered with send.
     *
     * @param output The output to unsubscribe.
     * @see #send(ccre.channel.FloatOutput)
     */
    public void unsend(FloatOutput output);
}
