/*
 * Copyright 2013 Colby Skeggs and Vincent Miller
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
package ccre.obsidian;

/**
 * A simulated world that can interact with i/o in the Obsidian Emulator. This
 * should be used to modify the values of inputs, and is a compliment to the
 * core class.
 *
 * @author MillerV
 */
public class WorldModule {

    /**
     * Called by the Emulator ever ~20 milliseconds.
     *
     * @param gui the EmulatorGUI from which pins can be accessed.
     */
    public void periodic(EmulatorGUI gui) {

    }

    /**
     * Called by the Emulator when an output pin's value was changed.
     *
     * @param gui the EmulatorGUI from which pins can be accessed.
     * @param changed the pin whose value has changed.
     */
    public void outputChanged(EmulatorGUI gui, EmulatorPin changed) {

    }
}
