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
package ccre.frc;

import ccre.discrete.DiscreteType;

/**
 * An enumeration that represents the current robot mode.
 *
 * @author skeggsc
 */
public enum FRCMode {
    /**
     * Disabled mode.
     */
    DISABLED,
    /**
     * Autonomous mode.
     */
    AUTONOMOUS,
    /**
     * Teleoperated mode.
     */
    TELEOP,
    /**
     * Test mode.
     */
    TEST;

    /**
     * The discrete type for this enum, which does not include null.
     */
    public static final DiscreteType<FRCMode> discreteType = new DiscreteType<FRCMode>() {
        @Override
        public Class<FRCMode> getType() {
            return FRCMode.class;
        }

        @Override
        public FRCMode[] getOptions() {
            return values();
        }

        @Override
        public boolean isOption(FRCMode e) {
            return e != null;
        }

        @Override
        public String toString(FRCMode e) {
            return e.name();
        }

        @Override
        public FRCMode getDefaultValue() {
            return DISABLED;
        }
    };
}
