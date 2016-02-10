/*
 * Copyright 2014-2015 Cel Skeggs
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
import ccre.channel.BooleanOutput;
import ccre.frc.Device;
import ccre.frc.components.BooleanTextComponent;
import ccre.frc.components.SpacingComponent;
import ccre.frc.components.TextComponent;

/**
 * A device representing some sort of boolean readout, such as a Solenoid or
 * digital output.
 *
 * @author skeggsc
 */
public class BooleanViewDevice extends Device implements BooleanOutput, Disableable {

    private final BooleanTextComponent actuated;
    private boolean savedValue = false, disabled = true;
    private boolean bypassDisabled;

    /**
     * Creates a new BooleanViewDevice with a label to describe the device.
     *
     * @param label how to describe the device.
     */
    public BooleanViewDevice(String label) {
        add(new SpacingComponent(20));
        add(new TextComponent(label));
        actuated = new BooleanTextComponent("DEACTUATED", "ACTUATED");
        add(actuated);
    }

    /**
     * Creates a new BooleanViewDevice with a label to describe the device and a
     * default value.
     *
     * @param label how to describe the device.
     * @param enabledByDefault the default value to display.
     */
    public BooleanViewDevice(String label, boolean enabledByDefault) {
        this(label);
        this.set(enabledByDefault);
    }

    /**
     * Sets this BooleanViewDevice to ignore whether the robot is in disabled
     * mode: this is useful for outputs that work regardless of enablement. If
     * this isn't called, then it is forced to false whenever the robot is
     * disabled.
     *
     * @return this BooleanViewDevice, for method chaining purposes.
     */
    public BooleanViewDevice setBypassDisabledMode() {
        notifyDisabled(false);
        bypassDisabled = true;
        return this;
    }

    @Override
    public void set(boolean value) {
        savedValue = value;
        if (!disabled) {
            actuated.safeSet(value);
        }
    }

    @Override
    public void notifyDisabled(boolean disabled) {
        if (bypassDisabled) {
            return;
        }
        this.disabled = disabled;
        if (disabled) {
            actuated.safeSet(false);
        } else {
            actuated.safeSet(savedValue);
        }
    }

    /**
     * Provides a BooleanInput representing the current state of this
     * BooleanViewDevice.
     *
     * @return the BooleanInput.
     */
    public BooleanInput asInput() {
        return actuated.asInput();
    }
}
