/*
 * Copyright 2014 Colby Skeggs, Gregor Peach (Added Folders)
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
package intelligence.monitor;

import java.io.Serializable;

/**
 * An interface implemented both by the real and fake PhidgetMonitors.
 *
 * @author skeggsc
 */
public interface IPhidgetMonitor extends Serializable {

    /**
     * Share all the inputs and outputs and the current attachment state over
     * the network, if available, and set up anything else required.
     */
    public void share();

    /**
     * Unshare (remove) all the inputs and outputs and the current attachment
     * state from the network.
     */
    public void unshare();

    /**
     * Called when the connection becomes online, so the LCD screen can be
     * updated.
     */
    public void connectionUp();

    /**
     * Called when the connection becomes offline, so the LCD screen can be
     * updated.
     */
    public void connectionDown();

    /**
     * Called when the program is about to shut down, so the LCD screen can be
     * updated.
     */
    public void displayClosing();
}
