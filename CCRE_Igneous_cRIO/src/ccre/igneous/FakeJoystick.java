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
package ccre.igneous;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.ctrl.IJoystickWithPOV;

class FakeJoystick implements IJoystickWithPOV {

    private final String errorMessage;

    public FakeJoystick(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public EventInput getButtonSource(int id) {
        throw new RuntimeException(errorMessage);
    }

    public FloatInput getAxisSource(int axis) {
        throw new RuntimeException(errorMessage);
    }

    public FloatInputPoll getAxisChannel(int axis) {
        throw new RuntimeException(errorMessage);
    }

    public BooleanInputPoll getButtonChannel(int button) {
        throw new RuntimeException(errorMessage);
    }

    public FloatInputPoll getXChannel() {
        throw new RuntimeException(errorMessage);
    }

    public FloatInputPoll getYChannel() {
        throw new RuntimeException(errorMessage);
    }

    public FloatInput getXAxisSource() {
        throw new RuntimeException(errorMessage);
    }

    public FloatInput getYAxisSource() {
        throw new RuntimeException(errorMessage);
    }

    public BooleanInputPoll isPOVPressed(int id) {
        throw new RuntimeException(errorMessage);
    }

    public FloatInputPoll getPOVAngle(int id) {
        throw new RuntimeException(errorMessage);
    }

    public BooleanInput isPOVPressedSource(int id) {
        throw new RuntimeException(errorMessage);
    }

    public FloatInput getPOVAngleSource(int id) {
        throw new RuntimeException(errorMessage);
    }
}
