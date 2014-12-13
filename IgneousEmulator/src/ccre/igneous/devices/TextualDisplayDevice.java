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

import ccre.igneous.Device;
import ccre.igneous.components.SpacingComponent;
import ccre.igneous.components.TextComponent;

public class TextualDisplayDevice extends Device {

    private final int height;
    private final TextComponent text;

    public TextualDisplayDevice(String string, int height) {
        this.height = height;
        add(new SpacingComponent(20));
        text = new TextComponent(string);
        add(text);
    }
    
    public void set(String str) {
        text.setLabel(str);
    }
    
    public int getHeight() {
        return height;
    }
}
