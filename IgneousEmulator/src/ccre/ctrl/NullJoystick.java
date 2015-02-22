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
import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;

/**
 * A Joystick that has nothing pressed and all axes as zero.
 *
 * @author skeggsc
 */
public class NullJoystick implements IJoystickWithPOV {

    public EventInput getButtonSource(int id) {
        return EventMixing.never;
    }

    public FloatInput getAxisSource(int axis) {
        return FloatMixing.always(0);
    }

    public FloatInputPoll getAxisChannel(int axis) {
        return FloatMixing.always(0);
    }

    public BooleanInputPoll getButtonChannel(int button) {
        return BooleanMixing.alwaysFalse;
    }

    public FloatInputPoll getXChannel() {
        return FloatMixing.always(0);
    }

    public FloatInputPoll getYChannel() {
        return FloatMixing.always(0);
    }

    public FloatInput getXAxisSource() {
        return FloatMixing.always(0);
    }

    public FloatInput getYAxisSource() {
        return FloatMixing.always(0);
    }

    public BooleanInputPoll isPOVPressed(int id) {
        return BooleanMixing.alwaysFalse;
    }

    public FloatInputPoll getPOVAngle(int id) {
        return FloatMixing.always(0);
    }

    public BooleanInput isPOVPressedSource(int id) {
        return BooleanMixing.alwaysFalse;
    }

    public FloatInput getPOVAngleSource(int id) {
        return FloatMixing.always(0);
    }
}
