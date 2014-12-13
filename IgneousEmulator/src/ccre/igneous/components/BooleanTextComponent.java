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
package ccre.igneous.components;

import java.awt.Color;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;

public class BooleanTextComponent extends TextComponent implements BooleanOutput, BooleanInputPoll {

    private final String off, on;
    private boolean state = false;
    private boolean editable = false;

    public BooleanTextComponent(String off, String on) {
        super(off, new String[] {off, on});
        this.off = off;
        this.on = on;
        setColor(Color.RED.darker());
    }
    
    public BooleanTextComponent(String string) {
        this(string, string);
    }

    public BooleanTextComponent setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public boolean get() {
        return state;
    }

    public void set(boolean value) {
        state = value;
        this.setLabel(value ? on : off);
        this.setColor(value ? Color.green : Color.RED.darker());
    }

    @Override
    public void onPress(int x, int y) {
        if (editable) {
            set(!get());
            repaint();
        }
    }
}
