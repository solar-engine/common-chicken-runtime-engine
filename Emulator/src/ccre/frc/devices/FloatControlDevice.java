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

import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.frc.Device;
import ccre.frc.components.ControlBarComponent;
import ccre.frc.components.SpacingComponent;
import ccre.frc.components.TextComponent;

/**
 * A device allowing control of a floating-point number.
 *
 * @author skeggsc
 */
public class FloatControlDevice extends Device {

    private final ControlBarComponent value;

    /**
     * Create a new FloatControlComponent with a label to describe this device.
     *
     * @param label how to describe this device.
     */
    public FloatControlDevice(String label) {
        this(label, -1.0f, 1.0f, 0.0f, 0.0f);
    }

    /**
     * Create a new FloatControlComponent with a label to describe this device
     * and specified minima, maxima, default, and origin.
     *
     * @param label how to describe this device.
     * @param min the minimum value (at the left of the slider)
     * @param max the maximum value (at the right of the slider)
     * @param defaultValue the default value
     * @param originValue the origin point
     */
    public FloatControlDevice(String label, float min, float max, float defaultValue, float originValue) {
        add(new SpacingComponent(20));
        add(new TextComponent(label));
        add(value = new ControlBarComponent(min, max, defaultValue, originValue).setMaxWidth(200));
    }

    /**
     * Queries the current value of this FloatControlDevice.
     *
     * @return the current value.
     */
    public float get() {
        return value.get();
    }

    /**
     * Provides access to this FloatControlDevice as a FloatInput.
     *
     * @return the FloatInput representing the value of this device.
     */
    public FloatInput asInput() {
        return value.asInput();
    }

    /**
     * Provides access to this FloatControlDevice as a FloatIO.
     *
     * @return the FloatIO representing the value of this device.
     */
    public FloatIO asIO() {
        return FloatIO.compose(value.asInput(), value);
    }
}
