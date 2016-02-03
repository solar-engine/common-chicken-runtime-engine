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
import ccre.channel.FloatIO;

/**
 * The soft limits on a Talon.
 *
 * @author skeggsc
 */
public interface TalonSoftLimits {

    /**
     * Controls the forward soft limit setting.
     *
     * @return a FloatIO representing the soft limit.
     */
    public FloatIO getForwardSoftLimit();

    /**
     * Controls whether or not the forward soft limit is enabled.
     *
     * @return a BooleanIO representing the soft limit's activation.
     */
    public BooleanIO getEnableForwardSoftLimit();

    /**
     * Controls the reverse soft limit setting.
     *
     * @return a FloatIO representing the soft limit.
     */
    public FloatIO getReverseSoftLimit();

    /**
     * Controls whether or not the reverse soft limit is enabled.
     *
     * @return a BooleanIO representing the soft limit's activation.
     */
    public BooleanIO getEnableReverseSoftLimit();
}
