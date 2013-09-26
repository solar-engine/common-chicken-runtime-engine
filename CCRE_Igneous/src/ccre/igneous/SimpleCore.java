/*
 * Copyright 2013 Colby Skeggs
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
package ccre.igneous;

import ccre.ctrl.IDispatchJoystick;

/**
 * Provides a wrapper over IgneousCore that provides the joysticks as
 * easy-to-access objects.
 *
 * @author skeggsc
 */
public abstract class SimpleCore extends IgneousCore {

    /**
     * The first joystick attached to the driver station.
     */
    protected IDispatchJoystick joystick1;
    /**
     * The second joystick attached to the driver station.
     */
    protected IDispatchJoystick joystick2;
    /**
     * The third joystick attached to the driver station.
     */
    protected IDispatchJoystick joystick3;
    /**
     * The fourth joystick attached to the driver station.
     */
    protected IDispatchJoystick joystick4;

    /**
     * Implement this method - it should set up everything that your robot needs to do.
     */
    protected abstract void createSimpleControl();

    /**
     * Sets up the joysticks and then calls createSimpleControl.
     */
    protected final void createRobotControl() {
        joystick1 = makeDispatchJoystick(1);
        joystick2 = makeDispatchJoystick(2);
        joystick3 = makeDispatchJoystick(3);
        joystick4 = makeDispatchJoystick(4);
        createSimpleControl();
    }
}
