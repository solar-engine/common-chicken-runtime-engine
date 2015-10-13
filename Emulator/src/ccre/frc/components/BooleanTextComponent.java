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
package ccre.frc.components;

import java.awt.Color;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;

/**
 * A textual display component that displays one of two strings based on whether
 * the value is on or off. This can be optionally editable by the user by
 * clicking it - this is disabled by default.
 *
 * @author skeggsc
 */
public class BooleanTextComponent extends TextComponent implements BooleanOutput {

    private final String off, on;
    private final BooleanStatus state = new BooleanStatus();
    private boolean editable = false;

    /**
     * Create a new BooleanTextComponent with off and on as its strings for
     * being boolean TRUE or boolean FALSE.
     *
     * @param off the label to display when FALSE
     * @param on the label to display when TRUE
     */
    public BooleanTextComponent(String off, String on) {
        super(off, new String[] { off, on });
        this.off = off;
        this.on = on;
        setColor(Color.RED.darker());
    }

    /**
     * Create a new BooleanTextComponent that only displays label, regardless of
     * boolean state.
     *
     * @param label the label to always display
     */
    public BooleanTextComponent(String label) {
        this(label, label);
    }

    /**
     * Sets whether the component is editable by the user by clicking on this
     * component.
     *
     * @param editable if the component should be editable.
     * @return this component, for method chaining.
     */
    public BooleanTextComponent setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    @Override
    public void set(boolean value) {
        this.setLabel(value ? on : off);
        this.setColor(value ? Color.green : Color.RED.darker());
        state.set(value);
    }

    @Override
    public void onPress(int x, int y) {
        if (editable) {
            safeSet(!state.get());
            repaint();
        }
    }

    public BooleanInput asInput() {
        return state;
    }

    public boolean get() {
        return state.get();
    }
}
