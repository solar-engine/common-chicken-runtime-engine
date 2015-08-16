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
import ccre.channel.FloatInput;

/**
 * A combination of two Joysticks into a single virtual Joystick. Buttons are
 * xor'd, and axes are summed. POV is xor'd by direction.
 *
 * @author skeggsc
 */
public class CombinationJoystickWithPOV implements IJoystick {

    private final IJoystick alpha, beta;

    /**
     * Combine two Joysticks into one.
     *
     * @param alpha the first Joystick.
     * @param beta the second Joystick.
     */
    public CombinationJoystickWithPOV(IJoystick alpha, IJoystick beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    public FloatInput axis(int axis) {
        return FloatMixing.addition.of(alpha.axis(axis), beta.axis(axis));
    }

    public BooleanInput button(int button) {
        return BooleanMixing.xorBooleans(alpha.button(button), beta.button(button));
    }

    @Override
    public BooleanInput isPOV(int direction) {
        return BooleanMixing.xorBooleans(alpha.isPOV(direction), beta.isPOV(direction));
    }
}
