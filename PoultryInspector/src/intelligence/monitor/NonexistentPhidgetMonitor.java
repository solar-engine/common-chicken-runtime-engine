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

/**
 * An IPhidgetMonitor that doesn't actually publish anything.
 *
 * @author skeggsc
 */
public class NonexistentPhidgetMonitor implements IPhidgetMonitor {

    private static final long serialVersionUID = -4203403857863877908L;

    @Override
    public void share() {
    }

    @Override
    public void unshare() {
    }

    @Override
    public void connectionUp() {
    }

    @Override
    public void connectionDown() {
    }

    @Override
    public void displayClosing() {
    }

}
