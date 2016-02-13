/*
 * Copyright 2014-2016 Cel Skeggs
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
package ccre.frc.devices;

import ccre.channel.BooleanInput;
import ccre.channel.DerivedBooleanInput;
import ccre.discrete.DerivedDiscreteInput;
import ccre.discrete.DiscreteInput;
import ccre.frc.Device;
import ccre.frc.FRCMode;
import ccre.frc.components.BooleanTextComponent;
import ccre.frc.components.SpacingComponent;
import ccre.frc.components.TextComponent;

/**
 * A device allowing for changing the virtual robot's mode and
 * enabling/disabling it.
 *
 * @author skeggsc
 */
public class RobotModeDevice extends Device {

    // null, "AUTO", "TELE", "TEST"

    private final BooleanTextComponent enabled = new BooleanTextComponent("DISABLED", "ENABLED").setEditable(true);
    private final BooleanTextComponent autoLight = new BooleanTextComponent("AUTO") {
        @Override
        public void onPress(int x, int y) {
            setMode(FRCMode.AUTONOMOUS);
        }
    };
    private final BooleanTextComponent teleLight = new BooleanTextComponent("TELE") {
        @Override
        public void onPress(int x, int y) {
            setMode(FRCMode.TELEOP);
        }
    };
    private final BooleanTextComponent testLight = new BooleanTextComponent("TEST") {
        @Override
        public void onPress(int x, int y) {
            setMode(FRCMode.TEST);
        }
    };
    // except for DISABLED
    private FRCMode selectedMode = FRCMode.TELEOP;

    private void setMode(FRCMode mode) {
        if (enabled.get()) {
            return;// Can't change mode while enabled.
        }
        this.selectedMode = mode;
        autoLight.safeSet(mode == FRCMode.AUTONOMOUS);
        teleLight.safeSet(mode == FRCMode.TELEOP);
        testLight.safeSet(mode == FRCMode.TEST);
    }

    /**
     * Create a new FRCModeDevice.
     */
    public RobotModeDevice() {
        add(new SpacingComponent(20));
        add(new TextComponent("Mode"));
        add(enabled);
        setMode(FRCMode.TELEOP);
        add(autoLight);
        add(teleLight);
        add(testLight);
    }

    /**
     * Return a BooleanInput representing if the robot is in the specified mode.
     *
     * @param mode the mode to monitor.
     * @return the channel representing if the robot is in that mode.
     */
    public BooleanInput getIsMode(final FRCMode mode) {
        if (mode == FRCMode.DISABLED) {
            return enabled.asInput().not();
        } else {
            return new DerivedBooleanInput(enabled.asInput()) {
                // updates only matter when enabled changes... the mode (and so
                // the result) can't change when the mode is enabled.
                // and when disabled, the result is false anyway... until
                // enabled becomes true.
                @Override
                protected boolean apply() {
                    return enabled.get() && selectedMode == mode;
                }
            };
        }
    }

    /**
     * Return a BooleanInput representing if the robot is enabled.
     *
     * @return the channel representing if the robot is enabled.
     */
    public BooleanInput getIsEnabled() {
        return enabled.asInput();
    }

    public DiscreteInput<FRCMode> getMode() {
        return new DerivedDiscreteInput<FRCMode>(FRCMode.discreteType, enabled.asInput()) {
            @Override
            protected FRCMode apply() {
                return enabled.get() ? selectedMode : FRCMode.DISABLED;
            }
        };
    }
}
