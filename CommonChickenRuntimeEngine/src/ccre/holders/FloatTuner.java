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
package ccre.holders;

import ccre.chan.FloatInput;
import ccre.chan.FloatInputProducer;
import ccre.chan.FloatOutput;

/**
 * A tunable float value. This is a FloatInput and FloatOutput. Also allows for
 * specification of a channel to be used for automatic tuning.
 *
 * @author skeggsc
 */
public interface FloatTuner extends FloatInput, FloatOutput {

    /**
     * Fetch the automatic tuning channel (see getNetworkChannelForAutomatic).
     * This is the same as getAutomaticChannel(CluckGlobals.encoder)
     *
     * @return the automatic tuning channel or null if none exists.
     */
    FloatInputProducer getAutomaticChannel();

    /**
     * Get the current value for the Tuner, or null if it is unknown.
     *
     * @return the current value or null if it is unknown.
     */
    Float getCurrentValue();

    /**
     * Change the current value to the specified value.
     *
     * @param newValue The new value to have.
     */
    void tuneTo(float newValue);
}
