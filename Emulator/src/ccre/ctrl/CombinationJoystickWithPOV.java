/*
 * Copyright 2015-2016 Cel Skeggs
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
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;

/**
 * A combination of two Joysticks into a single virtual Joystick. Buttons are
 * xor'd, and axes are summed. POV is xor'd by direction.
 *
 * @author skeggsc
 */
public class CombinationJoystickWithPOV implements Joystick {

    private final Joystick alpha, beta;

    /**
     * Combine two Joysticks into one.
     *
     * @param alpha the first Joystick.
     * @param beta the second Joystick.
     */
    public CombinationJoystickWithPOV(Joystick alpha, Joystick beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    public FloatInput axis(int axis) {
        return alpha.axis(axis).plus(beta.axis(axis));
    }

    public BooleanInput button(int button) {
        return alpha.button(button).xor(beta.button(button));
    }

    @Override
    public BooleanInput isPOV(int direction) {
        return alpha.isPOV(direction).xor(beta.isPOV(direction));
    }

    @Override
    public FloatOutput rumble(boolean right) {
        return alpha.rumble(right).combine(beta.rumble(right));
    }
}
