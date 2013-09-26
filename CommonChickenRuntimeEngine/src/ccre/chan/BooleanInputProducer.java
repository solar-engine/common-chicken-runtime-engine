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
 * A BooleanInputProducer is a way to subscribe to notifications of changes in a
 * boolean input's value.
 *
 * @see BooleanOutput
 * @author skeggsc
 */
public interface BooleanInputProducer {

    /**
     * Subscribe to changes in this boolean input's value. The boolean output
     * will be modified whenever the value of this input changes.
     *
     * @param output The boolean output to notify when the value changes.
     * @see BooleanOutput#writeValue(boolean) 
     * @see #removeTarget(ccre.chan.BooleanOutput)
     */
    public void addTarget(BooleanOutput output);

    /**
     * Unsubscribe from changes in this boolean input's value. This reverses the
     * actions of a previous addTarget call.
     *
     * @param output The output to unsubscribe.
     * @return Whether the output was actually subscribed.
     * @see #addTarget(ccre.chan.BooleanOutput)
     */
    public boolean removeTarget(BooleanOutput output);
}
