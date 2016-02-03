/*
 * Copyright 2016 Colby Skeggs
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

import ccre.channel.BooleanIO;
import ccre.ctrl.ExtendedMotor;

/**
 * An ExtendedMotor that also provides a full Talon SRX interface.
 *
 * @author skeggsc
 */
public abstract class TalonExtendedMotor extends ExtendedMotor implements TalonSRX {
    /**
     * Provides the enablement state of this ExtendedMotor.
     *
     * @return a BooleanIO representing the enable state of the Talon.
     */
    public abstract BooleanIO asEnable();

    public TalonExtendedMotor modMotor() {
        return this;
    }
}
