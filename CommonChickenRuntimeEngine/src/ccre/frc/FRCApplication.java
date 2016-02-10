/*
 * Copyright 2013-2015 Cel Skeggs
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
package ccre.frc;

/**
 * The core Application class for FRC. Implement this in order to write an
 * application to run on the robot. Use the FRC class to access hardware.
 *
 * @see FRC
 * @author skeggsc
 */
public interface FRCApplication {

    /**
     * Sets up the robot. This is called exactly once by the CCRE runtime.
     *
     * @throws Throwable if something fails during setup.
     */
    public void setupRobot() throws Throwable;
}
