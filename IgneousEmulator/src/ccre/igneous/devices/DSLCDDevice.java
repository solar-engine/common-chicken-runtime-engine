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

import ccre.igneous.DeviceGroup;

public class DSLCDDevice extends DeviceGroup {

    private TextualDisplayDevice[] lines = new TextualDisplayDevice[6];

    public DSLCDDevice() {
        add(new HeadingDevice("Driver Station LCD"));
        for (int i = 0; i < lines.length; i++) {
            add(lines[i] = new TextualDisplayDevice("....................", 30));
        }
    }

    public void update(int lineid, String value) {
        if (lineid < 1 || lineid > lines.length) {
            throw new IllegalArgumentException("Invalid DS LCD line: " + lineid);
        }
        lines[lineid - 1].set(value);
    }
}
