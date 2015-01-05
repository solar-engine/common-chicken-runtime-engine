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
import ccre.channel.BooleanOutput;
import ccre.igneous.Device;
import ccre.igneous.components.BooleanTextComponent;
import ccre.igneous.components.SpacingComponent;
import ccre.igneous.components.TextComponent;

/**
 * A device representing some sort of boolean readout, such as a Solenoid or
 * digital output.
 * 
 * @author skeggsc
 */
public class BooleanViewDevice extends Device implements BooleanOutput, BooleanInputPoll {

    private final BooleanTextComponent actuated = new BooleanTextComponent("DEACTUATED", "ACTUATED");

    /**
     * Create a new BooleanViewDevice with a label to describe the device.
     * 
     * @param label how to describe the device.
     */
    public BooleanViewDevice(String label) {
        add(new SpacingComponent(20));
        add(new TextComponent(label));
        add(actuated);
    }

    public void set(boolean value) {
        actuated.set(value);
    }

    public boolean get() {
        return actuated.get();
    }
}
