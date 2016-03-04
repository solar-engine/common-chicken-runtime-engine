/*
 * Copyright 2016 Cel Skeggs
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

import ccre.channel.BooleanInput;

/**
 * The hard limits on a Talon.
 *
 * @author skeggsc
 */
public interface TalonHardLimits {
    /**
     * The possible configuration modes of a hard limit switch.
     *
     * @author skeggsc
     */
    public static enum TalonLimitSwitchMode {
        /**
         * This mode means that the switch should be ignored.
         */
        DISABLED,
        /**
         * This mode means that the switch should be interpreted as normally
         * closed.
         */
        NORMALLY_CLOSED,
        /**
         * This mode means that the switch should be interpreted as normally
         * open.
         */
        NORMALLY_OPEN
    }

    /**
     * Configures the two hard limit switches on the Talon. This selects whether
     * each switch should be ignored or observed, and the interpretation
     * polarity.
     *
     * @param forward the configuration for the forward direction switch
     * @param reverse the configuration for the reverse direction switch
     */
    public default void configureLimitSwitches(TalonLimitSwitchMode forward, TalonLimitSwitchMode reverse) {
        configureLimitSwitches(forward != TalonLimitSwitchMode.DISABLED, forward == TalonLimitSwitchMode.NORMALLY_CLOSED, reverse != TalonLimitSwitchMode.DISABLED, reverse == TalonLimitSwitchMode.NORMALLY_CLOSED);
    }

    /**
     * Configures the two hard limit switches on the Talon. This selects whether
     * each switch should be ignored or observed, and the interpretation
     * polarity.
     *
     * @param forwardEnable if the forward switch should be observed.
     * @param forwardNC if the forward switch should be normally closed.
     * @param reverseEnable if the reverse switch should be observed.
     * @param reverseNC if the reverse switch should be normally closed.
     */
    public void configureLimitSwitches(boolean forwardEnable, boolean forwardNC, boolean reverseEnable, boolean reverseNC);

    /**
     * Provides the state of the forward limit switch.
     *
     * @return a BooleanInput representing whether the switch is closed.
     */
    public BooleanInput getIsForwardLimitSwitchClosed();

    /**
     * Provides the state of the reverse limit switch.
     *
     * @return a BooleanInput representing whether the switch is closed.
     */
    public BooleanInput getIsReverseLimitSwitchClosed();
}
