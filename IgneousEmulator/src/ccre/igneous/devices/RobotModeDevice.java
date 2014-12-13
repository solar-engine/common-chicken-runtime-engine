/*
 * Copyright 2014 Colby Skeggs
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

import ccre.channel.BooleanInputPoll;
import ccre.ctrl.BooleanMixing;
import ccre.igneous.Device;
import ccre.igneous.components.BooleanTextComponent;
import ccre.igneous.components.SpacingComponent;
import ccre.igneous.components.TextComponent;

public class RobotModeDevice extends Device {

    public static enum RobotMode {
        DISABLED(null), AUTONOMOUS("AUTO"), TELEOPERATED("TELE"), TESTING("TEST");

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

    public RobotModeDevice() {
        add(new SpacingComponent(20));
        add(new TextComponent("Mode"));
        add(enabled);
        setMode(RobotMode.TELEOPERATED);
        add(autoLight);
        add(teleLight);
        add(testLight);
    }

    public BooleanInputPoll getIsMode(final RobotMode mode) {
        return mode == RobotMode.DISABLED ? BooleanMixing.invert((BooleanInputPoll) enabled) : new BooleanInputPoll() {
            public boolean get() {
                return selectedMode == mode;
            }
        };
    }
}
