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

import ccre.chan.FloatInput;
import ccre.event.EventSource;

/**
 * A joystick that provides asynchronous triggers for its inputs, instead of
 * pollable values.
 *
 * @see ISimpleJoystick
 * @author skeggsc
 */
public interface IDispatchJoystick extends ISimpleJoystick {

    /**
     * Get an EventSource that will be fired when the given button is pressed.
     *
     * @param id the button ID.
     * @return the EventSource representing the button being pressed.
     */
    public EventSource getButtonSource(int id);

    /**
     * Get a FloatInput that represents the given axis.
     *
     * @param axis the axis ID.
     * @return the FloatInput representing the axis.
     */
    public FloatInput getAxisSource(int axis);
}
