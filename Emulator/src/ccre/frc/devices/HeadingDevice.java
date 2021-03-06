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

import ccre.frc.Device;
import ccre.frc.components.SpacingComponent;
import ccre.frc.components.TextComponent;

/**
 * A device simply used before a list of other devices to give a title.
 *
 * @author skeggsc
 */
public class HeadingDevice extends Device {

    private final TextComponent text;

    /**
     * Creates a new HeadingDevice with a fixed title.
     *
     * @param title the title to display.
     */
    public HeadingDevice(String title) {
        add(new SpacingComponent(30));
        text = new TextComponent(title);
        add(text);
    }

    /**
     * Changes the displayed heading.
     *
     * @param title the new displayed heading.
     */
    public void setHeading(String title) {
        text.setLabel(title);
    }
}
