/*
 * Copyright 2014-2015 Colby Skeggs
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
package ccre.igneous.devices;

import ccre.channel.BooleanInput;
import ccre.channel.DerivedBooleanInput;
import ccre.ctrl.BooleanMixing;
import ccre.igneous.Device;
import ccre.igneous.components.BooleanTextComponent;
import ccre.igneous.components.SpacingComponent;
import ccre.igneous.components.TextComponent;

/**
 * A device allowing for changing the virtual robot's mode and
 * enabling/disabling it.
 *
 * @author skeggsc
 */
public class RobotModeDevice extends Device {

    /**
     * An enum of the possible modes.
     *
     * @author skeggsc
     */
    public static enum RobotMode {
        /**
         * The DISABLED mode. Has a null selection name.
         */
        DISABLED(null),
        /**
         * The AUTONOMOUS mode. The selection name is AUTO.
         */
        AUTONOMOUS("AUTO"),
        /**
         * The TELEOPERATED mode. The selection name is TELE.
         */
        TELEOPERATED("TELE"),
        /**
         * The TESTING mode. The selection name is TEST.
         */
        TESTING("TEST");

        /**
         * The selection name, used for displaying a short name for the current
         * selection.
         *
         * This is either null, "AUTO", "TELE", or "TEST".
         */
        public final String selectionName;

        private RobotMode(String name) {
            selectionName = name;
        }
    }

    private final BooleanTextComponent enabled = new BooleanTextComponent("DISABLED", "ENABLED").setEditable(true);
    private final BooleanTextComponent autoLight = new BooleanTextComponent("AUTO") {
        @Override
        public void onPress(int x, int y) {
            setMode(RobotMode.AUTONOMOUS);
        }
    };
    private final BooleanTextComponent teleLight = new BooleanTextComponent("TELE") {
        @Override
        public void onPress(int x, int y) {
            setMode(RobotMode.TELEOPERATED);
        }
    };
    private final BooleanTextComponent testLight = new BooleanTextComponent("TEST") {
        @Override
        public void onPress(int x, int y) {
            setMode(RobotMode.TESTING);
        }
    };
    private RobotMode selectedMode = RobotMode.TELEOPERATED; // except for DISABLED

    private void setMode(RobotMode mode) {
        if (enabled.get()) {
            return; // Can't change mode while enabled.
        }
        this.selectedMode = mode;
        autoLight.set(mode == RobotMode.AUTONOMOUS);
        teleLight.set(mode == RobotMode.TELEOPERATED);
        testLight.set(mode == RobotMode.TESTING);
    }

    /**
     * Create a new RobotModeDevice.
     */
    public RobotModeDevice() {
        add(new SpacingComponent(20));
        add(new TextComponent("Mode"));
        add(enabled);
        setMode(RobotMode.TELEOPERATED);
        add(autoLight);
        add(teleLight);
        add(testLight);
    }

    /**
     * Return a BooleanInputPoll representing if the robot is in the specified
     * mode.
     *
     * @param mode the mode to monitor.
     * @return the channel representing if the robot is in that mode.
     */
    public BooleanInput getIsMode(final RobotMode mode) {
        if (mode == RobotMode.DISABLED) {
            return BooleanMixing.invert((BooleanInput) enabled);
        } else {
            return new DerivedBooleanInput(enabled) {
                // updates only matter when enabled changes... the mode (and so the result) can't change when the mode is enabled.
                // and when disabled, the result is false anyway... until enabled becomes true.
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
        return enabled;
    }
}
