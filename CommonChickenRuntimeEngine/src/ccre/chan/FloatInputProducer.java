/*
 * Copyright 2013 Colby Skeggs
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
package ccre.chan;

/**
 * A FloatInputProducer is a way to subscribe to notifications of changes in a
 * float input's value.
 *
 * By convention, most float inputs and outputs have states that range from
 * -1.0f to 1.0f.
 *
 * @see FloatOutput
 * @author skeggsc
 */
public interface FloatInputProducer {

    /**
     * Subscribe to changes in this float input's value. The float output will
     * be modified whenever the value of this input changes.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * @param output The float output to notify when the value changes.
     * @see FloatOutput#writeValue(float)
     * @see #removeTarget(ccre.chan.FloatOutput)
     */
    public void addTarget(FloatOutput output);

    /**
     * Unsubscribe from changes in this float input's value. This reverses the
     * actions of a previous addTarget call.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * @param output The output to unsubscribe.
     * @return Whether the output was actually subscribed.
     * @see #addTarget(ccre.chan.FloatOutput)
     */
    public boolean removeTarget(FloatOutput output);
}
