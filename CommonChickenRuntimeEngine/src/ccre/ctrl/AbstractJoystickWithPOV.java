/*
 * Copyright 2015 Colby Skeggs
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

import ccre.channel.BooleanInput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;

/**
 * An abstract class for easy implementation of IJoystickWithPOV based only on a
 * set of polling implementations and a check event.
 * 
 * @author skeggsc
 */
public abstract class AbstractJoystickWithPOV implements IJoystickWithPOV {

    protected final EventInput check;

    /**
     * Create a new Joystick that updates on the specified event.
     * 
     * @param check when to update the Joystick's sources.
     */
    public AbstractJoystickWithPOV(EventInput check) {
        this.check = check;
    }

    public FloatInputPoll getXChannel() {
        return getAxisChannel(1);
    }

    public FloatInputPoll getYChannel() {
        return getAxisChannel(2);
    }

    public EventInput getButtonSource(int id) {
        return BooleanMixing.whenBooleanBecomes(getButtonChannel(id), true, check);
    }

    public FloatInput getAxisSource(int axis) {
        return FloatMixing.createDispatch(getAxisChannel(axis), check);
    }

    public FloatInput getXAxisSource() {
        return getAxisSource(1);
    }

    public FloatInput getYAxisSource() {
        return getAxisSource(2);
    }

    public BooleanInput isPOVPressedSource(int id) {
        return BooleanMixing.createDispatch(isPOVPressed(id), check);
    }

    public FloatInput getPOVAngleSource(int id) {
        return FloatMixing.createDispatch(getPOVAngle(id), check);
    }
}