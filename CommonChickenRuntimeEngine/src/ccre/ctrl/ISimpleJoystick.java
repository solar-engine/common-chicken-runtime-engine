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
package ccre.ctrl;

import ccre.chan.BooleanInputPoll;
import ccre.chan.FloatInputPoll;

/**
 * A joystick that provides pollable channels for all of its inputs.
 *
 * @see IDispatchJoystick
 * @author skeggsc
 */
public interface ISimpleJoystick {

    /**
     * Get a FloatInputPoll representing the state of the specified axis on this
     * joystick.
     *
     * @param axis the axis ID.
     * @return the FloatInputPoll representing the status of the axis.
     */
    public FloatInputPoll getAxisChannel(int axis);

    /**
     * Get a FloatInputPoll for the X axis.
     *
     * @return the FloatInputPoll representing the status of the X axis.
     */
    public FloatInputPoll getXChannel();

    /**
     * Get a FloatInputPoll for the Y axis.
     *
     * @return the FloatInputPoll representing the status of the Y axis.
     */
    public FloatInputPoll getYChannel();

    /**
     * Get a BooleanInputPoll representing whether or not the given button is
     * pressed.
     *
     * @param button the button ID.
     * @return the BooleanInputPoll representing if the given button is pressed.
     */
    public BooleanInputPoll getButtonChannel(int button);
}
