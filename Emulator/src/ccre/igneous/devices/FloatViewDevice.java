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

import ccre.channel.FloatOutput;
import ccre.igneous.Device;
import ccre.igneous.components.FillBarComponent;
import ccre.igneous.components.SpacingComponent;
import ccre.igneous.components.TextComponent;

/**
 * A device to view the value of a floating-point number.
 *
 * @author skeggsc
 */
public class FloatViewDevice extends Device implements FloatOutput, Disableable {

    private final FillBarComponent value = new FillBarComponent();
    private final float minInput, maxInput;
    private float savedValue;
    private boolean disabled = true;

    /**
     * Create a new FloatViewDevice with a label where the minimum value is -1.0
     * and the maximum value is 1.0.
     *
     * @param label the name of this device.
     */
    public FloatViewDevice(String label) {
        this(label, -1, 1);
    }

    /**
     * Create a new FloatViewDevice with a label and a given minimum and maximum
     * value.
     *
     * @param label the name of this device.
     * @param minInput the minimum displayed value.
     * @param maxInput the maximum displayed value.
     */
    public FloatViewDevice(String label, float minInput, float maxInput) {
        add(new SpacingComponent(20));
        add(new TextComponent(label));
        add(value);
        this.minInput = minInput;
        this.maxInput = maxInput;
    }

    public void set(float value) {
        float rvalue = 2 * (value - minInput) / (maxInput - minInput) - 1;
        savedValue = rvalue;
        if (!disabled) {
            this.value.set(rvalue);
        }
    }

    @Override
    public void notifyDisabled(boolean disabled) {
        this.disabled = disabled;
        if (disabled) {
            this.value.set(0);
        } else {
            this.value.set(savedValue);
        }
    }
}
