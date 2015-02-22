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
 * A combination of two Joysticks into a single virtual Joystick. Buttons are
 * xor'd, and axes are summed. POV comes from one of the Joysticks - and the
 * Alpha Joystick gets priority.
 *
 * @author skeggsc
 */
public class CombinationJoystickWithPOV implements IJoystickWithPOV {

    private final IJoystickWithPOV alpha, beta;

    /**
     * Combine two Joysticks into one.
     *
     * @param alpha the first Joystick. (Gets POV hat priority.)
     * @param beta the second Joystick.
     */
    public CombinationJoystickWithPOV(IJoystickWithPOV alpha, IJoystickWithPOV beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    public EventInput getButtonSource(int id) {
        return EventMixing.combine(alpha.getButtonSource(id), beta.getButtonSource(id));
    }

    public FloatInput getAxisSource(int axis) {
        return FloatMixing.addition.of(alpha.getAxisSource(axis), beta.getAxisSource(axis));
    }

    public FloatInputPoll getAxisChannel(int axis) {
        return FloatMixing.addition.of(alpha.getAxisChannel(axis), beta.getAxisChannel(axis));
    }

    public BooleanInputPoll getButtonChannel(int button) {
        return BooleanMixing.xorBooleans(alpha.getButtonChannel(button), beta.getButtonChannel(button));
    }

    public FloatInputPoll getXChannel() {
        return getAxisChannel(1);
    }

    public FloatInputPoll getYChannel() {
        return getAxisChannel(2);
    }

    public FloatInput getXAxisSource() {
        return getAxisSource(1);
    }

    public FloatInput getYAxisSource() {
        return getAxisSource(2);
    }

    public BooleanInputPoll isPOVPressed(int id) {
        return BooleanMixing.orBooleans(alpha.isPOVPressed(id), beta.isPOVPressed(id));
    }

    public FloatInputPoll getPOVAngle(int id) {
        return Mixing.select(alpha.isPOVPressed(id), beta.getPOVAngle(id), alpha.getPOVAngle(id));
    }

    public BooleanInput isPOVPressedSource(int id) {
        return BooleanMixing.orBooleans(alpha.isPOVPressedSource(id), beta.isPOVPressedSource(id));
    }

    public FloatInput getPOVAngleSource(int id) {
        BooleanInput useA = alpha.isPOVPressedSource(id);
        FloatInput aa = alpha.getPOVAngleSource(id);
        FloatInput ba = beta.getPOVAngleSource(id);
        return FloatMixing.createDispatch(getPOVAngle(id), EventMixing.combine(FloatMixing.onUpdate(aa), FloatMixing.onUpdate(ba), BooleanMixing.whenBooleanChanges(useA)));
    }
}
