/*
 * Copyright 2016 Cel Skeggs
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
package ccre.drivers.ctre.talon;

import ccre.channel.BooleanInput;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;

/**
 * The encoder input of a Talon.
 *
 * @author skeggsc
 */
public interface TalonEncoder {

    /**
     * Configures the rates at which the encoder frame will be updated. Read the
     * Talon SRX documentation for details.
     *
     * @param millis the millisecond period of the "encoder" frame.
     */
    public void configureEncoderUpdateRate(int millis);

    /**
     * Sets the Talon to read from a quadrature encoder for its sensor position.
     */
    public void useEncoder();

    /**
     * Sets the Talon to read from a rising-edge counter for its sensor
     * position.
     */
    public void useRisingEdge();

    /**
     * Sets the Talon to read from a falling-edge counter for its sensor
     * position.
     */
    public void useFallingEdge();

    /**
     * Provides the current encoder position.
     *
     * @return a FloatIO representing the current encoder position.
     */
    public FloatIO getEncoderPosition();

    /**
     * Provides the current encoder velocity.
     *
     * @return a FloatInput representing the current encoder velocity.
     */
    public FloatInput getEncoderVelocity();

    /**
     * Configures the number of encoder ticks per encoder revolution.
     *
     * @param perRev the number of ticks.
     */
    public void configureEncoderCodesPerRev(float perRev);

    /**
     * Provides the number of times that the index pin has risen.
     *
     * @return a FloatInput representing the number of low-to-high transitions.
     */
    public FloatInput getNumberOfQuadIndexRises();

    /**
     * Provides the current state of the A pin. True if the pin is high, and
     * false if the pin is low.
     *
     * @return a BooleanInput representing whether the A pin is high.
     */
    public BooleanInput getQuadAPin();

    /**
     * Provides the current state of the B pin. True if the pin is high, and
     * false if the pin is low.
     *
     * @return a BooleanInput representing whether the B pin is high.
     */
    public BooleanInput getQuadBPin();

    /**
     * Provides the current state of the Index pin. True if the pin is high, and
     * false if the pin is low.
     *
     * @return a BooleanInput representing whether the Index pin is high.
     */
    public BooleanInput getQuadIndexPin();
}
