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

/**
 * An interface for devices that get disabled when the robot is disabled. This
 * lets them respond to the robot's state and know when to be disabled.
 * 
 * @author skeggsc
 */
public interface Disableable {

    /**
     * Notify this device about whether or not the robot is disabled.
     * 
     * @param disabled if the robot is now disabled.
     */
    public void notifyDisabled(boolean disabled);
}
