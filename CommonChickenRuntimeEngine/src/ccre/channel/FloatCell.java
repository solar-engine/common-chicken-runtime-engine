/*
 * Copyright 2013-2015 Colby Skeggs
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

import java.io.Serializable;

/**
 * A virtual node that is both a FloatOutput and a FloatInput. You can modify
 * its value, read its value, and subscribe to changes in its value. FloatStatus
 * also provides a number of useful helper functions.
 *
 * By convention, most float inputs and outputs have states that range from
 * -1.0f to 1.0f.
 *
 * @author skeggsc
 */
public class FloatCell extends AbstractUpdatingInput implements FloatIO, Serializable {

    private static final long serialVersionUID = 4452265093224394680L;

    private float value = 0;

    /**
     * Create a new FloatStatus with the specified default value.
     *
     * @param value The default value.
     */
    public FloatCell(float value) {
        this.value = value;
    }

    /**
     * Create a new FloatStatus that automatically updates all of the specified
     * FloatOutputs with the current state of this FloatStatus. This is the same
     * as creating a new FloatStatus and then adding all of the FloatOutputs as
     * targets.
     *
     * The default value is zero.
     *
     * @see FloatCell#send(ccre.channel.FloatOutput)
     * @param targets The FloatOutputs to automatically update.
     */
    public FloatCell(FloatOutput... targets) {
        for (FloatOutput o : targets) {
            send(o);
        }
    }

    public final synchronized float get() {
        return value;
    }

    public final synchronized void set(float newValue) {
        if (Float.floatToIntBits(value) != Float.floatToIntBits(newValue)) {
            value = newValue;
            perform();
        } else {
            // Do nothing; we want to ignore the value if it's the same.
        }
    }
}
