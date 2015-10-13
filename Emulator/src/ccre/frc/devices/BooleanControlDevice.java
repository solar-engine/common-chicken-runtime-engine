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
package ccre.frc.devices;

import ccre.channel.BooleanInput;
import ccre.channel.EventInput;
import ccre.channel.EventStatus;
import ccre.frc.Device;
import ccre.frc.components.BooleanTextComponent;
import ccre.frc.components.SpacingComponent;
import ccre.frc.components.TextComponent;

/**
 * A device representing some sort of boolean that can be modified, such as a
 * Joystick button or digital input.
 *
 * @author skeggsc
 */
public class BooleanControlDevice extends Device {

    private final EventStatus pressEvent = new EventStatus();
    private final BooleanTextComponent actuated = new BooleanTextComponent("INACTIVE", "ACTIVE") {
        @Override
        public void onPress(int x, int y) {
            boolean wasDown = get();
            super.onPress(x, y);
            if (!wasDown && get()) {
                pressEvent.safeEvent();
                repaint();
            }
        }
    }.setEditable(true);

    /**
     * Create a new BooleanControlDevice with label as the displayed name.
     *
     * @param label what to call the label.
     */
    public BooleanControlDevice(String label) {
        add(new SpacingComponent(20));
        add(new TextComponent(label));
        add(actuated);
    }

    /**
     * Returns an event representing when the BooleanControlDevice is pressed.
     *
     * @return an EventInput of when this is pressed.
     */
    public EventInput whenPressed() {
        return pressEvent;
    }

    public BooleanInput asInput() {
        return actuated.asInput();
    }
}
