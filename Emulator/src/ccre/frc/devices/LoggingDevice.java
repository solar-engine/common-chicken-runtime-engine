/*
 * Copyright 2015 Cel Skeggs
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
import ccre.frc.components.TextBoxComponent;
import ccre.log.LogLevel;
import ccre.log.LoggingTarget;

/**
 * A device to display a chunk of text.
 *
 * @author skeggsc
 */
public class LoggingDevice extends Device implements LoggingTarget {

    private final int height;
    private final TextBoxComponent text;

    /**
     * Create a new empty LoggingDevice with a specified height.
     *
     * @param height the wanted height of the device.
     */
    public LoggingDevice(int height) {
        this.height = height;
        add(new SpacingComponent(20));
        text = new TextBoxComponent();
        add(text);
    }

    /**
     * Add a line to the displayed text.
     *
     * @param line the additional text to display.
     */
    public void addLine(String line) {
        text.addLine(line);
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        if (throwable == null) {
            addLine("[" + level + "] " + message);
        } else {
            addLine("[" + level + "] " + message + ": " + throwable.getMessage());
        }
    }

    @Override
    public void log(LogLevel level, String message, String extended) {
        if (extended == null) {
            addLine("[" + level + "] " + message);
        } else {
            addLine("[" + level + "] " + message + ": " + extended.split("\n")[0]);
        }
    }

    /**
     * Remove all displayed text.
     */
    public void clearLines() {
        text.clearLines();
    }
}
